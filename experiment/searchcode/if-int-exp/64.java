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
// File: JPEGInfoReader.java
//
// Created: Fri Jul  5 16:37:52 2002
//
// $Id: JPEGInfoReader.java 1117 2009-06-09 11:54:21Z tsarev_adv $
// $Name:  $
//


package ru.adv.util.image;

import java.io.*;

/**
 * ?????? ?????????? ?? jpeg-??????
 * @version $Revision: 1.5 $
 */
public class JPEGInfoReader extends ImageInfoReader {

	private static final int SOF0  = 0xc0;
	private static final int SOF1  = 0xc1;
	private static final int SOF2  = 0xc2;
	private static final int SOF3  = 0xc3;

	private static final int DHT   = 0xc4;

	private static final int SOF5  = 0xc5;
	private static final int SOF6  = 0xc6;
	private static final int SOF7  = 0xc7;

	private static final int JPG   = 0xc8;

	private static final int SOF9  = 0xc9;
	private static final int SOF10 = 0xca;
	private static final int SOF11 = 0xcb;

	private static final int DAC   = 0xcc;

	private static final int SOF13 = 0xcd;
	private static final int SOF14 = 0xce;
	private static final int SOF15 = 0xcf;

	private static final int RST0  = 0xd0;
	private static final int RST1  = 0xd1;
	private static final int RST2  = 0xd2;
	private static final int RST3  = 0xd3;
	private static final int RST4  = 0xd4;
	private static final int RST5  = 0xd5;
	private static final int RST6  = 0xd6;
	private static final int RST7  = 0xd7;

	private static final int SOI   = 0xd8;
	private static final int EOI   = 0xd9;
	private static final int SOS   = 0xda;
	private static final int DQT   = 0xdb;
	private static final int DNL   = 0xdc;
	private static final int DRI   = 0xdd;
	private static final int DHP   = 0xde;
	private static final int EXP   = 0xdf;

	private static final int APP0  = 0xe0;
	private static final int APP1  = 0xe1;
	private static final int APP2  = 0xe2;
	private static final int APP3  = 0xe3;
	private static final int APP4  = 0xe4;
	private static final int APP5  = 0xe5;
	private static final int APP6  = 0xe6;
	private static final int APP7  = 0xe7;
	private static final int APP8  = 0xe8;
	private static final int APP9  = 0xe9;
	private static final int APP10 = 0xea;
	private static final int APP11 = 0xeb;
	private static final int APP12 = 0xec;
	private static final int APP13 = 0xed;
	private static final int APP14 = 0xee;
	private static final int APP15 = 0xef;

	private static final int JPG0  = 0xf0;
	private static final int JPG1  = 0xf1;
	private static final int JPG2  = 0xf2;
	private static final int JPG3  = 0xf3;
	private static final int JPG4  = 0xf4;
	private static final int JPG5  = 0xf5;
	private static final int JPG6  = 0xf6;
	private static final int JPG7  = 0xf7;
	private static final int JPG8  = 0xf8;
	private static final int JPG9  = 0xf9;
	private static final int JPG10 = 0xfa;
	private static final int JPG11 = 0xfb;
	private static final int JPG12 = 0xfc;
	private static final int JPG13 = 0xfd;

	private static final int COM   = 0xfe;

	private static final int TEM   = 0x01;
	private static final int RES0  = 0x02;
	private static final int RESN  = 0xBF;

	protected ImageInfo doReadInfo(String filename) throws ImageInfoReadException {
		InputStream is = null;
		try {
			try {
				is = getInputStream(filename);
				readHeader(is, filename);

				int height = readUnsignedShort(is);
				int width = readUnsignedShort(is);
				if (!(height >= 0 && height <= 65535) ||
					!(height >= 1 && height <= 65535))
					throw new ImageInfoReadException("JPEG file corrupted", filename);

				return new JPEGImageInfo(filename,
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
		// jpeg file must be started from SOI marker
		int marker = readMarker(is, filename);
		if (marker != SOI)
			throw new ImageInfoReadException("JPEG file corrupted", filename);

		// read all blocks in jpeg file until SOF block is not appeared
		while (true) {
			marker = readMarker(is, filename);
			if (isSOF(marker)) {
				// skip frame header length (2 bytes) &
				// sample precision (1 byte)
				is.skip(3);
				break;
			}
			skipBlock(is);
		}
	}

	private boolean isSOF(int marker) {
		return (marker >= SOF0 && marker <= SOF3) ||
			(marker >= SOF5 && marker <= SOF7) ||
			(marker >= SOF9 && marker <= SOF11) ||
			(marker >= SOF13 && marker <= SOF15);
	}

	private int readMarker(InputStream is, String filename) throws IOException,ImageInfoReadException {
		int b1 = is.read();
		int b2 = is.read();
		if (b1 != 0xff || b2 == 0x0 || b2 == 0xff) {
			throw new ImageInfoReadException("JPEG file corrupted", filename);
		}
		return b2;
	}

	private int readUnsignedShort(InputStream is) throws IOException {
		int b1 = is.read();
		int b2 = is.read();
		return b1 << 8 | b2;
	}

	private void skipBlock(InputStream is) throws IOException {
		// first two bytes from beginning of the block are one's length
		int size = readUnsignedShort(is);
		is.skip(size-2);
	}

	private String printMarker(int m) {
		String s = "unknown";
		if (m >= SOF0 && m <= SOF3)
			s = "SOF" + (m - 0xc0);
		else if (m == DHT)
			s = "DHT";
		else if (m >= SOF5 && m <= SOF7)
			s = "SOF" + (m - 0xc0);
		else if (m == JPG)
			s = "JPG";
		else if (m >= SOF9 && m <= SOF11)
			s = "SOF" + (m - 0xc0);
		else if (m == DAC)
			s = "DAC";
		else if (m >= SOF13 && m <= SOF15)
			s = "SOF" + (m - 0xc0);
		else if (m >= RST0 && m <= RST7)
			s = "RST" + (m - 0xd0);
		else if (m == SOI)
			s = "SOI";
		else if (m == EOI)
			s = "EOI";
		else if (m == SOS)
			s = "SOS";
		else if (m == DQT)
			s = "DQT";
		else if (m == DNL)
			s = "DNL";
		else if (m == DRI)
			s = "DRI";
		else if (m == DHP)
			s = "DHP";
		else if (m == EXP)
			s = "EXP";
		else if (m >= APP0 && m <= APP15)
			s = "APP" + (m - 0xe0);
		else if (m >= JPG0 && m <= JPG13)
			s = "JPG" + (m - 0xd0);
		else if (m == COM)
			s = "COM";
		else if (m == TEM)
			s = "TEM";
		else if (m >= RES0 && m <= RESN)
			s = "RES" + (m - 0x2);
		return s;
	}
}

