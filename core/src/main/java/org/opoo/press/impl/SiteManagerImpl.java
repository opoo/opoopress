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
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Application;
import org.opoo.press.Context;
import org.opoo.press.Site;
import org.opoo.press.SiteManager;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Alex Lin
 *
 */
public class SiteManagerImpl implements SiteManager {
	private static final Log log = LogFactory.getLog(SiteManagerImpl.class);

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.SiteManager#getSite(java.util.Map)
	 */
	@Override
	public Site getSite(Map<String, Object> config) {
		fixRoot(config);
		return new SiteImpl(config);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#getSite(java.io.File)
	 */
	@Override
	public Site getSite(File siteDir) {
		return getSite(siteDir, null);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#getSite(java.io.File, java.util.Map)
	 */
	@Override
	public Site getSite(File siteDir, Map<String, Object> extraOptions) {
		Map<String, Object> config = loadConfig(siteDir, extraOptions);
		return getSite(config);
	}
	
	@SuppressWarnings("unchecked")
	Map<String, Object> loadConfig(File siteDir, Map<String,Object> extraConfig){
		Context context = Application.getContext();
		Yaml yaml = context.getYaml();
		File configFile = new File(siteDir, "config.yml");
		Map<String,Object> config = null;
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(configFile);
			config = (Map<String, Object>) yaml.load(stream);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Site config file not found: " + e.getMessage());
		}finally{
			IOUtils.closeQuietly(stream);
		}
		
		if(extraConfig != null && !extraConfig.isEmpty()){
			log.debug("Merge extra config to main config map.");
			config.putAll(extraConfig);
		}
		
		config.put("site", siteDir);
		
		//show drafts
		if("true".equals(config.get("show_drafts"))){
			log.info("+ Show drafts option set 'ON'");
		}
		
		//debug option
		boolean debug = "true".equals(config.get("debug"));
		
		if(debug){
			for(Map.Entry<String, Object> en: config.entrySet()){
				String name = en.getKey();
				name = StringUtils.leftPad(name, 25);
				log.info(name + ": " + en.getValue());
			}
		}
		return config;
	}
	
	private void fixRoot(Map<String, Object> config){
		String rootUrl = (String)config.get("root");
		if(rootUrl == null){
			config.put("root", "");
			return;
		}
		rootUrl = rootUrl.trim();
		if(rootUrl.equals("/") || "".equals(rootUrl)){
			config.put("root", "");
			return;
		}
		if(rootUrl.endsWith("/")){
			rootUrl = StringUtils.removeEnd(rootUrl, "/");
		}
		if(!rootUrl.startsWith("/")){
			rootUrl = "/" + rootUrl;
		}
		config.put("root", rootUrl);
	}
}
