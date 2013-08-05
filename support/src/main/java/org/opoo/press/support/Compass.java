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
package org.opoo.press.support;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jruby.embed.ScriptingContainer;

/**
 * @author Alex Lin
 * 
 */
public class Compass {
	private static final Log log = LogFactory.getLog(Compass.class);
	private String path;
	private String config;

	public Compass(File site) {
		File configFile = new File(site, "config.rb");
		if (!site.exists() || !site.isDirectory() || !configFile.exists()) {
			throw new IllegalArgumentException("Not a valid compass project path: "	+ path);
		}
		this.path = FilenameUtils.separatorsToUnix(site.getAbsolutePath());
		this.config = FilenameUtils.separatorsToUnix(configFile.getAbsolutePath());
	}

	public void compile() {
		runScriptlet(buildCompileScript());
	}

	public void watch() {
		runScriptlet(buildWatchScript());
	}

	private void runScriptlet(String script) {
		log.debug(script);
		new ScriptingContainer().runScriptlet(script);
	}

	private void buildBasicScript(PrintWriter script) {
		script.println("require 'rubygems'");
		script.println("require 'compass'");
		script.println("frameworks = Dir.new(Compass::Frameworks::DEFAULT_FRAMEWORKS_PATH).path");
		script.println("Compass::Frameworks.register_directory(File.join(frameworks, 'compass'))");
		script.println("Compass::Frameworks.register_directory(File.join(frameworks, 'blueprint'))");
		script.println("Compass.add_project_configuration '" + config + "'");
		script.println("Compass.configure_sass_plugin!");
	}

	private String buildCompileScript() {
		StringWriter raw = new StringWriter();
		PrintWriter script = new PrintWriter(raw);

		buildBasicScript(script);

		script.println("Dir.chdir('" + path + "') do ");
		// script.println("Dir.chdir(File.dirname('" + config + "')) do ");
		script.println("  Compass.compiler.run");
		script.println("end");
		script.flush();

		return raw.toString();
	}

	private String buildWatchScript() {
		StringWriter raw = new StringWriter();
		PrintWriter script = new PrintWriter(raw);

		buildBasicScript(script);
		script.println("require 'compass/commands'");
		script.println("command = Compass::Commands::WatchProject.new('" + path
				+ "', {})");
		script.println("command.perform");
		script.flush();

		return raw.toString();
	}
}
