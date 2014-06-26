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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.opoo.press.Config;
import org.opoo.press.converter.TextilejConverter;
import org.opoo.press.converter.TxtmarkMarkdownConverter;
import org.opoo.press.converter.WikiTextConfluenceConverter;
import org.opoo.press.converter.WikiTextMediaWikiConverter;
import org.opoo.press.converter.WikiTextTWikiConverter;
import org.opoo.press.converter.WikiTextTracWikiConverter;
import org.opoo.press.generator.CategoryGenerator;
import org.opoo.press.generator.PaginationGenerator;
import org.opoo.press.generator.TagGenerator;
import org.opoo.press.highlighter.SyntaxHighlighter;
import org.opoo.press.slug.DefaultSlugHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;


/**
 * @author Alex Lin
 * @since 1.2
 */
public class ConfigImpl extends HashMap<String,Object> implements Config {
	private static final long serialVersionUID = 2347499217663917623L;
	private static final Logger log = LoggerFactory.getLogger(ConfigImpl.class);
	
	public static final String DEFAULT_POSTS_FOLDER = "posts";
	public static final String DEFAULT_PAGES_FOLDER = "pages";
	public static final String DEFAULT_ASSETS_FOLDER = "assets";
	public static final String DEFAULT_NEW_POST_FILE = "posts/${year}-${month}-${day}-${name}.${format}";
	public static final String DEFAULT_NEW_PAGE_FILE = "pages/${name}.${format}";
	public static final String DEFAULT_PERMALINK_STYLE = "/article/${year}/${month}/${name}.html";
	
	private static ObjectMapper objectMapper;
	private Map<String,Object> overrideConfig;
	private File[] configFiles;
	private final File base;
	
	/**
	 * Construct configuration instance.
	 * 
	 * @param base Base directory of this site. 
	 * 		Call {@link File#getCanonicalFile()} first.
	 * @param override Options override.
	 */
	public ConfigImpl(File base, Map<String,Object> override){
		this.base = base;
		
		//default config
		initDefaultConfig();
		
		//override config
		if(override != null){
			putAll(override);
			this.overrideConfig = override;
		}
		
		//config files
		long start = System.currentTimeMillis();
		this.configFiles = resolveConfigFiles(base, override);
		loadConfigFromFiles(configFiles);
		log.debug("Config files loaded in {} ms.", (System.currentTimeMillis() - start));
	}
	
	private void initDefaultConfig(){
		put("opoopress", defaultOpooPressOptions());

		put("root", "");
		put("assets", Arrays.asList(DEFAULT_ASSETS_FOLDER));
		put("sources", Arrays.asList(DEFAULT_PAGES_FOLDER, DEFAULT_POSTS_FOLDER));
		put("destination", "target/public");
		put("working_dir", "target/work");
		put("permalink", DEFAULT_PERMALINK_STYLE);
		put("permalink_label", "&infin;");
		put("new_post", DEFAULT_NEW_POST_FILE);
		put("new_page", DEFAULT_NEW_PAGE_FILE);
	
		put("highlighter", SyntaxHighlighter.class.getName());
		
		put("slugHelper", DefaultSlugHelper.class.getName());
		
		put("relatedPostsFinder", NoOpRelatedPostsFinder.class.getName());
		
		put("converters", Arrays.asList(
				TxtmarkMarkdownConverter.class.getName(),
				TextilejConverter.class.getName(),
				WikiTextMediaWikiConverter.class.getName(),
				WikiTextTracWikiConverter.class.getName(),
				WikiTextTWikiConverter.class.getName(),
				WikiTextConfluenceConverter.class.getName())
		);
		
		put("generators", Arrays.asList(
				PaginationGenerator.class.getName(),
				CategoryGenerator.class.getName(),
				TagGenerator.class.getName())
		);
	}
	
	public static Map<String,Object> defaultOpooPressOptions(){
		Map<String,Object> options = new HashMap<String,Object>();
		Package pkg = ConfigImpl.class.getPackage();
		String version = pkg != null ? pkg.getSpecificationVersion() : null;
		if(StringUtils.isBlank(version)){
			version = "unkown_version";
		}

		options.put("version", version);
		options.put("name", "OpooPress");
		return options;
	}
	
	private void loadConfigFromFiles(File[] configFiles){
		for(File file: configFiles){
			log.info("Loading config from {}", file);
			loadConfigFromFile(file);
		}
	}
	
	/**
	 * @param file
	 */
	@SuppressWarnings("unchecked")
	private void loadConfigFromFile(File file) {
		String name = file.getName();
		
		InputStream inputStream = null;
		Map<String,Object> map = null;
		try {
			inputStream = FileUtils.openInputStream(file);
			if(FilenameUtils.isExtension(name, "json")){
				if(objectMapper == null){
					objectMapper = new ObjectMapper();
				}
				map = objectMapper.readValue(inputStream, Map.class);
			}else{
				//yaml is not thread safe, so create new instance
				map = new Yaml().loadAs(inputStream, Map.class);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(inputStream);
		}
		
		if(map != null){
			log.debug("Config loaded: {}", map);
			putAll(map);
		}
	}

	/**
	 * @return
	 */
	private File[] resolveConfigFiles(File base, Map<String,Object> overrideConfig) {
		//Override
		String configFile = System.getProperty("config");
		if(StringUtils.isBlank(configFile) && overrideConfig != null){
			configFile = (String) overrideConfig.get("config");
		}
		if(StringUtils.isNotBlank(configFile)){
			log.info("Using config files: {}", configFile);

			String[] strings = StringUtils.split(configFile, ',');
			File[] files = new File[strings.length];
			for(int i = 0 ; i < strings.length ; i++){
				files[i] = new File(base, strings[i]);
			}
			return files;
		}

		//default
		final String[] acceptableExts = new String[]{"yml", "yaml", "json"};
		return base.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				return name.startsWith("config") 
						&& FilenameUtils.isExtension(name, acceptableExts)
						&& pathname.canRead();
			}});
	}

	Map<String, Object> getOverrideConfig(){
		return overrideConfig;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Config#getConfigFiles()
	 */
	@Override
	public File[] getConfigFiles() {
		return configFiles;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Config#get(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String name) {
		return (T) super.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Config#get(java.lang.String, java.lang.Object)
	 */
	@Override
	public <T> T get(String name, T defaultValue) {
		if(!containsKey(name)){
			return defaultValue;
		}
		return get(name);
	}
	
	public File getBasedir(){
		return base;
	}
}
