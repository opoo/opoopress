/*
 * Copyright 2013-2015 Alex Lin and Apache maven-site-plugin project.
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

import freemarker.template.utility.StringUtil;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Profile;
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
import org.opoo.press.impl.ConfigImpl;
import org.opoo.press.impl.SiteImpl;
import org.opoo.util.ChainingClassLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Alex Lin
 * @author <a href="mailto:michal@org.codehaus.org">Michal Maczka</a>
 */
public class AbstractDeployMojo extends AbstractBuildMojo implements Contextualizable {
    /**
     * Set this to 'true' to skip deploy.
     *
     * @parameter expression="${op.deploy.skip}" default-value="false"
     */
    private boolean skipDeploy;


    /**
     * Extra deploy repository configurations. format: <code>id::url[,id::url]</code>.
     *
     * @parameter expression="${op.deploy.repos}"
     */
    private String deployRepositories;

    /**
     * Whether to run the "chmod" command on the remote site after the deploy.
     * Defaults to "true".
     *
     * @parameter expression="${op.chmod}" default-value="true"
     * @since 2.1
     */
    private boolean chmod;

    /**
     * The mode used by the "chmod" command. Only used if chmod = true. Defaults
     * to "g+w,a+rX".
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

    @Override
    protected void executeInternal(ConfigImpl config) throws MojoExecutionException, MojoFailureException {
        if (skipDeploy) {
            getLog().info("Skipping deploy.");
            return;
        }
        showDrafts = false;
        super.executeInternal(config);
    }

    @Override
    protected void executeInternal(ConfigImpl config, SiteImpl site) throws MojoExecutionException, MojoFailureException {
        File destination = site.getDestination();
        getLog().info("Destination [" + destination + "]");
        getLog().info("Site root [" + site.getRoot() + "]");

        if (!destination.exists()) {
            throw new MojoFailureException("The site output folder does not exist, please run mvn op:build first");
        }

        List<Map<String, String>> deployList = getDeployRepositoryList(config); //config.get("deploy");

        if (deployList == null || deployList.isEmpty()) {
            throw new MojoFailureException("Deploy configuration not found in config.yml");
        }

        for (Map<String, String> deployRepo : deployList) {
            Repository repository = createRepository(deployRepo);
            deploy(site, destination, repository);
        }
    }

    private List<Map<String,String>> getDeployRepositoryList(ConfigImpl config){
        List<Map<String, String>> deployList = config.get("deploy");

        if(deployRepositories != null){
            String[] split = StringUtil.split(deployRepositories, ',');
            for (String str: split){
                String[] strings = str.split("::");
                if(strings.length == 2){
                    Map<String,String> map = new HashMap<String, String>();
                    map.put("id", strings[0]);
                    map.put("url", strings[1]);

                    if(deployList == null){
                        deployList = new ArrayList<Map<String, String>>();
                    }
                    deployList.add(map);
                }
            }
        }

        return deployList;
    }


    private Repository createRepository(Map<String, String> repo) throws MojoExecutionException, MojoFailureException {
        String id = repo.get("id");
        String url = repo.get("url");

        if (id == null || url == null) {
            throw new MojoFailureException("Deploy configuration must contains 'id' and 'url': " + repo);
        }

        url = resolveRepositoryURL(id, url);

        Properties props = new Properties();
        for (String key : repo.keySet()) {
            if ("id".equals(key) || "url".equals(key)) {
                continue;
            }
            props.setProperty(key, repo.get(key));
        }

        Repository repository = new Repository(id, appendSlash(url));
        if (!props.isEmpty()) {
            repository.setParameters(props);
        }
        return repository;
    }

    private String resolveRepositoryURL(String repositoryId, String repositoryURL) throws MojoFailureException {
        //1. starts with '${' and ends with '}'
        //if(repositoryURL.startsWith("${") && repositoryURL.endsWith("}")){
        //    return resolveRepositoryURL(repositoryId, repositoryURL.substring(2, repositoryURL.length() - 1));
        //}

        //2. more properties holder
        if(repositoryURL.contains("${") && repositoryURL.contains("}")){
            Set<String> propertyNames = getPropertyNames(repositoryURL);
            for(String propertyName: propertyNames){
                String value = getPropertyValue(propertyName);
                repositoryURL = repositoryURL.replace("${" + propertyName + "}", value);
            }
        }
        return repositoryURL;
    }

    private Set<String> getPropertyNames(String url) throws MojoFailureException {
        Set<String> set = new HashSet<String>();
        int fromIndex = 0;
        while(true) {
            int start = url.indexOf("${", fromIndex);
            if(start == -1){
                break;
            }

            int end = url.indexOf("}", start + 2);
            if(end == -1){
                throw new MojoFailureException("Invalid deploy repository url: " + url);
            }

            fromIndex = end + 2;

            String prop = url.substring(start + 2,  end);
            set.add(prop);
        }
        return set;
    }

    private String getPropertyValue(String propertyName) throws MojoFailureException {
        Map<String, Profile> profiles = settings.getProfilesAsMap();
        List<String> activeProfiles = settings.getActiveProfiles();
        for(String id: activeProfiles){
            Profile profile = profiles.get(id);
            if(profile != null){
                Properties properties = profile.getProperties();
                if(properties != null){
                    String property = properties.getProperty(propertyName);
                    if(property != null){
                        getLog().info("Resolve deploy repository url: " + propertyName + " => " + property);
                        return property;
                    }
                }
            }
        }

        for(Profile profile: settings.getProfiles()){
            if(profile.getActivation().isActiveByDefault()){
                Properties properties = profile.getProperties();
                if(properties != null){
                    String property = properties.getProperty(propertyName);
                    if(property != null){
                        getLog().info("Resolve deploy repository url: " + propertyName + " => " + property);
                        return property;
                    }
                }
            }
        }

        throw new MojoFailureException("Can not resolve deploy repository url: " + propertyName);
    }

    static String appendSlash(final String url) {
        if (url.endsWith("/")) {
            return url;
        } else {
            return url + "/";
        }
    }

    private void deploy(SiteImpl site, File directory, Repository repository) throws MojoExecutionException, MojoFailureException {
        // TODO: work on moving this into the deployer like the other deploy
        // methods
        final Wagon wagon = getWagon(repository, wagonManager);

        try {
            configureWagon(wagon, repository.getId(), settings, container, getLog());
        } catch (WagonConfigurationException e) {
            throw new MojoExecutionException("Unable to configure Wagon: '" + repository.getProtocol() + "'", e);
        }

        String relativeDir = site.getRoot();
        if ("".equals(relativeDir)) {
            relativeDir = "./";
        } else if (relativeDir.startsWith("/")) {
            relativeDir = relativeDir.substring(1);
        }

        try {
            final ProxyInfo proxyInfo = getProxyInfo(repository, wagonManager);
            push(directory, repository, wagonManager, wagon, proxyInfo, relativeDir, getLog());

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

    private static void configureScpWagonIfRequired(Wagon wagon, Log log) {
        log.debug("configureScpWagonIfRequired: " + wagon.getClass().getName());

        if (System.console() == null) {
            log.debug("No System.console(), skip configure Wagon");
            return;
        }

        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) {
            parent = AbstractDeployMojo.class.getClassLoader();
        }
        List<ClassLoader> loaders = new ArrayList<ClassLoader>();
        loaders.add(wagon.getClass().getClassLoader());
        ChainingClassLoader loader = new ChainingClassLoader(parent, loaders);

        Class<?> scpWagonClass;
        try {
            scpWagonClass = ClassUtils.getClass(loader, "org.apache.maven.wagon.providers.ssh.jsch.ScpWagon");
        } catch (ClassNotFoundException e) {
            log.debug("Class 'org.apache.maven.wagon.providers.ssh.jsch.ScpWagon' not found, skip configure Wagon.");
            return;
        }

        //is ScpWagon
        if (scpWagonClass.isInstance(wagon)) {
            try {
                Class<?> userInfoClass = ClassUtils.getClass(loader, "com.opoopress.maven.plugins.plugin.ssh.SystemConsoleInteractiveUserInfo");
                Object userInfo = userInfoClass.newInstance();
                MethodUtils.invokeMethod(wagon, "setInteractiveUserInfo", userInfo);
                log.debug("ScpWagon using SystemConsoleInteractiveUserInfo(Java 6+).");
            } catch (ClassNotFoundException e) {
                log.debug("Class 'com.opoopress.maven.plugins.plugin.ssh.SystemConsoleInteractiveUserInfo' not found, skip configure Wagon.");
            } catch (InstantiationException e) {
                log.debug("Instantiate class exception", e);
            } catch (IllegalAccessException e) {
                log.debug(e.getMessage(), e);
            } catch (NoSuchMethodException e) {
                log.debug(e.getMessage(), e);
            } catch (InvocationTargetException e) {
                log.debug(e.getMessage(), e);
            }
        } else {
            log.debug("Not a ScpWagon.");
        }
    }

    private static Wagon getWagon(final Repository repository, final WagonManager manager)
            throws MojoExecutionException {
        final Wagon wagon;

        try {
            wagon = manager.getWagon(repository);
        } catch (UnsupportedProtocolException e) {
            throw new MojoExecutionException("Unsupported protocol: '" + repository.getProtocol() + "'", e);
        } catch (WagonConfigurationException e) {
            throw new MojoExecutionException("Unable to configure Wagon: '" + repository.getProtocol() + "'", e);
        }

        if (!wagon.supportsDirectoryCopy()) {
            throw new MojoExecutionException("Wagon protocol '" + repository.getProtocol() + "' doesn't support directory copying");
        }

        return wagon;
    }

    private static void push(final File inputDirectory,
                             final Repository repository, final WagonManager manager,
                             final Wagon wagon, final ProxyInfo proxyInfo,
                             final String relativeDir,
                             final Log log) throws MojoExecutionException {
        AuthenticationInfo authenticationInfo = manager.getAuthenticationInfo(repository.getId());
        log.debug("authenticationInfo with id '" + repository.getId() + "': "
                + ((authenticationInfo == null) ? "-" : authenticationInfo.getUserName()));

        try {
            Debug debug = new Debug();

            wagon.addSessionListener(debug);

            wagon.addTransferListener(debug);

            if (proxyInfo != null) {
                log.debug("connect with proxyInfo");
                wagon.connect(repository, authenticationInfo, proxyInfo);
            } else if (proxyInfo == null && authenticationInfo != null) {
                log.debug("connect with authenticationInfo and without proxyInfo");
                wagon.connect(repository, authenticationInfo);
            } else {
                log.debug("connect without authenticationInfo and without proxyInfo");
                wagon.connect(repository);
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

    /**
     * Configure the Wagon with the information from serverConfigurationMap ( which comes from settings.xml )
     *
     * @param wagon
     * @param repositoryId
     * @param settings
     * @param container
     * @param log
     * @throws WagonConfigurationException
     * @todo Remove when {@link WagonManager#getWagon(Repository) is available}. It's available in Maven 2.0.5.
     */
    private static void configureWagon(Wagon wagon, String repositoryId, Settings settings, PlexusContainer container, Log log)
            throws WagonConfigurationException {
        log.debug(" configureWagon ");

        //config log
        configureLog(wagon, log);

        configureScpWagonIfRequired(wagon, log);

        // MSITE-25: Make sure that the server settings are inserted
        for (Server server : settings.getServers()) {
            String id = server.getId();

            log.debug("configureWagon server " + id);

            if (id != null && id.equals(repositoryId) && (server.getConfiguration() != null)) {
                final PlexusConfiguration plexusConf =
                        new XmlPlexusConfiguration((Xpp3Dom) server.getConfiguration());

                ComponentConfigurator componentConfigurator = null;
                try {
                    componentConfigurator = (ComponentConfigurator) container.lookup(ComponentConfigurator.ROLE);
                    componentConfigurator.configureComponent(wagon, plexusConf, container.getContainerRealm());
                } catch (final ComponentLookupException e) {
                    throw new WagonConfigurationException(repositoryId, "Unable to lookup wagon configurator."
                            + " Wagon configuration cannot be applied.", e);
                } catch (ComponentConfigurationException e) {
                    throw new WagonConfigurationException(repositoryId, "Unable to apply wagon configuration.", e);
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


    /**
     * @param wagon
     * @param log
     */
    private static void configureLog(Wagon wagon, Log log) {
        try {
            Method method = wagon.getClass().getMethod("setLog", Log.class);
            method.invoke(wagon, log);
            log.info("Set log for wagon: " + wagon);
        } catch (Exception e) {
            log.debug("Wagon does not supports setLog() method.");
        }
    }

    public static ProxyInfo getProxyInfo(Repository repository, WagonManager wagonManager) {
        ProxyInfo proxyInfo = wagonManager.getProxy(repository.getProtocol());

        if (proxyInfo == null) {
            return null;
        }

        String host = repository.getHost();
        String nonProxyHostsAsString = proxyInfo.getNonProxyHosts();
        for (String nonProxyHost : StringUtils.split(nonProxyHostsAsString, ",;|")) {
            if (org.apache.commons.lang.StringUtils.contains(nonProxyHost, "*")) {
                // Handle wildcard at the end, beginning or middle of the nonProxyHost
                final int pos = nonProxyHost.indexOf('*');
                String nonProxyHostPrefix = nonProxyHost.substring(0, pos);
                String nonProxyHostSuffix = nonProxyHost.substring(pos + 1);
                // prefix*
                if (StringUtils.isNotEmpty(nonProxyHostPrefix) && host.startsWith(nonProxyHostPrefix)
                        && StringUtils.isEmpty(nonProxyHostSuffix)) {
                    return null;
                }
                // *suffix
                if (StringUtils.isEmpty(nonProxyHostPrefix)
                        && StringUtils.isNotEmpty(nonProxyHostSuffix) && host.endsWith(nonProxyHostSuffix)) {
                    return null;
                }
                // prefix*suffix
                if (StringUtils.isNotEmpty(nonProxyHostPrefix) && host.startsWith(nonProxyHostPrefix)
                        && StringUtils.isNotEmpty(nonProxyHostSuffix) && host.endsWith(nonProxyHostSuffix)) {
                    return null;
                }
            } else if (host.equals(nonProxyHost)) {
                return null;
            }
        }
        return proxyInfo;
    }

    @Override
    public void contextualize(Context context) throws ContextException {
        container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    }
}
