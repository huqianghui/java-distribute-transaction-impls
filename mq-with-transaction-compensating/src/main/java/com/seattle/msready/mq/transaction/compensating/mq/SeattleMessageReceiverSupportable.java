package com.seattle.msready.mq.transaction.compensating.mq;


import com.seattle.msready.mq.transaction.compensating.mq.domain.SeattleMessage;

public interface SeattleMessageReceiverSupportable<T extends SeattleMessage> {
    Class<T> getSupportableClass();

    boolean isSupportTopic(String topic);
}
