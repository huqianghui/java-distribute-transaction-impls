package com.seattle.msready.mq.transaction.compensating.mq.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppMessage implements SeattleMessage, Serializable {
    private boolean needTransactionCompensating = false;

    private String topics;

    private String messageType;

    private AppUser currentUser;

    private String traceId;

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public AppUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(AppUser currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isNeedTransactionCompensating() {
        return needTransactionCompensating;
    }

    public void setNeedTransactionCompensating(boolean needTransactionCompensating) {
        this.needTransactionCompensating = needTransactionCompensating;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
