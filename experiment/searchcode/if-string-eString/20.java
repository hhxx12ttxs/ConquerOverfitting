/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package plots.io.readers;
import plots.io.DataPointSet;
import plots.io.DataPoint2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Read a data file in the following format:
 *  x      x_bin    |       N0 degrees       N1 degrees   |  Angle integrated DDXS
 * (unit) (unit)    |   value    error                    |   DDXS        error
 * -----------------+-------------------------------------+-------------------------
 * 3.0      0.5     |     1.3     0.1        30.4    0.3  |    31.7        0.4
 * -----------------+-------------------------------------+-------------------------
 * Energy integrated|     1.3     0.1        30.4         |    31.7        0.4
 *
 * Components of the data file format:
 *
 * Header information (2 first lines):
 * 0. Divided into 3 sections, separator "|"
 * 1. x-variable and units
 * 2. List of angles (arbitrary number of angles listed). Need to detect the
 *    the number of angles N and the label of the each angle. To accomplish this
 *    we need to remove extra spaces and use "degrees" as a delimeter.
 * 3. Angle integrated DDXS (treated as a comment)
 *
 * Separator: ------------+-------------+---------------
 *
 * Main data:
 * 0. Arbitrary number of lines, starting from line 4 and ending with
 *    a separator line.
 * 1. Divided into 3 sections (delimeter: "|"): x-value (energy + bin width),
 *    individual angles (value-error pairs) and angle integrated value-error
 *    pair.
 *
 * Energy integrated values:
 * 0. Divided into 3 sections, delimeter "|"
 * 1. Comment section (do nothing)
 * 2. Arbitrary number of angles (value-error pairs), number of angles N
 * 3. Angle integrated, value-error pair
 *
 * @author Pekka Kaitaniemi
 */
public class ColumnReader implements AbstractReader {
    private String filename;
    private int numberOfAngles = 0;
    private ArrayList<DataPointSet> ddxsHistograms;
    private DataPointSet energyIntegrated;
    private DataPointSet angleIntegrated;

    public ColumnReader(String filename) {
        this.filename = filename;
        angleIntegrated = new DataPointSet("Angle integrated");
        energyIntegrated = new DataPointSet("Energy integrated");
        ddxsHistograms = new ArrayList<DataPointSet>();
    }

    public ArrayList<DataPointSet> getDoubleDifferentialDatasets() {
        return this.ddxsHistograms;
    }
    
    public void read() throws FileNotFoundException {
        BufferedReader reader = openFile();
        readHeader(reader);
        try {
            readMainData(reader);
        } catch (IOException ex) {
            Logger.getLogger(ColumnReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        ddxsHistograms.get(0).printData();
    }

    /**
     * Read the data header information
     *
     * The most important thing to do here is to discover the number of
     * angles we have to plot and their labels. The numbering of the angles
     * starts from 0 and will be used later when we fill the actual data points
     * to the data point sets.
     */
    private void readHeader(BufferedReader br) {
        String line = null;
        String doubleDifferentials = null;
        for(int i = 0; i < 3; i++) { // Three header lines
            try {
                line = br.readLine();
                System.out.println(line);
                if(i == 0) {         // Needed information is on the 1st line
                    line.trim();
                    Scanner s = new Scanner(line).useDelimiter("\\s*\\|\\s*");
                    while(s.hasNext()) {
                        String section = s.next();
                        System.out.println(section);
                        if(section.contains("degree")) {
                            doubleDifferentials = section;
                            System.out.println(section);
                        }
                    }
                    s.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ColumnReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Parse the DDXS definition string. We have to isolate the angles from
        // the rest of the string
        Scanner angleScanner = new Scanner(doubleDifferentials);
        angleScanner.useDelimiter("degrees");
        String angle;
        while(angleScanner.hasNext()) {
            angle = angleScanner.next().trim();
            ddxsHistograms.add(new DataPointSet(angle + " degrees"));
            numberOfAngles++;
            System.out.println(angle);
        }
        angleScanner.close();
        System.out.println("Found " + numberOfAngles + " angles in the file.");
    }

    /**
     * Read the main double-differential cross section data
     * 
     * Here we read the data (both enery and cross section values) and use
     * them to fill the corresponding DataPointSet objects.
     *
     * @param br connection to the open file
     */
    private void readMainData(BufferedReader br) throws IOException {
        double s, ds;
        double E = 0, Ebin = 0;
        int angleNumber = 0;
        String line = br.readLine();
        line = br.readLine();
        System.out.println(line);
        while(line.contains("------") == false) { // Main data ends with line containing -----
            Scanner sectionScanner = new Scanner(line);
            sectionScanner.useDelimiter("\\|");
            String[] section = new String[3];
            int i = 0;
            while(sectionScanner.hasNext()) { // Section 0: energy, 1: ddxs data, 2: angle integrated
                section[i] = sectionScanner.next().replaceAll("\\b\\s{2,}\\b", " "); // Remove all spaces
                section[i] = section[i].trim();
                //section[i] = sectionScanner.next().trim();
                System.out.println(section[i]);
                i++;
            }
            sectionScanner.close();

            // Get energy and energy bin width
            System.out.println("Now parsing: " + section[0]);
            //String[] tokens = section[0].split("\\ ");
            //String[] tokens2 = section[0].split("\\d\\ ");
            //System.out.println("Token 0: " + tokens[0]);
            //System.out.println("Token 1: " + tokens[1]);
            //System.out.println("Token 0: " + tokens2[0]);
            //System.out.println("Token 1: " + tokens2[1]);
            Scanner energyScan = new Scanner(section[0]);
            //String Estring, deString;
            energyScan.useDelimiter(" ");
            E = new Double(energyScan.next().trim());
            Ebin = new Double(energyScan.next().trim());
            //System.out.println("E = " + Estring + "   deString = " + deString);
            energyScan.close();
            
            // Get the double differential cross section values
            Scanner angleScan = new Scanner(section[1]);
            angleScan.useDelimiter(" ");
            int angleIndex = 0;
            while(angleScan.hasNext()) {
                s = new Double(angleScan.next().trim());
                ds = new Double(angleScan.next().trim());
                ddxsHistograms.get(angleIndex).addPoint(new DataPoint2D(E, Ebin, s, ds));
                angleIndex++;
            }
            angleScan.close();
            line = br.readLine();
        }
    }
    
    private String readEnergyIntegratedData(BufferedReader br) {
        return null;
    }

    private BufferedReader openFile() throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(this.filename));
        return br;
    }
}

