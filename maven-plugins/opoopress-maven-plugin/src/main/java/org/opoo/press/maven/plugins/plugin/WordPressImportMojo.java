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
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.importer.ImportException;
import org.opoo.press.importer.WordPressImporter;

/**
 * Import posts and pages from the XML file that exported from WordPress.
 * 
 * @author Alex Lin
 * @goal wordpress-import
 */
public class WordPressImportMojo extends AbstractPressMojo{
	/**
	 * The XML file that exported from WordPress.
	 * @parameter expression="${file}"
	 */
	protected File file;
	
	/**
	 * Set this to 'true' to import drafts.
	 * @parameter expression="${import-drafts}" default-value="false"
	 */
	private boolean importDrafts = false;
	
	/**
	 * Set this to 'true' to import author info.
	 * @parameter expression="${import-author}" default-value="false"
	 */
	private boolean importAuthor = false;
	
	/**
	 * The permalink style of WordPress.
	 * Such as <code>/%year%/%monthnum%/%postname%/%post_id%/</code>.
	 * 
	 * @parameter expression="${permalink-style}"
	 */
	private String permalinkStyle;

	/**
	 * The directory that imported files to write.
	 * @parameter expression="${import-dir}"
	 */
	private String importDir;
	
	/**
	 * 
	 * @parameter expression="${replace-entries}"
	 */
	private Map<String,String> replaceEntries;


	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractPressMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Map<String,Object> props = new HashMap<String,Object>();
		
		if(file == null || !file.exists() || !file.isFile()){
			throw new MojoFailureException("the wordpress exported XML file is required, use '-Dfile=/path/to/file.xml'");
		}
		
		if(StringUtils.isBlank(permalinkStyle)){
			throw new MojoFailureException("permalink style is required, \nsuch as '-Dpermalink-style=/%year%/%monthnum%/%postname%/%post_id%/");
		}else{
			props.put("permalink_style", permalinkStyle);
		}
		props.put("include_drafts", importDrafts);
		props.put("include_author", importAuthor);
		
		if(StringUtils.isNotBlank(importDir)){
			props.put("import_dir", importDir);
		}
		
		Map<String, String> entries = new HashMap<String,String>();
		entries.put("$", "${'$'}");
		if(replaceEntries != null){
			getLog().debug("Add replacement entries: " + replaceEntries);
			entries.putAll(replaceEntries);
		}
		props.put("content_replacements", entries);

		WordPressImporter importer = new WordPressImporter(props);
		URI uri = file.toURI();
		try {
			importer.doImport(site, uri);
		} catch (ImportException e) {
			throw new MojoFailureException(e.getMessage());
		}
	}
}
