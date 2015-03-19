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

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.Base;
import org.opoo.press.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * @author Alex Lin
 */
public abstract class AbstractFreeMarkerRenderer extends AbstractRenderer {
    private static final Logger log = LoggerFactory.getLogger(AbstractFreeMarkerRenderer.class);
    private long start = System.currentTimeMillis();

    @Override
    public void render(String templateName, Object rootMap, Writer out) {
        log.debug("Rendering template {}", templateName);
        try {
            Template template = getConfiguration().getTemplate(templateName, "UTF-8");
            process(template, rootMap, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void renderContent(String templateContent, Object rootMap, Writer out) {
        log.debug("Rendering content...");
        try {
            Template template = new Template("CT" + (start++), new StringReader(templateContent), getConfiguration(), "UTF-8");
            process(template, rootMap, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    private void process(Template template, Object rootMap, Writer out) throws IOException, TemplateException {
        preProcess(template, rootMap);

        template.process(rootMap, out);
        out.flush();

        postProcess(template, rootMap, out);
    }

    protected void preProcess(Template template, Object rootMap) {
    }

    protected void postProcess(Template template, Object rootMap, Writer out) {
    }

    protected abstract Configuration getConfiguration();

    @Override
    public boolean isRenderRequired(Site site, Base base) {
        boolean b = super.isRenderRequired(site, base);
        if (b) {
            return true;
        }

        //if auto_render set to true, check freemarker tag in content
        Boolean autoRender = (Boolean) site.get("auto_render_content");
        if (autoRender != null && autoRender) {
            String content = base.getContent();
            if (StringUtils.contains(content, "<#") && StringUtils.contains(content, ">")) {
                return true;
            }
            if (StringUtils.contains(content, "${") && StringUtils.contains(content, "}")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void render(Base base, Object rootMap) {
        render(base, (Map<String, Object>) rootMap);
    }

    public abstract void render(Base base, Map<String, Object> rootMap);
}
