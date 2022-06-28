package com.seattle.msready.transaction.compensating.support.api;


import com.seattle.msready.transaction.compensating.support.entity.AbstractTxInCommand;
import com.seattle.msready.transaction.compensating.support.entity.AbstractTxOutCommand;

public interface EventuallyConsistentService {
    String BEAN_NAME = "seattle.msready.transaction.compensating.EventuallyConsistentService";

    /**
     * Process out command,steps:<BR>
     * 1)Save command in db <BR>
     * 2)Register the command to spring transaction commit hook.<BR>
     * 3)After transaction commit,execute the command<BR>
     * 4)If the command doesn't need reply,mark it complete.<BR>
     * Used in service client side.
     *
     * @param command
     */
    void processOutCommand(AbstractTxOutCommand command);


    /**
     * log in command to database, return true if successfully,return false if
     * command already executed.<BR>
     * Used in service provider side.
     *
     * @param command
     * @return
     */
    void processInCommand(AbstractTxInCommand command);

    /**
     * execute the out command again.
     *
     * @param commandUUID
     */
    void redoOutCommand(String commandUUID);

    /**
     * set command completed.<BR>
     * return false if command already completed, return true if successfully.
     * Used in service client side.
     *
     * @param commandUUID key of the command
     */
    boolean markOutCommandResponded(String commandUUID);


    void markOutCommandErrorMessage(String commandUUID, Exception e);
}
