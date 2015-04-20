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

import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.Site;
import org.opoo.press.Source;

/**
 * @author Alex Lin
 *
 */
public class SourcePage extends AbstractSourcePage implements Page{

	/**
	 * Construct a page instance.
	 * @param site
	 * @param source
	 */
	public SourcePage(Site site, Source source) {
		super(site, source, null);
	}

	/**
	 * @param site
	 * @param source
	 * @param pager
	 */
	public SourcePage(Site site, Source source, Pager pager) {
		super(site, source, pager);
	}

}
