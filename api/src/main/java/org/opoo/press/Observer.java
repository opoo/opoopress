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
package org.opoo.press;

/**
 * Base usage.
 * <pre>
 *      Observer observer = ...;
 *      // intialize
 *      observer.initialize();
 *      ...
 *      // invoke as required
 *      observer.check();
 *      ...
 *      observer.check();
 *      ...
 *      // finished
 *      observer.destroy();
 * </pre>
 *
 * @author Alex Lin
 */
public interface Observer {

    void initialize() throws Exception;

    void check() throws Exception;

    void destroy() throws Exception;
}
