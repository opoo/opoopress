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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.opoo.press.Post;
import org.opoo.press.SiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Alex Lin
 * @since 1.2
 */
public class SiteConfigImpl extends HashMap<String,Object> implements SiteConfig {
	private static final long serialVersionUID = 2347499217663917623L;
	private static final Logger log = LoggerFactory.getLogger(SiteConfigImpl.class);

	public static final String DEFAULT_NEW_POST_TEMPLATE = "new_post.ftl";
	public static final String DEFAULT_NEW_PAGE_TEMPLATE = "new_page.ftl";
	
	public static final String DEFAULT_POSTS_FOLDER = "posts";
	public static final String DEFAULT_PAGES_FOLDER = "pages";
	public static final String DEFAULT_ASSETS_FOLDER = "assets";
	public static final String DEFAULT_NEW_POST_FILE = "posts/${year}-${month}-${day}-${name}.${format}";
	public static final String DEFAULT_NEW_PAGE_FILE = "pages/${name}.${format}";
	public static final String DEFAULT_POST_PERMALINK_STYLE = "/article/${year}/${month}/${name}.html";
	
	public static final FileFilter DEFAULT_CONFIG_FILES_FILTER = new DefaultConfigFilesFilter();
	
	private static ObjectMapper objectMapper;
	private Map<String,Object> overrideConfig;
	private File[] configFiles;
	private final File base;
	private boolean useDefaultConfigFiles = false;
	
	/**
	 * Construct configuration instance.
	 * 
	 * @param base Base directory of this site. 
	 * 		Call {@link File#getCanonicalFile()} before construct this..
	 * @param override Options override.
	 */
	public SiteConfigImpl(File base, Map<String, Object> override){
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
		Arrays.sort(configFiles, new ConfigFileComparator());
		loadConfigFromFiles(configFiles);
		log.debug("Config files loaded in {} ms.", (System.currentTimeMillis() - start));
	}
	
	private void initDefaultConfig(){
		put("opoopress", defaultOpooPressOptions());

		put("root", "");
		put("asset_dirs", Arrays.asList(DEFAULT_ASSETS_FOLDER));
		put("source_dirs", Arrays.asList(DEFAULT_PAGES_FOLDER, DEFAULT_POSTS_FOLDER));
		put("plugin_dir", "plugins");
		put("dest_dir", "target/public");
		put("work_dir", "target/work");

		put("permalink_post", DEFAULT_POST_PERMALINK_STYLE);
		put("permalink_label", "&infin;");

//		put("new_post", DEFAULT_NEW_POST_FILE);
//		put("new_page", DEFAULT_NEW_PAGE_FILE);

		put("category_dir", "/category");
		put("tag_dir", "/tag");

		put("excerpt_separator", Post.DEFAULT_EXCERPT_SEPARATOR);

		put("paginate", 10);
		put("recent_posts", 5);
		put("related_posts", 5);
	}
	
	public static Map<String,Object> defaultOpooPressOptions(){
		Map<String,Object> options = new HashMap<String,Object>();
		Package pkg = SiteConfigImpl.class.getPackage();
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
	 * Find all configuration files.
	 * @param base site base site
	 * @param override  command options, system properties, etc.
	 * @return
	 */
	private File[] resolveConfigFiles(File base, Map<String,Object> override) {
		//system properties
		//-Dconfig=config.json -> override
		//Override
		if(override != null){
			String configFilesString = (String) override.remove("config");
			if(StringUtils.isNotBlank(configFilesString)){
				log.info("Using config files: {}", configFilesString);
				
				String[] strings = StringUtils.split(configFilesString, ',');
				File[] files = new File[strings.length];
				for(int i = 0 ; i < strings.length ; i++){
					files[i] = new File(base, strings[i]);
				}
				return files;
			}
		}
		
		//default
		useDefaultConfigFiles = true;
		return base.listFiles(DEFAULT_CONFIG_FILES_FILTER);
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


	/* (non-Javadoc)
	 * @see org.opoo.press.Config#useDefaultConfigFiles()
	 */
	@Override
	public boolean useDefaultConfigFiles() {
		return useDefaultConfigFiles;
	}


	private static class DefaultConfigFilesFilter implements FileFilter{
		static final String[] ACCEPTABLE_EXTENSIONS = new String[]{"yml", "yaml", "json"};
		/* (non-Javadoc)
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File file) {
			String name = file.getName();
			return FilenameUtils.getBaseName(name).startsWith("config")
					&& FilenameUtils.isExtension(name, ACCEPTABLE_EXTENSIONS)
					&& file.canRead();
		}
	}

	public static class ConfigFileComparator implements Comparator<File>{
		@Override
		public int compare(File o1, File o2) {
			String name1 = o1.getName();
			String name2 = o2.getName();
			if(name1.equals(name2)){
				return 0;
			}

			int delta = getIndex(name1) - getIndex(name2);
			if(delta != 0){
				return delta;
			}

			name1 = FilenameUtils.removeExtension(name1);
			name2 = FilenameUtils.removeExtension(name2);
			return name1.compareTo(name2);
		}

		private int getIndex(String name){
			if(name.endsWith(".yml")){
				return 1;
			}else if(name.endsWith(".yaml")){
				return 2;
			}else if(name.endsWith(".json")){
				return 3;
			}else{
				return Integer.MAX_VALUE;
			}
		}
	}
}
