package com.tt.mj.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tt.mj.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("account")
public class DiscordAccount extends DomainObject {

	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 账号
	 */
	private String account;

	/**
	 * 频道ID.
	 */
	private String channelId;

	/**
	 * 用户Token.
	 */
	private String token;

	/**
	 * 用户UserAgent.
	 */
	private String userAgent = Constants.DEFAULT_DISCORD_USER_AGENT;

	/**
	 * 代理IP
	 */
	private String proxyIp;

	/**
	 * 代理端口
	 */
	private Integer proxyPort;

	/**
	 * 最大队列长度
	 */
	private int maxJob;

	/**
	 * 最多同时执行任务数
	 */
	private int maxQueue;

	/**
	 * 账号是否可用
	 */
	private boolean enable = true;

	@JsonIgnore
	public String getDisplay() {
		return this.channelId;
	}
}
