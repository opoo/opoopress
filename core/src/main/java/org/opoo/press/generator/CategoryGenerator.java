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
package org.opoo.press.generator;

import org.opoo.press.Category;
import org.opoo.press.Generator;
import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.Post;
import org.opoo.press.Site;
import org.opoo.press.impl.BasicBase;
import org.opoo.util.I18NUtils;
import org.opoo.util.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class CategoryGenerator implements Generator {
    private static final Logger log = LoggerFactory.getLogger(CategoryGenerator.class);

    /* (non-Javadoc)
     * @see org.opoo.press.Ordered#getOrder()
     */
    @Override
    public int getOrder() {
        return 200;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Generator#generate(org.opoo.press.Site)
     */
    @Override
    public void generate(Site site) {
        log.debug("Generating category pages...");
        List<Category> categories = site.getCategories();

        String categoryPageTitlePrefix = getCategoryPageTitlePrefix(site);
        String template = getCategoryPageTemplate(site);

        for (Category category : categories) {
            List<Post> posts = category.getPosts();
            if (posts.isEmpty()) {
                continue;
            }
            Collections.sort(posts);
            Collections.reverse(posts);

            CategoryPage page = new CategoryPage(site, template);
            page.setTitle(categoryPageTitlePrefix + category.getTitle());
            page.setUrl(category.getUrl());
            page.setPosts(posts);

            site.getPages().add(page);
        }
    }

    private String getCategoryPageTitlePrefix(Site site) {
        String prefix = I18NUtils.getString("messages", site.getLocale(), "category.page.title.prefix");
        if (prefix == null) {
            prefix = site.getConfig().get("category_page_title_prefix", "");
        }
        return prefix;
    }

    private String getCategoryPageTemplate(Site site) {
        return site.getConfig().get("category_page_template", "category.ftl");
    }


    public static class CategoryPage extends BasicBase implements Page {
        private List<Post> posts;
        private String template;

        public CategoryPage(Site site, String template) {
            super(site);
            this.template = template;
        }

        @Override
        public void render(Map<String, Object> rootMap) {
            rootMap = new HashMap<String, Object>(rootMap);
            rootMap.put("canonical", getSite().buildCanonical(getUrl()));
            rootMap.put("page", this);

            String output = getSite().getRenderer().render(template, rootMap);
            setContent(output);
        }

        /* (non-Javadoc)
         * @see org.opoo.press.Base#getLayout()
         */
        @Override
        public String getLayout() {
            return "nil";
        }

        /* (non-Javadoc)
         * @see org.opoo.press.Page#getPager()
         */
        @Override
        public Pager getPager() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.opoo.press.Page#setPager(org.opoo.press.Pager)
         */
        @Override
        public void setPager(Pager pager) {
        }

        @Override
        public String getOutputFileExtension() {
            return ".html";
        }

        public List<Post> getPosts() {
            return posts;
        }

        public void setPosts(List<Post> posts) {
            this.posts = posts;
        }

        public boolean isFooter() {
            return false;
        }

        public boolean isSidebar() {
            return true;
        }

        @Override
        public File getOutputFile(File dest) {
            String url = getUrl() + "index.html";
            url = URLUtils.decodeURL(url);
            File target = new File(dest, url);
            return target;
        }

        public boolean isComments() {
            return false;
        }
    }
}
