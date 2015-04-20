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
package org.opoo.press.collection.configuration.impl;

import java.util.Map;

/**
 * @author Alex Lin
 */
public class MapConfiguration{
    private final Map<String,?> map;

    MapConfiguration(Map<String,?> map){
        this.map = map;
    }

    public <T> T get(String key){
        return (T) map.get(key);
    }

    public <T> T get(String key, T defaultValue){
        T t = get(key);
        return (t == null) ? defaultValue : t;
    }
}
