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
package org.opoo.press;

import java.io.File;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public interface SiteManager extends SiteService{

	Site install(File siteDir, Locale locale, boolean createSamplePost) throws Exception;
	
	void clean(Site site) throws Exception;
	
	File newPage(Site site, String title, String name) throws Exception;
	
	File newPost(Site site, String title, String name, boolean draft) throws Exception;
	
	void build(Site site);

	void doImport(Site site, String importer, Map<String,Object> params) throws Exception;
}
