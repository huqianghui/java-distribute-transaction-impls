package com.seattle.msready.transaction.compensating.support.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringContextManager {
    private static final Logger logger = LoggerFactory.getLogger(SpringContextManager.class);

    private static ApplicationContext applicationContext = null;

    /**
     * get application context for current thread
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    /**
     * set application context instance to the static var,then the application
     * context is created.<BR>
     *
     * @param context
     */
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static void closeApplicationContext() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) applicationContext).close();
        } else {
            logger.warn("Can't close application context:" + applicationContext);
        }
    }
}
