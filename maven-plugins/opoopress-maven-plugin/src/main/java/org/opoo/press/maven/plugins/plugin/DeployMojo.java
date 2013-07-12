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

import java.io.Console;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.wagon.CommandExecutionException;
import org.apache.maven.wagon.CommandExecutor;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.observers.Debug;
import org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo;
import org.apache.maven.wagon.providers.ssh.jsch.ScpWagon;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author Alex Lin
 * @goal deploy
 */
public class DeployMojo extends AbstractGenerateMojo implements Contextualizable{

   /**
    * Whether to run the "chmod" command on the remote site after the deploy.
    * Defaults to "true".
    *
    * @parameter expression="${op.chmod}" default-value="true"
    * @since 2.1
    */
   private boolean chmod;

   /**
    * The mode used by the "chmod" command. Only used if chmod = true.
    * Defaults to "g+w,a+rX".
    *
    * @parameter expression="${op.chmod.mode}" default-value="g+w,a+rX"
    * @since 2.1
    */
   private String chmodMode;

   /**
    * The options used by the "chmod" command. Only used if chmod = true.
    * Defaults to "-Rf".
    *
    * @parameter expression="${op.chmod.options}" default-value="-Rf"
    * @since 2.1
    */
   private String chmodOptions;

   /**
    * Set this to 'true' to skip deployment.
    *
    * @parameter expression="${op.deploy.skip}" default-value="false"
    * @since 2.4
    */
   private boolean skipDeploy;
   
	/**
	 * @parameter expression="${op.generate.skip}" default-value="true"
	 */
	protected boolean skipGenerate;

   /**
    * @component
    */
   private WagonManager wagonManager;
   
   /**
    * The current user system settings for use in Maven.
    *
    * @parameter expression="${settings}"
    * @required
    * @readonly
    */
   private Settings settings;
   
   private PlexusContainer container;
   
	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.press.AbstractPressMojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if ( skipDeploy ){
	        getLog().info( "op.deploy.skip = true: Skipping deployment" );
	        return;
	    }
		
		super.execute();
		
		createSite();
		if(skipGenerate){
			getLog().info( "op.generate.skip = true: Skipping generating" );
		}else{
			generate();
		}
		
		File file = site.getDestination();
		getLog().info("Destination [" + file + "]");
		getLog().info("Site root [" + site.getRoot() + "]" );
		
