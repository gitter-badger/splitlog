package com.github.triceo.splitlog;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.github.triceo.splitlog.api.Message;
import com.github.triceo.splitlog.api.MessageDeliveryStatus;
import com.github.triceo.splitlog.api.MessageMeasure;
import com.github.triceo.splitlog.api.MessageMetric;
import com.github.triceo.splitlog.api.MessageMetricCondition;
import com.github.triceo.splitlog.api.MessageSource;

final class DefaultMessageMetric<T extends Number, S extends MessageSource<S>> implements MessageMetric<T, S>,
        MessageListener<S> {

    private final MessageMetricExchange<T, S> exchange = new MessageMetricExchange<T, S>(this);
    private final MessageMeasure<T, S> measure;
    private final S source;
    private final SortedMap<Long, Pair<Long, T>> stats = new TreeMap<Long, Pair<Long, T>>();

    public DefaultMessageMetric(final S source, final MessageMeasure<T, S> measure) {
        if (measure == null) {
            throw new IllegalArgumentException("Measure must not be null.");
        } else if (source == null) {
            throw new IllegalArgumentException("Source must not be null.");
        }
        this.source = source;
        this.measure = measure;
    }

    @Override
    public MessageMeasure<T, S> getMeasure() {
        return this.measure;
    }

    @Override
    public synchronized long getMessageCount() {
        if (this.stats.isEmpty()) {
            return 0;
        }
        return this.stats.get(this.stats.lastKey()).getLeft();
    }

    @Override
    public synchronized long getMessageCount(final Message timestamp) {
        if (timestamp == null) {
            return 0;
        } else if (!this.stats.containsKey(timestamp.getUniqueId())) {
            return -1;
        } else {
            return this.stats.get(timestamp.getUniqueId()).getLeft();
        }
    }

    @Override
    public S getSource() {
        return this.source;
    }

    @Override
    public synchronized T getValue() {
        if (this.stats.isEmpty()) {
            return null;
        }
        return this.stats.get(this.stats.lastKey()).getRight();
    }

    @Override
    public synchronized T getValue(final Message timestamp) {
        if ((timestamp == null) || !this.stats.containsKey(timestamp.getUniqueId())) {
            return null;
        } else {
            return this.stats.get(timestamp.getUniqueId()).getRight();
        }
    }

    @Override
    public synchronized void messageReceived(final Message msg, final MessageDeliveryStatus status, final S source) {
        if ((msg == null) || (status == null) || (source == null)) {
            throw new IllegalArgumentException("Neither message properties may be null.");
        }
        final T newValue = this.measure.update(this, msg, status, source);
        this.stats.put(msg.getUniqueId(), ImmutablePair.of(this.getMessageCount() + 1, newValue));
        this.exchange.messageReceived(msg, status, source);
    }

    /**
     * Will throw an exception if any other thread tries to specify a wait on
     * the instance while another thread is already waiting.
     */
    @Override
    public Message waitFor(final MessageMetricCondition<T, S> condition) {
        return this.exchange.waitForMessage(condition, -1, TimeUnit.NANOSECONDS);
    }

    /**
     * Will throw an exception if any other thread tries to specify a wait on
     * the instance while another thread is already waiting.
     */
    @Override
    public Message waitFor(final MessageMetricCondition<T, S> condition, final long timeout, final TimeUnit unit) {
        if (timeout < 1) {
            throw new IllegalArgumentException("Waiting time must be great than 0, but was: " + timeout + " " + unit);
        }
        return this.exchange.waitForMessage(condition, timeout, unit);
    }

}