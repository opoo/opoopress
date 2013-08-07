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
package org.opoo.press.maven.plugins.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.support.Compass;

/**
 * Watch the SASS for changes and compile whenever it does. Check <code>compass watch</code>
 * on http://compass-style.org/help/tutorials/command-line/ for more details.
 * 
 * @author Alex Lin
 * @goal sass-watch
 */
public class SassWatchMojo extends AbstractPressMojo {

	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.press.AbstractSassMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
		
		new Compass(siteDir).watch();
	}
}