/*
 * Copyright 2013 Alex Lin & https://github.com/synergian/.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * 
 * @author Alex Lin
 * @author synergian
 */
public class Git {
	
	private static final Logger log = /*new SimpleLog("Git");*/LoggerFactory.getLogger(Git.class);
	private final File workingDirectory;
	private final String remote;
	private final String branch;
	private String message;
	
	private final StringStreamConsumer stdout = new StringStreamConsumer() {
		public void consumeLine(String line) {
			log.info("[git] " + line);
		}
	};

	private final StringStreamConsumer stderr = new StringStreamConsumer() {
		public void consumeLine(String line) {
			log.info("[git] " + line);
		}
	};

	public Git(File workingDirectory, String remote, String branch) throws GitException {
		this.remote = remote;
		this.branch = branch;
		this.workingDirectory = workingDirectory;
		if (!workingDirectory.exists()){
			throw new GitException("Invalid directory");
		}
	}
	
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}



	private boolean execute(String command, String... args) throws GitException {
		Commandline cl = new Commandline();
		cl.setExecutable("git");
		cl.createArg().setValue(command);
		cl.setWorkingDirectory(workingDirectory.getAbsolutePath());
		
		//args
		for (int i = 0; i < args.length; i++){
			cl.createArg().setValue(args[i]);
		}
		
		if (log.isInfoEnabled()) {
			log.info("[" + cl.getWorkingDirectory().getAbsolutePath() + "] Executing: " + cl);
		}
		
		int exitCode;
		try {
			exitCode = CommandLineUtils.executeCommandLine(cl, stdout, stderr);
		} catch (CommandLineException e) {
			throw new GitException("Error while executing command.", e);
		}

		if(log.isDebugEnabled()){
			log.debug("Run: " + cl + " / $? = " + exitCode);
		}
		
		return exitCode == 0;
	}
	
	private boolean isValidGitRepository() {
		// Where are assuming that this was checked out by this wagon.
		return new File(workingDirectory, ".git").exists();
	}
	
	public void cloneAll() throws GitException{
		if (!execute("clone", remote, workingDirectory.getAbsolutePath())){
			throw new GitException("git clone failed");
		}
		
		if("master".equals(branch)){
			if(!execute("checkout")){
				throw new GitException("git checkout failed");
			}
		}else{
			if(!execute("checkout", branch)){
				throw new GitException("git checkout failed");
			}
		}
		
		if(!execute("ls-files")){
			throw new GitException("git ls-files failed");
		}
	}
	
	public void pullAll() throws GitException {
		// if there a valid ".git" directory?
		if (!isValidGitRepository()) {
			if (!execute("init")){
				throw new GitException("git init failed");
			}

			if (!execute("remote",  "add", "origin", remote)){
				log.warn("git remote add failed, try git remote set-url");
				if(!execute("remote", "set-url", "origin", remote)){
					throw new GitException("git remote failed");
				}
			}

			if (!execute("fetch", "--progress")){
				throw new GitException("git fetch failed");
			}
		}

		// if remote branch doesn't exist, create new "headless".
		if (!execute("show-ref", "refs/remotes/origin/" + branch)) {
			// git symbolic-ref HEAD refs/heads/<branch>
			if (!execute("symbolic-ref", "HEAD", "refs/heads/" + branch )){
				throw new GitException("Unable to create branch");
			}

			// rm .git/index
			File index = new File(workingDirectory, ".git/index");
			if (index.exists()){
				if (!index.delete()){
					throw new GitException("Unable to create branch");
				}
			}

			// git clean -fdx
			if (!execute("clean", "-fdx" )){
				throw new GitException("Unable to create branch");
			}
		} else
		// else if local branch doesn't exist, checkout -b
		if (!execute("show-ref", "refs/heads/" + branch)) {
			// git checkout -b <branch> origin/<branch>
			if (!execute("checkout", "-b", branch, "origin/" + branch )){
				throw new GitException("Unable to checkout branch");
			}
		}
		// else checkout local branch.
		else {
			// git checkout <branch>
			if (!execute("checkout", branch)){
				throw new GitException("Unable to checkout branch");
			}
		}
	}
	
	public void pushAll() throws GitException {
		if (!execute("add", "." )){
			throw new GitException("Unable to add files");
		}

		//String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		//message = "GitWagon: Adding site to branch " + branch + " at " + timestamp;
		if(message == null){
			message = "GitHubWagon: Deploying OpooPress to GitHub Pages.";
		}
		if (!execute("commit", "--allow-empty", "-m", message)){
			throw new GitException("Unable to commit files");
		}

		if (!execute("push", "--progress", "origin", branch)){
			throw new GitException("Unable to push files");
		}
	}
}
