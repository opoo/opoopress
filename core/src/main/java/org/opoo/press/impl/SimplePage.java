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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Base;
import org.opoo.press.Category;
import org.opoo.press.Excerptable;
import org.opoo.press.Highlighter;
import org.opoo.press.ListHolder;
import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.Site;
import org.opoo.press.Source;
import org.opoo.press.Tag;
import org.opoo.util.MapUtils;
import org.opoo.util.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class SimplePage implements Page{
    private static final Logger log = LoggerFactory.getLogger(SimplePage.class);

    private final Site site;
    private Source source;
    private String sourceContent;
    private String title;
    private String url;
    private String path;
    private String layout;
    private String permalink;
    private boolean published = true;
    private Date date;
    private Date updated;
    private String dateFormatted;
    private String updatedFormatted;
    private Map<String,Object> data = new HashMap<String, Object>();
    private Pager pager;
    private Page next;
    private Page previous;
    private ListHolder<Category> categoriesHolder;
    private ListHolder<Tag> tagsHolder;
	private String convertedContent;
    private String renderedContent;

    protected boolean renderSkip;
    protected boolean urlEncode;
    protected boolean urlDecode;

    public SimplePage(Site site) {
        this.site = site;
        //this.urlEncode = site.getConfig().get("url_encode", false);
        //this.urlDecode = site.getConfig().get("url_decode", false);
    }

    public SimplePage(Site site, Page page, Pager pager){
        this(site);
        this.setTitle(page.getTitle());
        this.setUrl(page.getUrl());
        this.setSourceContent(page.getSourceContent());
        this.setConvertedContent(page.getConvertedContent());
        this.setRenderedContent(page.getRenderedContent());
        this.setDate(page.getDate());
        this.setLayout(page.getLayout());
        this.setCategoriesHolder(page.getCategoriesHolder());
        this.setPager(pager != null ? pager : page.getPager());
        this.setNext(page.getNext());
        this.setPath(page.getPath());
        this.setPermalink(page.getPermalink());
        this.setPrevious(page.getPrevious());
        this.setPublished(page.isPublished());
        this.setSource(page.getSource());
        this.setTagsHolder(page.getTagsHolder());
        this.setUpdated(page.getUpdated());
        if(page instanceof SimplePage){
            SimplePage sp = (SimplePage)page;
            this.data = new LinkedHashMap<String, Object>(sp.data);
        }
    }

    public Site getSite(){
        return site;
    }

    @Override
    public Source getSource() {
        return source;
    }

    @Override
    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
    }

    @Deprecated
    public String getContent(){
        return getSourceContent();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getLayout() {
        return layout;
    }

    @Override
    public String getPermalink() {
        return permalink;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    @Override
    public String getDateFormatted() {
        return dateFormatted;
    }

    @Override
    public String getUpdatedFormatted() {
        return updatedFormatted;
    }

    @Override
    public Object get(String name) {
        if(data.containsKey(name)){
            return data.get(name);
        }
        if(source != null){
            return source.getMeta().get(name);
        }
        return null;
    }

    @Override
    public void set(String name, Object value) {
        MapUtils.put(data, name, value);
    }

    @Override
    public void convert() {
    }

    public SimplePage encodeUrl(){
        this.urlEncode = true;
        return this;
    }

    public SimplePage decodeUrl(){
        this.urlDecode = true;
        return this;
    }

    public SimplePage skipRender(){
        this.renderSkip = true;
        return this;
    }

    @Override
    public void render(Map<String, Object> rootMap) {
        if(renderSkip){
            return;
        }

        if(StringUtils.isBlank(getConvertedContent())){
            log.warn("Empty converted content, skip render: {}", getUrl());
            return;
        }

        rootMap = new HashMap<String, Object>(rootMap);
        mergeRootMap(rootMap);
        getSite().getRenderer().render(this, rootMap);
    }

    protected void mergeRootMap(Map<String, Object> rootMap) {
//        String canonical = getSite().buildCanonical(getUrl());
//        rootMap.put("canonical", canonical);
        mergeHighlighterParam(rootMap);

        rootMap.put("page", this);
        if(getPager() != null){
            rootMap.put("paginator", getPager());
        }
    }

    /**
     * @param rootMap
     */
    private void mergeHighlighterParam(Map<String, Object> rootMap) {
        Highlighter highlighter = getSite().getFactory().getHighlighter();
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
        boolean contains = highlighter.containsHighlightCodeBlock(getConvertedContent());
        if(contains){
            return true;
        }

        Pager pager = getPager();
        if(pager != null) {
            //check pager.items excerpt
            List<?> items = pager.getItems();
            if (items != null) {
                for (Object p : items) {
                    if (p instanceof Excerptable) {
                        String excerpt = ((Excerptable) p).getExcerpt();
                        if(excerpt == null && p instanceof Page){
                            excerpt = ((Page) p).getConvertedContent();
                        }
                        if (highlighter.containsHighlightCodeBlock(excerpt)) {
                            if(log.isDebugEnabled() && p instanceof Base){
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
            FileUtils.write(file, getRenderedContent(), "UTF-8");
        } catch (IOException e) {
            log.error("Write file error: {}", file, e);
            throw new RuntimeException(e);
        }
    }

    protected File getOutputFile(File dest) {
        String url = getUrlForOutputFile();
        if(url.endsWith("/")){
            url += "index" + getOutputFileExtension();
        }
        return new File(dest, url);
    }

    protected String getUrlForOutputFile(){
        String url = getUrl();
        if(urlDecode){
            url = URLUtils.decodeURL(url);
        }
        return url;
    }

    protected String getOutputFileExtension(){
        return ".html";
    }


    public void setSource(Source source) {
        this.source = source;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        if(url != null && urlEncode){
            url = URLUtils.encodeURL(url);
        }
        this.url = url;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public void setDate(Date date) {
        this.date = date;
        this.dateFormatted = site.formatDate(date);
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
        this.updatedFormatted = site.formatDate(updated);
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

    public void setNext(Page next){
        this.next = next;
    }

    @Override
    public Page getPrevious() {
        return previous;
    }

    public void setPrevious(Page previous){
        this.previous = previous;
    }

    @Override
    public ListHolder<Tag> getTagsHolder() {
        return tagsHolder;
    }

    @Override
    public ListHolder<Category> getCategoriesHolder() {
        return categoriesHolder;
    }

    public void setCategoriesHolder(ListHolder<Category> categoriesHolder) {
        this.categoriesHolder = categoriesHolder;
    }

    public void setTagsHolder(ListHolder<Tag> tagsHolder) {
        this.tagsHolder = tagsHolder;
    }

    @Override
    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Override
    public String getConvertedContent() {
        if(convertedContent == null){
            return getSourceContent();
        }
        return convertedContent;
    }

    public void setConvertedContent(String convertedContent) {
        this.convertedContent = convertedContent;
    }

    @Override
    public String getRenderedContent() {
        if(renderedContent == null){
            return getConvertedContent();
        }
        return renderedContent;
    }

    public void setRenderedContent(String renderedContent) {
        this.renderedContent = renderedContent;
    }
    /**
     *
     * @param current
     * @param targetPageNumber
     * @return the page object by the specified page number.
     */
    public static Page getPage(Page current, int targetPageNumber){
        if(current == null){
            log.warn("Current page is null, cannot found target page for page number " + targetPageNumber);
            return null;
        }
        Pager pa = current.getPager();
        if(pa == null){
            log.warn("Current page is not one of a pagination page.");
            return null;
        }
        int currentPageNumber = pa.getPageNumber();
        if(currentPageNumber == targetPageNumber){
            return current;
        }else if(targetPageNumber > currentPageNumber){
            return getPage(pa.getNext(), targetPageNumber);
        }else{
            return getPage(pa.getPrevious(), targetPageNumber);
        }
    }

    /**
     * 查找指定页码的 page 对象。
     * @param targetPageNumber
     * @return the target page
     */
    public Page getPage(int targetPageNumber){
        return getPage(this, targetPageNumber);
    }
}
