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

import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public abstract class MapUtils {
	
	public static <K,V> void put(Map<K, V> map, K key, V value){
		if(value == null){
			map.remove(key);
		}else{
			map.put(key, value);
		}
	}
	
	public static <K,V> boolean get(Map<K,V> map, K key, boolean defaultValue){
		Object object = map.get(key);
		if(object == null){
			return defaultValue;
		}
		if(object instanceof Boolean){
			return ((Boolean)object).booleanValue();
		}
		return "true".equalsIgnoreCase(object.toString());
	}
	
	public static <K,V> V get(Map<K,V> map, K key, V defaultValue){
		if(!map.containsKey(key)){
			return defaultValue;
		}
		return map.get(key);
	}
	
	
	public static <K,V> K getKeyByValue(Map<K,V> map, V value){
		if(map == null || map.isEmpty()){
			return null;
		}
		for(Map.Entry<K, V> en: map.entrySet()){
			if(value.equals(en.getValue())){
				return en.getKey();
			}
		}
		return null;
	}
}
