/*
 * IO utility class for WeatherScraper
 * 
 * Copyright (C) 2012 Jesse Blum
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package weatherscraper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class URLConnectionIO {

    public static final String CHARSET = "ISO-8859-1";

    /**
     * Receives a string such as: 17:21 on 03 February 2012 and returns the
     * corresponding strings in an array
     *
     * @param toParse is the string to parse such as "17:21 on 03 February 2012"
     * @return a Map with the time/date components
     */
    public static Map parseDate(String toParse) {
        //System.out.println(toParse);
        Map<String, String> dates = new HashMap();
        String[] vals = toParse.split("on");
        if(vals.length <=1){
            return null;
        }
        String[] time = vals[0].split(":");
        dates.put("hour", time[0].trim());
        if (time.length > 1) {
            dates.put("minute", time[1].trim());
        }
        if (time.length > 2) {
            dates.put("second", time[2].trim());
        }
        String[] date = vals[1].split(" ");
        for (int i = 0; i < date.length; i++) {
            if ("".equals(date[i].trim())) {
                continue;
            }
            if (date[i].length() == 2) {
                dates.put("day", date[i].trim());
            } else if (date[i].length() == 4) {
                dates.put("year", date[i].trim());
            } else if (date[i].matches("\\w++")) {
                dates.put("month", date[i].trim());
            }
        }
        return dates;
    }

    /**
     * Returns A json formatted Stirng from a Map with date values
     * @param date is a Map of date values (see parseDate)
     * @return a json formatted date String 
     */
    public static String jsonFromDate(Map date) {
        if(null == date){
            return "";
        }
        Iterator<Entry> itr = date.entrySet().iterator();
        List<String> entries = new ArrayList();
        while (itr.hasNext()) {
            Entry<String, String> e = itr.next();
            if (e.getKey().equals("month")) {
                entries.add(jsonFormatString(e.getKey(), e.getValue()));
            } else {
                try {
                    entries.add(jsonFormatString(e.getKey(), Integer.parseInt(e.getValue())));
                } catch (NumberFormatException ex) {
                    // If it can't be parsed store the string 
                    entries.add(jsonFormatString(e.getKey(), e.getValue()));
                }
            }
        }
        StringBuilder out = new StringBuilder("\"datetime\":{");
        for (int i = 0; i < entries.size(); i++) {
            out.append(entries.get(i)).append(",");
        }
        out.deleteCharAt(out.length()-1);
        out.append("}");
        return out.toString();
    }

    /**
     * Returns a formatted concatenated String given two String
     * @param key is the String for the key
     * @param value is the String for the value
     * @return a String in the form of {"key":"value"}
     */
    public static String jsonFormatString(String key, String value) {
        if (null == key) {
            key = "unknown";
        }
        if (null == value) {
            value = "";
        }
        return String.format("\"%s\":\"%s\"", key, value.replaceAll("\"", "'"));
    }

    /**
     * Returns a formatted concatenated String given a String and a Long
     * @param key is the String for the key
     * @param value is the Long for the value
     * @return a String in the form of {"key":value}
     */
    public static String jsonFormatString(String key, Long value) {
        if (null == key) {
            key = "unknown";
        }
        if (null == value) {
            value = new Long(-1);
        }
        return String.format("\"%s\":%d", key, value);
    }

    /**
     * Returns a formatted concatenated String given a String and an Integer
     * @param key is the String for the key
     * @param value is the Integer for the value
     * @return a String in the form of {"key":value}
     */
    public static String jsonFormatString(String key, Integer value) {
        if (null == key) {
            key = "unknown";
        }
        if (null == value) {
            value = -1;
        }
        return String.format("\"%s\":%d", key, value);
    }
    
    /**
     * Returns a formatted concatenated String given a String and a Float
     * @param key is the String for the key
     * @param value is the Float for the value
     * @return a String in the form of {"key":value}
     */
    public static String jsonFormatString(String key, Float value, int numDecimals) {
        if (null == key) {
            key = "unknown";
        }
        if (null == value) {
            value = new Float(-1);
        }
        
        return String.format("\"%s\":%." + numDecimals + "f", key, value);
    }

    /**
     * Returns a String containing response information from an input stream
     * @param istream
     * @return A String with the response
     */
    public static String getResponseInfo(InputStream istream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(istream))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine.trim());
            }
        } catch (IOException ex) {
            Logger.getLogger(URLConnectionIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    /**
     * Post HTTP request
     *
     * @param urlIn is String for the Database to post to such as: http://localhost:5984/weathermeta2
     * @param content must be in format: String charset = "UTF-8"; String
     * query = String.format( "{\"%s\":..."}", URLEncoder.encode(param1,
     * charset), URLEncoder.encode(param2, charset) );
     * @return A String containing the return code and message
     */
    public static String post(String urlIn, String content) throws IOException {
        String retVal = null;
        if (!content.startsWith("{")) {
            content = "{" + content;
        }
        if (!content.endsWith("}")) {
            content += "}";
        }
        Logger.getLogger(URLConnectionIO.class.getName()).log(Level.FINE,
                "{0}, {1}", new Object[]{urlIn, content});
        OutputStream out = null;
        try {
            URL url = new URL(urlIn);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            //httpCon.setRequestProperty("Accept-Charset", CHARSET);
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("POST");
            httpCon.setRequestProperty("Content-Type", "application/json");
            //OutputStreamWriter out = new OutputStreamWriter( httpCon.getOutputStream());   
            out = httpCon.getOutputStream();
            //out.write(content.getBytes(CHARSET));
            out.write(content.getBytes());
            retVal = "" + httpCon.getResponseCode();
            if (Integer.parseInt(retVal) != HttpURLConnection.HTTP_CREATED) {
                retVal += " " + httpCon.getResponseMessage() + "\n" + getResponseInfo(httpCon.getErrorStream());
            } else {
                retVal += " " + httpCon.getResponseMessage() + "\n" + getResponseInfo(httpCon.getInputStream());
            }
        } catch (IOException ex) {
            Logger.getLogger(URLConnectionIO.class.getName()).log(Level.SEVERE, null, ex);
        } finally{            
            if(null != out){
                out.close();
            }
        }

        return retVal;
    }

    /**
     * Writes content to a file
     * @param file is a Path to where the file should be added
     * @param content is the content to add
     * @return true if successful and false otherwise
     */
    public static boolean writeFile(Path file, String content) {
        //System.out.println("URLConnectionIO.writeFile(" + file.toString() +", " + content + ")");        
        Logger.getLogger(URLConnectionIO.class.getName()).log(Level.INFO, file.toString());
        Charset charset = Charset.forName(CHARSET);
        try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
            writer.write(content, 0, content.length());
            return true;
        } catch (IOException x) {
            Logger.getLogger(URLConnectionIO.class.getName()).log(Level.SEVERE, null, x);
            return false;
        }
    }

    /**
     * Reads conetent from a URL to a list of Strings (one per line)
     * @param url is the url to crawl
     * @return a list of strings with the url content
     * @throws Exception
     */
    public static List<String> read(URL url) {
        System.out.println("URLConnectionIO.read(" + url+")");

        List<String> sb = new ArrayList();
        try {
            URLConnection yc =
                    url.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    yc.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                sb.add(inputLine.trim());
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(URLConnectionIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb;
    }

    /**
     * Reads conetent from a URL to a list of Strings (one per line) and writes 
     * the content to a file.
     * @param url is the url to crawl
     * @param outputPath is the path to store the file in (and can be null if no
     * file is to be stored)
     * @param fileName is the name to store the file under (and can be null if
     * no file is to be stored)
     * @return a stringbuilder with the url content
     * @throws Exception
     */
    public static List<String> read(URL url, String outputPath, String fileName) {
        List<String> sb = read(url);
        if (null != outputPath && null != fileName) {     // store file if outputPath is not null
            if (!(outputPath.endsWith("\\") || outputPath.endsWith("/"))) {
                outputPath += "\\";
            }

            Logger.getLogger(URLConnectionIO.class.getName()).log(Level.INFO,
                    "{0}, {1}", new Object[]{"URLConnectionIO.read(...) path: ", outputPath + fileName});
            Path file = Paths.get(URI.create(outputPath + fileName));
            writeFile(file, sb.toString());
        }

        return sb;
    }
}
