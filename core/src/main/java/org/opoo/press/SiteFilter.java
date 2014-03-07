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
package org.opoo.press;


/**
 * @author Alex Lin
 *
 */
public interface SiteFilter extends Ordered{
	
	void postSetup(Site site);
	
	void postRead(Site site);
	
	void postGenerate(Site site);
	
	void postConvertPost(Site site, Post post);
	
	void postConvertPage(Site site, Page page);
	
	void postRenderPost(Site site, Post post);
	
	void postRenderPage(Site site, Page page);
	
	void postRenderAllPosts(Site site);
	
	void postRenderAllPages(Site site);
	
	/**
	 * @deprecated using {@link #postRenderAllPosts(Site)} and {@link #postRenderAllPages(Site)}
	 * @param site
	 */
	void postRender(Site site);
	
	void postWrite(Site site);
}
