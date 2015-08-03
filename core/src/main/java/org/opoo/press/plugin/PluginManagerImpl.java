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
package org.opoo.press.plugin;

import org.opoo.press.Converter;
import org.opoo.press.Generator;
import org.opoo.press.Named;
import org.opoo.press.Ordered;
import org.opoo.press.PluginManager;
import org.opoo.press.Processor;
import org.opoo.press.Registry;
import org.opoo.press.Site;
import org.opoo.press.Source;
import org.opoo.press.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Alex Lin
 */
public class PluginManagerImpl implements PluginManager {
    private static final Logger log = LoggerFactory.getLogger(PluginManagerImpl.class);

    private Site site;
    private List<Converter> converters;
    private List<Generator> generators;
    private List<Processor> processors;
    private Map<Class, List> listMap = new HashMap<Class, List>();
    private Map<Class, Map> mapMap = new HashMap<Class, Map>();

    public PluginManagerImpl(Site site) {
        this.site = site;
    }

    protected <T> T apply(T t) {
        return ClassUtils.apply(t, site);
    }

    protected <T extends Ordered> void sort(List<T> list) {
        if (!list.isEmpty()) {
            Collections.sort(list, Ordered.COMPARATOR);
        }
    }

    public <T> List<T> instantiateList(Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        ServiceLoader<T> loader = ServiceLoader.load(clazz, site.getClassLoader());
        for (T t : loader) {
            apply(t);
            log.debug("Load instance: {} => {}", clazz.getName(), t.getClass().getName());
            list.add(t);
        }

        return list;
    }

    public <T> Map<String, T> instantiateMap(Class<T> clazz) {
        Map<String, T> map = new HashMap<String, T>();
        ServiceLoader<T> loader = ServiceLoader.load(clazz, site.getClassLoader());
        for (T t : loader) {
            if (t instanceof Named) {
                apply(t);
                log.debug("Load instance for '{}': {} => {}", ((Named) t).getName(), clazz.getName(), t.getClass().getName());
                map.put(((Named) t).getName(), t);
            }
        }

        return map;
    }

    @Override
    public Converter getConverter(Source source) throws RuntimeException {
        getConverters();
        for (Converter c : converters) {
            if (c.matches(source)) {
                return c;
            }
        }
        throw new RuntimeException("No matched converter: " + source.getOrigin());
    }

    public List<Converter> getConverters() {
        if (converters == null) {
            converters = instantiateList(Converter.class);
            sort(converters);
        }
        return converters;
    }

    @Override
    public List<Generator> getGenerators() {
        if (generators == null) {
            generators = instantiateList(Generator.class);
            sort(generators);
        }
        return generators;
    }

    @Override
    public List<Processor> getProcessors() {
        if (processors == null) {
            processors = instantiateList(Processor.class);
            sort(processors);
        }
        return processors;
    }

    @Override
    public Registry registerConverter(Converter c) {
        getConverters();
        converters.add(c);
        sort(converters);
        return this;
    }

    @Override
    public Registry registerGenerator(Generator g) {
        getGenerators();
        generators.add(g);
        sort(generators);
        return this;
    }

    @Override
    public Registry registerProcessor(Processor processor) {
        getProcessors();
        processors.add(processor);
        sort(processors);
        return this;
    }


    @Override
    public <T> List<T> getObjectList(Class<T> clazz) {
        List<T> list = listMap.get(clazz);
        if (list == null) {
            list = instantiateList(clazz);
            listMap.put(clazz, list);
        }

        return list;
    }

    @Override
    public <T> Registry register(Class<T> clazz, T instance) {
        getObjectList(clazz).add(instance);
        return this;
    }

    @Override
    public <T> Map<String, T> getObjectMap(Class<T> clazz) {
        Map<String, T> map = mapMap.get(clazz);
        if (map == null) {
            map = instantiateMap(clazz);
            mapMap.put(clazz, map);
        }
        return map;
    }

    @Override
    public <T> Registry register(Class<T> clazz, String name, T instance) {
        getObjectMap(clazz).put(name, instance);
        return this;
    }

    @Override
    public <T> Registry register(Class<T> clazz, Named named) {
        return register(clazz, named.getName(), (T) named);
    }
}
