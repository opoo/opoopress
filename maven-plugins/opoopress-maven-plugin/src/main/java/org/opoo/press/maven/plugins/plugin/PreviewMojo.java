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
package org.opoo.press.maven.plugins.plugin;

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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.Application;
import org.opoo.press.impl.StaticFileImpl;
import org.opoo.press.source.NoFrontMatterException;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.opoo.press.tool.PreviewMonitor;
import org.opoo.press.tool.PreviewServer;

/**
 * @author Alex Lin
 * @goal preview
 */
public class PreviewMojo extends AbstractGenerateMojo{
	/**
     * Set this to 'true' to generate draft posts.
     *
     * @parameter expression="${show-drafts}" default-value="false"
     */
    protected boolean showDrafts;
	
	 /**
     * The amount of time in seconds to wait between checks of the site directory.
     *
     * @parameter expression="${interval}" default-value="5"
     */
	private int interval;
	
    /**
     * The port to execute the HTTP server on.
     *
     * @parameter expression="${port}" default-value="8080"
     */
    private int port;
    
    /**
	 * @parameter expression="${op.generate.skip}" default-value="true"
	 */
	protected boolean skipGenerate;
	
	private Compass compass;
	private File config;
	private File sassConfig;
	private File sassDir;
	
	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.press.GenerateMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
		
		createSite(showDrafts);
		if(skipGenerate){
			getLog().info( "op.generate.skip = true: Skipping generating" );
		}else{
			generate();
		}
		
		PreviewServer previewServer = new PreviewServer();
		PreviewMonitor previewMonitor = new PreviewMonitor();
		this.compass = new Compass(siteDir, getLog());
		this.config = new File(siteDir, "config.yml");
		this.sassConfig = new File(siteDir, "config.rb");
		this.sassDir = getSassDir();
		
		try {
			previewMonitor.start(siteDir, interval, new SiteAlterationListenerAdaptor());
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}
		
		try {
			previewServer.start(site, port);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}
	
	private void handleFileChange(File file){
		if(!file.isFile()){
			getLog().debug("'" + file + "' changed.");
			return;
		}
		
		if(file.equals(config)){
			getLog().info("config.yml changed, recreate site.");
			createSite(showDrafts);
			generate();
			return;
		}
		
		if(file.equals(sassConfig)){
			getLog().info("SASS/SCSS config file changed, recompile...");
			//read sass dir again
			this.sassDir = getSassDir();
			compass.compile();
			return;
		}
		
		if(isSassFile(file)){
			getLog().info("SASS/SCSS file '" + file + "' changed, recompile...");
			compass.compile();
			return;
		}
		
		if(isTemplateFile(file)){
			getLog().info("Template file '" + file + "' changed, regenerate site.");
			generate();
			return;
		}
		
		if(isAssetFile(file)){
			getLog().info("Copy static file: " + file);
			copyStaticFile(site.getAssets(), file);
			return;
		}
		
		if(isSourceFile(file)){
			SourceEntry sourceEntry = loadSourceEntry(site.getSource(), file);
			try {
				Source source = Application.getContext().getSourceParser().parse(sourceEntry);
				if(! showDrafts && isDraft(source.getMeta())){			
					getLog().info("showDrafts = false: Draft post file '" + file + "' changed, skip regenerate.");
					return;
				}
				
				getLog().info("Source file '" + file + "' changed, regenerate site.");
				generate();
				return;
			} catch (NoFrontMatterException e) {
				getLog().debug("Copy static file: " + file);
				// static file
				copyStaticFile(sourceEntry);
				return;
			}
		}
		
		getLog().warn("Unkown file changed, skip procress: " + file);
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
		return directoryContains(sassDir, file);
	}
	
	private File getSassDir(){
		InputStream stream = null;
		Properties props = new Properties();
		
		try {
			stream = new FileInputStream(sassConfig);
			props.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(stream);
		}
		
		String dir = props.getProperty("sass_dir");
		getLog().info("sass_dir: " + dir);
		
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
	
	static boolean directoryContains(File dir, File file){
		try {
			return FileUtils.directoryContains(dir, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private class SiteAlterationListenerAdaptor extends FileAlterationListenerAdaptor implements FileAlterationListener {
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
