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

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Config;
import org.opoo.press.Site;
import org.opoo.press.SiteManager;
import org.opoo.press.util.LinkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class SiteManagerImpl implements SiteManager {
    private static final Logger log = LoggerFactory.getLogger(SiteManagerImpl.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(File baseDirectory, Locale locale) throws Exception {
        updateConfigurationFile(baseDirectory, locale);
        checkDirectories(baseDirectory);
    }

    private void updateConfigurationFile(File baseDirectory, Locale locale) {
        File configFile = new File(baseDirectory, "config.yml");
        File configFileZH = new File(baseDirectory, "config_zh_CN.yml");

        if (!configFileZH.exists()) {
            log.debug("config-zh_CN.yml not exists, skip update.");
            return;
        }

        if (!configFile.exists()) {
            //simple rename
            configFileZH.renameTo(configFile);
            return;
        }

        if (locale == null) {
            locale = Locale.getDefault();
        }

        boolean isZH = "zh".equals(locale.getLanguage());

        if (isZH) {
            FileUtils.deleteQuietly(configFile);
            configFileZH.renameTo(configFile);
        } else {
            FileUtils.deleteQuietly(configFileZH);
        }
    }

    private void checkDirectories(File baseDirectory) throws Exception {
        Map<String, Object> override = new HashMap<String, Object>();
        ConfigImpl config = new ConfigImpl(baseDirectory, override);

        File[] configFiles = config.getConfigFiles();
        if (configFiles.length == 0) {
            log.warn("No site configuration file.");
            return;
        }

        List<String> sourcesConfig = config.get("source_dirs");
        checkDirectoryList(baseDirectory, sourcesConfig);

        List<String> assetsConfig = config.get("asset_dirs");
        checkDirectoryList(baseDirectory, assetsConfig);

        String sitePluginsDir = config.get("plugin_dir");
        checkDirectory(baseDirectory, sitePluginsDir);

        checkDirectory(baseDirectory, "themes");
    }

    private void checkDirectoryList(File basedir, List<String> dirs) throws Exception {
        if (dirs != null && !dirs.isEmpty()) {
            for (String dir : dirs) {
                checkDirectory(basedir, dir);
            }
        }
    }

    private void checkDirectory(File basedir, String dir) throws Exception {
        File file = new File(basedir, dir);
        if (!file.exists()) {
            file.mkdirs();
            log.info("mkdir: {}", file);
        } else if (!file.isDirectory() || !file.canRead() || !file.canWrite()) {
            throw new Exception(file + " must be a valid directory.");
        }
    }


    @Override
    public File createNewFile(Site site, String layout, String title, String name, String format,
                              String newFilePattern, String template, Map<String, Object> meta) throws Exception {
        //FIXME validate title
//        if (StringUtils.isBlank(title)) {
//            throw new Exception("Title is required.");
//        }

        name = processName(site, title, name);

        if (format == null) {
            format = "markdown";
        }

        Config config = site.getConfig();

        if (StringUtils.isBlank(newFilePattern)) {
            //new_page, new_page, new_pic
            newFilePattern = config.get("new_" + layout);
            if(StringUtils.isBlank(newFilePattern)){
                if("post".equals(layout)){
                    newFilePattern = ConfigImpl.DEFAULT_NEW_POST_FILE;
                }else if("page".equals(layout)){
                    newFilePattern = ConfigImpl.DEFAULT_NEW_PAGE_FILE;
                }
            }
        }

        if(StringUtils.isBlank(template)){
            template = config.get("new_" + layout + "_template");
            if(StringUtils.isBlank(template)){
                if("post".equals(layout)){
                    template = ConfigImpl.DEFAULT_NEW_POST_TEMPLATE;
                }else if("page".equals(layout)){
                    template = ConfigImpl.DEFAULT_NEW_PAGE_TEMPLATE;
                }
            }
        }

        return renderFile(site, title, name, format, newFilePattern, template, meta);
    }

    private File renderFile(Site site, String title, String name, String format,
                            String newFilePattern, String template, Map<String, Object> meta) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        if(meta != null){
            map.putAll(meta);
        }

        if(title != null) {
            map.put("title", title);
        }

        if(name != null){
            map.put("name", name);
        }

        map.put("format", format);

        Date date = new Date();
        map.put("date", DATE_FORMAT.format(date));
        LinkUtils.addDateParams(map, date);

        //render file path and name
//        String filename = site.getRenderer().renderContent(newFilePattern, map);
        String filename = LinkUtils.renderUrl(newFilePattern, map);
        File file = new File(site.getBasedir(), filename);

        //render
        map.put("site", site);
        map.put("file", file);
        map.put("opoopress", site.getConfig().get("opoopress"));

        FileOutputStream os = null;
        OutputStreamWriter out = null;
        try {
            file.getParentFile().mkdirs();

            os = new FileOutputStream(file);
            out = new OutputStreamWriter(os, "UTF-8");

            site.getRenderer().render(template, map, out);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(os);
        }

        log.info("Write to file: {}", file);
        return file;
    }

    private String processName(Site site, String title, String name) {
        if (name == null) {
            log.info("Using title as post name.");
            name = title;
        } else {
            name = name.trim();
        }
        name = site.toSlug(name);

        return name;
    }
}
