package org.testobject.espressorunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;
import org.slf4j.LoggerFactory;

public class Main {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EspressoRunner.class);

    public static void main(String... args) {
        Configuration config = new Configuration();
        JCommander jc = new JCommander(config);
        jc.setDefaultProvider(Configuration.ENVIRONMENT_DEFAULTS);
        jc.setProgramName("espressorunner.jar");
        jc.parse(args);

        if (config.getHelpRequested()) {
            jc.usage();
        } else {
			setLogLevel(config.getVerbosity());
			try {
                EspressoRunner espressoRunner = new EspressoRunner(config);
                espressoRunner.executeTests();
            } catch (Throwable t) {
                log.error("Uncaught error", t);
            }
        }
    }

    private static void setLogLevel(String level) {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(level));
    }
}
