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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import java.io.File;

/**
 * @author Alex Lin
 * @goal package
 * @phase package
 * @requiresProject
 * @requiresDependencyResolution runtime
 */
public class PackageMojo extends AbstractMojo {

    private static final String[] DEFAULT_EXCLUDES = {
            // Maven
            "pom*.xml",
            "src/**",
            "target/**",

            // Windows
            "**/Thumbs.db",

            // ".config.rb.cache",
            // ".project", ".classpath", ".settings/**",
            // "*.iml", "*.ipr", ".idea/**",
            // etc
            ".*",
            ".*/**",

            // idea
            "*.iml",
            "*.ipr",

            // Miscellaneous typical temporary files
            "**/*~",
            "**/#*#",
            "**/.#*",
            "**/%*%",
            "**/._*",

            // CVS
            "**/CVS",
            "**/CVS/**",
            "**/.cvsignore",

            // SCCS
            "**/SCCS",
            "**/SCCS/**",

            // Visual SourceSafe
            "**/vssver.scc",

            // Subversion
            "**/.svn",
            "**/.svn/**",

            // Arch
            "**/.arch-ids",
            "**/.arch-ids/**",

            //Bazaar
            "**/.bzr",
            "**/.bzr/**",

            //SurroundSCM
            "**/.MySCMServerInfo",

            // Mac
            "**/.DS_Store"
    };

    private static final String[] DEFAULT_INCLUDES = {"**/**"};

    /**
     * List of files to include. Specified as fileset patterns.
     *
     * @parameter
     */
    private String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns.
     *
     * @parameter
     */
    private String[] excludes;

    /**
     * The zip archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="zip"
     * @required
     */
    private ZipArchiver zipArchiver;

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
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * @parameter expression="${op.theme.classifier}"
     */
    protected String classifier;

    /**
     * @parameter expression="${op.theme.classesClassifier}" default-value="classes"
     */
    private String classesClassifier;

    /**
     * @parameter expression="${op.theme.type}" default-value="zip"
     */
    private String type;

    /**
     * Whether creating the archive should be forced.
     *
     * @parameter expression="${op.theme.forceCreation}" default-value="false"
     */
    private boolean forceCreation;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File outputFile = createArchive();

        String classifier = getClassifier();
        if (classifier != null) {
            projectHelper.attachArtifact(getProject(), getType(), classifier, outputFile);
        } else {
            getProject().getArtifact().setFile(outputFile);
        }
    }

    private File getOutputFile(File basedir, String finalName, String classifier) {
        if (classifier == null) {
            classifier = "";
        } else if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        return new File(basedir, finalName + classifier + "." + getType());
    }

    private File getClassesJarFile(File basedir, String finalName, String classesClassifier) {
        if (classesClassifier == null) {
            classesClassifier = "";
        } else if (classesClassifier.trim().length() > 0 && !classesClassifier.startsWith("-")) {
            classesClassifier = "-" + classesClassifier;
        }

        return new File(basedir, finalName + classesClassifier + ".jar");
    }

    private File createArchive() throws MojoExecutionException {
        File outputFile = getOutputFile(buildDirectory, finalName, getClassifier());

        File classesDirectory = getClassesDirectory();
        File classesJarFile = getClassesJarFile(buildDirectory, finalName, getClassesClassifier());

        //must copy all dependencies to 'target/plugins' directory
        File targetPluginsDir = new File(buildDirectory, "plugins");

        try {
            zipArchiver.setDestFile(outputFile);
            zipArchiver.setForced(forceCreation);

            zipArchiver.addDirectory(basedir, getIncludes(), getExcludes());
//            zipArchiver.addDirectory(basedir, buildIncludes(basedir), null);

            //classes jar
            if (classesDirectory.exists() && classesJarFile.exists()) {
                targetPluginsDir.mkdirs();
                FileUtils.copyFileToDirectory(classesJarFile, targetPluginsDir);
            } else {
                getLog().warn("No theme classes add to theme package.");
            }

            //archive classes jar file and all dependencies
            if (targetPluginsDir.exists() && targetPluginsDir.list().length > 0) {
                zipArchiver.addDirectory(buildDirectory, new String[]{"plugins/**"}, null);
            }

            zipArchiver.createArchive();

        } catch (Exception e) {
            throw new MojoExecutionException("Error assembling OpooPress theme package", e);
        }

        /*
        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver(jarArchiver);

        archiver.setOutputFile(outputFile);

        archive.setForced(forceCreation);
        archive.setAddMavenDescriptor(false);
//        archive.setManifest(null);

        try {
            archiver.getArchiver().addDirectory(basedir, getIncludes(), getExcludes());

            //classes jar
            if (classesDirectory.exists() && classesJarFile.exists()) {
                targetPluginsDir.mkdirs();
                FileUtils.copyFileToDirectory(classesJarFile, targetPluginsDir);
            } else {
                getLog().warn("No theme classes add to theme package.");
            }

            //archive classes jar file and all dependencies
            if (targetPluginsDir.exists() && targetPluginsDir.list().length > 0) {
                archiver.getArchiver().addDirectory(buildDirectory, new String[]{"plugins/**"}, null);
            }

            archiver.createArchive(project, archive);
        } catch (Exception e) {
            throw new MojoExecutionException("Error assembling OpooPress theme package", e);
        }*/

        return outputFile;
    }

    /*
    private String[] buildIncludes(File basedir){
        File[] files = basedir.listFiles();
        List<String> list = new ArrayList<String>();
        for(File file: files){
            String filename = file.getName();
            if(filename.startsWith(".") || filename.startsWith("#") || filename.endsWith("~")){
                continue;
            }

            if(file.isDirectory()){
                if(filename.equals("src") || filename.equals("target")){
                    continue;
                }else{
                    list.add(filename + "/**");
                }
            }else{
                if(filename.equals("pom.xml") || filename.startsWith("pom") && filename.endsWith(".xml")){
                    continue;
                }
                list.add(filename);
            }
        }
        getLog().info("Includes: " + list);
        return list.toArray(new String[list.size()]);
    }*/

    protected String getClassifier() {
        return classifier;
    }

    protected String getClassesClassifier() {
        return classesClassifier;
    }

    protected MavenProject getProject() {
        return project;
    }

    protected String getType() {
        return type;
    }

    protected File getClassesDirectory() {
        return outputDirectory;
    }

    private String[] getIncludes() {
        if (includes != null && includes.length > 0) {
            return includes;
        }
        return DEFAULT_INCLUDES;
    }

    private String[] getExcludes() {
        if (excludes != null && excludes.length > 0) {
            return excludes;
        }
        return DEFAULT_EXCLUDES;
    }
}
