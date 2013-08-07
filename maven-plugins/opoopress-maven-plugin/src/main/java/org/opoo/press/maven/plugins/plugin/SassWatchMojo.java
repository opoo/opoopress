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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.SiteManager;
import org.opoo.press.support.Compass;

/**
 * Watch the SASS for changes and compile whenever it does. Check <code>compass watch</code>
 * on http://compass-style.org/help/tutorials/command-line/ for more details.
 * 
 * @author Alex Lin
 * @goal sass-watch
 */
public class SassWatchMojo extends AbstractInstallMojo {

	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractInstallMojo#afterInstall(org.opoo.press.SiteManager, java.io.File)
	 */
	@Override
	protected void afterInstall(SiteManager siteManager, File siteDir)
			throws MojoExecutionException, MojoFailureException {
		new Compass(siteDir).watch();
	}
}
