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
 * @author Alex Lin
 * @since 1.2
 */
public interface Processor extends Ordered {

    void postSetup(Site site);

    void postRead(Site site, Page page);

    void postRead(Site site);

    void postGenerate(Site site);

    void postConvertPage(Site site, Page page);

    void postConvertAllPages(Site site);

    void preRenderAllPages(Site site);

    void preRenderPage(Site site, Page page);

    void postRenderPage(Site site, Page page);

    void postRenderAllPages(Site site);

    void postCleanup(Site site);

    void postWrite(Site site);

    void beforeBuildTheme(Theme theme);

    void afterBuildTheme(Theme theme);
}
