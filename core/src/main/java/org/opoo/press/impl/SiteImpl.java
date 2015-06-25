/*
 * Copyright 2013 Alex Lin.
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

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Collection;
import org.opoo.press.Converter;
import org.opoo.press.Factory;
import org.opoo.press.Generator;
import org.opoo.press.NoFrontMatterException;
import org.opoo.press.Observer;
import org.opoo.press.Page;
import org.opoo.press.Post;
import org.opoo.press.ProcessorsProcessor;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.SiteBuilder;
import org.opoo.press.SiteConfig;
import org.opoo.press.Source;
import org.opoo.press.SourceEntry;
import org.opoo.press.SourceEntryLoader;
import org.opoo.press.SourceEntryVisitor;
import org.opoo.press.SourceParser;
import org.opoo.press.StaticFile;
import org.opoo.press.Theme;
import org.opoo.press.ThemeCompiler;
import org.opoo.press.Writable;
import org.opoo.press.source.CachedSourceParserWrapper;
import org.opoo.press.task.RunnableTask;
import org.opoo.press.task.TaskExecutor;
import org.opoo.press.util.StaleUtils;
import org.opoo.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * @author Alex Lin
 *
 */
public class SiteImpl implements Site, SiteBuilder{
	private static final Logger log = LoggerFactory.getLogger(SiteImpl.class);

	private SiteConfigImpl config;
	private Map<String, Object> data;
	private File dest;
	private File templates;
	private File working;
	private File basedir;
	private ValidDirList sources;
	private ValidDirList assets;
	private String root;
	private List<StaticFile> staticFiles;
	private Date time;
	private boolean showDrafts = false;
	private Renderer renderer;
	private Locale locale;
	private TaskExecutor taskExecutor;
	private Theme theme;
//	private boolean setup = false;
	private ProcessorsProcessor processors;
	private ClassLoader classLoader;
	private Factory factory;
	private String dateFormatPattern;
	private Map<String,Collection> collections;
	private List<Page> allPages;

    private CacheManager cacheManager;
    private Cache<String,Source> sourceCache;
    private Cache<String,SourceEntry> staticFileSourceEntryCache;
    private Cache<String,String> contentCache;


	public SiteImpl(SiteConfigImpl siteConfig) {
		super();
		
		this.config = siteConfig;
		
		this.data = new HashMap<String,Object>(/*config*/);
		
		this.basedir = config.getBasedir();
		this.root = config.get("root", "");
//		this.permalink = config.get("permalink");
		this.showDrafts = config.get("show_drafts", false);
		boolean debug = config.get("debug", false);
		
		if(showDrafts){
			log.info("+ Show drafts option set 'ON'");
		}
		if(debug){
			for(Map.Entry<String, Object> en: config.entrySet()){
				String name = en.getKey();
				name = StringUtils.leftPad(name, 25);
				log.info(name + ": " + en.getValue());
			}
		}
		//theme
		theme = createTheme();

		//templates
		templates = theme.getTemplates();
		log.debug("Template directory: {}", templates);
		
		//sources
		sources = new ValidDirList();
		sources.addDir(theme.getSource());
		List<String> sourcesConfig = config.get("source_dirs");
		sources.addDirs(basedir, sourcesConfig);
		log.debug("Source directories: {}", sources);
		
		//assets
		assets = new ValidDirList();
		assets.addDir(theme.getAssets());
		List<String> assetsConfig = config.get("asset_dirs");
		assets.addDirs(basedir, assetsConfig);
		log.debug("Assets directories: {}", assets);

		//target directory
		String destDir = config.get("dest_dir");
		this.dest = PathUtils.appendBaseIfNotAbsolute(basedir, destDir);
		log.debug("Destination directory: {}", dest);

		//working directory
		String workingDir = config.get("work_dir");
		this.working = PathUtils.appendBaseIfNotAbsolute(basedir, workingDir);
		log.debug("Working directory: {}", working);
		
		reset();
		setup();
	}

	private Theme createTheme() {
		String name = config.get("theme", "default");
		File themes = new File(config.getBasedir(), "themes");
		File themeDir = PathUtils.appendBaseIfNotAbsolute(themes, name);
		if(!themeDir.exists() || !themeDir.isDirectory()){
			throw new IllegalArgumentException("Theme directory not exists or not valid, please install theme first: "
					+ themeDir);
		}

//		PathUtils.checkDir(themeDir, PathUtils.Strategy.THROW_EXCEPTION_IF_NOT_EXISTS);
		compileTheme(themeDir);
		return new ThemeImpl(themeDir, this);
	}

