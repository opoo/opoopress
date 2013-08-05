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
package org.opoo.press.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.opoo.press.Category;
import org.opoo.press.Site;
import org.opoo.press.Tag;
import org.opoo.press.source.Source;

/**
 * Import posts and pages from WordPress exported XML file.
 * 
 * @author Alex Lin
 *
 */
public class WordPressImporter implements Importer {
	private static final Log log = LogFactory.getLog(WordPressImporter.class);
	/**
	 * xmlns:excerpt="http://wordpress.org/export/1.2/excerpt/"
	 */
	public static Namespace NS_EXCERPT = new Namespace("excerpt", "http://wordpress.org/export/1.2/excerpt/");
	/**
	 * xmlns:content="http://purl.org/rss/1.0/modules/content/"
	 */
	public static Namespace NS_CONTENT = new Namespace("content", "http://purl.org/rss/1.0/modules/content/");
	/**
	 * xmlns:wfw="http://wellformedweb.org/CommentAPI/"
	 */
	public static Namespace NS_WFW = new Namespace("wfw", "http://wellformedweb.org/CommentAPI/");
	/**
	 * xmlns:dc="http://purl.org/dc/elements/1.1/"
	 */
	public static Namespace NS_DC = new Namespace("dc", "http://purl.org/dc/elements/1.1/");
	/**
	 * xmlns:wp="http://wordpress.org/export/1.2/"
	 */
	public static Namespace NS_WP = new Namespace("wp", "http://wordpress.org/export/1.2/");
	/**
	 * DateFormat for parse the post date in WordPress XML exported file.
	 */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * for the file name.
	 */
	private static final SimpleDateFormat NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	public WordPressImporter(){
	}
	
	
	@Override
	public void doImport(Site site, Map<String,Object> props) throws ImportException{
		String fileStr = (String) props.get("file");
		File file = new File(fileStr);
		
		if(!file.exists()){
			throw new ImportException("File not found: " + file);
		}
		
		try {
			importFromtFile(site, file, props);
		} catch (DocumentException e) {
			throw new ImportException(e);
		} catch (FileNotFoundException e) {
			throw new ImportException(e);
		} catch (ParseException e) {
			throw new ImportException(e);
		} catch (IOException e) {
			throw new ImportException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void importFromtFile(Site site, File file, Map<String,Object> props) throws DocumentException, ParseException, IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
		Reader reader = new BufferedReader(inputStreamReader);
		try{
			SAXReader xmlReader = new SAXReader();
			Document doc = xmlReader.read(reader);
			
			Element root = doc.getRootElement();
			Element channel = root.element("channel");
		
			List<Element> list = channel.elements("item");
			for(Element e: list){
				String postType = e.elementText("post_type");
				if("post".equals(postType) || "page".equals(postType)){
					importPostOrPage(site, postType, e, props);
				}
			}
		}finally{
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(inputStreamReader);
			IOUtils.closeQuietly(fileInputStream);
		}
	}

	@SuppressWarnings("unchecked")
	private void importPostOrPage(Site site, String postType, Element e, Map<String,Object> props) throws ParseException, IOException {
		boolean includeDrafts = "true".equals(props.get("include_drafts"));

		String status = e.elementTextTrim("status");
		boolean published = "publish".equals(status);
		String title = e.elementTextTrim("title");
		String name = e.elementTextTrim("post_name");
		boolean comments = "open".equals(e.elementTextTrim("comment_status"));
		String date = e.elementTextTrim("post_date");
		String author = e.elementTextTrim("creator");
		String postid = e.elementTextTrim("post_id");
		String link = e.elementTextTrim("link");
		String excerpt = e.elementTextTrim(new QName("encoded", NS_EXCERPT));
		String content = e.elementText(new QName("encoded", NS_CONTENT));
		
		boolean isPage = "page".equals(postType);
		Date parse = DATE_FORMAT.parse(date);
		
		String postname = name;
		if(postname.startsWith("%")){
			postname = title;
		}
		String url = buildURL(parse, postname, postid, author, props);
		
		if(!includeDrafts && !published){
			log.info(name + " is draft, skiping import. Set 'include_drafts' peroperty to enabled import drafts.");
			return;
		}
		
		//excerpt
		boolean excerpted = StringUtils.isNotBlank(excerpt);
		StringBuilder excerptBuilder = excerpted ? null : new StringBuilder();
		if(isPage){
			excerptBuilder = null;
		}
		
		//replace the content
		Map<String,String> rp = (Map<String, String>) props.get("content_replacements");
		if(rp != null){
			for(Map.Entry<String, String> en: rp.entrySet()){
				content = StringUtils.replace(content, en.getKey(), en.getValue());
			}
		}
		
		List<String> contentLines = processContent(content, excerptBuilder);
		
		if(!isPage && !excerpted){
			excerpt = excerptBuilder.toString();
		}
		
		//Categories and tags
		List<String> cats = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		List<Element> list = e.elements("category");
		for(Element n: list){
			String domain = n.attributeValue("domain");
			String nicename = n.attributeValue("nicename");
			String text = n.getTextTrim();

			if("post_tag".equals(domain)){
				String stringTag = text;
				Tag tag = site.getTag(nicename);
				if(tag != null){
					stringTag = tag.getSlug();
				}
				
				tags.add(stringTag);
			}
			if("category".equals(domain)){
				String stringCategory = text;
				Category category = site.getCategory(nicename);
				if(category != null){
					stringCategory = category.getNicename();
				}
				cats.add(stringCategory);
			}
		}
		
		List<String> lines = new ArrayList<String>();
		lines.add(Source.TRIPLE_DASHED_LINE);
		lines.add("layout: " + postType);
		lines.add("title: '" + title + "'");
//		lines.add("name", name);
		lines.add("comments: " +  comments);
		lines.add("published: " + published);
		lines.add("date: '" + date + "'");
		
		if("true".equals(props.get("include_author"))){
			lines.add("author: " + author);
		}
		
		lines.add("link: " + link);
		lines.add("post_id: " + postid);
		if(url != null){
			lines.add("url: '" + url + "'");
		}

		if(StringUtils.isNotBlank(excerpt)){
			excerpt = excerpt.replace('"', '\'');
			lines.add("excerpt: \"" + excerpt + "\"");
		}
		
		if(!cats.isEmpty()){
			lines.add("categories: " + cats);
		}
		if(!tags.isEmpty()){
			lines.add("tags: " + tags);
		}
		
		List<Element> meta = e.elements("postmeta");
		for(Element n: meta){
			String key = n.elementTextTrim("meta_key");
			String value = n.elementTextTrim("meta_value");
			if(key.startsWith("_")){
				log.debug("It's a WordPress intenal meta, skip parse: " + key);
				continue;
			}
			lines.add(key + ": \"" + value + "\"");
		}
		
		
		lines.add(Source.TRIPLE_DASHED_LINE);
		lines.addAll(contentLines);
		
		//filename
		String filename = NAME_FORMAT.format(parse) + "-" + postname + ".html";

		String importDir = (String) props.get("import_dir");
		if(StringUtils.isBlank(importDir)){
			importDir = "wordpress";
		}
		
		File dir = site.getSource();
		File file = new File(dir, importDir + "/" + filename);

		if(!file.getParentFile().exists()){
			file.getParentFile().mkdir();
		}
		log.info("Writing file " + file);
		FileUtils.writeLines(file, "UTF-8", lines);
	}
	
	private List<String> processContent(String content, StringBuilder excerptBuilder) {
		boolean excerptFound = false;
		List<String> contentLines = new ArrayList<String>();
		boolean lastLineIsBlank = true;
		LineIterator it = IOUtils.lineIterator(new StringReader(content));
		int preCount = 0;
		while(it.hasNext()){
			String line = it.next();
			boolean isBlank = StringUtils.isBlank(line);
			if(!isBlank){
				String lower = line.toLowerCase().trim();
				if(preCount == 0 && lastLineIsBlank && !lower.startsWith("<h") && !lower.startsWith("<!--more-->")){
					line = "<p>" + line;
				}
				contentLines.add(line);
				
				if(excerptBuilder != null){
					int indexOf = line.indexOf("<!--more-->");
					if(indexOf >= 0){
						excerptBuilder.append(line.substring(0, indexOf));
						excerptFound = true;
					}else{
						if(!excerptFound){
							excerptBuilder.append(line);
						}
					}
				}
				if(lower.contains("<pre>") || lower.contains("<pre ")){
					preCount++;
				}
				if(lower.contains("</pre>")){
					preCount--;
				}
				lastLineIsBlank = false;
			}else{
				lastLineIsBlank = true;
				contentLines.add(line);
			}
		}
		return contentLines;
	}
	
	/**
	 *  Build post/page url.
	 * 
	 * <p>Permalink details: http://codex.wordpress.org/Using_Permalinks.
	 * <p> %year%, %monthnum%, %day%, %hour%, %minute%, %second%, %postname%, %post_id%, 
	 * %category%,%tag%,%author%</p> 
	 * @param date
	 * @param postname
	 * @param post_id
	 * @param author
	 * @param props
	 * @return page/post url, return null if no 'permalink_style' defined.
	 */
	private String buildURL(Date date, String postname, String post_id, String author, Map<String,Object> props){
		String permalinkStyle = (String) props.get("permalink_style");
		if(StringUtils.isBlank(permalinkStyle)){
			return null;
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int year = c.get(Calendar.YEAR);
		int monthnum = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		
		permalinkStyle = StringUtils.replace(permalinkStyle, "%postname%", postname);
		permalinkStyle = StringUtils.replace(permalinkStyle, "%post_id%", post_id);
		permalinkStyle = StringUtils.replace(permalinkStyle, "%author%", author);
		permalinkStyle = StringUtils.replace(permalinkStyle, "%year%", year + "");
		permalinkStyle = StringUtils.replace(permalinkStyle, "%monthnum%", StringUtils.leftPad(monthnum + "", 2, '0'));
		permalinkStyle = StringUtils.replace(permalinkStyle, "%day%", StringUtils.leftPad(day + "", 2, '0'));
		permalinkStyle = StringUtils.replace(permalinkStyle, "%hour%", StringUtils.leftPad(hour + "", 2, '0'));
		permalinkStyle = StringUtils.replace(permalinkStyle, "%minute%", StringUtils.leftPad(minute + "", 2, '0'));
		permalinkStyle = StringUtils.replace(permalinkStyle, "%second%", StringUtils.leftPad(second + "", 2, '0'));
		
		return permalinkStyle;
	}
}
