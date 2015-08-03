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
package org.opoo.press.source;

import org.opoo.press.Origin;

/**
 * If the source entry has not front-matter header, throw this exception.
 *
 * @author Alex Lin
 */
public class NoFrontMatterException extends Exception {
    private static final long serialVersionUID = -5507870296641103275L;
    private Origin origin;

    public NoFrontMatterException(Origin origin) {
        super();
        this.origin = origin;
    }

    /**
     * @return the sourceEntry
     */
    public Origin getOrigin() {
        return origin;
    }
}
