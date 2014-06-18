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

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Application;
import org.opoo.press.Category;
import org.opoo.press.Converter;
import org.opoo.press.Generator;
import org.opoo.press.Page;
import org.opoo.press.Plugin;
import org.opoo.press.Post;
import org.opoo.press.RelatedPostsFinder;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.SiteBuilder;
import org.opoo.press.SiteConfig;
import org.opoo.press.SlugHelper;
import org.opoo.press.StaticFile;
import org.opoo.press.Tag;
import org.opoo.press.Writable;
import org.opoo.press.converter.IdentityConverter;
import org.opoo.press.highlighter.Highlighter;
import org.opoo.press.plugin.DefaultPlugin;
import org.opoo.press.slug.DefaultSlugHelper;
import org.opoo.press.source.NoFrontMatterException;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.opoo.press.source.SourceEntryLoader;
import org.opoo.press.source.SourceParser;
import org.opoo.press.task.RunnableTask;
import org.opoo.press.task.TaskExecutor;
import org.opoo.press.template.TitleCaseModel;
import org.opoo.press.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.TemplateModel;

/**
 * @author Alex Lin
 *
 */
public class SiteImpl implements Site, SiteBuilder{
	private static final Logger log = LoggerFactory.getLogger(SiteImpl.class);
	private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();
	private static final String LAST_BUILD_FILE_SUFFIX = "_lastbuild.properties";
	
	private SiteConfigImpl config;
	private Map<String, Object> data;
	private File source;
	private File dest;
	private File templates;
	private File assets;
	private File working;
	private File site;
	private List<File> sources;
	
	private String root;
	
	private List<Page> pages;
	private List<Post> posts;
	private List<StaticFile> staticFiles;
	
//	private Map<String, List<Post>> categories;
//	private Map<String, List<Post>> tags;
//	private Map<String, String> categoryNames;
//	private Map<String, String> tagNames;
	
	private Map<String, Category> categories;
	private List<Tag> tags;
	
	private Date time;
	private boolean showDrafts = false;
	
//	private transient List<Generator> generators;
//	private transient SiteFilter siteFilter;
	private Renderer renderer;
//	private List<String> includes;
//	private List<String> excludes;
	private RegistryImpl registry;
	private Locale locale;
	private Highlighter highlighter;
	private SlugHelper slugHelper;
	private String permalink;
	private RelatedPostsFinder relatedPostsFinder;
	
	private File lastBuildInfoFile;
	private TaskExecutor taskExecutor;
	
	SiteImpl(SiteConfigImpl siteConfig) {
		super();
		init(siteConfig);
		reset();
		setup();
	}
	
	private void init(SiteConfigImpl siteConfig){
		this.config = siteConfig;
		
		this.root = (String)config.get("root");
		
		//show drafts
		showDrafts = config.get("show_drafts", false);
		if(showDrafts){
			log.info("+ Show drafts option set 'ON'");
		}
		
		this.permalink = (String) config.get("permalink");
		this.data = new HashMap<String,Object>(config);
		
		//debug option
		boolean debug = config.get("debug", false);
		if(debug){
			for(Map.Entry<String, Object> en: config.entrySet()){
				String name = en.getKey();
				name = StringUtils.leftPad(name, 25);
				log.info(name + ": " + en.getValue());
			}
		}
		
		taskExecutor = new TaskExecutor(config);
	}
	
