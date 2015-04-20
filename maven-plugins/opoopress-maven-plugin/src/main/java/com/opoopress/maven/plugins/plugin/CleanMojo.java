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
import org.opoo.press.impl.SiteConfigImpl;
import org.opoo.press.impl.SiteImpl;

/**
 * @author Alex Lin
 * @goal clean
 */
public class CleanMojo extends AbstractOpooPressMojo{
    /**
     * @parameter expression="${op.clean.skip}" default-value="false"
     */
    private boolean skipClean = false;

    @Override
    protected void executeInternal(SiteConfigImpl config) throws MojoExecutionException, MojoFailureException {
        if(skipClean){
            getLog().info("Skipping clean site.");
            return;
        }
        SiteImpl site = new SiteImpl(config);

        try {
            site.getTheme().clean();
            site.clean();
        } catch (Exception e) {
            throw new MojoFailureException("Clean site failed.", e);
        }
    }
}
