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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.opoo.press.Category;

import java.util.List;

/**
 * @author Alex Lin
 */
public class CategoryImpl extends AbstractMetaTag implements Category {
    private Category parent;
    private List<Category> children = Lists.newArrayList();

    public CategoryImpl(String slug, String name) {
        this(slug, name, null);
    }

    public CategoryImpl(String slug, String name, Category parent) {
        super(slug, name);
        this.parent = parent;

        if (parent != null) {
            parent.getChildren().add(this);
        }
    }

    /**
     * @return the parent
     */
    @Override
    public Category getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Category#getChildren()
     */
    @Override
    public List<Category> getChildren() {
        return children;
    }

    @Override
    public String getPath() {
        return parent != null ? parent.getPath() + "/" + getSlug() : getSlug();
    }

    public String toString() {
        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this)
                .add("slug", getSlug())
                .add("name", getName())
                .add("size", getPagesSize());
        if (parent != null) {
            toStringHelper.add("parent", parent.getSlug());
        }
        return toStringHelper.toString();
    }
}
