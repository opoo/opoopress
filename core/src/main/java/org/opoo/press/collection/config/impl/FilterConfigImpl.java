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
package org.opoo.press.collection.config.impl;

import org.opoo.press.collection.config.FilterConfig;

import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class FilterConfigImpl extends MapConfig implements FilterConfig {
    private String type;
    private Object[] args;

    FilterConfigImpl(Map<String, ?> map) {
        super(map);
        this.type = get("type");
        List<?> list = get("args");
        if (list != null) {
            args = list.toArray(new Object[list.size()]);
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }
}
