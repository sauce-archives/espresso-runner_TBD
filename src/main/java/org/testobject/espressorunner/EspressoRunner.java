package org.testobject.espressorunner;

import com.beust.jcommander.JCommander;

public class EspressoRunner {

    public static void main(String... args) {
        Configuration config = new Configuration();
        new JCommander(config, args);
        TestObjectTestServer testObjectTestServer = new TestObjectTestServer(config);
        testObjectTestServer.runTests();
    }

}
