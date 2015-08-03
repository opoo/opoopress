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
package org.opoo.press.source;

import org.opoo.press.FileOrigin;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Alex Lin
 */
public class FileOriginImpl implements FileOrigin, Serializable {
    private File file;
    private String path = "";
    private File sourceDirectory;
    private long lastModified;
    private long length;

    public FileOriginImpl(File file, File sourceDirectory) {
        this(file, sourceDirectory, "");
    }

    public FileOriginImpl(File file, File sourceDirectory, FileOriginImpl parent) {
        this(file, sourceDirectory, (parent == null) ? "" : parent.getPath() + "/" + parent.getName());
    }

    public FileOriginImpl(File file, File sourceDirectory, String path) {
        if (!file.isFile() || !file.exists()) {
            throw new IllegalArgumentException("It's not a file or not exists: " + file);
        }

        this.file = file;
        this.sourceDirectory = sourceDirectory;
        this.path = (path == null) ? "" : path;
        this.lastModified = file.lastModified();
        this.length = file.length();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public File getSourceDirectory() {
        return sourceDirectory;
    }

    @Override
    public String getPath() {
        return path;
    }

    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.writeObject(file);
        out.writeObject(sourceDirectory);
        out.writeObject(path);
        out.writeLong(lastModified);
        out.writeLong(length);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        this.file = (File) in.readObject();
        this.sourceDirectory = (File) in.readObject();
        this.path = (String) in.readObject();
        this.lastModified = in.readLong();
        this.length = in.readLong();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileOriginImpl) {
            FileOriginImpl o = (FileOriginImpl) obj;
            return file.equals(o.file)
                    && sourceDirectory.equals(o.sourceDirectory)
                    && path.equals(o.path)
                    && lastModified == o.lastModified
                    && length == o.length;
        }
        return false;
    }

    public String toString(){
        return "[FileOriginImpl] " + file;
    }
}
