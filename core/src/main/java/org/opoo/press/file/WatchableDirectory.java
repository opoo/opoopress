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
package org.opoo.press.file;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Alex Lin
 *
 */
public class WatchableDirectory extends FileAlterationListenerAdaptor implements Watchable {
	private final File directory;
	private final FileFilter fileFilter;
	private Result result;
	private FileAlterationObserver fileAlterationObserver;
	
    /**
     * Construct an observer for the specified directory.
     *
     * @param directoryName the name of the directory to observe
     */
    public WatchableDirectory(String directoryName) {
        this(new File(directoryName));
    }

    /**
     * Construct an observer for the specified directory and file filter.
     *
     * @param directoryName the name of the directory to observe
     * @param fileFilter The file filter or null if none
     */
    public WatchableDirectory(String directoryName, FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

    /**
     * Construct an observer for the specified directory.
     *
     * @param directory the directory to observe
     */
    public WatchableDirectory(File directory) {
        this(directory, (FileFilter)null);
    }

    /**
     * Construct an observer for the specified directory and file filter.
     *
     * @param directory the directory to observe
     * @param fileFilter The file filter or null if none
     */
    public WatchableDirectory(File directory, FileFilter fileFilter) {
        this.directory = directory;
        this.fileFilter = fileFilter;
    }

	/* (non-Javadoc)
	 * @see org.opoo.press.monitor.Observer#initialize()
	 */
	@Override
	public void initialize() {
		this.fileAlterationObserver = new FileAlterationObserver(directory, fileFilter);
		this.fileAlterationObserver.addListener(this);
		try {
			this.fileAlterationObserver.initialize();
		} catch (Exception e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.monitor.Observer#check()
	 */
	@Override
	public Result check() {
		result = Result.newResult();
		fileAlterationObserver.checkAndNotify();
		return result;
	}

	/* (non-Javadoc)
	 * @see org.opoo.press.monitor.Observer#destroy()
	 */
	@Override
	public void destroy() {
		result = null;
		try {
			fileAlterationObserver.destroy();
		} catch (Exception e) {
		}
	}

	@Override
	public void onFileCreate(File file) {
		result.addCreatedFile(file);
	}

	@Override
	public void onFileChange(File file) {
		result.addUpdatedFile(file);
	}

	@Override
	public void onFileDelete(File file) {
		result.addDeletedFile(file);
	}
}
