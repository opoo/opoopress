package org.opoo.press.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.opoo.press.Category;
import org.opoo.press.Highlighter;
import org.opoo.press.PaginationUpdater;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.ResourceBuilder;
import org.opoo.press.Site;
import org.opoo.press.SlugHelper;
import org.opoo.press.Source;
import org.opoo.press.SourceDirectoryWalker;
import org.opoo.press.SourceManager;
import org.opoo.press.collection.filter.LayoutFilter;
import org.opoo.press.highlighter.SyntaxHighlighter;
import org.opoo.press.impl.CategoryImpl;
import org.opoo.press.impl.CosineSimilarityRelatedPostsFinder;
import org.opoo.press.impl.SourcePage;
import org.opoo.press.impl.SourcePost;
import org.opoo.press.impl.TagImpl;
import org.opoo.press.pagination.ConfigurablePaginationUpdater;
import org.opoo.press.renderer.FreeMarkerRenderer;
import org.opoo.press.renderer.VelocityRenderer;
import org.opoo.press.resource.CompassBuilder;
import org.opoo.press.resource.CssBuilder;
import org.opoo.press.resource.JsBuilder;
import org.opoo.press.slug.DefaultSlugHelper;
import org.opoo.press.slug.SimpleSlugHelper;
import org.opoo.press.source.SourceDirectoryWalkerImpl;
import org.opoo.press.source.SourceManagerImpl;

import java.lang.reflect.Constructor;

/**
 */
public class DefaultSiteModule extends AbstractModule implements Module {
    private Site site;

    DefaultSiteModule(Site site) {
        this.site = site;
    }

    @Override
    protected void configure() {
        bind(Site.class).toInstance(site);
        bind(SourceDirectoryWalker.class).to(SourceDirectoryWalkerImpl.class).in(Singleton.class);
        bind(SourceManager.class).to(SourceManagerImpl.class).in(Singleton.class);
        //bind(SlugHelper.class).annotatedWith(Names.named("zh_CN")).to(ChineseToPinyinSlugHelper.class).in(Singleton.class);
        bind(SlugHelper.class).annotatedWith(Names.named("zh_CN")).to(SimpleSlugHelper.class);
        bind(SlugHelper.class).annotatedWith(Names.named("default")).to(DefaultSlugHelper.class).in(Singleton.class);
        bind(Highlighter.class).to(SyntaxHighlighter.class).in(Singleton.class);
        bind(RelatedPostsFinder.class).to(CosineSimilarityRelatedPostsFinder.class).in(Singleton.class);

//        bind(Renderer.class).annotatedWith(Names.named("freemarker")).to(FreeMarkerRenderer.class).in(Singleton.class);
//        bind(Renderer.class).annotatedWith(Names.named("velocity")).to(VelocityRenderer.class).in(Singleton.class);
        bind(PaginationUpdater.class).to(ConfigurablePaginationUpdater.class).in(Singleton.class);
        bind(ResourceBuilder.class).annotatedWith(Names.named("css")).to(CssBuilder.class);
        bind(ResourceBuilder.class).annotatedWith(Names.named("js")).to(JsBuilder.class);
        bind(ResourceBuilder.class).annotatedWith(Names.named("compass")).to(CompassBuilder.class);

        try {
            bind(Constructor.class).annotatedWith(Names.named("Page:page"))
                    .toInstance(SourcePage.class.getConstructor(Site.class, Source.class));
            bind(Constructor.class).annotatedWith(Names.named("Page:post"))
                    .toInstance(SourcePost.class.getConstructor(Site.class, Source.class));


            bind(Constructor.class).annotatedWith(Names.named("Category:post-category"))
                    .toInstance(CategoryImpl.class.getConstructor(String.class, String.class));
            bind(Constructor.class).annotatedWith(Names.named("CategoryWithParent:post-category"))
                    .toInstance(CategoryImpl.class.getConstructor(String.class, String.class, Category.class));

            bind(Constructor.class).annotatedWith(Names.named("Tag:post-tag"))
                    .toInstance(TagImpl.class.getConstructor(String.class, String.class));

            bind(Constructor.class).annotatedWith(Names.named("Render:freemarker"))
                    .toInstance(FreeMarkerRenderer.class.getConstructor(Site.class));
            bind(Constructor.class).annotatedWith(Names.named("Render:velocity"))
                    .toInstance(VelocityRenderer.class.getConstructor(Site.class));

            //org.opoo.press.collection.Filter-layout: org.opoo.press.collection.filter.LayoutFilter
            bind(Constructor.class).annotatedWith(Names.named("Filter:layout"))
                    .toInstance(LayoutFilter.class.getConstructor(String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
