package com.seattle.msready.mq.transaction.compensating.tx.client;

public interface CallBack {

    /**
     * when get a response from integration point,execute callback function,
     * if this integration need some reply,must return a object ,if not,return null
     *
     * @param receivedObject that get from integration point
     * @return object if integration point needs reply,if not ,return null
     */
    Object execute(Object receivedObject);
}
