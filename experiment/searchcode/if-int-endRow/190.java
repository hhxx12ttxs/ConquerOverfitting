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
import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import bouttime.model.Wrestler;
import org.apache.poi.ss.usermodel.*;
import bouttime.mainview.BoutTimeApp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * This class retrieves Wrestler records from a MS Excel formatted file.
 */
public class ExcelFileInput implements FileInput {
    static Logger logger = Logger.getLogger(ExcelFileInput.class);

    public ExcelFileInput() {}
    
    /**
     * Input wrestlers from a MS Excel formatted file.
     *
     * @param file File to read data from
     * @param config Map of config parameters for column indexes, start and stop
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

        try {
            int sheetNumber = Integer.parseInt((String)config.get("sheet")) - 1;
            int startRow = Integer.parseInt((String)config.get("startRow")) - 1;
            int endRow = Integer.parseInt((String)config.get("endRow"));
            int fNameCol = Integer.parseInt((String)config.get("firstName")) - 1;
            int lNameCol = Integer.parseInt((String)config.get("lastName")) - 1;
            int tNameCol = Integer.parseInt((String)config.get("teamName")) - 1;
            int classCol = Integer.parseInt((String)config.get("classification")) - 1;
            int divCol = Integer.parseInt((String)config.get("division")) - 1;
            int wtClassCol = Integer.parseInt((String)config.get("weightClass")) - 1;
            int actWtCol = Integer.parseInt((String)config.get("actualWeight")) - 1;
            int levelCol = Integer.parseInt((String)config.get("level")) - 1;
            int idCol = Integer.parseInt((String)config.get("serialNumber")) - 1;
            InputStream inp = new FileInputStream(file);
            Workbook wb = WorkbookFactory.create(inp);
            Sheet sheet = wb.getSheetAt(sheetNumber);

            logger.info("Excel File Input configuration :" +
                    "\n    sheet=" + sheetNumber +
                    "\n    startRow=" + startRow +
                    "\n    endRow=" + endRow +
                    "\n    first=" + fNameCol +
                    "\n    last=" + lNameCol +
                    "\n    team=" + tNameCol +
                    "\n    class=" + classCol +
                    "\n    div=" + divCol + "\n" +
                    "\n    wtClass=" + wtClassCol +
                    "\n    actWt=" + actWtCol +
                    "\n    level=" + levelCol +
                    "\n    id=" + idCol);

            int i = startRow;
            while (i < endRow) {
                Wrestler w = new Wrestler();
                Row row = sheet.getRow(i);

                if (fNameCol >= 0) {
                    String fName = row.getCell(fNameCol).getRichStringCellValue().getString();

                    // Should always have a first name, so this is a
                    // reasonable check for the end of data.
                    if (fName.isEmpty())
                        break;  // found end of data

                    w.setFirstName(fName.trim());
                }

                if (lNameCol >= 0) {
                    String lName = row.getCell(lNameCol).getRichStringCellValue().getString();

                    // Should always have a last name, so this is a
                    // reasonable check for the end of data.
                    if (lName.isEmpty())
                        break;  // found end of data

                    w.setLastName(lName.trim());
                }

                if (divCol >= 0) {
                    String div;
                    if (row.getCell(divCol).getCellType() == Cell.CELL_TYPE_STRING) {
                        div = row.getCell(divCol).getRichStringCellValue().getString();
                    } else {
                        div = Long.valueOf(Double.valueOf(row.getCell(divCol).getNumericCellValue()).longValue()).toString();
                    }

                    w.setAgeDivision(div.trim());
                }

                if (wtClassCol >= 0) {
                    String wtClass;
                    if (row.getCell(wtClassCol).getCellType() == Cell.CELL_TYPE_STRING) {
                        wtClass = row.getCell(wtClassCol).getRichStringCellValue().getString();
                    } else {
                        wtClass = Long.valueOf(Double.valueOf(row.getCell(wtClassCol).getNumericCellValue()).longValue()).toString();
                    }

                    w.setWeightClass(wtClass.trim());
                }

                if (actWtCol >= 0) {
                    String actWt;
                    if (row.getCell(actWtCol).getCellType() == Cell.CELL_TYPE_STRING) {
                        actWt = row.getCell(actWtCol).getRichStringCellValue().getString();
                    } else {
                        actWt = Long.valueOf(Double.valueOf(row.getCell(actWtCol).getNumericCellValue()).longValue()).toString();
                    }

                    w.setActualWeight(actWt.trim());
                }

                if (classCol >= 0) {
                    String classification;
                    if (row.getCell(classCol).getCellType() == Cell.CELL_TYPE_STRING) {
                        classification = row.getCell(classCol).getRichStringCellValue().getString();
                    } else {
                        classification = Long.valueOf(Double.valueOf(row.getCell(classCol).getNumericCellValue()).longValue()).toString();
                    }
                    
                    w.setClassification(classification.trim());
                }

                if (tNameCol >= 0) {
                    String tName;
                    if (row.getCell(tNameCol).getCellType() == Cell.CELL_TYPE_STRING) {
                        tName = row.getCell(tNameCol).getRichStringCellValue().getString();
                    } else {
                        tName = Long.valueOf(Double.valueOf(row.getCell(tNameCol).getNumericCellValue()).longValue()).toString();
                    }

                    w.setTeamName(tName.trim());
                }

                if (idCol >= 0) {
                    String id;
                    if (row.getCell(idCol).getCellType() == Cell.CELL_TYPE_STRING) {
                        id = row.getCell(idCol).getRichStringCellValue().getString();
                    } else {
                        id = Long.valueOf(Double.valueOf(row.getCell(idCol).getNumericCellValue()).longValue()).toString();
                    }

                    w.setSerialNumber(id.trim());
                }

                if (levelCol >= 0) {
                    String level;
                    if (row.getCell(levelCol).getCellType() == Cell.CELL_TYPE_STRING) {
                        level = row.getCell(levelCol).getRichStringCellValue().getString();
                    } else {
                        level = Long.valueOf(Double.valueOf(row.getCell(levelCol).getNumericCellValue()).longValue()).toString();
                    }
                    
                    w.setLevel(level.trim());
                }

                recordsProcessed++;

                if (dao.addWrestler(w)) {
                    recordsAccepted++;
                } else {
                    recordsRejected++;
                    rejects.add(String.format("%s %s", w.getFirstName(),
                            w.getLastName()));
                    logger.warn("Duplicate: " + w.getFirstName() + " " +
                            w.getLastName());
                }

                i++;
            }
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException ife) {
            JFrame mainFrame = BoutTimeApp.getApplication().getMainFrame();
            JOptionPane.showMessageDialog(mainFrame, "Error while handling the spreadsheet file.\n\n" +
                    "This is not a file in an Excel file.", "Spreadsheet file error",
                    JOptionPane.ERROR_MESSAGE);
            logger.error(ife.getLocalizedMessage() + "\n" + Arrays.toString(ife.getStackTrace()));
        } catch (Exception e) {
            JFrame mainFrame = BoutTimeApp.getApplication().getMainFrame();
            JOptionPane.showMessageDialog(mainFrame, "Error while handling the spreadsheet file.\n\n" + e,
                    "Spreadsheet file error", JOptionPane.ERROR_MESSAGE);
            logger.error(e.getLocalizedMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }

        return(new FileInputResult(recordsProcessed, recordsAccepted,
                recordsRejected, rejects));
    }

    /**
     * Get the input from the file based on default configuration parameters.
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
            config.put("classification", "6");
            config.put("division", "3");
            config.put("weightClass", "4");
            config.put("actualWeight", "5");
            config.put("level", "0");
            config.put("serialNumber", "8");
            config.put("sheet", "1");
            config.put("startRow", "2");
            config.put("endRow", "100");
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
            logger.error("ExcelFileInput: " + msg);
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
        ExcelFileInputConfig fiConfig = dao.getExcelFileInputConfig();

        fiConfig.setActualWeight((String)config.get("actualWeight"));
        fiConfig.setClassification((String)config.get("classification"));
        fiConfig.setDivision((String)config.get("division"));
        fiConfig.setFirstName((String)config.get("firstName"));
        fiConfig.setLastName((String)config.get("lastName"));
        fiConfig.setLevel((String)config.get("level"));
        fiConfig.setTeamName((String)config.get("teamName"));
        fiConfig.setSerialNumber((String)config.get("serialNumber"));
        fiConfig.setWeightClass((String)config.get("weightClass"));
        fiConfig.setSheet((String)config.get("sheet"));
        fiConfig.setStartRow((String)config.get("startRow"));
        fiConfig.setEndRow((String)config.get("endRow"));
    }
}

