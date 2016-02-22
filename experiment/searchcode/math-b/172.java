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
// File: GIFInfoReader.java
//
// Created: Fri Jul  5 15:15:08 2002
//
// $Id: GIFInfoReader.java 1106 2009-06-03 07:32:17Z vic $
// $Name:  $
//


package ru.adv.util.image;

import java.io.*;

/**
 * ?????? ?????????? ?? gif-??????
 * @version $Revision: 1.5 $
 */
public class GIFInfoReader extends ImageInfoReader {

	protected ImageInfo doReadInfo(String filename) throws ImageInfoReadException {
		InputStream is = null;
		try {
			try {
				is = getInputStream(filename);
				readHeader(is, filename);
				long width = readUnsignedShort(is);
				long height = readUnsignedShort(is);
				if (width < 1 || height < 1)
					throw new ImageInfoReadException("GIF file corrupted", filename);
				return new GIFImageInfo(filename,
										width,
										height);
			}
			finally {
				if (is != null) {
					is.close();
				}
			}
		}
		catch (Exception e) {
			throw new ImageInfoReadException(e, filename);
		}
	}

	private void readHeader(InputStream is, String filename) throws IOException,ImageInfoReadException {
		byte[] b = new byte[6];
		is.read(b);
		if (b[0] != 0x47 || // G
			b[1] != 0x49 || // I
			b[2] != 0x46 || // F
			!(b[3] >= 0x30 && b[3] <= 0x39) || // 0..9
			!(b[4] >= 0x30 && b[4] <= 0x39) || // 0..9
			!(b[5] >= 0x61 && b[5] <= 0x7a))   // a..z
			throw new ImageInfoReadException("Invalig GIF header ", filename);
	}

	private int readUnsignedShort(InputStream is) throws IOException {
		int b1 = is.read();
		int b2 = is.read();
		return b2 << 8 | b1;
	}
}

