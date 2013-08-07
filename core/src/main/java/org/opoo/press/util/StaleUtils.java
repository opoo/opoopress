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
package org.opoo.press.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.CompassConfig;
import org.opoo.press.Site;
import org.opoo.press.Site.BuildInfo;
import org.opoo.press.SiteConfig;

/**
 * @author Alex Lin
 *
 */
public class StaleUtils {
	private static final Log log = LogFactory.getLog(StaleUtils.class);
	private static final String DEFAULT_CSS_FILE = "screen.css";

	public static boolean isCompassStale(CompassConfig compassConfig){
		return isCompassStale(compassConfig, DEFAULT_CSS_FILE);
	}
	
	public static boolean isCompassStale(CompassConfig compassConfig, String cssFile){
		File dir = compassConfig.getCssDirectory();
		File css = new File(dir, cssFile);
		
		if(!css.exists()){
			if(log.isInfoEnabled()){
				log.info("Css file not exists, need compile compass: " + css);
			}
			return true;
		}
		
		if(!css.isFile()){
			throw new IllegalArgumentException("Output css file is directory: " + css);
		}
		
		long lastModified = css.lastModified();
		
		File configFile = compassConfig.getConfigFile();
		if(configFile.lastModified() > lastModified){
			if(log.isInfoEnabled()){
				log.info("Compass config is newer than css file, need recompile compass.");
			}
			return true;
		}
		
		File sass = compassConfig.getSassDirectory();
		return isNewer(sass, lastModified, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".scss");
			}
		});
	}
	
	public static boolean isSourceStale(Site site){
		BuildInfo buildInfo = site.getLastBuildInfo();
		if(buildInfo == null){
			log.info("No last generate info, regenerate site.");
			return true;
		}
		
		boolean showDrafts = site.showDrafts();
		if(showDrafts != buildInfo.showDrafts()){
			log.info(String.format("Show drafts changed, last: %s, current: %s. regenerate site.", buildInfo.showDrafts(), showDrafts));
			return true;
		}
		
		long lastBuildTime = buildInfo.getBuildTime();
		if(lastBuildTime <= 0L){
			log.info("No last generate time, regenerate site.");
			return true;
		}
		
		SiteConfig config = site.getConfig();
		File configFile = config.getConfigFile();
		
		//config file
		if(configFile.lastModified() > lastBuildTime){
			if(log.isInfoEnabled()){
				log.info("Config file has been changed after time '" + new Date(lastBuildTime) + "', regenerate site.");
			}
			return true;
		}
		
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				char firstChar = name.charAt(0);
				if(firstChar == '.' || firstChar == '#'){
					return false;
				}
				char lastChar = name.charAt(name.length() - 1);
				if(lastChar == '~'){
					return false;
				}
				return true;
			}
		};
		
		//source file
		File source = site.getSource();
		boolean newer = isNewer(source, lastBuildTime, filter);
		if(newer){
			log.info("Source file has been changed after time '" + new Date(lastBuildTime) + "', regenerate site.");
			return true;
		}
		
		//templates
		File templates = site.getTemplates();
		newer = isNewer(templates, lastBuildTime, filter);
		if(newer){
			log.info("Template file has been changed after time '" + new Date(lastBuildTime) + "', regenerate site.");
			return true;
		}
		
		//assets
		File assets = site.getAssets();
		if(assets != null && assets.exists()){
			newer = isNewer(templates, lastBuildTime, filter);
			if(newer){
				log.info("Asset file has been changed after time '" + new Date(lastBuildTime) + "', regenerate site.");
				return true;
			}
		}
		
		return false;
	}
	
    public static boolean isNewer(File dir, long compareTime, FilenameFilter filter){
    	File[] listFiles = dir.listFiles(filter);
    	for(File file: listFiles){
    		if(file.isHidden()){
    			log.debug("Skip check hidden file: " + file);
    			continue;
    		}
    		if(file.isFile()){
    			if(file.lastModified() > compareTime){
    				if(log.isInfoEnabled()){
    					log.info(String.format("File '%s' is newer than '%s'", file, new Date(compareTime)));
    				}
    				return true;
    			}
    		}else if(file.isDirectory()){
    			if(isNewer(file, compareTime, filter)){
    				return true;
    			}
    		}
    	}
    	return false;
    }
}
