package com.github.triceo.splitlog;

import java.util.SortedSet;
import java.util.TreeSet;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.github.triceo.splitlog.api.Follower;
import com.github.triceo.splitlog.api.LogWatchBuilder;
import com.github.triceo.splitlog.api.Message;

@RunWith(Parameterized.class)
public class NestingTest extends DefaultFollowerBaseTest {

    public NestingTest(final LogWatchBuilder builder) {
        super(builder);
    }

    @Test
    public void testNesting() {
        final String message1 = "test1";
        final String message2 = "test2";
        final String message3 = "test3";
        final String message4 = "test4";
        final String message5 = "test5";
        final Follower follower = this.getLogWatch().startFollowing();
        // make sure the messages are received by the first follower
        this.getWriter().write(message1, follower);
        String result = this.getWriter().write(message2, follower);
        Assertions.assertThat(result).isEqualTo(message2);
        Assertions.assertThat(follower.getMessages().size()).isEqualTo(1);
        // start a second follower, send some messages
        final Follower nestedFollower = this.getLogWatch().startFollowing();
        result = this.getWriter().write(message3, follower);
        result = this.getWriter().write(message4, follower);
        Assertions.assertThat(result).isEqualTo(message4);
        this.getLogWatch().stopFollowing(nestedFollower);
        // send another message, so the original follower has something extra
        Assertions.assertThat(nestedFollower.isStopped()).isTrue();
        result = this.getWriter().write(message5, follower);
        // and make sure that the original follower has all messages
        final SortedSet<Message> messages = new TreeSet<Message>(follower.getMessages());
        DefaultFollowerBaseTest.assertProperOrder(messages, message1, message2, message3, message4);
        // and the nested follower has only the two while it was running
        final SortedSet<Message> nestedMessages = new TreeSet<Message>(nestedFollower.getMessages());
        DefaultFollowerBaseTest.assertProperOrder(nestedMessages, message2, message3);
    }

}
