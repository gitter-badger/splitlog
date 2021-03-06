package com.github.triceo.splitlog;

import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all Splitlog tests. Will enforce a default timeout and add
 * some useful test execution logging.
 */
public abstract class AbstractSplitlogTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSplitlogTest.class);

    @Rule
    public Timeout globalTimeout = new Timeout((int) TimeUnit.MINUTES.toMillis(1));

    @Rule
    public TestRule watchman = new TestWatcher() {

        @Override
        protected void finished(final Description d) {
            AbstractSplitlogTest.LOGGER.info("----- Finished test: {}\n", d);
        }

        @Override
        protected void starting(final Description d) {
            AbstractSplitlogTest.LOGGER.info("----- Starting test: {}", d);
        }
    };

}
