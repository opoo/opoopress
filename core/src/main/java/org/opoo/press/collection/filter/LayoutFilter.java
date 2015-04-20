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
package org.opoo.press.collection.filter;

import org.opoo.press.Page;
import org.opoo.press.collection.Filter;

/**
 * @author Alex Lin
 */
public class LayoutFilter implements Filter{
    private final String layout;
    private final boolean equals;

    public LayoutFilter(String layout, boolean equals){
        this.layout = layout;
        this.equals = equals;
    }

    public LayoutFilter(String layout){
        this(layout, true);
    }

    @Override
    public boolean apply(Page input) {
        boolean bool = layout.equals(input.getLayout());
        return equals ? bool : !bool;
    }
}
