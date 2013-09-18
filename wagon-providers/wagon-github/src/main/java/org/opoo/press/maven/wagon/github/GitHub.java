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

import static org.eclipse.egit.github.core.Blob.ENCODING_BASE64;
import static org.eclipse.egit.github.core.TreeEntry.MODE_BLOB;
import static org.eclipse.egit.github.core.TreeEntry.TYPE_BLOB;
import static org.eclipse.egit.github.core.TypedResource.TYPE_COMMIT;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.DirectoryScanner;
import org.eclipse.egit.github.core.Blob;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.TypedResource;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.util.EncodingUtils;

/**
 * Pure java GitHub client wagon.
 * @author Alex Lin
 * @author Kevin Sawicki (kevin@github.com)
 */
public class GitHub{
	private static final Log log = LogFactory.getLog(GitHub.class);

	/**
	 * BRANCH_DEFAULT
	 */
	public static final String BRANCH_DEFAULT = "refs/heads/gh-pages";

	/**
	 * NO_JEKYLL_FILE
	 */
	public static final String NO_JEKYLL_FILE = ".nojekyll";

	private String branch = BRANCH_DEFAULT;

	private String message;

	private String repositoryName;

	private String repositoryOwner;

	private String userName;

	private String password;

	private String oauth2Token;

	private String host;

	private String[] includes;

	private String[] excludes;

	private boolean force;

	private boolean noJekyll = true;

	private boolean merge;

	private boolean dryRun;
	
	private int numThreads = 1;

