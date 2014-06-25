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
package org.opoo.press.impl;

import java.io.File;

import org.opoo.press.Config;
import org.opoo.press.Site;
import org.opoo.press.Theme;
import org.opoo.press.ThemeManager;
import org.opoo.util.PathUtils;
import org.opoo.util.PathUtils.Strategy;

/**
 * @author Alex Lin
 * @since 1.2
 */
public class ThemeManagerImpl implements ThemeManager {

	/* (non-Javadoc)
	 * @see org.opoo.press.ThemeManager#getTheme(org.opoo.press.Site)
	 */
	@Override
	public Theme getTheme(Site site) {
		Config config = site.getConfig();
		String name = config.get("theme", "default");
		File base = config.getBasedir();
		File themeDir = PathUtils.dir(base, "themes/" + name, Strategy.THROW_EXCEPTION_IF_NOT_EXISTS);
		themeDir = PathUtils.canonical(themeDir);
		return new ThemeImpl(themeDir, site);
	}

}
