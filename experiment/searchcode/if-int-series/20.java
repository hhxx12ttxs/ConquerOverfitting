//
// ImageConverter.java
//
/* Modified for the output file name and tiff header for use by Ruben 
 * by Kota Miura @ CMCI, EMBL
 * 20100810
 *
 * 
 */
/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/



import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;

import loci.common.DebugTools;
import loci.common.Location;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelFiller;
import loci.formats.ChannelMerger;
import loci.formats.ChannelSeparator;
import loci.formats.FileStitcher;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.MissingLibraryException;
import loci.formats.in.OMETiffReader;
import loci.formats.meta.DummyMetadata;
import loci.formats.meta.IMetadata;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ImageConverter is a utility class for converting a file between formats.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/tools/ImageConverter.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/tools/ImageConverter.java">SVN</a></dd></dl>
 */
public final class ImageConverter {

  // -- Constants --

  private static final Logger LOGGER =
    LoggerFactory.getLogger(ImageConverter.class);

  // -- Constructor --

  private ImageConverter() { }

  // -- Utility methods --

  /** A utility method for converting a file from the command line. */
  public static boolean testConvert(IFormatWriter writer, String[] args)
    throws FormatException, IOException
  {
    DebugTools.enableLogging("INFO");
    String in = null, out = null;
    String map = null;
    String compression = null;
    boolean stitch = false, separate = false, merge = false, fill = false;
    boolean bigtiff = false;
    int series = -1;
    if (args != null) {
      for (int i=0; i<args.length; i++) {
        if (args[i].startsWith("-") && args.length > 1) {
          if (args[i].equals("-debug")) {
            DebugTools.enableLogging("DEBUG");
          }
          else if (args[i].equals("-stitch")) stitch = true;
          else if (args[i].equals("-separate")) separate = true;
          else if (args[i].equals("-merge")) merge = true;
          else if (args[i].equals("-expand")) fill = true;
          else if (args[i].equals("-bigtiff")) bigtiff = true;
          else if (args[i].equals("-map")) map = args[++i];
          else if (args[i].equals("-compression")) compression = args[++i];
          else if (args[i].equals("-series")) {
            try {
              series = Integer.parseInt(args[++i]);
            }
            catch (NumberFormatException exc) { }
          }
          else LOGGER.warn("Ignoring unknown command flag: {}", args[i]);
        }
        else {
          if (in == null) in = args[i];
          else if (out == null) out = args[i];
          else LOGGER.warn("Ignoring unknown argument: {}", args[i]);
        }
      }
    }
    if (in == null || out == null) {
      String[] s = {
        "To convert a file between formats, run:",
        "  bfconvert [-debug] [-stitch] [-separate] [-merge] [-expand]",
        "    [-bigtiff] [-compression codec] [-series series] [-map id]",
        "    in_file out_file",
        "",
        "      -debug: turn on debugging output",
        "     -stitch: stitch input files with similar names",
        "   -separate: split RGB images into separate channels",
        "      -merge: combine separate channels into RGB image",
        "     -expand: expand indexed color to RGB",
        "    -bigtiff: force BigTIFF files to be written",
        "-compression: specify the codec to use when saving images",
        "     -series: specify which image series to convert",
        "        -map: specify file on disk to which name should be mapped",
        "",
        "If any of the following patterns are present in out_file, they will",
        "be replaced with the indicated metadata value from the input file.",
        "",
        "   Pattern:\tMetadata value:",
        "   ---------------------------",
 //       "   " + FormatTools.SERIES_NUM + "\t\tseries index",
        "   " + RubenFilename.SERIES_NUM + "\t\tseries index",
        "   " + FormatTools.SERIES_NAME + "\t\tseries name",
        "   " + FormatTools.CHANNEL_NUM + "\t\tchannel index",
        "   " + FormatTools.CHANNEL_NAME +"\t\tchannel name",
        "   " + FormatTools.Z_NUM + "\t\tZ index",
        "   " + FormatTools.T_NUM + "\t\tT index",
        "",
        "If any of these patterns are present, then the images to be saved",
        "will be split into multiple files.  For example, if the input file",
        "contains 5 Z sections and 3 timepoints, and out_file is",
        "",
        "  converted_Z" + FormatTools.Z_NUM + "_T" +
        FormatTools.T_NUM + ".tiff",
        "",
        "then 15 files will be created, with the names",
        "",
        "  converted_Z0_T0.tiff",
        "  converted_Z0_T1.tiff",
        "  converted_Z0_T2.tiff",
        "  converted_Z1_T0.tiff",
        "  ...",
        "  converted_Z4_T2.tiff",
        "",
        "Each file would have a single image plane."
      };
      for (int i=0; i<s.length; i++) LOGGER.info(s[i]);
      return false;
    }

    if (map != null) Location.mapId(in, map);

    long start = System.currentTimeMillis();
    LOGGER.info(in);
    IFormatReader reader = new ImageReader();
    if (stitch) reader = new FileStitcher(reader);
    if (separate) reader = new ChannelSeparator(reader);
    if (merge) reader = new ChannelMerger(reader);
    if (fill) reader = new ChannelFiller(reader);

    reader.setMetadataFiltered(true);
    reader.setOriginalMetadataPopulated(true);
    try {
      ServiceFactory factory = new ServiceFactory();
      OMEXMLService service = factory.getInstance(OMEXMLService.class);
      reader.setMetadataStore(service.createOMEXMLMetadata());
    }
    catch (DependencyException de) {
      throw new MissingLibraryException(OMETiffReader.NO_OME_XML_MSG, de);
    }
    catch (ServiceException se) {
      throw new FormatException(se);
    }

    reader.setId(in);

    MetadataStore store = reader.getMetadataStore();
    if (store instanceof MetadataRetrieve) {
      writer.setMetadataRetrieve((MetadataRetrieve) store);
    }

    if (writer instanceof TiffWriter) {
      ((TiffWriter) writer).setBigTiff(bigtiff);
    }
    else if (writer instanceof ImageWriter) {
    	
      IFormatWriter w = ((ImageWriter) writer).getWriter(out+"dummy.tif");
      if (w instanceof TiffWriter) {
        ((TiffWriter) w).setBigTiff(bigtiff);
      }
    }

    String format = writer.getFormat();
    LOGGER.info("[{}] -> {} [{}]",
      new Object[] {reader.getFormat(), out, format});
    long mid = System.currentTimeMillis();

    if (format.equals("OME-TIFF") &&
      (out.indexOf(FormatTools.SERIES_NUM) > 0 ||
      out.indexOf(FormatTools.SERIES_NAME) > 0 ||
      out.indexOf(FormatTools.CHANNEL_NUM) > 0 ||
      out.indexOf(FormatTools.CHANNEL_NAME) > 0 ||
      out.indexOf(FormatTools.Z_NUM) > 0 ||
      out.indexOf(FormatTools.T_NUM) > 0))
    {
      // FIXME
      LOGGER.info(
        "Sorry, conversion to multiple OME-TIFF files is not yet supported.");
      return false;
    }
    
    int total = 0;
    int num = writer.canDoStacks() ? reader.getSeriesCount() : 1;
    long read = 0, write = 0;
    int first = series == -1 ? 0 : series;
    int last = series == -1 ? num : series + 1;
    long timeLastLogged = System.currentTimeMillis();
    for (int q=first; q<last; q++) {
      reader.setSeries(q);
      writer.setSeries(q);
      writer.setInterleaved(reader.isInterleaved());
      int numImages = writer.canDoStacks() ? reader.getImageCount() : 1;
      total += numImages;
      for (int i=0; i<numImages; i++) {
        //writer.setId(FormatTools.getFilename(q, i, reader, out));
    	  writer.setId(RubenFilename.getFilename(q, i, reader, out));
    	  IMetadata writermeta = (IMetadata) writer.getMetadataRetrieve();
    	  System.out.println("Before: " + writermeta.getPixelsType(0));
    	  writermeta.setPixelsType(ome.xml.model.enums.PixelType.UINT16, 0);
    	  //writermeta.setPixelsType(ome.xml.model.enums.PixelType.INT16, 0);
    	  System.out.println("After: " + writermeta.getPixelsType(0));

        if (compression != null) writer.setCompression(compression);

        long s = System.currentTimeMillis();
        byte[] buf = reader.openBytes(i);
        
        //Object plane = reader.openPlane(i, 0, 0, reader.getSizeX(), reader.getSizeY());
        byte[][] lut = reader.get8BitLookupTable();
        if (lut != null) {
          IndexColorModel model = new IndexColorModel(8, lut[0].length,
            lut[0], lut[1], lut[2]);
          writer.setColorModel(model);
        }
        long m = System.currentTimeMillis();
        convert12bitTo16bit(buf);
        writer.saveBytes(i, buf); 
        //writer.savePlane(i, plane);   //writing speed did not change much with this. 
        long e = System.currentTimeMillis();
        read += m - s;
        write += e - m;

        // log number of planes processed every second or so
        if (i == numImages - 1 || (e - timeLastLogged) / 1000 > 0) {
          int current = i + 1;
          int percent = 100 * current / numImages;
          StringBuilder sb = new StringBuilder();
          sb.append("\t");
          int numSeries = last - first;
          if (numSeries > 1) {
            sb.append("Series ");
            sb.append(q);
            sb.append(": converted ");
          }
          else sb.append("Converted ");
          LOGGER.info(sb.toString() + "{}/{} planes ({}%)",
            new Object[] {current, numImages, percent});
          timeLastLogged = e;
        }
      }
    }
    writer.close();
    long end = System.currentTimeMillis();
    LOGGER.info("[done]");

    // output timing results
    float sec = (end - start) / 1000f;
    long initial = mid - start;
    float readAvg = (float) read / total;
    float writeAvg = (float) write / total;
    LOGGER.info("{}s elapsed ({}+{}ms per plane, {}ms overhead)",
      new Object[] {sec, readAvg, writeAvg, initial});

    return true;
  }