	/**
	 * @param branch the branch to set
	 */
	public void setBranch(String branch) {
		if(!branch.startsWith("refs/heads/")){
			branch = "refs/heads/" + branch;
		}
		this.branch = branch;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param repositoryName the repositoryName to set
	 */
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	/**
	 * @param repositoryOwner the repositoryOwner to set
	 */
	public void setRepositoryOwner(String repositoryOwner) {
		this.repositoryOwner = repositoryOwner;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param oauth2Token the oauth2Token to set
	 */
	public void setOauth2Token(String oauth2Token) {
		this.oauth2Token = oauth2Token;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param includes the includes to set
	 */
	public void setIncludes(String[] includes) {
		this.includes = includes;
	}

	/**
	 * @param excludes the excludes to set
	 */
	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}

	/**
	 * @param force the force to set
	 */
	public void setForce(boolean force) {
		this.force = force;
	}

	/**
	 * @param noJekyll the noJekyll to set
	 */
	public void setNoJekyll(boolean noJekyll) {
		this.noJekyll = noJekyll;
	}

	/**
	 * @param merge the merge to set
	 */
	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	/**
	 * @param dryRun the dryRun to set
	 */
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}
	
	/**
	 * @param numThreads the numThreads to set
	 */
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	/**
	 * 
	 */
	public GitHub() {
		super();
	}

	public void deploy(File outputDirectory, String destinationDirectory) throws GitHubException {
		RepositoryId repository = getRepository(repositoryOwner, repositoryName);
		if (dryRun){
			log.info("Dry run mode, repository will not be modified");
		}
		
		String[] paths = getPaths(outputDirectory);
		String prefix = getPrefix(destinationDirectory);
		
		GitHubClient client = createClient(host, userName, password, oauth2Token);
		DataService service = new DataService(client);
		
		boolean createNoJekyll = noJekyll;
		
		if(createNoJekyll){
			for(String path: paths){
				if (NO_JEKYLL_FILE.equals(path)){
					createNoJekyll = false;
					break;
				}
			}
		}

		// Write blobs and build tree entries
		List<TreeEntry> entries = new ArrayList<TreeEntry>(paths.length);
		if(numThreads <= 1){
			createEntries(entries, prefix, paths, service, repository, outputDirectory);
		}else{
			createEntriesInThreads(entries, prefix, paths, service, repository, outputDirectory, numThreads);
		}
		
		if (createNoJekyll) {
			if (log.isDebugEnabled()){
				log.debug("Creating empty '.nojekyll' blob at root of tree");
			}
			TreeEntry entry = createEntry("", NO_JEKYLL_FILE, service, repository, outputDirectory);
			entries.add(entry);
		}

		Reference ref = getReference(service, repository);
		
		if(dryRun){
			log.debug("Dry run mode, skip deploy.");
			return;
		}

		// Write tree
		Tree tree = createTree(service, repository, ref, entries);

		// Build commit
		Commit commit = new Commit();
		commit.setMessage(message != null ? message : "GitHubWagon: Deploying OpooPress to GitHub Pages.");
		commit.setTree(tree);

		// Set parent commit SHA-1 if reference exists
		if (ref != null){
			commit.setParents(Collections.singletonList(new Commit().setSha(ref.getObject().getSha())));
		}

		Commit created;
		try {
			created = service.createCommit(repository, commit);
			log.info(MessageFormat.format("Creating commit with SHA-1: {0}", created.getSha()));
		} catch (IOException e) {
			throw new GitHubException("Error creating commit: " + e.getMessage(), e);
		}

		TypedResource object = new TypedResource();
		object.setType(TYPE_COMMIT).setSha(created.getSha());
		if (ref != null) {
			// Update existing reference
			ref.setObject(object);
			try {
				log.info(String.format("Updating reference %s from %s to %s", branch, commit.getParents().get(0).getSha(), created.getSha()));
				service.editReference(repository, ref, force);
			} catch (IOException e) {
				throw new GitHubException("Error editing reference: " + e.getMessage(), e);
			}
		} else {
			// Create new reference
			ref = new Reference().setObject(object).setRef(branch);
			try {
				log.info(MessageFormat.format("Creating reference {0} starting at commit {1}", branch, created.getSha()));
				service.createReference(repository, ref);
			} catch (IOException e) {
				throw new GitHubException("Error creating reference: " + e.getMessage(), e);
			}
		}
	}
	
	private List<TreeEntry> createEntries(List<TreeEntry> entries, final String prefix, final String[] paths, 
			final DataService service, final RepositoryId repository, final File outputDirectory) throws GitHubException{
		for (String path : paths) {
			TreeEntry entry = createEntry(prefix, path, service, repository, outputDirectory);
			entries.add(entry);
		}
		return entries;
	}

	private List<TreeEntry> createEntriesInThreads(List<TreeEntry> entries, final String prefix, final String[] paths, 
			final DataService service, final RepositoryId repository, final File outputDirectory, int numThreads) throws GitHubException{
		ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);//.newCachedThreadPool();  
        CompletionService<TreeEntry> cs = new ExecutorCompletionService<TreeEntry>(threadPool);

        for (final String path : paths) {
			cs.submit(new Callable<TreeEntry>() {
				@Override
				public TreeEntry call() throws Exception {
					return createEntry(prefix, path, service, repository, outputDirectory);
				}
			});
		}
		
        try {
        	//BUG: wait for ever??
//			Future<TreeEntry> future = cs.take();
//			while(future != null){
//				entries.add(future.get());
//				future = cs.take();
//			}
			
        	for(int i = 0 ; i < paths.length ; i++){
        		entries.add(cs.take().get());
        	}
			log.info("All entries created: " + paths.length);
		} catch (InterruptedException e) {
			throw new GitHubException("", e);
		} catch (ExecutionException e) {
			throw new GitHubException("", e);
		}
		return entries;
	}

	private Tree createTree(DataService service, RepositoryId repository, Reference ref, List<TreeEntry> entries) throws GitHubException {
		try {
			int size = entries.size();
			log.info(String.format("Creating tree with %s blob entries", size));

			String baseTree = null;
			if (merge && ref != null) {
				Tree currentTree = service.getCommit(repository, ref.getObject().getSha()).getTree();
				if (currentTree != null){
					baseTree = currentTree.getSha();
				}
				log.info(MessageFormat.format("Merging with tree {0}", baseTree));
			}
			
			return service.createTree(repository, entries, baseTree);
		} catch (IOException e) {
			throw new GitHubException("Error creating tree: " + e.getMessage(), e);
		}
	}

