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

import org.apache.commons.lang.StringUtils;
import org.opoo.press.Category;
import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.press.Source;
import org.opoo.press.Tag;

import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public class SourcePost extends AbstractSourcePage implements Post, Comparable<Post>{
	private String id;
	private String excerpt;
//	private boolean excerpted;
	private boolean excerptExtracted = false;


	public SourcePost(Site site, Source source) {
		super(site, source, null);
		init();
	}
	
	private void init() {
		if(getDate() == null || getDateFormatted() == null){
			throw new IllegalArgumentException("Date is required in post yaml front-matter header: " 
					+ getSource().getSourceEntry().getFile());
		}
		
		Map<String, Object> frontMatter = getSource().getMeta();

		String id = (String) frontMatter.get("id");
		
		excerpt = (String) frontMatter.get("excerpt");
		if(StringUtils.isBlank(excerpt)){
			excerpt = extractExcerpt(getSourceContent());
		}

		setTagsHolder(new ListHolderImpl<Tag>());
		setCategoriesHolder(new ListHolderImpl<Category>());
	}
	
	private String extractExcerpt(String content) {
		if(StringUtils.isBlank(content)){
			return null;
		}

		//default "<!--more-->";
		String excerptSeparator = getSite().getConfig().get("excerpt_separator", DEFAULT_EXCERPT_SEPARATOR);
		int index = content.indexOf(excerptSeparator);
		if(index > 0){
			String temp = content.substring(0, index);
			if(StringUtils.isNotBlank(temp)){
				excerptExtracted = true;
				return temp;
			}
		}

		return null;
	}

	
	/* (non-Javadoc)
	 * @see org.opoo.press.impl.Convertible#convert()
	 */
	@Override
	public void convert() {
		super.convert();
		if(excerptExtracted){
			log.debug("Converting excerpt.");
			this.excerpt = getConverter().convert(excerpt);

			if(log.isTraceEnabled()){
				log.trace("Excerpt converted[{}]: {}", getUrl(), excerpt);
			}
		}
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id != null ? id : getUrl();
	}

	public void setId(String id){
		this.id = id;
	}

	/**
	 * @return the excerpt
	 */
	@Override
	public String getExcerpt() {
		return excerpt;
	}

	@Override
	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}

	@Override
	public boolean isExcerptExtracted(){
		return excerptExtracted;
	}

	@Override
	public boolean isExcerpted(){
		return excerpt != null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Post o) {
		return o.getDate().compareTo(getDate());
	}

	@Override
	public List<Category> getCategories() {
		return getCategoriesHolder().get("category");
	}

	@Override
	public List<Tag> getTags() {
		return getTagsHolder().get("tag");
	}
}
