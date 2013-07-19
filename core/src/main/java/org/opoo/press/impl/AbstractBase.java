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
package org.opoo.press.impl;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Base;
import org.opoo.press.Converter;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.highlighter.Highlighter;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.opoo.press.util.MapUtils;

/**
 * @author Alex Lin
 *
 */
public abstract class AbstractBase extends AbstractConvertible implements Base{
	protected Log log = LogFactory.getLog(getClass());
	
	private static DateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static DateFormat f2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final Map<String, Object> data;
	
	private Source source;
	private Site site;

	private String content;
	private String url;
	private String path;
	private String layout;
	private String permalink;
	private Date date;
	private Date updated;
	private String dateFormatted;
	private String updatedFormatted;
	
	private String outputFileExtension;
	private Converter converter;
	
	AbstractBase(Site site, Source source){
		this.source = source;
		this.site = site;
		this.data = new HashMap<String,Object>(source.getMeta()); 
		String title = (String) data.get("title");
		if(title != null){
			log = LogFactory.getLog(getClass().getName() + "[" + title + "]");
		}
		init();
	}
	
	public void set(String name, Object value){
		MapUtils.put(data, name, value);
	}
	
	public Object get(String name){
		return data.get(name);
	}
	
	private void init() {
		this.converter = site.getConverter(source);
		this.outputFileExtension = this.converter.getOutputFileExtension(source);
		
		this.content = source.getContent();
		this.layout = (String) source.getMeta().get("layout");
		this.permalink = (String) source.getMeta().get("permalink");

		path = (String) source.getMeta().get("path");
		if(path == null){
			SourceEntry sourceEntry = source.getSourceEntry();
			path = sourceEntry.getPath() + "/" + sourceEntry.getName();
		}
		
		//date, updated
		date = lookup(source.getMeta(), "date");
		updated = lookup(source.getMeta(), "updated");
		
		//date_formatted, update_formatted
		String dateStyle = getDateFormat();
		dateFormatted = formatDate(date, dateStyle);
		updatedFormatted = formatDate(updated, dateStyle);
	}
	
	private String getDateFormat(){
		String dateStyle = (String) site.getConfig().get("date_format");
		if(dateStyle == null){
			dateStyle = "yyyy-MM-dd";
		}else if("ordinal".equals(dateStyle)){
			dateStyle = "MMM d yyyy";
		}
		return dateStyle;
	}
	
	private String formatDate(Date date, String style){
		if(date != null){
			if(site.getLocale() != null){
				return new SimpleDateFormat(style, site.getLocale()).format(date);
			}else{
				return new SimpleDateFormat(style).format(date);
			}
		}
		return null;
	}
	
	private Date lookup(Map<String, Object> frontMatter, String dateName){
		Object date = frontMatter.get(dateName);
		if(date != null && !(date instanceof Date)){
			String string = date.toString();
			//try parse from yyyy-MM-dd HH:mm
			try {
				date = f1.parse(string);
			} catch (ParseException e) {
				//ignore
			}
			if(date == null){
				try {
					date = f2.parse(string);
				} catch (ParseException e) {
					//ignore
				}
			}
//			if(date == null){
//				frontMatter.remove(dateName);
//			}else{
//				frontMatter.put(dateName, date);
//			}
		}
		return (Date)date;
	}

	public String getOutputFileExtension(){
		return this.outputFileExtension;
	}
	
	public Site getSite(){
		return site;
	}
	
	public Source getSource(){
		return source;
	}
	
	/**
	 * @return the converter
	 */
	protected Converter getConverter() {
		return converter;
	}

	protected Renderer getRenderer(){
		return site.getRenderer();
	}
	
	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}
	public void setContent(String content){
		this.content = content;
	}

	protected void convert(){
		this.content = this.converter.convert(content);
	}
	
	protected void mergeRootMap(Map<String,Object> rootMap){
		String canonical = site.buildCanonical(getUrl());
		rootMap.put("canonical", canonical);
		mergeHighlighterParam(rootMap);
	}

	/**
	 * @param rootMap
	 */
	private void mergeHighlighterParam(Map<String, Object> rootMap) {
		Highlighter highlighter = site.getHighlighter();
		if(highlighter != null && ".html".equals(outputFileExtension)
				&& containsHighlightCodeBlock(highlighter)){
			log.debug("The content contains highlight code block.");
			rootMap.put("highlighter", highlighter.getHighlighterName());
		}
	}
	
	/**
	 * @param highlighter
	 */
	protected boolean containsHighlightCodeBlock(Highlighter highlighter) {
		return highlighter.containsHighlightCodeBlock(getContent());
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPath() {
		return path;
	}

	public String getLayout() {
		return layout;
	}

	public String getPermalink() {
		return permalink;
	}

	public Date getDate() {
		return date;
	}

	public Date getUpdated() {
		return updated;
	}

	public String getDateFormatted() {
		return dateFormatted;
	}

	public String getUpdatedFormatted() {
		return updatedFormatted;
	}
	
	/**
	 * For freemarker template.
	 * @return the date formatted string
	 */
	public String getDate_formatted(){
		return getDateFormatted();
	}
	
	/**
	 * For freemarker template.
	 * @return the update date formatted string
	 */
	public String getUpdated_formatted(){
		return getUpdatedFormatted();
	}
}