	private Reference getReference(DataService service, RepositoryId repository) throws GitHubException {
		Reference ref = null;
		try {
			ref = service.getReference(repository, branch);
		} catch (RequestException e) {
			if (404 != e.getStatus()){			
				throw new GitHubException("Error getting reference: " + e.getMessage(), e);
			}
		} catch (IOException e) {
			throw new GitHubException("Error getting reference: " + e.getMessage(), e);
		}

		if (ref != null && !TYPE_COMMIT.equals(ref.getObject().getType())){
			throw new GitHubException(MessageFormat.format("Existing ref {0} points to a {1} ({2}) instead of a commmit",
					ref.getRef(), ref.getObject().getType(), ref.getObject().getSha()));
		}
		return ref;
	}

	private TreeEntry createEntry(String prefix, String path, DataService service, RepositoryId repository, File outputDirectory) throws GitHubException {
		TreeEntry entry = new TreeEntry();
		entry.setPath(prefix + path);
		entry.setType(TYPE_BLOB);
		entry.setMode(MODE_BLOB);
		if(!dryRun){
			entry.setSha(createBlob(service, repository, outputDirectory, path));
			log.info("" + path + " -> " + entry.getSha());
		}
		return entry;
	}

	/**
	 * @param destinationDirectory
	 * @return
	 */
	private String getPrefix(String destinationDirectory) {
		String prefix = destinationDirectory;
		//String prefix = site.getRoot();
		if (prefix == null){
			prefix = "";
		}
		if("./".equals(prefix)){
			prefix = "";
		}
		if (prefix.length() > 0 && !prefix.endsWith("/")){
			prefix += "/";
		}
		return prefix;
	}

	private String createBlob(DataService service, RepositoryId repository, File outputDirectory, String path) throws GitHubException {
		try {
			Blob blob = new Blob().setEncoding(ENCODING_BASE64);
			if(NO_JEKYLL_FILE.equals(path)){
				blob.setContent("");
				//log.debug("Creating blob from " + NO_JEKYLL_FILE);
			}else{
				File file = new File(outputDirectory, path);
				byte[] bytes = FileUtils.readFileToByteArray(file);
				String encoded = EncodingUtils.toBase64(bytes);
				blob.setContent(encoded);
				//log.debug("Creating blob from " +  file.getAbsolutePath());
			}
			if(log.isDebugEnabled()){
				log.debug("Creating blob from " +  path);
			}
			return service.createBlob(repository, blob);
		} catch (IOException e) {
			throw new GitHubException("Error creating blob from '" + path + "': " + e.getMessage(), e);
		}
	}

	private GitHubClient createClient(String host, String userName, String password, String oauth2Token) throws GitHubException {
		GitHubClient client;
		if (!StringUtils.isEmpty(host)) {
			if (log.isDebugEnabled()){
				log.debug("Using custom host: " + host);
			}
			client = createClient(host);
		} else{
			client = new GitHubClient();
		}
		
		if(!StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)){
			if (log.isDebugEnabled()){
				log.debug("Using basic authentication with username: " + userName);
			}
			client.setCredentials(userName, password);
			return client;
		}else if(!StringUtils.isEmpty(oauth2Token)){
			if (log.isDebugEnabled()){
				log.debug("Using OAuth2 access token authentication");
			}
			client.setOAuth2Token(oauth2Token);
			return client;
		}else if(StringUtils.isEmpty(userName) && !StringUtils.isEmpty(password)){
			if (log.isDebugEnabled()){
				log.debug("Using OAuth2 access token authentication");
			}
			client.setOAuth2Token(password);
			return client;
		}else if(!StringUtils.isEmpty(userName) && System.console() != null){
			Console console = System.console();
			while(StringUtils.isEmpty(password)){
				password = new String(console.readPassword("Input the password for '" + userName + "': "));
			}
			client.setCredentials(userName, password);
			return client;
		}

