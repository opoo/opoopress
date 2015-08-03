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
package org.opoo.press.impl;

import org.opoo.press.Collection;
import org.opoo.press.Page;
import org.opoo.press.Post;
import org.opoo.press.ProcessorAdapter;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.Site;
import org.opoo.press.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Alex Lin
 */
public class RelatedPostsProcessor extends ProcessorAdapter {
    private static final Logger log = LoggerFactory.getLogger(RelatedPostsProcessor.class);

    @Override
    public void preRender(Site site) {
        Collection collection = site.getCollections().get("post");
        if (collection != null) {
            RelatedPostsFinder postsFinder = site.getFactory().getRelatedPostsFinder();
            if (postsFinder == null) {
                return;
            }

            log.info("Processing related posts by '{}'.", postsFinder.getClass().getName());

            List<Page> posts = collection.getPages();
            for (Page page : posts) {
                Post post = PageUtils.unwrap(page, Post.class);
                List<Post> relatedPosts = postsFinder.findRelatedPosts(post);
                if (relatedPosts != null && !relatedPosts.isEmpty()) {
                    post.set("related_posts", relatedPosts);
                }
            }
        }
    }
}
