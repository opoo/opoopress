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
package org.opoo.press.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opoo.press.SiteConfig;
import org.opoo.util.MapUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Alex Lin
 *
 */
public class SiteConfigImpl extends LinkedHashMap<String, Object> implements SiteConfig{
	private static final Logger log = LoggerFactory.getLogger(SiteConfigImpl.class);
	
	private static final long serialVersionUID = 6443434786872527839L;
	private final Object lock = new Object();
	private final Map<String,Object> extraConfig;
	private final File configFile;
	private final File siteDir;
	private boolean loaded = false;
	private Yaml yaml;

	public SiteConfigImpl(File siteDir, Map<String,Object> extraConfig){
		if(siteDir == null || !siteDir.exists() || !siteDir.isDirectory() || !siteDir.canRead()){
			throw new IllegalArgumentException("Site directory not valid.");
		}
		this.configFile = new File(siteDir, "config.yml");
		this.extraConfig = extraConfig;
		this.siteDir = siteDir;
	}
	
	public SiteConfigImpl(File siteDir){
		this(siteDir, null);
	}
	
	void setYaml(Yaml yaml){
		this.yaml = yaml;
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void load(){
		if(yaml == null){
			yaml = new Yaml();
		}
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(configFile);
			Map<String,Object> config = (Map<String, Object>) yaml.load(stream);
			putAll(config);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("config file not found: " + configFile);
		}finally{
			IOUtils.closeQuietly(stream);
		}
		
		if(extraConfig != null){
			log.debug("Merge extra config to main config map.");
			putAll(extraConfig);
		}
		put("site", siteDir);
		loaded = true;
		
		fixRoot();
	}
	
	private void fixRoot(){
		String rootUrl = (String)super.get("root");
		if(rootUrl == null){
			put("root", "");
			return;
		}
		rootUrl = rootUrl.trim();
		if(rootUrl.equals("/") || "".equals(rootUrl)){
			put("root", "");
			return;
		}
		if(rootUrl.endsWith("/")){
			rootUrl = StringUtils.removeEnd(rootUrl, "/");
		}
		if(!rootUrl.startsWith("/")){
			rootUrl = "/" + rootUrl;
		}
		put("root", rootUrl);
	}

	/* (non-Javadoc)
	 * @see java.util.LinkedHashMap#get(java.lang.Object)
	 */
	@Override
	public Object get(Object key) {
		if(!loaded){
			synchronized (lock) {
				if(!loaded){
					load();
					loaded = true;
				}
			}
		}
		return super.get(key);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteConfig#getConfigFile()
	 */
	@Override
	public File getConfigFile() {
		return configFile;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteConfig#getExtraConfig()
	 */
	@Override
	public Map<String, Object> getExtraConfig() {
		return extraConfig;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteConfig#get(java.lang.String)
	 */
	@Override
	public Object get(String name) {
		return get((Object)name);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteConfig#get(java.lang.String, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String name, T defaultValue) {
		return (T) MapUtils.get(this, name, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteConfig#toMap()
	 */
	@Override
	public Map<String, Object> toMap() {
		return this;
	}
}
