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
package org.opoo.press.pagination;

import org.apache.commons.io.FilenameUtils;
import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.PaginationUpdater;

/**
 * @author Alex Lin
 */
public class DefaultPaginationUpdater implements PaginationUpdater {
    @Override
    public void update(Page page) {
        if(page.getPager() != null && page.getPager().getPageNumber() > 1){
            updateInternal(page, page.getPager());
        }
    }

    protected void updateInternal(Page page, Pager pager) {
        updateUrl(page, pager);
        updateTitle(page, pager);
    }

    protected void updateUrl(Page page, Pager pager) {
        String url = page.getUrl();
        int pageNumber = pager.getPageNumber();

        if(url.endsWith("/")){
            // /a/b/c/name/ --> /a/b/c/name/page/2/
            url += "page/" + pageNumber + "/";
        }else{
            //  /a/b/c/name.html --> /a/b/c/name-p2.html
            url = FilenameUtils.removeExtension(url) + "-p" + pageNumber
                    + "." + FilenameUtils.getExtension(url);
        }
        page.setUrl(url);
    }

    protected void updateTitle(Page page, Pager pager) {
        if(page.getTitle() != null){
            page.set("title_suffix", " - Part " + pager.getPageNumber());
        }else{
            page.set("title_suffix", " - Page " + pager.getPageNumber());
        }
    }
}
