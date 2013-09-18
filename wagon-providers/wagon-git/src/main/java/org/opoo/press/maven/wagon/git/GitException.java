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
package org.opoo.press.maven.wagon.git;

/**
 * 
 * @author Alex Lin
 *
 */
public class GitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4515490143322625634L;

	/**
	 * 
	 */
	public GitException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public GitException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public GitException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public GitException(Throwable cause) {
		super(cause);
	}
}
