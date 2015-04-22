package org.opoo.press.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
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
import org.opoo.press.SlugHelper;
import org.opoo.press.Source;
import org.opoo.press.SourceEntryLoader;
import org.opoo.press.SourceManager;
import org.opoo.press.SourceParser;
import org.opoo.press.Tag;
import org.opoo.press.impl.SourcePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 */
public class GuiceFactory implements Factory,ObjectFactory{
    private static final Logger log = LoggerFactory.getLogger(GuiceFactory.class);

    private Injector injector;
    private Site site;

    public void setSite(Site site){
        this.site = site;

        DefaultSiteModule module = new DefaultSiteModule(site);
        injector = Guice.createInjector(module);
    }

    @Override
    public SourceEntryLoader getSourceEntryLoader() {
        return injector.getInstance(SourceEntryLoader.class);
    }

    @Override
    public SourceParser getSourceParser() {
        return injector.getInstance(SourceParser.class);
    }

    @Override
    public SourceManager getSourceManager() {
        return injector.getInstance(SourceManager.class);
    }

    @Override
    public Highlighter getHighlighter() {
        return injector.getInstance(Highlighter.class);
    }

    @Override
    public SlugHelper getSlugHelper() {
        if(site.getLocale() != null){
            String name = site.getLocale().toString();
            try {
                return injector.getInstance(Key.get(SlugHelper.class, Names.named(name)));
            } catch (Exception e) {
                log.debug("No locale SlugHelper found: {}", name);
            }
        }
        return injector.getInstance(Key.get(SlugHelper.class, Names.named("default")));
    }

    @Override
    public RelatedPostsFinder getRelatedPostsFinder() {
        return injector.getInstance(RelatedPostsFinder.class);
    }

    @Override
    public Page createPage(Site site, Source source, String layout) {
        Constructor<Page> constructor = injector.getInstance(Key.get(Constructor.class, Names.named("Page:" + layout)));
        return newInstance(constructor, site, source);
    }

    @Override
    public Page createPage(Site site, Source source) {
//        return createPage(site, source, "page");
        return new SourcePage(site, source);
    }

    @Override
    public List<Plugin> getPlugins() {
        return null;
    }

    @Override
    public PluginManager getPluginManager() {
        return null;
    }

    @Override
    public Renderer getRenderer() {
        String className = (String) site.getTheme().get("renderer");
        if(className == null){
            className = "freemarker";
        }
        return injector.getInstance(Key.get(Renderer.class, Names.named(className)));
    }

    @Override
    public PaginationUpdater getPaginationUpdater() {
        return injector.getInstance(PaginationUpdater.class);
    }

    @Override
    public ResourceBuilder createResourceBuilder(String type) {
        return injector.getInstance(Key.get(ResourceBuilder.class, Names.named(type)));
    }

    @Override
    public Category createCategory(String categoryMeta,
                                   String slug, String categoryName, Category parent) {
        String name = "Category-with-parent:-" + categoryMeta;
        Constructor<Category> constructor = injector.getInstance(Key.get(Constructor.class, Names.named(name)));
        return newInstance(constructor, slug, categoryName, parent);
    }

    private <T> T newInstance(Constructor<T> constructor, Object... args){
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException e) {
            throw new RuntimeException("Create instance failed: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Create instance failed: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Create instance failed: " + e.getTargetException().getMessage(),
                    e.getTargetException());
        }
    }

    @Override
    public Category createCategory(String categoryMeta, String slug, String categoryName) {
        return null;
    }

    @Override
    public Category createCategory(String categoryMeta, String slugOrName) {
        return null;
    }

    @Override
    public Tag createTag(String tagMeta, String slug, String name) {
        return null;
    }

    @Override
    public Tag createTag(String tagMeta, String slugOrName) {
        return null;
    }

    @Override
    public <T> T getInstance(Class<T> clazz, String name) {
        return injector.getInstance(Key.get(clazz, Names.named(name)));
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public <T> T createInstance(Class<T> clazz, String hint) {
        return injector.getInstance(Key.get(clazz, Names.named(hint)));
    }

    @Override
    public <T> T createInstance(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public <T> T constructInstance(Class<T> clazz, String hint, Object... args) {
        Constructor constructor = injector.getInstance(Key.get(Constructor.class, Names.named(hint)));
        return (T) newInstance(constructor, args);
    }
}
