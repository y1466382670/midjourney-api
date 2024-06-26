package com.tt.mj.support;

import cn.hutool.core.exceptions.ValidateException;
import com.tt.mj.ReturnCode;
import com.tt.mj.domain.DiscordAccount;
import com.tt.mj.loadbalancer.DiscordInstance;
import com.tt.mj.loadbalancer.DiscordLoadBalancer;
import com.tt.mj.mapper.AccountMapper;
import com.tt.mj.util.AsyncLockUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordAccountInitializer implements ApplicationRunner {
	private final DiscordLoadBalancer discordLoadBalancer;
	private final DiscordAccountHelper discordAccountHelper;
	private final AccountMapper accountMapper;

	@Override
	public void run(ApplicationArguments args) {
		log.debug("初始化：DiscordAccountInitializer run");
		List<DiscordAccount> accounts = accountMapper.selectList(null);

		List<DiscordInstance> instances = this.discordLoadBalancer.getAllInstances();
		for (DiscordAccount account : accounts) {
			try {
				DiscordInstance instance = this.discordAccountHelper.createDiscordInstance(account);
				if (!account.isEnable()) {
					continue;
				}
				instance.startWss();
				AsyncLockUtils.LockObject lock = AsyncLockUtils.waitForLock("wss:" + account.getChannelId(), Duration.ofSeconds(10));
				if (ReturnCode.SUCCESS != lock.getProperty("code", Integer.class, 0)) {
					throw new ValidateException(lock.getProperty("description", String.class));
				}
				instances.add(instance);
			} catch (Exception e) {
				log.error("Account({}) init fail, disabled: {}", account.getDisplay(), e.getMessage());
				account.setEnable(false);
			}
		}
		Set<String> enableInstanceIds = instances.stream().filter(DiscordInstance::isAlive).map(DiscordInstance::getInstanceId).collect(Collectors.toSet());
		log.info("当前可用账号数 [{}] - {}", enableInstanceIds.size(), String.join(", ", enableInstanceIds));
	}

}
