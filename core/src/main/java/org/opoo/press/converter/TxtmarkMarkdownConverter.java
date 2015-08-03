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

import com.github.rjeschke.txtmark.BlockEmitter;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import org.apache.commons.io.FilenameUtils;
import org.opoo.press.Converter;
import org.opoo.press.Highlighter;
import org.opoo.press.Site;
import org.opoo.press.SiteAware;
import org.opoo.press.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A <code>txtmark</code> implemented converter.
 *
 * @author Alex Lin
 */
public class TxtmarkMarkdownConverter implements Converter, SiteAware {
    private static final Logger log = LoggerFactory.getLogger(TxtmarkMarkdownConverter.class);
    private Configuration config;
    private Highlighter highlighter;

    public TxtmarkMarkdownConverter() {
        super();
    }

    @Override
    public void setSite(Site site) {
        this.highlighter = site.getFactory().getHighlighter();
        if (this.highlighter == null) {
            log.warn("This converter might be need a Highlighter.");
        } else {
            config = Configuration.builder()
                    .setCodeBlockEmitter(new BlockEmitterImpl(highlighter))
                    .forceExtentedProfile()
                    .build();
        }
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Ordered#getOrder()
     */
    @Override
    public int getOrder() {
        return 100;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Converter#matches(org.opoo.press.Source)
     */
    @Override
    public boolean matches(Source src) {
        String name = src.getOrigin().getName().toLowerCase();
        if (FilenameUtils.isExtension(name, new String[]{"markdown", "md"})) {
            return true;
        }
        if ("post".equals(src.getMeta().get("layout")) && FilenameUtils.isExtension(name, "txt")) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Converter#convert(java.lang.String)
     */
    @Override
    public String convert(String content) {
        if (config != null) {
            return Processor.process(content, config);
        } else {
            return Processor.process(content);
        }
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Converter#getOutputFileExtension(org.opoo.press.Source)
     */
    @Override
    public String getOutputFileExtension(Source src) {
        return ".html";
    }

    /**
     * A BlockEmitter to process highlight code block.
     */
    private static class BlockEmitterImpl implements BlockEmitter {
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
