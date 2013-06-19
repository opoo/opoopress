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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Site;
import org.opoo.press.StaticFile;
import org.opoo.press.source.SourceEntry;

/**
 * @author Alex Lin
 *
 */
public class StaticFileImpl implements StaticFile {
	private static final Log log = LogFactory.getLog(StaticFileImpl.class);
	
	private Site site;
	private SourceEntry sourceEntry;

	public StaticFileImpl(Site site, SourceEntry sourceEntry){
		this.site = site;
		this.sourceEntry = sourceEntry;
	}

	/**
	 * @return the site
	 */
	public Site getSite() {
		return site;
	}

	/**
	 * @param site the site to set
	 */
	public void setSite(Site site) {
		this.site = site;
	}

	/**
	 * @return the sourceEntry
	 */
	public SourceEntry getSourceEntry() {
		return sourceEntry;
	}

	/**
	 * @param sourceEntry the sourceEntry to set
	 */
	public void setSourceEntry(SourceEntry sourceEntry) {
		this.sourceEntry = sourceEntry;
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.StaticFile#getOutputFile(java.io.File)
	 */
	@Override
	public File getOutputFile(File dest) {
		String file = sourceEntry.getPath() + "/" + sourceEntry.getName(); 
		return new File(dest, file);
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.StaticFile#write(java.io.File)
	 */
	@Override
	public void write(File dest) {
		File target = getOutputFile(dest);

		if(target.exists() && target.lastModified() >= sourceEntry.getLastModified()){
			log.debug("Target file is newer than source file, skip copying.");
			return;
		}
		
		try {
			File parentFile = target.getParentFile();
			if(!parentFile.exists()){
				parentFile.mkdirs();
			}
			
			FileUtils.copyFile(sourceEntry.getFile(), target);
		} catch (IOException e) {
			log.error("Copying static file error: " + target, e);
			throw new RuntimeException(e);
		}
	}
}
