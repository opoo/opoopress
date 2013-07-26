/*
 * Copyright 2013 Alex Lin and GitHub inc.
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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
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
import org.opoo.maven.plugins.logging.LogAware;

import com.github.maven.plugins.core.PathUtils;
import com.github.maven.plugins.core.RepositoryUtils;
import com.github.maven.plugins.core.StringUtils;

/**
 * @author Alex Lin
 * @author Kevin Sawicki (kevin@github.com)
 */
public class GitHub implements LogAware{
	private Log log = new SystemStreamLog();

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

	private boolean noJekyll;

	private boolean merge;

	private boolean dryRun;

	/**
	 * @param branch the branch to set
	 */
	public void setBranch(String branch) {
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
	 * @param log
	 */
	public GitHub(Log log) {
		this.log = log;
	}
	
	/**
	 * 
	 */
	public GitHub() {
		super();
	}

	public void deploy(File outputDirectory, String destinationDirectory) throws GitHubException {
		RepositoryId repository = getRepository(repositoryOwner, repositoryName);
		if (dryRun)
			log.info("Dry run mode, repository will not be modified");

//		File outputDirectory = site.getDestination();
		
		// Find files to include
		String baseDir = outputDirectory.getAbsolutePath();
		String[] includePaths = StringUtils.removeEmpties(includes);
		String[] excludePaths = StringUtils.removeEmpties(excludes);
		if (log.isDebugEnabled())
			log.debug(MessageFormat.format(
					"Scanning {0} and including {1} and exluding {2}", baseDir,
					Arrays.toString(includePaths),
					Arrays.toString(excludePaths)));
		String[] paths = PathUtils.getMatchingPaths(includePaths, excludePaths,
				baseDir);

		if (paths.length != 1)
			log.info(MessageFormat.format("Creating {0} blobs", paths.length));
		else
			log.info("Creating 1 blob");
		if (log.isDebugEnabled())
			log.debug(MessageFormat.format("Scanned files to include: {0}",
					Arrays.toString(paths)));

		GitHubClient client = createClient(host, userName, password, oauth2Token);
		DataService service = new DataService(client);

		// Write blobs and build tree entries
		List<TreeEntry> entries = new ArrayList<TreeEntry>(paths.length);
		String prefix = destinationDirectory;
		//String prefix = site.getRoot();
		if (prefix == null)
			prefix = "";
		if (prefix.length() > 0 && !prefix.endsWith("/"))
			prefix += "/";

		// Convert separator to forward slash '/'
		if ('\\' == File.separatorChar)
			for (int i = 0; i < paths.length; i++)
				paths[i] = paths[i].replace('\\', '/');

		boolean createNoJekyll = noJekyll;

		for (String path : paths) {
			TreeEntry entry = new TreeEntry();
			entry.setPath(prefix + path);
			// Only create a .nojekyll file if it doesn't already exist
			if (createNoJekyll && NO_JEKYLL_FILE.equals(entry.getPath()))
				createNoJekyll = false;
			entry.setType(TYPE_BLOB);
			entry.setMode(MODE_BLOB);
			entry.setSha(createBlob(service, repository, outputDirectory, path));
			entries.add(entry);
		}

		if (createNoJekyll) {
			TreeEntry entry = new TreeEntry();
			entry.setPath(NO_JEKYLL_FILE);
			entry.setType(TYPE_BLOB);
			entry.setMode(MODE_BLOB);

			if (log.isDebugEnabled())
				log.debug("Creating empty .nojekyll blob at root of tree");
			if (!dryRun)
				try {
					entry.setSha(service.createBlob(repository, new Blob()
							.setEncoding(ENCODING_BASE64).setContent("")));
				} catch (IOException e) {
					throw new GitHubException(
							"Error creating .nojekyll empty blob: "
									+ e.getMessage(), e);
				}
			entries.add(entry);
		}

		Reference ref = null;
		try {
			ref = service.getReference(repository, branch);
		} catch (RequestException e) {
			if (404 != e.getStatus())
				throw new GitHubException("Error getting reference: "
						+ e.getMessage(), e);
		} catch (IOException e) {
			throw new GitHubException("Error getting reference: "
					+ e.getMessage(), e);
		}

		if (ref != null && !TYPE_COMMIT.equals(ref.getObject().getType()))
			throw new GitHubException(
					MessageFormat
							.format("Existing ref {0} points to a {1} ({2}) instead of a commmit",
									ref.getRef(), ref.getObject().getType(),
									ref.getObject().getSha()));

		// Write tree
		Tree tree;
		try {
			int size = entries.size();
			if (size != 1)
				log.info(MessageFormat.format(
						"Creating tree with {0} blob entries", size));
			else
				log.info("Creating tree with 1 blob entry");
			String baseTree = null;
			if (merge && ref != null) {
				Tree currentTree = service.getCommit(repository,
						ref.getObject().getSha()).getTree();
				if (currentTree != null)
					baseTree = currentTree.getSha();
				log.info(MessageFormat.format("Merging with tree {0}", baseTree));
			}
			if (!dryRun)
				tree = service.createTree(repository, entries, baseTree);
			else
				tree = new Tree();
		} catch (IOException e) {
			throw new GitHubException("Error creating tree: "
					+ e.getMessage(), e);
		}

		// Build commit
		Commit commit = new Commit();
		commit.setMessage(message);
		commit.setTree(tree);

		// Set parent commit SHA-1 if reference exists
		if (ref != null)
			commit.setParents(Collections.singletonList(new Commit().setSha(ref
					.getObject().getSha())));

		Commit created;
		try {
			if (!dryRun)
				created = service.createCommit(repository, commit);
			else
				created = new Commit();
			log.info(MessageFormat.format("Creating commit with SHA-1: {0}",
					created.getSha()));
		} catch (IOException e) {
			throw new GitHubException("Error creating commit: "
					+ e.getMessage(), e);
		}

		TypedResource object = new TypedResource();
		object.setType(TYPE_COMMIT).setSha(created.getSha());
		if (ref != null) {
			// Update existing reference
			ref.setObject(object);
			try {
				log.info(MessageFormat.format(
						"Updating reference {0} from {1} to {2}", branch,
						commit.getParents().get(0).getSha(), created.getSha()));
				if (!dryRun)
					service.editReference(repository, ref, force);
			} catch (IOException e) {
				throw new GitHubException("Error editing reference: "
						+ e.getMessage(), e);
			}
		} else {
			// Create new reference
			ref = new Reference().setObject(object).setRef(branch);
			try {
				log.info(MessageFormat.format(
						"Creating reference {0} starting at commit {1}",
						branch, created.getSha()));
				if (!dryRun)
					service.createReference(repository, ref);
			} catch (IOException e) {
				throw new GitHubException("Error creating reference: "
						+ e.getMessage(), e);
			}
		}
	}

	
	/**
	 * Create blob
	 *
	 * @param service
	 * @param repository
	 * @param path
	 * @return blob SHA-1
	 * @throws MojoExecutionException
	 */
	private String createBlob(DataService service, RepositoryId repository, File outputDirectory,	String path) throws GitHubException {
		File file = new File(outputDirectory, path);
		
		byte[] bytes = null;
		try {
			bytes = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			throw new GitHubException("Error reading file: " + e.getMessage(), e);
		}

		Blob blob = new Blob().setEncoding(ENCODING_BASE64);
		String encoded = EncodingUtils.toBase64(bytes);
		blob.setContent(encoded);

		if (log.isDebugEnabled()){
			log.debug(MessageFormat.format("Creating blob from {0}", file.getAbsolutePath()));
		}
		
		try {
			if (!dryRun){
				return service.createBlob(repository, blob);
			}
			else{
				return null;
			}
		} catch (IOException e) {
			throw new GitHubException("Error creating blob: " + e.getMessage(), e);
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
			client = createClient();
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
			throw new GitHubException("Could not parse host URL "
					+ hostname, e);
		}
	}

	/**
	 * Create client
	 * <p>
	 * Subclasses can override to do any custom client configuration
	 *
	 * @return non-null client
	 */
	private GitHubClient createClient() {
		return new GitHubClient();
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
		RepositoryId repository = RepositoryUtils.getRepository(null, owner, name);
		if (repository == null)
			throw new GitHubException("No GitHub repository (owner and name) configured");
		if (log.isDebugEnabled())
			log.debug(MessageFormat.format("Using GitHub repository {0}", repository.generateId()));
		return repository;
	}

	@Override
	public void setLog(Log log) {
		this.log = log;
	}
}
