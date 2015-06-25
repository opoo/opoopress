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

import org.apache.commons.io.monitor.FileEntry;
import org.opoo.press.SourceEntry;
import org.opoo.press.SourceEntryLoader;
import org.opoo.press.SourceEntryVisitor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
	public List<SourceEntry> loadSourceEntries(File sourceDirectory, FileFilter fileFilter) {
		List<SourceEntry> results = new ArrayList<SourceEntry>();
		listFileEntries(sourceDirectory, fileFilter, results, sourceDirectory, null);
		return results;
	}
	
	private void listFileEntries(File sourceDirectory, FileFilter fileFilter,
								 List<SourceEntry> results, File parent, OldSourceEntry parentEntry){
		File[] files = parent.listFiles(fileFilter);
		for(File file: files){
			OldSourceEntry child = new OldSourceEntry(parentEntry, file, sourceDirectory);
			if(file.isFile()){
				results.add(child);
			}else if(file.isDirectory()){
				listFileEntries(sourceDirectory, fileFilter, results, file, child);
			}
		}
	}

	@Override
	public void walkSourceTree(File sourceDirectory, FileFilter fileFilter, SourceEntryVisitor sourceEntryVisitor){
		walkSourceTree(sourceDirectory, fileFilter, sourceEntryVisitor, sourceDirectory, "");
	}

	private void walkSourceTree(File sourceDirectory, FileFilter fileFilter,
								SourceEntryVisitor sourceEntryVisitor, File parent, String path){
		File[] files = parent.listFiles(fileFilter);
		for(File file: files){
			if(file.isFile()){
				SourceEntryImpl sourceEntry = new SourceEntryImpl(file, sourceDirectory, path);
				sourceEntryVisitor.visit(sourceEntry);
			}else if(file.isDirectory()){
				walkSourceTree(sourceDirectory, fileFilter, sourceEntryVisitor, file, path + "/" + file.getName());
			}
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

		OldSourceEntry parentEntry = null;
		if(!files.isEmpty()){
			Collections.reverse(files);
			Iterator<File> iterator = files.iterator();
			parentEntry = new OldSourceEntry(iterator.next(), root);
			while(iterator.hasNext()){
				parentEntry = new OldSourceEntry(parentEntry, iterator.next(), root);
			}
		}
		
		return new OldSourceEntry(parentEntry, file, root);
	}

	private static class OldSourceEntry extends FileEntry implements SourceEntry{
		private final File sourceDirectory;
		private String path = "";

		public OldSourceEntry(File file, File sourceDirectory) {
			super(file);
			this.sourceDirectory = sourceDirectory;
		}

		public OldSourceEntry(OldSourceEntry parent, File file, File sourceDirectory) {
			super(parent, file);
			this.sourceDirectory = sourceDirectory;
			if(parent != null){
				this.path = parent.getPath() + "/" + parent.getName();
			}
		}

		@Override
		public File getSourceDirectory() {
			return sourceDirectory;
		}

		@Override
		public String getPath() {
			return path;
		}
	}

	public static class SourceEntryImpl implements SourceEntry{
		private File file;
		private String path = "";
		private File sourceDirectory;
		private long lastModified;
		private long length;

		public SourceEntryImpl(File file, File sourceDirectory){
			this(file, sourceDirectory, "");
		}

		private SourceEntryImpl(File file, File sourceDirectory, SourceEntryImpl parent) {
			this(file, sourceDirectory, (parent == null) ? "" : parent.getPath() + "/" + parent.getName());
		}

		private SourceEntryImpl(File file, File sourceDirectory, String path){
			if(!file.isFile() || !file.exists()){
				throw new IllegalArgumentException("It's not a file or not exists: " + file);
			}

			this.file = file;
			this.sourceDirectory = sourceDirectory;
			this.path = (path == null) ? "" : path;
			this.lastModified = file.lastModified();
			this.length = file.length();
		}

		@Override
		public File getFile() {
			return file;
		}

		@Override
		public String getName() {
			return file.getName();
		}

		@Override
		public long getLastModified() {
			return lastModified;
		}

		@Override
		public long getLength() {
			return length;
		}

		@Override
		public File getSourceDirectory() {
			return sourceDirectory;
		}

		@Override
		public String getPath() {
			return path;
		}

		private void writeObject(java.io.ObjectOutputStream out)
				throws IOException{
			out.writeObject(file);
			out.writeObject(sourceDirectory);
			out.writeObject(path);
			out.writeLong(lastModified);
			out.writeLong(length);
		}
		private void readObject(java.io.ObjectInputStream in)
				throws IOException, ClassNotFoundException{
			this.file = (File) in.readObject();
			this.sourceDirectory = (File) in.readObject();
			this.path = (String) in.readObject();
			this.lastModified = in.readLong();
			this.length = in.readLong();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SourceEntryImpl){
				SourceEntryImpl o = (SourceEntryImpl) obj;
				return file.equals(o.file)
						&& sourceDirectory.equals(o.sourceDirectory)
						&& path.equals(o.path)
						&& lastModified == o.lastModified
						&& length == o.length;
			}
			return false;
		}
	}
}
