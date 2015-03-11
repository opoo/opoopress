/*
 * Copyright 2013 Alex Lin.
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
public class Pager {
    private List<? extends Base> posts;
    private int totalPosts;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
    private Page next;
    private Page previous;

    /**
     * @param pageNumber
     * @param totalPages
     * @param totalPosts
     * @param pageSize
     * @param pagePosts
     */
    public Pager(int pageNumber, int totalPages, int totalPosts, int pageSize, List<? extends Base> pagePosts) {
        this.pageNumber = pageNumber;
        this.totalPages = totalPages;
        this.totalPosts = totalPosts;
        this.pageSize = pageSize;
        this.posts = pagePosts;
    }

    /**
     * @return the next
     */
    public Page getNext() {
        return next;
    }

    /**
     * @param next the next to set
     */
    public void setNext(Page next) {
        this.next = next;
    }

    /**
     * @return the previous
     */
    public Page getPrevious() {
        return previous;
    }

    /**
     * @param previous the previous to set
     */
    public void setPrevious(Page previous) {
        this.previous = previous;
    }

    /**
     * @return the posts
     */
    public List<? extends Base> getPosts() {
        return posts;
    }

    /**
     * @return the totalPosts
     */
    public int getTotalPosts() {
        return totalPosts;
    }

    /**
     * @return the totalPages
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }
}
