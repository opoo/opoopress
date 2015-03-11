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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.opoo.press.Config;
import org.opoo.press.Observer;
import org.opoo.press.file.Result;
import org.opoo.press.file.Watchable;
import org.opoo.press.file.WatchableDirectory;
import org.opoo.press.file.WatchableFiles;
import org.opoo.press.SourceEntry;
import org.opoo.press.SourceEntryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Lin
 */
public class SiteObserver implements Observer{
    private static final Logger log = LoggerFactory.getLogger(SiteObserver.class);

    private final SiteImpl site;

    private Observer themeObserver;
    private List<Watchable> configWatchers = new ArrayList<Watchable>();
    private List<Watchable> otherWatchers = new ArrayList<Watchable>();


    SiteObserver(SiteImpl site){
        this.site = site;

        themeObserver = site.getTheme().getObserver();

        FileFilter siteConfigFilesFilter = createConfigFilesFilter();
        Watchable siteConfigWatcher = new WatchableDirectory(site.getBasedir(), siteConfigFilesFilter);
        configWatchers.add(siteConfigWatcher);

        Watchable themeConfigWatcher = new WatchableFiles(site.getTheme().getConfigFile());
        configWatchers.add(themeConfigWatcher);

        List<File> sources = site.getSources();
        for(File source: sources){
            otherWatchers.add(new WatchableDirectory(source));
        }

        otherWatchers.add(new WatchableDirectory(site.getTemplates()));

        List<File> assets = site.getAssets();
        for(File asset: assets){
            otherWatchers.add(new StaticFilesWatcher(asset));
        }
    }

    /**
     * Create configuration files FileFilter.
     * @return file filter
     */
    private FileFilter createConfigFilesFilter(){
        Config config = site.getConfig();
        boolean useDefaultConfigFiles = config.useDefaultConfigFiles();
        if(useDefaultConfigFiles){
            log.debug("Using default config files.");
            return ConfigImpl.DEFAULT_CONFIG_FILES_FILTER;
        }else{//custom config files
            final File[] configFiles = config.getConfigFiles();
            return new FileFilter(){
                @Override
                public boolean accept(File file) {
                    for(File configFile: configFiles){
                        if(configFile.equals(file)){
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
    }

    @Override
    public void initialize() throws Exception {
        if(themeObserver != null){
            themeObserver.initialize();
        }

        for(Watchable watcher: configWatchers){
            watcher.initialize();
        }

        for(Watchable watcher: otherWatchers){
            watcher.initialize();
        }
    }

    @Override
    public void check() throws Exception {
        if(themeObserver != null){
            themeObserver.check();
        }

        for(Watchable watcher: configWatchers){
            Result result = watcher.check();
            if(!result.isEmpty()) {
                log.info("Configuration file changed: \n{}", result.toString());
                throw new ConfigChangedException(result);
            }
        }

        Result result = Result.newResult();
        for(Watchable watcher: otherWatchers){
            Result check = watcher.check();
            if(!check.isEmpty()){
                result.addResult(check);
            }
        }
        if(!result.isEmpty()){
            log.info("Source file(s) changed: \n{}", result.toString());
            long start = System.currentTimeMillis();
            //force build
            site.build(true);
            log.info("Build time: {}ms", System.currentTimeMillis() - start);
        }else{
            log.debug("Nothing to build - all site output files are up to date.");
        }
    }

    @Override
    public void destroy() throws Exception {
        if(themeObserver != null){
            themeObserver.destroy();
        }

        for(Watchable watcher: configWatchers){
            watcher.destroy();
        }

        for(Watchable watcher: otherWatchers){
            watcher.destroy();
        }
    }


    public static class ConfigChangedException extends Exception{
        private Result result;

        public ConfigChangedException(Result result) {
            this.result = result;
        }

        public Result getResult(){
            return result;
        }
    }

    private class StaticFilesWatcher extends WatchableDirectory{
        private File dir;

        public StaticFilesWatcher(File directory) {
            super(directory);
        }

        @Override
        public void onStart(FileAlterationObserver observer) {
            this.dir = observer.getDirectory();
        }

        @Override
        public void onFileCreate(File file) {
            onFileChange(file);
        }

        @Override
        public void onFileChange(File file) {
            SourceEntryLoader loader = site.getFactory().getSourceEntryLoader();
            SourceEntry sourceEntry = loader.buildSourceEntry(dir, file);
            StaticFileImpl staticFile = new StaticFileImpl(site, sourceEntry);
            staticFile.write(site.getDestination());
            log.info("Copy static file: {} => {}", file, site.getDestination());
        }

        @Override
        public void onFileDelete(File file) {
            log.info("Static file deleted: {}", file);
            String filePath = file.getAbsolutePath();
            String dirPath = dir.getAbsolutePath();
            String string = filePath.replace(dirPath, "");
            while(string.startsWith("/") || string.startsWith("\\")){
                string = string.substring(1);
            }
            File destFile = new File(site.getDestination(), string);
            if(destFile.exists()){
                FileUtils.deleteQuietly(destFile);
                log.info("Delete static file: {}", destFile);
            }
        }
    }
}
