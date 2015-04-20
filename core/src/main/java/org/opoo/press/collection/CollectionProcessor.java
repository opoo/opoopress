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
import org.opoo.press.collection.configuration.CollectionConfiguration;
import org.opoo.press.collection.configuration.CollectionConfigurationResolver;
import org.opoo.press.collection.configuration.FilterConfiguration;
import org.opoo.press.collection.configuration.impl.CollectionConfigurationResolverImpl;
import org.opoo.press.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex Lin
 */
public class CollectionProcessor extends ProcessorAdapter {
    private static final Logger log = LoggerFactory.getLogger(CollectionProcessor.class);

    private CollectionConfigurationResolver collectionConfigurationResolver
            = new CollectionConfigurationResolverImpl();

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
        CollectionConfiguration configuration = collectionConfigurationResolver.resolve(collectionName,
                collectionConfigurationMap);

        CollectionImpl collection = new CollectionImpl(configuration);

        log.debug("Initializing default tags and categories for collection: {}", collectionName);
        Factory factory = site.getFactory();
        CollectionMetaTagUtils.initializeDefaultTags(collection, configuration, factory);
        CollectionMetaTagUtils.initializeDefaultCategories(collection, configuration, factory);

        FilterConfiguration filterConfiguration = configuration.getFilter();
        Predicate<Page> filter = createFilter(site, factory, filterConfiguration);

        log.debug("Filter pages by: " + filter);

        Iterator<Page> iterator = site.getAllPages().iterator();
        while (iterator.hasNext()) {
            Page page = iterator.next();
            if (filter.apply(page)) {
                collection.addPage(page);
                page.set("collection", collection);
                log.debug("Add page '{}' to collection '{}'", page.getUrl(), collectionName);

                log.debug("Processing tags and categories for page: {}", page.getUrl());
                CollectionMetaTagUtils.processPageMetaTags(collection, configuration, factory, page);
            }
        }

        //if collection name is 'post' or configuration has property 'sort'
        Boolean sortable = configuration.get("sortable");
        if (collectionName.equals("post")
                || sortable != null && sortable) {

//            Collections.sort(collection.getPages(), PageComparator.INSTANCE);
            sort(collection.getPages());
            sort(collection.getCategoriesHolder(), true);
            sort(collection.getTagsHolder(), true);
        } else {
            sort(collection.getCategoriesHolder(), false);
            sort(collection.getTagsHolder(), false);
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

    private void sort(List<? extends Page> pages) {
        Collections.sort(pages, PageComparator.INSTANCE);

        //set next and previous
        Page previous = null;
        for(Page page: pages){
            if(previous != null){
                previous.setNext(page);
                page.setPrevious(previous);
            }
            previous = page;
        }
    }

    private void sort(ListHolder<? extends MetaTag> listHolder, boolean sortPagesOfMetaTag) {
        String[] keys = listHolder.getKeys();
        for (String key : keys) {
            List<? extends MetaTag> list = listHolder.get(key);
            //sort tag
            Collections.sort(list, MetaTagComparator.INSTANCE);

            if (sortPagesOfMetaTag) {
                for (MetaTag metaTag : list) {
                    List<Page> pages = metaTag.getPages();
                    if (pages != null && !pages.isEmpty()) {
                        Collections.sort(pages, PageComparator.INSTANCE);
                    }
                }
            }
        }
    }


    private Filter createFilter(Site site, Factory factory, FilterConfiguration filterConfiguration) {
//        try {
        Object[] args = filterConfiguration.getArgs();
        Filter filter = null;
        if (args == null || args.length == 0) {
            filter = factory.createInstance(Filter.class, filterConfiguration.getType());
        } else {
            filter = factory.constructInstance(Filter.class, filterConfiguration.getType(), args);
        }

        if (filter != null) {
            return filter;
        }

        //type is classname
        String classname = filterConfiguration.getType();
        if (args == null || args.length == 0) {
            return ClassUtils.newInstance(classname, site.getClassLoader(), site, site.getConfig());
        } else {
            return ClassUtils.constructInstance(classname, site.getClassLoader(), null, args);
        }
//        }catch (Exception e){
//            throw new RuntimeException("Create collection filter failed: " + e.getMessage(), e);
//        }
    }


    @Override
    public void preRenderAllPages(Site site) {
        Set<Page> siteTemplatePages = (Set<Page>) site.get("template_pages");
        if (siteTemplatePages != null) {
            log.info("Removing template pages: {}", siteTemplatePages.size());
            site.getAllPages().removeAll(siteTemplatePages);
        }
    }
}
