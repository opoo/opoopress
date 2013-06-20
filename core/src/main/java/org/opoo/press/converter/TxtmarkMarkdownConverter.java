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

import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Converter;
import org.opoo.press.Initializable;
import org.opoo.press.Site;
import org.opoo.press.source.Source;
import org.opoo.press.util.MapUtils;
import org.opoo.press.util.Utils;

import com.github.rjeschke.txtmark.BlockEmitter;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

/**
 * A txtmark implemented converter.
 * @author Alex Lin
 *
 */
public class TxtmarkMarkdownConverter implements Converter, Initializable, HighlighterSupportConverter {
	private static final Log log = LogFactory.getLog(TxtmarkMarkdownConverter.class);
	private Configuration config;
	private Highlighter highlighter;

	public TxtmarkMarkdownConverter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Initializable#initialize(org.opoo.press.Site)
	 */
	@Override
	public void initialize(Site site) {
		Map<String, Object> map = site.getConfig();
		String highlighterClassName = MapUtils.get(map, "highlighter");
		if(highlighterClassName == null){
			log.warn("This converter might be need a Highlighter.");
		}else{
			highlighter = (Highlighter) Utils.newInstance(highlighterClassName, site);
			config = Configuration.builder()
					.setCodeBlockEmitter(new BlockEmitterImpl(highlighter))
					.forceExtentedProfile()
					.build();
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 100;
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Converter#matches(org.opoo.joctopress.source.Source)
	 */
	@Override
	public boolean matches(Source src) {
		String name = src.getSourceEntry().getName().toLowerCase();
		return FilenameUtils.isExtension(name, new String[]{"markdown", "md"});
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Converter#convert(java.lang.String)
	 */
	@Override
	public String convert(String content) {
		if(config != null){
			return Processor.process(content, config);
		}else{
			return Processor.process(content);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Converter#getOutputFileExtension(org.opoo.joctopress.source.Source)
	 */
	@Override
	public String getOutputFileExtension(Source src) {
		return ".html";
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.converter.HighlighterSupportConverter#getHighlighter()
	 */
	@Override
	public Highlighter getHighlighter() {
		return highlighter;
	}
	
	/**
	 * A BlockEmitter to process highlight code block. 
	 */
	private static class BlockEmitterImpl implements BlockEmitter{
		private Highlighter highlighter;
		public BlockEmitterImpl(Highlighter highlighter) {
			this.highlighter = highlighter;
		}

		/* (non-Javadoc)
		 * @see com.github.rjeschke.txtmark.BlockEmitter#emitBlock(java.lang.StringBuilder, java.util.List, java.lang.String)
		 */
		@Override
		public void emitBlock(StringBuilder out, List<String> lines, String meta) {
			highlighter.highlight(out, lines, meta);
		}
	}
}
