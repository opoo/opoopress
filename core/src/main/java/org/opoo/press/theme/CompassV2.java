/*
 * Copyright 2014 Alex Lin.
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
package org.opoo.press.theme;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Lin
 *
 */
class CompassV2 {
	private static final Logger log = LoggerFactory.getLogger(Compass.class);
	private String path;
	private String config;
	
	private String compileScript;
	private String watchScript;
	
	
	static List<String> toCompassConfigFileLines(Map<String, Object> options){
		List<String> lines = new ArrayList<String>();
		for(Map.Entry<String, Object> en: options.entrySet()){
			Object value = en.getValue();
			if(value instanceof String && !((String)value).startsWith(":")){
				value = "\"" + value + "\"";
			}
			String string = String.format("%s = %s", en.getKey(), value);
			lines.add(string);
		}
		return lines;
	}
	
	static String toCompassConfigRubyScript(Map<String, Object> options) {
		StringBuffer sb = new StringBuffer("{");
		for(Map.Entry<String, Object> en: options.entrySet()){
			Object value = en.getValue();
			if(value instanceof String && !((String)value).startsWith(":")){
				value = "'" + value + "'";
			}
			if(sb.length() > 1){
				sb.append(", ");
			}
			sb.append(":").append(en.getKey()).append(" => ").append(value); 
		}
		sb.append("}");
		return sb.toString();
	}

	CompassV2(File path, Map<String,Object> options) {
		this.path = FilenameUtils.separatorsToUnix(path.getAbsolutePath());
		this.config = toCompassConfigRubyScript(options);
	}

	public void compile() {
		if(compileScript == null){
			compileScript = buildCompileScript();
		}
		runScriptlet(compileScript);
	}

	public void watch() {
		if(watchScript == null){
			watchScript = buildWatchScript();
		}
		runScriptlet(watchScript);
	}

	private void runScriptlet(String script) {
		log.debug(script);
		new ScriptingContainer().runScriptlet(script);
	}

	private void buildBasicScript(PrintWriter script) {
		//script.println("require 'rubygems'");
		script.println("require 'compass'");
		script.println("frameworks = Dir.new(Compass::Frameworks::DEFAULT_FRAMEWORKS_PATH).path");
		script.println("Compass::Frameworks.register_directory(File.join(frameworks, 'compass'))");
		script.println("Compass::Frameworks.register_directory(File.join(frameworks, 'blueprint'))");
//		script.println("Compass.add_project_configuration '" + config + "'");
		script.println("Compass.add_configuration(" + config + ", 'custom')");
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
		script.println("command = Compass::Commands::WatchProject.new('" + path	+ "', {})");
		script.println("command.perform");
		script.flush();

		return raw.toString();
	}
}
