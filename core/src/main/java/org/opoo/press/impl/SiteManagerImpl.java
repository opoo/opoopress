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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Site;
import org.opoo.press.SiteManager;
import org.opoo.press.importer.Importer;
import org.opoo.press.util.ClassUtils;
import org.opoo.util.ClassPathUtils;

/**
 * @author Alex Lin
 *
 */
public class SiteManagerImpl extends SiteServiceImpl implements SiteManager {
	private static final Log log = LogFactory.getLog(SiteManagerImpl.class);
	
	public static final String DEFAULT_NEW_POST_TEMPLATE = "new_post.ftl";
	public static final String DEFAULT_NEW_PAGE_TEMPLATE = "new_page.ftl";
	public static final String DEFAULT_SAMPLE_POST_TEMPLATE = "sample-post.ftl";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final SimpleDateFormat NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final String DEFAULT_POST_DIR = "'article/'yyyy/MM/";
	private static final String DEFAULT_PAGE_DIR = "";
	
	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#install(java.io.File, java.util.Locale, boolean)
	 */
	@Override
	public Site install(File siteDir, Locale locale, boolean createSamplePost) throws Exception {
		if(siteDir.exists()){
			throw new Exception("Site already initialized - " + siteDir.getAbsolutePath());
		}

		siteDir.mkdirs();
		ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
		ClassPathUtils.copyPath(threadLoader, "site", siteDir);
		
		if(locale == null){
			locale = Locale.getDefault();
		}
		
		updateConfigFile(siteDir, locale);
		
		Site site = createSite(siteDir);
		
		if(createSamplePost){
			log.info("Creating sample post.");
			//create sample post: hello world
			createSamplePost(site, locale);
		}	
		
		return site;
	}

	/**
	 * @param siteDir
	 * @param locale
	 */
	private void updateConfigFile(File siteDir, Locale locale) {
		boolean isZH = "zh".equals(locale.getLanguage());
		if(isZH){
			FileUtils.deleteQuietly(new File(siteDir, "config.yml"));
			File file = new File(siteDir, "config_zh_CN.yml");
			file.renameTo(new File(siteDir, "config.yml"));
		}else{
			FileUtils.deleteQuietly(new File(siteDir, "config_zh_CN.yml"));
		}
	}
	
	protected String buildPostFileName(Site site, String name, Date date){
		return NAME_FORMAT.format(date) + "-" + name + ".markdown";
	}
	
	protected String buildPostFilePath(Site site, String name, Date date){
		String newPostDir = (String)site.getConfig().get("new_post_dir");
		if(newPostDir == null){
			newPostDir = DEFAULT_POST_DIR;
		}
		return new SimpleDateFormat(newPostDir).format(date);
	}
	
	protected File buildPostFile(Site site, String name, Date date){
		String path = buildPostFilePath(site, name, date);
		String filename = buildPostFileName(site, name, date);
		return new File(site.getSource(), path + filename);
	}

	protected void createSamplePost(Site site, Locale locale) throws IOException{
		Map<String,Object> map = new HashMap<String,Object>();
		
		Date date = new Date();
		String dateString = DATE_FORMAT.format(date);
		map.put("date", dateString);
		
		File file = buildPostFile(site, "hello-world", date);
		
		FileOutputStream os = null;
		OutputStreamWriter out = null;
		try {
			File dir = file.getParentFile();
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			os = new FileOutputStream(file);
			out = new OutputStreamWriter(os, "UTF-8");
			site.getRenderer().render(DEFAULT_SAMPLE_POST_TEMPLATE, map, out);
		} finally{
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(os);
		}
		
		log.info("Sample post created: " + file);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#clean(org.opoo.press.Site)
	 */
	@Override
	public void clean(Site site) throws Exception {
		File destination = site.getDestination();
		File working  = site.getWorking();
		
		log.info("Cleaning destination directory " + destination);
		FileUtils.deleteDirectory(destination);
		
		log.info("Cleaning working directory " + working);
		FileUtils.deleteDirectory(working);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#newPage(org.opoo.press.Site, java.lang.String, java.lang.String)
	 */
	@Override
	public File newPage(Site site, String title, String name) throws Exception {
		if(StringUtils.isBlank(title)){
			throw new IllegalArgumentException("Title is required.");
		}
		
		name = processName(site, title, name);
		
		String template = (String)site.getConfig().get("new_page_template");
		if(StringUtils.isBlank(template)){
			template = DEFAULT_NEW_PAGE_TEMPLATE;
		}
		
		String newPageDir = (String)site.getConfig().get("new_page_dir");
		if(newPageDir == null){
			newPageDir = DEFAULT_PAGE_DIR;
		}
		
		Date date = new Date();
		String filepath = newPageDir;
		String filename = name + ".markdown";

		return newFile(site, title, name, date, filepath, filename, template, false);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#newPost(org.opoo.press.Site, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public File newPost(Site site, String title, String name, boolean draft) throws Exception {
		if(StringUtils.isBlank(title)){
			throw new IllegalArgumentException("Title is required.");
		}
		
		name = processName(site, title, name);
		
		String template = (String)site.getConfig().get("new_post_template");
		if(StringUtils.isBlank(template)){
			template = DEFAULT_NEW_POST_TEMPLATE;
		}
		
		Date date = new Date();
		String filepath = buildPostFilePath(site, name, date);
		String filename = buildPostFileName(site, name, date);

		return newFile(site, title, name, date, filepath, filename, template, draft);
	}
	
	private File newFile(Site site, String title, String name, Date date, 
			String filePath, String fileName, String template, 
			boolean draft) throws IOException{
		log.info("title: " + title);
		log.info("name: " + name);
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("title", title);
		map.put("name", name);
		map.put("date", DATE_FORMAT.format(date) );
		map.put("filename", fileName);
		map.put("filepath", filePath);
		map.put("site", site);
		if(draft){
			map.put("published", false);
		}
		
		File file = new File(site.getSource(), filePath + fileName);
		
		FileOutputStream os = null;
		OutputStreamWriter out = null;
		try {
			File dir = file.getParentFile();
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			os = new FileOutputStream(file);
			out = new OutputStreamWriter(os, "UTF-8");
			site.getRenderer().render(template, map, out);
		} finally{
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(os);
		}
		
		log.info("Write to file " + file);
		return file;
	}
	
	protected String processName(Site site, String title, String name) {
		if(name == null){
			log.info("Using title as post name.");
			name = title;
		}else{
			name = name.trim();
		}
		name = site.toSlug(name);
		
		return name;
	}

	public void build(Site site){
		long start = System.currentTimeMillis();
		try{
			site.build();
		}catch(Exception e){
			log.error("Generate site exception", e);
		}finally{
			long time = System.currentTimeMillis() - start;
			log.info("Generate time: " + time + "ms");
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#doImport(org.opoo.press.Site, java.lang.String, java.util.Map)
	 */
	@Override
	public void doImport(Site site, String importer, Map<String, Object> params) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String,String> importers = (Map<String, String>) site.getConfig().get("importers");
		Importer importerInstance = null;
		if(importers != null){
			String className = importers.get(importer);
			if(className != null){
				importerInstance = (Importer) ClassUtils.newInstance(className, site);
			}
		}
		
		if(importerInstance == null){
			throw new Exception("No valid importer: " + importer);
		}
		
		importerInstance.doImport(site, params);
	}
}
