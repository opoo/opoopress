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
package org.opoo.press.resource;

import com.yahoo.platform.yui.compressor.YUICompressor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opoo.press.Observer;
import org.opoo.press.ResourceBuilder;
import org.opoo.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public class YUIBuilder implements ResourceBuilder, Observer{
	private static final Logger log = LoggerFactory.getLogger(YUIBuilder.class);

	public String charset = "UTF-8";
    public int lineBreak = -1;
    public boolean verbose = false;
    public boolean nomunge = false;
    public boolean preserveSemi = false;
    public boolean disableOptimizations = false;
    public boolean mergeOnly = false;

    private String type;
    private List<File> inputFiles;
    private File outputFile;

//	@Override
//    public void init(Site site, Theme theme, Map<String, Object> config) {
//    	throw new UnsupportedOperationException("this method must override in derived class.");
//    }

	@Override
	public void init(File resourceBaseDirectory, Map<String, Object> config) {
		throw new UnsupportedOperationException("init() method must override in derived class.");
	}

	protected YUIBuilder init(String type, Map<String,Object> config){
    	return init(type, null, config);
    }
    
	protected YUIBuilder init(String type, File base, Map<String,Object> config){
    	this.type = type;
    	this.inputFiles = parseInputFiles(base, config);
    	this.outputFile = parseOutputFile(base, config);
    	
    	lineBreak = (Integer) MapUtils.get(config, "line-break", lineBreak);
    	charset = (String) MapUtils.get(config, "charset", charset);
    	verbose = MapUtils.get(config, "verbose", verbose);
    	mergeOnly = MapUtils.get(config, "merge-only", mergeOnly);
    	
    	//for js only
    	if(type.equalsIgnoreCase("js")) {
    		nomunge = MapUtils.get(config, "nomunge", nomunge);
    		preserveSemi = MapUtils.get(config, "preserve-semi", preserveSemi);
    		disableOptimizations = MapUtils.get(config, "disable-optimizations", disableOptimizations);
    	}
    	
    	return this;
    }
    
    @SuppressWarnings("unchecked")
	private static List<File> parseInputFiles(File base, Map<String,Object> config){
    	Object input = config.get("input");
    	List<String> inputPaths;
    	if(input instanceof String){
    		inputPaths = Arrays.asList((String) input);
    	}else{
    		inputPaths = (List<String>) input;
    	}
    	
    	if(inputPaths == null || inputPaths.isEmpty()){
    		throw new IllegalArgumentException("input is required.");
    	}
    	
    	List<File> inputFiles = new ArrayList<File>();
    	for(String inputPath: inputPaths){
    		File inputFile = (base == null) ? new File(inputPath) : new File(base, inputPath);
    		inputFiles.add(inputFile);
    	}
    	return inputFiles;
    }
    
    private static File parseOutputFile(File base, Map<String,Object> config){
    	String output = (String) config.get("output");
    	return (base == null) ? new File(output) : new File(base, output);
    }
    
    protected void compress(File inputFile, File outputFile) throws Exception{
    	compress(inputFile.toString(), outputFile.toString());
    }
    
	protected void compress(String input, String output) throws Exception{
		log.info("Compressing file '{}' to '{}'", input, output);
		Args args = createCompressArgs().add(input).add("-o").add(output);

		log.debug("args: {}", args);
		YUICompressor.main(args.toArray());
		//Bootstrap.main(args.toArray());
	}
	
	private Args createCompressArgs(){
    	Args args = new Args();
    	args.add("--type");
    	args.add(type);
    	args.add("--line-break");
    	args.add(String.valueOf(lineBreak));
    	args.add("--charset");
    	args.add(charset);
    	
    	if(verbose){
    		args.add("--verbose");
    	}
    	if(type.equalsIgnoreCase("js")) {
    		if(nomunge){
    			args.add("--nomunge");
	    	}
	    	if(preserveSemi){
	    		args.add("--preserve-semi");
	    	}
	    	if(disableOptimizations){
	    		args.add("--disable-optimizations");
	    	}
    	}
    	
    	return args;
    }
	
	protected boolean shouldBuild(){
		//if output file not exists
		if(!outputFile.exists()){
			return true;
		}
		
		//if any input file is newer than output file
		long lastModified = outputFile.lastModified();
		for(File inputFile: inputFiles){
			if(inputFile.lastModified() > lastModified){
				return true;
			}
		}

		return false;
	}
	
	protected void buildInternal() throws Exception{
		if(outputFile.exists()){
    		FileUtils.deleteQuietly(outputFile);
    	}else{
			outputFile.getParentFile().mkdirs();
		}
		
		if(inputFiles.size() == 1){
			if(mergeOnly){
				//copy input file to output file only
				FileUtils.copyFile(inputFiles.get(0), outputFile);
			}else{
//				compress(inputFiles.get(0), outputFile);
				compressIfRequired(inputFiles.get(0), outputFile);
			}
			return;
		}
		
		if(mergeOnly){
			mergeFiles(inputFiles, outputFile, false);
		}else{
			//multiple files， compress and merge
	    	List<File> tempOutputFiles = new ArrayList<File>();
	    	for(File inputFile: inputFiles){
	    		File tempOutputFile = File.createTempFile("op-YUIBuilder-", "." + type);
	    		tempOutputFiles.add(tempOutputFile);

				compressIfRequired(inputFile, tempOutputFile);
	    	}
	    	
//	    	for(File tempOutputFile: tempOutputFiles){
//	    		List<String> lines = FileUtils.readLines(tempOutputFile, charset);
//	    		FileUtils.deleteQuietly(tempOutputFile);
//	    		FileUtils.writeLines(outputFile, charset, lines, true);
//	    		log.debug("Merge file '{}' to '{}'", tempOutputFile, outputFile);
//	    	}
	    	
	    	mergeFiles(tempOutputFiles, outputFile, true);
		}
	}

	private void compressIfRequired(File inputFile, File outputFile) throws Exception{
		//for min.css or min.js, copy only
		if(inputFile.getName().endsWith(".min." + type)){
			FileUtils.copyFile(inputFile, outputFile);
		}else {
			try {
				compress(inputFile, outputFile);
			} catch (Exception e) {
				FileUtils.deleteQuietly(outputFile);
				throw e;
			}
		}
	}
	
	private static void mergeFiles(List<File> inputFiles, File outputFile, boolean deleleInputFiles) throws IOException{
		FileOutputStream out = new FileOutputStream(outputFile, true);
		try{
			for(File inputFile: inputFiles){
				FileUtils.copyFile(inputFile, out);
	    		log.debug("Merge file '{}' to '{}'", inputFile, outputFile);
	    		if(deleleInputFiles){
	    			FileUtils.deleteQuietly(inputFile);
	    			log.debug("Delete file '{}'", inputFile);
	    		}
	    	}
		}finally{
			IOUtils.closeQuietly(out);
		}
	}

	@Override
	public void build() throws Exception{
		if(shouldBuild()){
			buildInternal();
		}else{
			log.trace("Nothing to build - the output file '{}' is up to date.", outputFile);
		}
	}

	@Override
	public void clean() throws Exception {
		if(outputFile != null && outputFile.exists()){
			FileUtils.deleteQuietly(outputFile);
		}
	}

	public static class Args{
		private List<String> list = new ArrayList<String>();
		public Args add(String arg){
			list.add(arg);
			return this;
		}
		
		public Args add(Object o){
			list.add(o.toString());
			return this;
		}
		
		public String[] toArray(){
			return list.toArray(new String[list.size()]);
		}
		
		public String toString(){
			return list.toString();
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Observer#initialize()
	 */
	@Override
	public void initialize() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Observer#check()
	 */
	@Override
	public void check() throws Exception {
		build();
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Observer#destroy()
	 */
	@Override
	public void destroy() throws Exception {
	}

	@Override
	public String toString(){
		if(inputFiles == null || outputFile == null){
			return super.toString();
		}

		return getClass().getSimpleName()
				+ "('"+ inputFiles
				+ "' => '"
				+ outputFile + "')";
	}
}
