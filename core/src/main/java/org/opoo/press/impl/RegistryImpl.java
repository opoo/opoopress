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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opoo.press.Converter;
import org.opoo.press.Generator;
import org.opoo.press.Ordered;
import org.opoo.press.Registry;
import org.opoo.press.Site;
import org.opoo.press.SiteFilter;
import org.opoo.press.filter.MultiSiteFilter;
import org.opoo.press.source.Source;

import freemarker.cache.TemplateLoader;
import freemarker.template.TemplateModel;

/**
 * 
 * @author Alex Lin
 *
 */
public class RegistryImpl implements Registry {
	
	private List<Converter> converters = new ArrayList<Converter>();
	private List<Generator> generators = new ArrayList<Generator>();
	private List<TemplateLoader> templateLoaders = new ArrayList<TemplateLoader>();
	private List<SiteFilter> siteFilters = new ArrayList<SiteFilter>();
	private Map<String, TemplateModel> templateModels = new HashMap<String,TemplateModel>();
	
	private SiteImpl siteImpl;
	public RegistryImpl(SiteImpl site){
		this.siteImpl = site;
	
		//defaults
//		registerConverter(new TxtmarkMarkdownConverter(site));
//		registerConverter(new IdentityConverter());
//		registerGenerator(new PaginationGenerator());
//		registerGenerator(new CategoryGenerator());
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
	public void registerSiteFilter(SiteFilter filter) {
		this.siteFilters.add(filter);
		if(this.siteFilters.size() > 1){
			Collections.sort(this.siteFilters, Ordered.COMPARATOR);
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

	public List<SiteFilter> getSiteFilters() {
		return siteFilters;
	}
	
	public MultiSiteFilter getSiteFilter(){
		return new MultiSiteFilter(siteFilters);
	}

	public Map<String, TemplateModel> getTemplateModels() {
		return templateModels;
	}

	@Override
	public Site getSite() {
		return siteImpl;
	}
}
