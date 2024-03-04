package com.tt.mj.wss.user;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.tt.mj.domain.DiscordAccount;
import com.tt.mj.enums.MessageType;
import com.tt.mj.wss.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
public class UserMessageListener {
	private final DiscordAccount account;
	private final List<MessageHandler> messageHandlers;

	@Resource
	private MessageHandler userWebSocketStarter;

	public UserMessageListener(DiscordAccount account, List<MessageHandler> messageHandlers) {
		this.account = account;
		this.messageHandlers = messageHandlers;
	}
	public void onMessage(DataObject raw) {
		MessageType messageType = MessageType.of(raw.getString("t"));
		if (messageType == null || MessageType.DELETE == messageType) {
			return;
		}
		DataObject data = raw.getObject("d");
		if (ignoreAndLogMessage(data, messageType)) {
			return;
		}
		ThreadUtil.sleep(50);

		for (MessageHandler messageHandler : this.messageHandlers) {
			messageHandler.handle(messageType, data);
		}
	}

	private boolean ignoreAndLogMessage(DataObject data, MessageType messageType) {
		String channelId = data.getString("channel_id");
		if (!CharSequenceUtil.equals(channelId, this.account.getMjBotId())) {
			return true;
		}
		String authorName = data.optObject("author").map(a -> a.getString("username")).orElse("System");
		log.debug("wss-receive" + data);
		log.debug("{} - {} - {}: {}", this.account.getDisplay(), messageType.name(), authorName, data.opt("content").orElse(""));
		return false;
	}

}
