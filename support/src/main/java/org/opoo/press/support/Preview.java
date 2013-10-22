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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mortbay.thread.QueuedThreadPool;
import org.opoo.press.Application;
import org.opoo.press.CompassConfig;
import org.opoo.press.Site;
import org.opoo.press.SiteConfig;
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
	private static final Logger log = LoggerFactory.getLogger(Preview.class);
	
	private final int port;
	private final int interval;
	private final File siteDir;
//	private final Map<String,Object> extraOptions;
	private final SiteManager siteManager;
	private final SiteConfig siteConfig;
	
	private boolean showDrafts;
	private boolean skipSassCompile;
	private boolean skipGenerate;

	private CompassConfig compassConfig;
	private Site site;
	private JettyServer server;
	private DirectoryMonitor monitor;
	private List<DirectoryMonitor> sourceMonitors;
	private QueuedThreadPool threadPool;
	
	public Preview(SiteManager siteManager, File siteDir, Map<String, Object> extraOptions, int port, int interval) {
		super();
		this.siteDir = siteDir;
//		this.extraOptions = extraOptions;
		this.port = port;
		this.interval = interval;
		this.siteManager = siteManager;
		
		initOptions(extraOptions);
		
		this.siteConfig = siteManager.createSiteConfig(siteDir, extraOptions);
		
		if(skipSassCompile){
			this.compassConfig = null;
		}else{
			this.compassConfig = siteManager.createCompassConfig(siteDir);
		}
	}
	
	public Preview(SiteManager siteManager, Site site, int port, int interval) {
		this.siteDir = site.getSite();
		this.port = port;
		this.interval = interval;
		this.siteManager = siteManager;
		this.siteConfig = site.getConfig();
		
		Map<String, Object> extraOptions = this.siteConfig.getExtraConfig();
		initOptions(extraOptions);
		
		if(skipSassCompile){
			this.compassConfig = null;
		}else{
			this.compassConfig = siteManager.createCompassConfig(siteDir);
		}
	}
	
	private void initOptions(Map<String, Object> extraOptions){
		if(extraOptions == null){
			showDrafts = false;
			skipGenerate = false;
			skipSassCompile = false;
		}else{
			showDrafts = MapUtils.get(extraOptions, "show_drafts", false);
			skipGenerate = MapUtils.get(extraOptions, "op.generate.skip", false);
			skipSassCompile = MapUtils.get(extraOptions, "op.sass.compile.skip", false);
		}
	}
	
	public void start() throws Exception{
		if(site == null){
			//site = siteManager.getSite(siteDir, extraOptions);
			site = siteManager.createSite(siteConfig);
		}
		
		if(skipGenerate && skipSassCompile){
			log.warn("'op.generate.skip = true' and 'op.sass.compile.skip = true', no directory monitor will be started.");
		}else{
			if(monitor == null){
				monitor = new DirectoryMonitor(siteDir, interval, new L());
				monitor.start();
			}
			if(sourceMonitors == null && site.getSources() != null){
				sourceMonitors = new ArrayList<DirectoryMonitor>();
				for(File source: site.getSources()){
					DirectoryMonitor dm = new DirectoryMonitor(source, interval, new M());
					sourceMonitors.add(dm);
					dm.start();
				}
			}
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
		if(sourceMonitors != null){
			for(DirectoryMonitor dm: sourceMonitors){
				dm.stop();
			}
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
		if(skipGenerate && skipSassCompile){
			log.warn("'op.generate.skip = true' and 'op.sass.compile.skip = true', skipping handle file changed events.");
			return;
		}
		
		if(!file.isFile()){
			log.debug("'" + file + "' changed.");
			return;
		}
		
		if(!skipGenerate){
			if(file.equals(siteConfig.getConfigFile())){
				log.info("Site config file changed, recreate site.");
				mainConfigChanged();
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
		}
		
		if(!skipSassCompile){
			if(file.equals(compassConfig.getConfigFile())){
				log.info("SASS/SCSS config file changed, recompile...");
				//read sass directory again
				this.compassConfig = siteManager.createCompassConfig(siteDir);
				compassCompile(siteDir);
				return;
			}
			
			if(isSassFile(file)){
				log.info("SASS/SCSS file '" + file + "' changed, recompile...");
				compassCompile(siteDir);
				return;
			}
		}

		log.warn("Unkown file changed or 'op.sass.compile.skip=true' or 'op.generate.skip=true', skipping handle file change: " + file);
	}
	
	private void mainConfigChanged() {
		try {
			if(server != null){
				server.stop();
				//server.setStopAtShutdown(false);
				server = null;//gc
			}
//			site = siteManager.getSite(siteDir, extraOptions);
			siteConfig.reload();
			site = siteManager.createSite(siteConfig);
			siteManager.build(site);
			
			server = new JettyServer(site, port);
			server.start();
		} catch (Exception e) {
			log.error("Handle main config changed error", e);
		}
	}
	
	/**
	 * @since 1.0.2
	 * @param file - other source directory
	 */
	private void otherSourceFileChanged(File file){
		SourceEntry sourceEntry =  new SourceEntry(null, file);
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
	
	private void compassCompile(File siteDir){
		new Compass(siteDir).compile();
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
		return directoryContains(compassConfig.getSassDirectory(), file);
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
	
	/**
	 * @since 1.0.2
	 */
	private class M extends L{
		public void onFileChange(File file) {
			otherSourceFileChanged(file);
		}
	}
}
