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
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.Application;
import org.opoo.press.Site;
import org.opoo.press.impl.ContextImpl;

/**
 * @author Alex Lin
 *
 */
public class AbstractPressMojo extends AbstractMojo {
	 protected Site site;
	 
	/**
	 * 
	 * @parameter expression="${site}" alias="blog" default-value="${basedir}/site"
	 */
	protected File siteDir;

	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("The site directory is : " + siteDir.getAbsolutePath());
		//getLog().info("The site output directory is : " + outputDirectory.getAbsolutePath());
		checkDirectory();
	}
	
	private void checkDirectory() throws MojoExecutionException{
		if(!siteDir.exists() || !siteDir.isDirectory()){
			throw new MojoExecutionException("Site directory not exists or not valid, run 'mvn op:install -Dsite=" + siteDir.getName() + "' first.");
		}
	}

	protected void createSite(){
		createSite(false);
	}
	
	protected void createSite(boolean showDrafts){
		Map<String,Object> config = new HashMap<String,Object>();
		config.put("show_drafts", showDrafts);
		config.put("debug", getLog().isDebugEnabled());
		
		new ContextImpl().initialize();
		site = Application.getContext().getSiteManager().getSite(siteDir, config);
	}
}
