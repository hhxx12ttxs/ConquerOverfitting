package taseanalyzer.net;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import taseanalyzer.model.entities.TASEZipFileEntry;

/**
 * Utility class used to fetch TASE zip files from the TASE web site.
 * @author vainolo
 */
public class TASEFileFetcher {

    private String baseAddress = "http://www.tase.co.il/FileDistribution/PackTarget/";
    private String taseZipFilesPath;
    static final Logger logger = Logger.getLogger(TASEFileFetcher.class.getName());

    /**
     * Initialize a TASEFileFetcher using the default file path: "files" directory
     * under the current program's executing path.
     */
    public TASEFileFetcher() {
        taseZipFilesPath = "files";
    }

    /**
     * Initialize a TASEFileFetcher with a given destination path.
     * @param destinationPath the path where the TASEZipFiles should be saved.
     */
    public TASEFileFetcher(String destinationPath) {
        taseZipFilesPath = destinationPath;
    }


    /**
     * Fetch a period of TASEZipFiles starting from startPeriod and ending in
     * endPeriod, both of them inclusive.
     * If start date is less than or equal to end date nothing is done.
     * @param startDate the initial date to fetch.
     * @param endDate the final date to fetch.
     * @return List of downloaded file entries. An empty list if no file was
     * downloaded.
     */
    public List<TASEZipFileEntry> fetchPeriod(Date startDate, Date endDate)  {
        List<TASEZipFileEntry> downloadedFileEntries = new ArrayList<TASEZipFileEntry>();
        if(startDate.after(endDate))
            return downloadedFileEntries;
        
        Calendar c = Calendar.getInstance();
        Date currentDate = startDate;
        while (currentDate.getTime() < endDate.getTime()) {
            c.setTime(currentDate);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);

            // This is the format of the TASEZipFiles in the TASE web site.
            String filename = "Full" + yearToString(year) + monthToString(month) + dayToString(day) + "0.zip";
            // Check if there is already a file with the given name if delete it
            // if it's size is 0. Otherwise don't retrieve the file.
            File testFile = new File(filename);
            if(testFile.exists()) {
                if(testFile.length() == 0) {
                    testFile.delete();
                } else {
                    logger.info("Skipping file " + filename);
                    c.add(Calendar.DAY_OF_YEAR, 1);
                    currentDate = c.getTime();
                    continue;
                }
            }

            if(FileRetriever.download(baseAddress + filename, taseZipFilesPath + File.separator + filename)) {
                TASEZipFileEntry entry = new TASEZipFileEntry();
                entry.setFileDate(new Date(c.getTimeInMillis()));
                entry.setFilename(filename);
                downloadedFileEntries.add(entry);
            }

            c.add(Calendar.DAY_OF_YEAR, 1);
            currentDate = c.getTime();

            // Sleep half a second to give the web site time to recover :-)
            // Otherwise they may think this is a DOS attack.
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(TASEFileFetcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return downloadedFileEntries;
    }
    
    /**
     * Return a string representation of a year. Currently only transforms 
     * it into string.
     * @param year a year number.
     * @return string representation of the year number.
     */
    private String yearToString(int year) {
        return Integer.toString(year);
    }
    
    /**
     * Return a string representation of a month in the year, preceding it with
     * 0 if the month is less than 10.
     * @param month number of the month in the year.
     * @return a string representation of the month in the year with preceding
     * 0 added is month < 10.
     */
    private String monthToString(int month) {
        if (month >= 10) {
            return Integer.toString(month);
        } else {
            return "0" + Integer.toString(month);
        }
    }

    /**
     * Return a string representation of a day in the month, preceding it with 0 if
     * the day is less than 10.
     * @param day number of the day in the month.
     * @return a string representation of the day with preceding 0 added if day < 10.
     */
    private String dayToString(int day) {
        if (day >= 10) {
            return Integer.toString(day);
        } else {
            return "0" + Integer.toString(day);
        }
    }

    public static void main(String args[]) {
        Calendar c1 = Calendar.getInstance();
        c1.clear();
        c1.set(2009, 5, 20);
        Calendar c2 = Calendar.getInstance();
        Date d1 = c1.getTime();
        c2.clear();
        c2.set(2009, 6, 20);
        Date d2 = c2.getTime();
        TASEFileFetcher ret = new TASEFileFetcher();
        ret.fetchPeriod(d1, d2);
    }
}

