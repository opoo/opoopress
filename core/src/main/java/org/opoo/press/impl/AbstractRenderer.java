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
import org.opoo.press.Base;
import org.opoo.press.Renderer;
import org.opoo.press.Site;

import java.io.StringWriter;

/**
 * @author Alex Lin
 */
public abstract class AbstractRenderer implements Renderer{

    public boolean isValidLayout(String layout){
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

    public boolean isRenderRequired(Site site, Base base) {
        Boolean requireRender = (Boolean) base.get("require_render_content");
        if(requireRender == null){
            requireRender = (Boolean) site.get("require_render_content");
        }

        if(requireRender != null){
            return requireRender;
        }

        return false;
    }

    @Override
    public void prepare(){

    }

    @Override
    public String renderContent(String templateContent, Object rootMap) {
        StringWriter out = new StringWriter();
        renderContent(templateContent, rootMap, out);
        IOUtils.closeQuietly(out);
        return out.toString();
    }

    @Override
    public String render(String templateName, Object rootMap) {
        StringWriter out = new StringWriter();
        render(templateName, rootMap, out);
        IOUtils.closeQuietly(out);
        return out.toString();
    }
}
