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
package org.opoo.press.filter;

import java.util.Collections;
import java.util.List;

import org.opoo.press.Ordered;
import org.opoo.press.Page;
import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.press.SiteFilter;

/**
 * @author Alex Lin
 *
 */
public class MultiSiteFilter implements SiteFilter {
	private List<SiteFilter> filters;
	
	public MultiSiteFilter(List<SiteFilter> filters){
		this.filters = filters;
		if(this.filters != null){
			Collections.sort(filters, Ordered.COMPARATOR);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.filter.SiteFilter#postSetup(org.opoo.press.Site)
	 */
	@Override
	public void postSetup(Site site) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postSetup(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.filter.SiteFilter#postRead(org.opoo.press.Site)
	 */
	@Override
	public void postRead(Site site) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postRead(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.filter.SiteFilter#postGenerate(org.opoo.press.Site)
	 */
	@Override
	public void postGenerate(Site site) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postGenerate(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.filter.SiteFilter#postRender(org.opoo.press.Site)
	 */
	@Override
	@Deprecated
	public void postRender(Site site) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postRender(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.filter.SiteFilter#postWrite(org.opoo.press.Site)
	 */
	@Override
	public void postWrite(Site site) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postWrite(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.filter.SiteFilter#getOrder()
	 */
	@Override
	public int getOrder() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postConvertPost(org.opoo.press.Site, org.opoo.press.Post)
	 */
	@Override
	public void postConvertPost(Site site, Post post) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postConvertPost(site, post);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postConvertPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postConvertPage(Site site, Page page) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postConvertPage(site, page);
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postRenderPost(org.opoo.press.Site, org.opoo.press.Post)
	 */
	@Override
	public void postRenderPost(Site site, Post post) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postRenderPost(site, post);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postRenderPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postRenderPage(Site site, Page page) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postRenderPage(site, page);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postRenderAllPosts(org.opoo.press.Site)
	 */
	@Override
	public void postRenderAllPosts(Site site) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postRenderAllPosts(site);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteFilter#postRenderAllPages(org.opoo.press.Site)
	 */
	@Override
	public void postRenderAllPages(Site site) {
		if(this.filters != null){
			for(SiteFilter f : filters){
				f.postRenderAllPages(site);
			}
		}
	}
}
