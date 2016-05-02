/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pb.servlet;

import com.pb.entity.Station;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Oriana
 */
public class GeoMatcherOpt extends HttpServlet {

   byte[] header;
   byte[] navbar;
   byte[] body;
   byte[] stat;
   byte[] footer;
   static final String TAB = "\t";
   double lat;
   double lng;

   @Override
   public void init(ServletConfig config) throws ServletException {
//create all the static data here
      StringBuilder sb = new StringBuilder(); // better to initialize the StringBuffer with some size to improve performance
      sb.append("<html>");
      sb.append("<body>");
      header = sb.toString().getBytes();
      sb.delete(0, sb.length());
      sb.append("<pre>").
	      append("distanza" + ServletDownload.ID + TAB
	      + ServletDownload.BRAND + TAB
	      + ServletDownload.ID2 + TAB
	      + ServletDownload.NAME + TAB
	      + ServletDownload.ADDRESS + TAB
	      + ServletDownload.ZIP + TAB
	      + ServletDownload.CITY + TAB
	      + ServletDownload.PROVINCE + TAB
	      + ServletDownload.REGION + TAB
	      + ServletDownload.PHONE + TAB
	      + ServletDownload.SERVICES + TAB
	      + ServletDownload.FUELS + TAB
	      + ServletDownload.STATUS + TAB
	      + ServletDownload.LAT + TAB
	      + ServletDownload.LNG);
      navbar = sb.toString().getBytes();

      sb.delete(0, sb.length());
      sb.append("</pre>");
      sb.append("</body>");
      sb.append("</html>");
      footer = sb.toString().getBytes();

      sb.delete(0, sb.length());


// do same for navbar if its data is static
// do same for footer if its data is static
   }

   @Override
   public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html");
      ServletOutputStream out = response.getOutputStream();
      out.write(header);
      out.flush();
      out.write(navbar);
      out.flush();
// write dynamic data here
      long timeStart = System.currentTimeMillis();
      try {
	 //riga di testo in input, estrazione di 2 double (lat e long)
	 String coorParameter = request.getParameter("coordinates");
	 String maxDistanceParameter = request.getParameter("maxDistance");

	 try {
	    if (true
		    && (coorParameter != null
		    && !coorParameter.isEmpty())
		    && (maxDistanceParameter != null
		    && (!maxDistanceParameter.isEmpty()))) {

	       Double maxDistance = Double.parseDouble(maxDistanceParameter);
	       String[] coordinates = coorParameter.split("\\n");

	       Pattern pattern = Pattern.compile(".*[,;| ] *(\\d+\\.\\d+) *[,;| ] *(\\d+\\.\\d+) *[,;| ].*");

	       //5 stazioni piů vicine a lat e long
	       String namedQuery = "Station.findByNorthEastSouthWest";
	       //set max query results
	       int maxNearStations = 5;
	       ArrayList<Double> distances = new ArrayList();

	       if (coordinates.length > 0) {
		  for (int i = 0; i < coordinates.length; i++) {

		     //elimino la newline alla fine della riga
		     coordinates[i] = coordinates[i].replaceAll("(\\r|\\n)", "");
		     Matcher matcher = pattern.matcher(" " + coordinates[i] + " ");
		     out.println("");
		     out.println("Riga " + (i + 1) + ": " + coordinates[i]);
		     if (matcher.matches()) {

			double latitude = Double.parseDouble(matcher.group(1));
			double longitude = Double.parseDouble(matcher.group(2));
			this.lat = latitude;
			this.lng = longitude;
			out.println("Lat: " + lat + "; Lng: " + lng);
			List<Station> nearestStations = MiddleServer.nearestStations(latitude, longitude, maxNearStations, namedQuery);

			if (nearestStations.size() > 0) {
			   for (Station s : nearestStations) {

			      Double distance = distanceCalculator(s.getLat(), s.getLng());

			      if (distance > maxDistance / 1000) {

				 StringBuilder sb = new StringBuilder();
				 sb.
					 append(new DecimalFormat("0.000").format(distance)).append(TAB).
					 append(s.getId()).append(TAB).
					 append(s.getBrand()).append(TAB).
					 append(s.getId2()).append(TAB).
					 append(s.getName()).append(TAB).
					 append(s.getAddress()).append(TAB).
					 append(s.getZip()).append(TAB).
					 append(s.getCity()).append(TAB).
					 append(s.getProvince()).append(TAB).
					 append(s.getRegion()).append(TAB).
					 append(s.getPhone()).append(TAB).
					 append(s.getServices()).append(TAB).
					 append(s.getFuels()).append(TAB).
					 append(s.getStatus()).append(TAB).
					 append(s.getLat()).append(TAB).
					 append(s.getLng()).append(TAB);
				 body = sb.toString().getBytes();
				 out.write(body);
				 out.flush();
			      }
			   }
			}
			Station first = nearestStations.get(0);
			distances.add(distanceCalculator(first.getLat(), first.getLng()));
		     }
		  }
		  //--STATISTICHE--
		  //max distanza trovata (la distanza piů alta tra tutti i blocchi)
		  Double max = Collections.max(distances);
		  Double min = Collections.min(distances);
		  //media?
		  Double sum = new Double(0);

		  for (Double d : distances) {
		     sum += d;
		  }

		  Double average = sum / distances.size();

		  int countRow = coordinates.length;

		  Double error = average * countRow;
		  //somma degli errori

		  out.println("------------STATISTICHE----------- ");
		  out.println("Record analizzati: " + countRow);
		  out.println("Risultato massimo: " + new DecimalFormat("0.000").format(max));
		  out.println("Risultato minimo: " + new DecimalFormat("0.000").format(min));
		  out.println("Risultato medio: " + new DecimalFormat("0.000").format(average));
		  out.println("Errore totale stimato: " + new DecimalFormat("0.000").format(error));
	       }

	       long timeEnd = System.currentTimeMillis();
	       long timeElapsed = timeEnd - timeStart;
	       out.println("Time Elapsed: " + timeElapsed + " ms");
	    }
	 } catch (Exception e) {
	 }
      } finally {
	 out.flush();
	 out.write(footer);
	 out.flush();
	 out.close();
      }


   }

   public double distanceCalculator(double lat, double lng) {
      double tmp =
	      Math.cos(Math.toRadians(lng - this.lng))
	      * Math.cos(Math.toRadians(this.lat))
	      * Math.cos(Math.toRadians(lat))
	      + Math.sin(Math.toRadians(this.lat))
	      * Math.sin(Math.toRadians(lat));
      tmp = tmp > 1 ? 1 : tmp < -1 ? -1 : tmp;
      tmp = 6372 * Math.acos(tmp);
      return tmp;
   }
}
