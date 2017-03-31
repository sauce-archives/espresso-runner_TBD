package org.testobject.espressorunner;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Strings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class Configuration {

	@Parameter(names = {"--help", "-h", "-?"}, description = "Displays details on usage", help = true)
	private boolean help = false;

	@Parameter(names = "--verbosity", description = "Logging level.")
	private String verbosity = getEnvDefault("VERBOSITY", "INFO");

	@Parameter(names = "--app", description = "Path to APK of app under test", validateWith = RequiredValidator.class)
	private String appApk = getEnvDefault("APP");

	@Parameter(names = "--test", description = "Path to test APK", validateWith = RequiredValidator.class)
	private String testApk = getEnvDefault("TEST");

	@Parameter(names = "--url", description = "URL of TestObject API endpoint")
	private String baseUrl = getEnvDefault("URL", "https://app.testobject.com/api/rest");

	@Parameter(names = "--username", description = "Your TestObject username", validateWith = RequiredValidator.class)
	private String username = getEnvDefault("USER_NAME");

	@Parameter(names = "--password", description = "Your TestObject password", validateWith = RequiredValidator.class)
	private String password = getEnvDefault("PASSWORD");

	@Parameter(names = "--team", description = "Your TestObject team")
	private String team = getEnvDefault("TEAM");

	@Parameter(names = "--project", description = "Your TestObject project", validateWith = RequiredValidator.class)
	private String project = getEnvDefault("PROJECT");

	@Parameter(names = "--suite", description = "ID of your Espresso suite within your project", validateWith = RequiredValidator.class)
	private Long testSuite = getEnvDefaultLong("SUITE");

	@Parameter(names = "--testsToRun", description = "Individual tests to run", variableArity = true)
	private List<String> tests;

	@Parameter(names = "--classesToRun", description = "Individual classes to run", variableArity = true)
	private List<String> classes;

	@Parameter(names = "--annotationsToRun", description = "Individual annotations to run", variableArity = true)
	private List<String> annotations;

	@Parameter(names = "--sizesToRun", description = "Test sizes to run", variableArity = true)
	private List<String> sizes;

	@Parameter(names = "--timeout", description = "Test timeout in minutes")
	private int testTimeout = Integer.parseInt(getEnvDefault("TIMEOUT", "60"));

	@Parameter(names = "--checkFrequency", description = "Interval in seconds to check test results")
	private int checkFrequency = Integer.parseInt(getEnvDefault("CHECK_FREQUENCY", "30"));

	@Parameter(names = "--failOnUnknown")
	private boolean failOnUnknown = Boolean.parseBoolean(getEnvDefault("FAIL_ON_UNKNOWN", "false"));

	@Parameter(names = "--failOnError")
	private boolean failOnError = Boolean.parseBoolean(getEnvDefault("FAIL_ON_ERROR", "false"));

	@Parameter(names = "--runAsPackage")
	private boolean runAsPackage = Boolean.parseBoolean(getEnvDefault("RUN_AS_PACKAGE", "false"));

	@Parameter(names = "--xmlFolder", description = "Folder where XML test results will be located")
	private String outputXml = getEnvDefault("xmlFolder", ".");

	String getEnvDefault(String option, String fallback) {
		String value = System.getenv(option);
		return Strings.isStringEmpty(value) ? fallback : value;
	}

	String getEnvDefault(String option) {
		return getEnvDefault(option, null);
	}

	/**
	 * Given the name of an environment variable, reads the value for it, and either returns null if it doesn't exist or returns the value
	 * converted to a Long
	 * @param option Name of the environment variable
	 * @return The value for the environment variable or null if none exists
	 */
	private Long getEnvDefaultLong(String option) {
		String value = getEnvDefault(option, null);
		return value == null ? null : Long.parseLong(value);
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public File getAppApk() {
		return new File(appApk);
	}

	public File getTestApk() {
		return new File(testApk);
	}

	public Long getTestSuite() {
		return testSuite;
	}

	public String getTeam() {
		return Strings.isStringEmpty(team) ? username : team;
	}

	public List<String> getTests() {
		return tests;
	}

	public List<String> getClasses() {
		return classes;
	}

	public List<String> getAnnotations() {
		return annotations;
	}

	public List<String> getSizes() {
		return sizes;
	}

	public boolean getFailOnUnknown() {
		return failOnUnknown;
	}

	public int getTestTimeout() {
		return testTimeout;
	}

	public int getCheckFrequency() {
		return checkFrequency;
	}

	public boolean getRunAsPackage() {
		return runAsPackage;
	}

	public boolean getFailOnError() {
		return failOnError;
	}

	public String getProject() {
		return project;
	}

	public String getVerbosity() {
		return verbosity;
	}

	public Path getXmlFolder() {
		return Paths.get(outputXml);
	}

	public boolean getHelpRequested() {
		return help;
	}

	public static class RequiredValidator implements IParameterValidator {
		public void validate(String name, String value) throws ParameterException {
			if (Strings.isStringEmpty(value)) {
				throw new ParameterException("Missing value for parameter " + name);
			}
		}
	}
}
