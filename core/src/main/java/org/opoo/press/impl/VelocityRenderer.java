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

import org.apache.velocity.app.VelocityEngine;
import org.opoo.press.Renderer;
import org.opoo.press.Site;
import org.opoo.press.SourceEntry;

import java.io.Writer;
import java.util.Map;

/**
 * TODO not finish yet.
 * @author Alex Lin
 */
public class VelocityRenderer implements Renderer{
    private Site site;

    public VelocityRenderer(Site site) {
        this.site = site;
        //TODO initialize this renderer
    }

    @Override
    public String render(String templateName, Map<String, Object> rootMap) {
        return null;
    }

    @Override
    public void render(String templateName, Map<String, Object> rootMap, Writer out) {

    }

    @Override
    public String renderContent(String templateContent, Map<String, Object> rootMap) {
        return null;
    }

    @Override
    public void renderContent(String templateContent, Map<String, Object> rootMap, Writer out) {

    }

    @Override
    public String prepareWorkingTemplate(String layout, boolean isValidLayout, String content, boolean isContentRenderRequired, SourceEntry entry) {
        return null;
    }

    @Override
    public void prepareLayoutWorkingTemplates() {

    }

    @Override
    public String getLayoutWorkingTemplate(String layout) {
        return null;
    }

    @Override
    public boolean isRenderRequired(String content) {
        //velocity renderer does not render content.
        return false;
    }

    @Override
    public boolean isValidLayout(String layout) {
        if(layout == null){
            return false;
        }
        if("nil".equalsIgnoreCase(layout)){
            return false;
        }
        if("null".equalsIgnoreCase(layout)){
            return false;
        }
        if("none".equalsIgnoreCase(layout)){
            return false;
        }
        return true;
    }
}
