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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opoo.press.CompassConfig;

/**
 * @author Alex Lin
 * 
 */
public class CompassConfigImpl implements CompassConfig{
	private static final Logger log = LoggerFactory.getLogger(CompassConfigImpl.class);
	private final File compassProjectPath;
	private File configFile;
	private File sassDir;
	private File cssDir;
	
	public CompassConfigImpl(File compassProjectPath) {
		this.compassProjectPath = compassProjectPath;
		
		configFile = new File(compassProjectPath, "config.rb");
		if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
			throw new IllegalArgumentException("Not a valid compass project config: " + compassProjectPath);
		}
		
		InputStream stream = null;
		Properties configContent = new Properties();
		try {
			stream = new FileInputStream(configFile);
			configContent.load(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}finally{
			IOUtils.closeQuietly(stream);
		}
		
		sassDir = getSassDirectory(configContent);
		cssDir = getCssDirectory(configContent);
	}
	
	private String get(Properties configContent, String name){
		String value = configContent.getProperty(name);
		if(value != null){
			value = StringUtils.remove(value, "\"");
			value = StringUtils.remove(value, "'");
		}

		log.debug(name + ": " + value);
		return value;
	}
	
	private File getSassDirectory(Properties configContent){
		String dir = get(configContent, "sass_dir");
		if(dir == null){
			dir = "sass";
		}
		
		File sass = new File(compassProjectPath, dir);
		if(!sass.exists() || !sass.isDirectory()){
			throw new IllegalArgumentException("No valid sass directory definded in config.rb");
		}
		return sass;
	}
	
	private File getCssDirectory(Properties configContent){
		String dir = get(configContent, "css_dir");
		if(dir == null){
			dir = "assets/stylesheets";
		}
		File css = new File(compassProjectPath, dir);
		
		/*if(!css.exists() || !css.isDirectory()){
			throw new IllegalArgumentException("No valid css directory definded in config.rb");
		}*/
		return css;
	}
	
	public File getConfigFile(){
		return configFile;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.support.CompassConfig#getSassDirectory()
	 */
	@Override
	public File getSassDirectory() {
		return sassDir;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.support.CompassConfig#getCssDirectory()
	 */
	@Override
	public File getCssDirectory() {
		return cssDir;
	}
}
