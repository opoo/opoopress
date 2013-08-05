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
package org.opoo.press.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.thread.QueuedThreadPool;
import org.opoo.press.Application;
import org.opoo.press.Site;
import org.opoo.press.SiteManager;
import org.opoo.press.impl.StaticFileImpl;
import org.opoo.press.source.NoFrontMatterException;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.opoo.util.MapUtils;

/**
 * @author Alex Lin
 *
 */
public class Preview{
	private static final Log log = LogFactory.getLog(Preview.class);
	
	private final int port;
	private final int interval;
	private final File siteDir;
	private final Map<String,Object> extraOptions;
	private final SiteManager siteManager;
	
	private final boolean showDrafts;
	private final File mainConfig;
	private final File compassConfig;
	
	private File sassDirectory;
	private Site site;
	private JettyServer server;
	private DirectoryMonitor monitor;
	private QueuedThreadPool threadPool;
	
	public Preview(SiteManager siteManager, File siteDir, Map<String, Object> extraOptions, int port, int interval) {
		super();
		this.siteDir = siteDir;
		this.extraOptions = extraOptions;
		this.port = port;
		this.interval = interval;
		this.siteManager = siteManager;
		
		if(extraOptions == null){
			showDrafts = false;
		}else{
			showDrafts = MapUtils.get(extraOptions, "show_drafts", false);
		}
		
		this.mainConfig = new File(siteDir, "config.yml");
		this.compassConfig = new File(siteDir, "config.rb");
		
		this.sassDirectory = getSassDirectory();
	}
	
	public void start() throws Exception{
		if(site == null){
			site = siteManager.getSite(siteDir, extraOptions);
		}
		if(monitor == null){
			monitor = new DirectoryMonitor(siteDir, interval, new L());
			monitor.start();
		}
		if(server == null){
			server = new JettyServer(site, port);
			server.start();
		}
		if(threadPool == null){
			threadPool = new QueuedThreadPool();
			Runtime.getRuntime().addShutdownHook(new Thread(){
				@Override
				public void run() {
					try {
						//threadPool.stop();
						Preview.this.stop();
					} catch (Exception e) {
						log.error("Stop thread pool exception", e);
					}
				}
			});
		}
		threadPool.start();
		threadPool.join();
	}
	
	public void stop() throws Exception{
		if(monitor != null){
			monitor.stop();
		}
		if(server != null){
			server.stop();
		}
		if(threadPool != null){
			threadPool.stop();
		}
	}
	
	/**
	 * @param file
	 */
	private void handleFileChange(File file) {
		if(!file.isFile()){
			log.debug("'" + file + "' changed.");
			return;
		}
		
		if(file.equals(mainConfig)){
			log.info("config.yml changed, recreate site.");
			mainConfigChanged();
			return;
		}
		
		if(file.equals(compassConfig)){
			log.info("SASS/SCSS config file changed, recompile...");
			//read sass directory again
			this.sassDirectory = getSassDirectory();
			compassCompile(siteDir);
			return;
		}
		
		if(isSassFile(file)){
			log.info("SASS/SCSS file '" + file + "' changed, recompile...");
			compassCompile(siteDir);
			return;
		}
		
		if(isTemplateFile(file)){
			log.info("Template file '" + file + "' changed, regenerate site.");
			siteManager.build(site);
			return;
		}
		
		if(isAssetFile(file)){
			log.info("Copy static file: " + file);
			copyStaticFile(site.getAssets(), file);
			return;
		}
		
		if(isSourceFile(file)){
			SourceEntry sourceEntry = loadSourceEntry(site.getSource(), file);
			try {
				Source source = Application.getContext().getSourceParser().parse(sourceEntry);
				if(! showDrafts && isDraft(source.getMeta())){			
					log.info("showDrafts = false: Draft post file '" + file + "' changed, skip regenerate.");
					return;
				}
				
				log.info("Source file '" + file + "' changed, regenerate site.");
				siteManager.build(site);
				return;
			} catch (NoFrontMatterException e) {
				log.debug("Copy static file: " + file);
				// static file
				copyStaticFile(sourceEntry);
				return;
			}
		}
		
		log.warn("Unkown file changed, skip procress: " + file);
	}

	private void mainConfigChanged() {
		try {
			if(server != null){
				server.stop();
				//server.setStopAtShutdown(false);
				server = null;//gc
			}
			site = siteManager.getSite(siteDir, extraOptions);
			siteManager.build(site);
			
			server = new JettyServer(site, port);
			server.start();
		} catch (Exception e) {
			log.error("Handle main config changed error", e);
		}
	}
	
	private void compassCompile(File siteDir){
		new Compass(siteDir).compile();
	}

	private File getSassDirectory(){
		InputStream stream = null;
		Properties props = new Properties();
		
		try {
			stream = new FileInputStream(compassConfig);
			props.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(stream);
		}
		
		String dir = props.getProperty("sass_dir");
		log.info("sass_dir: " + dir);
		
		if(dir == null){
			dir = "sass";
		}else{
			dir = StringUtils.remove(dir, "\"");
			dir = StringUtils.remove(dir, "'");
		}
		
		File sass = new File(siteDir, dir);
		if(!sass.exists() || !sass.isDirectory()){
			throw new IllegalArgumentException("No valid sass directory definded in config.rb");
		}
		return sass;
	}
	static boolean directoryContains(File dir, File file){
		try {
			return FileUtils.directoryContains(dir, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static SourceEntry loadSourceEntry(File dir, File file){
		List<File> files = new ArrayList<File>();
		File parent = file.getParentFile();
		if(parent == null){
			throw new IllegalArgumentException("Directory must contains file");
		}
		while(!parent.equals(dir)){
			files.add(parent);
			parent = parent.getParentFile();
		}
		
		SourceEntry parentEntry = null;
		if(!files.isEmpty()){
			Collections.reverse(files);
			Iterator<File> iterator = files.iterator();
			parentEntry = new SourceEntry(iterator.next());
			while(iterator.hasNext()){
				parentEntry = new SourceEntry(parentEntry, iterator.next());
			}
		}
		
		return new SourceEntry(parentEntry, file);
	}
	
	/**
	 * @param assets
	 * @param file
	 */
	private void copyStaticFile(File assets, File file) {
		SourceEntry sourceEntry = loadSourceEntry(assets, file);
		copyStaticFile(sourceEntry);
	}
	
	private void copyStaticFile(SourceEntry sourceEntry){
		StaticFileImpl staticFile = new StaticFileImpl(site, sourceEntry);
		staticFile.write(site.getDestination());
	}

	private boolean isSourceFile(File file) {
		return directoryContains(site.getSource(), file);
	}

	private boolean isAssetFile(File file) {
		if(site.getAssets() != null && site.getAssets().exists() && site.getAssets().isDirectory()){
			return directoryContains(site.getAssets(), file);
		}
		return false;
	}

	private boolean isTemplateFile(File file) {
		return directoryContains(site.getTemplates(), file);
	}
	
	private boolean isSassFile(File file) {
		return directoryContains(sassDirectory, file);
	}
	
	private boolean isDraft(Map<String, Object> meta){
		if(!"post".equals(meta.get("layout"))){
			return false;
		}
		if(!meta.containsKey("published")){
			return false;
		}
		Boolean b = (Boolean)meta.get("published");
		return !b.booleanValue();
	}
	
	private class L extends FileAlterationListenerAdaptor implements FileAlterationListener {
		public void onFileCreate(File file) {
			onFileChange(file);
		}

		public void onFileDelete(File file) {
			onFileChange(file);
		}

		public void onFileChange(File file) {
			handleFileChange(file);
		}
	}
}