	private void setupDirs(){
		//File site = null;
		Object siteObject = config.get("site");
		if(siteObject == null){
			throw new IllegalArgumentException("Site directory not set.");
		}
		if(siteObject instanceof File){
			site = (File) siteObject;
		}else{
			String siteDir = (String) siteObject;
			site = new File(siteDir);
		}
		
		if(!site.exists() || !site.isDirectory() || !site.canRead()){
			throw new IllegalArgumentException("Site directory not exists or not a directory: " + site);
		}
		
		source = new File(site, "source");
		if(!source.exists() || !source.isDirectory() || !source.canRead()){
			throw new IllegalArgumentException("Source directory not exists or not a directory.");
		}
		
		@SuppressWarnings("unchecked")
		List<String> sourceDirs = (List<String>) config.get("sources");
		if(sourceDirs != null && !sourceDirs.isEmpty()){
			sources = new ArrayList<File>();
			for(String src: sourceDirs){
				File file = new File(site, src);
				if(file.exists() && file.isDirectory() && file.canRead()){
					sources.add(file);
					log.debug("Find extra source directory: {}", file);
				}else{
					log.warn("Ignored bad extra source directory: {}", file);
				}
			}
		}
		
		templates = new File(site, "templates");
		if(!templates.exists() || !templates.isDirectory()){
			templates = new File(source, "_templates");
		}
		if(!templates.exists() || !templates.isDirectory()){
			throw new IllegalArgumentException("No valid templates directory in site or source.");
		}
		
		assets = new File(site, "assets");
		if(!assets.exists() || !assets.isDirectory()){
			assets = null;
		}
		
		//target directories
		String destDir = (String) config.get("destination");
		if(destDir != null){
			this.dest = new File(destDir);
		}else{
			this.dest = new File(site.getParentFile(), "target/public/" + site.getName());
		}
//		if(!dest.exists()){
//			dest.mkdirs();
//		}

		String workingDir = (String) config.get("working_dir");
		if(workingDir != null){
			this.working = new File(workingDir);
		}else{
			//String tmpdir = System.getProperty("java.io.tmpdir");
			//this.working = new File(tmpdir, "opoopress_cache/" + site.getName());
			this.working = new File(site.getParentFile(), "target/work/" + site.getName());
		}
//		if(!working.exists()){
//			working.mkdirs();
//		}
		
		if(dest.equals(source) || source.getAbsolutePath().startsWith(dest.getAbsolutePath())){
			throw new IllegalArgumentException("Destination directory cannot be or contain the Source directory.");
		}
		
		this.lastBuildInfoFile = new File(working, site.getName() + LAST_BUILD_FILE_SUFFIX);
	}
	
	public void build(){
		reset();
		read();
		generate();
		render();
		cleanup();
		write();
		
		saveLastBuildInfo();
	}

	void reset(){
		this.time = (Date) config.get("time");
		if(time == null){
			time = new Date();
		}
		this.pages = new ArrayList<Page>();
		this.posts = new ArrayList<Post>();
		//在多线程中 add
		this.staticFiles = Collections.synchronizedList(new ArrayList<StaticFile>());
		
		resetCategories();
		resetTags();
	}
	
	void resetCategories(){
		this.categories = new LinkedHashMap<String,Category>();
		@SuppressWarnings("unchecked")
		Map<String,String> names = (Map<String,String>) config.get("category_names");
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
		@SuppressWarnings("unchecked")
		Map<String,String> names = (Map<String, String>) config.get("tag_names");
		if(names == null || names.isEmpty()){
			return;
		}
		
		for(Map.Entry<String, String> en: names.entrySet()){
			 tags.add(new TagImpl(en.getKey(), en.getValue(), this));
		 }
	}
	
	void setup(){
		setupDirs();
		
		String localeString = (String) config.get("locale");
		if(localeString != null){
			locale = LocaleUtils.toLocale(localeString);
			log.debug("Set locale: " + locale);
		}
		
		String highlighterClassName = (String) config.get("highlighter");
		if(highlighterClassName != null){
			highlighter = (Highlighter) ClassUtils.newInstance(highlighterClassName, this);
			log.debug("Set highlighter: " + highlighterClassName);
		}
		
		//slug
		String slugHelperClassName = (String) config.get("slugHelper");
		if(slugHelperClassName == null){
			slugHelper = new DefaultSlugHelper();
			log.debug("Set SlugHelper: " + DefaultSlugHelper.class.getName());
		}else{
			slugHelper = Application.getContext().get(slugHelperClassName, SlugHelper.class);
			if(slugHelper != null){
				log.debug("Set SlugHelper from Context object: " + slugHelper);
			}else{
				slugHelper = (SlugHelper) ClassUtils.newInstance(slugHelperClassName, this);
				log.debug("Set SlugHelper: " + slugHelperClassName);
			}
		}
		
		this.registry = new RegistryImpl(this);
		//register default converter
		this.registry.registerConverter(new IdentityConverter());
		
		//plugins
		new DefaultPlugin().initialize(registry);
		
		@SuppressWarnings("unchecked")
		List<String> pluginClassNames = (List<String>) config.get("plugins");
		if(pluginClassNames != null && !pluginClassNames.isEmpty()){
			for(String className: pluginClassNames){
				Plugin p = (Plugin) ClassUtils.newInstance(className);
				log.debug("Initializing plugin: {}", p.getClass().getName());
				p.initialize(registry);
			}
		}
		
		//Construct RendererImpl after initializing all plugins
		this.renderer = new RendererImpl(this, registry.getTemplateLoaders());
		
		//RelatedPostsFinder 
		String relatedPostsFinderClassName = (String) config.get("relatedPostsFinder");
		if(relatedPostsFinderClassName != null){
			relatedPostsFinder = (RelatedPostsFinder) ClassUtils.newInstance(relatedPostsFinderClassName, this);
			log.debug("Set relatedPostsFinder: {}", relatedPostsFinderClassName);
		}else{
			//Use CosineSililarity
			relatedPostsFinder = new CosineSimilarityRelatedPostsFinder();
			((CosineSimilarityRelatedPostsFinder) relatedPostsFinder).initialize(this);
			log.debug("Set relatedPostsFinder: {}", CosineSimilarityRelatedPostsFinder.class.getName());
		}
	}


