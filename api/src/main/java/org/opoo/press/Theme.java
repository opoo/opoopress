/*
 * Copyright 2014 Alex Lin.
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

/**
 * Theme
 *
 * @author Alex Lin
 * @since 1.2
 */
public interface Theme {

    /**
     * Theme configuration file.
     *
     * @return theme configutation file
     */
    File getConfigFile();

    /**
     * Theme path
     *
     * @return theme path
     */
    File getPath();

    /**
     * Source directory.
     *
     * @return source directory
     */
    File getSource();

    /**
     * Template directory of theme.
     *
     * @return theme template directory
     */
    File getTemplates();

    /**
     * Asset directory.
     *
     * @return theme asset directory
     */
    File getAssets();

    /**
     * Variable.
     *
     * @param name variable name
     * @return variable value
     */
    Object get(String name);

    /**
     * build this theme.
     */
    void build();

    /**
     * Theme observer.
     *
     * @return the theme observer.
     */
    Observer getObserver();

    /**
     * Clean all output files or directory.
     */
    void clean() throws Exception;
}
