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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.Site;
import org.opoo.press.importer.ImportException;

/**
 * @author Alex Lin
 * @goal import
 */
public class ImportMojo extends AbstractPressMojo{
	/**
	 * Importer name.
	 * 
	 * @parameter expression="${importer}"
	 */
	private String importerName;

	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractPressMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
		
		if(StringUtils.isBlank(importerName)){
			throw new MojoFailureException("importer name is required, e.g. -Dimporter=wordpress");
		}
		
		//Copy all system properties
		Map<String,Object> props = new HashMap<String,Object>();
		Properties properties = System.getProperties();
		Enumeration<?> names = properties.propertyNames();
		while(names.hasMoreElements()){
			String name = (String) names.nextElement();
			String value = properties.getProperty(name);
			if(value != null){
				props.put(name, value);
			}
		}
		
		try {
			Site site = createSite();
			getSiteManager().doImport(site, importerName, props);
		} catch (ImportException e) {
			throw new MojoFailureException(e.getMessage());
		} catch (Exception e) {
			throw new MojoFailureException(e.getMessage());
		}
	}
}
