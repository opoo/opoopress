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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.*;
import org.opoo.press.util.LinkUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Alex Lin
 *
 */
public class PostImpl extends AbstractBase implements Post, Comparable<Post>{
	public static final String DEFAUL_EXCERPT_SEPARATOR = "<!--more-->";
	public static Pattern FILENAME_PATTERN = Pattern.compile("[1-9][0-9]{3}[-][0-1][0-9][-][0-3][0-9][-](.*)");
	//excerptSeparator = "<!--more-->";
	
	private List<String> stringCategories;
	private List<String> stringTags;
	private List<Category> categories = new ArrayList<Category>();
	private List<Tag> tags = new ArrayList<Tag>();
	
	private String id;
	private boolean published = true;
//	private String title;
	private String excerpt;
	private boolean excerpted;
	private boolean isExcerptExtracted = false;
	private List<Post> relatedPosts;
	
	private Post next;
	private Post previous;
	
	/**
	 * @param site
	 * @param source
	 */
	PostImpl(Site site, Source source) {
		super(site, source);
		init();
	}
	
	private void init() {
		if(getDate() == null || getDateFormatted() == null){
			throw new IllegalArgumentException("Date is required in post yaml front-matter header: " 
					+ getSource().getSourceEntry().getFile());
		}
		
		Map<String, Object> frontMatter = getSource().getMeta();
		
//		title = (String) frontMatter.get("title");

		stringCategories = getStringList(frontMatter, "categories", "category");
		
		initCategories(stringCategories);
		
		stringTags = getStringList(frontMatter, "tags", "tag");
		
		initTags(stringTags);
		
		String url = (String)frontMatter.get("url");
		if(url == null){
			String permalinkStyle = getPossiblePermalink();
			SourceEntry sourceEntry = getSource().getSourceEntry();
			url = buildPostUrl(permalinkStyle, getDate(), sourceEntry.getPath(), sourceEntry.getName(), getSource().getMeta());
		}
		setUrl(url);
		this.id = url;
		
		Boolean bool = (Boolean) frontMatter.get("published");
		published = bool == null || bool.booleanValue();
		
		excerpt = (String) frontMatter.get("excerpt");
		if(StringUtils.isNotBlank(excerpt)){
			excerpted = true;
		}else{
			extractExcerpt(getContent());
			isExcerptExtracted = true;
		}
	}

	private void initCategories(List<String> stringCategories) {
		if(stringCategories == null || stringCategories.isEmpty()){
			return;
		}
		Site site = getSite();
		for(String stringCategory: stringCategories){
			Category category = site.getCategory(stringCategory);
			if(category == null){
				String nicename = site.toNicename(stringCategory);
				category = new CategoryImpl(nicename, stringCategory, site);
				//add to site categories
				site.getCategories().add(category);
			}
			category.getPosts().add(this);
			this.categories.add(category);
		}
	}
	
	private void initTags(List<String> stringTags) {
		if(stringTags == null || stringTags.isEmpty()){
			return;
		}
		Site site = getSite();
		for(String stringTag: stringTags){
			Tag tag = site.getTag(stringTag);
			if(tag == null){
				String slug = site.toSlug(stringTag);
				tag = new TagImpl(slug, stringTag, site);
				//add to site tags list
				site.getTags().add(tag);
			}
			tag.getPosts().add(this);
			this.tags.add(tag);
		}
	}
	
	private void extractExcerpt(String content) {
		String excerptSeparator = (String) getSite().getConfig().get("excerpt_separator");
		if(excerptSeparator == null){
			excerptSeparator = DEFAUL_EXCERPT_SEPARATOR; //"<!--more-->";
		}
		int index = content.indexOf(excerptSeparator);
		if(index != -1){
			excerpted = true;
			excerpt = content.substring(0, index);
		}else{
			excerpted = false;
			excerpt = content;
		}
	}

