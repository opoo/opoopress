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
package org.opoo.press;

import java.io.File;
import java.io.Serializable;

/**
 * @author Alex Lin
 */
public interface SourceEntry extends Serializable{
    /**
     * The file.
     * @return
     */
    File getFile();

    /**
     * The name of file.
     * @return
     */
    String getName();

    /**
     * Last modified time of the file.
     * @return
     */
    long getLastModified();

    /**
     * The length of the file.
     * @return
     */
    long getLength();

    /**
     * Site source directory.
     * @return
     */
    File getSourceDirectory();

    /**
     * Path from source directory to this file.
     * @return "" or string starts with "/".
     */
    String getPath();
}
