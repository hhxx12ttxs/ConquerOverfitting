/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
// -*- java -*-
// File: GIFImageInfoTest.java
//
// Created: Mon Jul  8 12:46:46 2002
//
// $Id: GIFImageInfoTest.java 1032 2009-05-14 04:37:52Z vic $
// $Name:  $
//


package ru.adv.test.util.image;

import java.io.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ru.adv.test.AbstractTest;
import static org.junit.Assert.*;


import ru.adv.util.image.GIFInfoReader;
import ru.adv.util.image.ImageInfo;
import ru.adv.util.image.ImageInfoReadException;

/**
 *
 * @version $Revision: 1.2 $
 */
public class GIFImageInfoTest extends AbstractTest {

	private GIFInfoReader reader;

	private static final String GOOD1 = "good1.gif";
	// 1 x 300
	public static final int[] goodGif1 = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00,
										  0x2c, 0x01, 0x80, 0x00, 0x01, 0xff, 0xff, 0xff,
										  0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00,
										  0x01, 0x00, 0x2c, 0x01, 0x00, 0x08, 0x1e, 0x00,
										  0x01, 0x08, 0x1c, 0x48, 0xb0, 0xa0, 0xc1, 0x83,
										  0x08, 0x13, 0x2a, 0x5c, 0xc8, 0xb0, 0xa1, 0xc3,
										  0x87, 0x10, 0x23, 0x4a, 0x9c, 0x48, 0xb1, 0xa2,
										  0xc5, 0x8b, 0x18, 0x03, 0xfe, 0x00, 0x3b};

	private static final String GOOD2 = "good2.gif";
	// 300 x 1
	private static final int[] goodGif2 = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x2c, 0x01,
										   0x01, 0x00, 0x80, 0x00, 0x01, 0xff, 0xff, 0xff,
										   0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00,
										   0x2c, 0x01, 0x01, 0x00, 0x00, 0x08, 0x1e, 0x00,
										   0x01, 0x08, 0x1c, 0x48, 0xb0, 0xa0, 0xc1, 0x83,
										   0x08, 0x13, 0x2a, 0x5c, 0xc8, 0xb0, 0xa1, 0xc3,
										   0x87, 0x10, 0x23, 0x4a, 0x9c, 0x48, 0xb1, 0xa2,
										   0xc5, 0x8b, 0x18, 0x03, 0xfe, 0x00, 0x3b};

	private static final String ZERO = "zero.gif";
	// 0 x 0
	private static final int[] zeroSizeGif = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x00, 0x00,
											  0x00, 0x00, 0x80, 0x00, 0x01, 0xff, 0xff, 0xff,
											  0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00,
											  0x2c, 0x01, 0x01, 0x00, 0x00, 0x08, 0x1e, 0x00,
											  0x01, 0x08, 0x1c, 0x48, 0xb0, 0xa0, 0xc1, 0x83,
											  0x08, 0x13, 0x2a, 0x5c, 0xc8, 0xb0, 0xa1, 0xc3,
											  0x87, 0x10, 0x23, 0x4a, 0x9c, 0x48, 0xb1, 0xa2,
											  0xc5, 0x8b, 0x18, 0x03, 0xfe, 0x00, 0x3b};

	private static final String BIG = "big.gif";
	// 0xffff x 0xffff
	private static final int[] bigSizeGif = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0xff, 0xff,
											 0xff, 0xff, 0x80, 0x00, 0x01, 0xff, 0xff, 0xff,
											 0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00,
											 0x2c, 0x01, 0x01, 0x00, 0x00, 0x08, 0x1e, 0x00,
											 0x01, 0x08, 0x1c, 0x48, 0xb0, 0xa0, 0xc1, 0x83,
											 0x08, 0x13, 0x2a, 0x5c, 0xc8, 0xb0, 0xa1, 0xc3,
											 0x87, 0x10, 0x23, 0x4a, 0x9c, 0x48, 0xb1, 0xa2,
											 0xc5, 0x8b, 0x18, 0x03, 0xfe, 0x00, 0x3b};
	// invalid headers
	private static final String BAD1 = "bad1.gif";
	private static final int[] badGif1 = {};

	private static final String BAD2 = "bad2.gif";
	private static final int[] badGif2 = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61};

	private static final String BAD3 = "bad3.gif";
	private static final int[] badGif3 = {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0xff, 0xff};

	private static final String BAD4 = "bad4.gif";
	private static final int[] badGif4 = {0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0x00, 0x01,
										  0x00, 0x01, 0x80, 0x00, 0x01, 0xff, 0xff, 0xff,
										  0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00,
										  0x2c, 0x01, 0x01, 0x00, 0x00, 0x08, 0x1e, 0x00,
										  0x01, 0x08, 0x1c, 0x48, 0xb0, 0xa0, 0xc1, 0x83,
										  0x08, 0x13, 0x2a, 0x5c, 0xc8, 0xb0, 0xa1, 0xc3,
										  0x87, 0x10, 0x23, 0x4a, 0x9c, 0x48, 0xb1, 0xa2,
										  0xc5, 0x8b, 0x18, 0x03, 0xfe, 0x00, 0x3b};

	private File tmpdir = new File("tmp");


	@Before
	public void setUp() {
		reader = new GIFInfoReader();
		tmpdir.mkdirs();
		writeFile(GOOD1, goodGif1);
		writeFile(GOOD2, goodGif2);
		writeFile(ZERO, zeroSizeGif);
		writeFile(BIG, bigSizeGif);
		writeFile(BAD1, badGif1);
		writeFile(BAD2, badGif2);
		writeFile(BAD3, badGif3);
		writeFile(BAD4, badGif4);
	}

	@After
	public void tearDown() {
		deleteFile(GOOD1);
		deleteFile(GOOD2);
		deleteFile(ZERO);
		deleteFile(BIG);
		deleteFile(BAD1);
		deleteFile(BAD2);
		deleteFile(BAD3);
		deleteFile(BAD4);
		tmpdir.delete();
	}

	private String filename(String name) {
		try {
			return tmpdir.getCanonicalPath() + File.separator + name;
		}
		catch (IOException e) {
			assertTrue(e.getMessage(), false);
		}
		return null;
	}

	private void writeFile(String name, int[] data) {
		try {
			File f = new File(filename(name));
			FileOutputStream fs = new FileOutputStream(f);
			for (int i = 0; i < data.length; i++) {
				fs.write((byte)data[i]);
			}
			fs.close();
		}
		catch (IOException e) {
			assertTrue(e.getMessage(), false);
		}
	}

	private void deleteFile(String name) {
		File f = new File(filename(name));
		f.delete();
	}

	private void goodFile(String name, long width, long height) {
		try {
			ImageInfo ii = reader.readInfo(filename(name));
			assertTrue("image "+name+"size mismatch. must be: "+width+" x "+height+
					   " read: "+ii.getWidth()+" x "+ii.getHeight(),
					   width == ii.getWidth() && height == ii.getHeight());
		}
		catch (ImageInfoReadException e) {
			assertTrue("cannot read "+name+" file: "+e.getMessage(), false);
		}
	}

	private void zeroSizeFile(String name) {
		try {
			ImageInfo ii = reader.readInfo(filename(name));
		}
		catch (ImageInfoReadException e) {
			return;
		}
		assertTrue("image "+name+" cannot have zero width or height", false);
	}

	private void bigSizeFile(String name) {
		try {
			ImageInfo ii = reader.readInfo(filename(name));
			assertTrue("image "+name+"size mismatch. must be: "+0xffff+" x "+0xffff+
					   " read: "+ii.getWidth()+" x "+ii.getHeight(),
					   0xffff == ii.getWidth() && 0xffff == ii.getHeight());
		}
		catch (ImageInfoReadException e) {
			assertTrue("cannot read "+name+" file: "+e.getMessage(), false);
		}
	}

	private void badFile(String name) {
		try {
			ImageInfo ii = reader.readInfo(filename(name));
		}
		catch (ImageInfoReadException e) {
			return;
		}
		assertTrue("image "+name+" cannot have corrupted header", false);
	}

	private void badResource(String name) {
		try {
			ImageInfo ii = reader.readInfo(name);
		}
		catch (ImageInfoReadException e) {
			return;
		}
		assertTrue("image "+name+" cannot have corrupted header", false);
	}

	private void goodResource(String name, long width, long height) {
		try {
			ImageInfo ii = reader.readInfo(name);
			assertTrue("image "+name+"size mismatch. must be: "+width+" x "+height+
					   " read: "+ii.getWidth()+" x "+ii.getHeight(),
					   width == ii.getWidth() && height == ii.getHeight());
		}
		catch (ImageInfoReadException e) {
			assertTrue("cannot read "+name+" file: "+e.getMessage(), false);
		}
	}

	@Test
	public void testGoodFiles() {
		goodFile(GOOD1, 1, 300);
		goodFile(GOOD2, 300, 1);
	}

	@Test
	public void testZeroSizeFile() {
		zeroSizeFile(ZERO);
	}

	@Test
	public void testMaxSizeFile() {
		bigSizeFile(BIG);
	}

	@Test
	public void testBadFiles() {
		badFile(BAD1);
		badFile(BAD2);
		badFile(BAD3);
		badFile(BAD4);
	}

	@Test
	public void testResource() {
		goodResource("resource:///ru/adv/test/util/image/test/image01.gif", 128, 123);
	}

}

