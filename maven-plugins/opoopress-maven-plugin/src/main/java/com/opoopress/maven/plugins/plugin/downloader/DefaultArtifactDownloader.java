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
package com.opoopress.maven.plugins.plugin.downloader;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alex Lin
 */
public class DefaultArtifactDownloader implements ArtifactDownloader{
    private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile("(.+)::(.*)::(.+)");
    private static final String OP_RELEASES_REPO = "opoopress-releases::default::http://repo.opoopress.com/releases";
    private static final String OP_SNAPSHOTS_REPO = "opoopress-snapshots::default::http://repo.opoopress.com/snapshots";

    private Log log;

    private ArtifactFactory artifactFactory;

    private ArtifactResolver artifactResolver;

    private ArtifactRepository localRepository;

    private List<ArtifactRepository> remoteArtifactRepositories;

    private String remoteRepositories;

    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    private ArtifactRepositoryFactory artifactRepositoryFactory;

    protected ArtifactMetadataSource artifactMetadataSource;

    private boolean enableOpooPressRepos = true;

    public DefaultArtifactDownloader(Log log,
                                     ArtifactFactory artifactFactory,
                                     ArtifactResolver artifactResolver,
                                     ArtifactRepository localRepository,
                                     List<ArtifactRepository> remoteArtifactRepositories,
                                     String remoteRepositories,
                                     Map<String, ArtifactRepositoryLayout> repositoryLayouts,
                                     ArtifactRepositoryFactory artifactRepositoryFactory,
                                     ArtifactMetadataSource artifactMetadataSource,
                                     boolean enableOpooPressRepos) {
        this.artifactFactory = artifactFactory;
        this.artifactResolver = artifactResolver;
        this.localRepository = localRepository;
        this.remoteArtifactRepositories = remoteArtifactRepositories;
        this.remoteRepositories = remoteRepositories;
        this.repositoryLayouts = repositoryLayouts;
        this.artifactRepositoryFactory = artifactRepositoryFactory;
        this.artifactMetadataSource = artifactMetadataSource;
        this.enableOpooPressRepos = enableOpooPressRepos;
        this.log = log;
    }

    protected Log getLog() {
        return log;
    }

    @Override
    public File download(String groupId, String artifactId, String version, String classifier, String type) throws MojoFailureException {
//        if (theme != null) {
//            String[] tokens = StringUtils.split(theme, ":");
//            if (tokens.length < 3 || tokens.length > 5) {
//                throw new MojoFailureException(
//                        "Invalid theme artifact, you must specify groupId:artifactId:version[:packaging][:classifier] "
//                                + theme);
//            }
//            groupId = tokens[0];
//            artifactId = tokens[1];
//            version = tokens[2];
//            if (tokens.length >= 4) {
//                type = tokens[3];
//            }
//            if (tokens.length == 5) {
//                classifier = tokens[4];
//            } else {
//                classifier = null;
//            }
//        }
//
//        if (artifactId == null) {
//            throw new MojoFailureException("theme'' or 'name' property is required.");
//        }

        String dummyVersion = version;
        if (version == null) {
            dummyVersion = "1.0";
        }

        Artifact toDownload = classifier == null
                ? artifactFactory.createBuildArtifact(groupId, artifactId, dummyVersion, type)
                : artifactFactory.createArtifactWithClassifier(groupId, artifactId, dummyVersion, type, classifier);

        List<ArtifactRepository> remoteRepositoryList = getRemoteRepositoryList();

        if (version == null) {
            List<ArtifactVersion> versions = null;
            try {
                versions = artifactMetadataSource.retrieveAvailableVersions(toDownload, localRepository, remoteRepositoryList);
            } catch (ArtifactMetadataRetrievalException e) {
                throw new MojoFailureException("Retrieve theme artifact versions failed: " + e.getMessage(), e);
            }

            if (versions.isEmpty()) {
                throw new MojoFailureException("Theme artifact versions not found.");
            }

            getLog().info("Found versions: " + versions.toString());

            Collections.sort(versions);

            version = versions.get(versions.size() - 1).toString();

            toDownload.setVersion(version);
            getLog().info("Choose version: " + version);
        }

        try {
            artifactResolver.resolve(toDownload, remoteRepositoryList, localRepository);
        } catch (ArtifactResolutionException e) {
            throw new MojoFailureException("Download theme artifact failed: " + e.getMessage(), e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoFailureException("Theme artifact not found: " + e.getMessage());
        }

        return toDownload.getFile();
    }


    private List<ArtifactRepository> getRemoteRepositoryList() throws MojoFailureException {
        List<ArtifactRepository> repositories = new ArrayList<ArtifactRepository>();
        if (remoteArtifactRepositories != null) {
            repositories.addAll(remoteArtifactRepositories);
        }

        ArtifactRepositoryPolicy always = new ArtifactRepositoryPolicy(true,
                ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);

        if (remoteRepositories != null) {
            // Use the same format as in the deploy plugin id::layout::url
            List<String> repos = Arrays.asList(StringUtils.split(remoteRepositories, ","));
            for (String repo : repos) {
                repositories.add(parseRepository(repo, always));
            }
        }

        if (enableOpooPressRepos) {
            // Use the same format as in the deploy plugin id::layout::url
            ArtifactRepositoryPolicy never = new ArtifactRepositoryPolicy(true,
                    ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
            repositories.add(parseRepository(OP_RELEASES_REPO, never));
            repositories.add(parseRepository(OP_SNAPSHOTS_REPO, always));
        }

        return repositories;
    }

    private ArtifactRepository parseRepository(String repo, ArtifactRepositoryPolicy policy) throws MojoFailureException {
        // if it's a simple url
        String id = null;
        ArtifactRepositoryLayout layout = getLayout("default");
        String url = repo;

        // if it's an extended repo URL of the form id::layout::url
        if (repo.contains("::")) {
            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(repo);
            if (!matcher.matches()) {
                throw new MojoFailureException(repo, "Invalid syntax for repository: " + repo,
                        "Invalid syntax for repository. Use \"id::layout::url\" or \"URL\".");
            }

            id = matcher.group(1).trim();
            if (!StringUtils.isEmpty(matcher.group(2))) {
                layout = getLayout(matcher.group(2).trim());
            }
            url = matcher.group(3).trim();
        }
        return artifactRepositoryFactory.createArtifactRepository(id, url, layout, policy, policy);
    }

    private ArtifactRepositoryLayout getLayout(String id) throws MojoFailureException {
        ArtifactRepositoryLayout layout = repositoryLayouts.get(id);

        if (layout == null) {
            throw new MojoFailureException(id, "Invalid repository layout", "Invalid repository layout: " + id);
        }

        return layout;
    }

}
