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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Site;

/**
 * @author Alex Lin
 *
 */
public class Cleaner {
	private static final Log log = LogFactory.getLog(Cleaner.class);

	public void clean(Site site) throws Exception{
		File destination = site.getDestination();
		File working  = site.getWorking();
		
		log.info("Cleaning destination directory " + destination);
		FileUtils.deleteDirectory(destination);
		
		log.info("Cleaning working directory " + working);
		FileUtils.deleteDirectory(working);
	}
}
