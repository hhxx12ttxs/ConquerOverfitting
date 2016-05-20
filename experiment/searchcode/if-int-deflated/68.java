/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)ZipFileUtility.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

/**
 * 
 */
package com.sun.jbi.cam.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Graj
 * 
 */
public class ZipFileUtility {
    
    public static int BUFFER_SIZE = 10240;

    /**
     * Reads a compressed archive and decompresses the specified entry
     * 
     * @param archiveFileName
     * @param entryName
     * @param fileNamePath
     * @return
     */
    public static File readFileFromArchive(String archiveFileName,
            String entryName, String fileNamePath) {
        File file = null;
        ZipEntry entry = null;
        ZipInputStream zipInputStream = null;
        OutputStream outputStream = null;
        try {
            // Open the ZIP file
            zipInputStream = new ZipInputStream(new FileInputStream(
                    archiveFileName));
            do {
                entry = zipInputStream.getNextEntry();
                if (entry != null) {
                    // Get the entry name
                    String zipEntryName = entry.getName();
                    if (zipEntryName.equals(entryName) == true) {
                        break;
                    }
                }
            } while (entry != null);

            // Open the output file
            file = new File(fileNamePath);
            File parent = file.getParentFile();
            if (parent.exists() == false) {
                parent.mkdirs();
            }
            outputStream = new FileOutputStream(file);

            // Transfer bytes from the ZIP file to the output file
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            // Close the streams
            outputStream.close();
            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }

        return file;
    }

    /**
     * Lists the Contents of the specified archive file
     * 
     * @param archiveFileName
     * @return
     */
    public static List<ZipEntry> listFileContents(String archiveFileName) {
        List<ZipEntry> zipContentsList = new ArrayList<ZipEntry>();
        ZipEntry entry = null;
        try {
            // Open the ZIP file
            ZipFile zipFile = new ZipFile(archiveFileName);

            // Enumerate each entry
            for (Enumeration entries = zipFile.entries(); entries
                    .hasMoreElements();) {
                entry = (ZipEntry) entries.nextElement();
                // add entry
                zipContentsList.add(entry);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
        return zipContentsList;
    }

    /**
     * Creates an archive file
     * 
     * @param archiveFile
     * @param fileToBeZipped
     */
    public static void createArchive(File archiveFile,
            List<File> filesToBeZipped) {
        try {
            byte buffer[] = new byte[BUFFER_SIZE];
            // Open archive file
            FileOutputStream fileOutputStream = new FileOutputStream(
                    archiveFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(
                    fileOutputStream);

            File file = null;
            for (Iterator<File> iterator = filesToBeZipped.iterator(); iterator
                    .hasNext() == true;) {
                file = iterator.next();
                if ((file == null || (file.exists() == false) || (file
                        .isDirectory() == true))) {
                    continue; // Just in case...
                }
                System.out.println("Adding " + file.getName());

                // Add archive entry
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipEntry.setTime(file.lastModified());
                zipOutputStream.putNextEntry(zipEntry);

                // Read input & write to output
                FileInputStream fileInputStream = new FileInputStream(file);
                while (true) {
                    int nRead = fileInputStream.read(buffer, 0, buffer.length);
                    if (nRead <= 0) {
                        break;
                    }
                    zipOutputStream.write(buffer, 0, nRead);
                }
                fileInputStream.close();
            }
            zipOutputStream.close();
            fileOutputStream.close();
            System.out.println("Adding completed OK");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
            return;
        }
    }

    public static void main(String... args) {
        final String FILE_PATH = "/tmp";
        final String ARCHIVE_FILE_NAME_PATH = FILE_PATH + File.separator
                + "LoanProcessingCompositeApp.zip";
        final String ENTRY_NAME = "META-INF/jbi.xml";
        final String FILE_NAME_PATH = FILE_PATH + File.separator + ENTRY_NAME;

        // Testing listZipFileContents
        List<ZipEntry> entries = ZipFileUtility
                .listFileContents(ARCHIVE_FILE_NAME_PATH);
        for (ZipEntry entry : entries) {
            System.out.println("Name is: " + entry.getName());
            System.out.println("Compressed Size is: "
                    + entry.getCompressedSize());
            System.out.println("CRC is: " + entry.getCrc());
            if (ZipEntry.STORED == entry.getMethod()) {
                System.out.println("Compression method is STORED");
            }
            if (ZipEntry.DEFLATED == entry.getMethod()) {
                System.out.println("Compression method is DEFLATED");
            }
            System.out.println("Size is: " + entry.getSize());
            System.out.println("Time is: " + entry.getTime());
            System.out.println("Comment is: " + entry.getComment());
            System.out.println("/////////////////////");
        }

        // Testing readFileFromArchive
        File file = ZipFileUtility.readFileFromArchive(ARCHIVE_FILE_NAME_PATH,
                ENTRY_NAME, FILE_NAME_PATH);
        System.out.println("File is: " + file.getAbsolutePath());

    }
}

