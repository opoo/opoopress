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

import org.apache.commons.io.IOUtils;
import org.opoo.press.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Alex Lin
 *
 */
public class StaleUtils {
	private static final Logger log = LoggerFactory.getLogger(StaleUtils.class);
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	static File getLastBuildInfoFile(Site site){
		return new File(site.getWorking(), ".lastBuildInfo");
	}

	public static void saveLastBuildInfo(Site site){
		File file = getLastBuildInfoFile(site);

		File[] configFiles = site.getConfig().getConfigFiles();
		BuildInfo info = new BuildInfo();
		info.time = System.currentTimeMillis();
		info.siteConfigFilesLength = configFiles.length;

		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(info);
			oos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			IOUtils.closeQuietly(oos);
		}
	}

	private static BuildInfo getLastBuildInfo(Site site){
		File file = getLastBuildInfoFile(site);
		if(!file.exists()){
			return null;
		}

		ObjectInputStream ois = null;
		try{
			ois = new ObjectInputStream(new FileInputStream(file));
			return (BuildInfo) ois.readObject();
		}catch (IOException e){
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(ois);
		}
	}

	public static boolean isStale(Site site){
		return isStale(site, false);
	}

	public static boolean isStale(Site site, boolean checkAssets){
		BuildInfo info = getLastBuildInfo(site);
		if(info == null || info.time <= 0) {
			log.debug("No last build info.");
			return true;
		}

		if(site.showDrafts() != info.showDrafts){
			log.info("Show drafts option changed: {} -> {}", info.showDrafts, site.showDrafts());
			return true;
		}

		long lastBuildTime = info.time;

		//theme config
		if(site.getTheme().getConfigFile().lastModified() > lastBuildTime){
			log.info("Theme configuration file changed: {}", site.getTheme().getConfigFile());
			return true;
		}

		//config
		File[] configFiles = site.getConfig().getConfigFiles();
		if(info.siteConfigFilesLength != configFiles.length){
			log.info("Site configuration files changed.");
			return true;
		}
		for(File file: configFiles){
			if(file.lastModified() > lastBuildTime){
				log.info("Site configuration file changed: {}", file);
				return true;
			}
		}

		FileFilter filter = new ValidFileFilter();

		//source file
		List<File> sources = site.getSources();
		for(File source: sources){
			if(isNewer(source, lastBuildTime, filter)){
				log.info("Source file changed.");
				return true;
			}
		}

		//templates
		File templates = site.getTemplates();
		if(isNewer(templates, lastBuildTime, filter)){
			log.info("Template file changed.");
			return true;
		}

		if(checkAssets){
			//assets
			List<File> assets = site.getAssets();
			if(assets != null && !assets.isEmpty()){
				for(File asset: assets){
					if(isNewer(asset, lastBuildTime, filter)){
						log.info("Asset file changed.");
						return true;
					}
				}
			}
		}

		return false;
	}

	public static List<File> getStaleAssets(Site site){
		BuildInfo info = getLastBuildInfo(site);
		long lastBuildTime = info.time;
		FileFilter filter = new ValidFileFilter();

		List<File> list = new ArrayList<File>();
		List<File> assets = site.getAssets();
		if(assets != null && !assets.isEmpty()){
			for(File asset: assets){
				if(isNewer(asset, lastBuildTime, filter)){
					list.add(asset);
				}
			}
		}
		if(list.isEmpty()){
			return null;
		}
		return list;
	}
	
    public static boolean isNewer(File dir, long compareTime, FileFilter filter){
    	File[] listFiles = dir.listFiles(filter);
    	for(File file: listFiles){
    		if(file.isHidden()){
    			log.debug("Skip check hidden file: " + file);
    			continue;
    		}
    		if(file.isFile()){
    			if(file.lastModified() > compareTime){
					log.info("File {} changed.", file);
    				return true;
    			}
    		}else if(file.isDirectory()){
    			if(isNewer(file, compareTime, filter)){
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    public static String format(long millis){
    	return SDF.format(new Date(millis));
    }

	private static class ValidFileFilter implements FileFilter{
		@Override
		public boolean accept(File file) {
			String name = file.getName();
			char firstChar = name.charAt(0);
			if(firstChar == '.' || firstChar == '#'){
				return false;
			}
			char lastChar = name.charAt(name.length() - 1);
			if(lastChar == '~'){
				return false;
			}
			return true;
		}
	}

	public static class BuildInfo implements Externalizable{
		private long time;
		private boolean showDrafts;
		private int siteConfigFilesLength;
//		private long siteConfigFilesLastModified;
//		private long themeConfigFileLastModified;
//		private int sourcesLength;
//		private long sourcesLastModified;
//		private int templatesLength;
//		private long templatesLastModified;
//		private int assetsLength;
//		private long assetsLastModified;


		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeLong(time);
			out.writeBoolean(showDrafts);
			out.writeInt(siteConfigFilesLength);
//			out.writeLong(siteConfigFilesLastModified);
//			out.writeLong(themeConfigFileLastModified);
//			out.writeInt(sourcesLength);
//			out.writeLong(sourcesLastModified);
//			out.writeInt(templatesLength);
//			out.writeLong(templatesLastModified);
//			out.writeInt(assetsLength);
//			out.writeLong(assetsLastModified);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			time = in.readLong();
			showDrafts = in.readBoolean();
			siteConfigFilesLength = in.readInt();
//			siteConfigFilesLastModified = in.readLong();
//			themeConfigFileLastModified = in.readLong();
//			sourcesLength = in.readInt();
//			sourcesLastModified = in.readLong();
//			templatesLength = in.readInt();
//			templatesLastModified = in.readLong();
//			assetsLength = in.readInt();
//			assetsLastModified = in.readLong();
		}
	}
}
