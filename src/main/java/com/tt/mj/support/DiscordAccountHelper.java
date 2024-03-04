package com.tt.mj.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.tt.mj.Constants;
import com.tt.mj.config.MyProxyConfig;
import com.tt.mj.domain.DiscordAccount;
import com.tt.mj.loadbalancer.DiscordInstance;
import com.tt.mj.loadbalancer.DiscordInstanceImpl;
import com.tt.mj.service.NotifyService;
import com.tt.mj.wss.handle.MessageHandler;
import com.tt.mj.wss.user.UserMessageListener;
import com.tt.mj.wss.user.UserWebSocketStarter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DiscordAccountHelper {
	private final DiscordHelper discordHelper;
	private final RestTemplate restTemplate;
	private final NotifyService notifyService;
	private final List<MessageHandler> messageHandlers;
	private final Map<String, String> paramsMap;

	public DiscordInstance createDiscordInstance(DiscordAccount account) {
		if (!CharSequenceUtil.isAllNotBlank(account.getChannelId(), account.getToken())) {
			throw new IllegalArgumentException("channelId, userToken must not be blank");
		}
		MyProxyConfig myProxyConfig = new MyProxyConfig();
		myProxyConfig.setHost(account.getProxyIp());
		myProxyConfig.setPort(account.getProxyPort());

		if (CharSequenceUtil.isBlank(account.getUserAgent())) {
			account.setUserAgent(Constants.DEFAULT_DISCORD_USER_AGENT);
		}
		var messageListener = new UserMessageListener(account, this.messageHandlers);
		var webSocketStarter = new UserWebSocketStarter(this.discordHelper.getWss(), account, messageListener, myProxyConfig);

		return new DiscordInstanceImpl(account, webSocketStarter, this.restTemplate,
				this.notifyService, this.discordHelper.getServer(), this.paramsMap);
	}

}
