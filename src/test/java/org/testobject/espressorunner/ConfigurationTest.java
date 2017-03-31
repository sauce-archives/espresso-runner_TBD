package org.testobject.espressorunner;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationTest {
	private Configuration configuration;

	@Test
	void testUsernamePassword() {
		String username = "test";
		String password = "asdfasdfasdfasdfasdfasdf";
		main("--username", username, "--password", password);

		assertEquals(username, configuration.getUsername());
		assertEquals(password, configuration.getPassword());
	}

	@Test
	void testApk() {
		main();

		configuration.getAppApk();
	}

	private void main(String... args) {
		configuration = new Configuration();
		new JCommander(configuration, args);
	}
}