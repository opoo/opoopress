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
import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.SiteManager;

/**
 * @author Alex Lin
 *
 */
public abstract class AbstractInstallMojo extends AbstractPressMojo{
	/**
	 * @parameter expression="${locale}"
	 */
	protected String locale = Locale.getDefault().toString();

	/**
	 * @parameter expression="${op.sample.post}" default-value="true"
	 */
	protected boolean createSamplePost = true;
	
	/**
     * Set this to 'true' to skip install.
     *
     * @parameter expression="${op.install.skip}" default-value="false"
     */
	protected boolean skipInstall = false;
	
	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractPressMojo#execute(org.opoo.press.SiteManager, java.io.File)
	 */
	@Override
	protected final void execute(SiteManager siteManager, File siteDir)
			throws MojoExecutionException, MojoFailureException {
		if(skipInstall){
			 getLog().info( "op.install.skip = true: Skipping install" );
		}else if(skipInstall(siteManager, siteDir)){
			getLog().info("Skipping install, site already installed.");
		}else{
			Locale loc = Locale.getDefault();
			if(StringUtils.isNotEmpty(locale)){
				loc = LocaleUtils.toLocale(locale);
			}
			
			getLog().info("Installing site...");
			try {
				siteManager.install(siteDir, loc, createSamplePost);
			} catch (Exception e) {
				throw new MojoFailureException(e.getMessage());
			}
		}
		
		afterInstall(siteManager, siteDir);
	}
	
	/**
	 * @param siteManager
	 * @param siteDir
	 * @return
	 */
	private boolean skipInstall(SiteManager siteManager, File siteDir) {
//		if(!siteDir.exists() || !siteDir.isDirectory()){
//			throw new MojoExecutionException("Site directory not exists or not valid, run 'mvn op:install -Dsite=" + siteDir.getName() + "' first.");
//		}
		return siteDir.exists() && siteDir.isDirectory();			
	}

	/**
	 * @param siteManager
	 * @param siteDir
	 */
	protected void afterInstall(SiteManager siteManager, File siteDir) 
		throws MojoExecutionException, MojoFailureException {
	}
}
