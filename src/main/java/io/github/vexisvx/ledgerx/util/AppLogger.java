package io.github.vexisvx.ledgerx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {
    private static final Logger log = LoggerFactory.getLogger("LedgerX");
    public static void info(String m) { log.info(m); }
    public static void warn(String m) { log.warn(m); }
    public static void error(String m, Throwable t) { log.error(m, t); }
}
