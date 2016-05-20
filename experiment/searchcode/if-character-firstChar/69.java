package parsers;

import Geolocatie.Coordinaten;
import Geolocatie.MultipleAdressesFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java_backend.Locatie;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author LeonHuzen
 */
public class JSON {
    /**
     * Ophalen van een enkele waarde van een JSON request
     * @param url De url waar de JSON van op te halen is.
     * @param requestMethod 
     * <UL>
     *  <LI>GET
     *  <LI>POST
     *  <LI>HEAD
     *  <LI>OPTIONS
     *  <LI>PUT
     *  <LI>DELETE
     *  <LI>TRACE
     * </UL> are legal, subject to protocol restrictions. The default method is GET.
     * @param key De op te halen key uit de JSON
     * @return Object, value van key
     * @throws IOException
     * @throws ParseException 
     */
    public Object getKeyFromJSONURL(URL url, String requestMethod, String key) throws IOException, ParseException {
        String JSONResult;
        
        JSONResult = GetJSONFromURL(url, requestMethod);
        
        Character firstChar;
        firstChar = (Character)JSONResult.charAt(0);
        JSONResult = firstChar.equals('[') ? JSONResult : "[0," + JSONResult + "]";
        
        JSONParser parser;
        KeyFinder finder;
        String result;
        parser = new JSONParser();
        finder = new KeyFinder();
        finder.setMatchKey(key);
        result = "";
        while(!finder.isEnd()) {
            parser.parse(JSONResult, finder, true);
            if (finder.isFound()) {
                finder.setFound(false);
                result += finder.getValue();
            }
        }
        return result;
    }
    
    /**
     * Ophalen van coordinaten van een JSON request
     * @param url De url waar de JSON van op te halen is.
     * @param requestMethod 
     * <UL>
     *  <LI>GET
     *  <LI>POST
     *  <LI>HEAD
     *  <LI>OPTIONS
     *  <LI>PUT
     *  <LI>DELETE
     *  <LI>TRACE
     * </UL> are legal, subject to protocol restrictions. The default method is GET.
     * @param Coordinaten De coordinaten uit de JSONRequest.
     * @return Object, value van key
     * @throws IOException
     * @throws ParseException
     * @throws MultipleAdressesFoundException 
     */
    public Coordinaten getCoordinatenFromJSONURL(URL url, String requestMethod)
            throws IOException, ParseException, MultipleAdressesFoundException {
        String JSONResult = GetJSONFromURL(url, requestMethod);
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(JSONResult);

        JSONObject jsonObject = (JSONObject) obj;

        Object found = jsonObject.get("found");
        if (!found.equals((long)1) || found == null) {
            throw new MultipleAdressesFoundException(url.getQuery());
        }
        // loop array
        JSONArray features = (JSONArray) jsonObject.get("features");
        JSONObject centroid = (JSONObject) features.get(0);
        JSONObject coordinates = (JSONObject) centroid.get("centroid");
        JSONArray latlong = (JSONArray) coordinates.get("coordinates");
        Iterator<Object> iterator = latlong.iterator();
        Coordinaten coordinaten = new Coordinaten(Double.NaN, Double.NaN);
        int c = 1;
        while (iterator.hasNext()) {
            if (c == 1) {
                coordinaten.Latitude = (Double) iterator.next();
            } else if (c == 2) {
                coordinaten.Longitude = (Double) iterator.next();
            }
            c++;
        }
        return coordinaten;
    }
    
    public Locatie getTZTPointFromJSONURL(URL url, String requestMethod)
            throws IOException, ParseException {
        Locatie tztpoint = new Locatie();
        String JSONResult = GetJSONFromURL(url, requestMethod);
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(JSONResult);

        JSONObject jsonObject = (JSONObject) obj;
 
        // loop array
        JSONArray features = (JSONArray) jsonObject.get("features");
        Iterator<Object> iterator;
        iterator = features.iterator();
        
        ArrayList<String> Stations = new ArrayList<>();
        Stations.add("Amersfoort");
        Stations.add("Amsterdam Centraal");
        Stations.add("Arnhem");
        Stations.add("Assen");
        Stations.add("Breda");
        Stations.add("'s  Hertogenbosch");
        Stations.add("Den Haag");
        Stations.add("Den Helder");
        Stations.add("Deventer");
        Stations.add("Eindhoven");
        Stations.add("Enschede");
        Stations.add("Gouda");
        Stations.add("Groningen");
        Stations.add("Haarlem");
        Stations.add("Leeuwarden");
        Stations.add("Leiden");
        Stations.add("Maastricht");
        Stations.add("Nijmegen");
        Stations.add("Roermond");
        Stations.add("Rotterdam");
        Stations.add("Schiphol");
        Stations.add("Utrecht");
        Stations.add("Venlo");
        Stations.add("Zwolle");
        Stations.add("Zutphen");
            
        int count = 0;
        JSONObject centroid = null;
        while (iterator.hasNext()) {
            String station = iterator.next().toString();
            for (int i = 0; i < Stations.size(); i++) {
                if (station.contains(Stations.get(i))) {
                    if (centroid == null) {
                        centroid = (JSONObject) features.get(count);
                    }
                }
            }
            count++;
        }
        
        //JSONObject centroid = (JSONObject) features.get(0);
        JSONObject coordinates = (JSONObject) centroid.get("centroid");
        JSONArray latlong = (JSONArray) coordinates.get("coordinates");
        iterator = latlong.iterator();
        Coordinaten coordinaten = new Coordinaten(Double.NaN, Double.NaN);
        int c = 1;
        while (iterator.hasNext()) {
            if (c == 1) {
                coordinaten.Latitude = (Double) iterator.next();
            } else if (c == 2) {
                coordinaten.Longitude = (Double) iterator.next();
            }
            c++;
        }
        tztpoint.setCoordinaten(coordinaten);
        return tztpoint;
    }

    /**
     * Haal JSON op uit URL (geef requestmethode mee) zie json.org voor de JSON specificatie.
     * @param url De url waar de JSON van op te halen is.
     * @param requestMethod 
     * <UL>
     *  <LI>GET
     *  <LI>POST
     *  <LI>HEAD
     *  <LI>OPTIONS
     *  <LI>PUT
     *  <LI>DELETE
     *  <LI>TRACE
     * </UL> are legal, subject to protocol restrictions. The default method is GET.
     * @return JSON
     * @throws IOException Afvangen omdat de JSON invalide kan zijn.
     */
    public String GetJSONFromURL(URL url, String requestMethod) throws IOException {
        HttpURLConnection conn;
        BufferedReader rd;
        String line, JSONResult = "";
        
        // Open connectie   
        conn = (HttpURLConnection) url.openConnection();
        if (conn.getRequestMethod().equals(""))
            conn.setRequestMethod(requestMethod);
        // Haal JSON op (zie json.org voor de specificatie)
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        // Voeg lijnen van JSON toe aan JSONResult
        while ((line = rd.readLine()) != null) {
            JSONResult += line;
        }
        // Sluit connectie
        rd.close();
        
        return JSONResult;
    }
}

