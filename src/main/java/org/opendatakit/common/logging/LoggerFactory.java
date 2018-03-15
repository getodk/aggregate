package org.opendatakit.common.logging;

import io.sentry.Sentry;
import io.sentry.appengine.AppEngineSentryClientFactory;
import io.sentry.jul.SentryHandler;
import org.slf4j.Logger;

import static java.util.logging.Level.WARNING;
import static org.opendatakit.aggregate.buildconfig.BuildConfig.*;

/**
 * This Logger factory class is a drop-in replacement of the LoggerFactory in SLF4J
 * used to set Sentry support up while bootstrapping the app in memory.
 */
public class LoggerFactory {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LoggerFactory.class);

    static {
        if (SENTRY_ENABLED) {
            Sentry.init(String.format(
                    "%s?release=%s&stacktrace.app.packages=org.opendatakit&uncaught.handler.enabled=false&tags=jvm:%s",
                    SENTRY_DSN,
                    VERSION,
                    System.getProperty("java.version")
            ), new AppEngineSentryClientFactory());
            SentryHandler handler = new GAESafeSentryHandler();
            handler.setLevel(WARNING);
            java.util.logging.Logger.getLogger("").addHandler(handler);
            LOGGER.info("Logging context configured to use Sentry handler");
        } else {
            LOGGER.info("Logging context configured");
        }
    }

    public static Logger getLogger(Class clazz) {
        return org.slf4j.LoggerFactory.getLogger(clazz);
    }

    public static Logger getLogger(String name) {
        return org.slf4j.LoggerFactory.getLogger(name);
    }
}
