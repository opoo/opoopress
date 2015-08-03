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
import org.opoo.press.Page;
import org.opoo.press.PageWrapper;

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
}
