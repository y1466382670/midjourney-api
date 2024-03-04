package com.tt.mj.loadbalancer.rule;

import com.tt.mj.loadbalancer.DiscordInstance;

import java.util.List;

public interface IRule {

	DiscordInstance choose(List<DiscordInstance> instances);
}
