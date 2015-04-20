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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.opoo.press.Config;
import org.opoo.press.MetaTag;
import org.opoo.press.Page;

import java.util.List;

/**
 * @author Alex Lin
 */
public abstract class AbstractMetaTag implements MetaTag{
    private String slug;
    private String name;
    private List<Page> pages = Lists.newArrayList();
    private Page page;
    private Config config;

    public AbstractMetaTag(String slug, String name){
        this.slug = slug;
        this.name = name;
    }

    /**
     * @return the slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * @param slug the slug to set
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isNameOrSlug(String nameOrSlug) {
        if(nameOrSlug.equals(getSlug())){
            return true;
        }

        if(nameOrSlug.equals(getName())){
            return true;
        }
        return false;
    }

    @Override
    public List<Page> getPages() {
        return pages;
    }

    @Override
    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public int getPagesSize(){
        return pages.size();
    }

    @Override
    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getUrl(){
        if(page != null){
            return page.getUrl();
        }
        return null;
    }
    public String toString(){
        return MoreObjects.toStringHelper(this)
                .add("slug", getSlug())
                .add("name", getName())
                .add("size", pages.size())
                .toString();
    }
}
