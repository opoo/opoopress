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

import com.opoopress.maven.plugins.plugin.downloader.ArtifactDownloader;
import com.opoopress.maven.plugins.plugin.downloader.DefaultArtifactDownloader;
import com.opoopress.maven.plugins.plugin.downloader.ProgressURLDownloader;
import com.opoopress.maven.plugins.plugin.zip.ZipUtils;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;
import org.opoo.press.impl.ConfigImpl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alex Lin
 * @goal theme
 * @execute phase="package"
 */
public class ThemeMojo extends AbstractOpooPressMojo {
    private static final String OP_THEME_ARTIFACT_PREFIX = "opoopress-theme-";
    private static final int OP_THEME_ARTIFACT_PREFIX_LENGTH = OP_THEME_ARTIFACT_PREFIX.length();

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @parameter default-value="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * List of Remote Repositories used by the resolver.
     *
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List<ArtifactRepository> remoteArtifactRepositories;

    /**
     * Repositories in the format id::[layout]::url or just url, separated by comma.
     * ie. central::default::http://repo1.maven.apache.org/maven2,myrepo::::http://repo.acme.com,http://repo.acme2.com
     *
     * @parameter expression="${remoteRepositories}"
     */
    private String remoteRepositories;

    /**
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    /**
     * @component
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * The artifact metadata source to use.
     *
     * @component
     * @required
     * @readonly
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @parameter expression="${op.repos.enabled}" default-value="true"
     */
    private boolean enableOpooPressRepos = true;


//    /**
//     * A string of the form groupId:artifactId:version[:packaging][:classifier].
//     *
//     * @parameter expression="${artifact}"
//     */
//    private String artifact;

    /**
     * Theme name.
     *
     * @parameter expression="${name}"
     */
    private String name;

    /**
     * Theme use following schemes:
     * <ul>
     * <li>maven artifact form: '<code>groupId:artifactId:version[:packaging][:classifier]</code>'</li>
     * <li>url form: '<code>http://...</code>' or '<code>https://...</code>' with theme package.</li>
     * </ul>
     *
     * @parameter expression="${theme}"
     */
    private String theme;

    /**
     * The groupId of the theme artifact to download. Ignored if {@link #theme} is used.
     *
     * @parameter expression="${groupId}" default-value="org.opoo.press.themes"
     */
    private String groupId = "org.opoo.press.themes";

    /**
     * The artifactId of the theme artifact to download. Ignored if {@link #theme} is used.
     *
     * @parameter expression="${artifactId}"
     */
    private String artifactId;

    /**
     * The version of the theme artifact to download. Ignored if {@link #theme} is used.
     *
     * @parameter expression="${version}"
     */
    private String version;

    /**
     * The classifier of the theme artifact to download. Ignored if {@link #theme} is used.
     *
     * @parameter expression="${classifier}"
     */
    private String classifier;

    /**
     * The packaging of the theme artifact to download. Ignored if {@link #theme} is used.
     *
     * @parameter expression="${op.theme.type}" default-value="zip"
     */
    private String type;

//    /**
//     * The maven project.
//     *
//     * @parameter expression="${project}"
//     * @required
//     * @readonly
//     */
//    private MavenProject project;

    /**
     * @parameter expression="${locale}"
     */
    private String locale;


    private URL url;

    @Override
    protected void executeInternal(ConfigImpl config) throws MojoExecutionException, MojoFailureException {
        processParameters();

        if (name == null) {
            throw new MojoFailureException("Theme name is required.");
        }

        if (url == null && artifactId == null) {
            throw new MojoFailureException("Theme download url or artifactId is required.");
        }

        String currentThemeName = config.get("theme");
        getLog().debug("Changing theme '" + currentThemeName + "' to '" + name + "' ...");

        if (currentThemeName != null && currentThemeName.equalsIgnoreCase(name)) {
            getLog().info("The new theme is same as current, skip change.");
            return;
        }

        File themeDir = new File(config.getBasedir(), "themes/" + name);
        if (themeDir.exists()) {
            if (!themeDir.isDirectory()) {
                throw new MojoFailureException("Theme '" + name + "' already exists, but not a valid directory: " + themeDir);
            }
            getLog().info("Theme '" + name + "' already exists, change config only.");
        } else {
            downloadAndUnzipTheme(themeDir);
            updateThemeConfigurationFile(config, themeDir);
        }

        updateSiteConfigurationFile(config, currentThemeName, name);
    }

    private void updateSiteConfigurationFile(ConfigImpl config, String currentThemeName, String newThemeName)
            throws MojoFailureException {

        if(currentThemeName != null){
            File file = new File(config.getBasedir(), "config.yml");
            if(file.exists()) {
                try {
                    List<String> lines = FileUtils.readLines(file, "UTF-8");
                    int lineNumber = -1;
                    for(int i = 0 ; i < lines.size() ; i++){
                        if(lines.get(i).startsWith("theme: ")){
                            lineNumber = i;
                            break;
                        }
                    }
                    if(lineNumber != -1){
                        getLog().debug("Change theme to '" + newThemeName + "' in file:" + file);
                        lines.set(lineNumber, "theme: '" + newThemeName + "'");
                        FileUtils.writeLines(file, "UTF-8", lines);
                        return;
                    }
                } catch (IOException e) {
                    throw new MojoFailureException("Update configuration file failed: " + e.getMessage(), e);
                }
            }
        }

        getLog().debug("Changing config file 'config-theme.yml'.");
        File themeConfigFile = new File(config.getBasedir(), "config-theme.yml");
        try {
            FileUtils.writeStringToFile(themeConfigFile, "theme: '" + name + "'");
        } catch (IOException e) {
            throw new MojoFailureException("Change theme config file failed.", e);
        }
    }

