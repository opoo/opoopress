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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.opoo.press.source.Source;
import org.opoo.press.util.ClassPathUtils;

/**
 * @author Alex Lin
 *
 */
public class Installer {
	
	public void install(File siteDir, Locale locale) throws Exception{
		if(siteDir.exists()){
			throw new Exception("Site already initialized - " + siteDir.getAbsolutePath());
		}
		
		siteDir.mkdirs();
		ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
		ClassPathUtils.copyPath(threadLoader, "site", siteDir);
		
		if(locale == null){
			locale = Locale.getDefault();
		}
		createSamlePost(siteDir, locale);
		updateConfig(siteDir, locale);
	}
	
	private void updateConfig(File siteDir, Locale locale){
		boolean isZH = "zh".equals(locale.getLanguage());
		if(isZH){
			FileUtils.deleteQuietly(new File(siteDir, "config.yml"));
			File file = new File(siteDir, "config_zh_CN.yml");
			file.renameTo(new File(siteDir, "config.yml"));
		}else{
			FileUtils.deleteQuietly(new File(siteDir, "config_zh_CN.yml"));
		}
	}
	
	private void createSamlePost(File siteDir, Locale locale){
		boolean isZH = "zh".equals(locale.getLanguage());
		Date date = new Date();
		String filename = "'source/article/'yyyy/MM/yyyy-MM-dd-'hello-world.markdown'";
		filename = new SimpleDateFormat(filename).format(date);
		File file = new File(siteDir, filename);
		String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
		
		List<String> lines = new ArrayList<String>();
		lines.add(Source.TRIPLE_DASHED_LINE);
		lines.add("layout: post");
		if(isZH){
			lines.add("title: '世界，你好！'");
		}else{
			lines.add("title: 'Hello World!'");
		}
		lines.add("date: '" + dateString + "'");
		lines.add("comments: true");
		lines.add("categories: ['opoopress', 'blog', 'Custom Category']");
		lines.add("tags: ['Tag1', 'Tag2']");
		lines.add(Source.TRIPLE_DASHED_LINE);
		if(isZH){
			lines.add("欢迎使用 OpooPress！");
			lines.add("");
			lines.add("这是系统自动生成的演示文章。编辑或者删除它，然后开始您的博客！");
		}else{
			lines.add("Welcome to OpooPress. This is your first post.");
			lines.add("");
			lines.add("Edit or delete it, then start blogging!");
		}
		
		try {
			FileUtils.writeLines(file, "UTF-8", lines);
		} catch (IOException e) {
			throw new RuntimeException("Write file exception", e);
		}
	}
}
