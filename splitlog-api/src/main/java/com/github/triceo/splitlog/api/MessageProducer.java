package com.github.triceo.splitlog.api;

/**
 * A tagging interface to mark classes that are allowed to send message delivery
 * notifications to others.
 *
 * @param <S>
 *            The type to send out {@link Message} notifications. Typically it
 *            is the implementing type.
 */
public interface MessageProducer<S extends MessageProducer<S>> extends MessageMetricProducer<S> {

}
