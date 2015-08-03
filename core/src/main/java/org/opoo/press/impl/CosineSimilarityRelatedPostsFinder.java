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

import org.opoo.press.Config;
import org.opoo.press.ConfigAware;
import org.opoo.press.MetaTag;
import org.opoo.press.Page;
import org.opoo.press.Post;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.SiteConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Cosine similarity related posts algorithm.
 * <ul>
 * <li><a href="http://zh.wikipedia.org/wiki/%E4%BD%99%E5%BC%A6%E7%9B%B8%E4%BC%BC%E6%80%A7">余弦相似性</a></li>
 * <li><a href="http://en.wikipedia.org/wiki/Cosine_similarity">Cosine Similarity</a></li>
 * </ul>
 *
 * @author Alex Lin
 * @since 1.0.2
 */
public class CosineSimilarityRelatedPostsFinder implements RelatedPostsFinder, ConfigAware {
    private static final Logger log = LoggerFactory.getLogger(CosineSimilarityRelatedPostsFinder.class);

    private int size = 5;
    private double categoriesFactor = 1.0;
    private double tagsFactor = 1.0;

    public CosineSimilarityRelatedPostsFinder() {
    }

    CosineSimilarityRelatedPostsFinder(SiteConfig config) {
        setConfig(config);
    }

    static Map<Post, Integer> calculatePostsCount(Post post, List<? extends MetaTag> tags) {
        Map<Post, Integer> countMap = new HashMap<Post, Integer>();
        for (MetaTag tag : tags) {
            List<Page> posts = tag.getPages();
            for_01:
            for (Page p : posts) {
                if (p instanceof Post) {
                    if (p.equals(post)) {
                        continue for_01;
                    }
                    Integer count = countMap.get(p);
                    if (count == null) {
                        countMap.put((Post) p, 1);
                    } else {
                        countMap.put((Post) p, count.intValue() + 1);
                    }
                }
            }
        }

        return countMap;
    }

    static double calculate(int n, int a, int b) {
        double x = n * 1.0 / (Math.sqrt(a) * Math.sqrt(b));
        return x;
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
        if (size <= 0 || (post.getCategories().isEmpty() && post.getTags().isEmpty())) {
            return null;
        }

        // n = n(a, b);
        // x = n / (sqrt(a.count) * sqrt(b.count))
        Map<Post, Integer> categoriesCountMap = calculatePostsCount(post, post.getCategories());
        Map<Post, Integer> tagsCountMap = calculatePostsCount(post, post.getTags());

        Map<Post, Double> scoreMap = new HashMap<Post, Double>();
        //categories
        for (Map.Entry<Post, Integer> en : categoriesCountMap.entrySet()) {
            double score = calculate(en.getValue(), post.getCategories().size(), en.getKey().getCategories().size());
            score = score * categoriesFactor;
            scoreMap.put(en.getKey(), score);
        }

        //tags
        for (Map.Entry<Post, Integer> en : tagsCountMap.entrySet()) {
            double score = calculate(en.getValue(), post.getTags().size(), en.getKey().getTags().size());
            Double val = scoreMap.get(en.getKey());
            if (val == null) {
                val = 0.0;
            }
            score = score * tagsFactor + val.doubleValue();
            scoreMap.put(en.getKey(), score);
        }

        final long time = post.getDate().getTime();
        List<Entry<Post, Double>> list = new ArrayList<Entry<Post, Double>>(scoreMap.entrySet());
        Collections.sort(list, new Comparator<Entry<Post, Double>>() {
            @Override
            public int compare(Entry<Post, Double> o1, Entry<Post, Double> o2) {
                double x = o1.getValue().doubleValue() - o2.getValue().doubleValue();
                int compare = (int) (x * 100000000.0);
                if (x == 0) {
                    //most near
                    long a = Math.abs(o1.getKey().getDate().getTime() - time);
                    long b = Math.abs(o2.getKey().getDate().getTime() - time);
                    return (a < b ? 1 : (a == b ? 0 : -1));
                    //most recent
                    //return o1.getKey().compareTo(o2.getKey());
                }
                return compare;
            }
        });

        Collections.reverse(list);

        if (size > list.size()) {
            size = list.size();
        }

        List<Entry<Post, Double>> subList = list.subList(0, size);

        ArrayList<Post> result = new ArrayList<Post>();
        for (Entry<Post, Double> en : subList) {
            result.add(en.getKey());
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.ConfigAware#setConfig(org.opoo.press.Config)
     */
    @Override
    public void setConfig(Config config) {
        String prefix = CosineSimilarityRelatedPostsFinder.class.getName();
        String categoriesFactorKey = prefix + ".categories.factor";
        String tagsFactorKey = prefix + ".tags.factor";

        this.size = config.get("related_posts", size);
        this.categoriesFactor = config.get(categoriesFactorKey, this.categoriesFactor);
        this.tagsFactor = config.get(tagsFactorKey, this.tagsFactor);

        log.debug("Set {}: {}", categoriesFactorKey, categoriesFactor);
        log.debug("Set {}: {}", tagsFactorKey, tagsFactor);
    }
}
