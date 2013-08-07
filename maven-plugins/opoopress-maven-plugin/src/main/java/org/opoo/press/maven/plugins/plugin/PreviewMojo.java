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
import org.opoo.press.Site;
import org.opoo.press.SiteManager;
import org.opoo.press.support.Preview;

/**
 * Start a web server for preview, monitor the site for changes and generate whenever it does.
 * 
 * @author Alex Lin
 * @goal preview
 */
public class PreviewMojo extends AbstractGenerateMojo{
	/**
     * Set this to 'true' to generate draft posts.
     *
     * @parameter expression="${op.show-drafts}" default-value="false"
     */
    protected boolean showDrafts;
	
	 /**
     * The amount of time in seconds to wait between checks of the site directory.
     *
     * @parameter expression="${interval}" default-value="5"
     */
	private int interval;
	
    /**
     * The port to execute the HTTP server on.
     *
     * @parameter expression="${port}" default-value="8080"
     */
    private int port;
    
    /**
     * Set this to 'true' to skip preview.
     * 
	 * @parameter expression="${op.preview.skip}" default-value="false"
	 */
	protected boolean skipPreview;
	
	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractGenerateMojo#showDrafts()
	 */
	@Override
	protected boolean showDrafts() {
		return showDrafts;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractGenerateMojo#afterGenerate(org.opoo.press.SiteManager, java.io.File, org.opoo.press.Site)
	 */
	@Override
	protected void afterGenerate(SiteManager siteManager, File siteDir,
			Site site) throws MojoExecutionException, MojoFailureException {
		if(skipPreview){
			getLog().info( "op.preview.skip = true: Skipping preview" );
			return;
		}
		
		Preview preview = new Preview(siteManager, site, port, interval);
		try {
			preview.start();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}
}
