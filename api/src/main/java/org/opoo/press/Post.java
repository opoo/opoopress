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

import java.util.List;
import java.util.regex.Pattern;


/**
 * @author Alex Lin
 */
public interface Post extends Comparable<Post>, Page, Excerptable {

    String DEFAULT_EXCERPT_SEPARATOR = "<!--more-->";

    Pattern FILENAME_PATTERN = Pattern.compile("[1-9][0-9]{3}[-][0-1][0-9][-][0-3][0-9][-](.*)");

    List<Category> getCategories();

    List<Tag> getTags();

    String getId();
}
