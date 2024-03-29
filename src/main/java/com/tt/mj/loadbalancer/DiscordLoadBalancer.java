package com.tt.mj.loadbalancer;

import cn.hutool.core.text.CharSequenceUtil;
import com.tt.mj.Constants;
import com.tt.mj.loadbalancer.rule.IRule;
import com.tt.mj.support.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

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

	public Task getRunningTask(String jobId) {
		for (DiscordInstance instance : getAliveInstances()) {
			Optional<Task> optional = instance.getRunningTasks().stream().filter(t -> jobId.equals(t.getProperty(Constants.TASK_PROPERTY_JOB_ID))).findFirst();
			if (optional.isPresent()) {
				return optional.get();
			}
		}
		return null;
	}

}
