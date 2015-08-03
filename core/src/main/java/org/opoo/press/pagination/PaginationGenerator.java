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
package org.opoo.press.pagination;

import org.opoo.press.Collection;
import org.opoo.press.Generator;
import org.opoo.press.Page;
import org.opoo.press.Site;
import org.opoo.press.SiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.opoo.press.pagination.PaginationUtils.getPagination;

/**
 * @author Alex Lin
 */
public class PaginationGenerator implements Generator {
    private static final Logger log = LoggerFactory.getLogger(PaginationGenerator.class);

    /* (non-Javadoc)
     * @see org.opoo.press.Generator#generate(org.opoo.press.Site)
     */
    @Override
    public void generate(Site site) {
        log.debug("Generating paginate pages...");

        List<Page> pages = site.getAllPages();
        SiteConfig config = site.getConfig();

        List<Page> allNewPages = new ArrayList<Page>();
        for (Page page : pages) {
            if (isPaginationEnabled(config, page)) {

                log.debug("[{}]: Paginating...", page.getUrl());
                List<Page> newPages = paginate(site, page);
                if (newPages != null) {
                    allNewPages.addAll(newPages);
                }
            }
        }

        if (!allNewPages.isEmpty()) {
            log.debug("Add {} paginated pages.", allNewPages.size());
            for(Page page: allNewPages){
                site.addPage(page);
                log.debug(page.getUrl());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Ordered#getOrder()
     */
    @Override
    public int getOrder() {
        return 2000;
    }

    public static int getPageSize(Site site, Page page) {
        return getPageSize(site, page, null);
    }

    public static int getPageSize(Site site, Page page, Map<String, ?> pagination) {
        Number number = page.get("paginate");
        if (number == null) {
            if (pagination == null) {
                pagination = getPagination(page);
            }
            if (pagination != null) {
                number = (Number) pagination.get("size");
            }
        }
        if (number == null) {
            number = site.getConfig().get("paginate");
        }
        if (number == null) {
            throw new IllegalArgumentException("No page size variable specified.");
        }

        return number.intValue();
    }

    public static List<Page> getPaginatingPages(Site site, Page page) {
        return getPaginatingPages(site, page, null);
    }

    public static List<Page> getPaginatingPages(Site site, Page page, Map<String, ?> pagination) {
        if (pagination == null) {
            pagination = getPagination(page);
        }

        if (pagination == null) {
            return null;
        }

        String collectionName = (String) pagination.get("collection");
        if (collectionName == null) {
            log.debug("[{}]: No collection name in pagination config, use 'post' as default.", page.getUrl());
            collectionName = "post";//paginate for post
        }

        Collection collection = site.getCollections().get(collectionName);
        if (collection != null) {
            return collection.getPages();
            /*
            final List<PageHolder> pages = collection.getPages();
            return new AbstractList<Page>() {
                @Override
                public Page get(int index) {
                    return pages.get(index).getPage();
                }

                @Override
                public int size() {
                    return pages.size();
                }
            };*/
        }
        return null;
    }


    public static boolean isPaginationEnabled(Site site, Page page) {
        return isPaginationEnabled(site.getConfig(), page);
    }

    public static boolean isPaginationEnabled(SiteConfig config, Page page) {
//		SourceEntry entry = page.getSource().getSourceEntry();
//		String name = entry.getName();
//		String path = entry.getPath();
//		// (isIndexPaginationEnabled || isNormalPagePaginationEnabled) && containsPaginatorInContent
//		return (config.getPage("paginate") != null && "".equals(path) && "index.html".equals(name)
//				|| page.getPage("paginate") != null)
//				&& page.getContent().contains("paginator.");

        return page.get("pagination") != null || page.get("paginate") != null;
    }

    public static List<Page> paginate(Site site, Page page) {
        Map<String, ?> pagination = getPagination(page);
        List<?> items = getPaginatingPages(site, page, pagination);

        if (items != null && !items.isEmpty()) {
            log.debug("[{}]: Paginating {} items", page.getUrl(), items.size());
            int pageSize = getPageSize(site, page, pagination);
            return PaginationUtils.paginate(site, page, items, pageSize);
        } else {
            log.warn("[{}]: No paginating items.", page.getUrl());
        }
        return null;
    }

    public static List<Page> paginate(Site site, Page page, List<?> items) {
        int pageSize = getPageSize(site, page);
        return PaginationUtils.paginate(site, page, items, pageSize);
    }

}
