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
package org.opoo.press.impl;

import com.google.common.base.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Base;
import org.opoo.press.Category;
import org.opoo.press.Converter;
import org.opoo.press.Excerptable;
import org.opoo.press.Highlighter;
import org.opoo.press.ListHolder;
import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.Source;
import org.opoo.press.Tag;
import org.opoo.util.MapUtils;
import org.opoo.util.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Alex Lin
 */
public class SimplePage implements Page {
    private static final Logger log = LoggerFactory.getLogger(SimplePage.class);

    private String title;
    private String layout;
    private String permalink;
    private boolean published = true;
    private Date date;
    private Date updated;
    private Map<String, Object> data = new HashMap<String, Object>();
    private Pager pager;
    private Page next;
    private Page previous;
    private ListHolder<Category> categoriesHolder = new ListHolderImpl<Category>();
    private ListHolder<Tag> tagsHolder = new ListHolderImpl<Tag>();

    private String outputFileExtension = ".html";
    private String originalUrl;
    private String encodedUrl;
    private String decodedUrl;

    private boolean skipRender;
    private boolean urlEncode;
    private boolean urlDecode;

    protected final Site site;
    protected final ContentHolder contentHolder;

    public SimplePage(Site site) {
        this.site = site;
        if(site instanceof SiteImpl && ((SiteImpl) site).pageCache != null){
            contentHolder = new CachedContentHolder(((SiteImpl) site).pageCache);
        }else {
            contentHolder = new SimpleContentHolder();
        }
    }

    public SimplePage(Site site, Page page, Pager pager) {
        this(site);
        this.setTitle(page.getTitle());
        this.setContent(page.getContent());
        this.setDate(page.getDate());
        this.setLayout(page.getLayout());
        this.setCategoriesHolder(page.getCategoriesHolder());
        this.setPager(pager != null ? pager : page.getPager());
        this.setNext(page.getNext());
        this.setPermalink(page.getPermalink());
        this.setPrevious(page.getPrevious());
        this.setPublished(page.isPublished());
        this.setSource(page.getSource());
        this.setTagsHolder(page.getTagsHolder());
        this.setUpdated(page.getUpdated());
        this.setOutputFileExtension(page.getOutputFileExtension());
        this.setUrl(page.getOriginalUrl());

        if (page instanceof SimplePage) {
            SimplePage sp = (SimplePage) page;
            this.urlDecode = sp.isUrlDecode();
            this.urlEncode = sp.isUrlEncode();
            //this.data = new LinkedHashMap<String, Object>(sp.data);
            MapUtils.copy(this.data, sp.data);
        }
    }

    /**
     * @param current
     * @param targetPageNumber
     * @return the page object by the specified page number.
     */
    public static Page getPage(Page current, int targetPageNumber) {
        if (current == null) {
            log.warn("Current page is null, cannot found target page for page number " + targetPageNumber);
            return null;
        }
        Pager pa = current.getPager();
        if (pa == null) {
            log.warn("Current page is not one of a pagination page.");
            return null;
        }
        int currentPageNumber = pa.getPageNumber();
        if (currentPageNumber == targetPageNumber) {
            return current;
        } else if (targetPageNumber > currentPageNumber) {
            return getPage(pa.getNext(), targetPageNumber);
        } else {
            return getPage(pa.getPrevious(), targetPageNumber);
        }
    }

    @Override
    public Source getSource() {
        return contentHolder.getSource();
    }

    public void setSource(Source source) {
        contentHolder.setSource(source);
    }

    @Override
    public String getContent() {
        return contentHolder.getContent();
    }

