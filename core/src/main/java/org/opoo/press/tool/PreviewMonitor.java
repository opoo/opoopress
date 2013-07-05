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
package org.opoo.press.tool;

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
public class PreviewMonitor {
	private static final Log log = LogFactory.getLog(PreviewMonitor.class);
	
//	private class SiteAlterationListenerAdaptor extends FileAlterationListenerAdaptor implements FileAlterationListener {
//		public void onFileCreate(File file) {
//			onFileChange(file);
//		}
//
//		public void onFileDelete(File file) {
//			onFileChange(file);
//		}
//	}
//	
//	/**
//	 * 
//	 * @param site site object
//	 * @param siteDir site directory
//	 * @param interval time unit is second
//	 * @throws Exception
//	 */
//    public void startSiteMonitor(final Site site, File siteDir, int interval) throws Exception{
//		long intervalMillis = interval * 1000;
//		if(intervalMillis <= 1000){
//			intervalMillis = 1000;
//		}
//
//		// observer all site directory
//		FileAlterationObserver observer = new FileAlterationObserver(siteDir);
//		
//		SiteAlterationListenerAdaptor w = new SiteAlterationListenerAdaptor(){
//			public void onFileChange(File file) {
//				if("config.yml".equals(file.getName())){
//					log.warn("Config file changed, you need stop and run preview again.");
//				}else{
//					//rebuild 
//					log.info("File '" + file + "' changed, regenerate...");
//
//					long start = System.currentTimeMillis();
//					site.build();
//					long time = System.currentTimeMillis() - start;
//					log.info("Rebuild time: " + time + "ms");
//				}
//			}
//		};
//		//set file change listener
//		observer.addListener(w); 
//		//create file alteration monitor
//		FileAlterationMonitor monitor = new FileAlterationMonitor(interval,	observer);
//		
//		//start monitor
//		monitor.start();
//	}
    
    
    public void start(File siteDir, int interval, FileAlterationListener listener) throws Exception{
    	long intervalMillis = interval * 1000;
		if(intervalMillis <= 1000){
			intervalMillis = 1000;
		}

		// observer all site directory
		FileAlterationObserver observer = new FileAlterationObserver(siteDir);
		// set file change listener
		observer.addListener(listener); 
		//create file alteration monitor
		FileAlterationMonitor monitor = new FileAlterationMonitor(interval,	observer);
		
		log.info("Starting monitor on directory: " + siteDir);
		//start monitor
		monitor.start();
    }
}
