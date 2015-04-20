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
import org.opoo.press.Category;
import org.opoo.press.Factory;
import org.opoo.press.Highlighter;
import org.opoo.press.Named;
import org.opoo.press.ObjectFactory;
import org.opoo.press.Page;
import org.opoo.press.PaginationUpdater;
import org.opoo.press.Plugin;
import org.opoo.press.PluginManager;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.Renderer;
import org.opoo.press.ResourceBuilder;
import org.opoo.press.Site;
import org.opoo.press.SlugHelper;
import org.opoo.press.Source;
import org.opoo.press.SourceEntryLoader;
import org.opoo.press.SourceManager;
import org.opoo.press.SourceParser;
import org.opoo.press.Tag;
import org.opoo.press.highlighter.SyntaxHighlighter;
import org.opoo.press.pagination.ConfigurablePaginationUpdater;
import org.opoo.press.plugin.PluginManagerImpl;
import org.opoo.press.renderer.FreeMarkerRenderer;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Alex Lin
 */
public class FactoryImpl extends PluginManagerImpl implements Factory, PluginManager, ObjectFactory {
    private static final Logger log = LoggerFactory.getLogger(FactoryImpl.class);

    private Site site;
    private Map<String,Object> configuration = new LinkedHashMap<String, Object>();;

    private SourceEntryLoader sourceEntryLoader;
    private SourceParser sourceParser;
    private SourceManager sourceManager;
    private Highlighter highlighter;
    private SlugHelper slugHelper;
    private RelatedPostsFinder relatedPostsFinder;
    private PaginationUpdater paginationUpdater;
    private List<Plugin> plugins;

    private Map<String,Object> instances = new HashMap<String,Object>();


