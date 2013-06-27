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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.opoo.press.Site;

/**
 * @author Alex Lin
 *
 */
public abstract class Utils {

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
		return /*rootUrl + */ categoryDir + "/" + encodeURL(categoryName) + "/";
	}
	
	public static String buildTagUrl(Site site, String tagName){
		//String rootUrl = (String) site.getConfig().get("root");
		String tagDir = (String) site.getConfig().get("tag_dir");
		return /*rootUrl + */ tagDir + "/" + encodeURL(tagName) + "/";
	}
	
	public static String encodeURL(String url){
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String decodeURL(String url){
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String toSlug(String string){
		string = string.toLowerCase();
		//return StringUtils.remove(string, " ");
		return StringUtils.replace(string, " ", "-");
	}
}
