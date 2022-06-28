package com.seattle.msready.mq.transaction.compensating.mq.tx;



import com.seattle.msready.mq.transaction.compensating.mq.MessageQueueAutoConfig;
import com.seattle.msready.mq.transaction.compensating.mq.utils.JsonBaseUtils;
import com.seattle.msready.mq.transaction.compensating.tx.client.Client;
import com.seattle.msready.transaction.compensating.support.entity.AbstractTxOutCommand;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MQSendCommand extends AbstractTxOutCommand {

    private Map<String, Object> parameterMap = new HashMap<>();

    private final static String KEY_INTEGRATION_POINT = "integrationPoint";

    private final static String KEY_CONTENT = "content";

    private final static String CORRELATION_ID = "correlationId";

    private final static String COMMAND_INFO = "commandInfo";

    public void setIntegrationPoint(String integrationPoint) {
        parameterMap.put(KEY_INTEGRATION_POINT, integrationPoint);
    }

    public void setContent(Object content) {
        parameterMap.put(KEY_CONTENT, content);
    }

    public void setCorrelationId(String correlationId) {
        parameterMap.put(CORRELATION_ID, correlationId);
    }

    public String getCorrelationId() {
        return (String) parameterMap.get(CORRELATION_ID);
    }

    public void setCommandInfo(Object commandInfo) {
        parameterMap.put(COMMAND_INFO, commandInfo);
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
        try {
            client.send((String) parameterMap.get(KEY_INTEGRATION_POINT), (Serializable) parameterMap.get(KEY_CONTENT), this.getCorrelationId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
