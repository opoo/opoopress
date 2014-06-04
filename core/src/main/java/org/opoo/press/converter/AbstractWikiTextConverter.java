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

import java.io.StringWriter;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.opoo.press.Converter;
import org.opoo.press.source.Source;

/**
 * WikiText converter.
 * 
 * *.textile, *.tracwiki, *.mediawiki, *.twiki, *.confluence
 * 
 * @see http://wiki.eclipse.org/Mylyn/WikiText
 * @see http://help.eclipse.org/juno/topic/org.eclipse.mylyn.wikitext.help.ui/help/devguide/Using-The-WikiText-Parser.html
 * @author Alex Lin
 *
 */
public abstract class AbstractWikiTextConverter implements Converter{
	/* (non-Javadoc)
	 * @see org.opoo.press.Converter#convert(java.lang.String)
	 */
	@Override
	public String convert(String content) {
		MarkupParser parser = getMarkupParser();
		
		StringWriter writer = new StringWriter();
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
		// avoid the <html> and <body> tags 
		builder.setEmitAsDocument(false);

		parser.setBuilder(builder);
		parser.parse(content);
		parser.setBuilder(null);
		return writer.toString();
	}
	
//	public String convert2(String content){
//		MarkupParser parser = getMarkupParser();
//		StringWriter out = new StringWriter();
//		parser.setBuilder(new HtmlDocumentBuilder(out));
//		parser.parse(content, false);
//		parser.setBuilder(null);
//		return out.toString();
//	}
	
	/**
	 * Return the MarkupParser instance.
	 * @return MarkupParser for this converter.
	 */
	protected abstract MarkupParser getMarkupParser();
	

	/* (non-Javadoc)
	 * @see org.opoo.press.Converter#getOutputFileExtension(org.opoo.press.source.Source)
	 */
	@Override
	public String getOutputFileExtension(Source src) {
		return ".html";
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Converter#matches(org.opoo.press.source.Source)
	 */
	@Override
	public boolean matches(Source src) {
		String name = src.getSourceEntry().getName().toLowerCase();
		String[] extensions = getInputFileExtensions();
		if(extensions.length == 1){
			return FilenameUtils.isExtension(name, extensions[0]);
		}
		
		return FilenameUtils.isExtension(name, extensions);
	}
	
	protected abstract String[] getInputFileExtensions();
}
