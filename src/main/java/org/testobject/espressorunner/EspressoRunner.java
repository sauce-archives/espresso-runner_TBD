package org.testobject.espressorunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testobject.api.TestObjectClient;
import org.testobject.rest.api.model.TestSuiteReport;
import org.testobject.rest.api.resource.TestSuiteResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

// TODO: Better name for this class
class EspressoRunner {

	private static final Logger log = LoggerFactory.getLogger(EspressoRunner.class);
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final Configuration config;

	EspressoRunner(Configuration config) {
		this.config = config;
		log.info("EspressoRunner initialized.");
		String prettyConfig = gson.toJson(config);
		log.debug("Configuration: " + prettyConfig);
	}

	void executeTests() throws TestFailedException {
		TestObjectClient client = createClient();
		TestSuiteResource.InstrumentationTestSuiteRequest instrumentationTestSuiteRequest = createSuiteRequest();
		login(client, config.getUsername(), config.getPassword());
		updateInstrumentationSuite(client, instrumentationTestSuiteRequest);

		Instant start = Instant.now();
		TestSuiteReport suiteReport = runTests(client);
		Instant end = Instant.now();
		Duration executionTime = Duration.between(start, end);

		int errors = countErrors(suiteReport);
		String message = printResults(suiteReport, executionTime, errors);

		if (errors == 0) {
			log.info(message);
		} else if (config.getFailOnError()) {
			throw new TestFailedException("failure during test suite execution of test suite " + config.getTestSuite(),
					new Exception(message));
		} else {
			log.warn(message);
		}
	}

	private String printResults(TestSuiteReport suiteReport, Duration executionTime, int errors) {
		String baseUrl = config.getBaseUrl();
		String team = config.getTeam();
		String project = config.getProject();
		Long suite = config.getTestSuite();
		Long suiteReportId = suiteReport.getId();
		String downloadURL = String.format("%s/users/%s/projects/%s/automationReports/%d/download/zip", baseUrl, team, project, suiteReportId);
		String reportURL = String
				.format("%s/#/%s/%s/espresso/%d/reports/%d", baseUrl.replace("/api/rest", ""), team, project, suite, suiteReportId);

		StringBuilder msg = new StringBuilder();

		msg.append(String.format("%n%s", getTestsList(suiteReport)));
		msg.append("----------------------------------------------------------------------------------%n");
		msg.append(String.format("Ran %d tests in %ss%n", suiteReport
				.getReports().size(), executionTime.getSeconds()));
		msg.append(String.format("%s%n", suiteReport.getStatus()));

		if (errors > 0) {
			msg.append(String.format("List of failed Test (Total errors : %d)%n", errors));
			msg.append(String.format("%s%n", failedTestsList(suiteReport, reportURL)));
		}

		msg.append(String.format("DownloadZIP URL: '%s'%n", downloadURL));
		msg.append(String.format("Report URL : '%s'", reportURL));

		return msg.toString();
	}

	private TestSuiteReport runTests(TestObjectClient client) {
		String team = config.getTeam();
		String project = config.getProject();
		Long testSuite = config.getTestSuite();
		long suiteReportId = client.startInstrumentationTestSuite(team, project, testSuite);
		TestSuiteReport suiteReport = client
				.waitForSuiteReport(team, project, suiteReportId, TimeUnit.MINUTES.toMillis(config.getTestTimeout()),
						TimeUnit.SECONDS.toMillis(config.getCheckFrequency()));
		try {
			writeSuiteReportXML(client, team, project, suiteReportId);
		} catch (IOException e) {
			log.warn("Failed to write test report to XML", e);
		}
		return suiteReport;
	}

	private TestObjectClient createClient() {
		TestObjectClient.ProxySettings proxySettings = getProxySettings();
		String baseUrl = config.getBaseUrl();
		return TestObjectClient.Factory.create(baseUrl, proxySettings);
	}

	private TestSuiteResource.InstrumentationTestSuiteRequest createSuiteRequest() {
		TestSuiteResource.InstrumentationTestSuiteRequest request = new TestSuiteResource.InstrumentationTestSuiteRequest(
				config.getRunAsPackage());
		request.methodsToRun = config.getTests();
		request.annotationsToRun = config.getAnnotations();
		request.classesToRun = config.getClasses();
		request.sizesToRun = config.getSizes();

		log.debug("InstrumentationTestSuiteRequest created: " + gson.toJson(request));

		return request;
	}

