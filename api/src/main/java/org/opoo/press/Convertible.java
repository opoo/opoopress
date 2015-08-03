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

import java.util.Map;

/**
 * Interface to describe convertible object.
 *
 * @author Alex Lin
 */
public interface Convertible extends Writable {

    /**
     * @param converter
     */
    void convert(Converter converter);

    /**
     * @param renderer content renderer
     * @param highlighter highlighter handler
     * @param rootMap root object for FreeMarker template
     */
    void render(Renderer renderer,Highlighter highlighter, Map<String, Object> rootMap);

}
