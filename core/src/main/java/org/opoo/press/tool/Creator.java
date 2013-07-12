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
package org.opoo.press.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Renderer;
import org.opoo.press.Site;

/**
 * @author Alex Lin
 *
 */
public class Creator {
	private static final Log log = LogFactory.getLog(Creator.class);

	public static final String DEFAULT_NEW_POST_TEMPLATE = "new_post.ftl";
	public static final String DEFAULT_NEW_PAGE_TEMPLATE = "new_page.ftl";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final SimpleDateFormat NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final String DEFAULT_POST_DIR = "'article/'yyyy/MM/";
	private static final String DEFAULT_PAGE_DIR = "";
    
	public File createNewPage(Site site, String title, String name) throws Exception{
		String newPageDir = (String)site.getConfig().get("new_page_dir");
		if(newPageDir == null){
			newPageDir = DEFAULT_PAGE_DIR;
		}
		return createNewFile(site, title, name, newPageDir, true, false);
	}
	
	public File createNewPost(Site site, String title, String name, boolean draft) throws Exception{
		String newPostDir = (String)site.getConfig().get("new_post_dir");
		if(newPostDir == null){
			newPostDir = DEFAULT_POST_DIR;
		}
		return createNewFile(site, title, name, newPostDir, draft, true);
	}
	
	private File createNewFile(Site site, String title, String name, String dir, boolean draft, boolean isPost) throws Exception{
		//String permalinkStyle = (String)site.getConfig().get("permalink");
		if(title == null){
			throw new IllegalArgumentException("Title is required.");
		}
		title = title.trim();

		if(name == null){
			log.info("Using title as post name.");
			name = title;
		}else{
			name = name.trim();
		}
		
		name = site.toSlug(name);
		
		log.info("title: " + title);
		log.info("name: " + name);
		
		Date date = new Date();
		String filename = name + ".markdown";
		String filepath = dir;
		if(isPost){
			filename = NAME_FORMAT.format(date) + "-" + name + ".markdown";
			filepath = new SimpleDateFormat(dir).format(date);
			//url = "/" + filepath + name + "/";
			//If it's post, don't specify url, use site's 'permalink'
			//url = buildPostUrl(date, name, filepath, permalinkStyle);
		}else{
			//url = "/" + name + "/";
		}
		File file = new File(site.getSource(), filepath + filename);
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("title", title);
		map.put("name", name);
		map.put("date", DATE_FORMAT.format(date) );
		map.put("filename", filename);
		map.put("filepath", filepath);
		map.put("site", site);
		
		String template = getTemplate(site, isPost);
		Renderer renderer = site.getRenderer();
		renderer.render(template, map, writer);
		
		log.info("Write to file " + file);
		return file;
	}
	/*
	private String buildPostUrl(Date date, String name, String filepath, String permalinkStyle) {
		if(StringUtils.isBlank(permalinkStyle)){
			return "/" + filepath + name + "/";
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int year = c.get(Calendar.YEAR);
		int monthnum = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		
		permalinkStyle = StringUtils.replace(permalinkStyle, ":title", name);
		permalinkStyle = StringUtils.replace(permalinkStyle, ":year", year + "");
		permalinkStyle = StringUtils.replace(permalinkStyle, ":month", StringUtils.leftPad(monthnum + "", 2, '0'));
		permalinkStyle = StringUtils.replace(permalinkStyle, ":day", StringUtils.leftPad(day + "", 2, '0'));
		permalinkStyle = StringUtils.replace(permalinkStyle, ":hour", StringUtils.leftPad(hour + "", 2, '0'));
		permalinkStyle = StringUtils.replace(permalinkStyle, ":minute", StringUtils.leftPad(minute + "", 2, '0'));
		permalinkStyle = StringUtils.replace(permalinkStyle, ":second", StringUtils.leftPad(second + "", 2, '0'));
		return permalinkStyle;
	}
	*/

	private String getTemplate(Site site, boolean isPost) {
		if(isPost){
			String template = (String)site.getConfig().get("new_post_template");
			if(StringUtils.isBlank(template)){
				template = DEFAULT_NEW_POST_TEMPLATE;
			}
			return template;
		}else{
			String template = (String)site.getConfig().get("new_page_template");
			if(StringUtils.isBlank(template)){
				template = DEFAULT_NEW_PAGE_TEMPLATE;
			}
			return template;
		}
	}
}
