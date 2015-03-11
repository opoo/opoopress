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
import org.apache.commons.io.LineIterator;
import org.opoo.press.NoFrontMatterException;
import org.opoo.press.Source;
import org.opoo.press.SourceEntry;
import org.opoo.press.SourceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public class SourceParserImpl implements SourceParser {
	private static final Logger log = LoggerFactory.getLogger(SourceParserImpl.class);
	private Yaml yaml = new Yaml();
	
	/* (non-Javadoc)
	 * @see org.opoo.press.SourceParser#parse(org.opoo.press.SourceEntry)
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

			if(!iterator.hasNext()){
				throw new RuntimeException("File not content: " + sourceEntry.getFile());
			}
			
			String line = iterator.next();
			if(!isFrontMatterStartLine(line, sourceEntry)){
				log.debug("Maybe a static file: " + sourceEntry.getFile());
				throw new NoFrontMatterException(sourceEntry);
			}
			
			boolean hasFrontMatterEndLine = false;
			//process headers
			while(iterator.hasNext()){
				line = iterator.next();
				if(isFrontMatterEndLine(line)){
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
	
	private static boolean isFrontMatterStartLine(String line, SourceEntry sourceEntry){
		if( Source.TRIPLE_DASHED_LINE.equals(line)){
			return true;
		}
		if(line.length() == 4){
			if(65279 == line.charAt(0) && Source.TRIPLE_DASHED_LINE.equals(line.substring(1))){
				log.debug("UTF-8 with BOM file: " + sourceEntry.getFile());
				return true; 
			}
		}
		return false;
	}
	private static boolean isFrontMatterEndLine(String line){
		return Source.TRIPLE_DASHED_LINE.equals(line);
	}
}
