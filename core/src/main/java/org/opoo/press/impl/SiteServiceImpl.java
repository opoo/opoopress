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

import org.opoo.press.CompassConfig;
import org.opoo.press.Site;
import org.opoo.press.SiteConfig;
import org.opoo.press.SiteService;
import org.yaml.snakeyaml.Yaml;

/**
 * @author Alex Lin
 *
 */
public class SiteServiceImpl implements SiteService {
	private Yaml yaml;

	public void setYaml(Yaml yaml) {
		this.yaml = yaml;
	}

	public Yaml getYaml() {
		return yaml;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#getSite(java.io.File)
	 */
	@Override
	public Site createSite(File siteDir) {
		return createSite(siteDir, null);
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteManager#getSite(java.io.File, java.util.Map)
	 */
	@Override
	public Site createSite(File siteDir, Map<String, Object> extraOptions) {
		SiteConfigImpl siteConfigImpl = new SiteConfigImpl(siteDir, extraOptions);
		siteConfigImpl.setYaml(yaml);
		return new SiteImpl(siteConfigImpl);
	}

	@Override
	public SiteConfig createSiteConfig(File siteDir, Map<String, Object> extraOptions) {
		SiteConfigImpl siteConfigImpl = new SiteConfigImpl(siteDir, extraOptions);
		siteConfigImpl.setYaml(yaml);
		return siteConfigImpl;
	}

	@Override
	public Site createSite(SiteConfig siteConfig) {
		if(siteConfig instanceof SiteConfigImpl){
			return new SiteImpl((SiteConfigImpl) siteConfig);
		}
		throw new IllegalArgumentException("SiteConfig type not match.");
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.SiteService#getCompassConfig(java.io.File)
	 */
	@Override
	public CompassConfig createCompassConfig(File compassProjectPath) {
		return new CompassConfigImpl(compassProjectPath);
	}
}
