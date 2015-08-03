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
package org.opoo.press.collection.config.impl;

import org.opoo.press.collection.config.CategoryConfig;
import org.opoo.press.collection.config.CollectionConfig;
import org.opoo.press.collection.config.FilterConfig;
import org.opoo.press.collection.config.TagConfig;

import java.util.Map;

/**
 * @author Alex Lin
 */
public class CollectionConfigImpl extends MapConfig implements CollectionConfig {
    private String name;
    private FilterConfig filter;
    private TagConfig[] tags;
    private CategoryConfig[] categories;

    CollectionConfigImpl(String name, Map<String, ?> map) {
        super(map);
        this.name = name;

        Map<String, Object> filterMap = get("filter");
        if (filterMap != null) {
            this.filter = new FilterConfigImpl(filterMap);
        } else {
            throw new IllegalArgumentException("No filter defined in collection: " + name);
        }

        Map<String, ?> tagsMap = get("tags");
        if (tagsMap != null) {
            tags = new TagConfig[tagsMap.size()];
            int index = 0;
            for (Map.Entry<String, ?> entry : tagsMap.entrySet()) {
                TagConfigImpl tagConfiguration = new TagConfigImpl(entry);
                tags[index++] = tagConfiguration;
            }
        }

        Map<String, ?> categoriesMap = get("categories");
        if (categoriesMap != null) {
            categories = new CategoryConfig[categoriesMap.size()];
            int index = 0;
            for (Map.Entry<String, ?> entry : categoriesMap.entrySet()) {
                CategoryConfigImpl categoryConfiguration = new CategoryConfigImpl(entry);
                categories[index++] = categoryConfiguration;
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FilterConfig getFilter() {
        return filter;
    }

    @Override
    public TagConfig[] getTags() {
        return tags;
    }

    @Override
    public CategoryConfig[] getCategories() {
        return categories;
    }
}
