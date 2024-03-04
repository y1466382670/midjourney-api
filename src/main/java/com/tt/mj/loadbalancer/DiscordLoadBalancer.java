package com.tt.mj.loadbalancer;

import cn.hutool.core.text.CharSequenceUtil;
import com.tt.mj.domain.DiscordAccount;
import com.tt.mj.loadbalancer.rule.IRule;
import com.tt.mj.support.Task;
import com.tt.mj.support.TaskCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class DiscordLoadBalancer {
	private final IRule rule;

	private final List<DiscordInstance> instances = Collections.synchronizedList(new ArrayList<>());

	public List<DiscordInstance> getAllInstances() {
		return this.instances;
	}

	public List<DiscordInstance> getAliveInstances() {
		return this.instances.stream().filter(DiscordInstance::isAlive).toList();
	}

	public void remove(DiscordAccount discordAccount) {
		instances.removeIf(d -> d.account().getChannelId().equals(discordAccount.getChannelId()));
	}

	public DiscordInstance chooseInstance() {
		return this.rule.choose(getAliveInstances());
	}

	public DiscordInstance getDiscordInstance(String instanceId) {
		if (CharSequenceUtil.isBlank(instanceId)) {
			return null;
		}
		return this.instances.stream()
				.filter(instance -> CharSequenceUtil.equals(instanceId, instance.getInstanceId()))
				.findFirst().orElse(null);
	}

	public Set<String> getQueueTaskIds() {
		Set<String> taskIds = Collections.synchronizedSet(new HashSet<>());
		for (DiscordInstance instance : getAliveInstances()) {
			taskIds.addAll(instance.getRunningFutures().keySet());
		}
		return taskIds;
	}

	public Stream<Task> findRunningTask(TaskCondition condition) {
		return getAliveInstances().stream().flatMap(instance -> instance.getRunningTasks().stream().filter(condition));
	}

	public Task getRunningTask(String id) {
		for (DiscordInstance instance : getAliveInstances()) {
			Optional<Task> optional = instance.getRunningTasks().stream().filter(t -> id.equals(t.getId())).findFirst();
			if (optional.isPresent()) {
				return optional.get();
			}
		}
		return null;
	}

	public Task getRunningTaskByNonce(String nonce) {
		if (CharSequenceUtil.isBlank(nonce)) {
			return null;
		}
		TaskCondition condition = new TaskCondition().setNonce(nonce);
		for (DiscordInstance instance : getAliveInstances()) {
			Optional<Task> optional = instance.getRunningTasks().stream().filter(condition).findFirst();
			if (optional.isPresent()) {
				return optional.get();
			}
		}
		return null;
	}

}
