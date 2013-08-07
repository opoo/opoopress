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
import java.util.List;

import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.press.Tag;
import org.opoo.util.URLUtils;

/**
 * @author Alex Lin
 *
 */
public class TagImpl implements Tag {
	private String slug;
	private String name;
	private List<Post> posts = new ArrayList<Post>();
	
	private String url;
	/**
	 * @param slug
	 * @param name
	 */
	public TagImpl(String slug, String name, Site site) {
		this.slug = slug;
		this.name = name;
		
		String tagDir = site.getConfig().get("tag_dir", "");
		this.url = /*rootUrl + */ tagDir + "/" + URLUtils.encodeURL(slug) + "/";
	}
	/**
	 * @return the slug
	 */
	public String getSlug() {
		return slug;
	}

	/**
	 * @param slug the slug to set
	 */
	public void setSlug(String slug) {
		this.slug = slug;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see org.opoo.press.Tag#isNameOrSlug(java.lang.String)
	 */
	@Override
	public boolean isNameOrSlug(String nameOrSlug) {
		if(nameOrSlug.equals(getSlug())){
			return true;
		}
		
		if(nameOrSlug.equals(getName())){
			return true;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.opoo.press.Tag#getUrl()
	 */
	@Override
	public String getUrl() {
		return url;
	}
	/* (non-Javadoc)
	 * @see org.opoo.press.PostsHolder#getPosts()
	 */
	@Override
	public List<Post> getPosts() {
		return posts;
	}
	
	public int getPostSize(){
		return posts.size();
	}
}
