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

import static java.lang.Integer.MAX_VALUE;
import static org.eclipse.egit.github.core.Blob.ENCODING_BASE64;
import static org.eclipse.egit.github.core.TreeEntry.MODE_BLOB;
import static org.eclipse.egit.github.core.TreeEntry.TYPE_BLOB;
import static org.eclipse.egit.github.core.TypedResource.TYPE_COMMIT;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
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

import com.github.maven.plugins.core.PathUtils;
import com.github.maven.plugins.core.RepositoryUtils;
import com.github.maven.plugins.core.StringUtils;


/**
 * @author Alex Lin
 * @goal deploy-to-github
 */
public class DeployToGitHubMojo extends AbstractDeployMojo{
	/**
	 * BRANCH_DEFAULT
	 */
	public static final String BRANCH_DEFAULT = "refs/heads/gh-pages";

	/**
	 * NO_JEKYLL_FILE
	 */
	public static final String NO_JEKYLL_FILE = ".nojekyll";

	/**
	 * Branch to update
	 *
	 * @parameter expression="${github.site.branch}" default-value="refs/heads/gh-pages"
	 */
	private String branch = BRANCH_DEFAULT;

	/**
	 * Commit message
	 *
	 * @parameter expression="${github.site.commit.message}"
	 * @required
	 */
	private String message;

	/**
	 * Name of repository
	 *
	 * @parameter expression="${github.site.repositoryName}"
	 */
	private String repositoryName;

	/**
	 * Owner of repository
	 *
	 * @parameter expression="${github.site.repositoryOwner}"
	 */
	private String repositoryOwner;

	/**
	 * User name for authentication
	 *
	 * @parameter expression="${github.site.userName}"
	 *            default-value="${github.global.userName}"
	 */
	private String userName;

	/**
	 * User name for authentication
	 *
	 * @parameter expression="${github.site.password}"
	 *            default-value="${github.global.password}"
	 */
	private String password;

	/**
	 * User name for authentication
	 *
	 * @parameter expression="${github.site.oauth2Token}"
	 *            default-value="${github.global.oauth2Token}"
	 */
	private String oauth2Token;

	/**
	 * Host for API calls
	 *
	 * @parameter expression="${github.site.host}"
	 *            default-value="${github.global.host}"
	 */
	private String host;

	/**
	 * Id of server to use
	 *
	 * @parameter expression="${github.site.server}"
	 *            default-value="${github.global.server}"
	 */
	private String server;

	/**
	 * Paths and patterns to include
	 *
	 * @parameter
	 */
	private String[] includes;

	/**
	 * Paths and patterns to exclude
	 *
	 * @parameter
	 */
	private String[] excludes;

	/**
	 * Project being built
	 *
	 * @parameter expression="${project}
	 * @required
	 */
	private MavenProject project;

	/**
	 * Session
	 *
	 * @parameter expression="${session}
	 */
	private MavenSession session;

	/**
	 * Settings
	 *
	 * @parameter expression="${settings}
	 */
	private Settings settings;

	/**
	 * Force reference update
	 *
	 * @parameter expression="${github.site.force}"
	 */
	private boolean force;

	/**
	 * True to always create a '.nojekyll' file at the root of the site if one
	 * doesn't already exist.
	 *
	 * @parameter expression="${github.site.noJekyll}"
	 */
	private boolean noJekyll;

	/**
	 * Merge with existing the existing tree that is referenced by the commit
	 * that the ref currently points to
	 *
	 * @parameter expression="${github.site.merge}"
	 */
	private boolean merge;

	/**
	 * Show what blob, trees, commits, and references would be created/updated
	 * but don't actually perform any operations on the target GitHub
	 * repository.
	 *
	 * @parameter expression="${github.site.dryRun}"
	 */
	private boolean dryRun;

