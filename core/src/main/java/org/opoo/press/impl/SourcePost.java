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
package org.opoo.press.impl;

import org.apache.commons.lang.StringUtils;
import org.opoo.press.Category;
import org.opoo.press.Converter;
import org.opoo.press.Highlighter;
import org.opoo.press.Post;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.Source;
import org.opoo.press.Tag;

import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class SourcePost extends AbstractSourcePage implements Post, Comparable<Post> {
    private final boolean excerptable;

    private String id;
    private boolean excerpted = false;
    private boolean excerptExtracted = false;

    public SourcePost(Site site, Source source) {
        super(site, source, null);

        if (getDate() == null || get("dateFormatted") == null) {
            throw new IllegalArgumentException("Date is required in post YAML front-matter header: "
                    + getSource().getOrigin());
        }

        Map<String, Object> frontMatter = getSource().getMeta();

        id = (String) frontMatter.get("id");

        excerptable = isExcerptable(site);
        initExcerpt(site, frontMatter);
    }

    private static boolean isExcerptable(Site site) {
        Boolean bool = site.get("excerptable");
        return (bool == null || bool);
    }

    private void initExcerpt(Site site, Map<String, Object> frontMatter) {
        if (!excerptable) {
            log.debug("Skip process excerpt.");
            return;
        }

        String excerpt = (String) frontMatter.get("excerpt");
        if (StringUtils.isNotBlank(excerpt)) {
            excerpted = true;
            setExcerpt(excerpt);
            return;
        }

        String content = getContent();
        if (StringUtils.isBlank(content)) {
            log.debug("Content is empty, can not extract excerpt.");
            excerpt = "";
            setExcerpt(excerpt);
            return;
        }

        //default "<!--more-->";
        String excerptSeparator = site.getConfig().get("excerpt_separator", DEFAULT_EXCERPT_SEPARATOR);
        int index = content.indexOf(excerptSeparator);
        if (index > 0) {
            excerpt = content.substring(0, index);
            if (StringUtils.isNotBlank(excerpt)) {
                excerptExtracted = true;
                excerpted = true;
                setExcerpt(excerpt);
                return;
            }
        }

        excerptExtracted = true;
        excerpt = content;
        setExcerpt(excerpt);
    }

    @Override
    public void convert(Converter converter) {
        super.convert(converter);
        if (excerptable && converter != null) {
            setExcerpt(converter.convert(getExcerpt()));
        }
    }

    @Override
    public void render(Renderer renderer, Highlighter highlighter, Map<String, Object> rootMap) {
        if (isSkipRender()) {
            return;
        }

        super.render(renderer, highlighter, rootMap);

        if (excerptable) {
            String excerpt = getExcerpt();
            if (renderer.isRenderRequired(this, excerpt)) {
                log.debug("Rendering excerpt.");
                setExcerpt(renderer.renderContent(excerpt, rootMap));

                if (log.isTraceEnabled()) {
                    log.trace("Excerpt rendered[{}]: {}", getUrl(), excerpt);
                }
            }
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id != null ? id : getUrl();
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the excerpt
     */
    @Override
    public String getExcerpt() {
        return contentHolder.getExcerpt();
    }

    public void setExcerpt(String excerpt) {
        contentHolder.setExcerpt(excerpt);
    }

    public boolean isExcerptExtracted() {
        return excerptExtracted;
    }

    @Override
    public boolean isExcerpted() {
        return excerpted;
    }

    public boolean isExcerptable() {
        return excerptable;
    }

    @Override
    public int compareTo(Post o) {
        return o.getDate().compareTo(getDate());
    }

    @Override
    public List<Category> getCategories() {
        return getCategoriesHolder().get("category");
    }

    @Override
    public List<Tag> getTags() {
        return getTagsHolder().get("tag");
    }
}
