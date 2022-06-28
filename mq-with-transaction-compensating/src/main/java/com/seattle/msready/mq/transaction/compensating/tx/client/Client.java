package com.seattle.msready.mq.transaction.compensating.tx.client;


import java.io.Serializable;


public interface Client {

    /**
     * send an object to integration point,waiting for response ,when get response,execute callBack.
     * this method do not support transation
     *
     * @param integrationPoint
     * @param content          an object to be sended
     * @param callBack         when get a response ,callback
     */
    void request(String integrationPoint, Serializable content, CallBack callBack) throws ClientException;


    /**
     * dispatch content into integration point, needn't response
     *
     * @param integrationPoint integration point
     * @param content
     */
    void dispatch(String integrationPoint, Serializable content) throws ClientException;

    void ECDispatch(String integrationPoint, Serializable content, Object commandMessageInfo) throws ClientException;


    /**
     * send content to integration point,and return the id of integration point action
     * this purpose is that it can get response from integration point by this id
     * do not support transation
     * example:
     * String ipcId = client.send("integrationPoint1","hello world");
     * client.receive("integrationPoint1",ipcId,new new CallBack(){
     * void execute(Object receiveMessage)
     * {
     * //to do
     * }
     * });
     *
     * @param integrationPoint integration point
     * @param content
     * @return the id of integration point action
     */
    String send(String integrationPoint, Serializable content) throws ClientException, ClassNotFoundException;

    String ECSend(String integrationPoint, Serializable content) throws ClientException, ClassNotFoundException;

    String send(String integrationPoint, Serializable content, String correlationId) throws ClientException, ClassNotFoundException;

    /**
     * waiting response from integration point by integration point action id,when get response,execute callBack,
     * if timeout,throw a exception
     * do not support transation
     *
     * @param integrationPoint
     * @param content
     * @param callBack
     */
    void receive(String integrationPoint, String content, CallBack callBack) throws ClientException, ClassNotFoundException;

    /**
     * listening on the integration point,when get a response ,execute callback
     *
     * @param integrationPoint
     * @param callBack
     */
    void listen(String integrationPoint, CallBack callBack) throws ClientException;

    /**
     * waiting response from integration point by integration point action id,when get response,execute callBack,
     * if timeout,do nothing
     * example:
     * String ipcId = send("integrationPoint1","hello world");
     * listen("integrationPoint1",ipcId,new CallBack(){
     * void execute(Object receiveMessage)
     * {
     * //to do
     * }
     * });
     *
     * @param integrationPoint
     * @param integrationPointActionId
     * @param callBack
     */
    void listen(String integrationPoint, String integrationPointActionId, CallBack callBack) throws ClientException;

    /**
     * listening on integration point,when get data from integration point,execute callback,then reply to anther integration point that must be configured
     *
     * @param integrationPoint
     * @param callback
     */
    void listenAndReply(String integrationPoint, CallBack callback) throws ClientException;

    /**
     * close all connection,stop listener on all integration point,release all resources that client needs;
     */
    void close();


}
