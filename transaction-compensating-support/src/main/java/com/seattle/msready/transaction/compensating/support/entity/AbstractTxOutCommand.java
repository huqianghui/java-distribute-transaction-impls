package com.seattle.msready.transaction.compensating.support.entity;

import java.util.Date;

public abstract class AbstractTxOutCommand {

    private Long transactionType;
    private String globalTransactionId;
    private String needResponse;
    private String commandUUID;
    private Object commandMessageInfo;
    private String senderAppName;

    public abstract void setCommandDataAsJson(String json);

    public abstract String getCommandDataAsJson();

    public String getCommandUUID() {
        return commandUUID;
    }

    public void setCommandUUID(String commandUUID) {
        this.commandUUID = commandUUID;
    }

    public String getNeedResponse() {
        return needResponse;
    }

    public void setNeedResponse(String needResponse) {
        this.needResponse = needResponse;
    }

    public String getGlobalTransactionId() {
        return globalTransactionId;
    }

    public void setGlobalTransactionId(String globalTransactionId) {
        this.globalTransactionId = globalTransactionId;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    private Date transactionTime;

    public Long getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Long transactionType) {
        this.transactionType = transactionType;
    }


    /**
     * main business logic to call remote system
     */
    public abstract void execute();

    public Object getCommandMessageInfo() {
        return commandMessageInfo;
    }

    public void setCommandMessageInfo(Object commandMessageInfo) {
        this.commandMessageInfo = commandMessageInfo;
    }

    public String getSenderAppName() {
        return senderAppName;
    }

    public void setSenderAppName(String senderAppName) {
        this.senderAppName = senderAppName;
    }
}
