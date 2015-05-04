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
package org.opoo.press.slug;

import org.opoo.press.SlugHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Lin
 */
public class SimpleSlugHelper implements SlugHelper {
    private static final Logger log = LoggerFactory.getLogger(SimpleSlugHelper.class);

    @Override
    public String toSlug(String text) {
        if(text == null || text.length() == 0){
            return null;
        }

        char[] chars = text.toCharArray();
        StringBuffer sb = new StringBuffer();
        for(char c: chars){
            toSlug(text, sb, c);
        }
        if(sb.length() > 0){
            DefaultSlugHelper.trimDot(sb);
        }
        if(sb.length() == 0){
            throw new UnsupportedOperationException("Cannot process text '"
                    + text + "' to slug, try configure another SlugHelper instead.");
        }
        return sb.toString();
    }

    protected void toSlug(String text, StringBuffer result, char c) {
        // \/:*?"<>| not allowed for file name.
        if(c != '\\' && c != ':' && c != '*' && c != '?' && c != '"' && c != '<' && c != '>' && c != '|'){
            result.append(c);
        }else{
            log.debug("Slug contains illegal char, remote it: {} -> '{}'", text, c);
        }
    }
}
