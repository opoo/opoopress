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
package org.opoo.press.impl;

import org.apache.commons.io.FileUtils;
import org.opoo.press.FileOrigin;
import org.opoo.press.Origin;
import org.opoo.press.Site;
import org.opoo.press.StaticFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Alex Lin
 */
public class StaticFileImpl implements StaticFile {
    private static final Logger log = LoggerFactory.getLogger(StaticFileImpl.class);

    private Site site;
    private Origin origin;

    public StaticFileImpl(Site site, Origin origin) {
        this.site = site;
        this.origin = origin;
    }

    /**
     * @return the site
     */
    public Site getSite() {
        return site;
    }

    /**
     * @param site the site to set
     */
    public void setSite(Site site) {
        this.site = site;
    }

    /**
     * @return the sourceEntry
     */
    public Origin getOrigin() {
        return origin;
    }

    /**
     * @param origin the sourceEntry to set
     */
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    /* (non-Javadoc)
     * @see org.opoo.press.StaticFile#getOutputFile(java.io.File)
     */
    @Override
    public File getOutputFile(File dest) {
        String file = origin.getPath() + "/" + origin.getName();
        return new File(dest, file);
    }

    /* (non-Javadoc)
     * @see org.opoo.press.StaticFile#write(java.io.File)
     */
    @Override
    public void write(File dest) {
        if (origin instanceof FileOrigin) {
            File target = getOutputFile(dest);

            FileOrigin fo = (FileOrigin) origin;
            if (target.exists() && target.length() == fo.getLength()
                    && target.lastModified() >= fo.getLastModified()) {
                //log.debug("Target file is newer than source file, skip copying.");
                return;
            }
            try {
                File parentFile = target.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }

                log.debug("Copying static file to " + target);
                FileUtils.copyFile(fo.getFile(), target);
            } catch (IOException e) {
                log.error("Copying static file error: " + target, e);
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Origin not support yet: " + origin);
        }
    }
}
