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

/**
 * @author Alex Lin
 */
public class ObserversObserver implements Observer {
    private List<Observer> observers;

    /**
     * @param observers
     */
    public ObserversObserver(List<Observer> observers) {
        super();
        this.observers = observers;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Observer#initialize()
     */
    @Override
    public void initialize() throws Exception {
        for (Observer o : observers) {
            o.initialize();
        }
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Observer#check()
     */
    @Override
    public void check() throws Exception {
        for (Observer o : observers) {
            o.check();
        }
    }

    /* (non-Javadoc)
     * @see org.opoo.press.Observer#destroy()
     */
    @Override
    public void destroy() throws Exception {
        for (Observer o : observers) {
            o.destroy();
        }
    }

}
