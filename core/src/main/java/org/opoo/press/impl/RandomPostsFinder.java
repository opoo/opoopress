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
import org.opoo.press.Post;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.Site;
import org.opoo.press.SiteAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Lin
 */
public class RandomPostsFinder implements RelatedPostsFinder, SiteAware {
    private static final Logger log = LoggerFactory.getLogger(RandomPostsFinder.class);
    private Site site;
    private int size = 10;

    @Override
    public List<Post> findRelatedPosts(Post post) {
        return findRelatedPosts(post, size);
    }

    @Override
    public List<Post> findRelatedPosts(Post post, int size) {
        Collection collection = site.getCollections().get("post");
        if(collection == null){
            return null;
        }

        List<Post> posts = (List<Post>) collection.getPages();
        int postSize = posts.size();
        if(postSize <= size){
            List<Post> randomPosts = new ArrayList<Post>(posts);
            randomPosts.remove(post);
            return randomPosts;
        }

        SecureRandom random = new SecureRandom();
        List<Post> randomPosts = new ArrayList<Post>();
        int count = 0;
        while(count < size){
            int x = random.nextInt(postSize);
            Post temp = posts.get(x);
            if(!randomPosts.contains(temp) && !temp.equals(post)){
                randomPosts.add(temp);
                count++;
            }
        }
        return randomPosts;
    }

    @Override
    public void setSite(Site site) {
        this.site = site;

        Number num = site.getConfig().get("random_posts");
        if(num != null){
            this.size = num.intValue();
            log.debug("Set random posts size: {}", size);
        }
    }
}
