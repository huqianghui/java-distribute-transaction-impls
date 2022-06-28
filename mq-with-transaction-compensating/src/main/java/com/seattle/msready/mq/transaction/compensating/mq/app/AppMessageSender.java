package com.seattle.msready.mq.transaction.compensating.mq.app;


import com.seattle.msready.mq.transaction.compensating.mq.domain.AppMessage;
import com.seattle.msready.mq.transaction.compensating.mq.domain.MdcConstant;
import com.seattle.msready.mq.transaction.compensating.mq.tx.MQSendCommand;
import com.seattle.msready.mq.transaction.compensating.mq.utils.JsonBaseUtils;
import com.seattle.msready.transaction.compensating.support.api.EventuallyConsistentService;
import com.seattle.msready.transaction.compensating.support.entity.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
public class AppMessageSender {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AppMessageSender.class);

    @Autowired(required = false)
    @Qualifier("ebaoRabbitTemplate")
    private RabbitTemplate rabbitTemplate;

    @Autowired(required = false)
    private EventuallyConsistentService eventuallyConsistentService;

    public void sendMessage(String applicationName, AppMessage appMessage) {
        LOGGER.debug("applicationName:{}", applicationName);
        LOGGER.debug("appMessage:{}", appMessage);
        if (appMessage.getCurrentUser() == null && AppMessageQueueConfiguration.getAppUser() != null) {
            appMessage.setCurrentUser(AppMessageQueueConfiguration.getAppUser());
        }
        if (appMessage.isNeedTransactionCompensating()) {
            String correlationId = messageCompensatingPrepare(applicationName, appMessage);
            LOGGER.debug("The message correlationId is:{}", correlationId);
        } else {
            rabbitTemplate.convertAndSend(applicationName, applicationName, appMessage);
        }
    }


    private String messageCompensatingPrepare(String applicationName, AppMessage appMessage) {
        MQSendCommand command = new MQSendCommand();
        String traceId = MDC.get(MdcConstant.MDC_KEY__TRACE_ID);
        if (StringUtils.isEmpty(traceId)) {
            LOGGER.warn("The traceId is null.");
        } else {
            command.setGlobalTransactionId(traceId);
            appMessage.setTraceId(traceId);
        }
        final String correlationId = UUID.randomUUID().toString();
        command.setCorrelationId(correlationId);
        command.setCommandUUID(correlationId);
        command.setIntegrationPoint(applicationName);
        command.setSenderAppName(applicationName);
        command.setContent(JsonBaseUtils.toJSON(appMessage));
        command.setTransactionType(-4L);
        command.setNeedResponse("Y");

        Map<String, Object> commandInfo = new HashMap<String, Object>();
        commandInfo.put(CommandInfo.CommandUUID.toString(), command.getCommandUUID());
        commandInfo.put(CommandInfo.GlobalTransactionId.toString(), command.getGlobalTransactionId());
        commandInfo.put(CommandInfo.TransactionType.toString(), command.getTransactionType());
        commandInfo.put(CommandInfo.NeedResponse.toString(), command.getNeedResponse());
        commandInfo.put(CommandInfo.SourceSystem.toString(), command.getSenderAppName());
        command.setCommandInfo(commandInfo);
        eventuallyConsistentService.processOutCommand(command);

        return command.getCommandUUID();
    }
}
