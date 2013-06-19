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
package org.opoo.press.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Plugin;
import org.opoo.press.Site;

/**
 * @author Alex Lin
 *
 */
public abstract class Utils {
	private static final Log log = LogFactory.getLog(Utils.class);
	
	public static List<Plugin> instancePlugins(List<String> classNames){
		List<Plugin> list = new ArrayList<Plugin>();
		for(String className: classNames){
			Plugin plugin = instancePlugin(className);
			if(plugin != null){
				list.add(plugin);
			}
		}
		return list;
	}

	private static Plugin instancePlugin(String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<Plugin> clazz = ClassUtils.getClass(className);
			return (Plugin) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			log.error("Instance plugin error", e);
		} catch (InstantiationException e) {
			log.error("Instance plugin error", e);
		} catch (IllegalAccessException e) {
			log.error("Instance plugin error", e);
		}
		return null;
	}

	public static String buildCanonical(Site site, String url){
		String canonical = "" + site.getConfig().get("url");
		String permalink = "" + site.getConfig().get("permalink");
		String pageUrl = url;
		if(permalink.endsWith(".html")){
			canonical += pageUrl;
		}else{
			canonical += StringUtils.removeEnd(pageUrl, "index.html");
		}
		return canonical;
	}
	
	public static String buildCategoryUrl(Site site, String categoryName){
		//String rootUrl = (String) site.getConfig().get("root");
		String categoryDir = (String) site.getConfig().get("category_dir");
		return /*rootUrl + */ categoryDir + "/" + categoryName + "/";
	}
	
	public static boolean containsHighlighterCodeBlock(String content){
		return StringUtils.contains(content, "<pre class='brush:")
				|| StringUtils.contains(content, "<pre class=\"brush:");
	}
}
