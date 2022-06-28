package com.seattle.msready.mq.transaction.compensating.mq;

import com.seattle.msready.mq.transaction.compensating.mq.domain.SeattleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class SeattleMessageReceiver<T extends SeattleMessage> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SeattleMessageReceiver.class);

    public static final String DEFAULT_LISTENER_METHOD_NAME = "receiveMessage";

    public abstract void receiveMessage(T message);
}
