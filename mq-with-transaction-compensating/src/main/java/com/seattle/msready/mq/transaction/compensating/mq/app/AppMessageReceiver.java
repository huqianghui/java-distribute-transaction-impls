package com.seattle.msready.mq.transaction.compensating.mq.app;



import com.seattle.msready.mq.transaction.compensating.mq.MessageQueueAutoConfig;
import com.seattle.msready.mq.transaction.compensating.mq.SeattleMessageReceiver;
import com.seattle.msready.mq.transaction.compensating.mq.domain.AppMessage;
import com.seattle.msready.mq.transaction.compensating.mq.tx.MQReceiveCommand;
import com.seattle.msready.mq.transaction.compensating.mq.utils.JsonBaseUtils;
import com.seattle.msready.transaction.compensating.support.api.EventuallyConsistentService;
import com.seattle.msready.transaction.compensating.support.impl.SpringContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@ConditionalOnProperty(value = "need.app.queue", havingValue = "true")
@RabbitListener(queues = "${spring.application.name}",admin = "primaryAmqpAdmin")
public class AppMessageReceiver<T extends AppMessage> extends SeattleMessageReceiver<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppMessageReceiver.class);

    @Value("${spring.application.name}")
    private String appName;

    @RabbitHandler
    public void receiveMessage(T message) {
        LOGGER.debug("receiveMessage:{}", message);
        if (message.getCurrentUser() != null && AppMessageQueueConfiguration.getAppUser() == null) {
            AppMessageQueueConfiguration.setAppUser(message.getCurrentUser());
        }

        if (message.isNeedTransactionCompensating()) {
            String commandUUID = messageReceiveCompensatingPrepare(message);
            LOGGER.debug("commandUUID:{}", commandUUID);
        } else {
            String topics = message.getTopics();
            Map<String, AppMessageReceiverSupportable> registeredAppMessageReceiverSupportableMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(SpringContextManager.getApplicationContext(),AppMessageReceiverSupportable.class,true,true);
            List<AppMessageReceiverSupportable> matchedAppMessageReceiverSupportableList = registeredAppMessageReceiverSupportableMap.entrySet().stream()
                    .filter(entry -> entry.getValue().isSupportTopic(topics))
                    .map(filteredEntry -> filteredEntry.getValue())
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(matchedAppMessageReceiverSupportableList)) {
                matchedAppMessageReceiverSupportableList.forEach(appMessageReceiverSupportable -> {
                    appMessageReceiverSupportable.receiveAppMessage(message);
                });
            }
        }
    }


    private String messageReceiveCompensatingPrepare(AppMessage appMessage) {
        MQReceiveCommand command = new MQReceiveCommand();
        LOGGER.debug("The traceId of appMessage is {}.", appMessage.getTraceId());
        LOGGER.debug("The applicationName is {}.", appName);

        if (StringUtils.isEmpty(appMessage.getTraceId())) {
            LOGGER.warn("The traceId is null.");
        } else {
            command.setGlobalTransactionId(appMessage.getTraceId());
        }
        final String correlationId = UUID.randomUUID().toString();
        command.setCommandUUID(correlationId);
        command.setIntegrationPoint(appName);
        command.setContent(JsonBaseUtils.toJSON(appMessage));
        command.setTransactionType(-4L);
        command.setNeedResponse("N");
        ((EventuallyConsistentService) MessageQueueAutoConfig.getApplicationContext().getBean(EventuallyConsistentService.BEAN_NAME)).processInCommand(command);
        return command.getCommandUUID();
    }
}
