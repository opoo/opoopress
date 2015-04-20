package org.opoo.press.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.opoo.press.Highlighter;
import org.opoo.press.Site;
import org.opoo.press.SlugHelper;
import org.opoo.press.Source;
import org.opoo.press.SourceEntryLoader;
import org.opoo.press.SourceManager;
import org.opoo.press.SourceParser;
import org.opoo.press.highlighter.SyntaxHighlighter;
import org.opoo.press.impl.SourcePage;
import org.opoo.press.impl.SourcePost;
import org.opoo.press.slug.ChineseToPinyinSlugHelper;
import org.opoo.press.slug.DefaultSlugHelper;
import org.opoo.press.source.SourceEntryLoaderImpl;
import org.opoo.press.source.SourceManagerImpl;
import org.opoo.press.source.SourceParserImpl;

import java.lang.reflect.Constructor;

/**
 */
public class SiteModule extends AbstractModule implements Module{
    private final Site site;
    public SiteModule(Site site){
        this.site = site;
    }

    @Override
    protected void configure() {
        bind(Site.class).toInstance(site);
        bind(SourceEntryLoader.class).to(SourceEntryLoaderImpl.class).in(Singleton.class);
        bind(SourceParser.class).to(SourceParserImpl.class).in(Singleton.class);
        bind(SourceManager.class).to(SourceManagerImpl.class).in(Singleton.class);
        bind(SlugHelper.class).annotatedWith(Names.named("zh_CN")).to(ChineseToPinyinSlugHelper.class).in(Singleton.class);
        bind(SlugHelper.class).annotatedWith(Names.named("default")).to(DefaultSlugHelper.class).in(Singleton.class);
        bind(Highlighter.class).to(SyntaxHighlighter.class).in(Singleton.class);

        try {
            bind(Constructor.class).annotatedWith(Names.named("Page:page"))
                    .toInstance(SourcePage.class.getConstructor(Site.class, Source.class));
            bind(Constructor.class).annotatedWith(Names.named("Page:post"))
                    .toInstance(SourcePost.class.getConstructor(Site.class, Source.class));

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
