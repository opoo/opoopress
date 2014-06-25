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
import java.util.Map;

import org.opoo.press.Config;
import org.opoo.press.Site;
import org.opoo.press.SiteService;

/**
 * @author Alex Lin
 *
 */
public class SiteServiceImpl implements SiteService {
	
	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#getSite(java.io.File)
	 */
	@Override
	public Site createSite(File siteDir) {
		return createSite(siteDir, null);
	}
	/* (non-Javadoc)
	 * @see org.opoo.press.SiteService#createConfig(java.io.File, java.util.Map)
	 */
	@Override
	public Config createConfig(File siteDir, Map<String, Object> override) {
		return new ConfigImpl(siteDir, override);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteService#createSite(org.opoo.press.Config)
	 */
	@Override
	public Site createSite(Config siteConfig) {
		if(siteConfig instanceof ConfigImpl){
			return new SiteImpl((ConfigImpl)siteConfig);
		}else{
			throw new IllegalArgumentException("Config type not match.");
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteService#createSite(java.io.File, java.util.Map)
	 */
	@Override
	public Site createSite(File siteDir, Map<String, Object> override) {
		ConfigImpl configImpl = new ConfigImpl(siteDir, override);
		return new SiteImpl(configImpl);
	}
}
