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
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opoo.press.CompassConfig;
import org.opoo.press.Config;
import org.opoo.press.Site;
import org.opoo.press.Site.BuildInfo;

/**
 * @author Alex Lin
 *
 */
public class StaleUtils {
	private static final Logger log = LoggerFactory.getLogger(StaleUtils.class);
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
	
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
		return isNewer(sass, lastModified, new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith(".scss");
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
		
		Config config = site.getConfig();
		File[] configFiles = config.getConfigFiles();
		
		for(File configFile: configFiles){
			//config file
			if(configFile.lastModified() > lastBuildTime){
				if(log.isInfoEnabled()){
					log.info("Config file has been changed after time '" + format(lastBuildTime) + "', regenerate site.");
				}
				return true;
			}
		}
		
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				String name = file.getName();
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
		List<File> sources = site.getSources();
		for(File source: sources){
			boolean newer = isNewer(source, lastBuildTime, filter);
			if(newer){
				log.info("Source file has been changed after time '" + format(lastBuildTime) + "', regenerate site.");
				return true;
			}
		}
		
		//templates
		File templates = site.getTemplates();
		boolean newer = isNewer(templates, lastBuildTime, filter);
		if(newer){
			log.info("Template file has been changed after time '" + format(lastBuildTime) + "', regenerate site.");
			return true;
		}
		
		//assets
		List<File> assets = site.getAssets();
		if(assets != null && !assets.isEmpty()){
			for(File asset: assets){
				newer = isNewer(asset, lastBuildTime, filter);
				if(newer){
					log.info("Asset file has been changed after time '" + format(lastBuildTime) + "', regenerate site.");
					return true;
				}
			}
		}
		
		return false;
	}
	
    public static boolean isNewer(File dir, long compareTime, FileFilter filter){
    	File[] listFiles = dir.listFiles(filter);
    	for(File file: listFiles){
    		if(file.isHidden()){
    			log.debug("Skip check hidden file: " + file);
    			continue;
    		}
    		if(file.isFile()){
    			if(file.lastModified() > compareTime){
    				if(log.isInfoEnabled()){
    					log.info(String.format("File '%s' is newer than '%s'", file, format(compareTime)));
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
    
    public static String format(long millis){
    	return SDF.format(new Date(millis));
    }
}
