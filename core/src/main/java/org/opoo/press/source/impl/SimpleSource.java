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
package org.opoo.press.source.impl;

import java.util.Map;

import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;

/**
 * @author Alex Lin
 *
 */
public class SimpleSource implements Source {
	private final Map<String, Object> frontMatter;
	private final String content;
	private final SourceEntry sourceEntry;
	
	/**
	 * @param sourceEntry
	 * @param frontMatter
	 * @param content
	 */
	public SimpleSource(SourceEntry sourceEntry,
			Map<String, Object> frontMatter, String content) {
		super();
		this.sourceEntry = sourceEntry;
		this.frontMatter = frontMatter;
		this.content = content;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.source.Source#getSourceEntry()
	 */
	@Override
	public SourceEntry getSourceEntry() {
		return sourceEntry;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.source.Source#getMeta()
	 */
	@Override
	public Map<String, Object> getMeta() {
		return frontMatter;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.source.Source#getContent()
	 */
	@Override
	public String getContent() {
		return content;
	}
}
