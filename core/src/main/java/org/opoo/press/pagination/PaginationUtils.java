package org.opoo.press.pagination;

import org.opoo.press.Collection;
import org.opoo.press.Page;
import org.opoo.press.Pager;
import org.opoo.press.Site;
import org.opoo.press.impl.SimplePage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 */
public class PaginationUtils {

    public static int calculateTotalPages(int totalItems, int pageSize) {
        return (int) Math.ceil((double) totalItems / (double) pageSize);
    }


    public static List<Page> paginate(Site site, Page page, List<?> items, int pageSize) {
        //only new pages, exclude first page
        List<Page> newPages = new ArrayList<Page>();
        int totalItems = items.size();
        int totalPages = calculateTotalPages(totalItems, pageSize);

        Page[] pages = new Page[totalPages];
        Pager[] pagers = new Pager[totalPages];
        for (int i = 0; i < totalPages; i++) {
            int pageNumber = i + 1;
            int fromIndex = i * pageSize;
            int toIndex = fromIndex + pageSize;
            if (toIndex > totalItems) {
                toIndex = totalItems;
            }
            List<?> pageItems = items.subList(fromIndex, toIndex);

            Pager pager = new Pager(pageNumber, totalPages, totalItems, pageSize, pageItems);
            if (pageNumber > 1) {
                SimplePage newPage = /*(page.getSource() != null)
                        ? new PageImpl(site, page.getSource(), pager)
                        : */new SimplePage(site, page, pager);

                pages[i] = newPage;
                newPages.add(newPage);

                site.getFactory().getPaginationUpdater().apply(newPage);
            } else {
                page.setPager(pager);
                pages[i] = page;
            }
            pagers[i] = pager;
        }

        //set next and previous
        int maxIndex = totalPages - 1;
        for (int i = 0; i < totalPages; i++) {
            if (i > 0) {
                pagers[i].setPrevious(pages[i - 1]);
            }
            if (i < maxIndex) {
                pagers[i].setNext(pages[i + 1]);
            }
        }

        return newPages;
    }

    public static Map<String, ?> getPagination(Page page) {
        Map<String, ?> pagination = page.get("pagination");
        if (pagination == null) {
            Collection collection = page.get("collection");
            if (collection != null) {
                pagination = collection.getConfig().get("pagination");
            }
        }
        return pagination;
    }
}
