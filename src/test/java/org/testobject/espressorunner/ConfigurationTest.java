package org.testobject.espressorunner;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationTest {
	private Configuration configuration;
	private IDefaultProvider defaults;

	private String mockTest = "test";
	private String mockApp = "app";
	private String mockUsername = "username";
	private String mockPassword = "password";
	private String mockProject = "project";
	private String mockSuite = "12";
	private String[] mockArgs = new String[] { "--test", mockTest, "--app", mockApp, "--suite", mockSuite, "--username", mockUsername,
			"--password", mockPassword, "--project", mockProject };

	@Test
	void testRequiredArgs() {
		main(mockArgs);

		assertEquals(configuration.getTestApk().toString(), mockTest);
		assertEquals(configuration.getUsername(), mockUsername);
		assertEquals(configuration.getPassword(), mockPassword);
		assertEquals(configuration.getProject(), mockProject);
		Long suiteLong = Long.parseLong(mockSuite);
		assertEquals(configuration.getTestSuite(), suiteLong);
	}

	@Test
	void testMissingRequiredArgs() {
		assertThrows(ParameterException.class, this::main);
	}

	@Test
	void testDefault() {
		main(mockArgs);

		assertEquals(defaults.getDefaultValueFor("--url"), configuration.getBaseUrl());
	}

	@Test
	void testDefaultOverridden() {
		String overridden = "override";
		List<String> argsWithOverride = new ArrayList<>(Arrays.asList(mockArgs));
		argsWithOverride.add("--url");
		argsWithOverride.add(overridden);
		main(argsWithOverride.toArray(new String[0]));

		assertEquals(overridden, configuration.getBaseUrl());
	}

	private void main(String ... args) {
		main(new HashMap<>(), args);
	}

	private void main(Map<String, String> environmentVariables, String... args) {
		configuration = new Configuration();
		defaults = new Configuration.EnvironmentDefaultProvider() {
			@Override
			String getEnvDefault(String option, String fallback) {
				return environmentVariables.getOrDefault(option, fallback);
			}
		};
		JCommander jc = new JCommander(configuration);
		jc.setDefaultProvider(defaults);
		jc.parse(args);
	}
}