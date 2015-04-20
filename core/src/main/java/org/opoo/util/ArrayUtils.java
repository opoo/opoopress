/*
 * Copyright 2015 Alex Lin.
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
package org.opoo.util;


import com.google.common.base.Predicate;

/**
 * @author Alex Lin
 *
 */
public class ArrayUtils extends org.apache.commons.lang.ArrayUtils{

    /**
     * Returns the first element in {@code array} that satisfies the given
     * predicate.
     *
     * @param array
     * @param predicate
     * @param <T>
     * @return
     */
    public static <T> T find(T[] array, Predicate<T> predicate){
        if(array == null){
            return null;
        }
        for(T t: array){
            if(predicate.apply(t)){
                return t;
            }
        }
        return null;
    }
}
