package com.seattle.msready.mq.transaction.compensating.mq.tx;


import com.seattle.msready.mq.transaction.compensating.mq.MessageQueueAutoConfig;
import com.seattle.msready.mq.transaction.compensating.mq.utils.JsonBaseUtils;
import com.seattle.msready.mq.transaction.compensating.tx.client.CallBack;
import com.seattle.msready.mq.transaction.compensating.tx.client.Client;
import com.seattle.msready.transaction.compensating.support.entity.AbstractTxInCommand;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MQReceiveCommand extends AbstractTxInCommand {

    private Map<String, String> parameterMap = new HashMap<String, String>();

    private final static String KEY_INTEGRATION_POINT = "integrationPoint";

    private final static String KEY_CALLBACK_BEAN = "callbackBean";

    private final static String KEY_CONTENT = "content";

    public void setIntegrationPoint(String integrationPoint) {
        parameterMap.put(KEY_INTEGRATION_POINT, integrationPoint);
    }

    public void setContent(String content) {
        parameterMap.put(KEY_CONTENT, content);
    }


    public void setCallbackBean(String callbackBean) {
        parameterMap.put(KEY_CALLBACK_BEAN, callbackBean);
    }


    @Override
    public void setCommandDataAsJson(String json) {
        parameterMap = JsonBaseUtils.fromJSON(json, Map.class);
    }

    @Override
    public String getCommandDataAsJson() {
        return JsonBaseUtils.toJSON(parameterMap);
    }

    @Override
    public void execute() {
        Client client = MessageQueueAutoConfig.getApplicationContext().getBean(Client.class);
        CallBack callBackBean = null;
        if (!StringUtils.isEmpty(parameterMap.get(KEY_CALLBACK_BEAN))) {
            callBackBean = (CallBack) MessageQueueAutoConfig.getApplicationContext().getBean(parameterMap.get(KEY_CALLBACK_BEAN));
        }
        try {
            client.receive(parameterMap.get(KEY_INTEGRATION_POINT), parameterMap.get(KEY_CONTENT), callBackBean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteMessageAfterTxComplete() {
        // TODO Auto-generated method stub

    }

}
