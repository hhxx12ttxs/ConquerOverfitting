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
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Oriana
 */
public class GeoMatcher extends HttpServlet {

   /**
    * Processes requests for both HTTP
    * <code>GET</code> and
    * <code>POST</code> methods.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   static final String TAB = "\t";
   double lat;
   double lng;

   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
	   throws ServletException, IOException {
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter out = response.getWriter();

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
	       out.println("<pre>");
	       out.println(""
		       + "distanza" + TAB
		       + ServletDownload.ID + TAB
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
			      
			      if (distance > maxDistance/1000) {
				 
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
				 out.println(sb);
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
	       out.println("</pre>");
	       long timeEnd = System.currentTimeMillis();
	       long timeElapsed = timeEnd - timeStart;
	       out.println("Time Elapsed: " + timeElapsed + " ms");
	    }
	 } catch (Exception e) {
	 }
      } finally {

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

   // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
   /**
    * Handles the HTTP
    * <code>GET</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
	   throws ServletException, IOException {
      processRequest(request, response);
   }

   /**
    * Handles the HTTP
    * <code>POST</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
	   throws ServletException, IOException {
      processRequest(request, response);
   }

   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "Short description";
   }// </editor-fold>
}