	private void compileTheme(File themeDir){
		ThemeCompiler themeCompiler = config.get("theme.compiler");
		if(themeCompiler != null){
			log.debug("Compile theme by '{}'", themeCompiler.getClass().getName());
			themeCompiler.compile(themeDir);
		}else{
			log.debug("no theme compiler found.");
		}
	}

	public void build(){
		build(false);
	}

	public void build(boolean force){
		if(force){
			log.info("force build.");
			buildInternal();
			return;
		}

		if(StaleUtils.isStale(this, false)){
			buildInternal();
			return;
		}

		// only asset file(s) changed.
		List<File> staleAssets = StaleUtils.getStaleAssets(this);
		if(staleAssets != null){
			for(File staleAsset: staleAssets){
				//copy asset directory to destination directory
				log.info("Copying stale asset: {}...", staleAsset);
				try {
					FileUtils.copyDirectory(staleAsset, dest, buildFilter());
					StaleUtils.saveLastBuildInfo(this);
					return;
				} catch (IOException e) {
					throw new RuntimeException("Copy stale asset exception: " + staleAsset, e);
				}
			}
		}

		log.info("Nothing to build - all site output files are up to date.");
	}

	@Override
	public void clean() throws Exception{
		log.info("Cleaning destination directory " + dest);
		FileUtils.deleteDirectory(dest);

		log.info("Cleaning working directory " + working);
		FileUtils.deleteDirectory(working);
	}

	private void buildInternal(){
//		if(!setup){
//			setup = true;
//			setup();
//		}

		reset();
        prepare();
        read();
		generate();
		convert();
		render();
		cleanup();
		write();
        close();

		StaleUtils.saveLastBuildInfo(this);
	}

    void prepare() {
        boolean cache = config.get("cache", false);
        if(cache){
            cacheManager = Caching.getCachingProvider().getCacheManager();
            sourceCache = cacheManager.getCache("sources");
            staticFileSourceEntryCache = cacheManager.getCache("static-file-source-entries");
            contentCache = cacheManager.getCache("contents");

            if(sourceCache == null){
                throw new IllegalArgumentException("'sources' cache not defined");
            }
            if(staticFileSourceEntryCache == null){
                throw new IllegalArgumentException("'static-file-source-entries' cache not defined");
            }
            if(contentCache == null){
                throw new IllegalArgumentException("'contents' cache not defined");
            }
            data.put("contentCache", contentCache);
        }
    }

    void close() {
        if(cacheManager != null){
            data.remove("contentCache");
            contentCache.clear();

            cacheManager.close();
            cacheManager = null;
        }
    }

    void reset(){
		this.time = config.get("time", new Date());
		//Call #add() in multi-threading
		this.allPages = Collections.synchronizedList(new ArrayList<Page>());

		this.collections = new LinkedHashMap<String, Collection>();

		//Call #add() in multi-threading
		this.staticFiles = Collections.synchronizedList(new ArrayList<StaticFile>());
	}
	

	void setup(){
		//ensure source not in destination
		for(File source: sources){
			source = PathUtils.canonical(source);
			if(dest.equals(source) || source.getAbsolutePath().startsWith(dest.getAbsolutePath())){
				throw new IllegalArgumentException("Destination directory cannot be or contain the Source directory.");
			}
		}

		//locale
		String localeString = config.get("locale");
		if(localeString != null){
			locale = LocaleUtils.toLocale(localeString);
			log.debug("Set locale: " + locale);
		}

		//date_format
		dateFormatPattern =  config.get("date_format");
		if(dateFormatPattern == null){
			dateFormatPattern = "yyyy-MM-dd";
		}else if("ordinal".equals(dateFormatPattern)){
			dateFormatPattern = "MMM d yyyy";
		}

		//object instances
		classLoader = createClassLoader(config, theme);
		taskExecutor = new TaskExecutor(config);
		factory = FactoryImpl.createInstance(this);

		processors = new ProcessorsProcessor(factory.getPluginManager().getProcessors());

		//Construct RendererImpl after initializing all plugins
		renderer = factory.getRenderer();

		processors.postSetup(this);
	}

