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
package org.opoo.press.theme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.internal.BiVariableMap;
import org.opoo.press.Site;
import org.opoo.press.Theme;
import org.opoo.press.ThemeBuilder;
import org.opoo.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Lin
 *
 */
public class CompassStyleThemeBuilder implements ThemeBuilder {
	private static final Logger log = LoggerFactory.getLogger(CompassStyleThemeBuilder.class);
//	public static final String[] KNOWN_CONFIG_LOCATIONS = new String[]{"config/compass.rb", ".compass/config.rb", "config/compass.config", "config.rb"};
	public static final String CONFIG_FILE_NAME = "config.rb";
	public static final String CACHE_FILE_NAME = "config.rb.cache";

	/* (non-Javadoc)
	 * @see org.opoo.press.ThemeBuilder#build(org.opoo.press.Theme)
	 */
	@Override
	public void build(Site site, Theme theme) {
		compile(theme.getPath());
	}
	
	void compile(File path) {
		File configFile = new File(path, CONFIG_FILE_NAME);
		if(!configFile.exists()){
			log.debug("Not a compass project, skip compile.");
			return;
		}
		
		File cacheFile = new File(path, CACHE_FILE_NAME);
		
		Properties cache;
		if(!cacheFile.exists() || FileUtils.isFileOlder(cacheFile, configFile)){
			log.debug("Cache file not exists or older than config file, crete cache now: {}", cacheFile);
			cache = createCache(configFile, cacheFile);
		}else{
			cache = loadCache(cacheFile);
		}
		
		String sassDir = cache.getProperty("sass_dir", "sass");
		String cssDir = cache.getProperty("css_dir", "assets/stylesheets");
		
		File sassPath = new File(path, sassDir);
		File cssPath = new File(path, cssDir);
		
		List<File> sassFiles = PathUtils.listFiles(sassPath, new SassFilenameFilter(), true);
		List<File> cssFiles = toCssFiles(sassFiles, sassPath, cssPath);
		
		if(shoudCompile(configFile, sassFiles, cssFiles)){
			new Compass(path, configFile).compile();
		}else{
			log.debug("Nothing to compile - all css files are up to date");
		}
	}
	
	private boolean shoudCompile(File configFile, List<File> sassFiles, List<File> cssFiles) {
		int size = cssFiles.size();
		for(int i = 0 ; i < size ; i++){
			File cssFile = cssFiles.get(i);
			if(!cssFile.exists()){
				log.debug("css file '{}' not eixsts, need compile.", cssFile);
				return true;
			}
			if(FileUtils.isFileOlder(cssFile, configFile)){
				log.debug("css file '{}' is older than compass config file, need compile.", cssFile);
				return true;
			}
			File sassFile = sassFiles.get(i);
			if(FileUtils.isFileOlder(cssFile, sassFile)){
				log.debug("css file '{}' is older than sass file '{}', need compile.", cssFile, sassFile);
				return true;
			}
		}
		return false;
	}

	private List<File> toCssFiles(Collection<File> sassFiles,	File sassPath, File cssPath) {
		int prefixLength = sassPath.getAbsolutePath().length();
		List<File> cssFiles = new ArrayList<File>();
		for(File f:sassFiles){
			String path = f.getAbsolutePath();
			String pathInfo = path.substring(prefixLength, path.length() - 4) + "css";
			File cssFile = new File(cssPath, pathInfo);
			cssFiles.add(cssFile);
		}
		return cssFiles;
	}

	private Properties createCache(File configFile, File cacheFile) {
		ScriptingContainer container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
		container.runScriptlet(PathType.ABSOLUTE, configFile.getAbsolutePath());
		
		@SuppressWarnings("unchecked") 
		BiVariableMap<String, Object> varMap = container.getVarMap();
		@SuppressWarnings("unchecked") 
		Set<Map.Entry<String,Object>> entrySet = varMap.entrySet();

		Properties props = new Properties();
		for(Map.Entry<String, Object> en: entrySet){
			if(en.getValue() instanceof String){
				props.setProperty(en.getKey(), (String)en.getValue());
			}
		}
		
		//save
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(cacheFile);
			props.store(outputStream, "compass configuration cache");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(outputStream);
		}
		
		return props;
	}

	private Properties loadCache(File cacheFile) {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(cacheFile);
			Properties props = new Properties();
			props.load(inputStream);
			return props;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(inputStream);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.ThemeBuilder#watch(org.opoo.press.Theme)
	 */
	@Override
	public void watch(Site site, Theme theme) {
		File path = theme.getPath();
		watch(path);
	}
	
	void watch(File path){
		File configFile = new File(path, CONFIG_FILE_NAME);
		if(!configFile.exists()){
			log.debug("Not a compass project, skip watch.");
			return;
		}
		new Compass(path, configFile).watch();
	}
	
	public static class SassFilenameFilter implements FilenameFilter{
		/* (non-Javadoc)
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		@Override
		public boolean accept(File dir, String name) {
			char firstChar = name.charAt(0);
			if(firstChar == '.' || firstChar == '_' || firstChar == '#'){
				return false;
			}
			
			if(name.endsWith(".scss") || name.endsWith(".sass")){
				return true;
			}
			return false;
		}
	}
}
