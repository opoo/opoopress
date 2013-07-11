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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.source.NoFrontMatterException;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.opoo.press.source.SourceParser;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Alex Lin
 *
 */
public class SourceParserImpl implements SourceParser {
	private static final Log log = LogFactory.getLog(SourceParserImpl.class);
	private Yaml yaml = new Yaml();
	
	/**
	 * @return the yaml
	 */
	public Yaml getYaml() {
		return yaml;
	}

	/**
	 * @param yaml the yaml to set
	 */
	public void setYaml(Yaml yaml) {
		this.yaml = yaml;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.source.SourceParser#parse(org.opoo.press.source.SourceEntry)
	 */
	@Override
	public Source parse(SourceEntry sourceEntry)	throws NoFrontMatterException {
		List<String> metaLines = new ArrayList<String>();
		List<String> contentLines = new ArrayList<String>();
		InputStream stream = null;
		List<String> currentList = metaLines;
		try {
			stream = new FileInputStream(sourceEntry.getFile());
			LineIterator iterator = IOUtils.lineIterator(stream, "UTF-8");

			String line = iterator.next();
			if(!isFrontMatterStartOrEndLine(line)){
				log.debug("Maybe a static file: " + sourceEntry.getFile());
				throw new NoFrontMatterException(sourceEntry);
			}
			
			boolean hasFrontMatterEndLine = false;
			//process headers
			while(iterator.hasNext()){
				line = iterator.next();
				if(isFrontMatterStartOrEndLine(line)){
					hasFrontMatterEndLine = true;
					currentList = contentLines;
					continue;
				}
				currentList.add(line);
			}
			
			if(!hasFrontMatterEndLine){
				log.debug("Maybe a static file: " + sourceEntry.getFile());
				throw new NoFrontMatterException(sourceEntry);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(stream);
		}
		
		StringWriter metaWriter = new StringWriter();
		StringWriter contentWriter = new StringWriter();
		try {
			IOUtils.writeLines(metaLines, null, metaWriter);
			IOUtils.writeLines(contentLines, null, contentWriter);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(contentWriter);
			IOUtils.closeQuietly(metaWriter);
		}
		
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) yaml.load(metaWriter.toString());
		String content = contentWriter.toString();
		
		return new SimpleSource(sourceEntry, map, content);
	}
	
	private static boolean isFrontMatterStartOrEndLine(String line){
		return Source.TRIPLE_DASHED_LINE.equals(line);
	}
}
