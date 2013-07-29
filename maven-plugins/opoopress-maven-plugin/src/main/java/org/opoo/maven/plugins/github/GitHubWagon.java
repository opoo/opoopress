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
package org.opoo.maven.plugins.github;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.opoo.maven.plugins.logging.LogAware;

/**
 * <url>github://user@github.com/myuser/myproject/</url>
 * 
 * @author Alex Lin
 * @plexus.component role="org.apache.maven.wagon.Wagon" role-hint="github"
 *                   instantiation-strategy="per-lookup"
 */
@SuppressWarnings("unchecked")
public class GitHubWagon extends StreamWagon implements LogAware{
	private Log log = new SystemStreamLog();
	
	/**
	 * @param log the log to set
	 */
	public void setLog(Log log) {
		this.log = log;
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.AbstractWagon#supportsDirectoryCopy()
	 */
	@Override
	public boolean supportsDirectoryCopy() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.StreamWagon#fillInputData(org.apache.maven.wagon.InputData)
	 */
	@Override
	public void fillInputData(InputData inputData)
			throws TransferFailedException, ResourceDoesNotExistException,
			AuthorizationException {
		log.debug("fillInputData");
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.StreamWagon#fillOutputData(org.apache.maven.wagon.OutputData)
	 */
	@Override
	public void fillOutputData(OutputData outputData)
			throws TransferFailedException {
		log.debug("fillOutputData");
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.StreamWagon#closeConnection()
	 */
	@Override
	public void closeConnection() throws ConnectionException {
		log.debug("closeConnection");
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.AbstractWagon#openConnectionInternal()
	 */
	@Override
	protected void openConnectionInternal() throws ConnectionException,
			AuthenticationException {
		log.debug("openConnectionInternal");
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.AbstractWagon#putDirectory(java.io.File, java.lang.String)
	 */
	@Override
	public void putDirectory(File sourceDirectory, String destinationDirectory)
			throws TransferFailedException, ResourceDoesNotExistException,
			AuthorizationException {
		String userName = getAuthenticationInfo().getUserName();
		String password = getAuthenticationInfo().getPassword();
		Repository repo = getRepository();
	
		String[] strings = StringUtils.split(repo.getBasedir(), '/');
		if(strings.length != 2){
			throw new IllegalArgumentException("GitHub url error, no repository owner or name.");
		}
		
		String repositoryName = strings[1];
		String repositoryOwner = strings[0];
		
		String branch = repo.getParameter("branch");
		String dryRun = repo.getParameter("dryRun");
		String force = repo.getParameter("force");
		String host = repo.getParameter("host");
		String merge = repo.getParameter("merge");
		String message = repo.getParameter("message");
		String noJekyll = repo.getParameter("noJekyll");
		String oauth2Token = repo.getParameter("oauth2Token");
		String repoUsername = repo.getUsername();
		String repoPassword = repo.getPassword();
		String threads = System.getProperty("threads", "1");
		
		if(StringUtils.isNotBlank(repoUsername)){
			log.info("Override userName: " + userName + " -> " + repoUsername);
			userName = repoUsername;
		}
		if(StringUtils.isNotBlank(repoPassword)){
			log.info("Override password by repository's.");
			password = repoPassword;
		}
		
		log.debug("userName: " + userName);
		log.debug("password: " + (password != null ? "***" : ""));
		log.debug("message: " + message);
		log.debug("branch: " + branch);
		log.debug("dryRun: " + dryRun);
		log.debug("force: " + force);
		log.debug("host: " + host);
		log.debug("merge: " + merge);
		log.debug("noJekyll: " + noJekyll);
		log.debug("oauth2Token: " + oauth2Token);
		log.debug("numThreads: " + threads);
		
		GitHub github = new GitHub(log);
		github.setUserName(userName);
		github.setPassword(password);
		github.setRepositoryName(repositoryName);
		github.setRepositoryOwner(repositoryOwner);
		
		if(StringUtils.isNotBlank(branch)){
			github.setBranch(branch);
		}
		if(StringUtils.isNotBlank(dryRun)){
			github.setDryRun("true".equals(dryRun));
		}
//		github.setExcludes(excludes);
		if(StringUtils.isNotBlank(force)){
			github.setForce("force".equals(force));
		}
		if(StringUtils.isNotBlank(host)){
			github.setHost(host);
		}
//		github.setIncludes(includes);
		if(StringUtils.isNotBlank(merge)){
			github.setMerge("true".equals(merge));
		}
		github.setMessage(message);
		if(StringUtils.isNotBlank(noJekyll)){
			github.setNoJekyll("true".equals(noJekyll));
		}
		if(StringUtils.isNotBlank(oauth2Token)){
			github.setOauth2Token(oauth2Token);
		}
		if(StringUtils.isNumeric(threads)){
			github.setNumThreads(Integer.parseInt(threads));
		}
		
		try {
			github.deploy(sourceDirectory, destinationDirectory);
		} catch (GitHubException e) {
			throw new TransferFailedException(e.getMessage(), e);
		}
	}
}
