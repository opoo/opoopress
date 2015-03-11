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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.StringUtils;
import org.opoo.press.ThemeCompiler;
import org.opoo.press.impl.ConfigImpl;
import org.opoo.press.impl.SiteImpl;

import java.io.File;
import java.util.Arrays;

/**
 * @author Alex Lin
 */
public abstract class AbstractBuildMojo extends AbstractOpooPressMojo{

    /**
     * @parameter expression="${op.theme.build.skip}" default-value="false"
     */
    private boolean skipThemeBuild = false;

    /**
     * @parameter expression="${op.site.build.skip}" default-value="false"
     */
    private boolean skipSiteBuild = false;

    /**
     * @parameter expression="${op.show-drafts}" default-value="false"
     */
    protected boolean showDrafts = false;

    /**
     * @parameter expression="${op.site.build.force}" default-value="false"
     */
    private boolean forceBuild = false;

    /**
     * @component
     */
    private Invoker invoker;

    @Override
    protected void executeInternal(ConfigImpl config) throws MojoExecutionException, MojoFailureException {
        if(showDrafts){
            config.put("show_drafts", true);
        }

        config.put("theme.compiler", new ThemeCompiler() {
            @Override
            public void compile(File themeDir) {
                File src = new File(themeDir, "src");
                File pom = new File(themeDir, "pom.xml");
                if(src.exists() && pom.exists()){
                    try {
                        invokeGoals("compile", themeDir);
                    } catch (MavenInvocationException e) {
                       getLog().error("Compile theme error, invoke goals failed: " + e.getMessage(), e);
                    }
                }else{
                    System.out.println("-- theme '" + themeDir.getName() + "':: no java sources, skip compile --");
                }
            }
        });

        SiteImpl site = new SiteImpl(config);

        if(skipThemeBuild){
            getLog().info("Skipping build theme.");
        }else{
            site.getTheme().build();
        }

        if(skipSiteBuild){
            getLog().info("Skipping build site.");
        }else{
            //site.build(forceBuild);
            build(site, forceBuild);
        }

        executeInternal(config, site);
    }

    protected void executeInternal(ConfigImpl config, SiteImpl site) throws MojoExecutionException, MojoFailureException{
    }


    private void invokeGoals( String goals, File projectBasedir) throws MavenInvocationException {
        getLog().info( "[" + projectBasedir +"] Invoking goals: " + goals );

        InvocationRequest request = new DefaultInvocationRequest()
                .setBaseDirectory(projectBasedir)
                .setGoals(Arrays.asList(StringUtils.split(goals, ",")));

        invoker.execute( request );
    }

    private void build(SiteImpl site, boolean forceBuild){
        long start = System.currentTimeMillis();
        try {
            site.build(forceBuild);
        } finally {
            long time = System.currentTimeMillis() - start;
            getLog().info("Generate time: " + time + "ms");
        }
    }
}
