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

import org.apache.commons.io.IOUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.opoo.press.Base;
import org.opoo.press.Excerptable;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author Alex Lin
 */
public class VelocityRenderer extends AbstractVelocityRenderer implements Renderer {
    private static final Logger log = LoggerFactory.getLogger(VelocityRenderer.class);

    private final VelocityEngine velocityEngine;
    private final Site site;
    private File templateDir;
//    private File workingTemplateDir;

    public VelocityRenderer(Site site) {
        this.site = site;

        templateDir = site.getTemplates();
        log.debug("Template directory: " + templateDir.getAbsolutePath());

//        //Working directory
//        workingTemplateDir = new File(site.getWorking(), "templates");
//        PathUtils.checkDir(workingTemplateDir, PathUtils.Strategy.CREATE_IF_NOT_EXISTS);
//        log.debug("Working template directory: {}", workingTemplateDir.getAbsolutePath());

        velocityEngine = new VelocityEngine();
        initializeVelocityEngine(site, velocityEngine);
    }

    private void initializeVelocityEngine(Site site, VelocityEngine velocityEngine) {
        //configuration file: velocity.properties
        File themeDir = site.getTheme().getPath();
        File configurationFile = new File(themeDir, "velocity.properties");
        if (configurationFile.exists() && configurationFile.isFile() && configurationFile.canRead()) {
            Properties props = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(configurationFile);
                props.load(in);
                log.info("Load template engine configuration from " + configurationFile);
            } catch (IOException e) {
                throw new IllegalArgumentException("Initializing velocity engine failed: " + e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

        //configuration in theme.yaml
        Map<String, Object> configuration = (Map<String, Object>) site.get("velocity.properties");
        if (configuration != null) {
            for (Map.Entry<String, Object> entry : configuration.entrySet()) {
                velocityEngine.setProperty(entry.getKey(), entry.getValue());
            }
        }

        //file.resource.loader.path
        String path = (String) velocityEngine.getProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH);
        String newPath = /*workingTemplateDir.getAbsolutePath() + ", " + */templateDir.getAbsolutePath();
        if (path != null) {
            newPath += ", " + path;
        }
        velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, newPath);
        velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, true);

        //init the engine
        velocityEngine.init();
    }

    @Override
    public void render(Base base, Object rootMap) {
        Context context = convert(rootMap);

        String content = base.getContent();
        String layout = base.getLayout();

        boolean isContentRenderRequired = isRenderRequired(site, base);
        boolean isValidLayout = isValidLayout(layout);

        if (isContentRenderRequired) {
            content = renderContent(content, context);
        }

        if (isValidLayout) {
            context.put("content", content);
            content = render("_" + layout + ".vm", context);
        } else {
            //do nothing
            //content = content;
        }

        base.setContent(content);

        if(isContentRenderRequired && base instanceof Excerptable && ((Excerptable) base).isExcerpted()){
            Excerptable o = (Excerptable) base;
            String excerpt = renderContent(o.getExcerpt(), context);
            o.setExcerpt(excerpt);
        }
    }

    @Override
    protected VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }
}
