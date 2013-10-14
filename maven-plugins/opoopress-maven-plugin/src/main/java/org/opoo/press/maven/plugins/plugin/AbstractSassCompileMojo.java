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
import org.opoo.press.CompassConfig;
import org.opoo.press.SiteManager;
import org.opoo.press.support.Compass;
import org.opoo.press.util.StaleUtils;

/**
 * @author Alex Lin
 *
 */
public class AbstractSassCompileMojo extends AbstractInstallMojo{
	/**
     * Set this to 'true' to skip SASS/SCSS compile.
     *
     * @parameter expression="${op.sass.compile.skip}" default-value="false"
     */
	protected boolean skipSassCompile = false;

	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractInstallMojo#afterInstall(org.opoo.press.SiteManager, java.io.File)
	 */
	@Override
	protected final void afterInstall(SiteManager siteManager, File siteDir)
			throws MojoExecutionException, MojoFailureException {
		if(skipSassCompile){
			 getLog().info( "op.sass.compile.skip = true: Skipping sass compile" );
		}else if(!new File(siteDir, "config.rb").exists()){
			//skipping compile if config.rb not exists
			getLog().warn("Compass/sass config file not exists, skipping sass compile");
			skipSassCompile = true;
		}else if(skipSassCompile(siteManager, siteDir)){
			getLog().info("Skipping sass compile, css file is up to date.");
		}else{
			new Compass(siteDir).compile();
		}
		
		afterSassCompile(siteManager, siteDir);
	}

	protected void afterSassCompile(SiteManager siteManager, File siteDir) 
			throws MojoExecutionException, MojoFailureException {
	}

	private boolean skipSassCompile(SiteManager siteManager, File siteDir) {
		CompassConfig compassConfig = siteManager.createCompassConfig(siteDir);
		boolean compassStale = StaleUtils.isCompassStale(compassConfig);
		return !compassStale;
	}
}
