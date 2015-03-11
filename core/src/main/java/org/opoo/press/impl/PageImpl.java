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
import org.opoo.press.*;

import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public class PageImpl extends AbstractBase implements Page{
	private Pager pager;
	
	/**
	 * Construct a page instance.
	 * @param site
	 * @param source
	 */
	public PageImpl(Site site, Source source) {
		super(site, source);
		resetUrl();
	}

	/**
	 * @param site
	 * @param source
	 * @param pager
	 */
	public PageImpl(Site site, Source source, Pager pager) {
		super(site, source);
		setPager(pager);
	}

	private void resetUrl(){
		String url = (String) getSource().getMeta().get("url");
		if(url == null){
			SourceEntry sourceEntry = getSource().getSourceEntry();
			String baseName = FilenameUtils.getBaseName(sourceEntry.getName());
			String path = sourceEntry.getPath();
			int pageNumber = 1;
			if(pager != null){
				pageNumber = pager.getPageNumber();
			}
			
			String ext = this.getOutputFileExtension();
			if(pageNumber > 1){
				if("index".equals(baseName) && ".html".equals(ext)){
					// index page or archive page. eg: "/page/2/index.html" = > "/page/2/"
					url = path + "/page/" + pageNumber + "/"; 
				}else{
					//eg:/category/page.html => /category/page-p3.html
					url = path + "/" + baseName + "-p" + pageNumber + ext;
				}
			}else{
				if("index".equals(baseName) && ".html".equals(ext)){
					// "index.html" page url will be remove filename. 
					//eg: "/index.html" => "/" or "/about/index.html" => "/about/"
					url = path + "/";
				}else{
					// "/about.html"
					url = path + "/" + baseName + ext;
				}
			}
		}
		setUrl(url);
	}
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Page#setPager(org.opoo.press.Pager)
	 */
	@Override
	public void setPager(Pager pager) {
		this.pager = pager;
		//reset
		resetUrl();
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Page#getPager()
	 */
	@Override
	public Pager getPager() {
		return pager;
	}
	
	@Override
	protected void mergeRootMap(Map<String, Object> rootMap) {
		super.mergeRootMap(rootMap);
		rootMap.put("page", this);
		if(pager != null){
			rootMap.put("paginator", pager);
		}
	}
	
	@Override
	protected boolean containsHighlightCodeBlock(Highlighter highlighter) {
		boolean contains = super.containsHighlightCodeBlock(highlighter);
		if(contains){
			return true;
		}
		
		//check pager.posts excerpt
		if(pager != null && pager.getPosts() != null){
			for(Base post: pager.getPosts()){
				if(post instanceof Excerptable){
				String excerpt = ((Excerptable)post).getExcerpt();
				if(highlighter.containsHighlightCodeBlock(excerpt)){
					log.debug("Found highlighter code block in post excerpt: " + post.getTitle());
					return true;
				}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param current
	 * @param targetPageNumber
	 * @return the page object by the specified page number.
	 */
	public Page getPage(Page current, int targetPageNumber){
		if(current == null){
			log.warn("Current page is null, cannot found target page for pagenumber " + targetPageNumber);
			return null;
		}
		Pager pa = current.getPager();
		if(pa == null){
			log.warn("Current page is not one of a pigination page.");
			return null;
		}
		int currentPageNumber = pa.getPageNumber();
		if(currentPageNumber == targetPageNumber){
			return current;
		}else if(targetPageNumber > currentPageNumber){
			return getPage(pa.getNext(), targetPageNumber);
		}else{
			return getPage(pa.getPrevious(), targetPageNumber);
		}
	}
	
	/**
	 * 查找指定页码的 page 对象。
	 * @param targetPageNumber
	 * @return the target page
	 */
	public Page getPage(int targetPageNumber){
		return getPage(this, targetPageNumber);
	}
}
