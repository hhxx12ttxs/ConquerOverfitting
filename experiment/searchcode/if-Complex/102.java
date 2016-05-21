if (complexValues) { amtRead *= 2;   dataInputStream.readLong(); }
int complex = restOfLine.indexOf(\"complex\");
if (complex >= 0) complexValues = true;
int r = restOfLine.indexOf(\"real\");
 *
 * Electric(tm) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * You should have received a copy of the GNU General Public License
 * along with Electric(tm); see the file COPYING.  If not, write to
// for complex plots, ignore imaginary part
if (complexValues) { amtRead *= 2;   dataInputStream.readInt(); }
// for complex plots, ignore imaginary part

