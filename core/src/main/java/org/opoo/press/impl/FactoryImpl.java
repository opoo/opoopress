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
package org.opoo.press.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.ConfigAware;
import org.opoo.press.Converter;
import org.opoo.press.Factory;
import org.opoo.press.Generator;
import org.opoo.press.Highlighter;
import org.opoo.press.Named;
import org.opoo.press.Ordered;
import org.opoo.press.Plugin;
import org.opoo.press.PluginManager;
import org.opoo.press.Processor;
import org.opoo.press.Registry;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.Renderer;
import org.opoo.press.ResourceBuilder;
import org.opoo.press.Site;
import org.opoo.press.SiteAware;
import org.opoo.press.SlugHelper;
import org.opoo.press.Source;
import org.opoo.press.SourceEntryLoader;
import org.opoo.press.SourceManager;
import org.opoo.press.SourceParser;
import org.opoo.press.highlighter.SyntaxHighlighter;
import org.opoo.press.slug.DefaultSlugHelper;
import org.opoo.press.source.SourceEntryLoaderImpl;
import org.opoo.press.source.SourceManagerImpl;
import org.opoo.press.source.SourceParserImpl;
import org.opoo.press.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Alex Lin
 */
public class FactoryImpl extends AbstractFactory implements Factory, PluginManager, SiteAware{
    private static final Logger log = LoggerFactory.getLogger(FactoryImpl.class);

    private Site site;
//    private ProviderImpl provider;
    private Map<String,Object> configuration = new LinkedHashMap<String, Object>();;

    private SourceEntryLoader sourceEntryLoader;
    private SourceParser sourceParser;
    private SourceManager sourceManager;
    private Highlighter highlighter;
    private SlugHelper slugHelper;
    private RelatedPostsFinder relatedPostsFinder;

    private List<Converter> converters;
    private List<Generator> generators;
    private List<Processor> processors;
//    private List<TemplateLoader> templateLoaders;
    private List<Plugin> plugins;
//    private Map<String,TemplateModel> templateModels;
    private Map<String,String> resourceBuilderClassNames;

    private Map<Class,List> listMap = new HashMap<Class, List>();
    private Map<Class,Map> mapMap = new HashMap<Class, Map>();

    @Override
    public void setSite(Site site){
        this.site = site;

        //default factory configuration
        InputStream is = Site.class.getResourceAsStream("/factory.yml");
        addConfiguration(is);

        //theme factory configuration
        File themeDir = site.getTheme().getPath();
        File factoryFile = new File(themeDir, "factory.yml");
        addConfiguration(factoryFile);

        //site factory configuration
        File basedir = site.getBasedir();
        factoryFile = new File(basedir, "factory.yml");
        addConfiguration(factoryFile);

        initializePlugins();
    }

    void addConfiguration(InputStream is){
        if(is != null) {
            Map map = new Yaml().loadAs(is, Map.class);
            configuration.putAll(map);
            IOUtils.closeQuietly(is);
        }
    }

