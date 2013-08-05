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
package org.opoo.press.source.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.opoo.press.source.Source;
import org.opoo.press.source.SourceEntry;
import org.opoo.press.source.SourceManager;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Alex Lin
 *
 */
public class SourceManagerImpl implements SourceManager {
	private Yaml yaml;// = new Yaml();

	public void setYaml(Yaml yaml) {
		this.yaml = yaml;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.source.SourceManager#saveSourceToFile(org.opoo.press.source.Source)
	 */
	@Override
	public void saveSourceToFile(Source source) {
		File file = source.getSourceEntry().getFile();
		File dir = file.getParentFile();
		if(!dir.exists()){
			dir.mkdirs();
		}else{
			if(!dir.isDirectory()){
				throw new RuntimeException("Can not save file, '" + dir + "' is not a valid directory.");
			}
		}
		List<String> lines = new ArrayList<String>();
		lines.add(Source.TRIPLE_DASHED_LINE);
		//for(Map.Entry<String, Object> en: source.getMeta().entrySet()){
		//	
		//}
		String line = yaml.dumpAsMap(source.getMeta());
		lines.add(line);
		lines.add(Source.TRIPLE_DASHED_LINE);
		
		lines.add(source.getContent());

		try {
			FileUtils.writeLines(file, "UTF-8", lines);
		} catch (IOException e) {
			throw new RuntimeException("Write file exception", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.source.SourceManager#buildEntry(java.io.File, java.lang.String)
	 */
	@Override
	public SourceEntry buildEntry(File sourceDir, String path) {
		path = FilenameUtils.separatorsToUnix(path);
		String[] arr = StringUtils.split(path, "/");
		
		SourceEntry entry = null;
		int i = 0;
		for(String s: arr){
			System.out.println("[" + (i++) + "]: " + s);
			sourceDir = new File(sourceDir, s);
			entry = new SourceEntry(entry, sourceDir);
		}
		
		System.out.println(entry.getPath());
		System.out.println(entry.getName());
		System.out.println(entry.getFile().getAbsolutePath());
		System.out.println(entry.getParent().getFile().getAbsolutePath());
		return entry;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.source.SourceManager#buildSource(java.io.File, java.lang.String, java.util.Map, java.lang.String)
	 */
	@Override
	public Source buildSource(File sourceDir, String path, Map<String, Object> meta, String content) {
		SourceEntry entry = buildEntry(sourceDir, path);
		return new SimpleSource(entry, meta, content);
	}
	
	public static void main(String[] args){
		SourceEntry entry = new SourceManagerImpl().buildEntry(new File("."), "article/2013/07/2013-07-31-hello-world.markdown");
		System.out.println(entry);
	
		Yaml yaml = new Yaml();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("abcd", "aaaaaaaaaa");
		map.put("published", true);
		map.put("date", new Date());
		map.put("tags", new String[]{"tag1", "Tag2"});
		String string = yaml.dumpAsMap(map);
		System.out.println("[" + string + "]");
	}
}