    private void processParameters() throws MojoFailureException {
        if (name != null) {
            if (name.startsWith(OP_THEME_ARTIFACT_PREFIX)) {
                if (artifactId == null) {
                    artifactId = name;
                }
                name = name.substring(OP_THEME_ARTIFACT_PREFIX_LENGTH);
            } else {
                if (artifactId == null) {
                    artifactId = OP_THEME_ARTIFACT_PREFIX + name;
                }
            }
        }

//        URL url = null;

        if (theme != null) {
            String str = theme.toLowerCase();
            //url
            if (str.startsWith("http://") || str.startsWith("https://")) {
                try {
                    url = new URL(theme);
                } catch (MalformedURLException e) {
                    throw new MojoFailureException("Not a valid url: " + theme, e);
                }
                if (name == null) {
                    String filename = URIUtil.getName(theme);
                    name = FilenameUtils.removeExtension(filename);
                }
            } else {
                //groupId:artifactId:version[:packaging][:classifier]
                String[] tokens = StringUtils.split(theme, ":");
                if (tokens.length < 3 || tokens.length > 5) {
                    throw new MojoFailureException(
                            "Invalid theme artifact, you must specify groupId:artifactId:version[:packaging][:classifier] "
                                    + theme);
                }
                groupId = tokens[0];
                artifactId = tokens[1];
                version = tokens[2];
                if (tokens.length >= 4) {
                    type = tokens[3];
                }
                if (tokens.length == 5) {
                    classifier = tokens[4];
                } else {
                    classifier = null;
                }

                if (name == null) {
                    if (artifactId.startsWith(OP_THEME_ARTIFACT_PREFIX)) {
                        name = artifactId.substring(OP_THEME_ARTIFACT_PREFIX_LENGTH);
                    } else {
                        name = artifactId;
                    }
                }
            }
        }
    }


    protected void downloadAndUnzipTheme(File themeDir) throws MojoFailureException {
        File file;
        File tempFile = null;

        if (url != null) {
            getLog().info("Downloading theme '" + name + "' from '" + url + "'.");
            tempFile = file = downloadHttpFile(url);
        } else {
            String artifact = groupId + ":" + artifactId + ":" + (version == null ? "LATEST" : version);
            getLog().info("Downloading theme '" + name + "' artifact '" + artifact + "' from maven repository.");
            file = downloadArtifactFile();
        }

        if (file == null) {
            throw new MojoFailureException("Download theme failed.");
        }

        try {
            ZipUtils.unzipFileToDirectory(file, themeDir);
            //delete META-INF directory
            File metaDir = new File(themeDir, "META-INF");
            if (metaDir.exists() && metaDir.isDirectory()) {
                System.out.println("Deleting: " + metaDir);
                FileUtils.deleteDirectory(metaDir);
            }
        } catch (IOException e) {
            throw new MojoFailureException("Unzip theme failed.", e);
        }

        //temp file
        if (tempFile != null) {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    private void updateThemeConfigurationFile(ConfigImpl siteConfig, File themeDir) throws MojoFailureException{
        File config = new File(themeDir, "theme.yml");
        if (!config.exists()) {
            throw new MojoFailureException("Config file '" + config + "' not exists.");
        }

        Locale loc = Locale.getDefault();
        //locale from parameter
        String localeString = locale;
        //locale from site configuration
        if(StringUtils.isBlank(localeString)){
            localeString = siteConfig.get("locale");
        }
        if (StringUtils.isNotEmpty(localeString)) {
            loc = LocaleUtils.toLocale(localeString);
        }

        File localeConfig = new File(themeDir, "theme_" + loc.toString() + ".yml");
        if (localeConfig.exists()) {
            config.renameTo(new File(themeDir, "theme-original.yml"));
            localeConfig.renameTo(config);
        }
    }

    protected File downloadArtifactFile() throws MojoFailureException {
        ArtifactDownloader downloader = new DefaultArtifactDownloader(getLog(),
                artifactFactory,
                artifactResolver,
                localRepository,
                remoteArtifactRepositories,
                remoteRepositories,
                repositoryLayouts,
                artifactRepositoryFactory,
                artifactMetadataSource,
                enableOpooPressRepos);

        return downloader.download(groupId, artifactId, version, classifier, type);
    }

    protected File downloadHttpFile(URL url) throws MojoFailureException {
        try {
            File tempFile = File.createTempFile("tempOpooPressTheme", ".zip");

            //1. simplest way, no output
            //FileUtils.copyURLToFile(url, tempFile);

            //2. complex way
            new ProgressURLDownloader().setQuiet(false).download(url, tempFile);

            return tempFile;
        } catch (IOException e) {
            throw new MojoFailureException("Error download file: " + e.getMessage(), e);
        }
    }

}
