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
package org.opoo.press.source;

import org.opoo.press.SourceEntry;
import org.opoo.press.SourceEntryLoader;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Alex Lin
 *
 */
public class SourceEntryLoaderImpl implements SourceEntryLoader {

	/* (non-Javadoc)
	 * @see org.opoo.press.SourceEntryLoader#loadSourceEntries(java.io.File, java.io.FileFilter)
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

	/* (non-Javadoc)
	 * @see org.opoo.press.SourceEntryLoader#buildSourceEntry(java.io.File, java.io.File)
	 */
	@Override
	public SourceEntry buildSourceEntry(File root, File file) {
		List<File> files = new ArrayList<File>();
		File parent = file.getParentFile();
		if(parent == null){
			throw new IllegalArgumentException("Directory must contains file");
		}
		while(!parent.equals(root)){
			files.add(parent);
			parent = parent.getParentFile();
		}
		
		SourceEntry parentEntry = null;
		if(!files.isEmpty()){
			Collections.reverse(files);
			Iterator<File> iterator = files.iterator();
			parentEntry = new SourceEntry(iterator.next());
			while(iterator.hasNext()){
				parentEntry = new SourceEntry(parentEntry, iterator.next());
			}
		}
		
		return new SourceEntry(parentEntry, file);
	}
}
