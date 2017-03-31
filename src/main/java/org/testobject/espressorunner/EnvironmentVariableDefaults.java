package org.testobject.espressorunner;

import com.beust.jcommander.IDefaultProvider;

public class EnvironmentVariableDefaults implements IDefaultProvider {
	public String getDefaultValueFor(String option) {
		throw new RuntimeException(option);
		/*String defaultFromEnvironment = System.getenv(option.toUpperCase());
		if (Strings.isStringEmpty(defaultFromEnvironment)) {
			throw new IllegalArgumentException(option);
		} else {
			return defaultFromEnvironment;
		}*/
	}
}
