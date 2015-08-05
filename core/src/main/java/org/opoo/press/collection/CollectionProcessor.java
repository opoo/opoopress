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
import org.opoo.press.Category;
import org.opoo.press.Factory;
import org.opoo.press.ListHolder;
import org.opoo.press.MetaTag;
import org.opoo.press.MetaTagComparator;
import org.opoo.press.Page;
import org.opoo.press.PageComparator;
import org.opoo.press.ProcessorAdapter;
import org.opoo.press.Site;
import org.opoo.press.Tag;
import org.opoo.press.collection.config.CollectionConfig;
import org.opoo.press.collection.config.CollectionConfigResolver;
import org.opoo.press.collection.config.FilterConfig;
import org.opoo.press.collection.config.impl.CollectionConfigResolverImpl;
import org.opoo.press.util.ClassUtils;
import org.opoo.press.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex Lin
 */
public class CollectionProcessor extends ProcessorAdapter {
    private static final Logger log = LoggerFactory.getLogger(CollectionProcessor.class);

    private CollectionConfigResolver collectionConfigResolver = new CollectionConfigResolverImpl();

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public void postRead(Site site) {
        createCollections(site);
    }

    private void createCollections(Site site) {
        Map<String, ?> collectionsMap = site.getConfig().get("collections");
        if (collectionsMap == null) {
            log.warn("No collections defined, skip processing.");
            return;
        }

        for (Map.Entry<String, ?> configEntry : collectionsMap.entrySet()) {
            String collectionName = configEntry.getKey();
            Map<String, ?> collectionConfiguration = (Map<String, ?>) configEntry.getValue();
            createCollection(site, collectionName, collectionConfiguration);
        }
    }

    private void createCollection(Site site, String collectionName, Map<String, ?> collectionConfigurationMap) {
        CollectionConfig configuration = collectionConfigResolver.resolve(collectionName,
                collectionConfigurationMap);

        CollectionImpl collection = new CollectionImpl(site, configuration);

        log.debug("Initializing default tags and categories for collection: {}", collectionName);
        Factory factory = site.getFactory();
        CollectionMetaTagsUtils.initializeDefaultTags(collection, configuration, factory);
        CollectionMetaTagsUtils.initializeDefaultCategories(collection, configuration, factory);

        FilterConfig filterConfig = configuration.getFilter();
        Predicate<Page> filter = createFilter(site, factory, filterConfig);

        log.debug("Filter pages by: " + filter);

        for (Page page : site.getAllPages()) {
            if (filter.apply(page)) {
                //collection.addPage(page);
                collection.getPages().add(page);

                //collection not serializable
                //page.set("collection", collection);
                log.debug("Add page '{}' to collection '{}'", page.getUrl(), collectionName);

                log.debug("Processing tags and categories for page: {}", page.getUrl());
                CollectionMetaTagsUtils.processPageMetaTags(collection, configuration, factory, page);
            }
        }

        //if collection name is 'post' or config has property 'sort'
        Boolean sortable = configuration.get("sortable");
        if (collectionName.equals("post")
                || sortable != null && sortable) {

//            Collections.sort(collection.getPages(), PageComparator.INSTANCE);
            PageUtils.sort(collection.getPages());
            PageUtils.sort(collection.getCategoriesHolder(), true);
            PageUtils.sort(collection.getTagsHolder(), true);
        } else {
            PageUtils.sort(collection.getCategoriesHolder(), false);
            PageUtils.sort(collection.getTagsHolder(), false);
        }

        site.getCollections().put(collectionName, collection);

        if (collectionName.equals("post")) {
            log.debug("Set post collection tags and categories to site.");
            List<Category> categories = collection.getCategoriesHolder().get("category");
            List<Tag> tags = collection.getTagsHolder().get("tag");

            //Collections.sort(categories, MetaTagComparator.INSTANCE);
            //Collections.sort(tags, MetaTagComparator.INSTANCE);

            site.set("tags", tags);
            site.set("categories", categories);
        }
    }

    private Filter createFilter(Site site, Factory factory, FilterConfig filterConfig) {
        Object[] args = filterConfig.getArgs();
        Filter filter = null;
        if (args == null || args.length == 0) {
            filter = factory.createInstance(Filter.class, filterConfig.getType());
        } else {
            filter = factory.constructInstance(Filter.class, filterConfig.getType(), args);
        }

        if (filter != null) {
            return filter;
        }

        //type is classname
        String classname = filterConfig.getType();
        if (args == null || args.length == 0) {
            return ClassUtils.newInstance(classname, site.getClassLoader(), site, site.getConfig());
        } else {
            return ClassUtils.constructInstance(classname, site.getClassLoader(), null, args);
        }
    }

    @Override
    public void postGenerate(Site site) {
        Set<Page> siteTemplatePages = site.get("template_pages");
        if (siteTemplatePages != null) {
            log.info("Removing template pages: {}", siteTemplatePages.size());
            site.getAllPages().removeAll(siteTemplatePages);
        }
    }
}
