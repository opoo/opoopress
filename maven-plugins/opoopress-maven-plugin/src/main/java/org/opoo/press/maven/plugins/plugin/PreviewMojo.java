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

import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.Site;
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
     * @parameter expression="${show-drafts}" default-value="false"
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
	 * @parameter expression="${op.generate.skip}" default-value="true"
	 */
	protected boolean skipGenerate;
	
	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.press.GenerateMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
		
		if(skipGenerate){
			getLog().info( "op.generate.skip = true: Skipping generating" );
		}else{
			Site site = createSite(showDrafts);
			generate(site);
		}
		
		Map<String, Object> extraConfig = buildExtraConfig(showDrafts);
		Preview preview = new Preview(getSiteManager(), siteDir, extraConfig, port, interval);
		try {
			preview.start();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}
}
