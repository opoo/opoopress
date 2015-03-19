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

import freemarker.template.TemplateModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Category;
import org.opoo.press.Config;
import org.opoo.press.Converter;
import org.opoo.press.Factory;
import org.opoo.press.Generator;
import org.opoo.press.Observer;
import org.opoo.press.Page;
import org.opoo.press.Post;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.SiteBuilder;
import org.opoo.press.StaticFile;
import org.opoo.press.Tag;
import org.opoo.press.Theme;
import org.opoo.press.ThemeCompiler;
import org.opoo.press.Writable;
import org.opoo.press.processor.ProcessorsProcessor;
import org.opoo.press.NoFrontMatterException;
import org.opoo.press.Source;
import org.opoo.press.SourceEntry;
import org.opoo.press.SourceEntryLoader;
import org.opoo.press.SourceParser;
import org.opoo.press.task.RunnableTask;
import org.opoo.press.task.TaskExecutor;
import org.opoo.press.util.StaleUtils;
import org.opoo.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author Alex Lin
 *
 */
public class SiteImpl implements Site, SiteBuilder{
	private static final Logger log = LoggerFactory.getLogger(SiteImpl.class);

	private ConfigImpl config;
	private Map<String, Object> data;
	private File dest;
	private File templates;
	private File working;
	private File basedir;
	private ValidDirList sources;
	private ValidDirList assets;
	
	private String root;
	
	private List<Page> pages;
	private List<Post> posts;
	private List<StaticFile> staticFiles;
	
	private Map<String, Category> categories;
	private List<Tag> tags;
	
	private Date time;
	private boolean showDrafts = false;
	
//	private transient List<Generator> generators;
	private Renderer renderer;
//	private List<String> includes;
//	private List<String> excludes;
//	private RegistryImpl registry;
	private Locale locale;
	//private Highlighter highlighter;
	//private SlugHelper slugHelper;
	private String permalink;
	//private RelatedPostsFinder relatedPostsFinder;
	
//	private File lastBuildInfoFile;
	private TaskExecutor taskExecutor;
	
	private Theme theme;
	//private ThemeManager themeManager;
	//private PluginManager pluginManager;
//	private boolean setup = false;
	private ProcessorsProcessor processors;

	//private Provider provider;
	private ClassLoader classLoader;
	private Factory factory;

	//private SourceEntryLoader sourceEntryLoader;
	//private SourceParser sourceParser;

	private String dateFormatPattern;

	public SiteImpl(ConfigImpl siteConfig) {
		super();
		
		this.config = siteConfig;
		
		this.data = new HashMap<String,Object>(/*config*/);
		
		this.basedir = config.getBasedir();
		this.root = config.get("root", "");
		this.permalink = config.get("permalink");
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
		read();
		generate();
		render();
		cleanup();
		write();

		StaleUtils.saveLastBuildInfo(this);
	}

	void reset(){
		this.time = config.get("time", new Date());
		this.pages = new ArrayList<Page>();
		this.posts = new ArrayList<Post>();
		//Call #add() in multi-threading
		this.staticFiles = Collections.synchronizedList(new ArrayList<StaticFile>());
		
//		resetCategories();
//		resetTags();
	}
	
	void resetCategories(){
		this.categories = new LinkedHashMap<String,Category>();
		Map<String,String> names = config.get("category_names");
		if(names == null || names.isEmpty()){
			return;
		}
		//sort name
		names = new TreeMap<String,String>(names);
		for(Map.Entry<String, String> en: names.entrySet()){
			String path = en.getKey();
			String name = en.getValue();
			
			String nicename = path;
			String parentPath = null;
			int index = path.lastIndexOf('.');
			if(index != -1){
				nicename = path.substring(index + 1);
				parentPath = path.substring(0, index);
			}
			
			Category parent = null;
			if(parentPath != null){
				parent = categories.get(parentPath);
				if(parent == null){
					throw new IllegalArgumentException("Parent category not found: " + parentPath);
				}
			}
			CategoryImpl category = new CategoryImpl(nicename, name, parent, this);
			categories.put(path, category);
		}
	}
	
