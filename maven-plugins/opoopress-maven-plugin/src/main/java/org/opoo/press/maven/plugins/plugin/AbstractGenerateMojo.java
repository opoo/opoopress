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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.Site;
import org.opoo.press.SiteManager;
import org.opoo.press.util.StaleUtils;

/**
 * @author Alex Lin
 */
public abstract class AbstractGenerateMojo extends AbstractSassCompileMojo{
    /**
     * Set this to 'true' to skip generate.
     * 
	 * @parameter expression="${op.generate.skip}" default-value="false"
	 */
	protected boolean skipGenerate;
	
	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractSassCompileMojo#afterSassCompile(org.opoo.press.SiteManager, java.io.File)
	 */
	@Override
	protected final void afterSassCompile(SiteManager siteManager, File siteDir)
			throws MojoExecutionException, MojoFailureException {
		Map<String,Object> config = new HashMap<String,Object>();
		config.put("show_drafts", showDrafts());
		config.put("debug", getLog().isDebugEnabled());
		Site site = siteManager.createSite(siteDir, config);
		
		//System.out.println("Site extra: " + config);
		//System.out.println("Site show drafts: " + site.showDrafts());
		
		if(skipGenerate){
			getLog().info( "op.generate.skip = true: Skipping generate" );
		}else if(skipGenerate(siteManager, siteDir, site)){
			getLog().info("Skipping generate, all output files are up to date.");
		}else{
			generate(site);
		}
		
		afterGenerate(siteManager, siteDir, site);
	}

	/**
	 * @param siteManager
	 * @param siteDir
	 * @param site
	 */
	protected void afterGenerate(SiteManager siteManager, File siteDir, Site site) 
		throws MojoExecutionException, MojoFailureException{
	}

	private boolean skipGenerate(SiteManager siteManager, File siteDir, Site site){
		return !StaleUtils.isSourceStale(site);
	}

	protected abstract boolean showDrafts();
	
	private void generate(Site site){
		long start = System.currentTimeMillis();
		site.build();
		long time = System.currentTimeMillis() - start;
		getLog().info("Generate time: " + time + "ms");
	}
}
