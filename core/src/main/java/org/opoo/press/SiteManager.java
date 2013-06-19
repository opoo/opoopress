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
package org.opoo.press;

import java.util.Map;


/**
 * @author Alex Lin
 *
 */
public interface SiteManager {
	
//	Map<String,Object> getConfig();
	
	Site getSite(Map<String, Object> config);

//	List<SiteFilter> getSiteFilters();
//	
//	List<Generator> getGenerators();
//	
//	Converter getConverter(Source source) throws RuntimeException;
//
//	Renderer getRenderer(Site site);
//	
//	Map<String, TemplateModel> getTemplateModels();
//	 
//	<F extends RenderFilter> List<F> getRenderFilter(Class<F> clazz);
//	
//	List<TemplateLoader> getTemplateLoaders();
}
