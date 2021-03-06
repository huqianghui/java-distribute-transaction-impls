package com.seattle.msready.mq.transaction.compensating.tx.client;

public interface ClientExceptionCode {

	String CLIENT_ERR_TIMEOUT="CLIENT_ERR_TIMEOUT";
	
	String CLIENT_ERR_CONFIG = "CLIENT_ERR_CONFIG";
	
	String CLIENT_ERR_PARAM = "CLIENT_ERR_PARAM";
	
	String CLIENT_ERR_JMS = "CLIENT_ERR_JMS";
	
	String CLIENT_ERR_UNSUPPORTED_MESSAGE_TYPE = "CLIENT_ERR_UNSUPPORTED_MESSAGE_TYPE";
	
	String CLIENT_ERR_DUPLICATE_LISTEN = "CLIENT_ERR_DUPLICATE_LISTEN";
}
