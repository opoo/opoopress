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

import org.opoo.press.collection.config.CollectionConfig;
import org.opoo.press.collection.config.CollectionConfigResolver;

import java.util.Map;

/**
 * @author Alex Lin
 */
public class CollectionConfigResolverImpl implements CollectionConfigResolver {
    @Override
    public CollectionConfig resolve(String collectionName, Map<String, ?> configurationMap) {
        return new CollectionConfigImpl(collectionName, configurationMap);
    }
}
