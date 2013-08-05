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
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.repository.Repository;
import org.opoo.press.Site;

/**
 * 
 * @author Alex Lin
 */
public abstract class AbstractDeployMojo extends AbstractGenerateMojo{

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
		
		Site site = createSite();
		if(skipGenerate){
			getLog().info( "op.generate.skip = true: Skipping generating" );
		}else{
			generate(site);
		}
		
		File destination = site.getDestination();
		getLog().info("Destination [" + destination + "]");
		getLog().info("Site root [" + site.getRoot() + "]" );
		
		if ( !destination.exists()) {
            throw new MojoExecutionException( "The site does not exist, please run mvn op:generate first" );
        }
		
		deploy(site, destination);
	}
	
	protected void deploy(Site site, File destination) throws MojoExecutionException, MojoFailureException{
		Repository repository = getRepository(site);
		if ( getLog().isDebugEnabled()){
            getLog().debug( "Deploying to '" + repository.getUrl() + "',\n    Using credentials from server id '" + repository.getId() + "'" );
        }
		
		deploy(site, destination, repository );
	}
	
	protected void deploy(Site site, File destination, Repository repository) throws MojoExecutionException, MojoFailureException{
		throw new UnsupportedOperationException("deploy(File, Repository)");
	}
	
	@SuppressWarnings("unchecked")
	private Repository getRepository(Site site) throws MojoExecutionException, MojoFailureException{
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
		
		Properties props = new Properties();
		for(String key: repo.keySet()){
			if("id".equals(key) || "url".equals(key)){
				continue;
			}
			props.setProperty(key, repo.get(key));
		}
		
		Repository repository = new Repository(id, appendSlash(url));
		if(!props.isEmpty()){
			repository.setParameters(props);
		}
		
		return repository;
	}
	
	/**
     * Make sure the given url ends with a slash.
     *
     * @param url a String.
     *
     * @return if url already ends with '/' it is returned unchanged,
     *      otherwise a '/' character is appended.
     */
	static String appendSlash(final String url) {
		if (url.endsWith("/")) {
			return url;
		} else {
			return url + "/";
		}
	}
}
