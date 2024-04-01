package config;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import com.tt.mj.ProxyProperties;
import com.tt.mj.loadbalancer.rule.IRule;
import com.tt.mj.service.NotifyService;
import com.tt.mj.service.TranslateService;
import com.tt.mj.service.translate.BaiduTranslateServiceImpl;
import com.tt.mj.service.translate.GPTTranslateServiceImpl;
import com.tt.mj.service.translate.NoTranslateServiceImpl;
import com.tt.mj.support.DiscordAccountHelper;
import com.tt.mj.support.DiscordHelper;
import com.tt.mj.wss.handle.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BeanConfig {
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private ProxyProperties properties;

	@Bean
	TranslateService translateService() {
		return switch (this.properties.getTranslateWay()) {
			case BAIDU -> new BaiduTranslateServiceImpl(this.properties.getBaiduTranslate());
			case GPT -> new GPTTranslateServiceImpl(this.properties);
			default -> new NoTranslateServiceImpl();
		};
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public IRule loadBalancerRule() {
		String ruleClassName = IRule.class.getPackageName() + "." + this.properties.getAccountChooseRule();
		return ReflectUtil.newInstance(ruleClassName);
	}

	@Bean
	List<MessageHandler> messageHandlers() {
		return this.applicationContext.getBeansOfType(MessageHandler.class).values().stream().toList();
	}

	@Bean
	DiscordAccountHelper discordAccountHelper(DiscordHelper discordHelper, NotifyService notifyService) throws IOException {
		var resources = this.applicationContext.getResources("classpath:api-params/*.json");
		Map<String, String> paramsMap = new HashMap<>();
		for (var resource : resources) {
			String filename = resource.getFilename();
			String params = IoUtil.readUtf8(resource.getInputStream());
			paramsMap.put(filename.substring(0, filename.length() - 5), params);
		}
		return new DiscordAccountHelper(discordHelper, restTemplate(), notifyService, messageHandlers(), paramsMap);
	}
}
