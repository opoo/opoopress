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
package org.opoo.press.support;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Alex Lin
 *
 */
public class DirectoryMonitor {
	private static final Log log = LogFactory.getLog(DirectoryMonitor.class);
	private final FileAlterationMonitor monitor;
	private final File siteDir;
	
	DirectoryMonitor(File siteDir, int interval, FileAlterationListener listener) {
		this.siteDir = siteDir;
		
		long intervalMillis = interval * 1000;
		if(intervalMillis <= 1000){
			intervalMillis = 10000;
		}

		// observer all site directory
		FileAlterationObserver observer = new FileAlterationObserver(siteDir);
		// set file change listener
		observer.addListener(listener); 
		//create file alteration monitor
		monitor = new FileAlterationMonitor(interval, observer);
	}
	
    public void start() throws Exception{
    	if(monitor != null){
			log.info("Starting monitor on directory: " + siteDir);
			//start monitor
			monitor.start();
    	}
    }
    
    public FileAlterationMonitor getMonitor() {
		return monitor;
	}
    
    public void stop() throws Exception{
    	if(monitor != null){
    		monitor.stop();
    	}
    }
}
