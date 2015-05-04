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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.opoo.press.Category;
import org.opoo.press.Collection;
import org.opoo.press.Config;
import org.opoo.press.Generator;
import org.opoo.press.ListHolder;
import org.opoo.press.MetaTag;
import org.opoo.press.Page;
import org.opoo.press.Site;
import org.opoo.press.Tag;
import org.opoo.press.impl.SimplePage;
import org.opoo.press.pagination.PaginationUtils;
import org.opoo.press.renderer.AbstractFreeMarkerRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex Lin
 */
public class CollectionMetaTagPageGenerator implements Generator{
    private static final Logger log = LoggerFactory.getLogger(CollectionMetaTagPageGenerator.class);


    @Override
    public void generate(Site site) {
        Map<String, Collection> collections = site.getCollections();

        Set<Page> templatePages = Sets.newHashSet();
        List<Page> allNewPages = Lists.newArrayList();

        for(Collection collection: collections.values()){
            log.debug("Generate meta tag page for collection: {}", collection.getName());
            generateCollectionTagPages(site, collection, templatePages, allNewPages);
            generateCollectionCategoryPages(site, collection, templatePages, allNewPages);
        }

        // put tempate pages in cache, will be removed later.
        if(!templatePages.isEmpty()){
            //site.getAllPages().removeAll(templatePages);
            Set<Page> siteTemplatePages = site.get("template_pages");
            if(siteTemplatePages != null){
                siteTemplatePages.addAll(templatePages);
            }else{
                site.set("template_pages", templatePages);
            }
        }

        if(!allNewPages.isEmpty()){
            site.getAllPages().addAll(allNewPages);

            if(log.isDebugEnabled()){
                log.debug("Add {} meta tag pages.", allNewPages.size());
                for(Page p: allNewPages){
                    log.debug(p.getUrl());
                }
            }
        }
    }


    private void generateCollectionTagPages(Site site, Collection collection, Set<Page> templatePages, List<Page> allNewPages) {
        ListHolder<Tag> tagsHolder = collection.getTagsHolder();
        String[] tagMetaNames = tagsHolder.getKeys();

        for(String tagMeta: tagMetaNames) {
            String templateIdentity = "tag_template_" + collection.getName() + "_" + tagMeta;

            Page templatePage = lookupTemplatePage(templateIdentity, site.getAllPages(), templatePages);

            if(templatePage == null){
                log.warn("Template page for collection '{}', tag '{}' not found.", collection.getName(), tagMeta);
                continue;
            }else{
                log.debug("Template found: {}", templatePage);
            }

            templatePages.add(templatePage);

            List<Tag> tags = tagsHolder.get(tagMeta);

            generateCollectionMetaTagPages(site, collection, templatePage, tags, allNewPages);
        }
    }

    private void generateCollectionMetaTagPages(Site site, Collection collection, Page templatePage, List<? extends MetaTag> tags,
                                            List<Page> allNewPages) {
        for(MetaTag tag: tags){
            List<Page> pages = tag.getPages();
            if(pages.isEmpty()){
                continue;
            }

            SimplePage tagPage = new SimplePage(site, templatePage, null);

            String title = tag.getName();
            String titlePrefix = getProperty(templatePage, tag.getConfig(), "title_prefix");
            if(titlePrefix != null){
                title = titlePrefix + title;
            }
            tagPage.setTitle(title);


            String permalink = getProperty(templatePage, tag.getConfig(), "permalink");
            String url = "/" + tag.getSlug() + "/";
            if(permalink != null){
                url = AbstractFreeMarkerRenderer.process(permalink, tag);
            }else {
                if(tag instanceof Category){
                    url = "/" + ((Category) tag).getPath() + "/";
                }
                String tagDir = getProperty(templatePage, tag.getConfig(), "output_dir");
                if(tagDir != null){
                    url = tagDir + url;
                }
            }
            tagPage.setUrl(url);

            //require render content
            //tagPage.set("render", true);

            tagPage.set("metaTag", tag);
            tag.setPage(tagPage);
            allNewPages.add(tagPage);

            //List<Page> pages = tag.getPages();
            //Collections.sort(pages, PageComparator.INSTANCE);

            Number paginate = getProperty(templatePage, tag.getConfig(), "paginate");
            if(paginate != null && paginate.intValue() > 0){
                List<Page> pagedList = PaginationUtils.paginate(site, tagPage, pages, paginate.intValue());
                if(pagedList != null){
                    allNewPages.addAll(pagedList);
                }
            }
        }
    }

    private <T> T getProperty(Page templatePage, Config metaTagConfig, String propertyName){
        T value = templatePage.get(propertyName);
        if(value == null && metaTagConfig != null){
            value = metaTagConfig.get(propertyName);
        }
        return value;
    }


    private Page lookupTemplatePage(final String identity, List<Page> allPages, Set<Page> templatePages){
        Predicate<Page> predicate = new Predicate<Page>() {
            @Override
            public boolean apply(Page input) {
                return input.get(identity) != null;
            }
        };

        Page page = Iterables.tryFind(templatePages, predicate).orNull();
        if(page == null) {
            page = Iterables.tryFind(allPages, predicate).orNull();
        }

//        if(page == null){
//            throw new RuntimeException("Template page not found: " + identity);
//        }
        return page;
    }


    private void generateCollectionCategoryPages(Site site, Collection collection, Set<Page> templatePages,
                                                 List<Page> allNewPages) {
        ListHolder<Category> categoriesHolder = collection.getCategoriesHolder();
        String[] categoryMetaNames = categoriesHolder.getKeys();

        for(String categoryMeta: categoryMetaNames) {
            String templateIdentity = "category_template_" + collection.getName() + "_" + categoryMeta;

            Page templatePage = lookupTemplatePage(templateIdentity, site.getAllPages(), templatePages);

            if(templatePage == null){
                log.warn("Template page for collection '{}', category '{}' not found.", collection.getName(), categoryMeta);
                continue;
            }

            templatePages.add(templatePage);

            List<Category> categories = categoriesHolder.get(categoryMeta);

            generateCollectionMetaTagPages(site, collection, templatePage, categories, allNewPages);
        }
    }



    @Override
    public int getOrder() {
        return 3000;
    }
}
