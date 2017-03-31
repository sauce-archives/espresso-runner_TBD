package org.testobject.espressorunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;
import org.slf4j.LoggerFactory;

public class EspressoRunner {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TestObjectTestServer.class);

    public static void main(String... args) {
        Configuration config = new Configuration();
        new JCommander(config, args);

        setLogLevel(config.getVerbosity());

        try {
            TestObjectTestServer testObjectTestServer = new TestObjectTestServer(config);
            testObjectTestServer.executeTests();
        } catch (Throwable t) {
            log.error("Uncaught error", t);
        }
    }

    private static void setLogLevel(String level) {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(level));
    }
}
