package com.github.triceo.splitlog.splitters;

import java.util.Date;
import java.util.List;

import com.github.triceo.splitlog.api.MessageSeverity;
import com.github.triceo.splitlog.api.MessageType;

final public class SimpleTailSplitter extends AbstractTailSplitter {

    @Override
    public Date determineDate(final List<String> raw) {
        return null;
    }

    @Override
    public String determineLogger(final List<String> line) {
        return null;
    }

    @Override
    public MessageSeverity determineSeverity(final List<String> raw) {
        return MessageSeverity.UNKNOWN;
    }

    @Override
    public MessageType determineType(final List<String> raw) {
        return MessageType.LOG;
    }

    @Override
    public boolean isStartingLine(final String line) {
        return true;
    }

    @Override
    public String stripOfMetadata(final String line) {
        return line;
    }

}