	void read(){
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
		
		final SourceEntryLoader loader = Application.getContext().getSourceEntryLoader();
		final SourceParser parser = Application.getContext().getSourceParser();
		
		//load sources and load static files
		
		FileFilter fileFilter = buildFilter();
		List<SourceEntry> list = loader.loadSourceEntries(source, fileFilter);
		if(sources != null && !sources.isEmpty()){
			for(File src: sources){
				List<SourceEntry> tempList = loader.loadSourceEntries(src, fileFilter);
				if(tempList != null && !tempList.isEmpty()){
					list.addAll(tempList);
				}
			}
		}
		
		for(SourceEntry en: list){
			read(en, parser);
		}
		
		Collections.sort(posts);
		Collections.reverse(posts);
		setPostNextOrPrevious(posts);
		
//		sort(categories);
//		sort(tags);
	}
	
	
	/**
	 * @param posts2
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
		if(assets != null){
			log.info("Reading assets ...");
			SourceEntryLoader loader = Application.getContext().getSourceEntryLoader();
			FileFilter fileFilter = buildFilter();
			List<SourceEntry> list = loader.loadSourceEntries(assets, fileFilter);
			for(SourceEntry se: list){
				this.staticFiles.add(new StaticFileImpl(this, se));
			}
		}
	}


	/**
	 * 
	 */
	private void postRead() {
		registry.getSiteFilter().postRead(this);
	}

	/**
	 * @param en
	 */
	private void read(SourceEntry en, SourceParser parser) {
		try {
			Source src = parser.parse(en);
			log.debug("read source " + src.getSourceEntry().getFile());
			
			Map<String, Object> map = src.getMeta();
			String layout = (String) map.get("layout");
			if("post".equals(layout)){
				readPost(src);
			}else{
				pages.add(new PageImpl(this, src));
			}
		} catch (NoFrontMatterException e) {
			this.staticFiles.add(new StaticFileImpl(this, en));
		}
	}
	
