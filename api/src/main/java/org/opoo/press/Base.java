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

import java.util.Date;


/**
 * The base interface for Post and Page class.
 *
 * @author Alex Lin
 */
public interface Base extends Convertible{
    String DATE_FORMAT_PATTERN_1 = "yyyy-MM-dd HH:mm";
    String DATE_FORMAT_PATTERN_2 = "yyyy-MM-dd HH:mm:ss";

    /**
     * The file source.
     *
     * @return source
     */
    Source getSource();

    /**
     * Return the post or page content.
     * Content might be changed by {@link Converter} in generate processing.
     *
     * @return the content text of post or page
     */
    String getContent();

    /**
     * Set new content.
     * @param content new content
     */
    void setContent(String content);

    /**
     * Page title.
     *
     * @return page title.
     */
    String getTitle();

    /**
     * Return the URL.
     * It's part of output URL, start with a '/', relative to
     * destination directory, without site root prefix.
     * <p/>
     * <p>e.g.: '/index.html', '/categories/', '/about/'
     * <p/>
     * <p>If the site url is 'http://press.opoo.org', the site root is '/docs',
     * and this page/post url is '/chapter01/', then the completely URL is
     * 'http://press.opoo.org/docs/chapter01/'.
     *
     * @return the page or post main URL
     * @see Site#getRoot()
     */
    String getUrl();

    /**
     * The origin file path of the source file, not source directory prefix.
     * Such as '/about/index.markdown'.
     *
     * @return the origin file path
     */
    String getPath();

    /**
     * The layout of source file.
     * All build in layout are 'default', 'post', 'page' and 'nil'.
     * <p>'nil' means null, not apply any template.
     * <p>Every layout(except 'nil') has a template defined in templates directory.
     * the template file name is '_&lt;layout&gt;.ftl', can extend OpooPress layout
     * using this mechanism.
     *
     * @return the layout of source file
     */
    String getLayout();

    /**
     * The permalink style.
     *
     * @return permalink
     */
    String getPermalink();

//	boolean isPublished();

    /**
     * The date format in the source file is 'yyyy-MM-dd HH:mm' or
     * 'yyyy-MM-dd HH:mm:ss'.
     *
     * @return date
     */
    Date getDate();

    /**
     * The updated date format in the source file is 'yyyy-MM-dd HH:mm' or
     * 'yyyy-MM-dd HH:mm:ss'.
     *
     * @return the updated date
     */
    Date getUpdated();

    /**
     * The output date format of the date.
     *
     * @return date format string
     * @see #getDate()
     */
    String getDateFormatted();

    /**
     * The output date format of the updated date.
     *
     * @return updated date format string
     * @see #getUpdated()
     */
    String getUpdatedFormatted();

    /**
     * Get meta data.
     *
     * @param name
     * @return value
     */
    Object get(String name);

    /**
     * Set meta data.
     *
     * @param name
     * @param value
     * @see #get(String)
     */
    void set(String name, Object value);
}
