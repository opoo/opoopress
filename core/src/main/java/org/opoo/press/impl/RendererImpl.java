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

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Config;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.SourceEntry;
import org.opoo.util.PathUtils;
import org.opoo.util.PathUtils.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alex Lin
 *
 */
@Deprecated
public class RendererImpl implements Renderer {
	private static final Logger log = LoggerFactory.getLogger(RendererImpl.class);
	
	private Configuration configuration;
	private Site site;
	private File templateDir;
	private File workingTemplateDir;
	private long start = System.currentTimeMillis();
	
	public RendererImpl(Site site, List<TemplateLoader> templateLoaders) {
		super();
		this.site = site;
		templateDir = site.getTemplates();
		log.debug("Template directory: " + templateDir.getAbsolutePath());
		
		//Working directory
		workingTemplateDir = new File( site.getWorking(), "templates");
		PathUtils.checkDir(workingTemplateDir, Strategy.CREATE_IF_NOT_EXISTS);
		log.debug("Working template directory: {}", workingTemplateDir.getAbsolutePath());
		
		//configuration
		configuration = new Configuration();
		configuration.setObjectWrapper(new DefaultObjectWrapper());
		configuration.setTemplateLoader(buildTemplateLoader(templateLoaders));
		
		Locale locale = site.getLocale();
		if(locale != null){
			configuration.setLocale(site.getLocale());
		}
		
		//Add import i18n messages template.
		configuration.addAutoImport("i18n", "i18n/messages.ftl");
		
		Config config = site.getConfig();
		List<String> autoIncludeTemplates = config.get("auto_include_templates");
		if(autoIncludeTemplates != null && !autoIncludeTemplates.isEmpty()){
			for(String template: autoIncludeTemplates){
				configuration.addAutoInclude(template);
				log.info("Add auto include: " + template);
			}
		}
		
		Map<String,String> autoImportTemplates = config.get("auto_import_templates");
		if(autoImportTemplates != null && !autoImportTemplates.isEmpty()){
			for(Map.Entry<String, String> en: autoImportTemplates.entrySet()){
				configuration.addAutoImport(en.getKey(), en.getValue());
				log.info("Add auto import: " + en.getKey() + " -> " + en.getValue());
			}
		}
	}
	
