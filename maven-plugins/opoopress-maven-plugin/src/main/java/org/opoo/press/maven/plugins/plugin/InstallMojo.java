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
import org.opoo.press.tool.Installer;

/**
 * @author Alex Lin
 * @goal install
 */
public class InstallMojo extends AbstractPressMojo {
	private Installer installer = new Installer();

	/**
	 * @parameter expression="${locale}"
	 */
	protected String locale = Locale.getDefault().toString();
	
	/**
	 * @parameter expression="${op.sass.compile.skip}" default-value="false"
	 */
	protected boolean skipSassCompile = false;

	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.press.AbstractPressMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("The site directory is : " + siteDir.getAbsolutePath());
		
		Locale loc = Locale.getDefault();
		if(StringUtils.isNotEmpty(locale)){
			loc = LocaleUtils.toLocale(locale);
		}
		
		getLog().info("Installing site.");
		try {
			installer.install(siteDir, loc);
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage());
		}

		//if system property 'install.skip.sass.compile' is 'true', skip compile SASS/SCSS
		if(skipSassCompile){
			getLog().info("op.sass.compile.skip = true: Skip compile sass.");
			return;
		}

		File config = new File(siteDir, "config.rb");
		if(!config.exists()){
			getLog().warn("config.rb not exists, skip compile sass.");
		}else{
			getLog().info("Compiling SASS/SCSS to css.");

			new Compass(siteDir, getLog()).compile();
		}
	}
}
