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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.opoo.press.impl.ConfigImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Lin
 * //requiresDependencyResolution
 */
abstract class AbstractOpooPressMojo extends AbstractMojo{

    /**
     * Base directory of the project.
     * @parameter expression="${basedir}"
     * @readonly
     * @required
     */
    private File baseDirectory;

    /**
     * Site configuration files string, separated by ','.
     * @parameter expression="${op.config}"
     */
    private String config;


    /**
     * @parameter expression="${debug}"  default-value="false"
     */
    private boolean debug = false;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ConfigImpl configImpl = new ConfigImpl(baseDirectory, getOverrideConfiguration());

            if(configImpl.getConfigFiles().length == 0){
                getLog().info("No OpooPress site to generate.");
                return;
            }

            executeInternal(configImpl);
        }catch(RuntimeException e){
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    protected Map<String,Object> getOverrideConfiguration(){
        Map<String,Object> override = new HashMap<String,Object>();
        if(StringUtils.isNotBlank(config)){
            override.put("config", config);
        }
        if(debug){
            override.put("debug", true);
        }
        return override;
    }

    protected abstract void executeInternal(ConfigImpl config) throws MojoExecutionException, MojoFailureException;
}
