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

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.SiteManager;
import org.opoo.press.impl.SiteManagerImpl;

import java.io.File;
import java.util.Locale;

/**
 * @author Alex Lin
 * @goal init
 * @execute phase="package"
 */
public class InitMojo extends AbstractMojo {

    /**
     * @parameter expression="${locale}"
     */
    private String locale;

    /**
     * @parameter expression="${op.init.skip}" default-value="false"
     */
    private boolean skipInit = false;

    /**
     * Base directory of the project.
     *
     * @parameter expression="${basedir}"
     * @readonly
     * @required
     */
    private File baseDirectory;


    private SiteManager siteManager = new SiteManagerImpl();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipInit) {
            getLog().info("Skiping initialize site.");
            return;
        }

        Locale loc = null;
        if (StringUtils.isNotEmpty(locale)) {
            loc = LocaleUtils.toLocale(locale);
        }

        try {
            siteManager.initialize(baseDirectory, loc);
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

}
