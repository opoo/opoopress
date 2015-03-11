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

/**
 * Converter interface.
 *
 * @author Alex Lin
 */
public interface Converter extends Ordered {

    /**
     * Determine this converter matches the specified source.
     *
     * @param src
     * @return return true if matched
     */
    boolean matches(Source src);

    /**
     * Convert the content.
     *
     * @param content
     * @return converted content
     */
    String convert(String content);

    /**
     * The converted content filename extension, must start with a '.',
     * such as '.html'.
     *
     * @param src
     * @return file extension start with a '.'
     */
    String getOutputFileExtension(Source src);
}
