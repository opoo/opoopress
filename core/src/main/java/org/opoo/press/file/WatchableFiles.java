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
package org.opoo.press.file;

import org.apache.commons.io.monitor.FileEntry;

import java.io.File;

/**
 * @author Alex Lin
 *
 */
public class WatchableFiles implements Watchable {
	private FileEntry[] entries;
	public WatchableFiles(File... files){
		if(files == null || files.length == 0){
			throw new IllegalArgumentException("files are required.");
		}
		entries = new FileEntry[files.length];
		for(int i = 0 ; i < files.length ; i++){
			entries[i] = new FileEntry(files[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.monitor.Observer#initialize()
	 */
	@Override
	public void initialize() {
		for(FileEntry entry: entries){
			entry.refresh(entry.getFile());
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.monitor.Observer#check()
	 */
	@Override
	public Result check() {
		Result result = Result.newResult();
		for(FileEntry entry: entries){
			if(entry.refresh(entry.getFile())){
				//check update only
				result.addUpdatedFile(entry.getFile());
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.monitor.Observer#destroy()
	 */
	@Override
	public void destroy() {
	}
}
