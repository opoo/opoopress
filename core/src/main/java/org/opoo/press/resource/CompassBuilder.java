/*
 * Copyright 2014 Alex Lin.
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
package org.opoo.press.resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileEntry;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.internal.BiVariableMap;
import org.opoo.press.Observer;
import org.opoo.press.ResourceBuilder;
import org.opoo.press.file.Result;
import org.opoo.press.file.Watchable;
import org.opoo.press.file.WatchableDirectory;
import org.opoo.util.MapUtils;
import org.opoo.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Alex Lin
 */
public class CompassBuilder implements ResourceBuilder, Observer {
    private static final Logger log = LoggerFactory.getLogger(CompassBuilder.class);
    public static final String DEFAULT_CONFIG_FILE_NAME = "config.rb";
    public static final String CACHE_FILE_SUFFIX = ".cache";

    private File themePath;
    private File configFile;

    private File sassPath;
    private File cssPath;
    private Watchable sassWatchable;
    private FileEntry configFileEntry;

    @Override
    public void init(File resourceBaseDirectory, Map<String, Object> config) {
        this.themePath = resourceBaseDirectory;

        String fileName = (String) MapUtils.get(config, "configFile", DEFAULT_CONFIG_FILE_NAME);
        configFile = new File(resourceBaseDirectory, fileName);

        if (!configFile.exists() || !configFile.canRead()) {
            throw new IllegalArgumentException("Compass config file not exists or invalid: " + configFile);
        }
    }

    protected boolean shouldBuild() {
        prepareSassAndCssPaths();

        long configFileLastModified = configFile.lastModified();

        List<File> partialSassFiles = PathUtils.listFiles(sassPath, new PartialSassFilenameFilter(), true);
        File lastModifiedPartialSassFile = getLastModifiedFile(partialSassFiles);
        long partialSassFileLastModified = lastModifiedPartialSassFile.lastModified();
//		log.debug("Partial sass file last modified: {}", new Date(partialSassFileLastModified));

        int sassPathLength = sassPath.getPath().length() + 1;
        List<File> mainSassFiles = PathUtils.listFiles(sassPath, new MainSassFilenameFilter(), true);
        for (File sassFile : mainSassFiles) {
            String relativeSassFilePath = sassFile.getPath().substring(sassPathLength);
//			log.debug("Checking sass file: {}", relativeSassFilePath);

            String cssFilePath = FilenameUtils.removeExtension(relativeSassFilePath) + ".css";
            File cssFile = new File(cssPath, cssFilePath);
            log.debug("Checking {} => {}", sassFile, cssFile);

            if (!cssFile.exists()) {
                log.debug("css file '{}' not eixsts, need compile.", cssFile);
                return true;
            }

            long cssFileLastModified = cssFile.lastModified();

            if (configFileLastModified > cssFileLastModified) {
                log.debug("css file '{}' is older than compass config file, need compile.", cssFile);
                return true;
            }

            if (sassFile.lastModified() > cssFileLastModified) {
                log.debug("css file '{}' is older than sass file '{}', need compile.", cssFile, sassFile);
                return true;
            }

            if (partialSassFileLastModified > cssFileLastModified) {
                log.debug("css file '{}' is older than sass partial '{}', need compile.", cssFile, lastModifiedPartialSassFile);
                return true;
            }
        }

        return false;
    }


    private File getLastModifiedFile(List<File> files) {
        File lastModifiedFile = null;
        for (File f : files) {
            if (lastModifiedFile == null) {
                lastModifiedFile = f;
            } else if (f.lastModified() > lastModifiedFile.lastModified()) {
                lastModifiedFile = f;
            }
        }
        return lastModifiedFile;
    }

    private void prepareSassAndCssPaths() {
        if (sassPath != null && cssPath != null) {
            return;
        }

        File cacheFile = getCacheFile();//new File(themePath, configFile.getName() + CACHE_FILE_SUFFIX);
        Properties cache = null;
        if (!cacheFile.exists() || configFile.lastModified() > cacheFile.lastModified()) {
            log.debug("Cache file not exists or older than config file, create cache now: {}", cacheFile);
            cache = createCache(configFile, cacheFile);
        } else {
            cache = loadCache(cacheFile);
        }

        String sassDir = cache.getProperty("sass_dir", "sass");
        String cssDir = cache.getProperty("css_dir", "assets/stylesheets");

        sassPath = new File(themePath, sassDir);
        cssPath = new File(themePath, cssDir);
    }

