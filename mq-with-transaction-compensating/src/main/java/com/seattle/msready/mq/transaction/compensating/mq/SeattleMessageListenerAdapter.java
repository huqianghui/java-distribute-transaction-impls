package com.seattle.msready.mq.transaction.compensating.mq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpIllegalStateException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.listener.adapter.InvocationResult;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.utils.SerializationUtils;

import java.lang.reflect.Method;

public class SeattleMessageListenerAdapter extends MessageListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeattleMessageListenerAdapter.class);

    public SeattleMessageListenerAdapter() {

    }

    public SeattleMessageListenerAdapter(Object delegate, MessageConverter messageConverter) {
        doSetDelegate(delegate);
        super.setMessageConverter(messageConverter);
    }

    private void doSetDelegate(Object delegate) {
        org.springframework.util.Assert.notNull(delegate, "Delegate must not be null");
        setDelegate(delegate);
    }

    public SeattleMessageListenerAdapter(Object delegate, String defaultListenerMethod) {
        doSetDelegate(delegate);
        setDefaultListenerMethod(defaultListenerMethod);
    }

    /**
     * Spring {@link ChannelAwareMessageListener} entry point.
     * <p>
     * Delegates the message to the target listener method, with appropriate conversion of the message argument. If the
     * target method returns a non-null object, wrap in a Rabbit message and send it back.
     *
     * @param message the incoming Rabbit message
     * @param channel the Rabbit channel to operate on
     * @throws Exception if thrown by Rabbit API methods
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        // Check whether the delegate is a MessageListener impl itself.
        // In that case, the adapter will simply act as a pass-through.
        Object delegate = getDelegate();
        if (delegate != this) {
            if (delegate instanceof ChannelAwareMessageListener) {
                if (channel != null) {
                    ((ChannelAwareMessageListener) delegate).onMessage(message, channel);
                    return;
                } else if (!(delegate instanceof MessageListener)) {
                    throw new AmqpIllegalStateException("MessageListenerAdapter cannot handle a "
                            + "ChannelAwareMessageListener delegate if it hasn't been invoked with a Channel itself");
                }
            }
            if (delegate instanceof MessageListener) {
                ((MessageListener) delegate).onMessage(message);
                return;
            }
        }
        // Regular case: find a handler method reflectively.
        LOGGER.debug("message:", message);
        Object convertedMessage = extractMessage(message);
        LOGGER.debug("convertedMessage:", convertedMessage);
        String methodName = getListenerMethodName(message, convertedMessage);
        LOGGER.debug("methodName:", methodName);
        if (methodName == null) {
            throw new AmqpIllegalStateException("No default listener method specified: "
                    + "Either specify a non-null value for the 'defaultListenerMethod' property or "
                    + "override the 'getListenerMethodName' method.");
        }

        // Invoke the handler method with appropriate arguments.
        Object[] listenerArguments = buildListenerArguments(delegate, methodName, convertedMessage, message);
        LOGGER.debug("listenerArguments:", listenerArguments);
        Object result = invokeListenerMethod(methodName, listenerArguments, message);
        if (result != null) {
            handleResult((InvocationResult) result, message, channel);
        } else {
            logger.trace("No result object given - no result to handle");
        }
    }


    /**
     * Build an array of arguments to be passed into the target listener method. Allows for multiple method arguments to
     * be built from a single message object.
     * <p>
     * The default implementation builds an array with the given message object as sole element. This means that the
     * extracted message will always be passed into a <i>single</i> method argument, even if it is an array, with the
     * target method having a corresponding single argument of the array's type declared.
     * <p>
     * This can be overridden to treat special message content such as arrays differently, for example passing in each
     * element of the message array as distinct method argument.
     *
     * @param extractedMessage the content of the message
     * @return the array of arguments to be passed into the listener method (each element of the array corresponding to
     * a distinct method argument)
     */
    protected Object[] buildListenerArguments(Object delegate, String methodName, Object extractedMessage, Message
            message) {
        LOGGER.debug("buildListenerArguments...");
        LOGGER.debug("delegate:{}", delegate);
        LOGGER.debug("methodName:{}", methodName);
        LOGGER.debug("extractedMessage:{}", extractedMessage);
        LOGGER.debug("message:{}", message);
        Method[] methodArray = delegate.getClass().getMethods();
        Method matchedMethod = null;
        for (Method method : methodArray) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                matchedMethod = method;
            }
        }
        LOGGER.debug("matchedMethod.parameterCount:{}", matchedMethod.getParameterCount());
        if (matchedMethod.getParameterCount() == 1) {
            LOGGER.debug("matchedMethod.getParameters()[0].parameterizedType:{}", matchedMethod.getParameters()[0].getParameterizedType());
            if (matchedMethod.getParameters()[0].getParameterizedType().equals(String.class)) {
                return new Object[]{getBodyContentAsString(message)};
            }
        }
        return new Object[]{extractedMessage};
    }

    private String getBodyContentAsString(Message message) {
        if (message.getBody() == null) {
            return null;
        } else {
            try {
                String contentType = message.getMessageProperties() != null ? message.getMessageProperties().getContentType() : null;
                if ("application/x-java-serialized-object".equals(contentType)) {
                    return SerializationUtils.deserialize(message.getBody()).toString();
                }

                if ("text/plain".equals(contentType) || "application/json".equals(contentType) || "text/x-json".equals(contentType) || "application/xml".equals(contentType)) {
                    return new String(message.getBody(), message.getMessageProperties().getContentEncoding());
                }
            } catch (Exception var2) {
                throw new RuntimeException(var2.toString());
            }

            return message.getBody().toString() + "(byte[" + message.getBody().length + "])";
        }
    }

}