	private void writeSuiteReportXML(TestObjectClient client, String user, String app, long suiteReportId) throws IOException {
		Path xmlFolder = config.getXmlFolder();

		String filename = user + "-" + app + "-" + suiteReportId + ".xml";
		String xml = client.readTestSuiteXMLReport(user, app, suiteReportId);
		Files.createDirectories(xmlFolder);

		Path xmlFile = Files.write(xmlFolder.resolve(filename), xml.getBytes());
		log.info("Wrote XML report to '" + xmlFile + "'");
	}

	private void login(TestObjectClient client, String user, String password) {
		try {
			client.login(user, password);
			log.info("User " + user + " successfully logged in");
		} catch (Exception e) {
			throw new RuntimeException(String.format("Unable to login user %s", user), e);
		}
	}

	private void updateInstrumentationSuite(TestObjectClient client, TestSuiteResource.InstrumentationTestSuiteRequest request) {
		File testApk = config.getTestApk();
		File appApk = config.getAppApk();
		String team = config.getTeam();
		Long testSuite = config.getTestSuite();
		String project = config.getProject();
		try {
			client.updateInstrumentationTestSuite(team, project, testSuite, appApk, testApk, request);
			log.info(String.format("Uploaded appAPK : %s and testAPK : %s", appApk.getAbsolutePath(), testApk.getAbsolutePath()));
		} catch (Exception e) {
			throw new RuntimeException(String.format("Unable to update testSuite %s", testSuite), e);
		}
	}

	private int countErrors(TestSuiteReport suiteReport) {
		int errors = 0;
		for (TestSuiteReport.ReportEntry reportEntry : suiteReport.getReports()) {
			if (isFailed(reportEntry)) {
				errors++;
			}
		}
		return errors;
	}

	private boolean isFailed(TestSuiteReport.ReportEntry reportEntry) {
		if (config.getFailOnUnknown()) {
			return reportEntry.getView().getStatus() == TestSuiteReport.Status.FAILURE
					|| reportEntry.getView().getStatus() == TestSuiteReport.Status.UNKNOWN;
		} else {
			return reportEntry.getView().getStatus() == TestSuiteReport.Status.FAILURE;
		}
	}

	private static String getTestsList(TestSuiteReport suiteReport) {
		StringBuilder list = new StringBuilder();
		for (TestSuiteReport.ReportEntry reportEntry : suiteReport.getReports()) {
			String testName = getTestName(suiteReport, reportEntry.getKey().getTestId());
			String deviceId = reportEntry.getKey().getDeviceId();
			list.append(String.format("%s - %s .............  %s", testName, deviceId, reportEntry.getView().getStatus().toString()));
			list.append("\n");
		}
		return list.toString();
	}

	private static String failedTestsList(TestSuiteReport suiteReport, String baseReportUrl) {
		StringBuilder list = new StringBuilder();
		for (TestSuiteReport.ReportEntry reportEntry : suiteReport.getReports()) {
			if (reportEntry.getView().getStatus() == TestSuiteReport.Status.FAILURE) {
				String testName = getTestName(suiteReport, reportEntry.getKey().getTestId());
				String deviceId = reportEntry.getKey().getDeviceId();
				String url = String.format("%s/executions/%d",
						baseReportUrl, reportEntry.getView().getReportId());
				list.append(String.format("%s - %s ....  %s%n", testName, deviceId, url));
			}
		}
		return list.toString();
	}

	private static String getTestName(TestSuiteReport suiteReport, long testId) {
		for (TestSuiteReport.TestView testView : suiteReport.getTests()) {
			if (testView.getTestId() == testId) {
				return testView.getName();
			}
		}
		return "";
	}

	private static TestObjectClient.ProxySettings getProxySettings() {
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		String proxyUser = System.getProperty("http.proxyUser");
		String proxyPassword = System.getProperty("http.proxyPassword");

		return proxyHost != null ? new TestObjectClient.ProxySettings(proxyHost, Integer.parseInt(proxyPort), proxyUser, proxyPassword)
				: null;
	}

	class TestFailedException extends RuntimeException {
		TestFailedException(String reason, Exception e) {
			super(reason, e);
		}
	}
}