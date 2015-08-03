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
package org.opoo.press.generator;

import org.opoo.press.Generator;
import org.opoo.press.Ordered;
import org.opoo.press.Site;

import java.util.Collections;
import java.util.List;

/**
 * @author Alex Lin
 */
public class GeneratorsGenerator implements Generator {
    private final List<Generator> generators;

    public GeneratorsGenerator(List<Generator> generators) {
        this.generators = generators;
        if (this.generators != null) {
            Collections.sort(generators, Ordered.COMPARATOR);
        }
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Ordered#getOrder()
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Generator#generate(org.opoo.press.Site)
     */
    @Override
    public void generate(Site site) {
        if (generators != null) {
            for (Generator g : generators) {
                g.generate(site);
            }
        }
    }
}
