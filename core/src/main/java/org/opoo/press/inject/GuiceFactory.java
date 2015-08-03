package org.opoo.press.inject;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.opoo.press.Category;
import org.opoo.press.Factory;
import org.opoo.press.Highlighter;
import org.opoo.press.ObjectFactory;
import org.opoo.press.Page;
import org.opoo.press.PaginationUpdater;
import org.opoo.press.Plugin;
import org.opoo.press.PluginManager;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.Renderer;
import org.opoo.press.ResourceBuilder;
import org.opoo.press.Site;
import org.opoo.press.SiteAware;
import org.opoo.press.SlugHelper;
import org.opoo.press.Source;
import org.opoo.press.SourceDirectoryWalker;
import org.opoo.press.SourceManager;
import org.opoo.press.SourceWalker;
import org.opoo.press.Tag;
import org.opoo.press.impl.CategoryImpl;
import org.opoo.press.impl.SourcePage;
import org.opoo.press.impl.TagImpl;
import org.opoo.press.plugin.PluginManagerImpl;
import org.opoo.press.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ServiceLoader;

/**
 */
public class GuiceFactory implements Factory, ObjectFactory, SiteAware {
    private static final Logger log = LoggerFactory.getLogger(GuiceFactory.class);

    private Injector injector;
    private Site site;
    private PluginManager pluginManager;

    @Override
    public void setSite(Site site) {
        this.site = site;

        Module module = ServiceModules.loadFromClasspath(site);
        injector = Guice.createInjector(module);

        pluginManager = new PluginManagerImpl(site);
    }

    @Override
    public SourceDirectoryWalker getSourceDirectoryWalker() {
        return getInstance(SourceDirectoryWalker.class);
    }

    @Override
    public List<SourceWalker> getSourceWalkers() {
        return pluginManager.getObjectList(SourceWalker.class);
    }

    @Override
    public SourceManager getSourceManager() {
        return getInstance(SourceManager.class);
    }

    @Override
    public Highlighter getHighlighter() {
        return getInstance(Highlighter.class);
    }

    @Override
    public SlugHelper getSlugHelper() {
        if (site.getLocale() != null) {
            String name = site.getLocale().toString();
            try {
                return getInstance(SlugHelper.class, name);
            } catch (Exception e) {
                log.debug("No locale SlugHelper found: {}", name);
            }
        }
        return getInstance(SlugHelper.class, "default");
    }

    @Override
    public RelatedPostsFinder getRelatedPostsFinder() {
        return getInstance(RelatedPostsFinder.class);
    }

    @Override
    public Page createPage(Site site, Source source, String layout) {
        try {
            return constructInstance(Page.class, "Page:" + layout, site, source);
        } catch (Exception e) {
            return new SourcePage(site, source);
        }
    }

    @Override
    public Page createPage(Site site, Source source) {
//        return createPage(site, source, "page");
        return new SourcePage(site, source);
    }

    @Override
    public List<Plugin> getPlugins() {
        ServiceLoader<Plugin> loader = ServiceLoader.load(Plugin.class, site.getClassLoader());
        return Lists.newArrayList(loader);
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public Renderer getRenderer() {
        String className = (String) site.getTheme().get("renderer");
        if (className == null) {
            className = "freemarker";
        }
//        return injector.getInstance(Key.getPage(Renderer.class, Names.named(className)));
        try {
            return constructInstance(Renderer.class, "Render:" + className, site);
        } catch (Exception e) {
            return getInstance(Renderer.class, className);
        }
    }

    @Override
    public PaginationUpdater getPaginationUpdater() {
        return getInstance(PaginationUpdater.class);
    }

    @Override
    public ResourceBuilder createResourceBuilder(String type) {
        return getInstance(ResourceBuilder.class, type);
    }

    @Override
    public Category createCategory(String categoryMeta,
                                   String slug, String categoryName, Category parent) {
        try {
            return constructInstance(Category.class, "CategoryWithParent:" + categoryMeta, slug, categoryName, parent);
        } catch (Exception e) {
            return new CategoryImpl(slug, categoryName, parent);
        }
    }


    @Override
    public Category createCategory(String categoryMeta, String slug, String categoryName) {
        try {
            return constructInstance(Category.class, "Category:" + categoryMeta, slug, categoryName);
        } catch (Exception e) {
            return new CategoryImpl(slug, categoryName);
        }
    }

    @Override
    public Category createCategory(String categoryMeta, String slugOrName) {
        String slug = getSlugHelper().toSlug(slugOrName);
        return createCategory(categoryMeta, slug, slugOrName);
    }

    @Override
    public Tag createTag(String tagMeta, String slug, String name) {
        try {
            return constructInstance(Tag.class, "Tag:" + tagMeta, slug, name);
        } catch (Exception e) {
            return new TagImpl(slug, name);
        }
    }

    @Override
    public Tag createTag(String tagMeta, String slugOrName) {
        String slug = getSlugHelper().toSlug(slugOrName);
        return createTag(tagMeta, slug, slugOrName);
    }

    @Override
    public <T> T getInstance(Class<T> clazz, String name) {
        return createInstance(clazz, name);
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return createInstance(clazz);
    }

    @Override
    public <T> T createInstance(Class<T> clazz, String hint) {
        return apply(injector.getInstance(Key.get(clazz, Names.named(hint))));
    }

    @Override
    public <T> T createInstance(Class<T> clazz) {
        return apply(injector.getInstance(clazz));
    }

    @Override
    public <T> T constructInstance(Class<T> clazz, String hint, Object... args) {
        Constructor<T> constructor = injector.getInstance(Key.get(Constructor.class, Names.named(hint)));
        return newInstance(constructor, args);
    }

    private <T> T apply(T t) {
        return ClassUtils.apply(t, site);
    }

    private <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            return apply(constructor.newInstance(args));
        } catch (InstantiationException e) {
            throw new RuntimeException("Create instance failed: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Create instance failed: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Create instance failed: " + e.getTargetException().getMessage(),
                    e.getTargetException());
        }
    }
}
