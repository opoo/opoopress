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

import org.opoo.press.Category;
import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.util.URLUtils;

/**
 * @author Alex Lin
 *
 */
public class CategoryImpl implements Category{
	private Category parent;
	private List<Category> children = new ArrayList<Category>();
	private String nicename;
	private String name;
	private List<Post> posts = new ArrayList<Post>();
	private Site site;
	
	private String path;
	private String title;
	private String url;

	public CategoryImpl(String nicename, String name, Site site){
		this(nicename, name, null, site);
	}
	
	public CategoryImpl(String nicename, String name, Category parent, Site site) {
		this.parent = parent;
		this.nicename = nicename;
		this.name = name;
		this.site = site;
		init();
	}

	private void init(){
		if(parent != null){
			parent.getChildren().add(this);
		}
		boolean categoryTree = site.getConfig().get("category_tree", true);
		
		if(categoryTree && parent != null){
			path =  parent.getPath() + "." + nicename;
		}else{
			path = nicename;
		}
		
		if(categoryTree && parent != null){
			url = parent.getUrl() + URLUtils.encodeURL(nicename) + "/";
		}else{
			String categoryDir = site.getConfig().get("category_dir", "");
			url = categoryDir + "/" + URLUtils.encodeURL(nicename) + "/";
		}
		
		if(categoryTree && parent != null){
			title = parent.getTitle() + " &#8250; " + name;
		}else{
			title = name;
		}
	}
	
	/**
	 * @return the parent
	 */
	public Category getParent() {
		return parent;
	}
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Category#getChildren()
	 */
	@Override
	public List<Category> getChildren() {
		return children;
	}

	/**
	 * @return the nicename
	 */
	public String getNicename() {
		return nicename;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the posts
	 */
	public List<Post> getPosts() {
		return posts;
	}

	public boolean isNameOrNicename(String nameOrNicename){
		if(nameOrNicename.equalsIgnoreCase(getNicename())){
			return true;
		}
		
		if(nameOrNicename.equals(getName())){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Category#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Category#getUrl()
	 */
	@Override
	public String getUrl() {
		//String categoryDir = (String) config.get("category_dir");
		//return /*rootUrl + */ categoryDir + "/" + category.getUrl() + "/";
		return url;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Category#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

}
