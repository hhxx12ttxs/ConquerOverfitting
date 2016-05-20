/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package domain.emdat;

import emdat.EmdatDao;
import emdat.Emdat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.CountryService;

/**
 *
 * @author wb385924
 */
public class EmdatCsvReader {

    private EmdatDao dao = new EmdatDao();
    private static final Logger log = Logger.getLogger(EmdatCsvReader.class.getName());

    public static void main(String[] args) {
        new EmdatCsvReader().handleFile(new File("C:\\Users\\wb385924\\Documents\\emdat\\storm_affected.csv"));
    }

    public void readFromRoot(String rootDirectory) {
        File rd = new File(rootDirectory);
        File[] files = rd.listFiles();

        for (File f : files) {
            handleFile(f);
        }
    }

    private void handleFile(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\,");
                if (parts.length > 1) {
                    String iso3 = parts[0];
                    int countryId = CountryService.get().getId(iso3);
                    if (countryId != -1 && parts != null && parts.length > 0) {
                        iso3 = parts[0];
                        int startYear = 1970;
                        for (int i = 1; i < parts.length; i++) {
                            int thisYear = startYear + i;
                            String fname = f.getName().substring(0, f.getName().indexOf("."));
                            log.log(Level.INFO, "filename is {0} ", fname);
                            if( (parts[i] != null) && (parts[i].trim().length() > 0))
                            {
                                log.log(Level.INFO, "inserting {0} {1} {2} {3}  ", new Object[]{countryId, thisYear, Double.parseDouble(parts[i]), Emdat.valueOf(fname)});
                                dao.insertEmdatData(countryId, thisYear, Double.parseDouble(parts[i]), Emdat.valueOf(fname));
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(EmdatCsvReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EmdatCsvReader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

