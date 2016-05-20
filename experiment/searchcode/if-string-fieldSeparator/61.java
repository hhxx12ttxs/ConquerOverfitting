/**                 ***COPYRIGHT STARTS HERE***
 *  BoutTime - the wrestling tournament administrator.
 *
 *  Copyright (C) 2010  Jeffrey K. Rutt
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *                  ***COPYRIGHT ENDS HERE***                                */

package bouttime.fileinput;

import bouttime.dao.Dao;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import bouttime.model.Wrestler;
import bouttime.mainview.BoutTimeApp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 * This class retrieves Wrestler records from a text (i.e. CSV) file.
 */
public class TextFileInput implements FileInput {
    static Logger logger = Logger.getLogger(TextFileInput.class);

    public TextFileInput() {}

    /**
     * Input wrestler entries for a text file.
     *
     * @param file File to read data from
     * @param config Map of config parameters for
     * column indexes, start and stop
     * row indexes, and the sheet number
     * @param dao Data access object
     *
     * @return A FileInputResult object with the results of the input operation
     */
    private FileInputResult addWrestlersFromFile(File file, Map config, Dao dao) {
        Integer recordsProcessed = Integer.valueOf(0);
        Integer recordsAccepted = Integer.valueOf(0);
        Integer recordsRejected = Integer.valueOf(0);
        List<String> rejects = new ArrayList<String>();
        int expectedCols = 0;

        try {
            String fieldSeparator = (String)config.get("fieldSeparator");
            int fNameCol = Integer.parseInt((String)config.get("firstName")) - 1;
            int lNameCol = Integer.parseInt((String)config.get("lastName")) - 1;
            int tNameCol = Integer.parseInt((String)config.get("teamName")) - 1;
            int classCol = Integer.parseInt((String)config.get("classification")) - 1;
            int divCol = Integer.parseInt((String)config.get("division")) - 1;
            int wtClassCol = Integer.parseInt((String)config.get("weightClass")) - 1;
            int actWtCol = Integer.parseInt((String)config.get("actualWeight")) - 1;
            int levelCol = Integer.parseInt((String)config.get("level")) - 1;
            int idCol = Integer.parseInt((String)config.get("serialNumber")) - 1;

            logger.info("Text File Input configuration :" +
                    "\n    separator=" + fieldSeparator +
                    "\n    first=" + fNameCol +
                    "\n    last=" + lNameCol +
                    "\n    team=" + tNameCol +
                    "\n    class=" + classCol +
                    "\n    div=" + divCol +
                    "\n    wtClass=" + wtClassCol +
                    "\n    actWt=" + actWtCol +
                    "\n    level=" + levelCol +
                    "\n    id=" + idCol);

            if (fNameCol >= 0) { expectedCols++; }
            if (lNameCol >= 0) { expectedCols++; }
            if (tNameCol >= 0) { expectedCols++; }
            if (classCol >= 0) { expectedCols++; }
            if (divCol >= 0) { expectedCols++; }
            if (wtClassCol >= 0) { expectedCols++; }
            if (actWtCol >= 0) { expectedCols++; }
            if (levelCol >= 0) { expectedCols++; }
            if (idCol >= 0) { expectedCols++; }
            
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            
            String rawdata = null;
            
            // Read each line from the file, while there are still lines
            // in the file to read.
            while ((rawdata = br.readLine()) != null) {
                logger.trace("readLine : " + rawdata);
                //Tokenize the line read from the file.
                String [] tokens = rawdata.split(fieldSeparator);
                if (tokens.length < 2) {
                    logger.warn("skipping line : blank");
                    rejects.add("Bad input : blank line");
                    recordsProcessed++;
                    recordsRejected++;
                    continue;
                } else if (tokens.length < expectedCols) {
                    logger.error("skipping line : unexpected number of columns " +
                            "(expected=" + expectedCols + " current=" +
                            tokens.length + ")\n    " + rawdata);
                    rejects.add(String.format("Bad input : %s", rawdata));
                    recordsProcessed++;
                    recordsRejected++;
                    continue;
                }

                logger.trace(tokens[0] + tokens[1] + tokens[2]);
                
                //Create a new Wrestler object based on the data
                Wrestler w = new Wrestler();

                if (fNameCol >= 0) {
                    w.setFirstName(tokens[fNameCol].trim());
                }

                if (lNameCol >= 0) {
                    w.setLastName(tokens[lNameCol].trim());
                }

                if (tNameCol >= 0) {
                    w.setTeamName(tokens[tNameCol].trim());
                }

                if (classCol >= 0) {
                    w.setClassification(tokens[classCol].trim());
                }

                if (divCol >= 0) {
                    w.setAgeDivision(tokens[divCol].trim());
                }

                if (wtClassCol >= 0) {
                    w.setWeightClass(tokens[wtClassCol].trim());
                }

                if (actWtCol >= 0) {
                    w.setActualWeight(tokens[actWtCol].trim());
                }

                if (levelCol >= 0) {
                    w.setLevel(tokens[levelCol].trim());
                }

                if (idCol >= 0) {
                    w.setSerialNumber(tokens[idCol].trim());
                }

                recordsProcessed++;
                
                if (dao.addWrestler(w)) {
                    recordsAccepted++;
                } else {
                    recordsRejected++;
                    rejects.add(String.format("Duplicate : %s %s", w.getFirstName(),
                            w.getLastName()));
                    logger.warn("Duplicate : " + w.getFirstName() + " " +
                            w.getLastName());
                }
            }
            
            br.close();
        } catch (IOException ex) {
            JFrame mainFrame = BoutTimeApp.getApplication().getMainFrame();
            JOptionPane.showMessageDialog(mainFrame, "Error while handling the text file.\n\n" + ex,
                    "Text File Input Error", JOptionPane.ERROR_MESSAGE);
            logger.error(ex.getLocalizedMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        }

        return(new FileInputResult(recordsProcessed, recordsAccepted,
                recordsRejected, rejects));
    }

    /**
     * @deprecated 
     * @param dao
     */
    @SuppressWarnings("unchecked")
    public void getInputFromFile(Dao dao) {
        JFrame mainFrame = BoutTimeApp.getApplication().getMainFrame();

        JFileChooser infile = new JFileChooser();
        if (infile.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            Map config = new HashMap();
            config.put("firstName", "1");
            config.put("lastName", "2");
            config.put("teamName", "7");
            config.put("classification", "3");
            config.put("division", "4");
            config.put("weightClass", "5");
            config.put("actualWeight", "0");
            config.put("level", "6");
            config.put("serialNumber", "0");
            config.put("fieldSeparator", ":");
            addWrestlersFromFile(infile.getSelectedFile(), config, dao);

            dao.flush();
        }
    }

    /**
     * Get the input from the file based on the values in the 'config' parameter.
     * @param config Map of configuration values.
     * @param dao The data access object to use.
     * @return FileInputResult of the operation.
     */
    @SuppressWarnings("unchecked")
    public FileInputResult getInputFromFile(Map config, Dao dao) {
        String fileName = (String)config.get("fileName");
        File file = new File(fileName);
        if (!file.exists()) {
            String msg = "file does not exist (" + fileName + ")";
            logger.error("TextFileInput: " + msg);
            config.put("error", msg);
            return null;
        }

        logger.info("Getting input from file [" + fileName + "]");
        FileInputResult result = addWrestlersFromFile(file, config, dao);
        updateFileInputConfig(config, dao);
        dao.flush();

        return result;
    }

    /**
     * Update the file input configuration stored in the Dao.
     * @param config Map of configuration values.
     * @param dao The data access object to use.
     */
    private void updateFileInputConfig(Map config, Dao dao) {
        TextFileInputConfig fiConfig = dao.getTextFileInputConfig();

        fiConfig.setActualWeight((String)config.get("actualWeight"));
        fiConfig.setClassification((String)config.get("classification"));
        fiConfig.setDivision((String)config.get("division"));
        fiConfig.setFirstName((String)config.get("firstName"));
        fiConfig.setLastName((String)config.get("lastName"));
        fiConfig.setLevel((String)config.get("level"));
        fiConfig.setTeamName((String)config.get("teamName"));
        fiConfig.setSerialNumber((String)config.get("serialNumber"));
        fiConfig.setWeightClass((String)config.get("weightClass"));
        fiConfig.setFieldSeparator((String)config.get("fieldSeparator"));
    }
}