	private ClassLoader createClassLoader(SiteConfig config, Theme theme) {
		log.debug("Create site ClassLoader.");

		ClassLoader parent = SiteImpl.class.getClassLoader();
		if(parent == null){
			parent = ClassLoader.getSystemClassLoader();
		}


		String sitePluginDir = config.get("plugin_dir");
		String themePluginDir = theme.get("plugin_dir");

		List<File> classPathEntries = new ArrayList<File>(2);

		if(StringUtils.isNotBlank(sitePluginDir)){
			File sitePlugins = PathUtils.canonical(new File(config.getBasedir(), sitePluginDir));
			addClassPathEntries(classPathEntries, sitePlugins);
		}

		if(StringUtils.isNotBlank(themePluginDir)){
			File themePlugins = PathUtils.canonical(new File(theme.getPath(), themePluginDir));
			addClassPathEntries(classPathEntries, themePlugins);
		}

		//theme classes
		File themeClasses = new File(theme.getPath(), "target/classes");
		File themeSrc = new File(theme.getPath(), "src");
		if(themeSrc.exists() && themeClasses.exists() && themeClasses.isDirectory()){
			classPathEntries.add(themeClasses);
		}

		//theme target/plugins
		File themeTargetPlugins = new File(theme.getPath(), "target/plugins");
		if(themeTargetPlugins.exists() && themeTargetPlugins.list().length > 0){
			addClassPathEntries(classPathEntries, themeTargetPlugins);
		}

		if(classPathEntries.isEmpty()){
			log.info("No custom classpath entries.");
			return parent;
		}

		URL[] urls = new URL[classPathEntries.size()];

		try {
			for(int i = 0 ; i < classPathEntries.size() ; i++){
				urls[i] = classPathEntries.get(i).toURI().toURL();
			}
		}catch (MalformedURLException e){
			throw new RuntimeException(e);
		}

		return new URLClassLoader(urls, parent);
	}

	private void addClassPathEntries(List<File> classPathEntries, File dir){
		if(dir.exists()){
			File[] files = dir.listFiles(new ValidPluginClassPathEntryFileFilter());
			if (files != null && files.length > 0) {
				classPathEntries.addAll(Arrays.asList(files));
			}
		}
	}

