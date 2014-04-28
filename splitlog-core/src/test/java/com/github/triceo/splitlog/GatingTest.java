package com.github.triceo.splitlog;

import java.util.Arrays;
import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.triceo.splitlog.api.LogWatch;
import com.github.triceo.splitlog.api.LogWatchBuilder;
import com.github.triceo.splitlog.api.Message;
import com.github.triceo.splitlog.api.MessageDeliveryStatus;
import com.github.triceo.splitlog.api.MessageMeasure;
import com.github.triceo.splitlog.api.MessageMetric;
import com.github.triceo.splitlog.conditions.AllLogWatchMessagesAcceptingCondition;
import com.github.triceo.splitlog.splitters.JBossServerLogTailSplitter;

@RunWith(Parameterized.class)
public class GatingTest extends DefaultFollowerBaseTest {

    private static final String MEASURE_ID = "test";

    // will verify various configs of log watch
    @Parameters(name = "{index}: {0}, {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { LogWatchBuilder.getDefault().watchingFile(DefaultFollowerBaseTest.getTempFile()), false },
                {
                        LogWatchBuilder.getDefault().watchingFile(DefaultFollowerBaseTest.getTempFile())
                                .withGateCondition(AllLogWatchMessagesAcceptingCondition.INSTANCE), true } });
    }

    private final boolean isGatingDisabled;

    public GatingTest(final LogWatchBuilder builder, final boolean gatingDisabled) {
        super(builder);
        this.isGatingDisabled = gatingDisabled;
    }

    private MessageMetric<Integer, LogWatch> fillWithData(final DefaultLogWatch watch) {
        final MessageMetric<Integer, LogWatch> metric = watch.startMeasuring(new MessageMeasure<Integer, LogWatch>() {

            @Override
            public Integer initialValue() {
                return 0;
            }

            @Override
            public Integer update(final MessageMetric<Integer, LogWatch> metric, final Message evaluate,
                final MessageDeliveryStatus status, final LogWatch source) {
                if (status != MessageDeliveryStatus.INCOMING) {
                    return metric.getValue() + 1;
                } else {
                    return metric.getValue();
                }
            }

        }, GatingTest.MEASURE_ID);
        watch.addLine("07:30:02,670 INFO  [org.jboss.msc] (main) JBoss MSC version 1.0.4.GA-redhat-1");
        watch.addLine("07:30:02,670 INFO  [com.github.triceo.splitlog] (main) random");
        watch.addLine("07:30:02,670 INFO  [com.github.triceo.splitlog.check] (main) something else random");
        watch.addLine("07:30:02,739 DEBUG [org.jboss.as.config] (MSC service thread 1-7) Configured system properties:");
        watch.addLine("07:30:02,731 INFO  [org.jboss.as] (MSC service thread 1-7) JBAS015899: JBoss BRMS 6.0.1.GA (AS 7.2.1.Final-redhat-10) starting");
        watch.terminate();
        return metric;
    }

    @Test
    public void testDefaults() {
        // with simple tail splitter, no gatting can take place
        final DefaultLogWatch watch = (DefaultLogWatch) this.getLogWatch();
        final MessageMetric<Integer, LogWatch> metric = this.fillWithData(watch);
        Assertions.assertThat(metric.getValue()).isEqualTo(4);
    }

    @Test
    public void testGatingCondition() {
        final DefaultLogWatch watch = (DefaultLogWatch) this.getBuilder().buildWith(new JBossServerLogTailSplitter());
        final MessageMetric<Integer, LogWatch> metric = this.fillWithData(watch);
        if (this.isGatingDisabled) {
            // all messages will be let through
            Assertions.assertThat(metric.getValue()).isEqualTo(4);
        } else {
            // only messages not from splitlog will be let through
            Assertions.assertThat(metric.getValue()).isEqualTo(2);
        }
    }
}
