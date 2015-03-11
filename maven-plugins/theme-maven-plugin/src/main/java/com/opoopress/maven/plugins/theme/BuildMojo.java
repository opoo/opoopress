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
package com.opoopress.maven.plugins.theme;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.opoo.press.ResourceBuilder;
import org.opoo.press.impl.ThemeImpl;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 * @goal build
 * @phase compile
 * @requiresProject
 */
public class BuildMojo extends AbstractMojo implements Contextualizable {
    /**
     * Base directory of the project.
     *
     * @parameter expression="${basedir}"
     * @readonly
     * @required
     */
    private File basedir;

    private PlexusContainer container;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //check configuration file
        File configFile = new File(basedir, ThemeImpl.RESOURCE_BUILDERS_CONFIGURATION_FILENAME);
        if(!configFile.exists()){
            getLog().warn("Resource builders configuration file '"
                    + ThemeImpl.RESOURCE_BUILDERS_CONFIGURATION_FILENAME
                    + "' not found, skip build.");
            return;
        }

        //read configuration
        List<Map<String,Object>> resourceBuildersConfig = null;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(configFile);
            resourceBuildersConfig = new Yaml().loadAs(stream, List.class);
        } catch (FileNotFoundException e) {
            throw new MojoFailureException("Theme config file not found: " + configFile, e);
        }finally{
            IOUtils.closeQuietly(stream);
        }

        //create builders
        List<ResourceBuilder> builders = new ArrayList<ResourceBuilder>();
        for(Map<String,Object> buildConfig: resourceBuildersConfig){
            ResourceBuilder resourceBuilder = createResourceBuilder(buildConfig);
            getLog().debug("Initializing resource builder: " + resourceBuilder);
            resourceBuilder.init(basedir, buildConfig);
            builders.add(resourceBuilder);
        }

        //build
        for(ResourceBuilder builder: builders){
            try {
                getLog().info("Building resource: " + builder);
                builder.build();
            } catch (Exception e) {
                throw new MojoFailureException("Build resource failed: " + e.getMessage(), e);
            }
        }
    }

    private ResourceBuilder createResourceBuilder(Map<String, Object> buildConfig) throws MojoFailureException {
        String type = (String) buildConfig.get("type");
        if(StringUtils.isBlank(type)){
            throw new MojoFailureException("The type of resource builder is required.");
        }

        try {
            return (ResourceBuilder) container.lookup(ResourceBuilder.class.getName(), type);
        } catch (ComponentLookupException e) {
            throw new MojoFailureException("Resource builder not found in plexus container: " + type, e);
        }
    }

    @Override
    public void contextualize(Context context) throws ContextException {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
}
