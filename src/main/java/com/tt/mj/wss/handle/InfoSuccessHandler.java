package com.tt.mj.wss.handle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tt.mj.entity.Account;
import com.tt.mj.enums.MessageType;
import com.tt.mj.mapper.AccountMapper;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * describe消息处理.
 */
@Component
public class InfoSuccessHandler extends MessageHandler {

	@Autowired
	private AccountMapper accountMapper;


	@Override
	public void handle(MessageType messageType, DataObject message) {
		Optional<DataObject> interaction = message.optObject("interaction");
		if (!MessageType.UPDATE.equals(messageType) || interaction.isEmpty() || !"info".equals(interaction.get().getString("name"))) {
			return;
		}
		DataArray embeds = message.getArray("embeds");
		if (embeds.isEmpty()) {
			return;
		}
		String description = embeds.getObject(0).getString("description");
		String channelId = message.getString("channel_id");

		if(description.isBlank() || channelId.isBlank()){
			return;
		}
		Account account = accountMapper.selectOne(
				new QueryWrapper<Account>().lambda()
						.eq(Account::getMjBotId, channelId)
		);
		account.setDiscordInfo(description);
		account.setUpdatedAt(new Date());
		accountMapper.updateById(account);

//		System.out.println("desc" + description);
//		System.out.println("channelId" + channelId);
	}

}
