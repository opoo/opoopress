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
import org.opoo.press.Site;

/**
 * @author Alex Lin
 */
public class AbstractServerMojo extends AbstractBuildMojo{
    private static final int MAX_IDLE_TIME = 30000;

    /**
     * The port to execute the HTTP server on.
     *
     * @parameter expression="${port}" default-value="8080"
     */
    private int port;


    protected Server createJettyServer(Site site){
        Server server = new Server();
        //FIXME
        server.setStopAtShutdown(true);

        Connector defaultConnector = createConnector(null, port);
        server.setConnectors( new Connector[] { defaultConnector } );

        String root = site.getRoot();
        String resourceBase = site.getDestination().getPath();
        getLog().info("Server resource base: " + resourceBase);

        if("".equals(root)){
            ResourceHandler resourceHandler = new ResourceHandler();
            //resourceHandler.setDirectoriesListed(true);
            resourceHandler.setWelcomeFiles(new String[]{"index.html"});

            resourceHandler.setResourceBase(resourceBase/*site.getDestination().getPath()*/);

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { resourceHandler, new DefaultHandler()});
            server.setHandler(handlers);
            //server.setHandlers(new Handler[]{handlers, logHandler});
            //getLog().info( "Startisng preview server on http://localhost:" + port + "/" );
        } else {
            getLog().info("Using " + ContextHandler.class.getName());
            ContextHandler contextHandler = new ContextHandler();
            contextHandler.setContextPath(root);
            contextHandler.setHandler(new ResourceHandler());
            contextHandler.setResourceBase(resourceBase/*site.getDestination().getPath()*/);
            //server.setHandler(contextHandler);
            server.setHandlers(new Handler[]{contextHandler, new DefaultHandler()});
            //server.setHandlers(new Handler[]{contextHandler, logHandler});
            //log.info( "Starting preview server on http://localhost:" + port + root );
        }
        return server;
    }

    /**
     *
     * @param host localhost, 127.0.0.1 or 0.0.0.0.
     * @param port port
     * @return jetty connector
     */
    protected Connector createConnector(String host, int port){
        Connector connector = new SelectChannelConnector();
        if(host != null){
            connector.setHost(host);
        }
        connector.setPort( port );
        connector.setMaxIdleTime(MAX_IDLE_TIME);
        return connector;
    }

    protected RequestLogHandler createRequestLogHandler(){
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

    protected void onServerStart(Site site, Server server){
        String path = "".equals(site.getRoot()) ? "/" : site.getRoot();
        getLog().info("Starting server on http://localhost:" + port + path);
        getLog().info("Press Ctrl+C to stop.");
    }
}
