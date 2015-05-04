/*
 * Copyright 2013-2015 Alex Lin.
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

import org.opoo.press.NoFrontMatterException;
import org.opoo.press.Source;
import org.opoo.press.SourceEntry;
import org.opoo.press.SourceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class CachedSourceParserWrapper implements SourceParser{
    private static final Logger log = LoggerFactory.getLogger(CachedSourceParserWrapper.class);
    private final SourceParser sourceParser;
    private final Cache<String, Source> sourceCache;
    private final Cache<String, SourceEntry> staticFileCache;

    public CachedSourceParserWrapper(SourceParser sourceParser,
                                     Cache<String, Source> sourceCache,
                                     Cache<String, SourceEntry> staticFileCache) {
        this.sourceParser = sourceParser;
        this.sourceCache = sourceCache;
        this.staticFileCache = staticFileCache;
    }

    @Override
    public Source parse(SourceEntry sourceEntry) throws NoFrontMatterException {
        String cacheKey = sourceEntry.getFile().getAbsolutePath();

        SourceEntry se = staticFileCache.get(cacheKey);
        if (se != null) {
            if (se.equals(sourceEntry)) {
                throw new NoFrontMatterException(sourceEntry);
            } else {
                staticFileCache.remove(cacheKey);
            }
        }

        Source source = sourceCache.get(cacheKey);
        if (source != null) {
            SourceEntry entry = source.getSourceEntry();
            if (entry != null && entry.equals(sourceEntry)) {
                log.debug("Find up-to-date source in cache: {}", cacheKey);
                return new CachedSource(cacheKey);
            } else {
                sourceCache.remove(cacheKey);
                log.debug("Clear source cache: ", cacheKey);
            }
        }

        try {
            Source src = sourceParser.parse(sourceEntry);
            sourceCache.put(cacheKey, src);

            log.debug("Put source into cache: {}", cacheKey);
            return new CachedSource(cacheKey);
        } catch (NoFrontMatterException e) {
            staticFileCache.put(cacheKey, sourceEntry);
            throw e;
        }
    }

    private class CachedSource implements Source {
        private final String cacheKey;

        CachedSource(String cacheKey) {
            this.cacheKey = cacheKey;
        }

        @Override
        public SourceEntry getSourceEntry() {
            return sourceCache.get(cacheKey).getSourceEntry();
        }

        @Override
        public Map<String, Object> getMeta() {
            return sourceCache.get(cacheKey).getMeta();
        }

        @Override
        public String getContent() {
            return sourceCache.get(cacheKey).getContent();
        }
    }
}
