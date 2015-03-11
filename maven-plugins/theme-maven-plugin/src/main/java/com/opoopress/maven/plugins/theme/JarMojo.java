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
package com.opoopress.maven.plugins.theme;

import org.apache.commons.io.FileUtils;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;

/**
 * @author Alex Lin
 * @goal jar
 * @phase package
 */
public class JarMojo extends AbstractMojo{

    /**
     * The Jar archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="jar"
     * @required
     */
    private JarArchiver jarArchiver;

    /**
     * The name of the generated module.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * The directory for the generated module.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File buildDirectory;

    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * Base directory of the project.
     *
     * @parameter expression="${basedir}"
     * @readonly
     * @required
     */
    private File basedir;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();


    /**
     * @parameter expression="${attach}" default-value="true"
     */
    private boolean attach = true;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * @parameter expression="${op.theme.classifier}" default-value="opoopress-theme"
     */
    protected String classifier;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String[] includes = {"**/*"};
        String[] excludes = {"**/Thumbs.db", "src/**", "target/**", ".config.rb.cache"};

        File outputFile = new File(buildDirectory, finalName + "-" + classifier + ".jar");
        File jarFile = new File(buildDirectory, finalName + ".jar");
        File targetPluginsDir = new File(buildDirectory, "plugins");

        try{
            MavenArchiver archiver = new MavenArchiver();
            archiver.setArchiver(jarArchiver);
            archiver.setOutputFile(outputFile);

            archiver.getArchiver().addDirectory(basedir, includes, excludes);

            if(outputDirectory.exists() && jarFile.exists()){
                if(!targetPluginsDir.exists()){
                    targetPluginsDir.mkdirs();
                }
                FileUtils.copyFileToDirectory(jarFile, targetPluginsDir);
            }else{
                getLog().warn("No theme classes add to theme package.");
            }

            if(targetPluginsDir.exists()){
                archiver.getArchiver().addDirectory(buildDirectory, new String[]{"plugins/**"}, null);
            }

            archiver.createArchive(project, archive);
        } catch (Exception e) {
            throw new MojoExecutionException("build archiver error: " + e.getMessage(), e);
        }

        if(attach){
            projectHelper.attachArtifact(project, "jar", classifier, outputFile);
        }
    }
}
