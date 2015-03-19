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
import java.util.Map;


/**
 * Interface to describe convertible object.
 *
 * @author Alex Lin
 */
public interface Convertible extends Writable {

    void convert();

    /**
     * @param rootMap root object for FreeMarker template
     */
    void render(Map<String, Object> rootMap);

//    /**
//     * @param dest
//     * @return output file
//     */
//    File getOutputFile(File dest);

    /**
     * @param dest
     */
    void write(File dest);
}