	void read(){
		log.info("Reading sources...");

        final FileFilter fileFilter = buildFilter();
        final SourceEntryLoader sourceEntryLoader = factory.getSourceEntryLoader();
        final SourceParser sourceParser = getSourceParser();//factory.getSourceParser();

        final SourceEntryVisitor sourceVisitor = new SourceEntryVisitor() {
            @Override
            public void visit(SourceEntry sourceEntry) {
                readSource(sourceEntry, sourceParser);
            }
        };
        final SourceEntryVisitor staticFileVisitor = new SourceEntryVisitor() {
            @Override
            public void visit(SourceEntry sourceEntry) {
                log.debug("Reading static file {} => [{}]", sourceEntry.getFile(), sourceEntry.getPath());
                staticFiles.add(new StaticFileImpl(SiteImpl.this, sourceEntry));
            }
        };

        List<Runnable> tasks = Lists.newArrayList();

        for(final File src: sources){
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    log.debug("Walk source: {}", src);
                    sourceEntryLoader.walkSourceTree(src, fileFilter, sourceVisitor);
                }
            });
        }


        for(final File assetDir: assets){
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    log.debug("Walk asset: {}", assetDir);
                    sourceEntryLoader.walkSourceTree(assetDir, fileFilter, staticFileVisitor);
                }
            });
        }

        taskExecutor.run(tasks);

		processors.postRead(this);

        log.debug("Read {} pages.", allPages.size());
        if(log.isTraceEnabled()){
            for(Page page: allPages) {
                System.out.println("==> " + page.getSource().getSourceEntry().getFile());
            }
        }
    }


    private SourceParser getSourceParser() {
        SourceParser sourceParser = factory.getSourceParser();

        if(cacheManager != null){
            log.debug("Use {} as SourceParser.", CachedSourceParserWrapper.class.getName());
            sourceParser = new CachedSourceParserWrapper(sourceParser, sourceCache, staticFileSourceEntryCache);
        }
        return sourceParser;
    }

	private void readSource(SourceEntry en, SourceParser parser) {
        try {

            Source src = parser.parse(en);

			SourceEntry sourceEntry = src.getSourceEntry();
			log.debug("Reading source {} => [{}]", sourceEntry.getFile(), sourceEntry.getPath());

			Map<String, Object> map = src.getMeta();
			String layout = (String) map.get("layout");
			boolean draft = isDraft(map);
			if(!draft || (draft && showDrafts)) {
				Page page = factory.createPage(this, src, layout);
				allPages.add(page);

				//path from basedir to source file
				String pathFromBasedirToFile = PathUtils.getRelativePath(basedir,  sourceEntry.getSourceDirectory())
						+ sourceEntry.getPath() + "/" + sourceEntry.getName();
				page.set("pathFromBasedirToFile", pathFromBasedirToFile);

				processors.postRead(this, page);
			}
		} catch (NoFrontMatterException e) {
			this.staticFiles.add(new StaticFileImpl(this, en));
		}
	}


	private boolean isDraft(Map<String, Object> meta){
		if(!meta.containsKey("published")){
			return false;
		}
		Boolean b = (Boolean)meta.get("published");
		return !b.booleanValue();
	}
	
	FileFilter buildFilter(){
		final List<String> includes = config.get("includes");
		final List<String> excludes = config.get("excludes");
		return new FileFilter(){
			@Override
			public boolean accept(File file) {
				String name = file.getName();
				if(includes != null && includes.contains(name)){
					return true;
				}
				if(excludes != null && excludes.contains(name)){
					return false;
				}
				char firstChar = name.charAt(0);
				if(firstChar == '.' || firstChar == '_' || firstChar == '#'){
					return false;
				}
				char lastChar = name.charAt(name.length() - 1);
				if(lastChar == '~'){
					return false;
				}
				if(file.isHidden()){
					return false;
				}
				return true;
			}
		};
	}
	
	
	void generate(){
        log.info("Generating...");
		for(Generator g: factory.getPluginManager().getGenerators()){
			g.generate(this);
		}
		processors.postGenerate(this);
	}


	void convert(){
		log.info("Converting {} pages...", allPages.size());
		taskExecutor.run(allPages, new RunnableTask<Page>() {
			public void run(Page page) {
				log.debug("Converting page: {}", page.getUrl());
				page.convert();
				processors.postConvert(SiteImpl.this, page);
			}
		});
		processors.postConvert(this);
	}

	void render(){
		processors.preRender(this);
		final Map<String, Object> rootMap = buildRootMap();
		renderer.prepare();

		log.info("Rendering {} pages...", allPages.size());
		taskExecutor.run(allPages, new RunnableTask<Page>() {
            public void run(Page page) {
                log.debug("Rendering page: {}", page.getUrl());

                page.render(rootMap);
                processors.postRender(SiteImpl.this, page);
            }
        });
		processors.postRender(this);
	}


	Map<String,Object> buildRootMap(){
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("site", this);
		map.put("root_url", getRoot());
		map.put("basedir", getRoot());
		map.put("opoopress", config.get("opoopress"));
		
		map.put("theme", theme);
		return map;
	}
	
	/**
	 * 
	 */
	void cleanup() {
		log.info("cleanup...");
		final List<File> destFiles = getAllDestFiles(dest);
		List<File> files = new ArrayList<File>();

		for(StaticFile staticFile: staticFiles){
			files.add(staticFile.getOutputFile(dest));
		}
		
		log.debug("Files in target: {}", destFiles.size());
		log.debug("Assets file in src: {}", files.size());

		
		//find obsolete files
		for(File file: files){
			destFiles.remove(file);
		}
//		destFiles.removeAll(files);
		
		log.debug("Files in target will be deleted: {}", destFiles.size());

		//delete obsolete files
		if(!destFiles.isEmpty()){
//			for(File destFile: destFiles){
//				//FileUtils.deleteQuietly(destFile);
//				if(IS_DEBUG_ENABLED){
//					log.debug("Delete file " + destFile);
//				}
//			}
			
			taskExecutor.run(destFiles, new RunnableTask<File>() {
				public void run(File file) {
					FileUtils.deleteQuietly(file);
					log.trace("File deleted: {}", file);
				}
			});
		}

		//call post cleanup
		processors.postCleanup(this);
	}
	
	/**
	 * @param dest
	 * @return
	 */
	private List<File> getAllDestFiles(File dest) {
		List<File> files = new ArrayList<File>();
		if(dest != null && dest.exists()){
			listDestFiles(files, dest);
		}
		return files;
	}
	
	private void listDestFiles(List<File> files, File dir){
		File[] list = dir.listFiles();
		for(File f: list){
			if(f.isFile()){
				files.add(f);
			}else if(f.isDirectory()){
				listDestFiles(files, f);
			}
		}
	}


	void write(){
		dest.mkdirs();
		
		List<Writable> list = new ArrayList<Writable>();
		list.addAll(allPages);
		if(!staticFiles.isEmpty()){
			list.addAll(staticFiles);
		}

        log.info("Writing {} files to {}...", list.size(), dest);
		
		taskExecutor.run(list, new RunnableTask<Writable>() {
			public void run(Writable o) {
				o.write(dest);
			}
		});

		processors.postWrite(this);
	}


    /**
	 * @return the pages
	 */
	public List<Page> getPages() {
		Collection collection = collections.get("page");
		if(collection != null){
			return (List<Page>) collection.getPages();
		}
		return Collections.emptyList();
	}

	/**
	 * @return the posts
	 */
	public List<Post> getPosts() {
		Collection collection = collections.get("post");
		if(collection != null){
			return (List<Post>) collection.getPages();
		}
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getConfig()
	 */
	@Override
	public SiteConfig getConfig() {
		return config;
	}

	@Override
	public List<File> getSources(){
		return sources;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getDestination()
	 */
	@Override
	public File getDestination() {
		return dest;
	}
	
	public List<StaticFile> getStaticFiles(){
		return staticFiles;
	}

	@Override
	public List<Page> getAllPages() {
		return allPages;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getTime()
	 */
	@Override
	public Date getTime() {
		return time;
	}

	public Object get(String name){
		if(data.containsKey(name)) {
			return data.get(name);
		}

		if(config.containsKey(name)){
			return config.get(name);
		}

		if(theme != null){
			return theme.get(name);
		}

		return null;
	}
	
	public void set(String name, Object value){
		data.put(name, value);
		//MapUtils.put(data, name, value);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getRenderer()
	 */
	@Override
	public Renderer getRenderer() {
		return renderer;
	}

	@Override
	public File getTemplates() {
		return templates;
	}

	@Override
	public List<File> getAssets() {
		return assets;
	}

	@Override
	public File getWorking() {
		return working;
	}

	@Override
	public Converter getConverter(Source source) {
		return factory.getPluginManager().getConverter(source);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getRoot()
	 */
	@Override
	public String getRoot() {
		return root;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getLocale()
	 */
	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public String getPermalink(String layout) {
		if(layout == null){
			return null;
		}
		return config.get("permalink_" + layout);
	}


	/* (non-Javadoc)
	 * @see org.opoo.press.SiteHelper#toSlug(java.lang.String)
	 */
	@Override
	public String toSlug(String tagName) {
		return factory.getSlugHelper().toSlug(tagName);
	}


	/**
	 * @return the site
	 */
	@Override
	public File getBasedir() {
		return basedir;
	}

	@Override
	public ClassLoader getClassLoader(){
		return classLoader;
	}

	@Override
	public Factory getFactory(){
		return factory;
	}

	@Override
	public Observer getObserver() {
		return new SiteObserver(this);
	}


	/**
	 * @return the showDrafts
	 */
	public boolean showDrafts() {
		return showDrafts;
	}


	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getTheme()
	 */
	@Override
	public Theme getTheme() {
		return theme;
	}
	
	ProcessorsProcessor getProcessors(){
		return processors;
	}

	@Override
	public Map<String,Collection> getCollections(){
		return collections;
	}

	@Override
	public String formatDate(Date date) {
		if(date != null){
			if(locale != null){
				return new SimpleDateFormat(dateFormatPattern, locale).format(date);
			}else{
				return new SimpleDateFormat(dateFormatPattern).format(date);
			}
		}
		return null;
	}


	static class ValidDirList extends ArrayList<File>{
		private static final long serialVersionUID = 6306507738477638252L;
		public ValidDirList addDir(File dir){
			if(PathUtils.isValidDirectory(dir)){
				add(dir);
			}
			return this;
		}
		
		public ValidDirList addDir(File base, String path){
			return addDir(new File(base, path));
		}
		
		public ValidDirList addDirs(File base, List<String> paths){
			for(String path: paths){
				addDir(base, path);
			}
			return this;
		}
	}

	static class ValidPluginClassPathEntryFileFilter implements FileFilter {
		/* (non-Javadoc)
         * @see java.io.FileFilter#accept(java.io.File)
         */
		@Override
		public boolean accept(File file) {
			String name = file.getName();
			char firstChar = name.charAt(0);
			if (firstChar == '.' || firstChar == '_' || firstChar == '#') {
				return false;
			}
			char lastChar = name.charAt(name.length() - 1);
			if (lastChar == '~') {
				return false;
			}
			if (file.isHidden()) {
				return false;
			}
			if (file.isDirectory()) {
				return true;
			}
			if (file.isFile()) {
				name = name.toLowerCase();
				if (name.endsWith(".jar") || name.endsWith(".zip")) {
					return true;
				}
			}
			return false;
		}
	}
}
