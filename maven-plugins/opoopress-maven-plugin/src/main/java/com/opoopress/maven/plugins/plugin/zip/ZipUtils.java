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
package com.opoopress.maven.plugins.plugin.zip;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Alex Lin
 */
public class ZipUtils {

    public static void unzipFileToDirectory(File file, File destDir) throws IOException {
        unzipFileToDirectory(file, destDir, false);
    }

    public static void unzipFileToDirectory(File file, File destDir, boolean keepTimestamp) throws IOException {
        // create output directory if it doesn't exist
        if(!destDir.exists()) destDir.mkdirs();

        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if(entry.isDirectory()){
                new File(destDir, entry.getName()).mkdirs();
                continue;
            }

            InputStream inputStream = zipFile.getInputStream(entry);
            File destFile = new File(destDir, entry.getName());

            System.out.println("Unzipping to " + destFile.getAbsolutePath());
            FileUtils.copyInputStreamToFile(inputStream, destFile);
            IOUtils.closeQuietly(inputStream);

            if(keepTimestamp) {
                long time = entry.getTime();
                if (time > 0) {
                    destFile.setLastModified(time);
                }
            }
        }

        zipFile.close();
    }
}
