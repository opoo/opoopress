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

import com.google.common.collect.Lists;
import org.opoo.press.Category;
import org.opoo.press.Factory;
import org.opoo.press.Highlighter;
import org.opoo.press.ObjectFactory;
import org.opoo.press.Page;
import org.opoo.press.PaginationUpdater;
import org.opoo.press.Plugin;
import org.opoo.press.PluginManager;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.Renderer;
import org.opoo.press.ResourceBuilder;
import org.opoo.press.Site;
import org.opoo.press.SlugHelper;
import org.opoo.press.Source;
import org.opoo.press.SourceEntryLoader;
import org.opoo.press.SourceManager;
import org.opoo.press.SourceParser;
import org.opoo.press.Tag;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Alex Lin
 */
public class SimpleFactory implements Factory, ObjectFactory {
    private SourceEntryLoader sourceEntryLoader;
    private SourceParser sourceParser;
    private SourceManager sourceManager;
    private Highlighter highlighter;
    private SlugHelper slugHelper;
    private RelatedPostsFinder relatedPostsFinder;
    private PaginationUpdater paginationUpdater;



    @Override
    public SourceEntryLoader getSourceEntryLoader() {
        return sourceEntryLoader;
    }

    @Override
    public SourceParser getSourceParser() {
        return sourceParser;
    }

    @Override
    public SourceManager getSourceManager() {
        return sourceManager;
    }

    @Override
    public Highlighter getHighlighter() {
        return highlighter;
    }

    @Override
    public SlugHelper getSlugHelper() {
        return slugHelper;
    }

    @Override
    public RelatedPostsFinder getRelatedPostsFinder() {
        return relatedPostsFinder;
    }

    @Override
    public Page createPage(Site site, Source source, String layout) {
        if("post".equals(layout)){
            return new SourcePost(site, source);
        }
        return new SourcePage(site, source);
    }

    @Override
    public Page createPage(Site site, Source source) {
        return createPage(site, source, (String) source.getMeta().get("layout"));
    }

    @Override
    public List<Plugin> getPlugins() {
        Lists.newArrayList(ServiceLoader.load(Plugin.class));
        return null;
    }

    @Override
    public PluginManager getPluginManager() {
        return null;
    }

    @Override
    public Renderer getRenderer() {
        return null;
    }

    @Override
    public PaginationUpdater getPaginationUpdater() {
        return null;
    }

    @Override
    public ResourceBuilder createResourceBuilder(String type) {
        return null;
    }

    @Override
    public Category createCategory(String categoryMeta, String slug, String categoryName, Category parent) {
        return null;
    }

    @Override
    public Category createCategory(String categoryMeta, String slug, String categoryName) {
        return null;
    }

    @Override
    public Category createCategory(String categoryMeta, String slugOrName) {
        return null;
    }

    @Override
    public Tag createTag(String tagMeta, String slug, String name) {
        return null;
    }

    @Override
    public Tag createTag(String tagMeta, String slugOrName) {
        return null;
    }


    @Override
    public <T> T getInstance(Class<T> clazz, String hint) {
        return null;
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T createInstance(Class<T> clazz, String hint) {
        return null;
    }

    @Override
    public <T> T createInstance(Class<T> clazz) {
        return null;
    }

    @Override
    public <T> T constructInstance(Class<T> clazz, String hint, Object... args) {
        return null;
    }
}
