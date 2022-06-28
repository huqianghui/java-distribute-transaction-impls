package com.seattle.msready.mq.transaction.compensating.tx.client;

import org.slf4j.helpers.MessageFormatter;

public class ClientException extends Exception {
	
	private static final long serialVersionUID = -6904441291482175833L;

	private String code;
	
	private Object[] args;
	
	public ClientException(Throwable cause, String code, String message, Object... args) {
		super(message, cause);
		this.code = code;
		this.args = args;
	}

	public ClientException(String code, String message, Object... args) {
		super(message);
		this.code = code;
		this.args = args;
	}
	
	public String getMessage() {
		return "[" + code + "] - " +MessageFormatter.arrayFormat(super.getMessage(), args).getMessage() ;
	}
	
	public String getCode() {
		return this.code;
	}
}
