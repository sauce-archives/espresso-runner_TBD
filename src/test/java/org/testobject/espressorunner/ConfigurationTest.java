package org.testobject.espressorunner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationTest {
	private Configuration configuration;

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

		assertEquals(Configuration.ENVIRONMENT_DEFAULTS.getDefaultValueFor("--url"), configuration.getBaseUrl());
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

	private void main(String... args) {
		configuration = new Configuration();
		JCommander jc = new JCommander();
		jc.setDefaultProvider(Configuration.ENVIRONMENT_DEFAULTS);
		jc.addObject(configuration);
		jc.parse(args);
	}
}