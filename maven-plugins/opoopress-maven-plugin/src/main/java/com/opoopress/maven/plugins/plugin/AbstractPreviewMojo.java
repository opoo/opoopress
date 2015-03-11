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
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.opoo.press.Observer;
import org.opoo.press.Site;
import org.opoo.press.impl.ConfigImpl;
import org.opoo.press.impl.SiteImpl;
import org.opoo.press.impl.SiteObserver;

/**
 * @author Alex Lin
 *
 */
public class AbstractPreviewMojo extends AbstractBuildMojo{
    private static final int MAX_IDLE_TIME = 30000;
    /**
     * The amount of time in seconds to wait between checks of the site directory.
     *
     * @parameter expression="${interval}" default-value="3"
     */
    private int interval;

    /**
     * The port to execute the HTTP server on.
     *
     * @parameter expression="${port}" default-value="8080"
     */
    private int port;

    /**
     * Set this to 'true' to skip preview.
     *
     * @parameter expression="${op.preview.skip}" default-value="false"
     */
    private boolean skipPreview;

    private boolean running = true;

    @Override
    protected void executeInternal(ConfigImpl config)  throws MojoExecutionException, MojoFailureException{
        if(skipPreview){
            getLog().info("Skipping preview.");
            return;
        }
        super.executeInternal(config);
    }

    @Override
    protected void executeInternal(ConfigImpl config, SiteImpl site) throws MojoExecutionException, MojoFailureException {
        if(interval < 3){
            interval = 3;
        }
        long intervalMillis = interval * 1000L;

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                stopAll();
            }
        });


        try {
            startAll(config, site, intervalMillis);
        } catch (Exception e) {
            throw new MojoExecutionException("Start preview failed: " + e.getMessage(), e);
        }
    }

    private void startAll(ConfigImpl config, SiteImpl site, long intervalMillis) throws Exception {
        Server server = createJettyServer(site);
        server.start();

        String path = "".equals(site.getRoot()) ? "/" : site.getRoot();
        getLog().info( "Starting preview server on http://localhost:" + port + path);
        getLog().info("Press Ctrl+C to stop.");

        Observer observer = site.getObserver();
        observer.initialize();
        running = true;
        while(running){
            try {
                Thread.sleep(intervalMillis);
                if(!running){
                    break;
                }
                observer.check();
            }catch (SiteObserver.ConfigChangedException e){
                break;
            }
        }

        getLog().info("Destroy all observers.");
        observer.destroy();

        getLog().info("Stopping jetty server...");
        server.stop();

        if(!running){
            return;
        }

        //start again
        config = new ConfigImpl(config.getBasedir(), getOverrideConfiguration());
        if(config.getConfigFiles().length == 0){
            throw new Exception("No valid OpooPress configuration file.");
        }
        site = new SiteImpl(config);
        site.build(true);
        startAll(config, site, intervalMillis);
    }

    public void stopAll(){
        if(!running){
            throw new IllegalStateException("Thread is not running");
        }
        running = false;
        Thread.currentThread().interrupt();
    }

    Server createJettyServer(Site site){
        Server server = new Server();
        //FIXME
        server.setStopAtShutdown(true);

        Connector defaultConnector = getDefaultConnector(null, port);
        server.setConnectors( new Connector[] { defaultConnector } );

        String root = site.getRoot();
        String resourceBase = site.getDestination().getPath();
        if("".equals(root)){
            ResourceHandler resourceHandler = new ResourceHandler();
            // resource_handler.setDirectoriesListed(true);
            resourceHandler.setWelcomeFiles(new String[]{"index.html"});

            // resource_handler.setResourceBase("target/public");
            resourceHandler.setResourceBase(resourceBase/*site.getDestination().getPath()*/);
            String base = resourceHandler.getResourceBase();
            getLog().info("Server resource base: " + base);

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { resourceHandler, new DefaultHandler()});
            server.setHandler(handlers);
//			server.setHandlers(new Handler[]{handlers, logHandler});
//            getLog().info( "Starting preview server on http://localhost:" + port + "/" );
        } else {
            getLog().info("Using " + ContextHandler.class.getName());
            ContextHandler contextHandler = new ContextHandler();
            contextHandler.setContextPath(root);
            contextHandler.setHandler(new ResourceHandler());
            contextHandler.setResourceBase(resourceBase/*site.getDestination().getPath()*/);
            server.setHandler(contextHandler);
//			server.setHandlers(new Handler[]{contextHandler, logHandler});
//            log.info( "Starting preview server on http://localhost:" + port + root );
        }
        return server;
    }

    /**
     *
     * @param host localhost, 127.0.0.1 or 0.0.0.0.
     * @param port port
     * @return jetty connector
     */
    private Connector getDefaultConnector(String host, int port){
        Connector connector = new SelectChannelConnector();
        if(host != null){
            connector.setHost(host);
        }
        connector.setPort( port );
        connector.setMaxIdleTime( MAX_IDLE_TIME );
        return connector;
    }

    RequestLogHandler createRequestLogHandler(){
        boolean showRequestLog = Boolean.getBoolean("requestLog");
        if(!showRequestLog){
            return null;
        }

        NCSARequestLog requestLog = new NCSARequestLog();
        requestLog.setLogDateFormat(null);

        RequestLogHandler logHandler = new RequestLogHandler();
        logHandler.setRequestLog(requestLog);

        return logHandler;
    }
}
