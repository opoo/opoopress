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
package org.opoo.press.resource;

import org.opoo.press.ResourceBuilder;

import java.io.File;
import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public class CssBuilder extends YUIBuilder implements ResourceBuilder {
//	@Override
//	public void init(Site site, Theme theme, Map<String, Object> config) {
//		init("css", theme.getPath(), config);
//	}

	@Override
	public void init(File resourceBaseDirectory, Map<String, Object> config) {
		init("css", resourceBaseDirectory, config);
	}
}
