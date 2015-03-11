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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alex Lin
 * @goal get
 * @requiresProject false
 * @threadSafe true
 */
public class GetMojo extends AbstractMojo{
    private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile("(.+)::(.*)::(.+)");
    private static final String DEFAULT_OPOOPRESS_REPOS = "opoopress-releases::default::http://repo.opoopress.com/releases,opoopress-snapshots::default::http://repo.opoopress.com/snapshots";


    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @component
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @component
     */
    private ArtifactMetadataSource source;

    /**
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    /**
     * Repositories in the format id::[layout]::url or just url, separated by comma.
     * ie. central::default::http://repo1.maven.apache.org/maven2,myrepo::::http://repo.acme.com,http://repo.acme2.com
     * @parameter
     */
    private String remoteRepositories;

    /**
     * The destination file or directory to copy the artifact to, if other than the local repository
     *
     * @since 2.4
     * @deprecated if you need to copy the resolved artifact, use dependency:copy
     */
    private String destination;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List<ArtifactRepository> pomRemoteRepositories;

    /**
     * @parameter default-value="${localRepository}"
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Download transitively, retrieving the specified artifact and all of its dependencies.
     * @parameter expression="${transitive}" default-value="true"
     */
    private boolean transitive = true;

    /**
     * @component
     */
    private Invoker invoker;

    /**
     * @parameter expression="${basedir}"
     */
    private File basedir;

    /**
     * @parameter expression="${op.repo.enabled}" default-value="true"
     */
    private boolean enableOpooPressRepo = true;

    /**
     *
     <dependency>
     <groupId>org.springframework</groupId>
     <artifactId>spring-test</artifactId>
     <version>${spring.version}</version>
     <scope>test</scope>

     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String groupId = "org.springframework";
        String artifactId = "spring";
        String version = "2.5.6";
        String packaging = "jar";
        String classifier = "";

        groupId = "cn.redflagsoft";
        artifactId = "rfs-base";
        version = "2.1.17-SNAPSHOT";
        packaging = "jar";

//        Artifact toDownload = classifier == null
//                ? artifactFactory.createBuildArtifact( groupId, artifactId, version, packaging )
//                : artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, packaging, classifier );
//        Artifact dummyOriginatingArtifact =
//                artifactFactory.createBuildArtifact( "org.apache.maven.plugins", "maven-downloader-plugin", "1.0", "jar" );


        Artifact toDownload = artifactFactory.createBuildArtifact(groupId, artifactId, version, packaging);
        toDownload = artifactFactory.createArtifactWithClassifier("org.opoo.press.themes", "opoopress-theme-default",
                "1.2.0-SNAPSHOT", "jar", "opoopress-theme");


        Artifact dummyOriginatingArtifact = artifactFactory.createBuildArtifact("org.apache.maven.plugins", "maven-downloader-plugin", "1.0", "jar" );
        ArtifactRepositoryPolicy always = new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);

        List<ArtifactRepository> repoList = new ArrayList<ArtifactRepository>();

        if ( pomRemoteRepositories != null )
        {
            repoList.addAll( pomRemoteRepositories );
        }

//        remoteRepositories = "rfs.snapshots::default::http://192.168.18.6/maven2/snapshots";

        if ( remoteRepositories != null )
        {
            // Use the same format as in the deploy plugin id::layout::url
            List<String> repos = Arrays.asList(StringUtils.split(remoteRepositories, ","));
            for ( String repo : repos )
            {
                repoList.add( parseRepository( repo, always ) );
            }
        }


        try
        {
//            if ( transitive )
//            {
//                getLog().info( "Resolving " + toDownload + " with transitive dependencies" );
//                artifactResolver.resolveTransitively( Collections.singleton(toDownload), dummyOriginatingArtifact,
//                        repoList, localRepository, source );
//            }
//            else
//            {
//                getLog().info( "Resolving " + toDownload );
//                artifactResolver.resolve( toDownload, repoList, localRepository );
//            }



            getLog().info( "Resolving " + toDownload );
            artifactResolver.resolve( toDownload, repoList, localRepository );
            System.out.println(toDownload);
            System.out.println(toDownload.getFile().exists());
        }
        catch ( AbstractArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Couldn't download artifact: " + e.getMessage(), e );
        }



        //invokePostGoals("", "");

    }
    ArtifactRepository parseRepository( String repo, ArtifactRepositoryPolicy policy )
            throws MojoFailureException
    {
        // if it's a simple url
        String id = null;
        ArtifactRepositoryLayout layout = getLayout( "default" );
        String url = repo;

        // if it's an extended repo URL of the form id::layout::url
        if ( repo.contains( "::" ) )
        {
            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher( repo );
            if ( !matcher.matches() )
            {
                throw new MojoFailureException( repo, "Invalid syntax for repository: " + repo,
                        "Invalid syntax for repository. Use \"id::layout::url\" or \"URL\"." );
            }

            id = matcher.group( 1 ).trim();
            if ( !StringUtils.isEmpty( matcher.group( 2 ) ) )
            {
                layout = getLayout( matcher.group( 2 ).trim() );
            }
            url = matcher.group( 3 ).trim();
        }
        return artifactRepositoryFactory.createArtifactRepository( id, url, layout, policy, policy );
    }

    private ArtifactRepositoryLayout getLayout( String id ) throws MojoFailureException
    {
        ArtifactRepositoryLayout layout = repositoryLayouts.get( id );

        if ( layout == null )
        {
            throw new MojoFailureException( id, "Invalid repository layout", "Invalid repository layout: " + id );
        }

        return layout;
    }

    private void invokePostGoals( String goals, String artifactId )
            throws MojoExecutionException, MojoFailureException
    {
        getLog().info( "Invoking post-archetype-generation goals: " + goals );

        File projectBasedir = new File( basedir, artifactId );

        projectBasedir = new File("/home/lin/workspace/new-layout");
        goals = "op:build";

        if ( projectBasedir.exists() )
        {
            InvocationRequest request = new DefaultInvocationRequest()
                    .setBaseDirectory( projectBasedir )
                    .setGoals( Arrays.asList( StringUtils.split( goals, "," ) ) );

            try
            {
                invoker.execute( request );
            }
            catch ( MavenInvocationException e )
            {
                throw new MojoExecutionException( "Cannot run additions goals.", e );
            }
        }
        else
        {
            getLog().info( "Post-archetype-generation goals aborted: unavailable basedir " + projectBasedir );
        }
    }
}
