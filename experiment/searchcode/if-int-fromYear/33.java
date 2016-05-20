
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.StringTokenizer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author user
 */
public class CPIConverter {
   
   private double [] cpi;
   private static final int FIRST_YEAR = 1913;
   private static final int LAST_YEAR = 2013;
   
   
   public void read(String location) throws MalformedURLException, IOException 
   {
      cpi = new double[LAST_YEAR - FIRST_YEAR + 1];
      URL cpiURL = new URL(location);
      InputStream cpiIn = cpiURL.openStream();
      Scanner scan = new Scanner(cpiIn);
      while (scan.hasNextLine())
      {
         StringTokenizer token = new StringTokenizer(scan.nextLine(), " ");
         int year = Integer.parseInt(token.nextToken());
         if (year > LAST_YEAR)
            throw new IOException("Year should not exceed 2013");
         cpi[year - FIRST_YEAR] = Double.parseDouble(token.nextToken());
      }
   }

   public double equivalentAmount(double amount, int fromYear, int toYear) 
   {
      return amount * cpi[toYear - FIRST_YEAR] / cpi[fromYear - FIRST_YEAR];
   }
   
}

