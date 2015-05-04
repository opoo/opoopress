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

import org.opoo.press.ProcessorAdapter;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class PreRenderProcessor extends ProcessorAdapter{
    private static final Logger log = LoggerFactory.getLogger(PreRenderProcessor.class);

    @Override
    public void preRender(Site site) {
        List<Map<String,String>> preRenderConfiguration = site.get("pre_render");
        if(preRenderConfiguration == null || preRenderConfiguration.isEmpty()){
            log.debug("No 'pre_render' defined in site configuration, skip process.");
            return;
        }

        Renderer renderer = site.getRenderer();
        File workingTemplateDir = new File( site.getWorking(), "templates");
        workingTemplateDir.mkdirs();

        log.debug("Pre render content in directory: {}", workingTemplateDir);

        for(Map<String,String> preRenderItem: preRenderConfiguration){
            String template = preRenderItem.get("template");
            String output = preRenderItem.get("output");
            if(template == null || output == null){
                log.warn("pre_render item defined error, skip process {}", preRenderItem);
                continue;
            }
            render(site, renderer, workingTemplateDir, template, output);
        }
    }

    private void render(Site site, Renderer renderer, File workingTemplateDir,
                        String template, String output) {
        try {
            Map<String, Object> map = new HashMap<String,Object>();
            map.put("site", site);
            map.put("root_url", site.getRoot());
            map.put("basedir", site.getRoot());
            map.put("opoopress", site.getConfig().get("opoopress"));
            map.put("theme", site.getTheme());

            File outputFile = new File(workingTemplateDir, output);
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
            renderer.render(template, map, writer);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Pre render content failed: " + e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Pre render content failed: " + e.getMessage(), e);
        }
    }
}
