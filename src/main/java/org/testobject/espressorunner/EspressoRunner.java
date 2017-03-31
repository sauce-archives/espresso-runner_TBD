package org.testobject.espressorunner;

import com.beust.jcommander.*;

import java.util.List;

public class EspressoRunner {

    @Parameter(names = "--app", description = "Path to APK of app under test", validateWith = RequiredValidator.class)
    private String appApk = getEnvDefault("APP");

    @Parameter(names = "--test", description = "Path to test APK", validateWith = RequiredValidator.class)
    private String testApk = getEnvDefault("TEST");

    @Parameter(names = "--url")
    private String baseUrl = getEnvDefault("URL", "https://app.testobject.com/api/rest");

    @Parameter(names = {"--username", "-u"}, validateWith = RequiredValidator.class)
    private String username = getEnvDefault("USER");

    @Parameter(names = {"--password", "-p"}, validateWith = RequiredValidator.class)
    private String password = getEnvDefault("PASSWORD");

    @Parameter(names = {"--team", "-t"})
    private String team = getEnvDefault("TEAM");

    @Parameter(names = {"--project", "-p"}, validateWith = RequiredValidator.class)
    private String app = getEnvDefault("project");

    @Parameter(names = {"--suite", "-s"})
    private Long testSuite = Long.parseLong(getEnvDefault("SUITE"));

    @Parameter(names = "--tests", variableArity = true)
    private List<String> tests;

    @Parameter(names = "--classes", variableArity = true)
    private List<String> classes;

    @Parameter(names = "--annotations", variableArity = true)
    private List<String> annotations;

    @Parameter(names = "--sizes", variableArity = true)
    private List<String> sizes;

    @Parameter(names = "--timeout")
    private int testTimeout = Integer.parseInt(getEnvDefault("TIMEOUT", "60"));

    @Parameter(names = "--checkFrequency")
    private int checkFrequency = Integer.parseInt(getEnvDefault("CHECK_FREQUENCY", "30"));

    @Parameter(names = "--failOnUnknown")
    private boolean failOnUnknown = Boolean.parseBoolean(getEnvDefault("FAIL_ON_UNKNOWN", "false"));

    @Parameter(names = "--failOnError")
    private boolean failOnError = Boolean.parseBoolean(getEnvDefault("FAIL_ON_ERROR", "false"));

    @Parameter(names = "--runAsPackage")
    private boolean runAsPackage = Boolean.parseBoolean(getEnvDefault("RUN_AS_PACKAGE", "false"));

    public static void main(String... args) {
        EspressoRunner espressoRunner = new EspressoRunner();
        JCommander jc = new JCommander(espressoRunner, args);
        jc.setDefaultProvider(new EnvironmentVariableDefaults());
        espressoRunner.run();
    }

    private void run() {
        System.out.println("user: " + username + ", pass: " + password);
    }

    private String getEnvDefault(String option, String fallback) {
        String value = System.getenv(option);
        return Strings.isStringEmpty(option) ? fallback : value;
    }

    private String getEnvDefault(String option) {
        return getEnvDefault(option, null);
    }

    private class RequiredValidator implements IParameterValidator {
        public void validate(String name, String value) throws ParameterException {
            if (Strings.isStringEmpty(value)) {
                throw new ParameterException("Missing value for parameter " + name);
            }
        }
    }
}
