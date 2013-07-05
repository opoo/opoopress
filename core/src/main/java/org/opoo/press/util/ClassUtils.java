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
package org.opoo.press.util;

import org.opoo.press.Initializable;
import org.opoo.press.Site;

/**
 * @author Alex Lin
 *
 */
public abstract class ClassUtils extends org.apache.commons.lang.ClassUtils {
	
	/**
	 * Create a new instance for the specified class name and call 
	 * {@link Initializable#initialize(Site)} if required.
	 * @param className class name
	 * @param site site object
	 * @return new instance
	 */
	public static Object newInstance(String className, Site site){
		Object instance = newInstance(className);
		if(instance instanceof Initializable){
			((Initializable) instance).initialize(site);
		}
		return instance;
	}
	
	/**
	 * Create a new instance for the specified class name.
	 * @param className class name
	 * @return new instance
	 */
	public static Object newInstance(String className){
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Class<?> clazz = getClass(classLoader, className);
			return clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		}
	}
}
