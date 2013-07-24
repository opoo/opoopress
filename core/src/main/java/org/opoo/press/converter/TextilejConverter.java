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

import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.builder.HtmlDocumentBuilder;
import net.java.textilej.parser.markup.textile.TextileDialect;

import org.apache.commons.io.FilenameUtils;
import org.opoo.press.Converter;
import org.opoo.press.source.Source;

/**
 * @author Alex Lin
 *
 */
public class TextilejConverter implements Converter {
	private MarkupParser parser = new MarkupParser(new TextileDialect());
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 120;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Converter#convert(java.lang.String)
	 */
	@Override
	public String convert(String content) {
		StringWriter writer = new StringWriter();
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
		builder.setEmitAsDocument(false);
		parser.setBuilder(builder);
		
		parser.parse(content);
		return writer.toString();
	}

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
		if(FilenameUtils.isExtension(name, "textile")){
			return true;
		}
		return false;
	}
}
