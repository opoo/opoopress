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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.opoo.press.Category;
import org.opoo.press.Collection;
import org.opoo.press.Config;
import org.opoo.press.Factory;
import org.opoo.press.Page;
import org.opoo.press.Tag;
import org.opoo.press.collection.configuration.CategoryConfiguration;
import org.opoo.press.collection.configuration.CollectionConfiguration;
import org.opoo.press.collection.configuration.TagConfiguration;
import org.opoo.press.impl.AbstractMetaTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Alex Lin
 */
public class CollectionMetaTagUtils {
    private static final Logger log = LoggerFactory.getLogger(CollectionMetaTagUtils.class);


    public static void initializeDefaultTags(Collection collection, CollectionConfiguration configuration, Factory factory){
        TagConfiguration[] tags = configuration.getTags();

        if(tags != null && tags.length > 0){
            String collectionName = configuration.getName();

            for(TagConfiguration tagConfiguration: tags){

                String tagMeta = tagConfiguration.getMeta();
                String tagMetaForCollection = tagConfiguration.getMetaForCollection();
                List<Tag> tagList = collection.getTagsHolder().get(tagMetaForCollection);

                createTags(collectionName, tagMeta, tagConfiguration.getNames(), factory, tagConfiguration, tagList);
            }
        }
    }

    public static void createTags(String collectionName, String tagMeta, Map<String,String> names,
                                       Factory factory, Config config, List<Tag> tagList) {
        if(names == null || names.isEmpty()){
            return;
        }

        log.debug("Processing tags for: {}-{}", collectionName, tagMeta);

        for(Map.Entry<String,String> entry : names.entrySet()){
            Tag tag = factory.createTag(collectionName + "-" + tagMeta, entry.getKey(), entry.getValue());

            if(tag instanceof AbstractMetaTag){
                ((AbstractMetaTag) tag).setConfig(config);
            }

            if(log.isDebugEnabled()){
                log.debug("Add tag: {} => {}", tagMeta, tag);
            }

            tagList.add(tag);
        }
    }

    public static void initializeDefaultCategories(Collection collection, CollectionConfiguration configuration, Factory factory){
        CategoryConfiguration[] categories = configuration.getCategories();

        if(categories != null && categories.length > 0){
            String collectionName = configuration.getName();

            for(CategoryConfiguration categoryConfiguration: categories){

                String categoryMeta = categoryConfiguration.getMeta();
                String tagMetaForCollection = categoryConfiguration.getMetaForCollection();
                List<Category> categoryList = collection.getCategoriesHolder().get(tagMetaForCollection);

                createCategories(collectionName, categoryMeta, categoryConfiguration.getNames(),
                        factory, categoryConfiguration, categoryList);

            }
        }
    }

    private static void createCategories(String collectionName, String categoryMeta,
                                         Map<String, String> names, Factory factory, Config config,
                                         List<Category> categoryList) {
        if(names == null || names.isEmpty()){
            return;
        }

        log.debug("Processing categories for: {}-{}", collectionName, categoryMeta);

        Map<String,Category> map = new HashMap<String, Category>();

        //sort by key
        TreeMap<String, String> treeMap = new TreeMap<String, String>(names);
        for(Map.Entry<String,String> entry: treeMap.entrySet()){
            String path = entry.getKey();
            String categoryName = entry.getValue();
            String slug = path;
            String parentPath = null;

            int index = path.lastIndexOf('/');//changed since 2.0
            if(index != -1){
                slug = path.substring(index + 1);
                parentPath = path.substring(0, index);
            }

            Category cat;
            if(parentPath != null){
                Category parent = map.get(parentPath);
                if(parent == null){
                    throw new IllegalArgumentException("Parent category not found: " + parentPath);
                }
                cat = factory.createCategory(collectionName + "-" + categoryMeta, slug, categoryName, parent);
            }else{
                cat = factory.createCategory(collectionName + "-" + categoryMeta, slug, categoryName);
            }

            if(cat instanceof AbstractMetaTag){
                ((AbstractMetaTag) cat).setConfig(config);
            }

            if(log.isDebugEnabled()){
                log.debug("Add category: {} => {}", categoryMeta, cat);
            }

            map.put(path, cat);
            categoryList.add(cat);
        }
    }


