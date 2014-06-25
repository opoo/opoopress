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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opoo.press.Config;
import org.opoo.press.Converter;
import org.opoo.press.Generator;
import org.opoo.press.Ordered;
import org.opoo.press.Plugin;
import org.opoo.press.PluginManager;
import org.opoo.press.Processor;
import org.opoo.press.converter.IdentityConverter;
import org.opoo.press.source.Source;
import org.opoo.press.template.TitleCaseModel;
import org.opoo.press.util.ClassUtils;
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
//		this.siteImpl = site;
	
		//defaults
//		registerConverter(new TxtmarkMarkdownConverter(site));
//		registerConverter(new IdentityConverter());
//		registerGenerator(new PaginationGenerator());
//		registerGenerator(new CategoryGenerator());
		
		Config config = site.getConfig();
		List<String> converterNames = config.get("converters");
		List<String> generatorNames = config.get("generators");
		List<String> processorNames = config.get("processors");
		List<String> pluginNames = config.get("plugins");

		converters.add(new IdentityConverter());
		if(converterNames != null && !converterNames.isEmpty()){
			for(String converterName: converterNames){
				converters.add(ClassUtils.newInstance(converterName, site, Converter.class));
				log.debug("Register converter: {}", converterName);
			}
			Collections.sort(converters, Ordered.COMPARATOR);
		}
		
		if(generatorNames != null && !generatorNames.isEmpty()){
			for(String generatorName: generatorNames){
				generators.add(ClassUtils.newInstance(generatorName, site, Generator.class));
				log.debug("Register generator: {}", generatorName);
			}
			Collections.sort(generators, Ordered.COMPARATOR);
		}
		
		if(processorNames != null && !processorNames.isEmpty()){
			for(String processorName: processorNames){
				processors.add(ClassUtils.newInstance(processorName, site, Processor.class));
				log.debug("Register processor: {}", processorName);
			}
			Collections.sort(processors, Ordered.COMPARATOR);
		}
		
		if(pluginNames != null && !pluginNames.isEmpty()){
			for(String pluginName: pluginNames){
				Plugin plugin = ClassUtils.newInstance(pluginName, site, Plugin.class);
				log.debug("Initializing plugin: {}", pluginName);
				plugin.initialize(this);
			}
		}
		
		//title case
		TitleCaseModel model = new TitleCaseModel(site);
		templateModels.put("titleCase", model);
		templateModels.put("titlecase", model);
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
}
