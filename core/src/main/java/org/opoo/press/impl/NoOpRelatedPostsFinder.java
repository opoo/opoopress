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

import java.util.Collections;
import java.util.List;

import org.opoo.press.Post;
import org.opoo.press.RelatedPostsFinder;

/**
 * @author Alex Lin
 * @since 1.2
 */
public class NoOpRelatedPostsFinder implements RelatedPostsFinder {

	/* (non-Javadoc)
	 * @see org.opoo.press.RelatedPostsFinder#findRelatedPosts(org.opoo.press.Post)
	 */
	@Override
	public List<Post> findRelatedPosts(Post post) {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.RelatedPostsFinder#findRelatedPosts(org.opoo.press.Post, int)
	 */
	@Override
	public List<Post> findRelatedPosts(Post post, int size) {
		return Collections.emptyList();
	}

}
