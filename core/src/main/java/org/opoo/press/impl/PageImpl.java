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

import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.press.highlighter.Highlighter;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;

/**
 * @author Alex Lin
 *
 */
public class PageImpl extends AbstractBase implements Page{
	//private static final Log log = LogFactory.getLog(PageImpl.class);
	private Pager pager;
	
	/**
	 * 
	 * @param site
	 * @param frontMatterSource
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
			if(pageNumber > 1){
				// index page or archive page. eg: "/page/2/index.html" = > "/page/2/"
				url = path + "/page/" + pageNumber + "/"; 
			}else{
				String ext = this.getOutputFileExtension();
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
	 * @see org.opoo.joctopress.Page#setPager(org.opoo.joctopress.Pager)
	 */
	@Override
	public void setPager(Pager pager) {
		this.pager = pager;
		//reset
		resetUrl();
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Page#getPager()
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
			for(Post post: pager.getPosts()){
				String excerpt = post.getExcerpt();
				if(highlighter.containsHighlightCodeBlock(excerpt)){
					log.debug("Found highlighter code block in post excerpt: " + post.getTitle());
					return true;
				}
			}
		}
		return false;
	}

//	/* (non-Javadoc)
//	 * @see org.opoo.joctopress.Page#write(java.io.File)
//	 */
//	@Override
//	public void write(File dest) {
//		File file = getOutputFile(dest);
//		FileWriter fw = null;
//		try {
////			FileUtils.write(file, getContent(), "UTF-8");
//			fw = new FileWriter(file);
//			IOUtils.write(getContent(), fw);
//			fw.flush();
//		} catch (IOException e) {
//			log.error("Write page file error: " + file, e);
//			throw new RuntimeException(e);
//		}finally{
//			IOUtils.closeQuietly(fw);
//		}
//	}
	
	
	/**
	 * 
	 * @param current
	 * @param targetPageNumber
	 * @return
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
	 * 
	 * @param targetPageNumber
	 * @return
	 */
	public Page getPage(int targetPageNumber){
		return getPage(this, targetPageNumber);
	}
}
