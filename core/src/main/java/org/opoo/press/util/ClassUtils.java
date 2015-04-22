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

import org.opoo.press.ConfigAware;
import org.opoo.press.Site;
import org.opoo.press.SiteAware;
import org.opoo.press.SiteConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public abstract class ClassUtils extends org.apache.commons.lang.ClassUtils {
    private static Map<String,Class<?>> classMap = new HashMap<String, Class<?>>();
    private static Map<String,ClassNotFoundException> classNotFoundExceptionMap = new HashMap<String, ClassNotFoundException>();
	private static Map<String,Constructor> constructorMap = new HashMap<String, Constructor>();
	private static Map<String,NoSuchMethodException> noSuchMethodExceptionMap = new HashMap<String, NoSuchMethodException>();

	/**
	 * Create a new instance for the specified class name.
	 * @param className class name
	 * @param site site object
	 * @return new instance
	 */
	public static <T> T newInstance(String className, Site site){
		return newInstance(className, null, site, site != null ? site.getConfig() : null);
	}
	
	/**
	 * Create a new instance for the specified class name.
	 * @param className class name
	 * @param config site configuration
	 * @return new instance
	 */
	public static <T> T newInstance(String className, SiteConfig config){
		return newInstance(className, null, null, config);
	}
	
	/**
	 * Create a new instance for the specified class name.
	 * @param className class name
	 * @param <T>
	 * @return new instance
	 */
	public static <T> T newInstance(String className){
		return newInstance(className, (ClassLoader)null);
	}

	/**
	 *
	 * @param className
	 * @param classLoader
	 * @param <T>
	 * @return
	 */
	public static <T> T newInstance(String className, ClassLoader classLoader){
		return newInstance(className, classLoader, null, null);
	}

	/**
	 *
	 * @param className class name
	 * @param classLoader class loader
	 * @param site site
	 * @param config site configuration
	 * @param <T> type of class
	 * @return class instance
	 */
	public static <T> T newInstance(String className, ClassLoader classLoader, Site site, SiteConfig config){
		if(classLoader == null){
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		T instance = null;
		try {
			Class<?> clazz = getCachedClass(classLoader, className);
			instance = (T) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Create instance failed: " + className, e);
		}

		apply(instance, site);

		return instance;
	}

	public static <T> T apply(T t, Site site) {
		if(t instanceof SiteAware){
			((SiteAware) t).setSite(site);
		}
		if(t instanceof ConfigAware){
			((ConfigAware) t).setConfig(site.getConfig());
		}
		return t;
	}

	public static <T> void apply(Iterable<T> list, Site site){
		for(T t: list){
			apply(t, site);
		}
	}

    public static Class getCachedClass(ClassLoader classLoader, String className) throws ClassNotFoundException {
        Class<?> clazz = classMap.get(className);
        if(clazz != null){
            return clazz;
        }

        ClassNotFoundException exception = classNotFoundExceptionMap.get(className);
        if(exception != null){
            throw exception;
        }

        try {
            clazz = org.apache.commons.lang.ClassUtils.getClass(classLoader, className);
            classMap.put(className, clazz);
        } catch (ClassNotFoundException e) {
            classNotFoundExceptionMap.put(className, e);
            throw e;
        }

        return clazz;
    }

	public static <T> T constructInstance(String className, ClassLoader classLoader, Class<?>[] parameterTypes, Object[] args){
		if(parameterTypes == null || parameterTypes.length != args.length) {
			parameterTypes = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				parameterTypes[i] = args[i].getClass();
			}
		}

		Constructor<T> constructor = ClassUtils.getCachedConstructor(classLoader, className, parameterTypes);
		return ClassUtils.newInstance(constructor, args);
	}


	public static <T> Constructor<T> getCachedConstructor(ClassLoader classLoader, String className, Class<?>... parameterTypes){
		String cacheKey = className;
		for(Class<?> type: parameterTypes){
			cacheKey += ":" + type.getName();
		}
		if(constructorMap.containsKey(cacheKey)){
			return (Constructor<T>) constructorMap.get(cacheKey);
		}

		if(noSuchMethodExceptionMap.containsKey(cacheKey)){
			throw new RuntimeException( noSuchMethodExceptionMap.get(cacheKey));
		}

		try {
			Class<T> clazz = ClassUtils.getCachedClass(classLoader, className);
			return clazz.getConstructor(parameterTypes);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}


	public static <T> T newInstance(Constructor<T> constructor, Object... args){
		try {
			return constructor.newInstance(args);
		} catch (InstantiationException e) {
			throw new RuntimeException("Create instance failed: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Create instance failed: " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Create instance failed: " + e.getTargetException().getMessage(),
					e.getTargetException());
		}
	}
}
