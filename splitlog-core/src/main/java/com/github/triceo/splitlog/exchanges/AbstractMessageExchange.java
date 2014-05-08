package com.github.triceo.splitlog.exchanges;

import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;

import org.slf4j.Logger;

import com.github.triceo.splitlog.api.Message;
import com.github.triceo.splitlog.api.MessageDeliveryStatus;
import com.github.triceo.splitlog.api.MessageListener;
import com.github.triceo.splitlog.api.MessageProducer;
import com.github.triceo.splitlog.logging.SplitlogLoggerFactory;

abstract class AbstractMessageExchange<C, S extends MessageProducer<S>> implements MessageListener<S>,
Callable<Message> {

    private static final Logger LOGGER = SplitlogLoggerFactory.getLogger(AbstractMessageExchange.class);

    private final C blockingCondition;
    /**
     * Will prevent blocking in
     * {@link #messageReceived(Message, MessageDeliveryStatus, MessageProducer)}
     * until {@link #call()} has been called.
     */
    private boolean isBlocking = false;
    private final AbstractMessageExchangeManager<S, C> manager;
    private final Exchanger<Message> messageExchanger = new Exchanger<Message>();

    protected AbstractMessageExchange(final AbstractMessageExchangeManager<S, C> exchange, final C condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Must provide condition.");
        }
        this.manager = exchange;
        this.blockingCondition = condition;
    }

    @Override
    public Message call() {
        try {
            AbstractMessageExchange.LOGGER.info("Thread blocked waiting for message to pass condition {}.",
                    this.getBlockingCondition());
            this.isBlocking = true;
            return this.messageExchanger.exchange(null);
        } catch (final InterruptedException e) {
            return null;
        } finally { // just in case
            this.manager.unsetExpectation(this);
            AbstractMessageExchange.LOGGER.info("Thread unblocked.");
        }
    }

    protected C getBlockingCondition() {
        return this.blockingCondition;
    }

    protected abstract boolean isAccepted(final Message msg, final MessageDeliveryStatus status, final S source);

    @Override
    public void messageReceived(final Message msg, final MessageDeliveryStatus status, final S source) {
        if (!this.isBlocking) {
            return;
        }
        AbstractMessageExchange.LOGGER.info("Notified of message '{}' in state {} from {}.", msg, status, source);
        // check if the user code accepts the message
        if (!this.isAccepted(msg, status, source)) {
            return;
        }
        AbstractMessageExchange.LOGGER.debug("Condition passed by message '{}' in state {} from {}.", msg, status,
                source);
        try {
            this.isBlocking = false;
            this.messageExchanger.exchange(msg);
        } catch (final InterruptedException e) {
            AbstractMessageExchange.LOGGER.warn("Failed to notify of message '{}' in state {} from {}.", msg, status,
                    source, e);
        }
    }

}
