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
package org.opoo.press.source;

import org.apache.commons.io.IOUtils;
import org.opoo.press.Source;
import org.opoo.press.SourceEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class FileCachedSource implements Source {

	private final Map<String, Object> frontMatter;
	private final File contentFile;
	private final SourceEntry sourceEntry;
	
	FileCachedSource(SourceEntry sourceEntry, Map<String, Object> frontMatter, String content) {
		super();
		this.frontMatter = frontMatter;
		this.sourceEntry = sourceEntry;
		
		FileOutputStream stream = null;
		try {
			this.contentFile = File.createTempFile("CachedSource", ".bin");
			stream = new FileOutputStream(contentFile);
			IOUtils.write(content, stream, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(stream);
		}
	}
	
	FileCachedSource(SourceEntry sourceEntry, Map<String, Object> frontMatter, List<String> contentLines) {
		super();
		this.frontMatter = frontMatter;
		this.sourceEntry = sourceEntry;
		FileOutputStream stream = null;
		try {
			this.contentFile = File.createTempFile("PageContent", ".bin");
			stream = new FileOutputStream(contentFile);
			IOUtils.writeLines(contentLines, null, stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(stream);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Source#getSourceEntry()
	 */
	@Override
	public SourceEntry getSourceEntry() {
		return sourceEntry;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Source#getFrontMatter()
	 */
	@Override
	public Map<String, Object> getMeta() {
		return frontMatter;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Source#getContent()
	 */
	@Override
	public String getContent() {
		FileReader reader = null;
		try {
			reader = new FileReader(this.contentFile);
			return IOUtils.toString(reader);
		}catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(reader);
		}
	}
}
