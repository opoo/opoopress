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


import org.opoo.press.Base;
import org.opoo.press.Converter;
import org.opoo.press.Convertible;
import org.opoo.press.Highlighter;
import org.opoo.press.Site;
import org.opoo.press.Source;
import org.opoo.press.SourceEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Lin
 */
public abstract class AbstractBase extends BasicBase implements Base, Convertible {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private DateFormat f1 = new SimpleDateFormat(DATE_FORMAT_PATTERN_1);
    private DateFormat f2 = new SimpleDateFormat(DATE_FORMAT_PATTERN_1);

    private String outputFileExtension;
    private Converter converter;

    AbstractBase(Site site, Source source) {
        super(site);
        init(source);
    }

    private void init(Source source) {
        if (source == null) {
            log.warn("Source is null, skip initialize.");
        }

        setSource(source);

        this.converter = getSite().getConverter(source);
        this.outputFileExtension = this.converter.getOutputFileExtension(source);

        String title = (String) source.getMeta().get("title");
        String content = source.getContent();
        String layout = (String) source.getMeta().get("layout");
        String permalink = (String) source.getMeta().get("permalink");

        String path = (String) source.getMeta().get("path");
        if (path == null) {
            SourceEntry sourceEntry = source.getSourceEntry();
            path = sourceEntry.getPath() + "/" + sourceEntry.getName();
        }

        //date, updated
        Date date = lookup(source.getMeta(), "date");
        Date updated = lookup(source.getMeta(), "updated");

        setTitle(title);
        setContent(content);
        setLayout(layout);
        setPermalink(permalink);
        setPath(path);
        setDate(date);
        setUpdated(updated);

        if (title != null) {
            log = LoggerFactory.getLogger(getClass().getName() + "[" + title + "]");
        }
    }

    protected Date lookup(Map<String, Object> frontMatter, String dateName) {
        Object date = frontMatter.get(dateName);
        if (date != null && !(date instanceof Date)) {
            String string = date.toString();
            //try parse from yyyy-MM-dd HH:mm
            try {
                date = f1.parse(string);
            } catch (ParseException e) {
                //ignore
            }
            if (date == null) {
                try {
                    date = f2.parse(string);
                } catch (ParseException e) {
                    //ignore
                }
            }
        }
        return (Date) date;
    }

    @Override
    protected String getOutputFileExtension() {
        return this.outputFileExtension;
    }

    /**
     * @return the converter
     */
    protected Converter getConverter() {
        return converter;
    }


    @Override
    public void convert() {
        setContent(getConverter().convert(getContent()));
    }


    /**
     * For freemarker template.
     *
     * @return the date formatted string
     */
    public String getDate_formatted() {
        return getDateFormatted();
    }

    /**
     * For freemarker template.
     *
     * @return the update date formatted string
     */
    public String getUpdated_formatted() {
        return getUpdatedFormatted();
    }


    @Override
    public void render(Map<String, Object> rootMap) {
        rootMap = new HashMap<String, Object>(rootMap);
        mergeRootMap(rootMap);
        getSite().getRenderer().render(this, rootMap);
    }

    protected void mergeRootMap(Map<String, Object> rootMap) {
        String canonical = getSite().buildCanonical(getUrl());
        rootMap.put("canonical", canonical);
        mergeHighlighterParam(rootMap);
    }

    /**
     * @param rootMap
     */
    private void mergeHighlighterParam(Map<String, Object> rootMap) {
        Highlighter highlighter = getSite().getFactory().getHighlighter();
        if (highlighter != null && ".html".equals(outputFileExtension)
                && containsHighlightCodeBlock(highlighter)) {
            log.debug("The content contains highlight code block.");
            rootMap.put("highlighter", highlighter.getHighlighterName());
        }
    }

    /**
     * @param highlighter
     */
    protected boolean containsHighlightCodeBlock(Highlighter highlighter) {
        return highlighter.containsHighlightCodeBlock(getContent());
    }
}
