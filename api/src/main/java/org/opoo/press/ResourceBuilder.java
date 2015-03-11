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
import java.util.Map;

/**
 * Handle javascript, css, etc.
 * <p/>
 * Such as: minify the javascript file.
 *
 * @author Alex Lin
 */
public interface ResourceBuilder/* extends Observer */ {

//	/**
//	 * Initialize this builder.
//	 * @param site
//	 * @param theme
//	 * @param config
//	 */
//	void init(Site site, Theme theme, Map<String, Object> config);

    /**
     * Initialize this builder.
     *
     * @param resourceBaseDirectory
     * @param config
     */
    void init(File resourceBaseDirectory, Map<String, Object> config);

    /**
     * Build the theme.
     *
     * @throws Exception
     */
    void build() throws Exception;

    /**
     * Clean output files.
     *
     * @throws Exception
     */
    void clean() throws Exception;
}
