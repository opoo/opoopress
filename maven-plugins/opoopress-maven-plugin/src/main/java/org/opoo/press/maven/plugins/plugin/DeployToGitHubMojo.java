/*
 * Copyright 2013 Alex Lin.
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
package org.opoo.press.maven.plugins.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.opoo.press.Site;
import org.opoo.press.support.GitHub;
import org.opoo.press.support.GitHubException;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 * @author Alex Lin
 * @goal deploy-to-github
 * @deprecated
 */
public class DeployToGitHubMojo extends AbstractDeployMojo implements Contextualizable{

	/**
	 * Branch to update
	 *
	 * @parameter expression="${github.site.branch}" default-value="refs/heads/gh-pages"
	 */
	private String branch = GitHub.BRANCH_DEFAULT;

	/**
	 * Commit message
	 *
	 * @parameter expression="${github.site.commit.message}"
	 * @required
	 */
	private String message;

	/**
	 * Name of repository
	 *
	 * @parameter expression="${github.site.repositoryName}"
	 */
	private String repositoryName;

	/**
	 * Owner of repository
	 *
	 * @parameter expression="${github.site.repositoryOwner}"
	 */
	private String repositoryOwner;

	/**
	 * User name for authentication
	 *
	 * @parameter expression="${github.site.userName}"
	 *            default-value="${github.global.userName}"
	 */
	private String userName;

	/**
	 * User name for authentication
	 *
	 * @parameter expression="${github.site.password}"
	 *            default-value="${github.global.password}"
	 */
	private String password;

	/**
	 * User name for authentication
	 *
	 * @parameter expression="${github.site.oauth2Token}"
	 *            default-value="${github.global.oauth2Token}"
	 */
	private String oauth2Token;

	/**
	 * Host for API calls
	 *
	 * @parameter expression="${github.site.host}"
	 *            default-value="${github.global.host}"
	 */
	private String host;

	/**
	 * Id of server to use
	 *
	 * @parameter expression="${github.site.server}"
	 *            default-value="${github.global.server}"
	 */
	private String server;

	/**
	 * Paths and patterns to include
	 *
	 * @parameter
	 */
	private String[] includes;

	/**
	 * Paths and patterns to exclude
	 *
	 * @parameter
	 */
	private String[] excludes;

	/**
	 * Settings
	 *
	 * @parameter expression="${settings}
	 */
	private Settings settings;

	/**
	 * Force reference update
	 *
	 * @parameter expression="${github.site.force}"
	 */
	private boolean force;

	/**
	 * True to always create a '.nojekyll' file at the root of the site if one
	 * doesn't already exist.
	 *
	 * @parameter expression="${github.site.noJekyll}" default-value="true"
	 */
	private boolean noJekyll;

	/**
	 * Merge with existing the existing tree that is referenced by the commit
	 * that the ref currently points to
	 *
	 * @parameter expression="${github.site.merge}"
	 */
	private boolean merge;

	/**
	 * Show what blob, trees, commits, and references would be created/updated
	 * but don't actually perform any operations on the target GitHub
	 * repository.
	 *
	 * @parameter expression="${github.site.dryRun}"
	 */
	private boolean dryRun;
	
	/**
	 *
	 * @parameter expression="${github.site.numThreads}" default-value="1"
	 */
	private int numThreads = 1;
	
	private PlexusContainer container;

	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractDeployMojo#deployTo(java.io.File)
	 */
	@Override
	protected void deploy(Site site, File dest) throws MojoExecutionException, MojoFailureException {
		String username = this.userName;
		String password = this.password;
		String oauth2Token = this.oauth2Token;
		String serverId = this.server;
		
		if(StringUtils.isNotBlank(serverId) && (StringUtils.isBlank(username) || StringUtils.isBlank(password))){
			Server mavenServer = getServer(settings, server);
			if (mavenServer == null){
				throw new MojoExecutionException(MessageFormat.format("Server ''{0}'' not found in settings", server));
			}
			
			if (getLog().isDebugEnabled()){
				getLog().debug(MessageFormat.format("Using ''{0}'' server credentials", serverId));
			}
			
			username = mavenServer.getUsername();
			password = decryptPasswordIfRequired(mavenServer.getPassword(), serverId);
		}
		
		GitHub github = new GitHub();
		github.setBranch(branch);
		github.setDryRun(dryRun);
		github.setExcludes(excludes);
		github.setForce(force);
		github.setHost(host);
		github.setIncludes(includes);
		github.setMerge(merge);
		github.setMessage(message);
		github.setNoJekyll(noJekyll);
		github.setOauth2Token(oauth2Token);
		github.setPassword(password);
		github.setRepositoryName(repositoryName);
		github.setRepositoryOwner(repositoryOwner);
		github.setUserName(username);
		github.setNumThreads(numThreads);
		
		try {
			github.deploy(dest, site.getRoot());
		} catch (GitHubException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private String decryptPasswordIfRequired(String serverPassword, String serverId) {
		if(StringUtils.isBlank(serverPassword)){
			return serverPassword;
		}

		SecDispatcher sd = null;
		try{
            sd = (SecDispatcher) container.lookup( SecDispatcher.ROLE, "maven" );
        }catch (Exception e){
            getLog().warn( "Security features are disabled. Cannot find plexus component " + SecDispatcher.ROLE + ":maven" );
        }
		
		 if ( sd != null ) {
             try    {
            	 serverPassword = sd.decrypt( serverPassword );
             } catch ( SecDispatcherException e ){
            	 reportSecurityConfigurationError( "password for server '" + serverId + "'", e );
             }
         }
		return serverPassword;
	}
	
	private void reportSecurityConfigurationError( String affectedConfiguration, SecDispatcherException e ) {
        Throwable cause = e;
        String msg = "Not decrypting " + affectedConfiguration + " due to exception in security handler.";

        // Drop to the actual cause, it wraps multiple times
        while ( cause.getCause() != null ){
            cause = cause.getCause();
        }

        // common cause is missing settings-security.xml
        if ( cause instanceof FileNotFoundException ){
            msg += "\nEnsure that you have configured your master password file (and relocation if appropriate)\nSee the installation instructions for details.";
        }

        getLog().warn( msg + "\nCause: " + cause.getMessage() );
        getLog().debug( "Full trace follows", e );
    }

	/**
	 * Get server with given id
	 *
	 * @param settings
	 * @param serverId
	 *            must be non-null and non-empty
	 * @return server or null if none matching
	 */
	private Server getServer(Settings settings, String serverId) {
		if (settings == null){
			return null;
		}
		List<Server> servers = settings.getServers();
		if (servers == null || servers.isEmpty()){
			return null;
		}

		for (Server server : servers){
			if (serverId.equals(server.getId())){
				return server;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable#contextualize(org.codehaus.plexus.context.Context)
	 */
	@Override
	public void contextualize(Context context) throws ContextException {
		container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
	}
}
