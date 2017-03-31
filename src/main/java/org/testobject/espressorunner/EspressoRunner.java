package org.testobject.espressorunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;
import org.slf4j.LoggerFactory;

public class EspressoRunner {

    public static void main(String... args) {
        Configuration config = new Configuration();
        new JCommander(config, args);

        setLogLevel(config.getVerbosity());

        TestObjectTestServer testObjectTestServer = new TestObjectTestServer(config);
        testObjectTestServer.runTests();
    }

    private static void setLogLevel(String level) {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(level));
    }
}
