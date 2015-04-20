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


import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Base;
import org.opoo.press.Converter;
import org.opoo.press.Convertible;
import org.opoo.press.Pager;
import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.press.Source;
import org.opoo.press.SourceEntry;
import org.opoo.press.renderer.AbstractFreeMarkerRenderer;
import org.opoo.press.util.LinkUtils;
import org.opoo.util.URLUtils;
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
public abstract class AbstractSourcePage extends SimplePage implements Base, Convertible {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private DateFormat f1 = new SimpleDateFormat(DATE_FORMAT_PATTERN_1);
    private DateFormat f2 = new SimpleDateFormat(DATE_FORMAT_PATTERN_2);

    private String outputFileExtension;
    private Converter converter;
    private String url;

    AbstractSourcePage(Site site, Source source, Pager pager) {
        super(site);

        if (source == null) {
            log.warn("Source is null, skip initialize.");
        }

        setSource(source);
        setPager(pager);

        this.converter = getSite().getConverter(source);
        this.outputFileExtension = this.converter.getOutputFileExtension(source);

        String title = (String) source.getMeta().get("title");
        String content = source.getContent();
        String layout = (String) source.getMeta().get("layout");
        String permalink = (String) source.getMeta().get("permalink");
        url = (String) source.getMeta().get("url");

        String path = (String) source.getMeta().get("path");
        if (path == null) {
            SourceEntry sourceEntry = source.getSourceEntry();
            path = sourceEntry.getPath() + "/" + sourceEntry.getName();
        }

        //date, updated
        Date date = lookup(source.getMeta(), "date");
        Date updated = lookup(source.getMeta(), "updated");

        Boolean bool = (Boolean) source.getMeta().get("published");
        boolean published = (bool == null || bool.booleanValue());

        boolean urlEncode = site.getConfig().get("url_encode", false);
        boolean urlDecode = site.getConfig().get("url_decode", false);

        setTitle(published ? title : "[Draft]" + title);
        setSourceContent(content);
        setLayout(layout);
        setPermalink(permalink);
        setPath(path);
        setDate(date);
        setUpdated(updated);
        //setUrl(url);
        setPublished(published);

        if(urlDecode) decodeUrl();
        if(urlEncode) encodeUrl();

//        if (title != null) {
//            log = LoggerFactory.getLogger(getClass().getName() + "[" + title + "]");
//        }

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
        setConvertedContent(getConverter().convert(getSourceContent()));
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
    public String getUrl(){
        //Lazy build url
        if(url == null){
            url = buildUrl();

            if(urlEncode){
                url = URLUtils.encodeURL(url);
            }
        }
        return url;
    }

    @Override
    public void setUrl(String url){
        this.url = url;
    }


    private String buildUrl() {
        //log.debug("Building page url: " + getTitle());

        SourceEntry sourceEntry = getSource().getSourceEntry();
        String fileName = sourceEntry.getName();
        String baseName = FilenameUtils.getBaseName(fileName);
        String path = sourceEntry.getPath();
        String ext = getOutputFileExtension();
        String name = baseName;

        if(Post.FILENAME_PATTERN.matcher(baseName).matches()){
            name = baseName.substring(11);
        }

        String permalink = getPossiblePermalink();

        if(StringUtils.isBlank(permalink)){
            if ("index".equals(baseName) && (".html".equals(ext) || ".html".equals(ext))) {
                return path + "/";
            }else{
                return path + "/" + name + ext;
            }
        }

        Map<String,Object> params = new HashMap<String, Object>(getSource().getMeta());
        params.put("pathToFile", path);
        params.put("fileName", fileName);
        params.put("name", name);

        Date date = getDate();
        if(date != null){
            LinkUtils.addDateParams(params, date);
        }

        return AbstractFreeMarkerRenderer.process(permalink, params);
    }

    private String getPossiblePermalink(){
        String link = getPermalink();
        if(StringUtils.isNotBlank(link)){
            return link;
        }
        return getSite().getPermalink(getLayout());
    }
}
