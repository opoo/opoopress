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
package org.opoo.press.collection.configuration.impl;

import org.opoo.press.collection.configuration.CategoryConfiguration;
import org.opoo.press.collection.configuration.CollectionConfiguration;
import org.opoo.press.collection.configuration.FilterConfiguration;
import org.opoo.press.collection.configuration.TagConfiguration;

import java.util.Map;

/**
 * @author Alex Lin
 */
public class CollectionConfigurationImpl extends MapConfiguration implements CollectionConfiguration{
    private String name;
    private FilterConfiguration filter;
    private TagConfiguration[] tags;
    private CategoryConfiguration[] categories;

    CollectionConfigurationImpl(String name, Map<String, ?> map) {
        super(map);
        this.name = name;

        Map<String,Object> filterMap = get("filter");
        if(filterMap != null){
            this.filter = new FilterConfigurationImpl(filterMap);
        }else{
            throw new IllegalArgumentException("No filter defined in collection: " + name);
        }

        Map<String,?> tagsMap = get("tags");
        if(tagsMap != null){
            tags = new TagConfiguration[tagsMap.size()];
            int index = 0;
            for(Map.Entry<String,?> entry: tagsMap.entrySet()){
                TagConfigurationImpl tagConfiguration = new TagConfigurationImpl(entry);
                tags[index++] = tagConfiguration;
            }
        }

        Map<String,?> categoriesMap = get("categories");
        if(categoriesMap != null){
            categories = new CategoryConfiguration[categoriesMap.size()];
            int index = 0;
            for(Map.Entry<String,?> entry: categoriesMap.entrySet()){
                CategoryConfigurationImpl categoryConfiguration = new CategoryConfigurationImpl(entry);
                categories[index++] = categoryConfiguration;
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FilterConfiguration getFilter() {
        return filter;
    }

    @Override
    public TagConfiguration[] getTags() {
        return tags;
    }

    @Override
    public CategoryConfiguration[] getCategories() {
        return categories;
    }
}
