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
package org.opoo.press.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opoo.press.Convertible;
import org.opoo.press.Renderer;
import org.opoo.press.source.Source;

/**
 * @author Alex Lin
 *
 */
public abstract class AbstractConvertible implements Convertible {
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	public abstract String getUrl();

	public abstract String getContent();

	public abstract void setContent(String content);
	
	public abstract String getOutputFileExtension();
	
	public abstract String getLayout();
	
	protected abstract Renderer getRenderer();
	
	protected abstract void convert();
	
	protected abstract void mergeRootMap(Map<String,Object> rootMap);

	public abstract Source getSource();
	
	/* (non-Javadoc)
	 * @see org.opoo.press.Convertible#render(java.util.Map)
	 */
	@Override
	public void render(Map<String, Object> rootMap) {
		convert();
		
		Renderer renderer = getRenderer();
		boolean isContentRenderRequired = renderer.isRenderRequired(getContent());
		boolean isValidLayout = renderer.isValidLayout(getLayout());
		
		if(!isValidLayout && !isContentRenderRequired){
			log.debug("Layout is nil and content is plain text, skip render file: " 
					+ getSource().getSourceEntry().getFile());
			//output = content;
			//do nothing
			return;
		}
		
		rootMap = new HashMap<String,Object>(rootMap);
		mergeRootMap(rootMap);
		//if content is plain text
		if(!isContentRenderRequired){
			rootMap.put("content", getContent());
		}
		
		String name = renderer.prepareWorkingTemplate(getLayout(), isValidLayout, 
				getContent(), isContentRenderRequired, 
				getSource().getSourceEntry());
		String output = renderer.render(name, rootMap);
//		String output = getRenderer().render(getLayout(), getContent(), getSource().getSourceEntry(), rootMap);
		setContent(output);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Convertible#getOutputFile(java.io.File)
	 */
	@Override
	public File getOutputFile(File dest) {
		String url = getUrl();
		if(url.endsWith("/")){
			url += "index" + getOutputFileExtension();
		}
		File target = new File(dest, url);
		return target;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.Convertible#write(java.io.File)
	 */
	@Override
	public void write(File dest) {
		File file = getOutputFile(dest);
//		FileWriter fw = null;
		try {

			File parentFile = file.getParentFile();
			if(!parentFile.exists()){
				parentFile.mkdirs();
			}
//			fw = new FileWriter(file);
//			IOUtils.write(getContent(), fw);
//			fw.flush();
			
			FileUtils.write(file, getContent(), "UTF-8");
		} catch (IOException e) {
			log.error("Write file error: " + file, e);
			throw new RuntimeException(e);
		}finally{
//			IOUtils.closeQuietly(fw);
		}
	}
}
