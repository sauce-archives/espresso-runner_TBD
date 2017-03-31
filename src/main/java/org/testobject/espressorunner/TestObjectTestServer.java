package org.testobject.espressorunner;

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

public class TestObjectTestServer {

	private static final Logger log = LoggerFactory.getLogger(TestObjectTestServer.class);
	private final Configuration config;

	public TestObjectTestServer(Configuration config) {
		this.config = config;
	}

	public void uploadApks() throws TestFailedException {
		String baseUrl = config.getBaseUrl();
		TestObjectClient client = TestObjectClient.Factory.create(baseUrl, getProxySettings());

		String username = config.getUsername();
		String password = config.getPassword();
		String app = config.getProject();
		Long testSuite = config.getTestSuite();
		String team = config.getTeam();
		List<String> methodsToRun = config.getTests();
		List<String> classesToRun = config.getClasses();
		List<String> annotationsToRun = config.getAnnotations();
		List<String> sizesToRun = config.getSizes();
		boolean failOnUnknown = config.getFailOnUnknown();
		int testTimeout = config.getTestTimeout();
		int checkFrequency = config.getCheckFrequency();

		boolean runAsPackage = config.getRunAsPackage();

		TestSuiteResource.InstrumentationTestSuiteRequest instrumentationTestSuiteRequest = new TestSuiteResource.InstrumentationTestSuiteRequest(
				runAsPackage);
		instrumentationTestSuiteRequest.methodsToRun = methodsToRun;
		instrumentationTestSuiteRequest.annotationsToRun = annotationsToRun;
		instrumentationTestSuiteRequest.classesToRun = classesToRun;
		instrumentationTestSuiteRequest.sizesToRun = sizesToRun;

		login(client, username, password);

		updateInstrumentationSuite(config.getTestApk(), config.getAppApk(), client, team, app, testSuite, instrumentationTestSuiteRequest);

		Instant start = Instant.now();

		long suiteReportId = client.startInstrumentationTestSuite(team, app, testSuite);

		TestSuiteReport suiteReport = client
				.waitForSuiteReport(team, app, suiteReportId, TimeUnit.MINUTES.toMillis(testTimeout),
						TimeUnit.SECONDS.toMillis(checkFrequency));

		try {
			writeSuiteReportXML(client, team, app, suiteReportId);
		} catch (IOException e) {
			log.warn("Failed to write test report to XML", e);
		}

		Instant end = Instant.now();

		Duration executionTime = Duration.between(start, end);

		int errors = countErrors(suiteReport, failOnUnknown);
		String downloadURL = String.format("%s/users/%s/projects/%s/automationReports/%d/download/zip", baseUrl, team, app, suiteReportId);
		String reportURL = String
				.format("%s/#/%s/%s/espresso/%d/reports/%d", baseUrl.replace("/api/rest", ""), team, app, testSuite, suiteReportId);

		StringBuilder msg = new StringBuilder();

		msg.append("\n");
		msg.append(getTestsList(suiteReport));
		msg.append("----------------------------------------------------------------------------------");
		msg.append("\n");
		msg.append(String.format("Ran %d tests in %s", suiteReport
				.getReports().size(), executionTime.getSeconds()));
		msg.append("\n");
		msg.append(suiteReport.getStatus());
		msg.append("\n");

		if (errors > 0) {
			msg.append(String.format("List of failed Test (Total errors : %d)", errors));
			msg.append("\n");
			msg.append(failedTestsList(suiteReport, reportURL));
			msg.append("\n");
		}

		msg.append(String.format("DownloadZIP URL: '%s'", downloadURL));
		msg.append("\n");
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

	private static int countErrors(TestSuiteReport suiteReport, boolean failOnUnknown) {
		int errors = 0;
		for (TestSuiteReport.ReportEntry reportEntry : suiteReport.getReports()) {
			if (isFailed(reportEntry, failOnUnknown)) {
				errors++;
			}
		}
		return errors;
	}

	private static boolean isFailed(TestSuiteReport.ReportEntry reportEntry, boolean failOnUnknown) {
		if (failOnUnknown) {
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
				list.append(String.format("%s - %s ....  %s", testName, deviceId, url));
				list.append("\n");
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

	private class TestFailedException extends Throwable {
		TestFailedException(String reason, Exception e) {
			super(reason, e);
		}
	}
}