	private TemplateLoader buildTemplateLoader(List<TemplateLoader> templateLoaders){
		try {
			FileTemplateLoader loader1 = new FileTemplateLoader(workingTemplateDir);
			FileTemplateLoader loader2 = new FileTemplateLoader(templateDir);
			ClassTemplateLoader loader3 = new ClassTemplateLoader(RendererImpl.class, "/org/opoo/press/templates");

			List<TemplateLoader> loaders = (templateLoaders == null) ? new ArrayList<TemplateLoader>()
					: new ArrayList<TemplateLoader>(templateLoaders);

			loaders.add(0, loader3);
			loaders.add(0, loader2);
			loaders.add(0, loader1);

			TemplateLoader[] loaders2 = loaders.toArray(new TemplateLoader[loaders.size()]);
			//TemplateLoader loader = new MultiTemplateLoader(new TemplateLoader[]{loader1, loader2});
			return new MultiTemplateLoader(loaders2);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public Site getSite() {
		return site;
	}

//	private String buildTemplate(String layout, String content){
//		return buildTemplateContent(layout, content, false).toString();
//	}

	@Override
	public String render(String templateName, Map<String, Object> rootMap) {
		StringWriter out = new StringWriter();
		render(templateName, rootMap, out);
		IOUtils.closeQuietly(out);
		return out.toString();
	}
	
	@Override
	public void render(String templateName, Map<String, Object> rootMap, Writer out){
		log.debug("Rendering template {}", templateName);
		try {
			Template template = configuration.getTemplate(templateName, "UTF-8");
			process(template, rootMap, out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void process(Template template, Map<String,Object> rootMap, Writer out) throws IOException, TemplateException {
		//if(log.isDebugEnabled()){
			//log.debug("Template " + template);
		//}
				
//		try {
			template.process(rootMap, out);
			out.flush();
//		} catch (TemplateException e) {
//			throw new RuntimeException(e);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
	}
	
	public String prepareWorkingTemplate(String layout, boolean isValidLayout, 
			String content, boolean isContentRenderRequired, SourceEntry entry) {
		log.debug("Prepare template for {}", entry.getFile());

		String name = isContentRenderRequired ? buildTemplateName(layout, entry) : getLayoutWorkingTemplate(layout);
		File targetTemplateFile = new File(this.workingTemplateDir, name);
		
		if(targetTemplateFile.exists() && targetTemplateFile.lastModified() >= entry.getLastModified()){
			log.debug("Working template exists and newer than source file: {}", targetTemplateFile);
		}else{
			StringBuffer templateContent = buildTemplateContent(layout, isValidLayout, 
					content, isContentRenderRequired);
			try {
				FileUtils.write(targetTemplateFile, templateContent, "UTF-8");
				//targetTemplateFile.setLastModified(sourceEntry.getLastModified());
				log.debug("Create working template: {}", targetTemplateFile);
			} catch (IOException e) {
				throw new RuntimeException("Write working template error: " + targetTemplateFile, e);
			}
		}
		
		return name;
	}
	
//	private String buildPlainTextTemplateName(String layout){
//		return "_" + layout + ".content.ftl";
//	}
	
	private String buildTemplateName(String layout, SourceEntry entry){
		String name = entry.getPath() + "/" + entry.getName() + "." + layout + ".ftl";
//		return StringUtils.replace(name, "/", "__");
		return name;
	}

	private StringBuffer buildTemplateContent(String layout, boolean isValidLayout, 
			String content, boolean isContentRenderRequired){
		StringBuffer template = new StringBuffer();
		//appendPluginMarcos(template);
		
		if(isValidLayout){
			template.append("<#include \"/_" + layout + ".ftl\">");
			template.append("<@" + layout + "Layout>");
		}
		
		if(isContentRenderRequired){
			template.append(content);
		}else{
			template.append("${content}");
		}
		
		if(isValidLayout){
			template.append("</@" + layout + "Layout>");
		}
		
		return template;
	}

	@Override
	public String renderContent(String templateContent,	Map<String, Object> rootMap) {
		StringWriter out = new StringWriter();
		renderContent(templateContent, rootMap, out);
		IOUtils.closeQuietly(out);
		return out.toString();
	}
	
	@Override
	public void renderContent(String templateContent, Map<String, Object> rootMap, Writer out){
		log.debug("Rendering content...");
		try {
			Template template = new Template("CT" + (start++), new StringReader(templateContent), configuration, "UTF-8");
			process(template, rootMap, out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isRenderRequired(String content) {
		if(StringUtils.contains(content, "<#")){
			return true;
		}
		if(StringUtils.contains(content, "${")){
			return true;
		}
		return false;
	}

	@Override
	public boolean isValidLayout(String layout) {
		if(layout == null){
			return false;
		}
		if("nil".equalsIgnoreCase(layout)){
			return false;
		}
		if("null".equalsIgnoreCase(layout)){
			return false;
		}
		if("none".equalsIgnoreCase(layout)){
			return false;
		}
//		return (layout != null) && !"nil".equals(layout);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Renderer#prepareLayoutWorkingTemplates()
	 */
	@Override
	public void prepareLayoutWorkingTemplates() {
		log.debug("Prepare layout working templates...");
		
		File templates = site.getTemplates();
		File[] layoutFiles = templates.listFiles(new FilenameFilter(){
			public boolean accept(File file, String name) {
				return name.startsWith("_") && name.endsWith(".ftl");
			}
		});
		
		for(File layoutFile: layoutFiles){
			String layout = FilenameUtils.getBaseName(layoutFile.getName()).substring(1);
			String name = getLayoutWorkingTemplate(layout);
			
			File targetTemplateFile = new File(this.workingTemplateDir, name);
			if(targetTemplateFile.exists() && targetTemplateFile.lastModified() >= layoutFile.lastModified()){
				log.debug("Layout template exists and newer than source file: {}", targetTemplateFile);
			}else{
				StringBuffer templateContent = buildTemplateContent(layout, true, null, false);
				try {
					FileUtils.write(targetTemplateFile, templateContent, "UTF-8");
					//targetTemplateFile.setLastModified(sourceEntry.getLastModified());
					log.debug("Create layout template: {}", targetTemplateFile);
				} catch (IOException e) {
					throw new RuntimeException("Write layout template error: " + targetTemplateFile, e);
				}
			}
		}
	}
	
	public String getLayoutWorkingTemplate(String layout){
		return "_" + layout + ".content.ftl";
	}
}
