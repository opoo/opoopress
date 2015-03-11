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
package org.opoo.press.maven.wagon.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.util.FileUtils;

/**
 * Supports url: <code>git:ssh://user@github.com/myuser/myproject.git</code>,
 * <code>git:https://github.com/myuser/myproject.git</code>
 * 
 * @author Alex Lin
 * @plexus.component role="org.apache.maven.wagon.Wagon" role-hint="git"
 *                   instantiation-strategy="per-lookup"
 */
@SuppressWarnings("unchecked")
public class GitWagon extends AbstractWagon{
	
	//is true, checkout repository every time
	private boolean safeCheckout = false;
	private File checkoutDirectory;
	
	public GitWagon() {
		super();
		safeCheckout = "true".equals(System.getProperty("wagon.git.safe.checkout", "true"));
		fireSessionDebug("wagon.git.safe.checkout: " + safeCheckout);
	}

	/**
     * Get the directory where Wagon will checkout files from SCM.
     *
     * @return directory
     */
    public File getCheckoutDirectory() {
        return checkoutDirectory;
    }

	/**
     * @return true
     */
    public boolean supportsDirectoryCopy(){
        return true;
    }
    
	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.Wagon#get(java.lang.String, java.io.File)
	 */
	@Override
	public void get(String resourceName, File destination)
			throws TransferFailedException, ResourceDoesNotExistException,
			AuthorizationException {
		Resource resource = new Resource(resourceName);
		fireGetInitiated(resource, destination);
		fireGetStarted(resource, destination);

		try {
			File remote = new File(checkoutDirectory, resourceName);
			if (remote.exists()){
				transfer(resource, new FileInputStream(remote), new FileOutputStream(destination), TransferEvent.REQUEST_GET);
			}
		} catch (Exception e) {
			fireTransferError(resource, e, TransferEvent.REQUEST_GET);
			throw new TransferFailedException("Unable to get file", e);
		}

		fireGetCompleted(resource, destination);
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.Wagon#getIfNewer(java.lang.String, java.io.File, long)
	 */
	@Override
	public boolean getIfNewer(String resourceName, File destination,
			long timestamp) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		throw new UnsupportedOperationException( "Not currently supported: getIfNewer" );
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.Wagon#put(java.io.File, java.lang.String)
	 */
	@Override
	public void put(File source, String destination)
			throws TransferFailedException, ResourceDoesNotExistException,
			AuthorizationException {
        if ( source.isDirectory() ) {
            throw new IllegalArgumentException( "Source is a directory: " + source );
        }
        
		String resourceName = FilenameUtils.separatorsToUnix(destination);
		Resource resource = new Resource(resourceName);

		firePutInitiated(resource, source);
		firePutStarted(resource, source);

		try {

			File file = new File(checkoutDirectory, destination);
			file.getParentFile().mkdirs();
			transfer(resource, source, new FileOutputStream(file), true);

		} catch (Exception e) {
			fireTransferError(resource, e, TransferEvent.REQUEST_PUT);
			throw new TransferFailedException("Unable to put file", e);
		}

		firePutCompleted(resource, source);
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.AbstractWagon#openConnectionInternal()
	 */
	@Override
	protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
		if (checkoutDirectory == null) {
			checkoutDirectory = createCheckoutDirectory();
		}

		if (checkoutDirectory.exists() && safeCheckout) {
			removeCheckoutDirectory();
		}

		checkoutDirectory.mkdirs();
	}

	/* (non-Javadoc)
	 * @see org.apache.maven.wagon.AbstractWagon#closeConnection()
	 */
	@Override
	protected void closeConnection() throws ConnectionException {
		if(safeCheckout){
			removeCheckoutDirectory();
		}
	}
	
	public void putDirectory(File sourceDirectory, String destinationDirectory) 
			throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		if (!sourceDirectory.isDirectory()) {
			throw new IllegalArgumentException("Source is not a directory: " + sourceDirectory);
		}
		String resourceName = FilenameUtils.separatorsToUnix(destinationDirectory);
		Resource resource = new Resource(resourceName);
		firePutInitiated(resource, sourceDirectory);
		firePutStarted(resource, sourceDirectory);
		
		Repository repo = getRepository();
		String url = repo.getUrl();

		if (url.endsWith("/")){
			url = url.substring(0, url.length() - 1);
		}

		String remote = url.substring(4);
		String branch = repo.getParameter("branch");
		String message = repo.getParameter("message");

		if(remote.startsWith("default://")){
			remote = remote.substring(10);
		}
		
		if(branch == null){
			branch = "master";
		}
		
		try {
			Git git = new Git(checkoutDirectory, remote, branch);
			
			if(message != null){
				git.setMessage(message);
			}
			
			if(safeCheckout){//not cache, clone every time
				git.cloneAll();
			}else{
				git.pullAll();
			}
			
			FileUtils.copyDirectoryStructure(sourceDirectory, new File(checkoutDirectory, destinationDirectory));
			
			git.pushAll();
		} catch (Exception e) {
			fireTransferError(resource, e, TransferEvent.REQUEST_PUT);
			throw new TransferFailedException("Unable to put file", e);
		}

		firePutCompleted(resource, sourceDirectory);
	}
	
	private void removeCheckoutDirectory() throws ConnectionException {
		if (checkoutDirectory == null) {
			return; // Silently return.
		}
		try {
			FileUtils.deleteDirectory(checkoutDirectory);
		} catch (IOException e) {
			throw new ConnectionException("Unable to cleanup checkout directory", e);
		}
	}
	
	private File createCheckoutDirectory() {
		if(!safeCheckout){
			String url = getRepository().getUrl();
			try {
				return new File(System.getProperty("java.io.tmpdir"), "wagon-git-" + md5(url));
			} catch (Exception e) {
				fireSessionDebug("Create static checkout directory for '" + url + "' error, try create random directory again.");
				safeCheckout = true;
			}
		}
		
		//if safe checkout, create new directory every time
		File checkoutDirectory = null;
		DecimalFormat fmt = new DecimalFormat("#####");
		Random rand = new Random(System.currentTimeMillis() + Runtime.getRuntime().freeMemory());
		synchronized (rand) {
			do {
				checkoutDirectory = new File(System.getProperty("java.io.tmpdir"), "wagon-git"
						+ fmt.format(Math.abs(rand.nextInt()))
						+ ".checkout");
			} while (checkoutDirectory.exists());
		}
		return checkoutDirectory;
	}
	
	private static String md5(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("MD5");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++){
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}
