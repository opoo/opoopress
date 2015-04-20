/*
 * Copyright 2015 Alex Lin.
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
package org.opoo.press;

import java.util.List;

/**
 * @author Alex Lin
 */
public interface Factory extends ObjectFactory{

    SourceEntryLoader getSourceEntryLoader();

    SourceParser getSourceParser();

    SourceManager getSourceManager();

    Highlighter getHighlighter();

    SlugHelper getSlugHelper();

    RelatedPostsFinder getRelatedPostsFinder();

    Page createPage(Site site, Source source, String layout);

    Page createPage(Site site, Source source);

    List<Plugin> getPlugins();

    PluginManager getPluginManager();

    Renderer getRenderer();

    PaginationUpdater getPaginationUpdater();

    ResourceBuilder createResourceBuilder(String type);

    Category createCategory(String categoryMeta, String slug, String categoryName, Category parent);

    Category createCategory(String categoryMeta, String slug, String categoryName);

    Category createCategory(String categoryMeta, String slugOrName);

    Tag createTag(String tagMeta, String slug, String name);

    Tag createTag(String tagMeta, String slugOrName);
}
