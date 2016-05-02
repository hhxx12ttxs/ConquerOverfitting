package uk.ac.ebi.pride.chart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>PMD Quartiles file reader.</p>
 *
 * @author Antonio Fabregat
 * Date: 07-oct-2010
 * Time: 15:52:30
 */
public class PMDQuartilesFileReader {
    private static final Logger logger = LoggerFactory.getLogger(PMDQuartilesFileReader.class);

    /**
     * Contains the Q1 quartiles values from the file
     */
    private List<Double> q1Values = new ArrayList<Double>();

    /**
     * Contains the Q2 quartiles values from the file
     */
    private List<Double> q2Values = new ArrayList<Double>();

    /**
     * Contains the Q3 quartiles values from the file
     */
    private List<Double> q3Values = new ArrayList<Double>();

    /**
     * <p> Creates an instance of this PMDQuartilesFileReader object, setting all fields as per description below.</p>
     *
     * @param inputStream an input stream of the file with the PMD quartiles chartData
     */
    public PMDQuartilesFileReader(InputStream inputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        initialize(br);
    }

    /**
     * <p> Creates an instance of this PMDQuartilesFileReader object, setting all fields as per description below.</p>
     *
     * @param filePath the path of the file with the PMD quartiles chartData
     */
    public PMDQuartilesFileReader(String filePath) {
        File file = new File(filePath);

        if (!file.exists() || !file.canRead()) {
            logger.error("Can't read " + file);
            return;
        }

        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            initialize(in);
            in.close();
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(),e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Loads the data from the specified BufferedReader
     *
     * @param br the buffered reader of the file with the PMD quartiles chartData
     */
    private void initialize(BufferedReader br){
        String line;
        try {
            while ((line = br.readLine()) != null) {
                try{
                    String[] values = line.split(",");
                    //int bin = Integer.valueOf(values[0]);

                    double q1 = Double.valueOf(values[1]);
                    q1Values.add(q1);

                    double q2 = Double.valueOf(values[2]);
                    q2Values.add(q2);

                    double q3 = Double.valueOf(values[3]);
                    q3Values.add(q3);
                } catch (NumberFormatException e){/*Nothing here*/}
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
    }

    /**
     * Returns the PMD Q1 quartile data
     *
     * @return the PMD Q1 quartile data
     */
    public List<Double> getQ1Values() {
        return q1Values;
    }

    /**
     * Returns the PMD Q2 quartile data
     *
     * @return the PMD Q2 quartile data
     */
    public List<Double> getQ2Values() {
        return q2Values;
    }

    /**
     * Returns the PMD Q3 quartile data
     *
     * @return the PMD Q3 quartile data
     */
    public List<Double> getQ3Values() {
        return q3Values;
    }
}
