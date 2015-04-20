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
package org.opoo.press.collection;

import org.opoo.press.Category;
import org.opoo.press.Collection;
import org.opoo.press.Config;
import org.opoo.press.ListHolder;
import org.opoo.press.Page;
import org.opoo.press.Tag;
import org.opoo.press.collection.configuration.CollectionConfiguration;
import org.opoo.press.impl.ListHolderImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Lin
 */
public class CollectionImpl implements Collection{
    private CollectionConfiguration configuration;
    private List<Page> pages = new ArrayList<Page>();
    private ListHolder<Tag> tags;
    private ListHolder<Category> categories;

    public CollectionImpl(CollectionConfiguration configuration){
        this.configuration = configuration;
        this.tags = new ListHolderImpl<Tag>();
        this.categories = new ListHolderImpl<Category>();
    }

    @Override
    public Config getConfiguration() {
        return configuration;
    }

    @Override
    public List<? extends Page> getPages() {
        return pages;
    }

    @Override
    public ListHolder<Tag> getTagsHolder() {
        return tags;
    }

    @Override
    public ListHolder<Category> getCategoriesHolder() {
        return categories;
    }

    public CollectionImpl addPage(Page page){
        pages.add(page);
        return this;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }
}
