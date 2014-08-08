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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.opoo.press.Config;
import org.opoo.press.Converter;
import org.opoo.press.Generator;
import org.opoo.press.Ordered;
import org.opoo.press.Plugin;
import org.opoo.press.PluginManager;
import org.opoo.press.Processor;
import org.opoo.press.Site;
import org.opoo.press.SiteAware;
import org.opoo.press.converter.IdentityConverter;
import org.opoo.press.source.Source;
import org.opoo.press.template.TitleCaseModel;
import org.opoo.press.util.ClassUtils;
import org.opoo.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateModel;

/**
 * @author Alex Lin
 * @since 1.2
 */
public class PluginManagerImpl implements PluginManager {
	private static final Logger log = LoggerFactory.getLogger(PluginManagerImpl.class);
	
	private List<Converter> converters = new ArrayList<Converter>();
	private List<Generator> generators = new ArrayList<Generator>();
	private List<Processor> processors = new ArrayList<Processor>();
	private List<TemplateLoader> templateLoaders = new ArrayList<TemplateLoader>();
	private Map<String, TemplateModel> templateModels = new HashMap<String,TemplateModel>();
	
	public PluginManagerImpl(SiteImpl site){
		Config config = site.getConfig();
		List<String> converterNames = config.get("converters");
		List<String> generatorNames = config.get("generators");
		List<String> processorNames = config.get("processors");
		List<String> pluginNames = config.get("plugins");
		String pluginsDir = config.get("plugins_dir");
		
		Thread currentThread = Thread.currentThread();
		ClassLoader oldLoader = currentThread.getContextClassLoader();
		try{
			File dir = PathUtils.canonical(new File(site.getBasedir(), pluginsDir));
			ClassLoader classLoader = buildClassLoader(dir, getClass());
			if(classLoader != null){
				currentThread.setContextClassLoader(classLoader);
			}
			
			converters.add(new IdentityConverter());
			initialize(converters, Converter.class, converterNames, site);
			initialize(generators, Generator.class, generatorNames, site);
			initialize(processors, Processor.class, processorNames, site);
			
			List<Plugin> plugins = load(Plugin.class, pluginNames, site);
			for(Plugin plugin: plugins){
				log.debug("Initializing plugin: {}", plugin);
				plugin.initialize(this);
			}
		}catch(RuntimeException e){
			throw e;
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			if(oldLoader != null){
				currentThread.setContextClassLoader(oldLoader);
			}
		}
		
		//title case
		TitleCaseModel model = new TitleCaseModel(site);
		templateModels.put("titleCase", model);
		templateModels.put("titlecase", model);
	}
	
	void apply(Object object, Site site){
		if(object instanceof SiteAware){
			((SiteAware) object).setSite(site);
		}
	}

	<T> List<T> load(Class<T> clazz, List<String> classNames, Site site){
		List<T> list = new ArrayList<T>();
		ServiceLoader<T> loader = ServiceLoader.load(clazz);
		for(T t: loader){
			apply(t, site);
			list.add(t);
			log.debug("Service load {}", t.getClass().getName());
		}
		if(classNames != null && !classNames.isEmpty()){
			for(String className: classNames){
				list.add(ClassUtils.newInstance(className, site, clazz));
				log.debug("New instance {}", className);
			}
		}
		return list;
	}

	<T extends Ordered> void initialize(List<T> list, Class<T> clazz, List<String> classNames, Site site){
		list.addAll(load(clazz, classNames, site));
		if(!list.isEmpty()){
			Collections.sort(list, Ordered.COMPARATOR);
		}
	}
	
	ClassLoader buildClassLoader(File pluginDir, Class<?> clazz) throws MalformedURLException{
		System.out.println(pluginDir);
		File[] files = pluginDir.listFiles(new ValidClassPathEntryFileFilter());
		if(files == null || files.length == 0){
			return null;
		}
		
		URL[] urls = new URL[files.length];
		for(int i = 0 ; i < files.length ; i++){
			urls[i] = files[i].toURI().toURL();
		}
		
		ClassLoader parent = clazz.getClassLoader();
		if(parent == null){
			parent = ClassLoader.getSystemClassLoader();
		}
		return new URLClassLoader(urls, parent);
	}
	
	@Override
	public void registerConverter(Converter c) {
		this.converters.add(c);
		if(this.converters.size() > 1){
			Collections.sort(this.converters, Ordered.COMPARATOR);
		}
	}

	@Override
	public void registerGenerator(Generator g) {
		this.generators.add(g);
		if(this.generators.size() > 1){
			Collections.sort(this.generators, Ordered.COMPARATOR);
		}
	}

	@Override
	public void registerTemplateModel(String name, TemplateModel model) {
		templateModels.put(name, model);
	}

	@Override
	public void registerTemplateLoader(TemplateLoader loader) {
		templateLoaders.add(loader);
	}

	public List<Converter> getConverters() {
		return converters;
	}
	
	public Converter getConverter(Source source) throws RuntimeException {
		for(Converter c: converters){
			if(c.matches(source)){
				return c;
			}
		}
		throw new RuntimeException("No matched converter: " + source.getSourceEntry().getFile());
	}

	public List<Generator> getGenerators() {
		return generators;
	}

	public List<TemplateLoader> getTemplateLoaders() {
		return templateLoaders;
	}

	public Map<String, TemplateModel> getTemplateModels() {
		return templateModels;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Registry#registerProcessor(org.opoo.press.Processor)
	 */
	@Override
	public void registerProcessor(Processor processor) {
		this.processors.add(processor);
		if(this.processors.size() > 1){
			Collections.sort(this.processors, Ordered.COMPARATOR);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.PluginManager#getProcessors()
	 */
	@Override
	public List<Processor> getProcessors() {
		return processors;
	}
	
	public static class ValidClassPathEntryFileFilter implements FileFilter{
		/* (non-Javadoc)
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File file) {
			String name = file.getName();
			char firstChar = name.charAt(0);
			if(firstChar == '.' || firstChar == '_' || firstChar == '#'){
				return false;
			}
			char lastChar = name.charAt(name.length() - 1);
			if(lastChar == '~'){
				return false;
			}
			if(file.isHidden()){
				return false;
			}
			if(file.isDirectory()){
				return true;
			}
			if(file.isFile()){
				name = name.toLowerCase();
				if(name.endsWith(".jar") || name.endsWith(".zip")){
					return true;
				}
			}
			return false;
		}
	}
}
