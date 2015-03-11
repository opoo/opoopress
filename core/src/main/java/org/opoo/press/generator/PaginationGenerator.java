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
package org.opoo.press.generator;

import org.opoo.press.*;
import org.opoo.press.impl.PageImpl;
import org.opoo.press.SourceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Lin
 *
 */
public class PaginationGenerator implements Generator {
	private static final Logger log = LoggerFactory.getLogger(PaginationGenerator.class);
	/* (non-Javadoc)
	 * @see org.opoo.press.Generator#generate(org.opoo.press.Site)
	 */
	@Override
	public void generate(Site site) {
		log.debug("Generating paginate pages...");

		List<Page> pages = site.getPages();
		Config config = site.getConfig();
		
		List<Page> allNewPages = new ArrayList<Page>();
		for(Page page: pages){
			if(isPaginationEnabled(config, page)){
				List<Page> list = paginate(site, page, site.getPosts());
				allNewPages.addAll(list);
			}
		}
		if(!allNewPages.isEmpty()){
			site.getPages().addAll(allNewPages);
		}
	}

	/*
	private void paginate(Site site, Page page, List<Page> allNewPages) {
		List<Post> posts = site.getPosts();
		int pageSize = ((Number) site.getConfig().get("paginate")).intValue();
		
		int totalPosts = posts.size();
		int totalPages = Pager.calculateTotalPages(totalPosts, pageSize);
		
		Page[] pages = new Page[totalPages];
		Pager[] pagers = new Pager[totalPages];
		for(int i = 0 ; i < totalPages ; i++){
			int pageNumber = i + 1;
			int fromIndex = i * pageSize;
			int toIndex = fromIndex + pageSize;
			if(toIndex > totalPosts){
				toIndex = totalPosts;
			}
			List<Post> pagePosts = posts.subList(fromIndex, toIndex);
			
			Pager pager = new Pager(pageNumber, totalPages, totalPosts, pageSize, pagePosts);
			if(pageNumber > 1){
				PageImpl impl = new PageImpl(site, page.getSource(), pager);
//				site.getPages().add(impl);
				pages[i] = impl;
				allNewPages.add(impl);
			}else{
				page.setPager(pager);
				pages[i] = page;
			}
			pagers[i] = pager;
		}
		
		//set next and previous 
		for(int i = 0 ; i < totalPages ; i++){
			if(i > 0){
				pagers[i].setPrevious(pages[i - 1]);
			}
			if(i < (totalPages - 1 )){
				pagers[i].setNext(pages[i + 1]);
			}
		}
	}*/
	
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 100;
	}
	
	
	
	public static boolean isPaginationEnabled(Site site, Page page){
		return isPaginationEnabled(site.getConfig(), page);
	}

	public static boolean isPaginationEnabled(Config config, Page page) {
		SourceEntry entry = page.getSource().getSourceEntry();
		String name = entry.getName();
		String path = entry.getPath();
		// (isIndexPaginationEnabled || isNormalPagePaginationEnabled) && containsPaginatorInContent
		return (config.get("paginate") != null && "".equals(path) && "index.html".equals(name)
				|| page.get("paginate") != null)
				&& page.getContent().contains("paginator.");
	}
	
	public static int calculateTotalPages(int totalPosts, int pageSize){
		int totalPages = (int) Math.ceil((double) totalPosts / (double) pageSize);
		return totalPages;
	}
	
	public static List<Page> paginate(Site site, Page page, List<? extends Base> posts){
		Number number = (Number) page.get("paginate");
		if(number == null){
			number = (Number) site.getConfig().get("paginate");
		}
		if(number == null){
			throw new IllegalArgumentException("No page size variable specified.");
		}
		
		int pageSize = number.intValue();
		
		return paginate(site, page, posts, pageSize);
	}
	
	public static List<Page> paginate(Site site, Page page, List<? extends Base> posts, int pageSize) {
		if(posts.isEmpty()){
			throw new RuntimeException("There is no post in this site.");
		}

		//only new pages, exclude first page
		List<Page> newPages = new ArrayList<Page>();
		int totalPosts = posts.size();
		int totalPages = calculateTotalPages(totalPosts, pageSize);
		
		Page[] pages = new Page[totalPages];
		Pager[] pagers = new Pager[totalPages];
		for(int i = 0 ; i < totalPages ; i++){
			int pageNumber = i + 1;
			int fromIndex = i * pageSize;
			int toIndex = fromIndex + pageSize;
			if(toIndex > totalPosts){
				toIndex = totalPosts;
			}
			List<? extends Base> pagePosts = posts.subList(fromIndex, toIndex);
			
			Pager pager = new Pager(pageNumber, totalPages, totalPosts, pageSize, pagePosts);
			if(pageNumber > 1){
				PageImpl impl = new PageImpl(site, page.getSource(), pager);
//				site.getPages().add(impl);
				pages[i] = impl;
				newPages.add(impl);
			}else{
				page.setPager(pager);
				pages[i] = page;
			}
			pagers[i] = pager;
		}
		
		//set next and previous 
		int maxIndex = totalPages - 1;
		for(int i = 0 ; i < totalPages ; i++){
			if(i > 0){
				pagers[i].setPrevious(pages[i - 1]);
			}
			if(i < maxIndex){
				pagers[i].setNext(pages[i + 1]);
			}
		}
		
		return newPages;
	}
}
