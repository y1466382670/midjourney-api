package com.tt.mj.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tt.mj.ProxyProperties;
import com.tt.mj.domain.DiscordAccount;
import com.tt.mj.entity.LogModel;
import com.tt.mj.enums.MessageType;
import com.tt.mj.enums.StatusEnum;
import com.tt.mj.mapper.AccountMapper;
import com.tt.mj.mapper.LogModelMapper;
import com.tt.mj.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class ErrorMessageHandler extends MessageHandler {
	@Autowired
	protected ProxyProperties properties;

	@Autowired
	private LogModelMapper logModelMapper;

	@Autowired
	private NotifyService notifyService;

	@Autowired
	private AccountMapper accountMapper;

	@Override
	public void handle(MessageType messageType, DataObject message) {
		Optional<DataArray> embedsOptional = message.optArray("embeds");
		if (embedsOptional.isEmpty() || embedsOptional.get().isEmpty()) {
			return;
		}
		DataObject embed = embedsOptional.get().getObject(0);
		String title = embed.getString("title", null);
		String description = embed.getString("description", null);
		String footerText = "";
		Optional<DataObject> footer = embed.optObject("footer");
		if (footer.isPresent()) {
			footerText = footer.get().getString("text", "");
		}
		String channelId = message.getString("channel_id", "");
		int color = embed.getInt("color", 0);
		if (color == 16239475) {
			log.warn("{} - MJ警告信息: {}\n{}\nfooter: {}", channelId, title, description, footerText);
		} else if (color == 16711680) {
			log.error("{} - MJ异常信息: {}\n{}\nfooter: {}", channelId, title, description, footerText);
			log.error("{} - MJ异常信息message: {}\n", channelId, message);

			DiscordAccount account = accountMapper.selectOne(
					new QueryWrapper<DiscordAccount>().lambda().eq(DiscordAccount::getChannelId, channelId)
			);
			account.setEnable(false);
			accountMapper.updateById(account);

			if(MessageType.UPDATE.equals(messageType)){
				String progressId = message.getString("id");
				if(StrUtil.isNotBlank(progressId)){
					dealByProgressId(progressId, null, "[" + title + "] " + description);
				}
			} else if (MessageType.CREATE.equals(messageType)) {
				//可能是U操作 也可能是imagine
				log.error("{} - MJ异常信息messageCreate: {}\n", channelId, message);
				String progressId = getMessageReferenceDataObject(message);
				String nonce = getMessageNonce(message);
				dealByProgressId(progressId, nonce, "[" + title + "] " + description);
			}
		} else if (CharSequenceUtil.contains(title, "Invalid link")) {
			// 兼容 Invalid link! 错误
			log.error("{} - MJ异常信息: {}\n{}\nfooter: {}", channelId, title, description, footerText);
			DataObject messageReference = message.optObject("message_reference").orElse(DataObject.empty());

			log.error("{} - MJ异常信息message_reference: {}\n", channelId, messageReference);

			String referenceMessageId = messageReference.getString("message_id", "");

			if (CharSequenceUtil.isBlank(referenceMessageId)) {
				return;
			}
			dealByProgressId(referenceMessageId, null,"[" + title + "] " + description);
		}
	}

	public void dealByProgressId(String progressId, String nonce, String message){
		LogModel logModel = logModelMapper.selectOne(
				new QueryWrapper<LogModel>().lambda().eq(LogModel::getProgressMessageId, progressId)
		);
		if(logModel == null){
			logModel = logModelMapper.selectOne(
					new QueryWrapper<LogModel>().lambda().eq(LogModel::getNonce, nonce)
			);
		}
		if(logModel != null){
			logModel.setStatus(StatusEnum.FAILED.getCode());
			logModel.setMessage(message);
			logModelMapper.updateById(logModel);
			notifyService.hookUrl(logModel);
		}
	}



}