  //little endian
  public static void convert12bitTo16bit(byte[] buf){
	  for (int i = 0; i < buf.length; i += 2)
		  buf[i+1] = (byte)(buf[i+1] & 0x0F);
  }
  
  // -- Main method --

  public static void main(String[] args) throws FormatException, IOException {
	  if (!testConvert(new ImageWriter(), args)) System.exit(1);
	  System.exit(0);
  }

  static class RubenFilename {  
	  /** Patterns to be used when constructing a pattern for output filenames. */
	  public static final String SERIES_NUM = "%s";
	  public static final String SERIES_NAME = "%n";
	  public static final String CHANNEL_NUM = "%c";
	  public static final String CHANNEL_NAME = "%w";
	  public static final String Z_NUM = "%z";
	  public static final String T_NUM = "%t";

	  //out should end with path separator e.g."\"
	  public static String getFilename(int series, int image, IFormatReader r,
			  String out) throws FormatException, IOException
			  {
		  MetadataStore store = r.getMetadataStore();
		  MetadataRetrieve retrieve = store instanceof MetadataRetrieve ?
				  (MetadataRetrieve) store : new DummyMetadata();

				  //String filename = pattern.replaceAll(SERIES_NUM, String.valueOf(series));

				  String imageName = retrieve.getImageName(series);
				  System.out.println(imageName);
				  String wStr = getWellNumstring(imageName, "Well ", 5, ", Field");
				  String fStr = getWellNumstring(imageName, "Field ", 6, " (");
				  //System.out.println("extracted well: " + wStr);
				  //System.out.println("extracted field: " + fStr);			
				  if (imageName == null) imageName = "Series" + series;
				  imageName = imageName.replaceAll("/", "_");
				  imageName = imageName.replaceAll("\\\\", "_");

				  //filename = filename.replaceAll(SERIES_NAME, imageName);

				  r.setSeries(series);
				  int[] coordinates = r.getZCTCoords(image);

				  //filename = filename.replaceAll(Z_NUM, String.valueOf(coordinates[0]));
				  //filename = filename.replaceAll(T_NUM, String.valueOf(coordinates[2]));
				  //filename = filename.replaceAll(CHANNEL_NUM, String.valueOf(coordinates[1]));

				  String zStr = String.valueOf(coordinates[0]);
				  String tStr = String.valueOf(coordinates[2]);
				  String cStr = String.valueOf(coordinates[1]+1);			

				  File xmlfile = new File(r.getCurrentFile());
				  String parentdir = xmlfile.getParentFile().getName();
				  //String grandParentpath = xmlfile.getParentFile().getParent()+xmlfile.separator;

				  //System.out.println(grandParentpath);
				  out = out  
				  + "W"
				  + lPad(wStr, 4)
				  + xmlfile.separator
				  + "p" + lPad(fStr,3);
				  File subw = new File(out);
				  if (!subw.isDirectory()) subw.mkdirs();
				  out = out + xmlfile.separator;


				  String filenameRuben =out + parentdir
				  + "--W" + lPad(wStr, 4) 
				  + "--P" + lPad(fStr,3)  
				  + "--T" + lPad(tStr,5) 
				  + "--Z" + lPad(zStr,3) 
				  + "--C" + lPad(cStr,2) 
				  + ".ome.tif";
				  System.out.println(filenameRuben);
				  System.out.println(r);


				  String channelName = retrieve.getChannelName(series, coordinates[1]);
				  if (channelName == null) channelName = String.valueOf(coordinates[1]);
				  channelName = channelName.replaceAll("/", "_");
				  channelName = channelName.replaceAll("\\\\", "_");

				  //filename = filename.replaceAll(CHANNEL_NAME, channelName);
				  //return filename;
				  return filenameRuben;
			  }

	  public static String getWellNumstring(String imagename, String key1, int key1offset, String key2){
		  String wellnumstring = "";
		  int si = imagename.indexOf(key1);
		  int se = imagename.indexOf(key2); 
		  wellnumstring = imagename.substring(si + key1offset, se);			
		  return wellnumstring;
	  }

	  public static String lPad(String n, int width){			
		  String s = n;
		  while (s.length()<width)
			  s = "0" + s;
		  return s;						
	  }
  }

}

