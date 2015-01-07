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
import net.sf.ehcache.CacheManager;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * EHCached source parser.
 *
 * @author Alex Lin
 */
public class EHCachedSourceParseImpl extends SourceParserImpl{
    private static final Logger log = LoggerFactory.getLogger(EHCachedSourceParseImpl.class);
    private final Cache sourceContentCache;


    public EHCachedSourceParseImpl(Cache sourceContentCache) {
        this.sourceContentCache = sourceContentCache;
        log.info("Using source parser: {}", EHCachedSourceParseImpl.class.getName());
    }

    public EHCachedSourceParseImpl(CacheManager cacheManager){
        this(cacheManager.getCache("sourceContentCache"));
    }

    @Override
    protected Source createSource(SourceEntry sourceEntry, Map<String, Object> map, String content) {
        return new EHCachedSource(sourceEntry, map, content, sourceContentCache);
    }
}