		//deployTo(new Repository(id, appendSlash(url)), file);
		deployTo(getDeployRepository(), file);
	}
	
	@SuppressWarnings("unchecked")
	private Repository getDeployRepository() throws MojoExecutionException, MojoFailureException{
		Map<String, Object> config = site.getConfig();
		
		Object server = config.get("deploy_server");
		if(server == null){
			throw new MojoFailureException("Deploy server not found in config.yml");
		}
		
		String serverId = null;
		Map<String,String> repo = null;
		if(server instanceof Map){
			repo = (Map<String, String>) server;
		}else if(server instanceof String){
			serverId = (String) server;
			repo = (Map<String, String>) config.get(serverId);
			if(repo == null){
				throw new MojoFailureException("Deploy server not found: " + server);
			}
		}else{
			throw new MojoFailureException("Deploy server not found in config.yml");
		}
		
		String id = repo.get("id");
		String url = repo.get("url");
		if(id == null){
			id = serverId;
		}
		if(id == null || url == null){
			throw new MojoFailureException("Deploy server configuration must contains 'id' and 'url': " + server);
		}
		
		return new Repository(id, appendSlash(url));
	}

	/**
	 * @param repository
	 * @throws MojoExecutionException 
	 */
	private void deployTo(Repository repository, File destination) throws MojoExecutionException {
		if ( !destination.exists()) {
            throw new MojoExecutionException( "The site does not exist, please run mvn op:generate first" );
        }
		
		if ( getLog().isDebugEnabled()){
            getLog().debug( "Deploying to '" + repository.getUrl() + "',\n    Using credentials from server id '" + repository.getId() + "'" );
        }
		
		deploy( destination, repository );
	}

	private void deploy(final File directory, final Repository repository)
			throws MojoExecutionException {
		// TODO: work on moving this into the deployer like the other deploy
		// methods
		final Wagon wagon = getWagon(repository, wagonManager);

		//Using System.console(), password will not display
		if(System.console() != null && wagon instanceof ScpWagon){
			((ScpWagon)wagon).setInteractiveUserInfo(new SystemConsoleInteractiveUserInfo());
			getLog().debug("ScpWagon using SystemConsoleInteractiveUserInfo(Java 6+).");
		}
		
		try {
			configureWagon(wagon, repository.getId(), settings, container, getLog());
		} catch (WagonConfigurationException e) {
			throw new MojoExecutionException("Unable to configure Wagon: '"	+ repository.getProtocol() + "'", e);
		}

		String relativeDir = site.getRoot();
		if("".equals(relativeDir)){
			relativeDir = "./";
		}
		
		try {
			final ProxyInfo proxyInfo = getProxyInfo(repository, wagonManager);
			
			push(directory, repository, wagonManager, wagon, proxyInfo,	relativeDir, getLog());

			if (chmod) {
				chmod(wagon, repository, chmodOptions, chmodMode);
			}
		} finally {
			try {
				wagon.disconnect();
			} catch (ConnectionException e) {
				getLog().error("Error disconnecting wagon - ignored", e);
			}
		}
	}
	
    private static Wagon getWagon( final Repository repository, final WagonManager manager )
        throws MojoExecutionException{
        final Wagon wagon;

        try{
            wagon = manager.getWagon( repository );
        }
        catch ( UnsupportedProtocolException e ){
            throw new MojoExecutionException( "Unsupported protocol: '" + repository.getProtocol() + "'", e );
        }
        catch ( WagonConfigurationException e ){
            throw new MojoExecutionException( "Unable to configure Wagon: '" + repository.getProtocol() + "'", e );
        }

        if ( !wagon.supportsDirectoryCopy() ) {
            throw new MojoExecutionException("Wagon protocol '" + repository.getProtocol() + "' doesn't support directory copying" );
        }

        return wagon;
    }
    
	private static void push(final File inputDirectory,
			final Repository repository, final WagonManager manager,
			final Wagon wagon, final ProxyInfo proxyInfo,
			final String relativeDir,
			final Log log) throws MojoExecutionException {
		AuthenticationInfo authenticationInfo = manager.getAuthenticationInfo( repository.getId() );
        log.debug( "authenticationInfo with id '" + repository.getId() + "': "
                   + ( ( authenticationInfo == null ) ? "-" : authenticationInfo.getUserName() ) );

		try {
			Debug debug = new Debug();

			wagon.addSessionListener(debug);

			wagon.addTransferListener(debug);

			if ( proxyInfo != null ) {
                log.debug( "connect with proxyInfo" );
                wagon.connect( repository, authenticationInfo, proxyInfo );
            }
            else if ( proxyInfo == null && authenticationInfo != null ){
                log.debug( "connect with authenticationInfo and without proxyInfo" );
                wagon.connect( repository, authenticationInfo );
            }
            else  {
                log.debug( "connect without authenticationInfo and without proxyInfo" );
                wagon.connect( repository );
            }
			
			log.info("Pushing " + inputDirectory);

			// TODO: this also uploads the non-default locales,
			// is there a way to exclude directories in wagon?
			log.info("   >>> to " + repository.getUrl() + relativeDir);

			wagon.putDirectory(inputDirectory, relativeDir);
		} catch (ResourceDoesNotExistException e) {
			throw new MojoExecutionException("Error uploading site", e);
		} catch (TransferFailedException e) {
			throw new MojoExecutionException("Error uploading site", e);
		} catch (AuthorizationException e) {
			throw new MojoExecutionException("Error uploading site", e);
		} catch (ConnectionException e) {
			throw new MojoExecutionException("Error uploading site", e);
		} catch (AuthenticationException e) {
			throw new MojoExecutionException("Error uploading site", e);
		}
	}

	private static void chmod(final Wagon wagon, final Repository repository,
			final String chmodOptions, final String chmodMode)
			throws MojoExecutionException {
		try {
			if (wagon instanceof CommandExecutor) {
				CommandExecutor exec = (CommandExecutor) wagon;
				exec.executeCommand("chmod " + chmodOptions + " " + chmodMode + " " + repository.getBasedir());
			}
			// else ? silently ignore, FileWagon is not a CommandExecutor!
		} catch (CommandExecutionException e) {
			throw new MojoExecutionException("Error uploading site", e);
		}
	}

	
