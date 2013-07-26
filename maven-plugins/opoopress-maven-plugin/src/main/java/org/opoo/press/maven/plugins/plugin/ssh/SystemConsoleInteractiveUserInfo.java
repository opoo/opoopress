/*
 * Copyright 2013 Alex Lin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opoo.press.maven.plugins.plugin.ssh;

import java.io.Console;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo;

/**
 * @author Alex Lin
 *
 */
public class SystemConsoleInteractiveUserInfo implements InteractiveUserInfo {
	private Console console = System.console();
	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo#promptYesNo(java.lang.String)
	 */
	@Override
	public boolean promptYesNo(String message) {
		List<String> possibleValues = Arrays.asList(new String[] { "y", "n" });
		message += " (y/n): ";
		String line;

		do {
			line = new String(console.readLine(message));

			if (line != null && !possibleValues.contains(line)) {
				console.printf("Invalod selection");
			}
		} while (line == null || !possibleValues.contains(line));

		return "y".equalsIgnoreCase(line);
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo#showMessage(java.lang.String)
	 */
	@Override
	public void showMessage(String message) {
		console.printf("message");
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo#promptPassword(java.lang.String)
	 */
	@Override
	public String promptPassword(String message) {
		return new String(console.readPassword(message + ": "));
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo#promptPassphrase(java.lang.String)
	 */
	@Override
	public String promptPassphrase(String message) {
		return promptPassword(message);
	}
}
