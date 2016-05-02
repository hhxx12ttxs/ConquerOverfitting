/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kuhnlab.trixy.data.io;

import kuhnlab.trixy.data.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.StringTokenizer;
import javax.swing.filechooser.FileFilter;
import kuhnlab.coordinates.KPoint2D;
import kuhnlab.gui.GenericOptionsPanel;

/** Reads tab and comma separated files
 *
 * @author jrkuhn
 */
public class AbstractSeriesFileHandler implements SeriesFileHandler {
    protected static String ABSTRACT_METHOD = "Method not implemented by AbstractSeriesFileHandler";
    
    protected String separator;
    protected SeriesFileFilter filter;
    
    protected AbstractSeriesFileHandler(SeriesFileFilter filter, String separator) {
        this.filter = filter;
        this.separator = separator;
    }

    protected SeriesList seriesListFromReader(Reader reader, String separator, boolean ptiFormat, boolean singleX) {
        try {
            BufferedReader br = new BufferedReader(reader);
            String line;
            String[] split;
            StringTokenizer tok;

            int nCurves = 0;
            int nExpectedColumns = 0;
            SeriesList data = new SeriesList();
            
            if (ptiFormat) {
                // PTI LINE 1: contains number of series in file.
                line = br.readLine();
                nCurves = Integer.parseInt(line.trim());
                nExpectedColumns = nCurves*2;
                // PTI LINE 2: contains number of points in each series. IGNORE
                line = br.readLine();
                // PTI LINE 3: contains names of each series
                line = br.readLine();
                tok = new StringTokenizer(line, separator);
                for (int curve=0; curve<nCurves; curve++) {
                    String name = tok.nextToken();
                    if (name.startsWith("\"") && name.endsWith("\"")) {
                        name = name.substring(1,name.length()-1);
                    }
                    data.addSeries(new Series(name));
                }
                // PTI LINE 4: contains repeats of X Y X Y... IGNORE
                line = br.readLine();
            } else {
                // EXCEL LINE 1: contains column headers Time,YLabel,Time,YLabel,...
                line = br.readLine();
                split = line.split(separator);
                if (singleX) {
                    nCurves = split.length - 1;
                    nExpectedColumns = nCurves + 1;
                } else {
                    if (split.length % 2 == 1) {
                        // Odd number of titles. This is bad
                        br.close();
                        return null;
                    }
                    nCurves = split.length / 2;
                    nExpectedColumns = nCurves * 2;
                }
                for (int i=0; i<nCurves; i++) {
                    String name = singleX ? split[i+1] : split[i*2+1];
                    if (name.startsWith("\"") && name.endsWith("\"")) {
                        name = name.substring(1,name.length()-1);
                    }
                    // NOTE: names inclosed in curly brackets denote
                    // that the curve is currently hidden
                    boolean visible = true;
                    if (name.startsWith("{") && name.endsWith("}")) {
                        name = name.substring(1,name.length()-1);
                        visible = false;
                    }
                    Series newser = new Series(name);
                    newser .visible = visible;
                    data.addSeries(newser);
                }
            }

            // PTI LINES 5...END or
            // EXCEL LINES 2...END: contains data in the form xval,yval
            line = br.readLine();
            while (line != null) {
                String[] asData = line.split(separator);
                if (asData.length == nExpectedColumns) {
                    if (singleX) {
                        String sx = asData[0].trim();
                        if (!sx.equals("")) {
                            double x = Double.parseDouble(sx);
                            for (int curve=0; curve<nCurves; curve++) {
                                String sy = asData[curve+1].trim();
                                if (!sy.equals("")) {
                                    data.getSeries(curve).add(new KPoint2D(x, Double.parseDouble(sy)));
                                }
                            }
                        }
                    } else {
                        for (int curve=0; curve<nCurves; curve++) {
                            String sx = asData[curve*2].trim();
                            String sy = asData[curve*2+1].trim();
                            if (!(sx.equals("") || sy.equals(""))) {
                                data.getSeries(curve).add(new KPoint2D(Double.parseDouble(sx), Double.parseDouble(sy)));
                            }
                        }
                    }
                }
                line = br.readLine();
            }
            br.close();
            return data;
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected boolean seriesListToWriter(SeriesList data, Writer writer, String separator, boolean ptiFormat, boolean addQuotes, boolean singleX) {
        try {
            BufferedWriter bw = new BufferedWriter(writer);

            Series xvals = null;
            if (singleX) {
                double[] xrange = data.getXRange();
                double xmin = xrange[0], xmax = xrange[1];
                double delx = data.getAverageDeltaX();
                SeriesList newData = new SeriesList();
                for (Series ser : data.getSeries()) {
                    Series newser = (Series) ser.clone();
                    newser.resample(xmin, xmax, delx);
                    newData.addSeries(newser);
                }
                xvals = new Series(data.getSeries(0).getName());
                for (double x=xmin; x<=xmax; x+=delx) {
                    xvals.add(new KPoint2D(x, 0));
                }
                data = newData;
            }
            
            int nCurves = data.getSeriesCount();

            // calculate the maximum number of points for all curves
            int maxSize = 0;
            for (int curve=0; curve<nCurves; curve++) {
                int size = data.getSeries(curve).getSize();
                if (size > maxSize) 
                    maxSize = size;
            }
            
            if (ptiFormat) {
                // PTI LINE 1: contains number of series in file
                bw.write(""+nCurves);
                bw.newLine();

                // PTI LINE 2: contains number of points in each series
                for (int curve=0; curve<nCurves; curve++) {
                    Series ser = data.getSeries(curve);
                    int size = ser.getSize();
                    bw.write(""+size+separator);
                    if (curve < nCurves-1)
                        bw.write(separator);
                    else
                        bw.newLine();
                }

                // PTI LINE 3: contains names of each series
                for (int curve=0; curve<nCurves; curve++) {
                    Series ser = data.getSeries(curve);
                    bw.write(ser.getName()+separator);
                    if (curve < nCurves-1)
                        bw.write(separator);
                    else
                        bw.newLine();
                }

                // PTI LINE 4: contains repeats of X Y X Y...
                for (int curve=0; curve<nCurves; curve++) {
                    bw.write("X"+separator+"Y");
                    if (curve < nCurves-1)
                        bw.write(separator);
                    else
                        bw.newLine();
                }
            } else {
                // EXCEL LINE 1: contains column headings
                if (singleX) {
                    if (addQuotes) {
                        bw.write("\"X\""+separator);
                    } else {
                        bw.write("X"+separator);
                        
                    }
                    for (int curve=0; curve<nCurves; curve++) {
                        Series ser = data.getSeries(curve);
                        String sn = ser.getName();
                        if (!ser.visible) {
                            sn = "{"+sn+"}";
                        }
                        if (addQuotes) {
                            bw.write("\""+sn+"\"");
                        } else {
                            bw.write(sn);
                        }
                        if (curve < nCurves-1)
                            bw.write(separator);
                        else
                            bw.newLine();
                    }
                } else {
                    for (int curve=0; curve<nCurves; curve++) {
                        Series ser = data.getSeries(curve);
                        String sn = ser.getName();
                        // NOTE: names inclosed in curly brackets denote
                        // that the curve is currently hidden
                        if (!ser.visible) {
                            sn = "{"+sn+"}";
                        }
                        if (addQuotes) {
                            bw.write("\"X\""+separator+"\""+sn+"\"");
                        } else {
                            bw.write("X"+separator+sn);
                        }
                        if (curve < nCurves-1)
                            bw.write(separator);
                    }
                    bw.newLine();
                }
            }

            // PTI LINES 5...END or
            // EXCEL LINES 2...END: contains data in the form xval,yval
            if (singleX) {
                for (KPoint2D pt : xvals.getPoints()) {
                    bw.write(Double.toString(pt.x));
                    bw.write(separator);
                    for (int curve=0; curve<nCurves; curve++) {
                        Series ser = data.getSeries(curve);
                        double y = ser.interpolateY(pt.x);
                        if (!Double.isNaN(y)) {
                            bw.write(Double.toString(y));
                        }
                        if (curve < nCurves-1)
                            bw.write(separator);
                        else
                            bw.newLine();
                    }
                }
            } else {
                for (int linenum=0; linenum<maxSize; linenum++) {
                    for (int curve=0; curve<nCurves; curve++) {
                        Series ser = data.getSeries(curve);
                        if (linenum < ser.getSize()) {
                            KPoint2D pt = ser.getPoint(linenum);
                            if (!Double.isNaN(pt.x))
                                bw.write(Double.toString(pt.x));
                            bw.write(separator);
                            if (!Double.isNaN(pt.y))
                                bw.write(Double.toString(pt.y));
                        } else {
                            bw.write(separator);
                        }
                        if (curve < nCurves-1)
                            bw.write(separator);
                    }
                    bw.newLine();
                }
            }
            bw.close();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public GenericOptionsPanel getOptionsPanel(boolean writeMode) {
        return null;
    }


    public SeriesList readFile(File file) {
        try {
            FileReader fr = new FileReader(file);
            return seriesListFromReader(fr, separator, false, false);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean writeFile(SeriesList series, File file) {
        try {
            FileWriter fw = new FileWriter(file);
            return seriesListToWriter(series, fw, separator, false, true, false);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isFileSignature(File file) {
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String[] split;
            StringTokenizer tok;

            boolean goodSignature = true;
            
            // first line should be column headers separated by "separator" character
            line = br.readLine();
            split = line.split(separator);
            int nColumns = split.length;
            if (nColumns < 2) {
                goodSignature = false;
            }
            
            if (goodSignature) {
                //second line should have the same number of columns
                line = br.readLine();
                split = line.split(separator);
                if (split.length != nColumns) {
                    goodSignature = false;
                }
            }
            br.close();
            reader.close();
            return goodSignature;
        } catch (Exception ex) {
        }
        return false;
    }

    public boolean isFileExtension(File file) {
        return filter.isFiletype(file);
    }
    
    public File forceFileExtension(File file) {
        return filter.forceExtension(file);
    }

    public FileFilter getFilter() {
        return filter;
    }
}