	/**
	 * Create blob
	 *
	 * @param service
	 * @param repository
	 * @param path
	 * @return blob SHA-1
	 * @throws MojoExecutionException
	 */
	protected String createBlob(DataService service, RepositoryId repository, File outputDirectory,
			String path) throws MojoExecutionException {
		File file = new File(outputDirectory, path);
		final long length = file.length();
		final int size = length > MAX_VALUE ? MAX_VALUE : (int) length;
		ByteArrayOutputStream output = new ByteArrayOutputStream(size);
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			final byte[] buffer = new byte[8192];
			int read;
			while ((read = stream.read(buffer)) != -1)
				output.write(buffer, 0, read);
		} catch (IOException e) {
			throw new MojoExecutionException("Error reading file: "
					+ getExceptionMessage(e), e);
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					debug("Exception closing stream", e);
				}
		}

		Blob blob = new Blob().setEncoding(ENCODING_BASE64);
		String encoded = EncodingUtils.toBase64(output.toByteArray());
		blob.setContent(encoded);

		try {
			if (isDebug())
				debug(MessageFormat.format("Creating blob from {0}",
						file.getAbsolutePath()));
			if (!dryRun)
				return service.createBlob(repository, blob);
			else
				return null;
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating blob: "
					+ getExceptionMessage(e), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.maven.plugins.plugin.AbstractDeployMojo#deployTo(java.io.File)
	 */
	@Override
	protected void deployTo(File dest) throws MojoExecutionException,
			MojoFailureException {
		
		RepositoryId repository = getRepository(project, repositoryOwner, repositoryName);

		if (dryRun)
			info("Dry run mode, repository will not be modified");

		File outputDirectory = dest;
		
		// Find files to include
		String baseDir = outputDirectory.getAbsolutePath();
		String[] includePaths = StringUtils.removeEmpties(includes);
		String[] excludePaths = StringUtils.removeEmpties(excludes);
		if (isDebug())
			debug(MessageFormat.format(
					"Scanning {0} and including {1} and exluding {2}", baseDir,
					Arrays.toString(includePaths),
					Arrays.toString(excludePaths)));
		String[] paths = PathUtils.getMatchingPaths(includePaths, excludePaths,
				baseDir);

		if (paths.length != 1)
			info(MessageFormat.format("Creating {0} blobs", paths.length));
		else
			info("Creating 1 blob");
		if (isDebug())
			debug(MessageFormat.format("Scanned files to include: {0}",
					Arrays.toString(paths)));

		DataService service = new DataService(createClient(host, userName,
				password, oauth2Token, server, settings, session));

		// Write blobs and build tree entries
		List<TreeEntry> entries = new ArrayList<TreeEntry>(paths.length);
		//String prefix = path;
		String prefix = site.getRoot();
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

			if (isDebug())
				debug("Creating empty .nojekyll blob at root of tree");
			if (!dryRun)
				try {
					entry.setSha(service.createBlob(repository, new Blob()
							.setEncoding(ENCODING_BASE64).setContent("")));
				} catch (IOException e) {
					throw new MojoExecutionException(
							"Error creating .nojekyll empty blob: "
									+ getExceptionMessage(e), e);
				}
			entries.add(entry);
		}

		Reference ref = null;
		try {
			ref = service.getReference(repository, branch);
		} catch (RequestException e) {
			if (404 != e.getStatus())
				throw new MojoExecutionException("Error getting reference: "
						+ getExceptionMessage(e), e);
		} catch (IOException e) {
			throw new MojoExecutionException("Error getting reference: "
					+ getExceptionMessage(e), e);
		}

		if (ref != null && !TYPE_COMMIT.equals(ref.getObject().getType()))
			throw new MojoExecutionException(
					MessageFormat
							.format("Existing ref {0} points to a {1} ({2}) instead of a commmit",
									ref.getRef(), ref.getObject().getType(),
									ref.getObject().getSha()));

		// Write tree
		Tree tree;
		try {
			int size = entries.size();
			if (size != 1)
				info(MessageFormat.format(
						"Creating tree with {0} blob entries", size));
			else
				info("Creating tree with 1 blob entry");
			String baseTree = null;
			if (merge && ref != null) {
				Tree currentTree = service.getCommit(repository,
						ref.getObject().getSha()).getTree();
				if (currentTree != null)
					baseTree = currentTree.getSha();
				info(MessageFormat.format("Merging with tree {0}", baseTree));
			}
			if (!dryRun)
				tree = service.createTree(repository, entries, baseTree);
			else
				tree = new Tree();
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating tree: "
					+ getExceptionMessage(e), e);
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
			info(MessageFormat.format("Creating commit with SHA-1: {0}",
					created.getSha()));
		} catch (IOException e) {
			throw new MojoExecutionException("Error creating commit: "
					+ getExceptionMessage(e), e);
		}

		TypedResource object = new TypedResource();
		object.setType(TYPE_COMMIT).setSha(created.getSha());
		if (ref != null) {
			// Update existing reference
			ref.setObject(object);
			try {
				info(MessageFormat.format(
						"Updating reference {0} from {1} to {2}", branch,
						commit.getParents().get(0).getSha(), created.getSha()));
				if (!dryRun)
					service.editReference(repository, ref, force);
			} catch (IOException e) {
				throw new MojoExecutionException("Error editing reference: "
						+ getExceptionMessage(e), e);
			}
		} else {
			// Create new reference
			ref = new Reference().setObject(object).setRef(branch);
			try {
				info(MessageFormat.format(
						"Creating reference {0} starting at commit {1}",
						branch, created.getSha()));
				if (!dryRun)
					service.createReference(repository, ref);
			} catch (IOException e) {
				throw new MojoExecutionException("Error creating reference: "
						+ getExceptionMessage(e), e);
			}
		}
	}

	/**
	 * Get formatted exception message for {@link IOException}
	 *
	 * @param e
	 * @return message
	 */
	public static String getExceptionMessage(IOException e) {
		return e.getMessage();
	}

	/**
	 * Is debug logging enabled?
	 *
	 * @return true if enabled, false otherwise
	 */
	protected boolean isDebug() {
		final Log log = getLog();
		return log != null ? log.isDebugEnabled() : false;
	}

	/**
	 * Is info logging enabled?
	 *
	 * @return true if enabled, false otherwise
	 */
	protected boolean isInfo() {
		final Log log = getLog();
		return log != null ? log.isInfoEnabled() : false;
	}

	/**
	 * Log given message at debug level
	 *
	 * @param message
	 */
	protected void debug(String message) {
		final Log log = getLog();
		if (log != null)
			log.debug(message);
	}

	/**
	 * Log given message and throwable at debug level
	 *
	 * @param message
	 * @param throwable
	 */
	protected void debug(String message, Throwable throwable) {
		final Log log = getLog();
		if (log != null)
			log.debug(message, throwable);
	}

	/**
	 * Log given message at info level
	 *
	 * @param message
	 */
	protected void info(String message) {
		final Log log = getLog();
		if (log != null)
			log.info(message);
	}

	/**
	 * Log given message and throwable at info level
	 *
	 * @param message
	 * @param throwable
	 */
	protected void info(String message, Throwable throwable) {
		final Log log = getLog();
		if (log != null)
			log.info(message, throwable);
	}

	/**
	 * Create client
	 *
	 * @param host
	 * @param userName
	 * @param password
	 * @param oauth2Token
	 * @param serverId
	 * @param settings
	 * @param session
	 * @return client
	 * @throws MojoExecutionException
	 */
	protected GitHubClient createClient(String host, String userName,
			String password, String oauth2Token, String serverId,
			Settings settings, MavenSession session)
			throws MojoExecutionException {
		GitHubClient client;
		if (!StringUtils.isEmpty(host)) {
			if (isDebug())
				debug("Using custom host: " + host);
			client = createClient(host);
		} else
			client = createClient();

		if (configureUsernamePassword(client, userName, password)
				|| configureOAuth2Token(client, oauth2Token)
				|| configureServerCredentials(client, serverId, settings,
						session))
			return client;
		else
			throw new MojoExecutionException(
					"No authentication credentials configured");
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
	protected GitHubClient createClient(String hostname)
			throws MojoExecutionException {
		if (!hostname.contains("://"))
			return new GitHubClient(hostname);
		try {
			URL hostUrl = new URL(hostname);
			return new GitHubClient(hostUrl.getHost(), hostUrl.getPort(),
					hostUrl.getProtocol());
		} catch (MalformedURLException e) {
			throw new MojoExecutionException("Could not parse host URL "
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
	protected GitHubClient createClient() {
		return new GitHubClient();
	}

	/**
	 * Configure credentials from configured username/password combination
	 *
	 * @param client
	 * @param userName
	 * @param password
	 * @return true if configured, false otherwise
	 */
	protected boolean configureUsernamePassword(final GitHubClient client,
			final String userName, String password) {
		if (StringUtils.isEmpty(userName, password))
			return false;

		//
		if(!StringUtils.isEmpty(userName) && StringUtils.isEmpty(password) && System.console() != null){
			password = new String(System.console().readPassword("Input password for " + userName + ": "));
		}
		
		if (isDebug())
			debug("Using basic authentication with username: " + userName);
		client.setCredentials(userName, password);
		return true;
	}

	/**
	 * Configure credentials from configured OAuth2 token
	 *
	 * @param client
	 * @param oauth2Token
	 * @return true if configured, false otherwise
	 */
	protected boolean configureOAuth2Token(final GitHubClient client,
			final String oauth2Token) {
		if (StringUtils.isEmpty(oauth2Token))
			return false;

		if (isDebug())
			debug("Using OAuth2 access token authentication");
		client.setOAuth2Token(oauth2Token);
		return true;
	}

	/**
	 * Configure client with credentials from given server id
	 *
	 * @param client
	 * @param serverId
	 * @param settings
	 * @param session
	 * @return true if configured, false otherwise
	 * @throws MojoExecutionException
	 */
	protected boolean configureServerCredentials(final GitHubClient client,
			final String serverId, final Settings settings,
			final MavenSession session) throws MojoExecutionException {
		if (StringUtils.isEmpty(serverId))
			return false;

		String serverUsername = null;
		String serverPassword = null;

//		if (session != null) {
//			RepositorySystemSession systemSession = session
//					.getRepositorySession();
//			if (systemSession != null) {
//				Authentication authInfo = systemSession
//						.getAuthenticationSelector().getAuthentication(
//								new RemoteRepository().setId(serverId));
//				if (authInfo != null) {
//					serverUsername = authInfo.getUsername();
//					serverPassword = authInfo.getPassword();
//				}
//			}
//		}

		if (StringUtils.isEmpty(serverPassword)) {
			Server server = getServer(settings, serverId);
			if (server == null)
				throw new MojoExecutionException(MessageFormat.format(
						"Server ''{0}'' not found in settings", serverId));

			if (isDebug())
				debug(MessageFormat.format("Using ''{0}'' server credentials", serverId));

			serverUsername = server.getUsername();
			serverPassword = server.getPassword();
		}
		
		if(!StringUtils.isEmpty(serverUsername) && StringUtils.isEmpty(serverPassword) && System.console() != null){
			serverPassword = new String(System.console().readPassword("Input password for " + serverUsername + ": "));
		}

		if (!StringUtils.isEmpty(serverUsername, serverPassword)) {
			if (isDebug())
				debug("Using basic authentication with username: "
						+ serverUsername);
			client.setCredentials(serverUsername, serverPassword);
			return true;
		}

		// A server password without a username is assumed to be an OAuth2 token
		if (!StringUtils.isEmpty(serverPassword)) {
			if (isDebug())
				debug("Using OAuth2 access token authentication");
			client.setOAuth2Token(serverPassword);
			return true;
		}

		if (isDebug())
			debug(MessageFormat.format(
					"Server ''{0}'' is missing username/password credentials",
					serverId));
		return false;
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
	protected RepositoryId getRepository(final MavenProject project,
			final String owner, final String name)
			throws MojoExecutionException {
		RepositoryId repository = RepositoryUtils.getRepository(project, owner,
				name);
		if (repository == null)
			throw new MojoExecutionException(
					"No GitHub repository (owner and name) configured");
		if (isDebug())
			debug(MessageFormat.format("Using GitHub repository {0}",
					repository.generateId()));
		return repository;
	}

	/**
	 * Get server with given id
	 *
	 * @param settings
	 * @param serverId
	 *            must be non-null and non-empty
	 * @return server or null if none matching
	 */
	protected Server getServer(final Settings settings, final String serverId) {
		if (settings == null)
			return null;
		List<Server> servers = settings.getServers();
		if (servers == null || servers.isEmpty())
			return null;

		for (Server server : servers)
			if (serverId.equals(server.getId()))
				return server;
		return null;
	}
}
