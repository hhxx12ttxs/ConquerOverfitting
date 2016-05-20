/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trussoptimizater.Truss;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import trussoptimizater.Truss.Sections.CHS;
import trussoptimizater.Truss.Sections.RHS;
import trussoptimizater.Truss.Sections.SHS;
import trussoptimizater.Truss.Sections.TubularSection;

/**
 *
 * @author Chris
 */
public class SectionLibrary {

    /**
     * Relative file path to the file containing the CHS section data
     */
    private static final String CHSDataFileName = "/Data/S355 CHS Section properties.csv";
    /**
     * Relative file path to the file containing the RHS section data
     */
    private static final String RHSDataFileName = "/Data/S355 RHS Section properties.csv";
    /**
     * Relative file path to the file containing the SHS section data
     */
    private static final String SHSDataFileName = "/Data/S355 SHS Section properties.csv";
    public static final ArrayList<TubularSection> SECTIONS = readFiles();

    /**
     * Loops through sections and tests whether each section is equal to that of
     * "sectionName". If no matching section is found null is returned
     * @param sectionName The name of the section
     * @return A section with a name equal to that of sectionName. If there is no section with the name
     * sectionName then null is returned
     */
    public static TubularSection get(String sectionName) {
        for (int i = 0; i < SECTIONS.size(); i++) {
            if (sectionName.equals(SECTIONS.get(i).getName())) {
                return SECTIONS.get(i);
            }
        }
        return null;
    }

    /**
     *
     * @return an ArrayList of all sections
     */
    public static ArrayList<TubularSection> readFiles() {
        ArrayList<TubularSection> tempSections = new ArrayList<TubularSection>();
        try {

            tempSections.addAll(readCHSData());
            tempSections.addAll(readRHSData());
            tempSections.addAll(readSHSData());
        } catch (Exception ex) {
            System.err.println("Reading files: " + ex.getMessage());
        }
        return tempSections;
    }//end of readFile

    /**
     *
     * @return ArrayList of circle hollow sections (CHS)
     * @throws Exception if TrussModel.CHSDataFileName is invalid
     */
    private static ArrayList<TubularSection> readCHSData() throws Exception {
        ArrayList<TubularSection> CHSSections = new ArrayList<TubularSection>();
        BufferedReader fileR = null;



        try {
            String n = null;
            InputStream instream = SectionLibrary.class.getResourceAsStream(SectionLibrary.CHSDataFileName);
            InputStreamReader instreamReader = new InputStreamReader(instream);
            fileR = new BufferedReader(instreamReader);

            //FileReader file = new FileReader(TrussModel.CHSDataFileName);
            //fileR = new BufferedReader(file);
            while (true) {
                n = fileR.readLine();
                if (n == null) {
                    break;
                } else {
                    String[] elements = n.split(",");
                    String section = elements[0] + " x " + elements[1];
                    double D = Double.parseDouble(elements[0]);
                    double t = Double.parseDouble(elements[1]);
                    double area = Double.parseDouble(elements[3]);
                    double I = Double.parseDouble(elements[5]);
                    CHSSections.add(new CHS(section, D, t, area, I));
                }
            }
        } finally {
            if (fileR != null) {
                fileR.close();
            }
        }
        return CHSSections;

    }

    /**
     *
     * @return ArrayList of rectangle hollow sections (RHS)
     * @throws Exception if TrussModel.RHSDataFileName is invalid
     */
    private static ArrayList<TubularSection> readRHSData() throws Exception {
        ArrayList<TubularSection> RHSSections = new ArrayList<TubularSection>();
        BufferedReader fileR = null;
        try {
            String n = null;
            InputStream instream = SectionLibrary.class.getResourceAsStream(SectionLibrary.RHSDataFileName);
            InputStreamReader instreamReader = new InputStreamReader(instream);
            fileR = new BufferedReader(instreamReader);

            //FileReader file = new FileReader(TrussModel.RHSDataFileName);
            //fileR = new BufferedReader(file);
            while (true) {
                n = fileR.readLine();
                if (n == null) {
                    break;
                } else {
                    String[] elements = n.split(",");

                    String section = elements[0] + " x " + elements[1];
                    double B = Double.parseDouble(elements[0].split("x")[1]);
                    double D = Double.parseDouble(elements[0].split("x")[0]);
                    double t = Double.parseDouble(elements[1]);
                    double area = Double.parseDouble(elements[3]);
                    double Ixx = Double.parseDouble(elements[6]);
                    double Iyy = Double.parseDouble(elements[7]);
                    //RHSData.add(new RHS(section, B, t, D, area, Ixx, Iyy));
                    RHSSections.add(new RHS(section, B, t, D, area, Ixx, Iyy));
                    //tempSectionNames.add(section);
                }
            }//end while
        } finally {
            if (fileR != null) {
                fileR.close();
            }
        }
        return RHSSections;
    }

    /**
     *
     * @return ArrayList of square hollow sections (SHS)
     * @throws Exception if TrussModel.SHSDataFileName is invalid
     */
    private static ArrayList<TubularSection> readSHSData() throws Exception {
        ArrayList<TubularSection> CHSSections = new ArrayList<TubularSection>();
        BufferedReader fileR = null;
        try {
            String n = null;
            InputStream instream = SectionLibrary.class.getResourceAsStream(SectionLibrary.SHSDataFileName);
            InputStreamReader instreamReader = new InputStreamReader(instream);
            fileR = new BufferedReader(instreamReader);

            //FileReader file = new FileReader(TrussModel.SHSDataFileName);
            //fileR = new BufferedReader(file);
            while (true) {
                n = fileR.readLine();
                if (n == null) {
                    break;
                } else {
                    String[] elements = n.split(",");
                    String section = elements[0] + " x " + elements[1];
                    double D = Double.parseDouble(elements[0].split("x")[0]);
                    double t = Double.parseDouble(elements[1]);
                    double area = Double.parseDouble(elements[3]);
                    double I = Double.parseDouble(elements[5]);
                    //SHSData.add(new SHS(section, D, t, area, I));
                    CHSSections.add(new SHS(section, D, t, area, I));
                    //tempSectionNames.add(section);
                }
            }//end while
        } finally {
            if (fileR != null) {
                fileR.close();
            }
        }
        return CHSSections;
    }
}

