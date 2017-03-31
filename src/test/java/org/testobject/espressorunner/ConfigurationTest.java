package org.testobject.espressorunner;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationTest {
	private Configuration configuration;

	@Test
	void testRequiredArgs() {
		String username = "test";
		String password = "asdfasdfasdfasdfasdfasdf";
		main("--username", username, "--password", password);

		assertEquals(configuration.getUsername(), username);
		assertEquals(configuration.getPassword(), password);
	}

	@Test
	void testMissingRequiredArgs() {
		//assertThrows(ParameterException.class, this::main);
	}

	@Test
	void testEnvironmentDefault() {
		String defaultName = "default";
		configuration = new Configuration() {
			@Override
			String getEnvDefault(String option) {
				return defaultName;
			}
		};
		new JCommander(configuration);

		assertEquals(defaultName, configuration.getUsername());
	}

	@Test
	void testEnvironmentDefaultOverridden() {
		String defaultName = "default";
		configuration = new Configuration() {
			@Override
			String getEnvDefault(String option) {
				return defaultName;
			}
		};
		String overriddenName = "override";
		new JCommander(configuration, "--username", overriddenName);

		assertEquals(overriddenName, configuration.getUsername());
	}

	private void main(String... args) {
		configuration = new Configuration();
		new JCommander(configuration, args);
	}
}