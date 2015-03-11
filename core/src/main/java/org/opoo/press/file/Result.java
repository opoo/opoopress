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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Lin
 *
 */
public class Result {
	private final List<File> updatedFiles = new ArrayList<File>();
	private final List<File> createdFiles = new ArrayList<File>();
	private final List<File> deletedFiles = new ArrayList<File>();
	private int size = 0;
	
	private Result(){
	}
	
	public static Result newResult(){
		return new Result();
	}
	
	Result addUpdatedFile(File file){
		this.updatedFiles.add(file);
		this.size++;
		return this;
	}
	
	Result addCreatedFile(File file){
		this.createdFiles.add(file);
		this.size++;
		return this;
	}
	
	Result addDeletedFile(File file){
		this.deletedFiles.add(file);
		this.size++;
		return this;
	}

	public Result addResult(Result result){
		this.deletedFiles.addAll(result.deletedFiles);
		this.updatedFiles.addAll(result.updatedFiles);
		this.createdFiles.addAll(result.createdFiles);
		this.size = updatedFiles.size()
				+ createdFiles.size()
				+ deletedFiles.size();
		return this;
	}

	public List<File> getUpdatedFiles() {
		return updatedFiles;
	}

	public List<File> getCreatedFiles() {
		return createdFiles;
	}

	public List<File> getDeletedFiles() {
		return deletedFiles;
	}

	public int size(){
		return size;
//		return updatedFiles.size() 
//				+ createdFiles.size()
//				+ deletedFiles.size();
	}
	
	public void clear(){
		this.updatedFiles.clear();
		this.createdFiles.clear();
		this.deletedFiles.clear();
		this.size = 0;
	}
	
	public boolean isEmpty(){
//		return updatedFiles.isEmpty() 
//				&& createdFiles.isEmpty()
//				&& deletedFiles.isEmpty();
		return size == 0;
	}
	
	public String toString(){
		if(size == 0){
			return "No file changed.";
		}
		String lineSeparator = System.getProperty("line.separator");
		StringBuffer buffer = new StringBuffer();
		for(File f: updatedFiles){
			buffer.append("updated  ").append(f).append(lineSeparator);
		}
		for(File f: createdFiles){
			buffer.append("created  ").append(f).append(lineSeparator);
		}
		for(File f: deletedFiles){
			buffer.append("deleted  ").append(f).append(lineSeparator);
		}
		return buffer.toString();
	}
}