	private List<String> getStringList(Map<String, Object> frontMatter, String listName, String name){
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) frontMatter.get(listName);
		if(list == null || list.isEmpty()){
			String str = (String) frontMatter.get(name); 
			if(str != null){
				list = new ArrayList<String>();
				list.add(str);
			}
		}
		return list;
	}

	
	/* (non-Javadoc)
	 * @see org.opoo.press.impl.Convertible#convert()
	 */
	@Override
	public void convert() {
		super.convert();
		if(isExcerptExtracted){
			this.excerpt = getConverter().convert(excerpt);
		}
	}

	public List<String> getStringCategories() {
		return stringCategories;
	}

	public List<String> getStringTags() {
		return stringTags;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

//	/**
//	 * @return the title
//	 */
//	public String getTitle() {
//		return title;
//	}

	/**
	 * @return the excerpt
	 */
	public String getExcerpt() {
		return excerpt;
	}

	/**
	 * @return the excerpted
	 */
	public boolean isExcerpted() {
		return excerpted;
	}

	@Override
	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}

	public boolean isPublished(){
		return published;
	}
	
	/**
	 * @return the next
	 */
	public Post getNext() {
		return next;
	}

	
	public void setNext(Post next) {
		this.next = next;
	}

	public void setPrevious(Post previous) {
		this.previous = previous;
	}

	/**
	 * @return the previous
	 */
	public Post getPrevious() {
		return previous;
	}
	
	public List<Post> getRelatedPosts() {
		return relatedPosts;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Post o) {
		return getDate().compareTo(o.getDate());
	}
	
	
//	@Override
//	public void render(Map<String, Object> rootMap) {
//		super.render(rootMap);
//		renderExcerpt(rootMap);
//	}
	
	
	
	@Override
	protected void mergeRootMap(Map<String, Object> rootMap) {
		super.mergeRootMap(rootMap);
		rootMap.put("page", this);
		
		//Related posts
		//Map<String, Object> site = (Map<String, Object>) rootMap.get("site");
		//set("related_posts", findRelatedPost());
		
		//since 1.0.2
		List<Post> relatedPosts = getSite().getFactory().getRelatedPostsFinder().findRelatedPosts(this);
		set("related_posts", relatedPosts);
	}

//	private void renderExcerpt(Map<String,Object> rootMap){
////		rootMap = new HashMap<String,Object>(rootMap);
////		rootMap.put("page", this);
////		populateRootMap(rootMap);
////		excerpt = getRenderer().render("nil", excerpt, rootMap);
//		Renderer renderer = site.getRenderer();
//
//		boolean isExcerptRenderRequired =  getRenderer().isRenderRequired(excerpt);
//		if(isExcerptRenderRequired){
//			excerpt = getRenderer().renderContent(excerpt, rootMap);
//		}
//	}

	/**
	 * @return the categories
	 */
	public List<Category> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	/**
	 * @return the tags
	 */
	public List<Tag> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
	private String getPossiblePermalink(){
		String link = getPermalink();
		if(StringUtils.isNotBlank(link)){
			return link;
		}
		return getSite().getPermalink();
	}

	/**
	 *
	 * @param permalinkStyle
	 * @param date
	 * @param pathToFile
	 * @param fileName
	 * @param meta
	 * @return
	 */
	private String buildPostUrl(String permalinkStyle, Date date, String pathToFile, String fileName, Map<String, Object> meta) {
		String baseName = FilenameUtils.getBaseName(fileName);
		String name = baseName;
		if(FILENAME_PATTERN.matcher(baseName).matches()){
			name = baseName.substring(11);
		}
		
		if(StringUtils.isBlank(permalinkStyle)){
			return pathToFile + "/" + name + "/";
		}
		
		Map<String, Object> params = new HashMap<String,Object>(meta);
		params.put("pathToFile", pathToFile);
		params.put("fileName", fileName);
		params.put("name", name);
		LinkUtils.addDateParams(params, date);

		return LinkUtils.renderUrl(permalinkStyle, params);
	}
}
