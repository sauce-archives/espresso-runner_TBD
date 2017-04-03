package org.testobject.espressorunner;

import com.beust.jcommander.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class Configuration {

	@Parameter(names = "--app", description = "Path to APK of app under test", required = true, order = 0)
	private String appApk;

	@Parameter(names = "--test", description = "Path to test APK", required = true, order = 1)
	private String testApk;

	@Parameter(names = "--username", description = "Your TestObject username", required = true, order = 2)
	private String username;

	@Parameter(names = "--password", description = "Your TestObject password", required = true, order = 3)
	private String password;

	@Parameter(names = "--project", description = "Your TestObject project", required = true, order = 4)
	private String project;

	@Parameter(names = "--suite", description = "ID of your Espresso suite within your project", required = true, order = 5)
	private Long testSuite;

	@Parameter(names = "--testsToRun", description = "Individual tests to run", variableArity = true, order = 6)
	private List<String> tests;

	@Parameter(names = "--classesToRun", description = "Individual classes to run", variableArity = true, order = 7)
	private List<String> classes;

	@Parameter(names = "--annotationsToRun", description = "Individual annotations to run", variableArity = true, order = 8)
	private List<String> annotations;

	@Parameter(names = "--sizesToRun", description = "Test sizes to run", variableArity = true, order = 9)
	private List<String> sizes;

	@Parameter(names = {"--help", "-h", "-?"}, description = "Displays details on usage", help = true, order = 10)
	private boolean help;

	@Parameter(names = "--verbosity", description = "Logging level.")
	private String verbosity;

	@Parameter(names = "--team", description = "Your TestObject team")
	private String team;

	@Parameter(names = "--timeout", description = "Test timeout in minutes")
	private int testTimeout;

	@Parameter(names = "--checkFrequency", description = "Interval in seconds to check test results")
	private int checkFrequency;

	@Parameter(names = "--failOnUnknown", description = "Abort if an unknown error occurs")
	private boolean failOnUnknown;

	@Parameter(names = "--failOnError", description = "Abort if a test failure occurs")
	private boolean failOnError;

	@Parameter(names = "--runAsPackage", description = "Runs the test APK without any configuration")
	private boolean runAsPackage;

	@Parameter(names = "--xmlFolder", description = "Folder where XML test results will be located")
	private String outputXml;

	@Parameter(names = "--url", description = "URL of TestObject API endpoint")
	private String baseUrl;

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

	static IDefaultProvider ENVIRONMENT_DEFAULTS = new IDefaultProvider() {

		private String getEnvDefault(String option, String fallback) {
			String value = System.getenv(option);
			return Strings.isStringEmpty(value) ? fallback : value;
		}

		private String getEnvDefault(String option) {
			return getEnvDefault(option, null);
		}

		@Override
		public String getDefaultValueFor(String option) {
			switch (option) {
			case "--verbosity":
				return getEnvDefault("VERBOSITY", "INFO");
			case "--app":
				return getEnvDefault("APP");
			case "--test":
				return getEnvDefault("TEST");
			case "--url":
				return getEnvDefault("URL", "https://app.testobject.com/api/rest");
			case "--username":
				return getEnvDefault("USER_NAME");
			case "--password":
				return getEnvDefault("PASSWORD");
			case "--team":
				return getEnvDefault("TEAM");
			case "--project":
				return getEnvDefault("PROJECT");
			case "--suite":
				return getEnvDefault("SUITE");
			case "--testsToRun":
				return getEnvDefault("TESTS_TO_RUN");
			case "--classesToRun":
				return getEnvDefault("CLASSES_TO_RUN");
			case "--annotationsToRun":
				return getEnvDefault("ANNOTATIONS_TO_RUN");
			case "--sizesToRun":
				return getEnvDefault("SIZES_TO_RUN");
			case "--timeout":
				return getEnvDefault("TIMEOUT", "60");
			case "--checkFrequency":
				return getEnvDefault("CHECK_FREQUENCY", "30");
			case "--failOnUnknown":
				return getEnvDefault("FAIL_ON_UNKNOWN");
			case "--failOnError":
				return getEnvDefault("FAIL_ON_ERROR");
			case "--runAsPackage":
				return getEnvDefault("RUN_AS_PACKAGE");
			case "--xmlFolder":
				return getEnvDefault("xmlFolder", ".");
			default:
				return null;
			}
		}
	};
}
