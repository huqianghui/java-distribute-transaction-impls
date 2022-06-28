package com.seattle.msready.transaction.compensating.support.dto;

import java.util.Date;

public class TxOutCommandDto {
    private String isResponded;
    private long globalTransactionId;
    private long transactionType;
    private String commandUUID;
    private Date processStartTime;
    private Date processEndTime;
    private String mqIntegrationPoint;

    public String getIsResponded() {
        return isResponded;
    }

    public void setIsResponded(String isResponded) {
        this.isResponded = isResponded;
    }

    public long getGlobalTransactionId() {
        return globalTransactionId;
    }

    public void setGlobalTransactionId(long globalTransactionId) {
        this.globalTransactionId = globalTransactionId;
    }

    public long getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(long transactionType) {
        this.transactionType = transactionType;
    }

    public String getCommandUUID() {
        return commandUUID;
    }

    public void setCommandUUID(String commandUUID) {
        this.commandUUID = commandUUID;
    }

    public Date getProcessStartTime() {
        return processStartTime;
    }

    public void setProcessStartTime(Date processStartTime) {
        this.processStartTime = processStartTime;
    }

    public Date getProcessEndTime() {
        return processEndTime;
    }

    public void setProcessEndTime(Date processEndTime) {
        this.processEndTime = processEndTime;
    }

    public String getMqIntegrationPoint() {
        return mqIntegrationPoint;
    }

    public void setMqIntegrationPoint(String mqIntegrationPoint) {
        this.mqIntegrationPoint = mqIntegrationPoint;
    }
}
