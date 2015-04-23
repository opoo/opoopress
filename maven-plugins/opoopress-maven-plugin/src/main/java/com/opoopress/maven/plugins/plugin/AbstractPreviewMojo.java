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
import org.mortbay.jetty.Server;
import org.opoo.press.Observer;
import org.opoo.press.impl.SiteConfigImpl;
import org.opoo.press.impl.SiteImpl;
import org.opoo.press.impl.SiteObserver;

/**
 * @author Alex Lin
 *
 */
public class AbstractPreviewMojo extends AbstractServerMojo{
    /**
     * The amount of time in seconds to wait between checks of the site directory.
     *
     * @parameter expression="${interval}" default-value="3"
     */
    private int interval;

    /**
     * Set this to 'true' to skip preview.
     *
     * @parameter expression="${op.preview.skip}" default-value="false"
     */
    private boolean skipPreview;

    private boolean running = true;

    @Override
    protected void executeInternal(SiteConfigImpl config)  throws MojoExecutionException, MojoFailureException{
        if(skipPreview){
            getLog().info("Skipping preview.");
            return;
        }
        super.executeInternal(config);
    }

    @Override
    protected void executeInternal(SiteConfigImpl config, SiteImpl site) throws MojoExecutionException, MojoFailureException {
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

    private void startAll(SiteConfigImpl config, SiteImpl site, long intervalMillis) throws Exception {
        Server server = createJettyServer(site);
        server.start();

        onServerStart(site, server);

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
        config = new SiteConfigImpl(config.getBasedir(), getOverrideConfiguration());
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
}
