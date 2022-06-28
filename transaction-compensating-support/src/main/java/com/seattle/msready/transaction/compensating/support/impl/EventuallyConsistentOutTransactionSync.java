package com.seattle.msready.transaction.compensating.support.impl;


import com.seattle.msready.transaction.compensating.support.config.TransactionCompensatingAutoConfig;
import com.seattle.msready.transaction.compensating.support.api.EventuallyConsistentService;
import com.seattle.msready.transaction.compensating.support.entity.AbstractTxOutCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class EventuallyConsistentOutTransactionSync extends TransactionSynchronizationAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventuallyConsistentServiceImpl.class);

    public final static String SPRING_BEAN_TX_MANAGER = "transactionManager";

    private AbstractTxOutCommand outCommand;

    public AbstractTxOutCommand getOutCommand() {
        return outCommand;
    }

    public void setOutCommand(AbstractTxOutCommand outCommand) {
        this.outCommand = outCommand;
    }

    @Override
    public void afterCommit() {
        LOGGER.debug("afterCommit...");
        TransactionTemplate transactionTemplate = new TransactionTemplate(TransactionCompensatingAutoConfig.getApplicationContext().getBean(SPRING_BEAN_TX_MANAGER, PlatformTransactionManager.class));
        transactionTemplate.setPropagationBehavior(Propagation.REQUIRES_NEW.value());
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    LOGGER.debug("outCommand.execute...");
                    outCommand.execute();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    EventuallyConsistentService service = TransactionCompensatingAutoConfig.getApplicationContext().getBean(EventuallyConsistentService.BEAN_NAME, EventuallyConsistentService.class);
                    service.markOutCommandErrorMessage(outCommand.getCommandUUID(), e);
                }
            });
            throw new RuntimeException(e);
        }
        if ("N".equals(outCommand.getNeedResponse())) {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    EventuallyConsistentService service = TransactionCompensatingAutoConfig.getApplicationContext().getBean(EventuallyConsistentService.BEAN_NAME, EventuallyConsistentService.class);
                    service.markOutCommandResponded(outCommand.getCommandUUID());
                }
            });

        }
    }


}
