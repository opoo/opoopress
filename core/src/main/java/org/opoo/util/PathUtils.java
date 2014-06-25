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
package org.opoo.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * @author Alex Lin
 *
 */
public abstract class PathUtils {
	public static enum Strategy{
		NONE, CREATE_IF_NOT_EXISTS, THROW_EXCEPTION_IF_NOT_EXISTS, CREATE_ALWAYS;
	}
	
	public static File dir(File basedir, String path, Strategy p){
		File dir = new File(basedir, path);
		if(!dir.exists()){
			if(p == Strategy.CREATE_IF_NOT_EXISTS){
				dir.mkdirs();
			}else if(p == Strategy.THROW_EXCEPTION_IF_NOT_EXISTS){
				throw new IllegalArgumentException("Directory not exits: " + dir);
			}
		}else{
			if(!dir.isDirectory()){
				throw new IllegalArgumentException("Path exits but not a directory: " + dir);
			}
		}
		//Create new directory whatever
		if(Strategy.CREATE_ALWAYS == p){
			if(dir.exists()){
				FileUtils.deleteQuietly(dir);
			}
			dir.mkdirs();
		}
		
		return dir;
	}
	
	public static File dir(File basedir, String path){
		return dir(basedir, path, Strategy.NONE);
	}
	
	public static File file(File basedir, String path, Strategy p){
		File file = new File(basedir, path);
		if(!file.exists()){
			if(Strategy.CREATE_IF_NOT_EXISTS == p){
				file.getParentFile().mkdirs();
			}else if(Strategy.THROW_EXCEPTION_IF_NOT_EXISTS == p){
				throw new IllegalArgumentException("File not exits: " + file);
			}
		}else{
			if(!file.isFile()){
				throw new IllegalArgumentException("Path exits but not a file: " + file);
			}
		}
		
		//Create new directory whatever
		if(Strategy.CREATE_ALWAYS == p){
			if(file.exists()){
				FileUtils.deleteQuietly(file);
			}
			if(!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
		}
		
		return file;
	}
	
	public static File file(File basedir, String path){
		return file(basedir, path, Strategy.NONE);
	}
	
	public static File canonical(File file){
		try {
			return file.getCanonicalFile();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static boolean isValidDirectory(File dir){
		return dir != null && dir.exists() && dir.isDirectory() && dir.canRead();
	}
}
