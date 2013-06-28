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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.opoo.press.Site;
import org.opoo.press.util.MapUtils;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * Usage: ${titlecase(post.title)}
 * 
 * @author Alex Lin
 *
 */
public class TitleCaseModel implements TemplateMethodModel {
	private static String[] smallWords = {"a", "an", "am", "and", "as", "at", "but", "by", "en", "for", 
			"if", "in", "of", "on", "or", "the", "to", "v", "v.", "via", "vs", "vs."};
	private static List<String> smallWordList = Arrays.asList(smallWords);
			
	public static String toTitleCase(String string){
		StringTokenizer st = new StringTokenizer(string);
		StringBuffer sb = new StringBuffer();
		while(st.hasMoreTokens()){
			if(sb.length() > 0){
				sb.append(" ");
			}
			String str = st.nextToken();
			String lower = str.toLowerCase();
			if(sb.length() > 0 && smallWordList.contains(lower)){
				sb.append(lower);
			}else{
				sb.append((str.charAt(0) + "").toUpperCase());
				if(str.length() > 1){
					sb.append(str.substring(1));
				}
			}
		}
		return sb.toString();
	}
	
	
	private boolean titlecase;
	public TitleCaseModel(Site site){
		Map<String, Object> config = site.getConfig();
		titlecase = MapUtils.get(config, "titlecase", false);
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
//		return WordUtils.capitalizeFully(str);
		return titlecase ? toTitleCase(str) : str;
	}
}
