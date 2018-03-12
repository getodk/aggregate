package org.opendatakit.common.logging;

import io.sentry.jul.SentryHandler;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static java.util.logging.Level.OFF;

/**
 * This class is a simplified version of the default Sentry handler for JUL
 * which doesn't use any restricted artifact on Google AppEngine
 */
public final class GAESafeSentryHandler extends SentryHandler {

    private final DropSentryFilter dropSentryFilter;
    private volatile Level logLevel = Level.WARNING;


    GAESafeSentryHandler() {
        this.dropSentryFilter = new DropSentryFilter();
        this.setFilter(dropSentryFilter);
    }

    @Override
    public synchronized void setLevel(Level newLevel) throws SecurityException {
        logLevel = newLevel;
    }

    @Override
    protected void retrieveProperties() {
        // do nothing
    }

    @Override
    public boolean isLoggable(final LogRecord record) {
        final int levelValue = logLevel.intValue();
        return record.getLevel().intValue() >= levelValue && levelValue != OFF.intValue() && dropSentryFilter.isLoggable(record);
    }

    @Override
    public void setFilter(final Filter newFilter) throws SecurityException {
        // do nothing
    }

    private class DropSentryFilter implements Filter {
        @Override
        public boolean isLoggable(LogRecord record) {
            String loggerName = record.getLoggerName();
            return loggerName == null || !loggerName.startsWith("io.sentry");
        }
    }
}