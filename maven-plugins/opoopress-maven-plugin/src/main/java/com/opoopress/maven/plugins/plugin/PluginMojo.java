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
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;
import org.opoo.press.impl.SiteConfigImpl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 * @goal plugin
 * @execute phase="package"
 */
public class PluginMojo extends AbstractOpooPressMojo {
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

    /**
     * Use following schemes:
     * <ul>
     * <li>maven artifact form: '<code>groupId:artifactId:version[:packaging][:classifier]</code>'</li>
     * <li>url form: '<code>http://...</code>' or '<code>https://...</code>' with plugin package.</li>
     * </ul>
     *
     * @parameter expression="${plugin}"
     */
    private String plugin;

    /**
     * The groupId of the plugin artifact to download. Ignored if {@link #plugin} is used.
     *
     * @parameter expression="${groupId}"
     */
    private String groupId;

    /**
     * The artifactId of the plugin artifact to download. Ignored if {@link #plugin} is used.
     *
     * @parameter expression="${artifactId}"
     */
    private String artifactId;

    /**
     * The version of the plugin artifact to download. Ignored if {@link #plugin} is used.
     *
     * @parameter expression="${version}"
     */
    private String version;

    /**
     * The classifier of the plugin artifact to download. Ignored if {@link #plugin} is used.
     *
     * @parameter expression="${classifier}"
     */
    private String classifier;

    /**
     * The packaging of the plugin artifact to download. Ignored if {@link #plugin} is used.
     *
     * @parameter expression="${op.plugin.type}" default-value="jar"
     */
    private String type;


    @Override
    protected void executeInternal(SiteConfigImpl config) throws MojoExecutionException, MojoFailureException {
        String pluginDir = config.get("plugin_dir");
        File plugins = new File(config.getBasedir(), pluginDir);

        URL url = null;

        if (plugin != null) {
            String str = plugin.toLowerCase();
            //url
            if (str.startsWith("http://") || str.startsWith("https://")) {
                try {
                    url = new URL(plugin);
                } catch (MalformedURLException e) {
                    throw new MojoFailureException("Not a valid url: " + plugin, e);
                }
            } else {
                //groupId:artifactId:version[:packaging][:classifier]
                String[] tokens = StringUtils.split(plugin, ":");
                if (tokens.length < 3 || tokens.length > 5) {
                    throw new MojoFailureException(
                            "Invalid plugin artifact, you must specify groupId:artifactId:version[:packaging][:classifier] "
                                    + plugin);
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
            }
        }

        //url
        if(url != null){
            String filename = URIUtil.getName(plugin);
            File pluginFile = new File(plugins, filename);
            if(pluginFile.exists()){
                getLog().info("Plugin already exists: " + pluginFile);
                return;
            }

            try {
                new ProgressURLDownloader().setQuiet(false).download(url, pluginFile);
            } catch (IOException e) {
               throw new MojoFailureException("Download plugin file failed: " + e.getMessage(), e);
            }
            return;
        }

        //artifact
        if(StringUtils.isBlank(groupId)
                || StringUtils.isBlank(artifactId)){

            if(version != null){
                String filename = artifactId + "-" + version
                        + (classifier != null ? "-" + classifier : "")
                        + "." + type;

                File pluginFile = new File(plugins, filename);
                if(pluginFile.exists()){
                    getLog().info("Plugin already exists: " + pluginFile);
                    return;
                }
            }

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

            File file = downloader.download(groupId, artifactId, version, classifier, type);

            File pluginFile = new File(plugins, file.getName());
            if(pluginFile.exists()){
                getLog().info("Plugin already exists: " + pluginFile);
            }else{
                getLog().info("Install plugin: " + pluginFile);
                try {
                    FileUtils.copyFile(file, pluginFile);
                } catch (IOException e) {
                    throw new MojoFailureException("Copy plugin file failed: " + e.getMessage(), e);
                }
            }
        }
    }

}
