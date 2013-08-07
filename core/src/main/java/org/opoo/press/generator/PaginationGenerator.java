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

import java.util.ArrayList;
import java.util.List;

import org.opoo.press.Generator;
import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.press.impl.PageImpl;

/**
 * @author Alex Lin
 *
 */
public class PaginationGenerator implements Generator {

	/* (non-Javadoc)
	 * @see org.opoo.press.Generator#generate(org.opoo.press.Site)
	 */
	@Override
	public void generate(Site site) {
		List<Page> pages = site.getPages();
		List<Page> allNewPages = new ArrayList<Page>();
		for(Page page: pages){
			if(Pager.isPaginationEnabled(site.getConfig().toMap(), page)){
				paginate(site, page, allNewPages);
			}
		}
		if(!allNewPages.isEmpty()){
			site.getPages().addAll(allNewPages);
		}
	}

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
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 100;
	}
}
