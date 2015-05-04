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
package org.opoo.press;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alex Lin
 */
public interface Site extends SiteBuilder, SiteHelper {
    /**
     * Source directories.
     *
     * @return all source directories
     * @since 1.0.2
     */
    List<File> getSources();

    File getTemplates();

    List<File> getAssets();

    File getDestination();

    File getWorking();

    File getBasedir();

    String getRoot();

    SiteConfig getConfig();

    /**
     * @return all posts
     * @deprecated using getCollections().get("post").getPages();
     */
    @Deprecated
    List<Post> getPosts();

    /**
     * @deprecated using getCollections().get("page").getPages();
     * @return all pages(except posts)
     */
    @Deprecated
    List<Page> getPages();

    List<StaticFile> getStaticFiles();

    List<Page> getAllPages();

    Date getTime();

    Renderer getRenderer();

    Converter getConverter(Source source);

    Locale getLocale();

    /**
     * The permalink style for specified layout.
     *
     * @param layout
     * @return permalink
     */
    String getPermalink(String layout);

    boolean showDrafts();

    /**
     * Get value from configuration file or site variables.
     *
     * @param name
     * @return value
     * @see #set(String, Object)
     */
    <T> T get(String name);

    /**
     * Set a variable for site.
     *
     * @param name
     * @param value
     * @see #get(String)
     */
    <T> void set(String name, T value);

    /**
     * @return then theme of this site
     * @since 1.2
     */
    Theme getTheme();

    /**
     * @return site class loader
     */
    ClassLoader getClassLoader();

    Factory getFactory();

    Observer getObserver();

    Map<String, Collection> getCollections();
}
