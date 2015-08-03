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
package org.opoo.press.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Observer;
import org.opoo.press.ObserversObserver;
import org.opoo.press.ResourceBuilder;
import org.opoo.press.Theme;
import org.opoo.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 * @since 1.2
 */
public class ThemeImpl implements Theme {
    public static final String THEME_CONFIGURATION_FILENAME = "theme.yml";
    public static final String RESOURCE_BUILDERS_CONFIGURATION_FILENAME = "resource-builders.yml";
    private static final Logger log = LoggerFactory.getLogger(ThemeImpl.class);


    private final File path;
    private final File configFile;
    private final SiteImpl site;
    private Map<String, Object> config;
    private File source;
    private File assets;
    private File templates;
    private List<ResourceBuilder> builders;

    @SuppressWarnings("unchecked")
    ThemeImpl(File path, SiteImpl site) {
        this.site = site;
        this.path = PathUtils.canonical(path);

        configFile = new File(path, THEME_CONFIGURATION_FILENAME);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(configFile);
            config = new Yaml().loadAs(stream, Map.class);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Theme config file not found: " + configFile);
        } finally {
            IOUtils.closeQuietly(stream);
        }

        //
        String sourceConfig = (String) config.get("source_dir");
        if (StringUtils.isBlank(sourceConfig)) {
            sourceConfig = "sources";
        }
        source = new File(path, sourceConfig);

        //
        String assetsConfig = (String) config.get("asset_dir");
        if (StringUtils.isBlank(assetsConfig)) {
            assetsConfig = "assets";
        }
        assets = new File(path, assetsConfig);

        //
        String templatesConfig = (String) config.get("template_dir");
        if (StringUtils.isBlank(templatesConfig)) {
            templatesConfig = "templates";
        }
        templates = new File(path, templatesConfig);
    }

    public File getConfigFile() {
        return configFile;
    }

    private void initializeResourceBuilders() {
        if (builders == null) {
            builders = new ArrayList<ResourceBuilder>();

            //check config file
            File themeBuildersConfigFile = new File(path, RESOURCE_BUILDERS_CONFIGURATION_FILENAME);
            if (!themeBuildersConfigFile.exists()) {
                return;
            }

            //read config
            List<Map<String, Object>> resourceBuildersConfig;
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(themeBuildersConfigFile);
                resourceBuildersConfig = new Yaml().loadAs(stream, List.class);
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("Resource builders config file not found: " + configFile);
            } finally {
                IOUtils.closeQuietly(stream);
            }

            //create builders
            for (Map<String, Object> config : resourceBuildersConfig) {
                ResourceBuilder resourceBuilder = createResourceBuilder(config);
                builders.add(resourceBuilder);
                log.trace("Initializing resource builder: {}", resourceBuilder);
                resourceBuilder.init(path, config);
            }
            log.info("Initialized {} resource builders.", resourceBuildersConfig.size());
        }
    }

    protected ResourceBuilder createResourceBuilder(Map<String, Object> config) {
        String type = (String) config.get("type");
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("The type of resource builder is required.");
        }

        return site.getFactory().createResourceBuilder(type);
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Theme#getPath()
     */
    @Override
    public File getPath() {
        return path;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Theme#getSource()
     */
    @Override
    public File getSource() {
        return source;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Theme#getTemplates()
     */
    @Override
    public File getTemplates() {
        return templates;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Theme#getAssets()
     */
    @Override
    public File getAssets() {
        return assets;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Theme#build()
     */
    @Override
    public void build() {
        if (site != null) {
            site.getProcessors().beforeBuildTheme(this);
        }

        initializeResourceBuilders();
        if (!builders.isEmpty()) {
            try {
                for (ResourceBuilder builder : builders) {
                    builder.build();
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Theme build exception", e);
            }
        }

        if (site != null) {
            site.getProcessors().afterBuildTheme(this);
        }
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Theme#getPage(java.lang.String)
     */
    @Override
    public <T> T get(String name) {
        return (T) config.get(name);
    }


    @Override
    public Observer getObserver() {
        initializeResourceBuilders();
        if (builders.isEmpty()) {
            return null;
        }

        List<Observer> list = new ArrayList<Observer>();
        for (ResourceBuilder builder : builders) {
            if (builder instanceof Observer) {
                list.add((Observer) builder);
            }
        }

        return new ObserversObserver(list);
    }

    @Override
    public void clean() throws Exception {
        initializeResourceBuilders();
        if (!builders.isEmpty()) {
            for (ResourceBuilder builder : builders) {
                builder.clean();
            }
        }
    }
}
