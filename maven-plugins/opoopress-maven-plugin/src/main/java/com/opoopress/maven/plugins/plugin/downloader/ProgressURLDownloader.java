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
package com.opoopress.maven.plugins.plugin.downloader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Alex Lin
 */
public class ProgressURLDownloader implements URLDownloader {

    private static final int PROGRESS_CHUNK = 20000;
    private static final int BUFFER_SIZE = 10000;
    private String applicationName = "OpooPress-downloader";
    private String applicationVersion;
    private boolean quiet;
    private boolean useCache = true;
    private boolean useGzip;
    private boolean keepLastModified;
    private boolean checkContentLength;

    public ProgressURLDownloader(){
        configureProxyAuthentication();
    }

    private void configureProxyAuthentication() {
        if (System.getProperty("http.proxyUser") != null) {
            Authenticator.setDefault(new SystemPropertiesProxyAuthenticator());
        }
    }

    public ProgressURLDownloader setQuiet(boolean quiet){
        this.quiet = quiet;
        return this;
    }

    public ProgressURLDownloader setApplication(String applicationName, String applicationVersion){
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        return this;
    }

    public ProgressURLDownloader setKeepLastModified(boolean keepLastModified) {
        this.keepLastModified = keepLastModified;
        return this;
    }

    public ProgressURLDownloader setCheckContentLength(boolean checkContentLength) {
        this.checkContentLength = checkContentLength;
        return this;
    }

    public ProgressURLDownloader setUseCache(boolean useCache) {
        this.useCache = useCache;
        return this;
    }

    public ProgressURLDownloader setUseGzip(boolean useGzip) {
        this.useGzip = useGzip;
        return this;
    }

    @Override
    public void download(URL address, File destination) throws IOException {
        destination.getParentFile().mkdirs();
        downloadInternal(address, destination);
    }

    private void downloadInternal(URL url, File destination) throws IOException {
        OutputStream out = null;
        URLConnection conn;
        InputStream in = null;
        try {
            //URL url = address.toURL();
            conn = url.openConnection();

            //user agent
            final String userAgentValue = calculateUserAgent();
            conn.setRequestProperty("User-Agent", userAgentValue);

            //do not set gzip header if download zip file
            if (useGzip) {
                conn.setRequestProperty("Accept-Encoding", "gzip");
            }

            if (!useCache) {
                conn.setRequestProperty("Pragma", "no-cache");
            }

            in = conn.getInputStream();
            out = new BufferedOutputStream(new FileOutputStream(destination));

            copy(in, out);

            if(checkContentLength) {
                long contentLength = conn.getContentLengthLong();
                if (contentLength > 0 && contentLength != destination.length()) {
                    throw new IllegalArgumentException("File length mismatch. expected: "
                            + contentLength + ", actual: " + destination.length());
                }
            }

            if(keepLastModified) {
                long lastModified = conn.getLastModified();
                if (lastModified > 0) {
                    destination.setLastModified(lastModified);
                }
            }
        } finally {
            logMessage("");
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }


    private void copy(InputStream in, OutputStream out) throws IOException{
        //Simplest way, no progress
        //org.apache.commons.io.IOUtils.copy(in, out);

        byte[] buffer = new byte[BUFFER_SIZE];
        int numRead;
        long progressCounter = 0;
        while ((numRead = in.read(buffer)) != -1) {
            if (Thread.currentThread().isInterrupted()) {
                System.out.print("interrupted");
                throw new IOException("Download was interrupted.");
            }
            progressCounter += numRead;
            if (progressCounter / PROGRESS_CHUNK > 0) {
                logAppend(".");
                progressCounter = progressCounter - PROGRESS_CHUNK;
            }
            out.write(buffer, 0, numRead);
        }
    }

    private String calculateUserAgent() {
        if(applicationVersion == null){
            applicationVersion = detectApplicationVersion();
        }

        String javaVendor = System.getProperty("java.vendor");
        String javaVersion = System.getProperty("java.version");
        String javaVendorVersion = System.getProperty("java.vm.version");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        return String.format("%s/%s (%s;%s;%s) (%s;%s;%s)", applicationName, applicationVersion, osName, osVersion, osArch, javaVendor, javaVersion, javaVendorVersion);
    }

    private String detectApplicationVersion() {
        String version = "1.2";
        try {
            version = ProgressURLDownloader.class.getPackage().getSpecificationVersion();
        } catch (Exception e) {
        }
        return version;
    }

    private static class SystemPropertiesProxyAuthenticator extends Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(
                    System.getProperty("http.proxyUser"), System.getProperty(
                    "http.proxyPassword", "").toCharArray());
        }
    }

    private void logAppend(CharSequence str) {
        if (!quiet) {
            System.out.append(str);
        }
    }

    private void logMessage(CharSequence message) {
        if (!quiet) {
            System.out.println(message);
        }
    }
}
