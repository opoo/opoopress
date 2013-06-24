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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Post;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.opoo.press.util.Utils;

/**
 * @author Alex Lin
 *
 */
public class PostImpl extends AbstractBase implements Post, Comparable<Post>{
	private List<String> categories;
	private List<String> tags;
	private String id;
	private boolean published = true;
	private String title;
	private String excerpt;
	private boolean excerpted;
	private boolean isExcerptExtracted = false;
	private List<Post> relatedPosts;
	
	private Post next;
	private Post previous;
	
	/**
	 * @param siteManager
	 * @param frontMatterSource
	 */
	PostImpl(SiteImpl site, Source source) {
		super(site, source);
		init();
	}
	
	private void init() {
		if(getDate() == null || getDateFormatted() == null){
			throw new IllegalArgumentException("Date is required in post yaml front-matter header: " 
					+ getSource().getSourceEntry().getFile());
		}
		
		Map<String, Object> frontMatter = getSource().getMeta();
		
		title = (String) frontMatter.get("title");

		categories = getStringList(frontMatter, "categories", "category");
		
		tags = getStringList(frontMatter, "tags", "tag");
		
		String url = (String)frontMatter.get("url");
		if(url == null){
			SourceEntry sourceEntry = getSource().getSourceEntry();
			String baseName = FilenameUtils.getBaseName(sourceEntry.getName());
			/*String */url = sourceEntry.getPath() + "/" + baseName + "/";
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

	private void extractExcerpt(String content) {
		String excerptSeparator = (String) getSite().getConfig().get("excerpt_separator");
		if(excerptSeparator == null){
			excerptSeparator = "<!--more-->";
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
	 * @see org.opoo.joctopress.impl.Convertible#convert()
	 */
	@Override
	protected void convert() {
		super.convert();
		if(isExcerptExtracted){
			this.excerpt = getConverter().convert(excerpt);
		}
	}

	/**
	 * @return the categories
	 */
	public List<String> getCategories() {
		return categories;
	}

	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

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
	
	
	@Override
	public void render(Map<String, Object> rootMap) {
		super.render(rootMap);
		renderExcerpt(rootMap);
	}
	
	
	
	@Override
	protected void mergeRootMap(Map<String, Object> rootMap) {
		super.mergeRootMap(rootMap);
		rootMap.put("page", this);
		
		//Related posts
		//Map<String, Object> site = (Map<String, Object>) rootMap.get("site");
		//site.put("related_posts", "");
		//long start = System.currentTimeMillis();
		set("related_posts", findRelatedPost());
		//long time = System.currentTimeMillis() - start;
		//log.info("findRelatedPost =========> " + time + "ms");
	}
	
	private List<Post> findRelatedPost(){
		Number num = (Number) getSite().getConfig().get("related_posts");
		if(num == null){
			num = 5;
		}
		int n = num.intValue();
		if(n == 0){
			return null;
		}
		
		List<String> list = getCategories();
		if(list == null || list.isEmpty()){
			return null;
		}
		Map<String, List<Post>> map = getSite().getCategories();
		List<Post> allRelatedPosts = new ArrayList<Post>();
		for(String cat: list){
			String lo = Utils.toSlug(cat);
			List<Post> posts = map.get(lo);
			mergeRelatedPosts(allRelatedPosts, posts);
		}
		if(allRelatedPosts.isEmpty()){
			return Collections.emptyList();
		}
		Collections.sort(allRelatedPosts);
		Collections.reverse(allRelatedPosts);
		
		if(n > allRelatedPosts.size()){
			n =  allRelatedPosts.size();
		}
		return allRelatedPosts.subList(0, n);
	}

	/**
	 * @param allRelatedPosts
	 * @param posts
	 */
	private void mergeRelatedPosts(List<Post> allRelatedPosts, List<Post> posts) {
		for(Post post: posts){
			if(post.equals(this)){
				continue;
			}
			if(allRelatedPosts.contains(post)){
				continue;
			}
			allRelatedPosts.add(post);
		}
	}

	private void renderExcerpt(Map<String,Object> rootMap){
//		rootMap = new HashMap<String,Object>(rootMap);
//		rootMap.put("page", this);
//		populateRootMap(rootMap);
//		excerpt = getRenderer().render("nil", excerpt, rootMap);
		
		boolean isExcerptRenderRequired =  getRenderer().isRenderRequired(excerpt);
		if(isExcerptRenderRequired){
			excerpt = getRenderer().renderContent(excerpt, rootMap);
		}
	}


//	/* (non-Javadoc)
//	 * @see org.opoo.joctopress.Post#write(java.io.File)
//	 */
//	@Override
//	public void write(File dest) {
//		super.write(dest);
//	}
	
	
    /**
     * title
    url
    date
    id
    categories
    next
    previous
    tags
    content
    excerpt
    path
     */
//	public Map<String, Object> toModel(){
//		Map<String, Object> map = super.toModel();
//		map.put("excerpt", getExcerpt());
//		map.put("excerpted", isExcerpted());
//		map.put("title", getTitle());
//		map.put("id", getId());
//		map.put("categories", getCategories());
//		map.put("tags", getTags());
//		map.put("published", isPublished());
//		if(next != null){
//			map.put("next", next.toModel());
//		}
//		if(previous != null){
//			map.put("previous", previous.toModel());
//		}
//		return map;
//	}
}
