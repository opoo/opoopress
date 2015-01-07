package org.opoo.press.source.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.io.IOUtils;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
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