		throw new GitHubException("No authentication credentials configured");
	}

	/**
	 * Create client
	 * <p>
	 * Subclasses can override to do any custom client configuration
	 *
	 * @param hostname
	 * @return non-null client
	 * @throws MojoExecutionException
	 */
	private GitHubClient createClient(String hostname)	throws GitHubException {
		if (!hostname.contains("://"))
			return new GitHubClient(hostname);
		try {
			URL hostUrl = new URL(hostname);
			return new GitHubClient(hostUrl.getHost(), hostUrl.getPort(), hostUrl.getProtocol());
		} catch (MalformedURLException e) {
			throw new GitHubException("Could not parse host URL " + hostname, e);
		}
	}

	/**
	 * Get repository and throw a {@link MojoExecutionException} on failures
	 *
	 * @param project
	 * @param owner
	 * @param name
	 * @return non-null repository id
	 * @throws MojoExecutionException
	 */
	private RepositoryId getRepository(final String owner, final String name) throws GitHubException {
		RepositoryId repository = null;
		if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(owner)){
			repository = RepositoryId.create(owner, name);
		}else{
			throw new GitHubException("No GitHub repository (owner and name) configured");
		}
		if (log.isDebugEnabled()){
			log.debug(MessageFormat.format("Using GitHub repository {0}", repository.generateId()));
		}
		return repository;
	}
	
	private String[] getPaths(File outputDirectory){
		// Find files to include
		String baseDir = outputDirectory.getAbsolutePath();
		String[] includePaths = removeEmpties(includes);
		String[] excludePaths = removeEmpties(excludes);
		if (log.isDebugEnabled()){
			log.debug(MessageFormat.format("Scanning {0} and including {1} and exluding {2}", baseDir,
					Arrays.toString(includePaths), Arrays.toString(excludePaths)));
		}
		
		String[] paths = getMatchingPaths(includePaths, excludePaths,	baseDir);
		
		// Convert separator to forward slash '/'
		if ('\\' == File.separatorChar){
			for (int i = 0; i < paths.length; i++){
				paths[i] = paths[i].replace('\\', '/');
				//FilenameUtils.separatorsToUnix(paths[i]);
			}
		}

		if (paths.length != 1){
			log.info(MessageFormat.format("Creating {0} blobs", paths.length));
		}else{
			log.info("Creating 1 blob");
		}
		
		if (log.isDebugEnabled()){
			log.debug(MessageFormat.format("Scanned files to include: {0}", Arrays.toString(paths)));
		}
		
		return paths;
	}

	/**
	 * Create an array with only the non-null and non-empty values
	 *
	 * @param values
	 * @return non-null but possibly empty array of non-null/non-empty strings
	 */
	public static String[] removeEmpties(final String... values) {
		if (values == null || values.length == 0){
			return new String[0];
		}
		List<String> validValues = new ArrayList<String>();
		for (String value : values){
			if (value != null && value.length() > 0){
				validValues.add(value);
			}
		}
		return validValues.toArray(new String[validValues.size()]);
	}
	

	/**
	 * Get matching paths found in given base directory
	 *
	 * @param includes
	 * @param excludes
	 * @param baseDir
	 * @return non-null but possibly empty array of string paths relative to the
	 *         base directory
	 */
	public static String[] getMatchingPaths(final String[] includes,
			final String[] excludes, final String baseDir) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(baseDir);
		if (includes != null && includes.length > 0){
			scanner.setIncludes(includes);
		}
		if (excludes != null && excludes.length > 0){
			scanner.setExcludes(excludes);
		}
		scanner.scan();
		return scanner.getIncludedFiles();
	}
}