    private File getCacheFile() {
        String name = configFile.getName() + CACHE_FILE_SUFFIX;
        if (name.charAt(0) != '.') {
            name = "." + name;
        }
        return new File(themePath, name);
    }

    private Properties createCache(File configFile, File cacheFile) {
        ScriptingContainer container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
        container.runScriptlet(PathType.ABSOLUTE, configFile.getAbsolutePath());

        @SuppressWarnings("unchecked")
        BiVariableMap<String, Object> varMap = container.getVarMap();
        @SuppressWarnings("unchecked")
        Set<Map.Entry<String, Object>> entrySet = varMap.entrySet();

        Properties props = new Properties();
        for (Map.Entry<String, Object> en : entrySet) {
            if (en.getValue() instanceof String) {
                props.setProperty(en.getKey(), (String) en.getValue());
            }
        }

        //save
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(cacheFile);
            props.store(outputStream, "Compass config cache");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return props;
    }

    private Properties loadCache(File cacheFile) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(cacheFile);
            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }


    /* (non-Javadoc)
     * @see org.opoo.press.Observer#initialize()
     */
    @Override
    public void initialize() throws Exception {
        log.debug("Initialize the observer.");

        configFileEntry = new FileEntry(configFile);
        configFileEntry.refresh(configFile);

        prepareSassAndCssPaths();
        sassWatchable = new WatchableDirectory(sassPath, new SassFileFilter());
        sassWatchable.initialize();
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Observer#check()
     */
    @Override
    public void check() throws Exception {
        if (configFileEntry.refresh(configFile)) {
            log.debug("Compass config file changed, need compile.");
            buildInternal();
            return;
        }

        Result result = sassWatchable.check();
        if (!result.isEmpty()) {
            log.debug("File(s) changed:\n{}", result.toString());
            buildInternal();
        } else {
            log.debug("Nothing to compile - all css files are up to date");
        }
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Observer#destroy()
     */
    @Override
    public void destroy() throws Exception {
        sassWatchable.destroy();
    }

    /* (non-Javadoc)
     * @see org.opoo.press.ThemeBuilder#build()
     */
    @Override
    public void build() throws Exception {
        if (shouldBuild()) {
            buildInternal();
        } else {
            log.debug("Nothing to compile - all css files are up to date");
        }
    }

    @Override
    public void clean() throws Exception {
        File cacheFile = getCacheFile();
        if (cacheFile.exists()) {
            log.debug("Delete cache file '{}'", cacheFile);
            FileUtils.deleteQuietly(cacheFile);
        }
        if (sassPath != null && sassPath.exists() && cssPath != null & cssPath.exists()) {
            int sassPathLength = sassPath.getPath().length() + 1;
            List<File> mainSassFiles = PathUtils.listFiles(sassPath, new MainSassFilenameFilter(), true);
            for (File sassFile : mainSassFiles) {
                String relativeSassFilePath = sassFile.getPath().substring(sassPathLength);
                String cssFilePath = FilenameUtils.removeExtension(relativeSassFilePath) + ".css";
                File cssFile = new File(cssPath, cssFilePath);

                if (cssFile.exists()) {
                    log.debug("Cleaning {}", cssFile);
                    FileUtils.deleteQuietly(cssFile);
                }
            }
        }
    }

    private void buildInternal() {
        new Compass(themePath, configFile).compile();
    }

    public static class MainSassFilenameFilter implements FilenameFilter {
        /* (non-Javadoc)
         * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
         */
        @Override
        public boolean accept(File dir, String name) {
            char firstChar = name.charAt(0);
            if (firstChar == '.' || firstChar == '_' || firstChar == '#') {
                return false;
            }

            if (name.endsWith(".scss") || name.endsWith(".sass")) {
                return true;
            }
            return false;
        }
    }

    public static class PartialSassFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File file, String name) {
            return name.charAt(0) == '_'
                    && (name.endsWith(".scss") || name.endsWith(".sass"));
        }
    }

    public static class SassFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String name = file.getName();
            char firstChar = name.charAt(0);
            if (firstChar == '.' || firstChar == '#') {
                return false;
            }

            if (name.endsWith(".scss") || name.endsWith(".sass")) {
                return true;
            }
            return false;
        }
    }

    @Override
    public String toString() {
        if (configFile == null) {
            return super.toString();
        }
        return String.format("CompassThemeBuilder(config=%s)", configFile);
    }
}
