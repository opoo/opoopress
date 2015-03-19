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
import org.opoo.press.Base;
import org.opoo.press.Site;
import org.opoo.press.Source;
import org.opoo.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class BasicBase implements Base{
    private static final Logger log = LoggerFactory.getLogger(BasicBase.class);

    private final Site site;
    private Source source;
    private String content;
    private String title;
    private String url;
    private String path;
    private String layout;
    private String permalink;
    private Date date;
    private Date updated;
    private String dateFormatted;
    private String updatedFormatted;
    private Map<String,Object> data = new HashMap<String, Object>();

    public BasicBase(Site site) {
        this.site = site;
    }

    public Site getSite(){
        return site;
    }

    @Override
    public Source getSource() {
        return source;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
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

    @Override
    public void render(Map<String, Object> rootMap) {

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

    protected File getOutputFile(File dest) {
        String url = getUrlForOutputFile();
        if(url.endsWith("/")){
            url += "index" + getOutputFileExtension();
        }
        File target = new File(dest, url);
        return target;
    }

    protected String getUrlForOutputFile(){
        String url = getUrl();
        if(site.get("urldecode_for_output_file") != null){
            try{
                url = java.net.URLDecoder.decode(url, "UTF-8");
            }catch(Exception e){
                log.warn("url decode error", e);
            }
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
}