	private void readPost(Source src){
		if(isDraft(src.getMeta())){
			if(showDrafts){
				posts.add(new Draft(this, src));
			}
		}else{
			posts.add(new PostImpl(this, src));
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
		@SuppressWarnings("unchecked")
		final List<String> includes = (List<String>) config.get("includes");
		@SuppressWarnings("unchecked")
		final List<String> excludes = (List<String>) config.get("excludes");
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
		for(Generator g: registry.getGenerators()){
			g.generate(this);
		}
		postGenerate();
	}
	
	/**
	 * 
	 */
	private void postGenerate() {
		registry.getSiteFilter().postGenerate(this);
	}

	void render(){
		final Map<String, Object> rootMap = buildRootMap();
		renderer.prepareLayoutWorkingTemplates();
		
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
		
		postRender();
		
	}
	
	/**
	 * @param post
	 */
	private void postConvertPost(Post post) {
		registry.getSiteFilter().postConvertPost(this, post);
	}

	/**
	 * @param page
	 */
	private void postConvertPage(Page page) {
		registry.getSiteFilter().postConvertPage(this, page);
	}

	/**
	 * @param post
	 */
	private void postRenderPost(Post post) {
		registry.getSiteFilter().postRenderPost(this, post);
	}

	/**
	 * 
	 */
	private void postRenderPosts() {
		registry.getSiteFilter().postRenderAllPosts(this);
	}

	/**
	 * @param page
	 */
	private void postRenderPage(Page page) {
		registry.getSiteFilter().postRenderPage(this, page);
	}

	/**
	 * 
	 */
	private void postRenderPages() {
		registry.getSiteFilter().postRenderAllPages(this);
	}

	/**
	 * @deprecated Will be removed in the next main release.
	 */
	private void postRender() {
		registry.getSiteFilter().postRender(this);
	}

	Map<String,Object> buildRootMap(){
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("site", this);
		
//		String rootUrl = (String)config.get("root");
		map.put("root_url", getRoot());
		try{
			String version = Site.class.getPackage().getSpecificationVersion();
			if(StringUtils.isNotBlank(version)){
				map.put("version", version);
			}
		}catch(Exception e){
			//map.put("version", "unkown_version");
		}
		if(map.get("version") == null){
			map.put("version", "unkown_version");
		}
		
		Map<String, TemplateModel> models = registry.getTemplateModels();
		if(models != null && !models.isEmpty()){
			map.putAll(models);
		}
		
		TitleCaseModel model = new TitleCaseModel(this);
		map.put("titleCase", model);
		map.put("titlecase", model);
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
		
		if(log.isDebugEnabled()){
			log.debug("Files in target: " + destFiles.size());
			log.debug("Assets file in src: " + files.size());
		}
		
		if(log.isDebugEnabled()){
			log.debug("Files in target: " + destFiles.size());
			log.debug("Assets file in src: " + files.size());
		}
		
		//find obsolete files
		for(File file: files){
			destFiles.remove(file);
		}
//		destFiles.removeAll(files);
		
		if(log.isDebugEnabled()){
			log.debug("Files in target will be deleted: " + destFiles.size());
		}

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
					if(IS_DEBUG_ENABLED){
						log.debug("Delete file " + file);
					}
				}
			});
		}

		//call post cleanup
		postCleanup();
	}
	
	/**
	 * @param dest2
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
		registry.getSiteFilter().postWrite(this);
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
	public SiteConfig getConfig() {
		return config;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Site#getSouce()
	 */
	@Override
	public File getSource() {
		return source;
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
		return data.get(name);
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
	public File getAssets() {
		return assets;
	}

	@Override
	public File getWorking() {
		return working;
	}

	@Override
	public Converter getConverter(Source source) {
		return registry.getConverter(source);
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
	 * @see org.opoo.press.Site#getHighlighter()
	 */
	@Override
	public Highlighter getHighlighter() {
		return highlighter;
	}
	
	public RelatedPostsFinder getRelatedPostsFinder(){
		return relatedPostsFinder;
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
		return slugHelper.toSlug(tagName);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteHelper#toNicename(java.lang.String)
	 */
	@Override
	public String toNicename(String categoryName) {
		return slugHelper.toSlug(categoryName);
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
		for(Tag tag: tags){
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
	public File getSite() {
		return site;
	}
	
	/**
	 * @return the showDrafts
	 */
	public boolean showDrafts() {
		return showDrafts;
	}
	
	private void saveLastBuildInfo(){
		Properties props = new Properties();
		props.setProperty("build_time", String.valueOf(System.currentTimeMillis()));
		props.setProperty("show_drafts", String.valueOf(showDrafts));
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(lastBuildInfoFile);
			props.store(writer, "OpooPress last build information for site: " + site.getName());
		} catch (IOException e) {
			throw new RuntimeException("Write last build info exception", e);
		} finally{
			IOUtils.closeQuietly(writer);
		}
	}
	
	public BuildInfo getLastBuildInfo(){
		if(lastBuildInfoFile == null || !lastBuildInfoFile.exists() 
				|| !lastBuildInfoFile.isFile() || !lastBuildInfoFile.canRead()){
			log.debug("No build info file.");
			return null;
		}
		FileReader reader = null;
		try {
			reader = new FileReader(lastBuildInfoFile);
			Properties props = new Properties();
			props.load(reader);
			
			String val1 = props.getProperty("show_drafts");
			String val2 = props.getProperty("build_time");
			if(StringUtils.isBlank(val2) || StringUtils.isBlank(val1)){
				log.debug("No show_drafts or build_time in properties file: " + lastBuildInfoFile);
				return null;
			}
			
			BuildInfoImpl info = new BuildInfoImpl();
			info.buildTime = Long.parseLong(val2);
			info.showDrafts = Boolean.parseBoolean(val1);
			return info;
		} catch (IOException e) {
			throw new RuntimeException("Read last build info exception", e);
		} finally{
			IOUtils.closeQuietly(reader);
		}
	}
	
	static class BuildInfoImpl implements BuildInfo{
		private long buildTime;
		private boolean showDrafts;
		
		public long getBuildTime() {
			return buildTime;
		}
		public boolean showDrafts() {
			return showDrafts;
		}
	}
}
