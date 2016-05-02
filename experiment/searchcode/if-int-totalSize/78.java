/*
 * Copyright 2011 DeepDiff Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package deepdiff.scope;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import deepdiff.core.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * An implementation of {@link DiffScope} that compares two Zip files. These Zip files do not need
 * to be physical files; any {@link InputStream} can be used. Thus, it is possible to process Zip
 * files within Zip files.
 */
public class SortedZipDiffScope implements DiffScope {
    private static final Logger log = Logger.getLogger(SortedZipDiffScope.class);

    private SortedMap<String, CachedZipEntry> leftEntriesByPath;
    private SortedMap<String, CachedZipEntry> rightEntriesByPath;

    private Map<String, Map<String, CachedZipEntry>> leftEntriesBySha;
    private Map<String, Map<String, CachedZipEntry>> rightEntriesBySha;

    private InputStream isLeft;
    private InputStream isRight;

    private String path;

    /**
     * Initializes a new ZipDiffScope object.
     * 
     * @param path the scoped path to the Zip files
     * @param isLeft the {@link InputStream} for the Zip file on the left
     * @param isRight the {@link InputStream} for the Zip file on the right
     */
    public SortedZipDiffScope(String path, InputStream isLeft, InputStream isRight) {
        this.path = path;
        this.isLeft = isLeft;
        this.isRight = isRight;
        leftEntriesByPath = new TreeMap<String, CachedZipEntry>();
        rightEntriesByPath = new TreeMap<String, CachedZipEntry>();
        leftEntriesBySha = new HashMap<String, Map<String, CachedZipEntry>>();
        rightEntriesBySha = new HashMap<String, Map<String, CachedZipEntry>>();
    }

    /**
     * Scans the Zip archive for DiffUnits and processes them
     * 
     * @param unitProcessor the unit processor to process the DiffUnits found in the scan
     * @param pointProcessor the point processor to pass to the unit processor when processing units
     */
    public void scan(DiffUnitProcessor unitProcessor, DiffPointProcessor pointProcessor) {
        try {
            log.debug("Starting to scan scope " + path);

            loadEntries(isLeft, leftEntriesByPath, leftEntriesBySha);
            loadEntries(isRight, rightEntriesByPath, rightEntriesBySha);

            SortedSet<String> pathSet = new TreeSet<String>();
            pathSet.addAll(leftEntriesByPath.keySet());
            pathSet.addAll(rightEntriesByPath.keySet());

            Set<String> shaSet = new HashSet<String>();
            shaSet.addAll(leftEntriesBySha.keySet());
            shaSet.addAll(rightEntriesBySha.keySet());

            for (String sha : shaSet) {
                Map<String, CachedZipEntry> leftEntries = leftEntriesBySha.get(sha);
                Map<String, CachedZipEntry> rightEntries = rightEntriesBySha.get(sha);
                if (leftEntries != null && rightEntries != null) {
                    for (String leftPath : leftEntries.keySet()) {
                        if (!rightEntries.containsKey(leftPath)) {
                            for (String rightPath : rightEntries.keySet()) {
                                String leftDirName = StringUtils.substringBeforeLast(leftPath, "/");
                                String rightDirName = StringUtils.substringBeforeLast(rightPath, "/");
                                if ((!StringUtils.contains(leftPath, "/") && !StringUtils.contains(rightPath, "/"))
                                    || StringUtils.equals(leftDirName, rightDirName))
                                {
                                    CachedZipEntry leftEntry = leftEntries.get(leftPath);
                                    CachedZipEntry rightEntry = rightEntries.get(rightPath);
                                    CachedZipDiffUnit diffUnit = new CachedZipDiffUnit(this, leftEntry, rightEntry);
                                    DiffPoint diffPoint = new DiffPoint(diffUnit, "Renamed in right to " + rightPath);
                                    pointProcessor.processDiffPoint(diffPoint);
                                    pathSet.remove(leftEntry.getName());
                                    pathSet.remove(rightEntry.getName());
                                    leftEntry.cleanup();
                                    rightEntry.cleanup();
                                }
                            }
                        } else {
                            // The entry is identical between the left and right, because the entry exists on both the
                            // left and right sides with the same path and SHA-1 hash.  Remove the path from the path
                            // set to avoid doing an unnecessary comparison.
                            pathSet.remove(leftPath);
                            CachedZipEntry leftEntry = leftEntries.get(leftPath);
                            CachedZipEntry rightEntry = rightEntries.get(leftPath);
                            leftEntry.cleanup();
                            rightEntry.cleanup();
                        }
                    }
                }
            }

            for (String path : pathSet) {
                CachedZipEntry leftEntry = leftEntriesByPath.get(path);
                CachedZipEntry rightEntry = rightEntriesByPath.get(path);
                CachedZipDiffUnit diffUnit = new CachedZipDiffUnit(this, leftEntry, rightEntry);
                unitProcessor.processDiffUnit(diffUnit, pointProcessor);
                if (leftEntry != null) {
                    leftEntry.cleanup();
                }
                if (rightEntry != null) {
                    rightEntry.cleanup();
                }
            }
            leftEntriesByPath.clear();
            rightEntriesByPath.clear();
            leftEntriesBySha.clear();
            rightEntriesBySha.clear();
            log.debug("Completed scan of scope " + path);
        } finally {
            if (isLeft != null) {
                try {
                    isLeft.close();
                } catch (Exception ex) {
                    // Ignore exception on close
                }
            }
            if (isRight != null) {
                try {
                    isRight.close();
                } catch (Exception ex) {
                    // Ignore exception on close
                }
            }
        }
    }

    /**
     * Returns the scoped path to the Zip files
     * 
     * @return the scoped path to the Zip files
     */
    public String getPath() {
        return path;
    }

