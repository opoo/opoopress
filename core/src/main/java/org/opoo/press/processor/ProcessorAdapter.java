/*
 * Copyright 2014 Alex Lin.
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
package org.opoo.press.processor;

import org.opoo.press.Page;
import org.opoo.press.Post;
import org.opoo.press.Processor;
import org.opoo.press.Site;
import org.opoo.press.Theme;

/**
 * @author Alex Lin
 * @since 1.2
 */
public class ProcessorAdapter implements Processor {
	public static final int DEFAULT_ORDER = 100;
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postSetup(org.opoo.press.Site)
	 */
	@Override
	public void postSetup(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRead(org.opoo.press.Site)
	 */
	@Override
	public void postRead(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postGenerate(org.opoo.press.Site)
	 */
	@Override
	public void postGenerate(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postConvertPost(org.opoo.press.Site, org.opoo.press.Post)
	 */
	@Override
	public void postConvertPost(Site site, Post post) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postConvertPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postConvertPage(Site site, Page page) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRenderPost(org.opoo.press.Site, org.opoo.press.Post)
	 */
	@Override
	public void postRenderPost(Site site, Post post) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRenderPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postRenderPage(Site site, Page page) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRenderAllPosts(org.opoo.press.Site)
	 */
	@Override
	public void postRenderAllPosts(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRenderAllPages(org.opoo.press.Site)
	 */
	@Override
	public void postRenderAllPages(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postWrite(org.opoo.press.Site)
	 */
	@Override
	public void postWrite(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postCleanup(org.opoo.press.Site)
	 */
	@Override
	public void postCleanup(Site site) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#beforeBuildTheme(org.opoo.press.Theme)
	 */
	@Override
	public void beforeBuildTheme(Theme theme) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#afterBuildTheme(org.opoo.press.Theme)
	 */
	@Override
	public void afterBuildTheme(Theme theme) {
		// TODO Auto-generated method stub
		
	}
}
