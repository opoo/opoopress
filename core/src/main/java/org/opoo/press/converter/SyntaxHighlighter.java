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

import org.apache.commons.lang.StringUtils;

/**
 * @author Alex Lin
 *
 */
public class SyntaxHighlighter implements Highlighter {
	public static final String NAME = "SyntaxHighlighter";
	/* (non-Javadoc)
	 * @see org.opoo.press.converter.Highlighter#containsHighlightCodeBlock(java.lang.String)
	 */
	@Override
	public boolean containsHighlightCodeBlock(String content) {
		return StringUtils.contains(content, "<pre class='brush:")
				|| StringUtils.contains(content, "<pre class=\"brush:");
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.converter.Highlighter#getHighlighterName()
	 */
	@Override
	public String getHighlighterName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.converter.Highlighter#highlight(java.lang.StringBuilder, java.util.List, java.lang.String)
	 */
	@Override
	public void highlight(StringBuilder out, List<String> lines, String meta) {
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
