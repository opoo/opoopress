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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.tool.Creator;

/**
 * @author Alex Lin
 * @goal new-page
 */
public class NewPageMojo extends AbstractPressMojo {
    /**
    *
    * @parameter expression="${title}"
    */
    protected String title;
    
    /**
    *
    * @parameter expression="${name}"
    */
   protected String name;
    
	
	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractPressMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
		
		if(StringUtils.isBlank(title)){
			throw new MojoFailureException("'title' is required, use '-Dtitle=title'");
		}
		
		createSite();
		
		Creator creator = new Creator();
		try {
			creator.createNewPage(site, title, name);
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage());
		}
	}
}
