package com.tt.mj.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tt.mj.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("Discord账号")
public class DiscordAccount extends DomainObject {

	@ApiModelProperty("服务器ID")
	private String guildId;
	@ApiModelProperty("频道ID")
	private String channelId;
	@ApiModelProperty("用户Token")
	private String token;
	@ApiModelProperty("用户UserAgent")
	private String userAgent = Constants.DEFAULT_DISCORD_USER_AGENT;
	@ApiModelProperty("是否可用")
	private boolean enable = true;
	@ApiModelProperty("并发数")
	private int coreSize = 3;
	@ApiModelProperty("等待队列长度")
	private int queueSize = 10;
	@ApiModelProperty("任务超时时间(分钟)")
	private int timeoutMinutes = 5;

	/**
	 * 代理IP
	 */
	private String proxyIp;

	/**
	 * 代理端口
	 */
	private Integer proxyPort;

	private Long accountId;

	/**
	 * 账号类型  1-fast  4-relax
	 */
	private Integer accountType;

	//mj机器人id
	private String mjBotId;
	@JsonIgnore
	public String getDisplay() {
		return this.channelId;
	}
}
