package net.kodra.supereasy.traffic.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;

import net.kodra.supereasy.traffic.shared.Departure;
import net.kodra.supereasy.traffic.shared.Station;


/**
 * 
**/
public class QueueWorkerServlet extends HttpServlet
{

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = Logger.getLogger(QueueWorkerServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
  {
    LOGGER.log(Level.INFO, request.toString());

    updateInfo(request.getParameter(Station.PARAMETER_NAME));

    response.setStatus(HttpServletResponse.SC_OK);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
  {
    LOGGER.log(Level.INFO, request.toString());

    Enumeration<String> parameterNameEnumeration = request.getParameterNames();
    while (parameterNameEnumeration.hasMoreElements())
    {
      String parameterName = parameterNameEnumeration.nextElement();
      LOGGER.log(Level.INFO, parameterName + " = " + request.getParameter(parameterName));
    }

    updateInfo(request.getParameter(Station.PARAMETER_NAME));

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private void updateInfo(String stationName)
  {
    QuotaService qs = QuotaServiceFactory.getQuotaService();
    long first = qs.getCpuTimeInMegaCycles();

    List<Departure> departureList = MvgApiFactory.getCurrentSchedules(stationName);

    Station station = TrafficManager.loadStation(stationName);
    if (station == null)
      station = new Station(stationName);
    else
      TrafficManager.removeDepartures(station.getDepartureKeyList());

    station.setDepartureList(departureList);
    TrafficManager.saveStation(station);

    long middle = qs.getCpuTimeInMegaCycles();
    TrafficManager.processDepartures(departureList);
    long last = qs.getCpuTimeInMegaCycles();

    LOGGER.info("processing time: " + qs.convertMegacyclesToCpuSeconds(middle-first) + " " + qs.convertMegacyclesToCpuSeconds(last-middle));
  }

}

