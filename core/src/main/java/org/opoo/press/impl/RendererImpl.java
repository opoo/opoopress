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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.source.SourceEntry;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author Alex Lin
 *
 */
public class RendererImpl implements Renderer {
	private static final Log log = LogFactory.getLog(RendererImpl.class);
	
	private Configuration configuration;
	private Site site;
	private File templateDir;
	private File workingTemplateDir;
	private long start = System.currentTimeMillis();
	
	/**
	 * @param templateLoaders 
	 * @param templateDir
	 * @throws IOException 
	 */
	public RendererImpl(Site site, List<TemplateLoader> templateLoaders) {
		super();
		this.site = site;
		templateDir = site.getTemplates();
		log.info("Template directory is " + templateDir.getAbsolutePath());
		
		//Working directory
		workingTemplateDir = new File( site.getWorking(), "templates");
		if(!workingTemplateDir.exists()){
			workingTemplateDir.mkdirs();
		}
		log.info("Working template directory is " + workingTemplateDir.getAbsolutePath());
		
		//configuration
		configuration = new Configuration();
		configuration.setObjectWrapper(new DefaultObjectWrapper());
		configuration.setTemplateLoader(buildTemplateLoader(templateLoaders));
		
		Locale locale = site.getLocale();
		if(locale != null){
			configuration.setLocale(site.getLocale());
		}
	}
	
	private TemplateLoader buildTemplateLoader(List<TemplateLoader> loaders){
		try {
			FileTemplateLoader loader1 = new FileTemplateLoader(workingTemplateDir);
			FileTemplateLoader loader2 = new FileTemplateLoader(templateDir);
			ClassTemplateLoader loader3 = new ClassTemplateLoader(RendererImpl.class, "/org/opoo/press/templates");
				
			if(loaders == null){
				loaders = new ArrayList<TemplateLoader>();
			}
			if(loaders != null){
				loaders.add(0, loader3);
				loaders.add(0, loader2);
				loaders.add(0, loader1);
			}
			TemplateLoader[] loaders2 = loaders.toArray(new TemplateLoader[loaders.size()]);
			//TemplateLoader loader = new MultiTemplateLoader(new TemplateLoader[]{loader1, loader2});
			TemplateLoader loader = new MultiTemplateLoader(loaders2);
			return loader;
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
		Template template = null;
		try {
			template = configuration.getTemplate(templateName, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		process(template, rootMap, out);
	}
	
	private void process(Template template, Map<String,Object> rootMap, Writer out){
		//if(log.isDebugEnabled()){
			//log.debug("Template " + template);
		//}
				
		try {
			template.process(rootMap, out);
			out.flush();
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String prepareWorkingTemplate(String layout, boolean isValidLayout, 
			String content, boolean isContentRenderRequired, SourceEntry entry) {
		log.debug("Prepare template for " + entry.getFile());

		String name = isContentRenderRequired ? buildTemplateName(layout, entry) : buildPlainTextTemplateName(layout);
		File targetTemplateFile = new File(this.workingTemplateDir, name);
		
		if(targetTemplateFile.exists() && targetTemplateFile.lastModified() >= entry.getLastModified()){
			log.debug("Working template exists and newer than source file: " + targetTemplateFile);
		}else{
			StringBuffer templateContent = buildTemplateContent(layout, isValidLayout, 
					content, isContentRenderRequired);
			try {
				FileUtils.write(targetTemplateFile, templateContent, "UTF-8");
				//targetTemplateFile.setLastModified(sourceEntry.getLastModified());
				if(log.isDebugEnabled()){
					log.debug("Create working template: " + targetTemplateFile);
				}
			} catch (IOException e) {
				throw new RuntimeException("Write working template error: " + targetTemplateFile, e);
			}
		}
		
		return name;
	}
	
	private String buildPlainTextTemplateName(String layout){
		return "_" + layout + ".content.ftl";
	}
	
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
		Template template = null;
		try {
			template = new Template("CT" + (start++), new StringReader(templateContent), configuration, "UTF-8");
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		process(template, rootMap, out);
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
		return (layout != null) && !"nil".equals(layout);
	}
}