    public static void processPageMetaTags(Collection collection, CollectionConfiguration configuration,
                                           Factory factory, Page page){
        if(page.getSource() == null || page.getSource().getMeta() == null){
            return;
        }

        Map<String,Object> sourceMeta = page.getSource().getMeta();
        String collectionName = configuration.getName();

        processPageTags(collection, configuration, factory, page, collectionName, sourceMeta);
        processPageCategories(collection, configuration, factory, page, collectionName, sourceMeta);
    }


    private static void processPageTags(Collection collection, CollectionConfiguration configuration,
                                        Factory factory, Page page,
                                        String collectionName, Map<String, Object> sourceMeta){
        TagConfiguration[] tags = configuration.getTags();
        if(tags == null || tags.length == 0){
            return;
        }

        for(TagConfiguration tagConfiguration: tags){
            processPageTag(collection, configuration, factory, page, tagConfiguration, collectionName, sourceMeta);
        }
    }

    private static void processPageTag(Collection collection, CollectionConfiguration configuration,
                                       Factory factory, Page page, TagConfiguration tagConfiguration,
                                       String collectionName, Map<String, Object> sourceMeta) {
        List<String> stringTags = MetaTagsUtils.getStringTags(sourceMeta, tagConfiguration);
        if(stringTags == null || stringTags.isEmpty()){
            return;
        }

        String tagMeta = tagConfiguration.getMeta();
        List<Tag> tags = collection.getTagsHolder().get(tagConfiguration.getMetaForCollection());

        for(final String stringTag: stringTags){
            Tag tag = Iterables.tryFind(tags, new Predicate<Tag>() {
                @Override
                public boolean apply(Tag tag) {
                    return tag.isNameOrSlug(stringTag);
                }
            }).orNull();

            if(tag == null){
                tag = factory.createTag(collectionName + "-" + tagMeta, stringTag);
                if(tag instanceof AbstractMetaTag){
                    ((AbstractMetaTag) tag).setConfig(tagConfiguration);
                }
                tags.add(tag);
            }

			//tag.getPages().add(page);
            List<Page> pages = tag.getPages();
			if(!pages.contains(page)){
				pages.add(page);
			}
            page.getTagsHolder().add(tagMeta, tag);
        }
    }


    private static void processPageCategories(Collection collection, CollectionConfiguration configuration,
                                              Factory factory, Page page,
                                              String collectionName, Map<String, Object> sourceMeta) {
        CategoryConfiguration[] categories = configuration.getCategories();
        if(categories == null || categories.length == 0){
            return;
        }

        for(CategoryConfiguration categoryConfiguration: categories){
            processPageCategory(collection, configuration, factory, page, categoryConfiguration,
                    collectionName, sourceMeta);
        }
    }

    private static void processPageCategory(Collection collection, CollectionConfiguration configuration,
                                            Factory factory, Page page, CategoryConfiguration categoryConfiguration,
                                            String collectionName, Map<String, Object> sourceMeta) {
        List<String> stringCategories = MetaTagsUtils.getStringTags(sourceMeta, categoryConfiguration);
        if(stringCategories == null || stringCategories.isEmpty()){
            return;
        }

        String categoryMeta = categoryConfiguration.getMeta();
        List<Category> categories = collection.getCategoriesHolder().get(categoryConfiguration.getMetaForCollection());

        for(final String stringCategory: stringCategories){
            Category category = Iterables.tryFind(categories, new Predicate<Category>() {
                @Override
                public boolean apply(Category input) {
                    return input.isNameOrSlug(stringCategory);
                }
            }).orNull();
            if(category == null){
                category = factory.createCategory(collectionName + "-" + categoryMeta, stringCategory);
                if(category instanceof AbstractMetaTag){
                    ((AbstractMetaTag) category).setConfig(categoryConfiguration);
                }
                categories.add(category);
            }

            //category.getPages().add(page);
            List<Page> pages = category.getPages();
			if(!pages.contains(page)){
				pages.add(page);
			}
			page.getCategoriesHolder().add(categoryMeta, category);
        }
    }
}
