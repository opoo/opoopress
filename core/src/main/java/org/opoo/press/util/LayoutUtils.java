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
package org.opoo.press.util;

/**
 * @author Alex Lin
 */
public abstract class LayoutUtils {

    public static boolean isValidLayout(String layout) {
        if (layout == null) {
            return false;
        }
        if ("nil".equalsIgnoreCase(layout)) {
            return false;
        }
        if ("null".equalsIgnoreCase(layout)) {
            return false;
        }
        if ("none".equalsIgnoreCase(layout)) {
            return false;
        }
        return true;
    }
}
