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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.opoo.press.source.SourceEntry;
import org.opoo.press.source.SourceEntryLoader;

/**
 * @author Alex Lin
 *
 */
public class SourceEntryLoaderImpl  implements SourceEntryLoader {

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.source.SourceEntryLoader#loadSourceEntries(java.io.File, java.io.FileFilter)
	 */
	@Override
	public List<SourceEntry> loadSourceEntries(File sourcePath, FileFilter fileFilter) {
		List<SourceEntry> results = new ArrayList<SourceEntry>();
		listFileEntries(results, sourcePath, null, fileFilter);
		return results;
	}
	
	private void listFileEntries(List<SourceEntry> results, File parent, SourceEntry parentEntry, FileFilter fileFilter){
		File[] files = parent.listFiles(fileFilter);
		SourceEntry[] children = new SourceEntry[files.length];
		for(int i = 0 ; i < files.length ; i++){
			File file = files[i];
			SourceEntry child = new SourceEntry(parentEntry, file);
			children[i] = child;
			
			if(file.isFile()){
				results.add(child);
			}else if(file.isDirectory()){
				listFileEntries(results, file, child, fileFilter);
			}
		}
		if(parentEntry != null){
			parentEntry.setChildren(children);
		}
	}
}
