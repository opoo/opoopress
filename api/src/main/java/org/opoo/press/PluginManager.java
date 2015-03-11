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


import java.util.List;
import java.util.Map;

/**
 * @author Alex Lin
 * @since 1.2
 */
public interface PluginManager extends Registry {

    //List<Converter> getConverters();

    Converter getConverter(Source source);

    List<Generator> getGenerators();

    List<Processor> getProcessors();

//	List<TemplateLoader> getTemplateLoaders();

//	Map<String, TemplateModel> getTemplateModels();

    <T> List<T> getObjectList(Class<T> clazz);

    <T> Map<String, T> getObjectMap(Class<T> clazz);
}
