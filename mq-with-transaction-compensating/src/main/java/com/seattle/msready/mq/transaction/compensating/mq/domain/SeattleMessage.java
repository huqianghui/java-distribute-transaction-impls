package com.seattle.msready.mq.transaction.compensating.mq.domain;

public interface SeattleMessage {

    String getTopics();

    void setTopics(String topics);

    String getMessageType();

    void setMessageType(String messageType);
}