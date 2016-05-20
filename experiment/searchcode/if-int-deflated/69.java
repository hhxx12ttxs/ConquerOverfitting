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
 * @(#)JarFileUtility.java 
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @author Graj
 * 
 */
public class JarFileUtility {

	public static int BUFFER_SIZE = 10240;

	/**
	 * Reads a compressed archive and decompresses the specified entry
	 * 
	 * @param archiveFileName
	 * @param entryName
	 * @param fileNamePath
	 * @return
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static File readFileFromArchive(String archiveFileName,
			String entryName, String fileNamePath) throws FileNotFoundException, IOException {
		File file = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;

			JarFile jarFile = new JarFile(archiveFileName);
			JarEntry entry = jarFile.getJarEntry(entryName);
			inputStream = jarFile.getInputStream(entry);

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
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}

			outputStream.flush();
			// Close the streams
			outputStream.close();
			inputStream.close();

		return file;
	}

	/**
	 * Lists the Contents of the specified archive file
	 * 
	 * @param archiveFileName
	 * @return
	 * 
	 * @throws IOException
	 */
	public static List<JarEntry> listFileContents(String archiveFileName) throws IOException {
		List<JarEntry> jarContentsList = new ArrayList<JarEntry>();
		JarEntry entry = null;
			// Open the JAR file
			JarFile jarfile = new JarFile(archiveFileName);

			for (Enumeration<JarEntry> entries = jarfile.entries(); entries
					.hasMoreElements();) {
				entry = entries.nextElement();
				jarContentsList.add(entry);
			}
		return jarContentsList;
	}

	/**
	 * Creates an archive file
	 * 
	 * @param archiveFile
	 * @param filesToBeJared
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void createArchive(File archiveFile, List<File> filesToBeJared)
			throws FileNotFoundException, IOException {
		FileOutputStream fileOutputStream = null;
		JarOutputStream jarOutputStream = null;
		byte buffer[] = new byte[BUFFER_SIZE];
		// Open archive file
		fileOutputStream = new FileOutputStream(archiveFile);
		jarOutputStream = new JarOutputStream(fileOutputStream, new Manifest());

		File file = null;
		for (Iterator<File> iterator = filesToBeJared.iterator(); iterator
				.hasNext() == true;) {
			file = iterator.next();
			if ((file == null || (file.exists() == false) || (file
					.isDirectory() == true))) {
				continue; // Just in case...
			}
			System.out.println("Adding " + file.getName());
			// Add archive entry
			JarEntry jarEntry = null;
			if (file.getAbsolutePath().contains("meta-inf") == true) {
				jarEntry = new JarEntry("meta-inf/" + file.getName());
			} else if (file.getAbsolutePath().contains("META-INF") == true) {
				jarEntry = new JarEntry("META-INF/" + file.getName());
			} else {
				jarEntry = new JarEntry(file.getName());
			}
			jarEntry.setTime(file.lastModified());
			jarOutputStream.putNextEntry(jarEntry);

			// Write file to archive
			FileInputStream fileInoutStream = new FileInputStream(file);
			while (true) {
				int nRead = fileInoutStream.read(buffer, 0, buffer.length);
				if (nRead <= 0) {
					break;
				}
				jarOutputStream.write(buffer, 0, nRead);
			}
			fileInoutStream.close();
		}
		if (jarOutputStream != null) {
			jarOutputStream.flush();
			jarOutputStream.close();
		}
		if (fileOutputStream != null) {
			fileOutputStream.flush();
			fileOutputStream.close();
		}
		System.out.println("Adding completed OK");
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		final String FILE_PATH = "C:/TEMP/SynchronousSample/SynchronousSampleApplication/build";
		final String ARCHIVE_FILE_NAME_PATH = FILE_PATH + File.separator
				+ "SynchronousSample.jar";
		final String ENTRY_NAME = "META-INF/jbi.xml";
		final String FILE_NAME_PATH = FILE_PATH + File.separator + ENTRY_NAME;

		// Testing listJarFileContents
		List<JarEntry> entries = null;
		File file = null;
		try {
			entries = JarFileUtility
					.listFileContents(ARCHIVE_FILE_NAME_PATH);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (JarEntry entry : entries) {
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
			try {
				Attributes attributes = entry.getAttributes();
				System.out.println("attributes: " + attributes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Certificate[] certificates = entry.getCertificates();
			if (certificates != null) {
				for (int index = 0; index < certificates.length; index++) {
					System.out.println("Certificate[" + index + "] = "
							+ certificates[index]);
				}
			}
			CodeSigner[] codeSigners = entry.getCodeSigners();
			if (codeSigners != null) {
				for (int index = 0; index < codeSigners.length; index++) {
					System.out.println("CodeSigner[" + index + "] = "
							+ codeSigners[index]);
				}
			}

			System.out.println("/////////////////////");

			// Testing readFileFromArchive
			try {
				file = JarFileUtility.readFileFromArchive(
						ARCHIVE_FILE_NAME_PATH, ENTRY_NAME, FILE_NAME_PATH);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("File is: " + file.getAbsolutePath());

		}

	}
}

