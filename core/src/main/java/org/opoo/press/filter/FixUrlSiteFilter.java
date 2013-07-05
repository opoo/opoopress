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
package org.opoo.press.filter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opoo.press.Site;

/**
 * @author Alex Lin
 *
 */
public class FixUrlSiteFilter extends SiteFilterAdapter {
	private static final Log log = LogFactory.getLog(FixUrlSiteFilter.class);

	/* (non-Javadoc)
	 * @see org.opoo.press.filter.SiteFilterAdapter#postWrite(org.opoo.press.Site)
	 */
	@Override
	public void postWrite(Site site) {
		String root = site.getRoot();
		if(StringUtils.isNotBlank(root)){
			String cssFile = "screen.css";
			File file = new File(site.getDestination(), "stylesheets/" + cssFile);
			try {
				String content = FileUtils.readFileToString(file);
				content = StringUtils.replace(content, "'/images/", "'" + root + "/images/");
				content = StringUtils.replace(content, "\"/images/", "\"" + root + "/images/");
				FileUtils.write(file, content, "UTF-8");
				log.debug("Fix file: " + file);
			} catch (IOException e) {
				log.error("Fix url in css file failed: " + file, e);
			}
		}
	}
}
