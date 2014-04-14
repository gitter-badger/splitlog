package com.github.triceo.splitlog;

import com.github.triceo.splitlog.api.Message;
import com.github.triceo.splitlog.api.MessageDeliveryStatus;
import com.github.triceo.splitlog.api.MessageSource;

/**
 * Implementors of this interface state that they are interested in knowing when
 * a new {@link Message} appears in the log.
 *
 * @param <S>
 *            The source that they expect such notifications from.
 */
public interface MessageListener<S extends MessageSource<S>> {

    /**
     * Notify the code of a new message becoming available in the log.
     *
     * Although this method is public, it only serves as means of communication
     * between Splitlog internals. It is forbidden for user code to ever call
     * this method. Only the authorized sources, all part of Splitlog core, may
     * use it.
     *
     * Implementors are encouraged to properly synchronize this method, as it is
     * expected that message notifications will be received in the same order in
     * which the messages appear in the log.
     *
     * @param message
     *            Message in question.
     * @param status
     *            Current status of the message.
     * @param source
     *            The code that is notifying us of this event.
     */
    void messageReceived(Message message, MessageDeliveryStatus status, S source);

}
