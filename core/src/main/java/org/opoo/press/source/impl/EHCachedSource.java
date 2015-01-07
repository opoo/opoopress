/*
 * Copyright 2014 Alex Lin.
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

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;

import java.util.Map;

/**
 * @author Alex Lin
 */
public class EHCachedSource implements Source {

    private final Map<String, Object> frontMatter;
    private final Cache sourceContentCache;
    private final SourceEntry sourceEntry;


    EHCachedSource(SourceEntry sourceEntry, Map<String, Object> frontMatter, String content, Cache sourceContentCache) {
        super();
        this.frontMatter = frontMatter;
        this.sourceEntry = sourceEntry;
        this.sourceContentCache = sourceContentCache;

        String key = sourceEntry.getFile().getAbsolutePath();
        sourceContentCache.put(new Element(key, content));
    }

    /* (non-Javadoc)
     * @see org.opoo.press.source.Source#getSourceEntry()
     */
    @Override
    public SourceEntry getSourceEntry() {
        return sourceEntry;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.source.Source#getFrontMatter()
     */
    @Override
    public Map<String, Object> getMeta() {
        return frontMatter;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.source.Source#getContent()
     */
    @Override
    public String getContent() {
        String key = sourceEntry.getFile().getAbsolutePath();
        Element element = sourceContentCache.get(key);
        if(element != null){
            return (String) element.getObjectValue();
        }
        return null;
    }
}
