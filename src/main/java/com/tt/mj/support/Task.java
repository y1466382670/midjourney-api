package com.tt.mj.support;

import com.tt.mj.domain.DomainObject;
import com.tt.mj.enums.TaskStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class Task extends DomainObject {
	@Serial
	private static final long serialVersionUID = -674915748204390789L;

	private String id;

	/**
	 * 任务类型
	 */
	private String action;
	/**
	 * 任务状态
	 */
	private TaskStatus status = TaskStatus.NOT_START;
	/**
	 * 提示词
	 */
	private String prompt;
	/**
	 * 提示词-英文
	 */
	private String promptEn;
	/**
	 * 任务描述
	 */
	private String description;
	/**
	 * 自定义参数
	 */
	private String state;
	/**
	 * 提交时间
	 */
	private Long submitTime;
	/**
	 * 开始执行时间
	 */
	private Long startTime;
	/**
	 * 结束时间
	 */
	private Long finishTime;
	/**
	 * 图片url
	 */
	private String imageUrl;
	/**
	 * 任务进度
	 */
	private String progress;
	/**
	 * 失败原因
	 */
	private String failReason;

	public void start() {
		this.startTime = System.currentTimeMillis();
		this.status = TaskStatus.SUBMITTED;
		this.progress = "0%";
	}

	public void success() {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.SUCCESS;
		this.progress = "100%";
	}

	public void fail(String reason) {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.FAILURE;
		this.failReason = reason;
		this.progress = "";
	}
}
