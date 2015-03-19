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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.opoo.press.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.Map;

/**
 * @author Alex Lin
 */
public abstract class AbstractVelocityRenderer extends AbstractRenderer implements Renderer {
    private static final Logger log = LoggerFactory.getLogger(AbstractVelocityRenderer.class);
    private long start = System.currentTimeMillis();

    @Override
    public void render(String templateName, Object context, Writer out) {
        render(templateName, convert(context), out);
    }

    @Override
    public void renderContent(String templateContent, Object context, Writer out) {
        renderContent(templateContent, convert(context), out);
    }

    public void render(String templateName, Context context, Writer out) {
        log.debug("Rendering template {}", templateName);
        Template template = getVelocityEngine().getTemplate(templateName, "UTF-8");
        template.merge(convert(context), out);
    }

    public void renderContent(String templateContent, Context context, Writer out) {
        log.debug("Rendering content...");
        String logTag = "CT" + (start++);
        getVelocityEngine().evaluate(context, out, logTag, templateContent);
    }


    protected Context convert(Object rootMap) {
        if (rootMap instanceof Context) {
            return (Context) rootMap;
        } else if (rootMap instanceof Map) {
            return new VelocityContext((Map) rootMap);
        }
        throw new IllegalArgumentException("rootMap type not supported: " + rootMap);
    }

    protected abstract VelocityEngine getVelocityEngine();
}
