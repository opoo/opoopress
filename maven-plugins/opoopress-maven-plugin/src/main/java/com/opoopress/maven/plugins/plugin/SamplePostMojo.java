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
import org.codehaus.plexus.i18n.I18N;
import org.opoo.press.SiteManager;
import org.opoo.press.impl.SiteConfigImpl;
import org.opoo.press.impl.SiteImpl;
import org.opoo.press.impl.SiteManagerImpl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alex Lin
 * @goal sample-post
 * @execute phase="package"
 */
public class SamplePostMojo extends AbstractOpooPressMojo{

    /**
     * @parameter expression="${draft}" default-value="false"
     */
    private boolean draft = false;

    /**
     * The title of your new article.
     *
     * @parameter expression="${title}"
     */
    private String title;

    /**
     * File name without extension.
     * @parameter expression="${name}"
     */
    private String name;

    /**
     * @parameter expression="${format}" default-value="markdown"
     */
    private String format;

    /**
     * @parameter expression="${template}" default-value="sample-post.ftl"
     */
    private String template;

    /**
     * Internationalization.
     *
     * @component
     */
    private I18N i18n;

    private SiteManager siteManager = new SiteManagerImpl();

    @Override
    protected void executeInternal(SiteConfigImpl config) throws MojoExecutionException, MojoFailureException {
        SiteImpl site = new SiteImpl(config);

        Locale locale = site.getLocale();
        if(locale == null){
            locale = Locale.getDefault();
        }

        if(StringUtils.isBlank(title)){
            title = i18n.getString("sample-post", locale, "title");
        }
        if(StringUtils.isBlank(name)){
            name = i18n.getString("sample-post", locale, "name");
        }

        String samplePostTemplate = site.getTheme().get("sample_post_template");
        if(samplePostTemplate != null){
            template = samplePostTemplate;
            getLog().info("Using template configured by theme: " + samplePostTemplate);
        }

        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("published", !draft);
        try {
            siteManager.createNewFile(site, "post", null, name, format, null, template, meta);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
