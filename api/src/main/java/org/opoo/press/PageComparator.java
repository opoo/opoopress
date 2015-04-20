/*
 * Copyright 2013-2015 Alex Lin.
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

import java.util.Comparator;
import java.util.Date;

/**
 * @author Alex Lin
 */
public class PageComparator implements Comparator<Page>{
    public static final PageComparator INSTANCE = new PageComparator();

    @Override
    public int compare(Page o1, Page o2) {
        Date date1 = o1.getDate();
        Date date2 = o2.getDate();

        if(date1 != null && date2 != null){
            return date2.compareTo(date1);
        }

        return 0;
    }
}
