package com.tt.mj.result;

import lombok.Data;

/**
 * 响应结果
 */
@Data
public class ResultJson<T> {

	private String status;

	private String message;

	private T data;

	/**
	 * 请求成功,不带响应数据
	 */
	public  ResultJson success() {
		this.status = "SUCCESS";
		this.message = "success";
		return this;
	}

	/**
	 * 请求成功，带响应数据
	 * @param data
	 */
	public ResultJson success(T data) {
		this.status = "SUCCESS";
		this.message = "success";
		this.data = data;
		return this;
	}

	public ResultJson message(String message) {
		this.message = message;
		return this;
	}


	/**
	 * 自定义响应状态码及响应信息
	 */
	public ResultJson fail(String msg) {
		this.status = "FAILED";
		this.message = msg;
		return this;
	}

	/**
	 * 自定义响应状态码及响应信息
	 */
	public ResultJson fail(String msg, T data) {
		this.status = "FAILED";
		this.message = msg;
		this.data = data;
		return this;
	}

}
