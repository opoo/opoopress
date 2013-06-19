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
package org.opoo.press;

/**
 * The global application instance class.
 * 
 * @author Alex Lin
 *
 */
public class Application {
	/**
	 * The global context.
	 */
	private static Context context;
	
	/**
	 * Check whether the application is initialized.
	 * @return
	 */
	public static boolean isInitialized(){
		return context != null;
	}

	/**
	 * @return the context
	 */
	public static Context getContext() {
		if(!isInitialized()){
			throw new IllegalStateException("Application not initialized.");
		}
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public static void setContext(Context context) {
		Application.context = context;
	}
}
