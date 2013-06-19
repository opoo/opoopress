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
package org.opoo.press.converter;

import org.apache.commons.io.FilenameUtils;
import org.opoo.press.Converter;
import org.opoo.press.source.Source;

/**
 * @author Alex Lin
 *
 */
public class IdentityConverter implements Converter {

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Converter#matches(org.opoo.joctopress.source.Source)
	 */
	@Override
	public boolean matches(Source src) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Converter#convert(java.lang.String)
	 */
	@Override
	public String convert(String content) {
		return content;
	}

	/* (non-Javadoc)
	 * @see org.opoo.joctopress.Converter#getOutputFileExtension(org.opoo.joctopress.source.Source)
	 */
	@Override
	public String getOutputFileExtension(Source src) {
		String name = src.getSourceEntry().getName();
		return "." + FilenameUtils.getExtension(name);
	}
}
