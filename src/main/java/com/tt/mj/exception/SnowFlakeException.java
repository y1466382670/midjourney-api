package com.tt.mj.exception;

public class SnowFlakeException extends RuntimeException {

	public SnowFlakeException(String message) {
		super(message);
	}

	public SnowFlakeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SnowFlakeException(Throwable cause) {
		super(cause);
	}
}
