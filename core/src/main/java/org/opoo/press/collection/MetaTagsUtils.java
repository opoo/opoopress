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
package org.opoo.press.collection;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.collection.configuration.MetaTagConfiguration;

import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class MetaTagsUtils {

    public static List<String> getStringTags(Map<String,?> meta, MetaTagConfiguration def){
        List<String> stringMetaValues = Lists.newArrayList();

        processSingleMeta(meta, def, def.getMeta(), stringMetaValues);

        String[] alias = def.getAlias();
        if(alias != null){
            for(String metaName: alias){
                processSingleMeta(meta, def, metaName, stringMetaValues);
            }
        }
        return stringMetaValues;
    }


    private static void processSingleMeta(Map<String,?> meta, MetaTagConfiguration def, String metaName, List<String> stringMetaValues){
        Object o = meta.get(metaName);
        if(o == null){
            return;
        }

        if(o instanceof List){
            stringMetaValues.addAll((List<String>) o);
            return;
        }

        String stringValue = o.toString();
        if(StringUtils.isBlank(stringValue)){
            return;
        }

        if(def.getSeparator() != null){
            List<String> strings = Splitter.on(def.getSeparator()).omitEmptyStrings().trimResults().splitToList(stringValue);
            stringMetaValues.addAll(strings);
        }else{
            stringMetaValues.add(stringValue.trim());
        }
    }
}