	void resetTags(){
		this.tags = new ArrayList<Tag>();
		Map<String,String> names = config.get("tag_names");
		if(names == null || names.isEmpty()){
			return;
		}
		
		for(Map.Entry<String, String> en: names.entrySet()){
			 tags.add(new TagImpl(en.getKey(), en.getValue(), this));
		 }
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
		renderer = factory.createRenderer(this);
	}

	private ClassLoader createClassLoader(Config config, Theme theme) {
		log.debug("Create site ClassLoader.");

		ClassLoader parent = SiteImpl.class.getClassLoader();
		if(parent == null){
			parent = ClassLoader.getSystemClassLoader();
		}


		String sitePluginDir = config.get("plugin_dir");
		String themePluginDir = (String) theme.get("plugin_dir");

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

//	@Deprecated
//	public <T> T instantiate(Class<T> clazz){
//		return instantiate(clazz, null);
//	}
//	@Deprecated
//	public <T> T instantiate(Class<T> clazz, String hint){
//		String className = provider.getClassName(clazz.getName(), hint);
//		if(StringUtils.isBlank(className) || "none".equalsIgnoreCase(className)){
//			return null;
//		}
//		return instantiate(className);
//	}
//	@Deprecated
//	public <T> T instantiate(String className){
//		T t = ClassUtils.newInstance(className, classLoader, this, config);
//		log.debug("New '{}' instance: {}", className, t);
//		return t;
//	}


	void read(){
		//read categories and tags from configuration
		resetCategories();
		resetTags();
		
		Runnable t1 = new Runnable(){
			public void run() {
				readSources();
			}
		};
		
		Runnable t2 = new Runnable(){
			public void run() {
				readStaticFiles();
			}
		};
		
		taskExecutor.run(t1, t2);
		
		postRead();
	}
	
	private void readSources(){
		log.info("Reading sources ...");
		final SourceEntryLoader sourceEntryLoader = factory.getSourceEntryLoader();
		final SourceParser sourceParser = factory.getSourceParser();

		//load sources and load static files
		FileFilter fileFilter = buildFilter();
		List<SourceEntry> list = new ArrayList<SourceEntry>();
		for(File src: sources){
			List<SourceEntry> tempList = sourceEntryLoader.loadSourceEntries(src, fileFilter);
			if(tempList != null && !tempList.isEmpty()){
				list.addAll(tempList);
			}
		}
		
		for(SourceEntry en: list){
			read(en, sourceParser);
		}
		
		Collections.sort(posts);
		Collections.reverse(posts);
		setPostNextOrPrevious(posts);
		
//		sort(categories);
//		sort(tags);
	}
	
	
	/**
	 * @param posts
	 */
	private void setPostNextOrPrevious(List<Post> posts) {
		/*
		for(int i = 0 ; i < posts.size() ; i++){
			Post post = posts.get(i);
			if(i > 0){
				post.setNext(posts.get(i - 1));
			}
			if(i < posts.size() - 1){
				post.setPrevious(posts.get(i + 1));
			}
		}*/
		Iterator<Post> it = posts.iterator();
		Post prev = null;
		Post curr = null;
		while(it.hasNext()){
			curr = it.next();
			if(prev != null){
				prev.setPrevious(curr);
				curr.setNext(prev);
			}
			prev = curr;
		}
	}
	
	private void readStaticFiles(){
		log.info("Reading assets ...");
		final SourceEntryLoader sourceEntryLoader = factory.getSourceEntryLoader();
		FileFilter fileFilter = buildFilter();
		for(File assetDir: assets){
			List<SourceEntry> tempList = sourceEntryLoader.loadSourceEntries(assetDir, fileFilter);
			for(SourceEntry se: tempList){
				log.debug("read static file {}", se.getFile());
				staticFiles.add(new StaticFileImpl(this, se));
			}
		}
	}

	/**
	 * 
	 */
	private void postRead() {
		processors.postRead(this);
	}


	private void read(SourceEntry en, SourceParser parser) {
		try {
			Source src = parser.parse(en);
			log.debug("read source {}", src.getSourceEntry().getFile());
			
			Map<String, Object> map = src.getMeta();
			String layout = (String) map.get("layout");
			if("post".equals(layout)){
				readPost(src);
			}else{
				pages.add(factory.createPage(this, src));
			}
		} catch (NoFrontMatterException e) {
			this.staticFiles.add(new StaticFileImpl(this, en));
		}
	}
	
	private void readPost(Source src){
		if(isDraft(src.getMeta())){
			if(showDrafts){
				posts.add(factory.createDraft(this, src));
			}
		}else{
			posts.add(factory.createPost(this, src));
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
		for(Generator g: factory.getPluginManager().getGenerators()){
			g.generate(this);
		}
		postGenerate();
	}
	
	/**
	 * 
	 */
	private void postGenerate() {
		processors.postGenerate(this);
	}

	void render(){
		final Map<String, Object> rootMap = buildRootMap();
		renderer.prepare();
		
//		for(Post post: posts){
//			post.convert();
//			postConvertPost(post);
//			
//			post.render(rootMap);
//			postRenderPost(post);
//		}
		log.info("Rendering {} posts...", posts.size());
		taskExecutor.run(posts, new RunnableTask<Post>(){
			public void run(Post post) {
				post.convert();
				postConvertPost(post);
//				
				post.render(rootMap);
				postRenderPost(post);
			}});
		postRenderPosts();
		
//		for(Page page: pages){
//			page.convert();
//			postConvertPage(page);
//			
//			page.render(rootMap);
//			postRenderPage(page);
//		}
		
		log.info("Rendering {} pages...", pages.size());
		taskExecutor.run(pages, new RunnableTask<Page>(){
			public void run(Page page) {
				page.convert();
				postConvertPage(page);
				
				page.render(rootMap);
				postRenderPage(page);
			}
		});
		postRenderPages();
	}
	
	/**
	 * @param post
	 */
	private void postConvertPost(Post post) {
		processors.postConvertPost(this, post);
	}

	/**
	 * @param page
	 */
	private void postConvertPage(Page page) {
		processors.postConvertPage(this, page);
	}

	/**
	 * @param post
	 */
	private void postRenderPost(Post post) {
		processors.postRenderPost(this, post);
	}

	/**
	 * 
	 */
	private void postRenderPosts() {
		processors.postRenderAllPosts(this);
	}

	/**
	 * @param page
	 */
	private void postRenderPage(Page page) {
		processors.postRenderPage(this, page);
	}

	/**
	 * 
	 */
	private void postRenderPages() {
		processors.postRenderAllPages(this);
	}


	Map<String,Object> buildRootMap(){
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("site", this);
		map.put("root_url", getRoot());
		map.put("basedir", getRoot());
		map.put("opoopress", config.get("opoopress"));
		
//		Map<String, TemplateModel> models = factory.getTemplateModels();
//		if(models != null && !models.isEmpty()){
//			map.putAll(models);
//		}
		
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
		
//		for(Post post: posts){
//			files.add(post.getOutputFile(dest));
//		}
//		for(Page page: pages){
//			files.add(page.getOutputFile(dest));
//		}
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
					log.debug("File deleted: {}", file);
				}
			});
		}

		//call post cleanup
		postCleanup();
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

	/**
	 * 
	 */
	private void postCleanup() {
		processors.postCleanup(this);
	}

	void write(){
		log.info("Writing {} posts, {} pages, and {} static files ...", 
				posts.size(), pages.size(), staticFiles.size());
		
		if(!dest.exists()){
			dest.mkdirs();
		}
		
//		log.info("Writing " + posts.size() + " posts");
//		for(Post post: posts){
//			post.write(dest);
//		}
//		
//		log.info("Writing " + pages.size() + " pages");
//		for(Page page: pages){
//			page.write(dest);
//		}
//		
//		if(!staticFiles.isEmpty()){
//			log.info("Copying " + staticFiles.size() + " static files");
//			for(StaticFile sf: staticFiles){
//				sf.write(dest);
//			}
//		}
		
		List<Writable> list = new ArrayList<Writable>();
		list.addAll(posts);
		list.addAll(pages);
		if(!staticFiles.isEmpty()){
			list.addAll(staticFiles);
		}
		
		taskExecutor.run(list, new RunnableTask<Writable>() {
			public void run(Writable o) {
				o.write(dest);
			}
		});
		
//		if(assets != null){
//			try {
//				log.info("Copying 1 assets directory");
//				log.debug("Copying assets...");
//				FileUtils.copyDirectory(assets, dest, buildFilter());
//				
//				log.debug("All asset files copied.");
//			} catch (IOException e) {
//				log.error("Copy assets error", e);
//			}
//		}
		postWrite();
	}

	/**
	 * 
	 */
	private void postWrite() {
		processors.postWrite(this);
	}

	/**
	 * @return the pages
	 */
	public List<Page> getPages() {
		return pages;
	}

	/**
	 * @return the posts
	 */
	public List<Post> getPosts() {
		return posts;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getConfig()
	 */
	@Override
	public Config getConfig() {
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


	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getCategories()
	 */
	@Override
	public List<Category> getCategories() {
		return new CategoriesList(categories);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getTags()
	 */
	@Override
	public List<Tag> getTags() {
		return tags;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteHelper#buildCanonical(java.lang.String)
	 */
	@Override
	public String buildCanonical(String url) {
		/*
		String canonical = (String) config.get("url");
		String permalink = (String) config.get("permalink");
		String pageUrl = url;
		if(permalink != null && permalink.endsWith(".html")){
			canonical += pageUrl;
		}else{
			canonical += StringUtils.removeEnd(pageUrl, "index.html");
		}
		return canonical;
		*/
		return url;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteHelper#toSlug(java.lang.String)
	 */
	@Override
	public String toSlug(String tagName) {
		return factory.getSlugHelper().toSlug(tagName);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteHelper#toNicename(java.lang.String)
	 */
	@Override
	public String toNicename(String categoryName) {
		return factory.getSlugHelper().toSlug(categoryName);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteHelper#getCategory(java.lang.String)
	 */
	@Override
	public Category getCategory(String categoryNameOrNicename) {
		if(categories == null || categories.isEmpty()){
			return null;
		}
		//If path equals
		if(categories.containsKey(categoryNameOrNicename)){
			return categories.get(categoryNameOrNicename);
		}
		for(Category category: new ArrayList<Category>(categories.values())){
			if(category.isNameOrNicename(categoryNameOrNicename)){
				return category;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteHelper#getTag(java.lang.String)
	 */
	@Override
	public Tag getTag(String tagNameOrSlug) {
		if(tags == null || tags.isEmpty()){
			return null;
		}
		for(Tag tag: new ArrayList<Tag>(tags)){
			if(tag.isNameOrSlug(tagNameOrSlug)){
				return tag;
			}
		}
		return null;
	}
	
	private static class CategoriesList extends AbstractList<Category>{
		private final List<Category> list = new ArrayList<Category>();
		private final Map<String, Category> categories;
		
		private CategoriesList(Map<String, Category> categories) {
			this.categories = categories;
			//this.list = new ArrayList<Category>(categories.values());
			for(Category category: categories.values()){
				if(!category.getPosts().isEmpty()){
					list.add(category);
				}
			}
		}

		@Override
		public boolean add(Category category){
			categories.put(category.getPath(), category);
			return list.add(category);
		}
		
		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public Category get(int index) {
			return list.get(index);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return list.size();
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getPermalink()
	 */
	@Override
	public String getPermalink() {
		return permalink;
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
