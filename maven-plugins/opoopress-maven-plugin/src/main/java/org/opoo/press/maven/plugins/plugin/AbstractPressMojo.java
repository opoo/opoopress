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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.Application;
import org.opoo.press.SiteManager;
import org.opoo.press.impl.ContextImpl;

/**
 * @author Alex Lin
 */
public abstract class AbstractPressMojo extends AbstractMojo {
	/**
	 * Site directory.
	 * 
	 * @parameter expression="${site}" alias="blog" default-value="${basedir}/site"
     * @required
     * @readonly
	 */
	private File siteDir;

	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("The site directory is : " + siteDir.getAbsolutePath());
		execute(getSiteManager(), siteDir);
	}

	private SiteManager getSiteManager(){
		new ContextImpl().initialize();
		return Application.getContext().getSiteManager();
	}
	
	protected abstract void execute(SiteManager siteManager, File siteDir) throws MojoExecutionException, MojoFailureException;
}