    public void setContent(String content) {
        contentHolder.setContent(content);
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getUrl() {
        return getEncodedUrl();
    }

    @Override
    public void setUrl(String url) {
        if (!Objects.equal(url, this.originalUrl)) {
            this.originalUrl = url;
            this.encodedUrl = null;
            this.decodedUrl = null;
        }
    }

    @Override
    public String getDecodedUrl() {
        if (originalUrl == null) {
            return null;
        }
        if (decodedUrl == null) {
            decodedUrl = urlDecode ? URLUtils.decodeURL(originalUrl) : originalUrl;
        }
        return decodedUrl;
    }

    @Override
    public String getOriginalUrl() {
        return originalUrl;
    }

    @Override
    public String getEncodedUrl() {
        if (originalUrl == null) {
            return null;
        }
        if (encodedUrl == null) {
            encodedUrl = urlEncode ? URLUtils.encodeURL(originalUrl) : originalUrl;
        }
        return encodedUrl;
    }

    @Override
    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    @Override
    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    @Override
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public <T> T get(String name) {
        if ("convertedContent".equals(name)) {
            return (T) "";
        }
        if (data.containsKey(name)) {
            return (T) data.get(name);
        }
        Source source = getSource();
        if (source != null) {
            return (T) source.getMeta().get(name);
        }
        return null;
    }

    @Override
    public <T> void set(String name, T value) {
        MapUtils.put(data, name, value);
    }

    public boolean isUrlEncode() {
        return urlEncode;
    }

    @Override
    public void setUrlEncode(boolean urlEncode) {
        this.urlEncode = urlEncode;
        this.encodedUrl = null;
    }

    public boolean isUrlDecode() {
        return urlDecode;
    }

    @Override
    public void setUrlDecode(boolean urlDecode) {
        this.urlDecode = urlDecode;
        this.decodedUrl = null;
    }

    public boolean isSkipRender() {
        return skipRender;
    }

    public void setSkipRender(boolean skipRender) {
        this.skipRender = skipRender;
    }

    @Override
    public void convert(Converter converter) {
        if (converter != null) {
            this.setOutputFileExtension(converter.getOutputFileExtension(getSource()));
            setContent(converter.convert(getContent()));
        }
    }

    @Override
    public void render(Renderer renderer, Highlighter highlighter, Map<String, Object> rootMap) {
        if (skipRender || renderer == null) {
            return;
        }

        if (StringUtils.isBlank(getContent())) {
            log.warn("Empty content, skip render: {}", getUrl());
            return;
        }

        rootMap = new HashMap<String, Object>(rootMap);
        mergeRootMap(highlighter, rootMap);
        setContent(renderer.render(this, rootMap));
    }

    protected void mergeRootMap(Highlighter highlighter, Map<String, Object> rootMap) {
//        String canonical = getSite().buildCanonical(getUrl());
//        rootMap.put("canonical", canonical);
        mergeHighlighterParam(highlighter, rootMap);

        rootMap.put("page", this);
        if (getPager() != null) {
            rootMap.put("paginator", getPager());
        }
    }

    private void mergeHighlighterParam(Highlighter highlighter, Map<String, Object> rootMap) {
        if (highlighter != null && ".html".equals(getOutputFileExtension())
                && containsHighlightCodeBlock(highlighter)) {
            log.debug("The content contains highlight code block.");
            rootMap.put("highlighter", highlighter.getHighlighterName());
        }
    }

    /**
     * @param highlighter the Highlighter
     */
    protected boolean containsHighlightCodeBlock(Highlighter highlighter) {
        boolean contains = highlighter.containsHighlightCodeBlock(getContent());
        if (contains) {
            return true;
        }

        Pager pager = getPager();
        if (pager != null) {
            //check pager.items excerpt
            List<?> items = pager.getItems();
            if (items != null) {
                for (Object p : items) {
                    if (p instanceof Excerptable) {
                        String excerpt = ((Excerptable) p).getExcerpt();
                        if (highlighter.containsHighlightCodeBlock(excerpt)) {
                            if (log.isDebugEnabled() && p instanceof Base) {
                                log.debug("Found highlighter code block in post excerpt: " + ((Base) p).getUrl());
                            }
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void write(File dest) {
        File file = getOutputFile(dest);
        try {
            file.getParentFile().mkdirs();

            log.debug("Writing file to {} [{}]", file, getUrl());
            FileUtils.write(file, getContent(), "UTF-8");
        } catch (IOException e) {
            log.error("Write file error: {}", file, e);
            throw new RuntimeException(e);
        }
    }

    public File getOutputFile(File dest) {
        String url = getDecodedUrl();
        if (url.endsWith("/")) {
            url += "index" + getOutputFileExtension();
        }
        return new File(dest, url);
    }

    @Override
    public String getOutputFileExtension() {
        return outputFileExtension;
    }

    @Override
    public void setOutputFileExtension(String outputFileExtension) {
        this.outputFileExtension = outputFileExtension;
    }

    @Override
    public Pager getPager() {
        return pager;
    }

    @Override
    public void setPager(Pager pager) {
        this.pager = pager;
    }

    @Override
    public Page getNext() {
        return next;
    }

    public void setNext(Page next) {
        this.next = next;
    }

    @Override
    public Page getPrevious() {
        return previous;
    }

    public void setPrevious(Page previous) {
        this.previous = previous;
    }

    @Override
    public ListHolder<Tag> getTagsHolder() {
        return tagsHolder;
    }

    public void setTagsHolder(ListHolder<Tag> tagsHolder) {
        this.tagsHolder = tagsHolder;
    }

    @Override
    public ListHolder<Category> getCategoriesHolder() {
        return categoriesHolder;
    }

    public void setCategoriesHolder(ListHolder<Category> categoriesHolder) {
        this.categoriesHolder = categoriesHolder;
    }

    @Override
    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    /**
     * 查找指定页码的 page 对象。
     *
     * @param targetPageNumber
     * @return the target page
     */
    public Page getPage(int targetPageNumber) {
        return getPage(this, targetPageNumber);
    }

    interface ContentHolder{
        String getContent();
        void setContent(String content);
        Source getSource();
        void setSource(Source source);
        String getExcerpt();
        void setExcerpt(String excerpt);
    }

    static class SimpleContentHolder implements ContentHolder{
        private String content;
        private Source source;
        private String excerpt;

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public Source getSource() {
            return source;
        }

        @Override
        public void setSource(Source source) {
            this.source = source;
        }

        @Override
        public String getExcerpt() {
            return excerpt;
        }

        @Override
        public void setExcerpt(String excerpt) {
            this.excerpt = excerpt;
        }
    }

    static class CachedContentHolder implements ContentHolder{
        private static final AtomicLong ID_GEN = new AtomicLong(0);

        private final Cache<String, Object> cache;

        private final String contentKey;
        private String sourceKey;
        private final String excerptKey;

        CachedContentHolder(Cache<String, Object> cache) {
            this.cache = cache;
            contentKey = String.valueOf(ID_GEN.incrementAndGet());
            excerptKey = String.valueOf(ID_GEN.incrementAndGet());
        }

        @Override
        public String getContent() {
            return (String) cache.get(contentKey);
        }

        @Override
        public void setContent(String content) {
            cache.put(contentKey, content);
        }

        @Override
        public Source getSource() {
            if(sourceKey == null) {
                return null;
            }
            return (Source) cache.get(sourceKey);
        }

        @Override
        public void setSource(Source source) {
            if(sourceKey == null) {
                sourceKey = source.getOrigin().toString();
            }
            cache.put(sourceKey, source);
        }

        @Override
        public String getExcerpt() {
               return (String) cache.get(excerptKey);
        }

        @Override
        public void setExcerpt(String excerpt) {
            cache.put(excerptKey, excerpt);
        }
    }
}
