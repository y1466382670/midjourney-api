package com.tt.mj;

import com.tt.mj.enums.TranslateWay;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@Component
@ConfigurationProperties(prefix = "mj")
public class ProxyProperties {
	/**
	 * discord账号选择规则.
	 */
	private String accountChooseRule = "BestWaitIdleRule";
	/**
	 * 百度翻译配置.
	 */
	private final BaiduTranslateConfig baiduTranslate = new BaiduTranslateConfig();
	/**
	 * openai配置.
	 */
	private final OpenaiConfig openai = new OpenaiConfig();
	/**
	 * 中文prompt翻译方式.
	 */
	private TranslateWay translateWay = TranslateWay.NULL;
	/**
	 * 接口密钥，为空不启用鉴权；调用接口时需要加请求头 SECRET-KEY.
	 */
	private String apiSecret;
	/**
	 * 通知回调线程池大小.
	 */
	private int notifyPoolSize = 10;

	private final ProxyConfig proxy = new ProxyConfig();
	/**
	 * 反代配置.
	 */
	private final NgDiscordConfig ngDiscord = new NgDiscordConfig();

	@Data
	public static class BaiduTranslateConfig {
		/**
		 * 百度翻译的APP_ID.
		 */
		private String appid;
		/**
		 * 百度翻译的密钥.
		 */
		private String appSecret;
	}

	@Data
	public static class OpenaiConfig {
		/**
		 * 自定义gpt的api-url.
		 */
		private String gptApiUrl;
		/**
		 * gpt的api-key.
		 */
		private String gptApiKey;
		/**
		 * 超时时间.
		 */
		private Duration timeout = Duration.ofSeconds(30);
		/**
		 * 使用的模型.
		 */
		private String model = "gpt-3.5-turbo";
		/**
		 * 返回结果的最大分词数.
		 */
		private int maxTokens = 2048;
		/**
		 * 相似度，取值 0-2.
		 */
		private double temperature = 0;
	}

	@Data
	public static class NgDiscordConfig {
		/**
		 * https://discord.com 反代.
		 */
		private String server;
		/**
		 * https://cdn.discordapp.com 反代.
		 */
		private String cdn;
		/**
		 * wss://gateway.discord.gg 反代.
		 */
		private String wss;
		/**
		 * https://discord-attachments-uploads-prd.storage.googleapis.com 反代.
		 */
		private String uploadServer;
	}

	@Data
	public static class ProxyConfig {
		/**
		 * 代理host.
		 */
		private String host;
		/**
		 * 代理端口.
		 */
		private Integer port;
	}

}