//	/**
//	 * @return
//	 */
//	protected abstract String getDeployRepositoryURL() throws MojoExecutionException;
//
//	/**
//	 * @return
//	 */
//	protected abstract String getDeployRepositoryID() throws MojoExecutionException;

	  /**
     * Make sure the given url ends with a slash.
     *
     * @param url a String.
     *
     * @return if url already ends with '/' it is returned unchanged,
     *      otherwise a '/' character is appended.
     */
	protected static String appendSlash(final String url) {
		if (url.endsWith("/")) {
			return url;
		} else {
			return url + "/";
		}
	}
    
    /**
     * Configure the Wagon with the information from serverConfigurationMap ( which comes from settings.xml )
     *
     * @todo Remove when {@link WagonManager#getWagon(Repository) is available}. It's available in Maven 2.0.5.
     * @param wagon
     * @param repositoryId
     * @param settings
     * @param container
     * @param log
     * @throws WagonConfigurationException
     */
    private static void configureWagon( Wagon wagon, String repositoryId, Settings settings, PlexusContainer container, Log log )
        throws WagonConfigurationException{
        log.debug( " configureWagon " );

        // MSITE-25: Make sure that the server settings are inserted
        for ( Object o : settings.getServers() ) {
        	Server server = (Server) o;
            String id = server.getId();

            log.debug( "configureWagon server " + id );

            if ( id != null && id.equals( repositoryId ) && ( server.getConfiguration() != null ) ) {
                final PlexusConfiguration plexusConf =
                    new XmlPlexusConfiguration( (Xpp3Dom) server.getConfiguration() );

                ComponentConfigurator componentConfigurator = null;
                try{
                    componentConfigurator = (ComponentConfigurator) container.lookup( ComponentConfigurator.ROLE );
                    componentConfigurator.configureComponent( wagon, plexusConf, container.getContainerRealm() );
                } catch ( final ComponentLookupException e ) {
                    throw new WagonConfigurationException( repositoryId, "Unable to lookup wagon configurator."
                        + " Wagon configuration cannot be applied.", e );
                } catch ( ComponentConfigurationException e ){
                    throw new WagonConfigurationException( repositoryId, "Unable to apply wagon configuration.", e );
				} finally {
					if (componentConfigurator != null) {
						try {
							container.release(componentConfigurator);
						} catch (ComponentLifecycleException e) {
							log.error("Problem releasing configurator - ignoring: " + e.getMessage());
						}
					}
				}
            }
        }
    }
    
    
	public static ProxyInfo getProxyInfo(Repository repository, WagonManager wagonManager) {
		ProxyInfo proxyInfo = wagonManager.getProxy(repository.getProtocol());

		if (proxyInfo == null) {
			return null;
		}

        String host = repository.getHost();
        String nonProxyHostsAsString = proxyInfo.getNonProxyHosts();
        for ( String nonProxyHost : StringUtils.split( nonProxyHostsAsString, ",;|" ) ){
            if ( org.apache.commons.lang.StringUtils.contains( nonProxyHost, "*" ) ) {
                // Handle wildcard at the end, beginning or middle of the nonProxyHost
                final int pos = nonProxyHost.indexOf( '*' );
                String nonProxyHostPrefix = nonProxyHost.substring( 0, pos );
                String nonProxyHostSuffix = nonProxyHost.substring( pos + 1 );
                // prefix*
                if ( StringUtils.isNotEmpty( nonProxyHostPrefix ) && host.startsWith( nonProxyHostPrefix )
                    && StringUtils.isEmpty( nonProxyHostSuffix ) ) {
                    return null;
                }
                // *suffix
                if ( StringUtils.isEmpty( nonProxyHostPrefix )
                		&& StringUtils.isNotEmpty( nonProxyHostSuffix ) && host.endsWith( nonProxyHostSuffix ) ) {
                    return null;
                }
                // prefix*suffix
                if ( StringUtils.isNotEmpty( nonProxyHostPrefix ) && host.startsWith( nonProxyHostPrefix )
                    && StringUtils.isNotEmpty( nonProxyHostSuffix ) && host.endsWith( nonProxyHostSuffix ) ){
                    return null;
                }
            }else if ( host.equals( nonProxyHost ) ){
                return null;
            }
        }
        return proxyInfo;
    }
    
	/* (non-Javadoc)
	 * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable#contextualize(org.codehaus.plexus.context.Context)
	 */
	@Override
	public void contextualize(Context context) throws ContextException {
		container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
	}
	
	static class SystemConsoleInteractiveUserInfo implements InteractiveUserInfo {
		private Console console = System.console();
		/* (non-Javadoc)
		 * @see org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo#promptYesNo(java.lang.String)
		 */
		@Override
		public boolean promptYesNo(String message) {
			List<String> possibleValues = Arrays.asList(new String[] { "y", "n" });
			message += " (y/n): ";
			String line;

			do {
				line = new String(console.readLine(message));

				if (line != null && !possibleValues.contains(line)) {
					console.printf("Invalod selection");
				}
			} while (line == null || !possibleValues.contains(line));

			return "y".equalsIgnoreCase(line);
		}

		/* (non-Javadoc)
		 * @see org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo#showMessage(java.lang.String)
		 */
		@Override
		public void showMessage(String message) {
			console.printf("message");
		}

		/* (non-Javadoc)
		 * @see org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo#promptPassword(java.lang.String)
		 */
		@Override
		public String promptPassword(String message) {
			return new String(console.readPassword(message + ": "));
		}

		/* (non-Javadoc)
		 * @see org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo#promptPassphrase(java.lang.String)
		 */
		@Override
		public String promptPassphrase(String message) {
			return promptPassword(message);
		}
	}

}
