package org.testobject.espressorunner;

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
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

// TODO: Better name for this class
class TestObjectTestServer {

	private static final Logger log = LoggerFactory.getLogger(TestObjectTestServer.class);
	private final Configuration config;

	TestObjectTestServer(Configuration config) {
		this.config = config;
		log.info("TestObjectTestServer started");
		String prettyConfig = new GsonBuilder().setPrettyPrinting().create().toJson(config);
		log.debug(prettyConfig);
	}

	void runTests() throws TestFailedException {
		String baseUrl = config.getBaseUrl();
		TestObjectClient client = TestObjectClient.Factory.create(baseUrl, getProxySettings());

		String project = config.getProject();
		Long testSuite = config.getTestSuite();
		String team = config.getTeam();
		List<String> methodsToRun = config.getTests();
		List<String> classesToRun = config.getClasses();
		List<String> annotationsToRun = config.getAnnotations();
		List<String> sizesToRun = config.getSizes();
		int checkFrequency = config.getCheckFrequency();

		boolean runAsPackage = config.getRunAsPackage();

		TestSuiteResource.InstrumentationTestSuiteRequest instrumentationTestSuiteRequest = new TestSuiteResource.InstrumentationTestSuiteRequest(
				runAsPackage);
		instrumentationTestSuiteRequest.methodsToRun = methodsToRun;
		instrumentationTestSuiteRequest.annotationsToRun = annotationsToRun;
		instrumentationTestSuiteRequest.classesToRun = classesToRun;
		instrumentationTestSuiteRequest.sizesToRun = sizesToRun;

		login(client, config.getUsername(), config.getPassword());

		updateInstrumentationSuite(config.getTestApk(), config.getAppApk(), client, team, project, testSuite, instrumentationTestSuiteRequest);

		Instant start = Instant.now();

		long suiteReportId = client.startInstrumentationTestSuite(team, project, testSuite);

		TestSuiteReport suiteReport = client
				.waitForSuiteReport(team, project, suiteReportId, TimeUnit.MINUTES.toMillis(config.getTestTimeout()),
						TimeUnit.SECONDS.toMillis(checkFrequency));

		try {
			writeSuiteReportXML(client, team, project, suiteReportId);
		} catch (IOException e) {
			log.warn("Failed to write test report to XML", e);
		}

		Instant end = Instant.now();

		Duration executionTime = Duration.between(start, end);

		int errors = countErrors(suiteReport);
		String downloadURL = String.format("%s/users/%s/projects/%s/automationReports/%d/download/zip", baseUrl, team, project, suiteReportId);
		String reportURL = String
				.format("%s/#/%s/%s/espresso/%d/reports/%d", baseUrl.replace("/api/rest", ""), team, project, testSuite, suiteReportId);

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

		if (errors == 0) {
			log.info(msg.toString());
		} else {
			if (config.getFailOnError()) {
				throw new TestFailedException("failure during test suite execution of test suite " + testSuite,
						new Exception(msg.toString()));
			}
		}
	}

	private void writeSuiteReportXML(TestObjectClient client, String user, String app, long suiteReportId) throws IOException {
		Path localDirectory = Paths.get("testobject");

		String filename = user + "-" + app + "-" + suiteReportId + ".xml";
		String xml = client.readTestSuiteXMLReport(user, app, suiteReportId);
		if (!Files.isDirectory(localDirectory)) {
			Files.createDirectory(localDirectory);
		}

		Files.write(localDirectory.resolve(filename), xml.getBytes());
		log.info("Wrote XML report to '" + filename + "'");
	}

	private void login(TestObjectClient client, String user, String password) {
		try {
			client.login(user, password);

			log.info(String.format("user %s successfully logged in", user));
		} catch (Exception e) {
			throw new RuntimeException(String.format("unable to login user %s", user), e);
		}
	}

	private void updateInstrumentationSuite(File testApk, File appAk, TestObjectClient client, String team, String app, Long testSuite,
			TestSuiteResource.InstrumentationTestSuiteRequest request) {
		try {
			client.updateInstrumentationTestSuite(team, app, testSuite, appAk, testApk, request);
			log.info(String.format("Uploaded appAPK : %s and testAPK : %s", appAk.getAbsolutePath(), testApk.getAbsolutePath()));
		} catch (Exception e) {
			throw new RuntimeException(String.format("unable to update testSuite %s", testSuite), e);
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