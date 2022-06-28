package com.seattle.msready.mq.transaction.compensating.mq.tx.client;


import com.seattle.msready.mq.transaction.compensating.mq.app.AppMessageQueueConfiguration;
import com.seattle.msready.mq.transaction.compensating.mq.app.AppMessageReceiverSupportable;
import com.seattle.msready.mq.transaction.compensating.mq.domain.AppMessage;
import com.seattle.msready.mq.transaction.compensating.mq.tx.MQSendCommand;
import com.seattle.msready.mq.transaction.compensating.tx.client.CallBack;
import com.seattle.msready.mq.transaction.compensating.tx.client.Client;
import com.seattle.msready.mq.transaction.compensating.tx.client.ClientException;
import com.seattle.msready.transaction.compensating.support.api.EventuallyConsistentService;
import com.seattle.msready.transaction.compensating.support.entity.CommandInfo;
import com.seattle.msready.transaction.compensating.support.impl.SpringContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.seattle.msready.mq.transaction.compensating.mq.utils.JsonBaseUtils.fromJSON;


@Service
public class MQClientImpl implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(MQClientImpl.class);

    @Autowired(required = false)
    private EventuallyConsistentService eventuallyConsistentService;

    @Autowired(required = false)
    @Qualifier("ebaoRabbitTemplate")
    private RabbitTemplate rabbitTemplate;

    // consumer listener map
    private Map<String, DefaultMessageListenerContainer> consumerListenerMap = new Hashtable<String, DefaultMessageListenerContainer>();

    // consumer listener need reply map
    private Map<String, DefaultMessageListenerContainer> consumerReplyListenerMap = new Hashtable<String, DefaultMessageListenerContainer>();

    @Override
    public void request(String integrationPoint, final Serializable content, final CallBack callBack)
            throws ClientException {
        return;
    }

    @Override
    public void dispatch(String integrationPoint, final Serializable content) throws ClientException {
        LOGGER.debug("dispatch...");
    }

    @Override
    public void ECDispatch(String integrationPoint, final Serializable content, Object commandMessageInfo)
            throws ClientException {
        return;
    }


    @Override
    public void listen(String integrationPoint, CallBack callBack) throws ClientException {
    }

    @Override
    public void listenAndReply(String integrationPoint, CallBack callback) throws ClientException {

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        // close all listeners
        Iterator<String> iterator = consumerListenerMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            DefaultMessageListenerContainer dmc = consumerListenerMap.get(key);
            dmc.destroy();
        }

        iterator = consumerReplyListenerMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();

            DefaultMessageListenerContainer dmc = consumerReplyListenerMap.get(key);
            dmc.destroy();
        }
    }


    @Override
    public String send(String integrationPoint, final Serializable content, final String correlationId)
            throws ClientException, ClassNotFoundException {
        LOGGER.debug("send...");
        LOGGER.debug("integrationPoint:{}", integrationPoint);
        LOGGER.debug("content:{}", content);
        LOGGER.debug("correlationId:{}", correlationId);
        AppMessage appMessage = null;
        if (content != null && !StringUtils.isEmpty(content.toString())) {
            appMessage = fromJSON(content.toString(), AppMessage.class);
            if (appMessage.getMessageType() != null && !appMessage.getMessageType().equalsIgnoreCase(AppMessage.class.getName())) {
                Class appMessageClass = MQClientImpl.class.forName(appMessage.getMessageType());
                appMessage = (AppMessage) fromJSON(content.toString(), appMessageClass);
            }
        }
        CorrelationData correlationData = new CorrelationData(correlationId);
        rabbitTemplate.convertAndSend(integrationPoint, integrationPoint, appMessage, correlationData);
        return correlationId;
    }

    @Override
    public String send(String integrationPoint, final Serializable content) throws ClientException, ClassNotFoundException {
        final String correlationId = UUID.randomUUID().toString();
        return send(integrationPoint, content, correlationId);
    }

    @Override
    public void receive(String integrationPoint, final String content, final CallBack callBack)
            throws ClientException, ClassNotFoundException {
        LOGGER.debug("receive...");
        LOGGER.debug("integrationPoint:{}", integrationPoint);
        LOGGER.debug("content:{}", content);
        LOGGER.debug("callBack:{}", callBack);
        AppMessage appMessage = fromJSON(content, AppMessage.class);
        if (appMessage.getMessageType() != null) {
            Class appMessageClass = MQClientImpl.class.forName(appMessage.getMessageType());
            appMessage = (AppMessage) fromJSON(content, appMessageClass);
        }
        final AppMessage appMessageFinal = appMessage;

        if (appMessageFinal.getCurrentUser() == null && AppMessageQueueConfiguration.getAppUser() != null) {
            appMessageFinal.setCurrentUser(AppMessageQueueConfiguration.getAppUser());
        }

        if (appMessageFinal.getCurrentUser() != null && AppMessageQueueConfiguration.getAppUser() == null) {
            AppMessageQueueConfiguration.setAppUser(appMessageFinal.getCurrentUser());
        }

        Map<String, AppMessageReceiverSupportable> registeredAppMessageReceiverSupportableMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(SpringContextManager.getApplicationContext(),AppMessageReceiverSupportable.class,true,true);
        List<AppMessageReceiverSupportable> matchedAppMessageReceiverSupportableList = registeredAppMessageReceiverSupportableMap.entrySet().stream()
                .filter(entry -> entry.getValue().isSupportTopic(appMessageFinal.getTopics()))
                .map(filteredEntry -> filteredEntry.getValue())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(matchedAppMessageReceiverSupportableList)) {
            matchedAppMessageReceiverSupportableList.forEach(appMessageReceiverSupportable -> {
                appMessageReceiverSupportable.receiveAppMessage(appMessageFinal);
            });
        }

        if (callBack != null) {
            callBack.execute(appMessageFinal);
        }
    }

    @Override
    public void listen(String integrationPoint, final String integrationPointActionId, final CallBack callBack)
            throws ClientException {
        return;
    }


    @Override
    public String ECSend(String integrationPoint, Serializable content) throws ClientException {

        MQSendCommand command = new MQSendCommand();
        final String correlationId = UUID.randomUUID().toString();
        command.setIntegrationPoint(integrationPoint);
        command.setContent(content);
        command.setCorrelationId(correlationId);
        command.setTransactionType(-4L);
        command.setNeedResponse("Y");
        eventuallyConsistentService.processOutCommand(command);

        Map<String, Object> commandInfo = new HashMap<String, Object>();
        commandInfo.put(CommandInfo.CommandUUID.toString(), command.getCommandUUID());
        commandInfo.put(CommandInfo.GlobalTransactionId.toString(), command.getGlobalTransactionId());
        commandInfo.put(CommandInfo.TransactionType.toString(), command.getTransactionType());
        commandInfo.put(CommandInfo.NeedResponse.toString(), command.getNeedResponse());
        commandInfo.put(CommandInfo.SourceSystem.toString(), integrationPoint);
        command.setCommandInfo(commandInfo);

        return correlationId;
    }
}
