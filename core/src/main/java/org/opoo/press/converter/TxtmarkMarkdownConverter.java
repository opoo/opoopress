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
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Converter;
import org.opoo.press.Site;
import org.opoo.press.source.Source;
import org.opoo.press.util.MapUtils;

import com.github.rjeschke.txtmark.BlockEmitter;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

/**
 * A txtmark implemented converter.
 * @author Alex Lin
 *
 */
public class TxtmarkMarkdownConverter implements Converter {
	private Configuration config;

	public TxtmarkMarkdownConverter(Site site) {
		super();
		Map<String, Object> map = site.getConfig();
		boolean useSyntaxHighlighterCompress = MapUtils.get(map, "syntax_highlighter_compress", true);
		if(useSyntaxHighlighterCompress){
			config = Configuration.builder()
				.setCodeBlockEmitter(new BlockEmitterImpl())
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
	
	private static class BlockEmitterImpl implements BlockEmitter{
		/* (non-Javadoc)
		 * @see com.github.rjeschke.txtmark.BlockEmitter#emitBlock(java.lang.StringBuilder, java.util.List, java.lang.String)
		 */
		@Override
		public void emitBlock(StringBuilder out, List<String> lines, String meta) {
			out.append("<pre");
			if(StringUtils.isNotBlank(meta)){
				out.append(" class='brush:" + meta + "'");
			}
			out.append(">");
			for(String line: lines)
            {
                for(int i = 0; i < line.length(); i++)
                {
                    final char c;
                    switch(c = line.charAt(i))
                    {
                    case '&':
                        out.append("&amp;");
                        break;
                    case '<':
                        out.append("&lt;");
                        break;
                    case '>':
                        out.append("&gt;");
                        break;
                    default:
                        out.append(c);
                        break;
                    }
                }
                out.append('\n');
            }
			out.append("</pre>");
		}
	}
}
