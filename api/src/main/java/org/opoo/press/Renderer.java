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

import java.io.Writer;

/**
 * @author Alex Lin
 */
public interface Renderer {

    /**
     * Prepare process before render.
     */
    void prepare();

    /**
     * Render the content.
     *
     * @param base    page or post etc.
     * @param rootMap root map, context
     */
    void render(Base base, Object rootMap);


    void render(String templateName, Object rootMap, Writer out);

    String render(String templateName, Object rootMap);

    void renderContent(String templateContent, Object rootMap, Writer out);

    String renderContent(String templateContent, Object rootMap);
}
