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
public interface Factory {

    SourceEntryLoader getSourceEntryLoader();

    SourceParser getSourceParser();

    SourceManager getSourceManager();

    Highlighter getHighlighter();

    SlugHelper getSlugHelper();

    RelatedPostsFinder getRelatedPostsFinder();

    Post createPost(Site site, Source source);

    Page createPage(Site site, Source source);

    Post createDraft(Site site, Source source);

//    List<Converter> getConverters();
//
//    List<Generator> getGenerators();
//
//    List<Processor> getProcessors();

//    List<TemplateLoader> getTemplateLoaders();

    List<Plugin> getPlugins();

//    Map<String,TemplateModel> getTemplateModels();

    PluginManager getPluginManager();

    Renderer createRenderer(Site site);

    ResourceBuilder createResourceBuilder(String type);
}
