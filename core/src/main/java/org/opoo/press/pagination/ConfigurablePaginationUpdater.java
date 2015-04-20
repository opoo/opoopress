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
import org.opoo.press.renderer.AbstractFreeMarkerRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class ConfigurablePaginationUpdater extends DefaultPaginationUpdater {
    @Override
    protected void updateInternal(Page page, Pager pager) {
        Map<String, ?> pagination = PaginationUtils.getPagination(page);

        //pagination->permalink: ${url}page/${pageNumber}                               //url ends with '/'
        //pagination->permalink: ${urlWithoutExtension}-page-${pageNumber}.html         //url ends with '.html'
        //pagination->permalink: ${urlWithoutExtension}-page-${pageNumber}.${urlExtension} //url ends with other ext
        String urlFormat = (pagination != null) ? (String) pagination.get("permalink") : null;
        if(urlFormat != null){
            String url = page.getUrl();
            String urlWithoutExtension = FilenameUtils.removeExtension(url);
            String urlExtension = FilenameUtils.getExtension(url);

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("url", url);
            map.put("urlWithoutExtension", urlWithoutExtension);
            map.put("urlExtension", urlExtension);
            map.put("pageSize", pager.getPageSize());
            map.put("pageNumber", pager.getPageNumber());
            map.put("totalPages", pager.getTotalPages());
            map.put("totalItems", pager.getTotalItems());

            page.setUrl(AbstractFreeMarkerRenderer.process(urlFormat, map));
        }else{
            super.updateUrl(page, pager);
        }

        //pagination->title_suffix_format: ' - Page ${pageNumber} of ${totalPages}'
        //pagination->title_suffix_format: ' - Part ${pageNumber}'
        String titleSuffixFormat = (pagination != null) ? (String) pagination.get("title_suffix_format") : null;
        if(titleSuffixFormat != null){
            page.set("title_suffix", AbstractFreeMarkerRenderer.process(titleSuffixFormat, pager));
        }else{
            super.updateTitle(page, pager);
        }
    }

//    private Map<String,?> getPagination(Page page){
//        Map<String,?> pagination = (Map<String, ?>) page.get("pagination");
//        if(pagination == null){
//            Collection collection = (Collection) page.get("collection");
//            if(collection != null){
//                pagination = collection.getConfiguration().get("pagination");
//            }
//        }
//        return pagination;
//    }

//    private String resolve(String pattern, Object rootMap){
//        try {
//            Configuration configuration = new Configuration();
//            Template template = new Template("pagination_temp", new StringReader(pattern), configuration, "UTF-8");
//            StringWriter writer = new StringWriter();
//            template.process(rootMap, writer);
//            writer.flush();
//            return writer.toString();
//        } catch (IOException e) {
//            throw new RuntimeException("Resolve format failed: " + pattern, e);
//        } catch (TemplateException e) {
//            throw new RuntimeException("Resolve format failed: " + pattern, e);
//        }
//    }
}
