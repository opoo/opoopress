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
package org.opoo.press.template;

import java.util.List;

import org.opoo.press.Site;
import org.opoo.press.util.Utils;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * Usage: ${buildTagUrl(categoryName)}
 *  
 * @author Alex Lin
 *
 */
public class BuildTagUrlModel implements TemplateMethodModel {
	
	private Site site;
	public BuildTagUrlModel(Site site){
		this.site = site;
	}
	
	/* (non-Javadoc)
	 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
	 */
	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		if(arguments == null || arguments.isEmpty()){
			return "";
		}
		String str = (String)arguments.get(0);
		return Utils.buildTagUrl(site, str);
	}
}
