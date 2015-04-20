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

import com.google.common.base.Strings;
import org.opoo.press.collection.configuration.MetaTagConfiguration;

import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class MetaTagConfigurationImpl implements MetaTagConfiguration {
    private String meta;
    private String metaForCollection;
    private String[] alias;
    private String separator;
    private Map<String,String> names;
    private Map<String,?> map;

    MetaTagConfigurationImpl(Map.Entry<String, ?> entry) {
        meta = entry.getKey();
        if(Strings.isNullOrEmpty(meta)){
            throw new IllegalArgumentException("No meta defined in collection's tag.");
        }

        Object value = entry.getValue();
        if(value != null && value instanceof Map) {
            this.map = (Map<String, ?>) value;

            this.metaForCollection = (String) map.get("metaForCollection");
            if(this.metaForCollection == null){
                this.metaForCollection = meta;
            }

            List<String> list = (List<String>) map.get("alias");
            if(list != null){
                this.alias = list.toArray(new String[list.size()]);
            }

            this.separator = (String) map.get("separator");
            this.names = (Map<String, String>) map.get("names");
        }
    }

    @Override
    public String getMeta() {
        return meta;
    }

    @Override
    public String[] getAlias() {
        return alias;
    }

    @Override
    public String getSeparator() {
        return separator;
    }

    @Override
    public Map<String, String> getNames() {
        return names;
    }

    @Override
    public String getMetaForCollection() {
        return metaForCollection;
    }

    @Override
    public <T> T get(String key) {
        return map != null ? (T) map.get(key) : null;
    }

    @Override
    public <T> T get(String key, T defaultValue){
        T t = get(key);
        return (t == null) ? defaultValue : t;
    }
}