    public FactoryImpl(Site site){
        super(site);
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
            sourceEntryLoader = getInstance(SourceEntryLoader.class);
            if(sourceEntryLoader == null){
                sourceEntryLoader = new SourceEntryLoaderImpl();
            }
        }
        return sourceEntryLoader;
    }

    @Override
    public SourceParser getSourceParser() {
        if(sourceParser == null){
            sourceParser = getInstance(SourceParser.class);
            if(sourceParser == null){
                sourceParser = new SourceParserImpl();
            }
        }
        return sourceParser;
    }

    @Override
    public SourceManager getSourceManager() {
        if(sourceManager == null){
            sourceManager = getInstance(SourceManager.class);
            if(sourceManager == null){
                sourceManager = new SourceManagerImpl();
            }
        }
        return sourceManager;
    }

    @Override
    public Highlighter getHighlighter() {
        if(highlighter == null){
            highlighter = getInstance(Highlighter.class);
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
                slugHelper = createInstance(SlugHelper.class);
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
            relatedPostsFinder = getInstance(RelatedPostsFinder.class);
            if(relatedPostsFinder == null){
                relatedPostsFinder = new NoOpRelatedPostsFinder();
            }
        }
        return relatedPostsFinder;
    }

    @Override
    public Page createPage(Site site, Source source) {
        return createPage(site, source, (String) source.getMeta().get("layout"));
    }

    @Override
    public Page createPage(Site site, Source source, String layout) {
        Page page = constructInstance(Page.class, layout,
                new Class[]{Site.class, Source.class},
                new Object[]{site, source});

        if(page != null){
            return page;
        }

        if("post".equalsIgnoreCase(layout)){
            if(log.isDebugEnabled()){
                log.debug("Create post using PostImpl as default: {}", source.getSourceEntry().getFile());
            }
            return new SourcePost(site, source);
        }

        if(log.isDebugEnabled()){
            log.debug("Create page using PageImpl class as default: {}", source.getSourceEntry().getFile());
        }
        return new SourcePage(site, source);
    }


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

    @Override
    public PluginManager getPluginManager() {
        return this;
    }

    @Override
    public Renderer getRenderer() {
        ServiceLoader<Renderer> loader = ServiceLoader.load(Renderer.class, site.getClassLoader());
        Iterator<Renderer> iterator = loader.iterator();
        if(iterator.hasNext()){
            return iterator.next();
        }

        String className = (String) site.getTheme().get("renderer");
        if(className != null){
            Class<Renderer> clazz;
            try {
                clazz = ClassUtils.getClass(site.getClassLoader(), className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e.getMessage());
            }

            Constructor<?> theConstructor = null;
            try {
                theConstructor = clazz.getConstructor(Site.class);
            } catch (NoSuchMethodException e) {
                //ignore
            }

            try {
                if (theConstructor != null) {
                    return (Renderer) theConstructor.newInstance(site);
                } else {
                    Renderer renderer = clazz.newInstance();
                    return apply(renderer);
                }

            } catch (InstantiationException e) {
                throw new RuntimeException("error instance: " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("error instance: " + e.getMessage(), e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("error instance: " + e.getTargetException().getMessage(),
                        e.getTargetException());
            }
        }
        //if custom render not specified
        //default: freemarker
        return new FreeMarkerRenderer(site);
    }

    @Override
    public PaginationUpdater getPaginationUpdater() {
        if(paginationUpdater == null){
            paginationUpdater = getInstance(PaginationUpdater.class);
            if(paginationUpdater == null){
                paginationUpdater = new ConfigurablePaginationUpdater();
            }
        }
        return paginationUpdater;
    }

    @Override
    public ResourceBuilder createResourceBuilder(String type) {
        ResourceBuilder builder = createInstance(ResourceBuilder.class, type);
        if(builder == null){
            builder = ClassUtils.newInstance(type, site.getClassLoader(), site, site.getConfig());
        }
        return builder;
   }

    @Override
    public Category createCategory(String categoryMeta, String slug, String categoryName, Category parent) {
        Category category = constructInstance(Category.class, categoryMeta,
                new Class[]{String.class, String.class, Category.class},
                new Object[]{slug, categoryName, parent});

        if(category != null){
            return category;
        }

        return new CategoryImpl(slug, categoryName, parent);
    }

    @Override
    public Category createCategory(String categoryMeta, String slug, String categoryName) {
        Category category = constructInstance(Category.class, categoryMeta,
                new Class[]{String.class, String.class},
                new Object[]{slug, categoryName});

        if(category != null){
            return category;
        }

        return new CategoryImpl(slug, categoryName);
    }



    @Override
    public Category createCategory(String categoryMeta, String slugOrName) {
        String slug = getSlugHelper().toSlug(slugOrName);
        return createCategory(categoryMeta, slug, slugOrName);
    }

    @Override
    public Tag createTag(String tagMeta, String slug, String name) {
        Tag tag = constructInstance(Tag.class, tagMeta,
                new Class[]{String.class, String.class},
                new Object[]{slug, name});

        if(tag != null){
            return tag;
        }


        return new TagImpl(slug, name);
    }

    @Override
    public Tag createTag(String tagMeta, String slugOrName) {
        log.debug("Create tag: {} {}", tagMeta, slugOrName);
        String slug = getSlugHelper().toSlug(slugOrName);
        return createTag(tagMeta, slug, slugOrName);
    }


    @Override
    public <T> T getInstance(Class<T> clazz, String name) {
        String cacheKey = clazz.getName();
        if(name != null){
            cacheKey += "-" + name;
        }

        if(instances.containsKey(cacheKey)){
            return (T) instances.get(cacheKey);
        }else{
            T o = createInstance(clazz, name);
            instances.put(cacheKey, o);
            return o;
        }
    }

    @Override
    public <T> T getInstance(Class<T> clazz){
        return getInstance(clazz, null);
    }

    @Override
    public <T> T createInstance(Class<T> clazz, final String hint) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz, site.getClassLoader());
        if(hint == null){
            Iterator<T> iterator = loader.iterator();
            if(iterator.hasNext()){
                return apply(iterator.next());
            }
        }else{
            T t = null;
            for(T tt: loader){
                if(t instanceof Named && hint.equals(((Named) t).getName())){
                    t = tt;
                    break;
                }
            }

            if(t != null){
                log.debug("Find instance in ServiceLoader: {}", clazz.getName());
                return apply(t);
            }
        }

        String classKey = clazz.getName();
        if(hint != null){
            classKey += "-" + hint;
        }

        String className = (String) configuration.get(classKey);
        if(StringUtils.isBlank(className) || "none".equalsIgnoreCase(className)){
            return null;
        }
        return newInstance(className);
    }

    @Override
    public <T> T createInstance(Class<T> clazz) {
        return createInstance(clazz, null);
    }

    @Override
    public <T> T constructInstance(Class<T> clazz, String hint, Object... args) {
        return constructInstance(clazz, hint, null, args);
    }


    public <T> T constructInstance(Class<T> clazz, String hint, Class<?>[] parameterTypes, Object[] args) {
        String classKey = clazz.getName();
        if(hint != null){
            classKey += "-" + hint;
        }

        String className = (String) configuration.get(classKey);
        if(StringUtils.isBlank(className) || "none".equalsIgnoreCase(className)){
            return null;
        }

        return ClassUtils.constructInstance(className, site.getClassLoader(), parameterTypes, args);
    }


    public <T> T newInstance(String className){
        T t = ClassUtils.newInstance(className, site.getClassLoader(), site, site.getConfig());
        //log.debug("Create instance for '{}': {}", className, t.getClass().getName());
        return t;
    }

    @Override
    public <T> List<T> instantiateList(Class<T> clazz){
        List<T> list = super.instantiateList(clazz);

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

    @Override
    public <T> Map<String,T> instantiateMap(Class<T> clazz){
        Map<String,T> map = super.instantiateMap(clazz);

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


    public static Factory createInstance(Site site){
        String factoryClassName = site.getConfig().get("factory");
        if(StringUtils.isNotBlank(factoryClassName)){
            return ClassUtils.newInstance(factoryClassName, site.getClassLoader(), site, site.getConfig());
        }

        //default factory
        return new FactoryImpl(site);
    }
}
