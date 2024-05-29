package com.personal.jre_creator;

import org.junit.jupiter.api.Test;

import com.utils.test.TestInputUtils;

class AppStartJreCreatorTest {

	@Test
	void testMain() {

		final String[] args;
		final int input = TestInputUtils.parseTestInputNumber("1");
		if (input == 1) {
			args = new String[] {
					"C:\\IVI\\Apps\\Java\\jdk21",
					"C:\\IVI\\Apps\\Java\\abc\\jre21"
			};
		} else {
			throw new RuntimeException();
		}

		AppStartJreCreator.main(args);
	}
}
