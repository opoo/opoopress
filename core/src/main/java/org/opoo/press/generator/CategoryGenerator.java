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

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opoo.press.Generator;
import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.Post;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.impl.AbstractConvertible;
import org.opoo.press.source.Source;
import org.opoo.press.util.Utils;

/**
 * @author Alex Lin
 *
 */
public class CategoryGenerator implements Generator {

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 200;
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Generator#generate(org.opoo.joctopress.Site)
	 */
	@Override
	public void generate(Site site) {
		Map<String, List<Post>> map = site.getCategories();
		Map<String, String> names = site.getCategoryNames();
		
		Renderer renderer = site.getRenderer();
		for(Map.Entry<String, String> en: names.entrySet()){
			String neme = en.getKey();
			String category = en.getValue();
			List<Post> posts = map.get(neme);
			if(posts != null && !posts.isEmpty()){
				Collections.sort(posts);
				Collections.reverse(posts);
				
				CategoryPage page = new CategoryPage(site, renderer);
				page.setTitle(/*"Category: " + */category);
				page.setUrl(Utils.buildCategoryUrl(site, neme));
				page.setPosts(posts);
				
				site.getPages().add(page);
			}
		}
	}
	
	
	public static class CategoryPage extends AbstractConvertible implements Page{
		public static final String TEMPLATE = "category_index.ftl";
		private String url;
		private Renderer renderer;
		private Site site;
		private String content;// = "<#include \"category_index.ftl\">";
		private String title;
		private List<Post> posts;
		
		private CategoryPage(Site site, Renderer renderer) {
			super();
			this.site = site;
			this.renderer = renderer;
		}

		@Override
		public void render(Map<String, Object> rootMap) {
			rootMap = new HashMap<String,Object>(rootMap);
			mergeRootMap(rootMap);

			String output = getRenderer().render(TEMPLATE, rootMap);
			setContent(output);
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
		
		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getSource()
		 */
		@Override
		public Source getSource() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getContent()
		 */
		@Override
		public String getContent() {
			return content;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getPath()
		 */
		@Override
		public String getPath() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getLayout()
		 */
		@Override
		public String getLayout() {
			return "nil";
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getPermalink()
		 */
		@Override
		public String getPermalink() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getDate()
		 */
		@Override
		public Date getDate() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getUpdated()
		 */
		@Override
		public Date getUpdated() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getDateFormatted()
		 */
		@Override
		public String getDateFormatted() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Base#getUpdatedFormatted()
		 */
		@Override
		public String getUpdatedFormatted() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Page#getPager()
		 */
		@Override
		public Pager getPager() {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.Page#setPager(org.opoo.joctopress.Pager)
		 */
		@Override
		public void setPager(Pager pager) {
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.impl.AbstractConvertible#setContent(java.lang.String)
		 */
		@Override
		public void setContent(String content) {
			this.content = content;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.impl.AbstractConvertible#getOutputFileExtension()
		 */
		@Override
		public String getOutputFileExtension() {
			return ".html";
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.impl.AbstractConvertible#getRenderer()
		 */
		@Override
		protected Renderer getRenderer() {
			return renderer;
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.impl.AbstractConvertible#convert()
		 */
		@Override
		protected void convert() {
			//do nothing
		}

		/* (non-Javadoc)
		 * @see org.opoo.joctopress.impl.AbstractConvertible#mergeRootMap(java.util.Map)
		 */
		@Override
		protected void mergeRootMap(Map<String, Object> rootMap) {
			rootMap.put("canonical", Utils.buildCanonical(site, getUrl()));
			rootMap.put("page", this);
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public List<Post> getPosts() {
			return posts;
		}

		public void setPosts(List<Post> posts) {
			this.posts = posts;
		}
		
		public boolean isFooter(){
			return false;
		}
		
		public boolean isSidebar(){
			return true;
		}
		
		public Object get(String string){
			return null;
		}
		
		@Override
		public File getOutputFile(File dest) {
			String url = getUrl() + "index.html";
			url = Utils.decodeURL(url);
			File target = new File(dest, url);
			return target;
		}
		
		public boolean isComments(){
			return false;
		}
	}
}
