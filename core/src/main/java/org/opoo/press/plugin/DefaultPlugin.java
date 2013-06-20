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
package org.opoo.press.plugin;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Converter;
import org.opoo.press.Generator;
import org.opoo.press.Plugin;
import org.opoo.press.Registry;
import org.opoo.press.Site;
import org.opoo.press.SiteFilter;
import org.opoo.press.util.Utils;

/**
 * The default site Plugin that instance all Converters and Generators 
 * by the class names specified in the site configuration file.
 * 
 * @author Alex Lin
 *
 */
public class DefaultPlugin implements Plugin {
	private static final Log log = LogFactory.getLog(DefaultPlugin.class);

	/* (non-Javadoc)
	 * @see org.opoo.press.Plugin#initialize(org.opoo.press.Registry)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(Registry registry) {
		Site site = registry.getSite();
		Map<String, Object> config = site.getConfig();
		List<String> converterNames = (List<String>) config.get("converters");
		List<String> generatorNames = (List<String>) config.get("generators");
		List<String> siteFilters = (List<String>) config.get("siteFilters");
		
		if(converterNames != null && !converterNames.isEmpty()){
			for(String converterName: converterNames){
				Converter c = (Converter) Utils.newInstance(converterName, site);
				registry.registerConverter(c);
				log.info("Register converter: " + converterName);
			}
		}
		
		if(generatorNames != null && !generatorNames.isEmpty()){
			for(String generatorName: generatorNames){
				Generator g = (Generator) Utils.newInstance(generatorName, site);
				registry.registerGenerator(g);
				log.info("Register generator: " + generatorName);
			}
		}
		
		if(siteFilters != null && !siteFilters.isEmpty()){
			for(String filterName: siteFilters){
				SiteFilter f = (SiteFilter) Utils.newInstance(filterName, site);
				registry.registerSiteFilter(f);
				log.info("Register site filter: " + filterName);
			}
		}
	}
}
