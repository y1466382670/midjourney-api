package com.tt.mj;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ReturnCode {
	/**
	 * 成功.
	 */
	public static final int SUCCESS = 1;
	/**
	 * 数据未找到.
	 */
	public static final int NOT_FOUND = 3;
	/**
	 * 校验错误.
	 */
	public static final int VALIDATION_ERROR = 4;
	/**
	 * 系统异常.
	 */
	public static final int FAILURE = 9;
}