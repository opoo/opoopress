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

import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * @author Alex Lin
 *
 */
public class TagLinksModel implements TemplateMethodModelEx{
	private Site site;
	public TagLinksModel(Site site){
		this.site = site;
	}

	/* (non-Javadoc)
	 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
	 */
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		if(arguments == null || arguments.isEmpty()){
			return "";
		}
		SimpleSequence ss = (SimpleSequence) arguments.get(0);
		if(ss == null){
			return "";
		}
		@SuppressWarnings("unchecked")
		List<String> tags = ss.toList();
		StringBuffer sb = new StringBuffer();
		for(String tag: tags){
			if(sb.length() > 0){
				sb.append(", ");
			}

			String lo = Utils.toSlug(tag);
			String tagName = site.getTagNames().get(lo);
			String rootUrl = (String) site.getConfig().get("root");
			sb.append("<a class=\"tag\" href=\"").append(rootUrl)
				.append(Utils.buildTagUrl(site, lo))
				.append("\">").append(tagName).append("</a>");
		}
		return sb.toString();
	}
}
