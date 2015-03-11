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
package org.opoo.press.maven.wagon.github;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;

/**
 * Supports url: <code>github://user@github.com/myuser/myproject/</code>
 * 
 * @author Alex Lin
 * @plexus.component role="org.apache.maven.wagon.Wagon" role-hint="github"
 *                   instantiation-strategy="per-lookup"
 */
@SuppressWarnings("unchecked")
public class GitHubWagon extends StreamWagon{
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
		fireSessionDebug("fillInputData");
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.StreamWagon#fillOutputData(org.apache.maven.wagon.OutputData)
	 */
	@Override
	public void fillOutputData(OutputData outputData)
			throws TransferFailedException {
		fireSessionDebug("fillOutputData");
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.StreamWagon#closeConnection()
	 */
	@Override
	public void closeConnection() throws ConnectionException {
		fireSessionDebug("closeConnection");
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.AbstractWagon#openConnectionInternal()
	 */
	@Override
	protected void openConnectionInternal() throws ConnectionException,
			AuthenticationException {
		fireSessionDebug("openConnectionInternal");
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
			fireTransferDebug("Override userName: " + userName + " -> " + repoUsername);
			userName = repoUsername;
		}
		if(StringUtils.isNotBlank(repoPassword)){
			fireTransferDebug("Override password by repository's.");
			password = repoPassword;
		}
		
		fireTransferDebug("userName: " + userName);
		fireTransferDebug("password: " + (password != null ? "***" : ""));
		fireTransferDebug("message: " + message);
		fireTransferDebug("branch: " + branch);
		fireTransferDebug("dryRun: " + dryRun);
		fireTransferDebug("force: " + force);
		fireTransferDebug("host: " + host);
		fireTransferDebug("merge: " + merge);
		fireTransferDebug("noJekyll: " + noJekyll);
		fireTransferDebug("oauth2Token: " + oauth2Token);
		fireTransferDebug("numThreads: " + threads);
		
		GitHub github = new GitHub();
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
		if(StringUtils.isNotBlank(message)){
			github.setMessage(message);
		}
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