    void addConfiguration(File factoryFile) {
        if(factoryFile != null && factoryFile.exists()){
            try {
                addConfiguration(new FileInputStream(factoryFile));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    @Override
    public SourceEntryLoader getSourceEntryLoader() {
        if(sourceEntryLoader == null){
            sourceEntryLoader = instantiate(SourceEntryLoader.class);
            if(sourceEntryLoader == null){
                sourceEntryLoader = new SourceEntryLoaderImpl();
            }
        }
        return sourceEntryLoader;
    }

    @Override
    public SourceParser getSourceParser() {
        if(sourceParser == null){
            sourceParser = instantiate(SourceParser.class);
            if(sourceParser == null){
                sourceParser = new SourceParserImpl();
            }
        }
        return sourceParser;
    }

    @Override
    public SourceManager getSourceManager() {
        if(sourceManager == null){
            sourceManager = instantiate(SourceManager.class);
            if(sourceManager == null){
                sourceManager = new SourceManagerImpl();
            }
        }
        return sourceManager;
    }

    @Override
    public Highlighter getHighlighter() {
        if(highlighter == null){
            highlighter = instantiate(Highlighter.class);
            if(highlighter == null){
                highlighter = new SyntaxHighlighter();
            }
        }
        return highlighter;
    }

    @Override
    public SlugHelper getSlugHelper() {
        if(slugHelper == null){
            if(site.getLocale() != null){
                String hint = site.getLocale().toString();
                String str = SlugHelper.class.getName() + "-" + hint;
                String className = (String) configuration.get(str);
                if(className != null){
                    slugHelper = newInstance(className);
                }
            }

            if(slugHelper == null){
                slugHelper = instantiate(SlugHelper.class);
            }

            if(slugHelper == null){
                slugHelper = new DefaultSlugHelper();
            }
        }
        return slugHelper;
    }

    @Override
    public RelatedPostsFinder getRelatedPostsFinder() {
        if(relatedPostsFinder == null){
            relatedPostsFinder = instantiate(RelatedPostsFinder.class);
            if(relatedPostsFinder == null){
                relatedPostsFinder = new NoOpRelatedPostsFinder();
            }
        }
        return relatedPostsFinder;
    }

    public List<Converter> getConverters() {
        if(converters == null){
            converters = instantiateList(Converter.class);
            sort(converters);
        }
        return converters;
    }

    @Override
    public List<Generator> getGenerators() {
        if(generators == null){
            generators = instantiateList(Generator.class);
            sort(generators);
        }
        return generators;
    }

    @Override
    public List<Processor> getProcessors() {
        if(processors == null){
            processors = instantiateList(Processor.class);
            sort(processors);
        }
        return processors;
    }

//    @Override
//    public List<TemplateLoader> getTemplateLoaders() {
//        if(templateLoaders == null){
//            templateLoaders = instantiateList(TemplateLoader.class);
//        }
//        return templateLoaders;
//    }

    private void initializePlugins(){
        if(plugins == null){
            log.debug("Initializing plugins");
            plugins = instantiateList(Plugin.class);
            for(Plugin plugin: plugins){
                log.debug("Initializing plugin: {}", plugin.getClass().getName());
                plugin.initialize(this);
            }
        }
    }

    @Override
    public List<Plugin> getPlugins() {
        return plugins;
    }

//    @Override
//    public Map<String, TemplateModel> getTemplateModels() {
//        if(templateModels == null){
//            templateModels = instantiateMap(TemplateModel.class, null);
//        }
//        return templateModels;
//    }

    @Override
    public PluginManager getPluginManager() {
        return this;
    }

    @Override
    public Renderer createRenderer(Site site) {
        //return new RendererImpl(site, getTemplateLoaders());

        String className = (String) site.getTheme().get("renderer");
        if(className != null){
            Class<Renderer> clazz;
            try {
                clazz = ClassUtils.getClass(site.getClassLoader(), className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getMessage());
            }

            Constructor<?>[] constructors = clazz.getConstructors();
            Constructor<?> theConstructor = null;
            for(Constructor<?> constructor: constructors){
                Class<?>[] types = constructor.getParameterTypes();
                if(types.length == 1 && types[0].equals(Site.class)){
                    theConstructor = constructor;
                    break;
                }
            }

            try {
                if (theConstructor != null) {
                    return (Renderer) theConstructor.newInstance(site);
                } else {
                    Renderer renderer = clazz.newInstance();
                    if(renderer instanceof SiteAware){
                        ((SiteAware) renderer).setSite(site);
                    }
                    if(renderer instanceof ConfigAware){
                        ((ConfigAware) renderer).setConfig(site.getConfig());
                    }
                    return renderer;
                }

            } catch (InstantiationException e) {
                throw new RuntimeException("error instance: " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("error instance: " + e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("error instance: " + e.getTargetException(), e.getTargetException());
            }
        }

        //default: freemarker
        return new FreeMarkerRenderer(site);
    }

    private Renderer newInstance(Class<Renderer> clazz, Site site) {
        try {
            Renderer renderer = clazz.newInstance();
            if(renderer instanceof SiteAware){
                ((SiteAware) renderer).setSite(site);
            }
            if(renderer instanceof ConfigAware){
                ((ConfigAware) renderer).setConfig(site.getConfig());
            }

            return renderer;
        } catch (InstantiationException e) {
            throw new RuntimeException("error instance" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("error instance" + e.getMessage(), e);
        }
    }

    @Override
    public ResourceBuilder createResourceBuilder(String type) {
        if(resourceBuilderClassNames == null){
            resourceBuilderClassNames = (Map<String, String>) configuration.get(ResourceBuilder.class.getName());
            if(resourceBuilderClassNames == null){
                resourceBuilderClassNames = Collections.emptyMap();
            }
        }

        String className = resourceBuilderClassNames.get(type);
        if(className == null){
            className = type;
        }

        return ClassUtils.newInstance(className, site.getClassLoader(), site, site.getConfig());
    }

    public <T> T instantiate(Class<T> clazz){
        ServiceLoader<T> loader = ServiceLoader.load(clazz, site.getClassLoader());
        Iterator<T> iterator = loader.iterator();
        if(iterator.hasNext()){
            return apply(iterator.next());
        }

        String className = (String) configuration.get(clazz.getName());
        if(StringUtils.isBlank(className) || "none".equalsIgnoreCase(className)){
            return null;
        }
        return newInstance(className);
    }

    private <T> T apply(T t) {
        if(t instanceof SiteAware){
            ((SiteAware) t).setSite(site);
        }
        if(t instanceof ConfigAware){
            ((ConfigAware) t).setConfig(site.getConfig());
        }
        return t;
    }

    public <T> T newInstance(String className){
        T t = ClassUtils.newInstance(className, site.getClassLoader(), site, site.getConfig());
        //log.debug("Create instance for '{}': {}", className, t.getClass().getName());
        return t;
    }

    public <T> List<T> instantiateList(Class<T> clazz){
        List<T> list = new ArrayList<T>();
        ServiceLoader<T> loader = ServiceLoader.load(clazz, site.getClassLoader());
        for(T t: loader){
            t = apply(t);
            log.debug("Load instance: {} => {}", clazz.getName(), t.getClass().getName());
            list.add(t);
        }

        Object o = configuration.get(clazz.getName());
        if(o instanceof List){
            List<String> classNames = (List<String>) o;
            if(!classNames.isEmpty()){
                for(String className: classNames){
                    T t = newInstance(className);
                    log.debug("Create instance: {} => {}", clazz.getName(), className);
                    list.add(t);
                }
            }
        }else if (o instanceof Map) {
            Map<String, String> classNames = (Map<String, String>) o;
            if(!classNames.isEmpty()) {
                for (Map.Entry<String, String> entry : classNames.entrySet()) {
                    String name = entry.getKey();
                    String className = entry.getValue();
                    T t = newInstance(className);
                    log.debug("Create instance: {} => {}", clazz.getName(), className);
                    list.add(t);
                }
            }
        }else if(o != null){
            log.warn("Configuration error: {} => {}", clazz.getName(), o);
        }

        return list;
    }

    public <T> Map<String,T> instantiateMap(Class<T> clazz){
        Map<String,T> map = new HashMap<String, T>();
        ServiceLoader<T> loader = ServiceLoader.load(clazz, site.getClassLoader());
        for(T t: loader){
            if(t instanceof Named) {
                t = apply(t);
                log.debug("Load instance for '{}': {} => {}", ((Named) t).getName(), clazz.getName(), t.getClass().getName());
                map.put(((Named) t).getName(), t);
            }
        }

        Object o = configuration.get(clazz.getName());
        if(o instanceof List){
            List<String> classNames = (List<String>) o;
            if(!classNames.isEmpty()){
                for(String className: classNames){
                    T t = newInstance(className);
                    if(t instanceof Named) {
                        log.debug("Create instance for '{}': {} => {}", ((Named) t).getName(), clazz.getName(), className);
                        map.put(((Named) t).getName(), t);
                    }
                }
            }

        }else if (o instanceof Map) {
            Map<String, String> classNames = (Map<String, String>) o;
            if(!classNames.isEmpty()) {
                for (Map.Entry<String, String> entry : classNames.entrySet()) {
                    String name = entry.getKey();
                    String className = entry.getValue();
                    T t = newInstance(className);
                    log.debug("Create instance for '{}': {} => {}", name, clazz.getName(), className);
                    map.put(name, t);
                }
            }
        }else if(o != null){
            log.warn("Configuration error: {} => {}", clazz.getName(), o);
        }

        return map;
    }

    private <T extends Ordered> void sort(List<T> list){
        if(!list.isEmpty()){
            Collections.sort(list, Ordered.COMPARATOR);
        }
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

//    @Override
//    public void registerTemplateModel(String name, TemplateModel model) {
//        getTemplateModels();
//        templateModels.put(name, model);
//    }
//
//    @Override
//    public void registerTemplateLoader(TemplateLoader loader) {
//        getTemplateLoaders();
//        templateLoaders.add(loader);
//    }

    @Override
    public Converter getConverter(Source source) throws RuntimeException {
        getConverters();
        for(Converter c: converters){
            if(c.matches(source)){
                return c;
            }
        }
        throw new RuntimeException("No matched converter: " + source.getSourceEntry().getFile());
    }


    @Override
    public <T> List<T> getObjectList(Class<T> clazz) {
        List<T> list = listMap.get(clazz);
        if(list == null){
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
    public <T> Map<String,T> getObjectMap(Class<T> clazz){
        Map<String,T> map = mapMap.get(clazz);
        if(map == null){
            map = instantiateMap(clazz);
            mapMap.put(clazz, map);
        }
        return map;
    }

    @Override
    public <T> Registry register(Class<T> clazz, String name, T instance){
        getObjectMap(clazz).put(name, instance);
        return this;
    }

    @Override
    public <T> Registry register(Class<T> clazz, Named named){
        return register(clazz, named.getName(), (T)named);
    }

    public static Factory createInstance(Site site){
        String factoryClassName = site.getConfig().get("factory");
        if(StringUtils.isNotBlank(factoryClassName)){
            return ClassUtils.newInstance(factoryClassName, site.getClassLoader(), site, site.getConfig());
        }

        FactoryImpl factory = new FactoryImpl();
        factory.setSite(site);
        return factory;
    }
}
