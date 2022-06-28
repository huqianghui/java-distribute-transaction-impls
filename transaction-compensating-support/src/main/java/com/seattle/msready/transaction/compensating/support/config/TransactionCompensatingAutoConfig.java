package com.seattle.msready.transaction.compensating.support.config;

import com.seattle.msready.transaction.compensating.support.impl.SpringContextManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;


@Configuration
@ComponentScan("com.seattle.msready.transaction.compensating.support")
public class TransactionCompensatingAutoConfig implements ApplicationContextAware {

    public final static String TRANSACTION_IN_COMMAND_FILE_PATH_KEY = "transaction.in.command.file.path";
    public final static String TRANSACTION_OUT_COMMAND_FILE_PATH_KEY = "transaction.out.command.file.path";

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        Assert.isNull(applicationContext, "The applicationContext is null.");
        return applicationContext;
    }

    public static EntityManager getEntityManager() {
        JpaTransactionManager jpaTransactionManager = (JpaTransactionManager) SpringContextManager.getApplicationContext().getBean("defaultTransactionManager");
        EntityManager entityManager = ((EntityManagerHolder) TransactionSynchronizationManager.getResource(jpaTransactionManager.getEntityManagerFactory())).getEntityManager();
        return entityManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        synchronized (TransactionCompensatingAutoConfig.class) {
            if (TransactionCompensatingAutoConfig.applicationContext == null) {
                TransactionCompensatingAutoConfig.applicationContext = applicationContext;
            }
        }

    }
}
