package com.seattle.msready.mq.transaction.compensating.mq;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
@ComponentScan(basePackages = {"com.seattle.msready.mq.transaction.compensating.mq", "com.seattle.msready.mq.transaction.compensating.mq.tx"})
public class MessageQueueAutoConfig implements ApplicationContextAware {

    private String rabbitMqAddress="172.18.24.48:5672";

    @Value("${spring.rabbitmq.username}")
    private String rabbitMqUserName;

    @Value("${spring.rabbitmq.password}")
    private String rabbitMqPassword;

    @Value("#{'${spring.rabbitmq.virtualHost:/}'}")
    private String rabbitMqVirtualHost="/";

    private static ApplicationContext applicationContext;

    @Bean
    @Primary
    public SeattleJackson2JsonMessageConverter ebaoJackson2JsonMessageConverter() {
        return new SeattleJackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setUsername(rabbitMqUserName);
        connectionFactory.setPassword(rabbitMqPassword);
        connectionFactory.setAddresses(rabbitMqAddress);
        connectionFactory.setVirtualHost(rabbitMqVirtualHost);
        connectionFactory.setChannelCacheSize(100);
        return connectionFactory;
    }


    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        synchronized (MessageQueueAutoConfig.class) {
            if (MessageQueueAutoConfig.applicationContext == null) {
                MessageQueueAutoConfig.applicationContext = applicationContext;
            }
        }

    }
}
