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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.internal.BiVariableMap;
import org.opoo.press.Theme;
import org.opoo.press.ThemeBuilder;
import org.opoo.util.PathUtils;
import org.opoo.util.PathUtils.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Lin
 *
 */
public class CompassStyleThemeBuilder implements ThemeBuilder {
	private static final Logger log = LoggerFactory.getLogger(CompassStyleThemeBuilder.class);
	
	public static final String CONFIG_FILE_NAME = "config.rb";
	public static final String CACHE_FILE_NAME = "config.rb.cache";

	/* (non-Javadoc)
	 * @see org.opoo.press.ThemeBuilder#build(org.opoo.press.Theme)
	 */
	@Override
	public void build(Theme theme) {
		build(theme.getPath());
	}
	
	void build(File path){
		File configFile = new File(path, CONFIG_FILE_NAME);
		File cacheFile = new File(path, CACHE_FILE_NAME);
		if(isBuildRequired(configFile, cacheFile)){
			new Compass(path, configFile).compile();
		}else{
			log.debug("Nothing to build - all css files are up to date");
		}
	}

	boolean isBuildRequired(File configFile, File cacheFile){
		Map<String,String> cache;
		if(!cacheFile.exists() || FileUtils.isFileOlder(cacheFile, configFile)){
			log.debug("Cache file not exists or older than config file, crete cache now: {}", cacheFile);
			cache = createCache(configFile, cacheFile);
		}else{
			cache = loadCache(cacheFile);
		}
		
		File dir = configFile.getParentFile();
		String sassDir = cache.get("sass_dir");
		String cssDir = cache.get("css_dir");
		File sassPath = PathUtils.dir(dir, sassDir != null ? sassDir : "sass", Strategy.THROW_EXCEPTION_IF_NOT_EXISTS);
		File cssPath = PathUtils.dir(dir, cssDir != null ? cssDir : "assets/stylesheets", Strategy.CREATE_IF_NOT_EXISTS);
		
		//checking sass source
		Collection<File> sassFiles = FileUtils.listFiles(sassPath, new String[]{"scss"}, true);

		int sassPathLength = sassPath.getAbsolutePath().length();
		long sourceModified = configFile.lastModified();
		long cssModified = 0L;
		for(File f: sassFiles){
			//寻找最后更新时间
			long lastModified = f.lastModified();
			if(lastModified > sourceModified){
				sourceModified = lastModified;
			}
			
			//寻找非片段sass文件
			if(!f.getName().startsWith("_")){
				String relativePath = f.getAbsolutePath().substring(sassPathLength);
				File cssFile = new File(cssPath, relativePath.replace(".scss", ".css"));
				log.debug("Checking {} => {}", f, cssFile);
				
				if(!cssFile.exists()){
					log.debug("CSS file not exists, require build: {}", cssFile);
					return true;
				}else{
					long modified = cssFile.lastModified();
					if(cssModified == 0L || modified < cssModified){
						cssModified = modified;
					}
					log.debug("css update time: {}", new Date(modified));
				}
			}
		}
		log.debug("sass of config file update time: {}", new Date(sourceModified));
		return cssModified < sourceModified;
	}

	/**
	 * @param configFile
	 * @param cacheFile
	 * @return
	 */
	private Map<String, String> createCache(File configFile, File cacheFile) {
		ScriptingContainer container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
		container.runScriptlet(PathType.ABSOLUTE, configFile.getAbsolutePath());
		
		@SuppressWarnings("unchecked") 
		BiVariableMap<String, Object> varMap = container.getVarMap();
		@SuppressWarnings("unchecked") 
		Set<Map.Entry<String,Object>> entrySet = varMap.entrySet();

		//to map
		HashMap<String,String> map = new HashMap<String,String>();
		for(Map.Entry<String, Object> en: entrySet){
			if(en.getValue() instanceof String){
				map.put(en.getKey(), (String) en.getValue());
			}
		}
		
		//save
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(cacheFile);
			SerializationUtils.serialize(map, outputStream);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(outputStream);
		}
		
		return map;
	}

	/**
	 * @param cacheFile
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> loadCache(File cacheFile) {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(cacheFile);
			return (Map<String, String>) SerializationUtils.deserialize(inputStream);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(inputStream);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.ThemeBuilder#watch(org.opoo.press.Theme)
	 */
	@Override
	public void watch(Theme theme) {
		File path = theme.getPath();
		File configFile = new File(path, CONFIG_FILE_NAME);
		new Compass(path, configFile).watch();
	}
}
