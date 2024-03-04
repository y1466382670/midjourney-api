package com.tt.mj.wss;

import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.tt.mj.config.MyProxyConfig;
import org.apache.logging.log4j.util.Strings;

public interface WebSocketStarter {

	void setTrying(boolean trying);

	void start() throws Exception;

	default WebSocketFactory createWebSocketFactory(MyProxyConfig proxy) {
		WebSocketFactory webSocketFactory = new WebSocketFactory().setConnectionTimeout(10000);
		if (Strings.isNotBlank(proxy.getHost())) {
			ProxySettings proxySettings = webSocketFactory.getProxySettings();
			proxySettings.setHost(proxy.getHost());
			proxySettings.setPort(proxy.getPort());
		}
		return webSocketFactory;
	}
}
