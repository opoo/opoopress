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
package org.opoo.press.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Alex Lin
 *
 */
public abstract class ClassPathUtils {
	
	private static final Log log = LogFactory.getLog(ClassPathUtils.class);
	private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled();
	
	/** Pseudo URL prefix for loading from the class path: "classpath:" */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	/** URL prefix for loading from the file system: "file:" */
	public static final String FILE_URL_PREFIX = "file:";
	
	/** URL protocol for a file in the file system: "file" */
	public static final String URL_PROTOCOL_FILE = "file";

	/** URL protocol for an entry from a jar file: "jar" */
	public static final String URL_PROTOCOL_JAR = "jar";

	/** URL protocol for an entry from a zip file: "zip" */
	public static final String URL_PROTOCOL_ZIP = "zip";

	/** URL protocol for an entry from a JBoss jar file: "vfszip" */
	public static final String URL_PROTOCOL_VFSZIP = "vfszip";

	/** URL protocol for a JBoss VFS resource: "vfs" */
	public static final String URL_PROTOCOL_VFS = "vfs";

	/** URL protocol for an entry from a WebSphere jar file: "wsjar" */
	public static final String URL_PROTOCOL_WSJAR = "wsjar";

	/** URL protocol for an entry from an OC4J jar file: "code-source" */
	public static final String URL_PROTOCOL_CODE_SOURCE = "code-source";

	/** Separator between JAR URL and file path within the JAR */
	public static final String JAR_URL_SEPARATOR = "!/";

    private ClassPathUtils()
    {
    }

    /**
     * 
     * @param loader
     * @param classPathSource
     * @param destination
     * @throws Exception
     */
    public static void copyPath(ClassLoader loader, String classPathSource, File destination)  throws Exception{
        copyPath(loader, classPathSource, destination, true);
    }

    /**
     * 
     * @param loader
     * @param classPathSource
     * @param destination
     * @param overwrite
     * @throws Exception
     */
    public static void copyPath(ClassLoader loader, String classPathSource, File destination, boolean overwrite) throws Exception{
    	if(classPathSource == null || destination == null){
    		return;
    	}
    	
    	URL url = loader.getResource(classPathSource);
    	if(IS_DEBUG_ENABLED){
			log.debug("Copy path from '" + url + "' to '" + destination + "'");
		}
    	
    	boolean b = isJarURL(url);
    	if(b){
    		URL jarFileURL = extractJarFileURL(url);
    		copyJarPath(jarFileURL, classPathSource, destination, overwrite);
    	}else{
    		copyFilePath(url, destination, overwrite);
    	}
    }
    
    /**
     * 
     * @param jarFileURL
     * @param sourcePath
     * @param destination
     * @param overwrite
     * @throws Exception
     */
    protected static void copyJarPath(URL jarFileURL, String sourcePath, File destination, boolean overwrite)throws Exception{
    	//URL jarFileURL = ResourceUtils.extractJarFileURL(url);
    	if(!sourcePath.endsWith("/")){
    		sourcePath += "/";
    	}
    	String root = jarFileURL.toString() + "!/";
    	if(!root.startsWith("jar:")){
    		root = "jar:" + root;
    	}
    	
    	JarFile jarFile = new JarFile(new File(jarFileURL.toURI()));
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = entries.nextElement();
			String name = jarEntry.getName();
			//log.debug(name + "." + sourcePath + "." + name.startsWith(sourcePath));
			if(name.startsWith(sourcePath)){
				String relativePath = name.substring(sourcePath.length());
				//log.debug("relativePath: " + relativePath);
				if(relativePath != null && relativePath.length() > 0){
					File tmp = new File(destination, relativePath);
					//not exists or overwrite permitted
					if(overwrite || !tmp.exists()){
						if(jarEntry.isDirectory()){
							tmp.mkdirs();
							if(IS_DEBUG_ENABLED){
								log.debug("创建目录：" + tmp);
							}
						}else{
							File parent = tmp.getParentFile();
							if(!parent.exists()){
								parent.mkdirs();
							}
							//1.FileCopyUtils.copy
							//InputStream is = jarFile.getInputStream(jarEntry);
							//FileCopyUtils.copy(is, new FileOutputStream(tmp));
							
							//2. url copy
							URL u = new URL(root + name);
							//log.debug(u.toString());
							FileUtils.copyURLToFile(u, tmp);
							if(IS_DEBUG_ENABLED){
								log.debug("Copyed file '" + u + "' to '" + tmp + "'.");
							}
						}
					}
				}
			}
		}
		
		try{
			jarFile.close();
		}catch(Exception ie){
		}
    }
    
    /**
     * 
     * @param sourceDirectoryURL
     * @param destination
     * @param overwrite
     * @throws Exception
     */
    protected static void copyFilePath(URL sourceDirectoryURL, File destination, boolean overwrite)
			throws Exception {
		URI classPathNode = sourceDirectoryURL.toURI();
		File f = new File(classPathNode);
		if (!destination.exists()){
			destination.mkdirs();
		}
		
		File nodes[] = f.listFiles();
		if (nodes == null || nodes.length == 0){
			return;
		}
		for (File node : nodes) {
			if (node.isDirectory()) {
				FileUtils.copyDirectory(node, new File(destination, node.getName()));
			} else {
				FileUtils.copyFile(node, new File(destination, node.getName()));
			}
		}
	}
    
	public static boolean isJarURL(URL url) {
		String protocol = url.getProtocol();
		return (URL_PROTOCOL_JAR.equals(protocol) ||
				URL_PROTOCOL_ZIP.equals(protocol) ||
				URL_PROTOCOL_WSJAR.equals(protocol) ||
				(URL_PROTOCOL_CODE_SOURCE.equals(protocol) && url.getPath().contains(JAR_URL_SEPARATOR)));
	}
    
	public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
		String urlFile = jarUrl.getFile();
		int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
		if (separatorIndex != -1) {
			String jarFile = urlFile.substring(0, separatorIndex);
			try {
				return new URL(jarFile);
			}
			catch (MalformedURLException ex) {
				// Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
				// This usually indicates that the jar file resides in the file system.
				if (!jarFile.startsWith("/")) {
					jarFile = "/" + jarFile;
				}
				return new URL(FILE_URL_PREFIX + jarFile);
			}
		}
		else {
			return jarUrl;
		}
	}
}
