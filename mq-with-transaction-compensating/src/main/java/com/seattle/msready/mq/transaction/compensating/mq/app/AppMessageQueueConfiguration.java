package com.seattle.msready.mq.transaction.compensating.mq.app;


import com.seattle.msready.mq.transaction.compensating.mq.MessageQueueAutoConfig;
import com.seattle.msready.mq.transaction.compensating.mq.SeattleJackson2JsonMessageConverter;
import com.seattle.msready.mq.transaction.compensating.mq.SeattleMessageListenerAdapter;
import com.seattle.msready.mq.transaction.compensating.mq.SeattleMessageReceiver;
import com.seattle.msready.mq.transaction.compensating.mq.domain.AppUser;
import com.seattle.msready.mq.transaction.compensating.mq.utils.JsonBaseUtils;
import com.seattle.msready.transaction.compensating.support.api.EventuallyConsistentService;
import com.seattle.msready.transaction.compensating.support.config.TransactionCompensatingAutoConfig;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


@Configuration
@Component
public class AppMessageQueueConfiguration {

    public final static String SPRING_BEAN_TX_MANAGER = "transactionManager";
    private final static String THREAD_BIND_KEY__APP_USER = "APP_USER";
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMessageQueueConfiguration.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("#{'${spring.rabbitmq.publisherConfirms:true}'}")
    private boolean publisherConfirms;

    @Value("#{'${spring.rabbitmq.publisherReturns:true}'}")
    private boolean publisherReturns;

    @Bean
    @ConditionalOnProperty(value = "need.app.queue", havingValue = "true")
    @Primary
    public Queue appQueue() {
        return new Queue(applicationName, true);
    }

    @Bean
    @ConditionalOnProperty(value = "need.app.queue", havingValue = "true")
    @Primary
    DirectExchange exchange() {
        return new DirectExchange(applicationName, true, false);
    }

    @Bean
    @ConditionalOnProperty(value = "need.app.queue", havingValue = "true")
    @Primary
    Binding binding() {
        return BindingBuilder.bind(appQueue()).to(exchange()).with(applicationName);
    }


    @Bean("primaryAmqpAdmin")
    @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "dynamic", matchIfMissing = true)
    @Primary
    public AmqpAdmin amqpAdmin(CachingConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }


    @Bean("appSimpleMessageListenerContainer")
    @ConditionalOnProperty(value = "need.app.queue", havingValue = "true")
    @Primary
    SimpleMessageListenerContainer container(CachingConnectionFactory connectionFactory) {
        connectionFactory.setPublisherConfirms(publisherConfirms);
        connectionFactory.setPublisherReturns(publisherReturns);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(applicationName);
        container.setMessageListener(listenerAdapter());
        // 接受消息的确认模式
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        //container.setMessageConverter(new EbaoJackson2JsonMessageConverter());

        return container;
    }

    @Bean("ebaoRabbitTemplate")
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        ((CachingConnectionFactory) connectionFactory).setPublisherReturns(true);
        ((CachingConnectionFactory) connectionFactory).setPublisherConfirms(true);
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setChannelTransacted(false);
        rabbitTemplate.setMessageConverter(new SeattleJackson2JsonMessageConverter());

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack,
                                String cause) {
                LOGGER.debug("correlationData:{}", correlationData);
                LOGGER.debug("ack:{}", ack);
                LOGGER.debug("cause:{}", cause);
                if (correlationData == null || correlationData.getId() == null) {
                    return;
                }
                EventuallyConsistentService eventuallyConsistentService = MessageQueueAutoConfig.getApplicationContext().getBean(EventuallyConsistentService.class);

                TransactionTemplate transactionTemplate = new TransactionTemplate(TransactionCompensatingAutoConfig.getApplicationContext().getBean(SPRING_BEAN_TX_MANAGER, PlatformTransactionManager.class));
                transactionTemplate.setPropagationBehavior(Propagation.REQUIRED.value());
                transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        if (ack) {
                            eventuallyConsistentService.markOutCommandResponded(correlationData.getId());
                        } else {
                            eventuallyConsistentService.markOutCommandErrorMessage(correlationData.getId(), new Exception(cause));
                        }
                    }
                });
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText,
                                        String exchange, String routingKey) {
                LOGGER.debug("message:{}", message);
                LOGGER.debug("replyCode:{}", replyCode);
                LOGGER.debug("replyText:{}", replyText);
                LOGGER.debug("exchange:{}", exchange);
                LOGGER.debug("routingKey:{}", routingKey);
            }
        });

        return rabbitTemplate;
    }

    @Bean
    @ConditionalOnProperty(value = "need.app.queue", havingValue = "true")
    @Primary
    MessageListenerAdapter listenerAdapter() {
        MessageListenerAdapter messageListenerAdapter = new SeattleMessageListenerAdapter(new AppMessageReceiver(),
                SeattleMessageReceiver.DEFAULT_LISTENER_METHOD_NAME);
        messageListenerAdapter.setMessageConverter(new SeattleJackson2JsonMessageConverter());
        return messageListenerAdapter;
    }

    public static AppUser getAppUser() {
        return JsonBaseUtils.fromJSON(ThreadContext.get(THREAD_BIND_KEY__APP_USER),AppUser.class);
    }

    public static void setAppUser(AppUser user) {
        ThreadContext.put(THREAD_BIND_KEY__APP_USER, JsonBaseUtils.toJSON(user));
    }

}
