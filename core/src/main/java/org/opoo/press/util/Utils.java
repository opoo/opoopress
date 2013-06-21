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

import org.apache.commons.lang.StringUtils;
import org.opoo.press.Initializable;
import org.opoo.press.Site;

/**
 * @author Alex Lin
 *
 */
public abstract class Utils {
	//private static final Log log = LogFactory.getLog(Utils.class);
	
	/**
	 * Create a new instance for the specified class name and call 
	 * {@link Initializable#initialize(Site)} if required.
	 * @param className class name
	 * @param site site object
	 * @return new instance
	 */
	public static Object newInstance(String className, Site site){
		Object instance = newInstance(className);
		if(instance instanceof Initializable){
			((Initializable) instance).initialize(site);
		}
		return instance;
	}
	
	/**
	 * Create a new instance for the specified class name.
	 * @param className class name
	 * @return new instance
	 */
	public static Object newInstance(String className){
		try {
			Class<?> class1 = Class.forName(className);
			return class1.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		}
	}

	public static String buildCanonical(Site site, String url){
		String canonical = "" + site.getConfig().get("url");
		String permalink = (String) site.getConfig().get("permalink");
		String pageUrl = url;
		if(permalink != null && permalink.endsWith(".html")){
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
	
	public static String buildTagUrl(Site site, String tagName){
		//String rootUrl = (String) site.getConfig().get("root");
		String tagDir = (String) site.getConfig().get("tag_dir");
		return /*rootUrl + */ tagDir + "/" + tagName + "/";
	}
	
	public static String toSlug(String string){
		string = string.toLowerCase();
		//return StringUtils.remove(string, " ");
		return StringUtils.replace(string, " ", "-");
	}
}
