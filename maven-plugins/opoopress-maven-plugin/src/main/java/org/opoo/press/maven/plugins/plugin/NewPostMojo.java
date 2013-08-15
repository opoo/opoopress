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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.Site;
import org.opoo.press.SiteManager;

/**
 * Create new post file.
 * 
 * @author Alex Lin
 * @goal new-post
 */
public class NewPostMojo extends AbstractInstallMojo {

	/**
     * Set this to 'true' if new post is draft.
     *
     * @parameter expression="${draft}" default-value="false"
     */
    protected boolean isDraft;
    
    /**
     *
     * @parameter expression="${name}"
     */
    protected String name;
    
    /**
     * @parameter expression="${title}"
     * @required
     * @readonly
     */
    protected String title;
    
    /**
     * @parameter expression="${format}" default-value="markdown"
     */
    protected String format;
    
    /* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractInstallMojo#afterInstall(org.opoo.press.SiteManager, java.io.File)
	 */
	@Override
	protected void afterInstall(SiteManager siteManager, File siteDir)
			throws MojoExecutionException, MojoFailureException {
		if(StringUtils.isBlank(title)){
			throw new MojoFailureException("'title' is required, use '-Dtitle=title'");
		}

		Site site = siteManager.createSite(siteDir);

		try {
			siteManager.newPost(site, title, name, isDraft, format);
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage());
		}
	}
}
