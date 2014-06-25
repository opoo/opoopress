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

import org.opoo.press.Config;
import org.opoo.press.ConfigAware;
import org.opoo.press.Site;
import org.opoo.press.SiteAware;

/**
 * @author Alex Lin
 *
 */
public abstract class ClassUtils extends org.apache.commons.lang.ClassUtils {
	
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String className, Site site, Class<T> clazz){
		return (T) newInstance(className, clazz.getClassLoader(), site, site != null ? site.getConfig() : null);
	}
	
	/**
	 * Create a new instance for the specified class name.
	 * @param className class name
	 * @param site site object
	 * @return new instance
	 */
	public static Object newInstance(String className, Site site){
		return newInstance(className, null, site, site != null ? site.getConfig() : null);
	}
	
	/**
	 * Create a new instance for the specified class name.
	 * @param className class name
	 * @param config site configuration
	 * @return new instance
	 */
	public static Object newInstance(String className, Config config){
		return newInstance(className, null, null, config);
	}
	
	public static Object newInstance(String className, ClassLoader classLoader, Site site, Config config){
		Object instance = newInstance(className, classLoader);
		if(instance instanceof SiteAware){
			((SiteAware) instance).setSite(site);
		}
		if(instance instanceof ConfigAware){
			((ConfigAware)instance).setConfig(config);
		}
		return instance;
	}
	
	/**
	 * Create a new instance for the specified class name.
	 * @param className class name
	 * @return new instance
	 */
	public static Object newInstance(String className){
		return newInstance(className, (ClassLoader)null);
	}
	
	public static Object newInstance(String className, ClassLoader classLoader){
		try {
			if(classLoader == null){
				classLoader = Thread.currentThread().getContextClassLoader();
			}
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
