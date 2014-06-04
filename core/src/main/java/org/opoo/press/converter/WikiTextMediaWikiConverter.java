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
package org.opoo.press.converter;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;

/**
 * WikiText MediaWiki converter. <code>*.mediawiki</code>
 * @author Alex Lin
 */
public class WikiTextMediaWikiConverter extends AbstractWikiTextConverter {
	private MarkupParser parser = new MarkupParser(new MediaWikiLanguage());
	/* (non-Javadoc)
	 * @see org.opoo.press.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 130;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.converter.AbstractWikiTextConverter#getMarkupParser()
	 */
	@Override
	protected MarkupParser getMarkupParser() {
		return parser;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.converter.AbstractWikiTextConverter#getInputFileExtensions()
	 */
	@Override
	protected String[] getInputFileExtensions() {
		return new String[]{"mediawiki"};
	}
}
