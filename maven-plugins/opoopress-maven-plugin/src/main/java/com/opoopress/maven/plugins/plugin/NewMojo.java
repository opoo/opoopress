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
package com.opoopress.maven.plugins.plugin;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.SiteManager;
import org.opoo.press.impl.SiteConfigImpl;
import org.opoo.press.impl.SiteImpl;
import org.opoo.press.impl.SiteManagerImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Lin
 * @goal new
 * @execute phase="package"
 */
public class NewMojo extends AbstractOpooPressMojo{

    /**
     * @parameter expression="${op.new.skip}" default-value="false"
     */
    private boolean skipNew = false;

    /**
     * @parameter expression="${draft}" default-value="false"
     */
    private boolean draft = false;

    /**
     * The layout of new source file.
     * @parameter expression="${layout}" default-value="post"
     */
    private String layout;

    /**
     * File name without extension.
     * @parameter expression="${name}"
     */
    private String name;

    /**
     * The title of your new article.
     *
     * @parameter expression="${title}"
     * @required
     */
    private String title;

    /**
     * @parameter expression="${format}" default-value="markdown"
     */
    private String format;

    /**
     * @parameter expression="${permalink}"
     */
    private String permalink;

    /**
     * @parameter expression="${template}"
     */
    private String template;


    private SiteManager siteManager = new SiteManagerImpl();

    @Override
    protected void executeInternal(SiteConfigImpl config) throws MojoExecutionException, MojoFailureException {
        if (skipNew) {
            getLog().info("Skiping create new file.");
            return;
        }

        if(StringUtils.isBlank(title)){
            throw new MojoFailureException("'title' is required, use '-Dtitle=\"your article title\"'");
        }

        SiteImpl site = new SiteImpl(config);

        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("published", !draft);
        try {
            siteManager.createNewFile(site, layout, title, name, format, permalink, template, meta);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
