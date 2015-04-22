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
package org.opoo.press;

/**
 * @author Alex Lin
 * @since 1.2
 */
public class ProcessorAdapter implements Processor {

	public static final int DEFAULT_ORDER = 100;

	@Override
	public void preRender(Site site, Page page) {

	}
	
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

	@Override
	public void postRead(Site site, Page page) {

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

	@Override
	public void preRender(Site site) {
	}

	/* (non-Javadoc)
         * @see org.opoo.press.Processor#postConvertPage(org.opoo.press.Site, org.opoo.press.Page)
         */
	@Override
	public void postConvert(Site site, Page page) {
	}

	@Override
	public void postConvert(Site site) {
	}


	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRenderPage(org.opoo.press.Site, org.opoo.press.Page)
	 */
	@Override
	public void postRender(Site site, Page page) {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#postRenderAllPages(org.opoo.press.Site)
	 */
	@Override
	public void postRender(Site site) {
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
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Processor#afterBuildTheme(org.opoo.press.Theme)
	 */
	@Override
	public void afterBuildTheme(Theme theme) {
	}
}
