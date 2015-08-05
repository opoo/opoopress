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
package org.opoo.press.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.opoo.press.ListHolder;
import org.opoo.press.MetaTag;
import org.opoo.press.MetaTagComparator;
import org.opoo.press.Page;
import org.opoo.press.PageComparator;
import org.opoo.press.PageWrapper;

import java.util.Collections;
import java.util.List;

/**
 * @author Alex Lin
 */
public abstract class PageUtils {

    public static <T extends Page> T unwrap(Page page, Class<T> clazz) {
        if (page instanceof PageWrapper) {
            return ((PageWrapper) page).unwrap(clazz);
        }
        return (T) page;
    }

    public static <T extends Page> List<T> unwrap(List<Page> pages, final Class<T> clazz){
        return Lists.transform(pages, new Function<Page, T>() {
            @Override
            public T apply(Page input) {
                return PageUtils.unwrap(input, clazz);
            }
        });
    }

    public static void sort(List<Page> pages) {
        Collections.sort(pages, PageComparator.INSTANCE);

        //set next and previous
        Page previous = null;
        for (Page page: pages) {
            if (previous != null) {
                previous.setNext(page);
                page.setPrevious(previous);
            }
            previous = page;
        }
    }

    public static void sort(ListHolder<? extends MetaTag> listHolder, boolean sortPagesOfMetaTag) {
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
}