    private void loadEntries(InputStream is, Map<String, CachedZipEntry> entriesByPath, Map<String, Map<String, CachedZipEntry>> entriesBySha) {
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry entry = null;
        boolean validEntry = false;
        try {
            while (!validEntry || entry != null) {
                try {
                    entry = zis.getNextEntry();
                    validEntry = true;
                    if (entry != null) {
                        String path = entry.getName();
                        CachedZipEntry cachedEntry = new CachedZipEntry(entry, zis);
                        entriesByPath.put(path, cachedEntry);
                        String sha = cachedEntry.getSha();
                        if (sha != null) {
                            if (!entriesBySha.containsKey(sha)) {
                                Map<String, CachedZipEntry> map = new TreeMap<String, CachedZipEntry>();
                                entriesBySha.put(sha, map);
                            }
                            entriesBySha.get(sha).put(path, cachedEntry);
                        }
                    }
                } catch (ZipException ze) {
                    if ("encrypted ZIP entry not supported".equals(ze.getMessage())) {
                        log.warn(path
                                + " contains encrypted ZIP entries; comparison of this file not supported");
                    } else {
                        log.error("Failure scanning scope: " + path, ze);
                    }
                } catch (IOException ioe) {
                    validEntry = false;
                    log.error("Failure scanning scope: " + path, ioe);
                }
            }
        } finally {
            try {
                zis.close();
            } catch (Exception ex) {
                // Ignore exception on close
            }
        }
    }

    private static class CachedZipEntry extends ZipEntry {
        private String name;
        private boolean directory;
        private File tmpFile;
        private byte[] data;
        int length;
        private String sha;

        public CachedZipEntry(ZipEntry ze, ZipInputStream zis) throws IOException {
            super(ze.getName());
            this.name = ze.getName();
            this.directory = ze.isDirectory();
            if (!this.directory) {
                readAll(zis, ze);

                if (tmpFile != null) {
                    InputStream is = new FileInputStream(tmpFile);
                    sha = DigestUtils.shaHex(is);
                    is.close();
                } else {
                    sha = DigestUtils.shaHex(data);
                }
            }
        }

        public void cleanup() {
            if (tmpFile != null) {
                if (!tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
            } else {
                data = null;
            }
        }

        public InputStream getStream() throws IOException {
            if (tmpFile != null) {
                return new FileInputStream(tmpFile);
            } else {
                return new ByteArrayInputStream(data, 0, length);
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isDirectory() {
            return directory;
        }

        public String getSha() {
            return sha;
        }

        private void readAll(ZipInputStream zis, ZipEntry entry) throws IOException {
            final int maxMemSize = 100000;
            long totalSize = entry.getSize();
            if (totalSize >= maxMemSize || totalSize < 0) {
                tmpFile = File.createTempFile("ZipContent", null);
                FileOutputStream fos = new FileOutputStream(tmpFile);
                byte[] buf = new byte[1024];
                int size = zis.read(buf);
                while (size != -1) {
                    fos.write(buf, 0, size);
                    size = zis.read(buf);
                }
                fos.close();
            } else if (totalSize == 0) {
                length = 0;
                data = new byte[length];
            } else {
                length = (int) totalSize;
                // Allow an extra byte in the buffer so we can request an extra
                // byte at the end, to get confirmation that there's no more data
                // left
                data = new byte[length + 1];
                int off = 0;
                int size = zis.read(data, off, data.length - off);
                while (size != -1) {
                    off += size;
                    size = zis.read(data, off, data.length - off);
                }
            }
        }
    }

    public static class CachedZipDiffUnit implements DiffUnit {
        private CachedZipEntry e1;
        private CachedZipEntry e2;
        private SortedZipDiffScope scope;

        CachedZipDiffUnit(SortedZipDiffScope scope, CachedZipEntry e1, CachedZipEntry e2) {
            this.scope = scope;
            this.e1 = e1;
            this.e2 = e2;
        }

        /**
         * Returns whether the left entry exists
         * 
         * @return whether the left entry exists
         */
        public boolean leftExists() {
            return e1 != null;
        }

        /**
         * Returns whether the right entry exists
         * 
         * @return whether the right entry exists
         */
        public boolean rightExists() {
            return e2 != null;
        }

        /**
         * Returns whether the left entry represents a directory
         * 
         * @return whether the left entry represents a directory
         */
        public boolean leftIsDir() {
            return leftExists() && e1.isDirectory();
        }

        /**
         * Returns whether the right entry represents a directory
         * 
         * @return whether the right entry represents a directory
         */
        public boolean rightIsDir() {
            return rightExists() && e2.isDirectory();
        }

        /**
         * Returns a stream to access the content of the left entry
         * 
         * @return a stream to access the content of the left entry
         * 
         * @throws IOException if there was an error creating the stream
         */
        public InputStream getLeftInputStream() throws IOException {
            return e1.getStream();
        }

        /**
         * Returns a stream to access the content of the right entry
         * 
         * @return a stream to access the content of the right entry
         * 
         * @throws IOException if there was an error creating the stream
         */
        public InputStream getRightInputStream() throws IOException {
            return e2.getStream();
        }

        /**
         * Returns the scoped path for the unit. This is the path of the Zip scope, followed by the
         * path of the entry within the Zip scope
         * 
         * @return the scoped path for the unit
         */
        public String getScopedPath() {
            String scopePath = scope.getPath();
            String relativePath = null;
            if (e1 != null) {
                relativePath = e1.getName();
            } else if (e2 != null) {
                relativePath = e2.getName();
            }
            return scopePath + "!" + relativePath;
        }
    }
}

