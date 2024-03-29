package com.tt.mj.wss.handle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tt.mj.entity.LogModel;
import com.tt.mj.enums.MessageType;
import com.tt.mj.enums.StatusEnum;
import com.tt.mj.mapper.LogModelMapper;
import com.tt.mj.service.NotifyService;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Optional;

/**
 * describe消息处理.
 */
@Component
public class DescribeSuccessHandler extends MessageHandler {

	@Autowired
	private LogModelMapper logModelMapper;

	@Autowired
	private NotifyService notifyService;

	@Override
	public void handle(MessageType messageType, DataObject message) {
		Optional<DataObject> interaction = message.optObject("interaction");
		if (!MessageType.UPDATE.equals(messageType) || interaction.isEmpty() || !"describe".equals(interaction.get().getString("name"))) {
			return;
		}
		DataArray embeds = message.getArray("embeds");
		if (embeds.isEmpty()) {
			return;
		}
		String description = embeds.getObject(0).getString("description");

		Optional<DataObject> imageOptional = embeds.getObject(0).optObject("image");
		if (imageOptional.isEmpty()) {
			return;
		}
		String messageId = message.getString("id", "");
		LogModel logModel = logModelMapper.selectOne(new QueryWrapper<LogModel>().lambda()
				.eq(LogModel::getProgressMessageId, messageId)
				.notIn(LogModel::getStatus, 2,3)
		);
		if (!ObjectUtils.isEmpty(logModel)) {
			logModel.setPrompt(description);
			logModel.setStatus(StatusEnum.SUCCESS.getCode());
			logModel.setMessageId(messageId);
			logModel.setProgress("100");
			logModel.setUpdatedAt(new Date());
			int updateLine = logModelMapper.updateById(logModel);
			if (updateLine > 0) {
				//回调接口
				notifyService.hookUrl(logModel);
				finishTask(logModel.getJobId());
			}
		}
	}

}
