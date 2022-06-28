package com.seattle.msready.mq.transaction.compensating.mq.app;

import com.seattle.msready.mq.transaction.compensating.mq.SeattleMessageReceiverSupportable;
import com.seattle.msready.mq.transaction.compensating.mq.domain.AppMessage;


public interface AppMessageReceiverSupportable<T extends AppMessage> extends SeattleMessageReceiverSupportable<T> {
    void receiveAppMessage(T message);
}
