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
package org.opoo.press.impl;

import org.opoo.press.Category;
import org.opoo.press.Config;
import org.opoo.press.ConfigAware;
import org.opoo.press.Page;
import org.opoo.press.Post;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.SiteConfig;
import org.opoo.press.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple algorithm for finding related posts.
 * It is not recommended to use this finder, just for demo.
 *
 * @author Alex Lin
 * @see CosineSimilarityRelatedPostsFinder
 * @since 1.0.2
 */
public class SimpleRelatedPostsFinder implements RelatedPostsFinder, ConfigAware {
    private int size = 5;

    SimpleRelatedPostsFinder(SiteConfig config) {
        setConfig(config);
    }

    public SimpleRelatedPostsFinder() {
    }

    @Override
    public void setConfig(Config config) {
        this.size = config.get("related_posts", size);
    }

    /* (non-Javadoc)
     * @see org.opoo.press.RelatedPostsFinder#findRelatedPosts(org.opoo.press.Post)
     */
    @Override
    public List<Post> findRelatedPosts(Post post) {
        return findRelatedPosts(post, size);
    }

    /* (non-Javadoc)
     * @see org.opoo.press.RelatedPostsFinder#findRelatedPosts(org.opoo.press.Post, int)
     */
    @Override
    public List<Post> findRelatedPosts(Post post, int size) {
        if (size <= 0) {
            return null;
        }

        List<Category> categories = post.getCategories();
        List<Tag> tags = post.getTags();
        if (categories.isEmpty() || tags.isEmpty()) {
            return null;
        }

        List<Post> allRelatedPosts = new ArrayList<Post>();

        for (Category category : categories) {
            mergeRelatedPosts(allRelatedPosts, post, category.getPages());
        }

        for (Tag tag : tags) {
            mergeRelatedPosts(allRelatedPosts, post, tag.getPages());
        }

        if (allRelatedPosts.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(allRelatedPosts);
        Collections.reverse(allRelatedPosts);

        if (size > allRelatedPosts.size()) {
            size = allRelatedPosts.size();
        }
        return allRelatedPosts.subList(0, size);
    }

    /**
     * @param allRelatedPosts
     * @param posts
     */
    private void mergeRelatedPosts(List<Post> allRelatedPosts, Post thisPost, List<Page> posts) {
        for (Page post : posts) {
            if (post instanceof Post) {
                if (post.equals(thisPost)) {
                    continue;
                }
                if (allRelatedPosts.contains(post)) {
                    continue;
                }
                allRelatedPosts.add((Post) post);
            }
        }
    }
}
