/*
 * Copyright 2014 Alex Lin.
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
package org.opoo.press.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Site;
import org.opoo.press.Theme;
import org.opoo.press.ThemeBuilder;
import org.opoo.press.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Alex Lin
 * @since 1.2
 */
public class ThemeImpl implements Theme{
	private static final Logger log = LoggerFactory.getLogger(ThemeImpl.class);
	
	private final File path;
	private final File configFile;
	private Map<String,Object> config;
	
	private File source;
	private File assets;
	private File templates;
	
	private final SiteImpl site;
	private ThemeBuilder builder;
	
	@SuppressWarnings("unchecked")
	public ThemeImpl(File path, Site site){
		this.site = (site instanceof SiteImpl)? (SiteImpl)site : null;
		try {
			this.path = path.getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		configFile = new File(path, "theme.yml");
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(configFile);
			config = new Yaml().loadAs(stream, Map.class);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("config file not found: " + configFile);
		}finally{
			IOUtils.closeQuietly(stream);
		}
		
		//
		String sourceConfig = (String) config.get("source");
		if(StringUtils.isBlank(sourceConfig)){
			sourceConfig = "source";
		}
		source = new File(path, sourceConfig);
		
		//
		String assetsConfig = (String) config.get("assets");
		if(StringUtils.isBlank(assetsConfig)){
			sourceConfig = "assets";
		}
		assets = new File(path, "assets");
		
		//
		String templatesConfig = (String) config.get("templates");
		if(StringUtils.isBlank(templatesConfig)){
			sourceConfig = "templates";
		}
		templates = new File(path, "templates");
		
		String builderClassName = (String)config.get("builder");
		if(StringUtils.isNotBlank(builderClassName)){
			this.builder = (ThemeBuilder) ClassUtils.newInstance(builderClassName, site);
			log.info("Using CSS builder: {}", builder);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Theme#getPath()
	 */
	@Override
	public File getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Theme#getSource()
	 */
	@Override
	public File getSource() {
		return source;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Theme#getTemplates()
	 */
	@Override
	public File getTemplates() {
		return templates;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Theme#getAssets()
	 */
	@Override
	public File getAssets() {
		return assets;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Theme#build()
	 */
	@Override
	public void build() {
		if(site != null){
			site.getProcessors().beforeBuildTheme(this);
		}
		
		if(builder != null){
			builder.build(this);
		}
		
		if(site != null){
			site.getProcessors().afterBuildTheme(this);		
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Theme#watch()
	 */
	@Override
	public void watch() {
		if(builder != null){
			builder.watch(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Theme#get(java.lang.String)
	 */
	@Override
	public Object get(String name) {
		return config.get(name);
	}
}
