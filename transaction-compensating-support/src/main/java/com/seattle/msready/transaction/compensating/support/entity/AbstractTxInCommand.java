package com.seattle.msready.transaction.compensating.support.entity;

import java.util.Date;

public abstract class AbstractTxInCommand {
    private String recAppName;
    private Long transactionType;
    private String globalTransactionId;
    private String needResponse;
    private String commandUUID;
    private String sourceAppName;

    public String getSourceAppName() {
        return sourceAppName;
    }

    public void setSourceAppName(String sourceAppName) {
        this.sourceAppName = sourceAppName;
    }

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
     * main business logical to handle remote call
     */
    public abstract void execute();

    /**
     * post process after tx commit,usually delete income MQ message here.
     */
    public abstract void deleteMessageAfterTxComplete();

    public String getRecAppName() {
        return recAppName;
    }

    public void setRecAppName(String recAppName) {
        this.recAppName = recAppName;
    }
}
