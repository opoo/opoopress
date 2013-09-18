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
package org.opoo.press.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Using jetty server as preview server.
 * 
 * @author Alex Lin
 *
 */
public class JettyServer extends Server{
	private static final int MAX_IDLE_TIME = 30000;
	private static final Logger log = LoggerFactory.getLogger(JettyServer.class);
	
	JettyServer(Site site, int port) {
		super();
		init(this, site.getDestination().getPath(), site.getRoot(), port);
	}
	
	JettyServer(String resourceBase, String contextPath, int port){
		init(this, resourceBase, contextPath, port);
	}

	void init(Server server, String resourceBase, String contextPath, int port){
		//server.setStopAtShutdown( true );
		server.setStopAtShutdown( false );
		
        Connector defaultConnector = getDefaultConnector(null, port);
        server.setConnectors( new Connector[] { defaultConnector } );

        //RequestLogHandler logHandler = createRequestLogHandler(site);
        
        //contextPath = site.root
        String root = contextPath;//site.getRoot();
		if("".equals(root)){
			ResourceHandler resource_handler = new ResourceHandler();
			// resource_handler.setDirectoriesListed(true);
			resource_handler.setWelcomeFiles(new String[] { "index.html" });

			// resource_handler.setResourceBase("target/public");
			resource_handler.setResourceBase(resourceBase/*site.getDestination().getPath()*/);
			String base = resource_handler.getResourceBase();
			log.info("Server resource base: " + base);
			
			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler()});
			server.setHandler(handlers);
//			server.setHandlers(new Handler[]{handlers, logHandler});
			
			log.info( "Starting Jetty on http://localhost:" + port + "/" );
		}else{
			log.info("Using " + ContextHandler.class.getName());
			ContextHandler contextHandler = new ContextHandler();
			contextHandler.setContextPath(root);
			contextHandler.setHandler(new ResourceHandler());
			contextHandler.setResourceBase(resourceBase/*site.getDestination().getPath()*/);
			server.setHandler(contextHandler);
//			server.setHandlers(new Handler[]{contextHandler, logHandler});
			
			log.info( "Starting Jetty on http://localhost:" + port + root );
		}
	}
	
	public void startAndJoin() throws Exception{
		 try{
            start();
        }
        catch ( Exception e ){
            throw new Exception( "Error executing Jetty: " + e.getMessage(), e );
        }

        // Watch it
        try{
            getThreadPool().join();
        }
        catch ( InterruptedException e ){
            log.warn( "Jetty was interrupted", e );
        }
	}

    RequestLogHandler createRequestLogHandler(Site site){
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
    
    /**
     * 
     * @param host localhost, 127.0.0.1 or 0.0.0.0.
     * @param port
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
}
