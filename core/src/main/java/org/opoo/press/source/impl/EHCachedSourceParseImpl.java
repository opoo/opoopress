package org.opoo.press.source.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


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
