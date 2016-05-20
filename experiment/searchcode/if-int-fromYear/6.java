/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package oldv1;

/**
 *
 * @author wb385924
 */
import util.ResponseConstants;
import emdat.EmdatDao;
import emdat.Emdat;
import emdat.EmdatsSingleton;
import export.DataFormatHelper;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import service.CountryService;

  // The Java class will be hosted at the URI path "/helloworld"
   @Path("/emdat")
   public class EmdatResource {
      private static Logger log=  Logger.getLogger(EmdatResource.class.getName());
      
      @GET
      @Path("/{emdat}/{fromYear}/{toYear}/{iso3:[a-zA-Z]{3}}")
      @Produces(MediaType.APPLICATION_JSON)
      public Response getDrainages( @PathParam("emdat") String emdat,  @PathParam("fromYear") int fromYear, @PathParam("toYear") int toYear, @PathParam("iso3") String iso3){
          int countryId = CountryService.get().getId(iso3);
          Emdat emdattype = EmdatsSingleton.get(emdat);
          log.log(Level.INFO,"emdat type is {0} countryId is {1}", new Object[]{emdat, countryId});
          if(emdattype == null || countryId == -1){
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
          }
          String response = DataFormatHelper.formatData(MediaType.APPLICATION_JSON,new EmdatDao().getEmdatData(countryId, fromYear, toYear, emdattype));
          return Response.ok(response, MediaType.APPLICATION_JSON).build();
      }

       @GET
      @Path("/combined/{emdat}/{fromYear}/{toYear}/{iso3:[a-zA-Z]{3}}")
      @Produces(MediaType.APPLICATION_JSON)
      public Response getCombinedEmdats( @PathParam("emdat") String emdat,  @PathParam("fromYear") int fromYear, @PathParam("toYear") int toYear, @PathParam("iso3") String iso3){
          int countryId = CountryService.get().getId(iso3);
          Emdat[] emdattype = EmdatsSingleton.getCombined(emdat);
          log.log(Level.INFO,"emdat type is {0} countryId is {1}", new Object[]{emdat, countryId});
          if(emdattype == null || countryId == -1){
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
          }
          String response = DataFormatHelper.formatData(MediaType.APPLICATION_JSON,new EmdatDao().getEmdatDataCombined(countryId, fromYear, toYear, emdattype[0],emdattype[1] ));
          return Response.ok(response, MediaType.APPLICATION_JSON).build();
      }

      @GET
      @Path("/{fromYear}/{toYear}/{iso3:[a-zA-Z]{3}}")
      @Produces(MediaType.APPLICATION_JSON)
      public Response getAllEmdatsForCountry( @PathParam("fromYear") int fromYear, @PathParam("toYear") int toYear, @PathParam("iso3") String iso3){

          int countryId = CountryService.get().getId(iso3);

          String response = DataFormatHelper.formatData(MediaType.APPLICATION_JSON,new EmdatDao().getEmdatData(countryId, fromYear, toYear));
          return Response.ok(response, MediaType.APPLICATION_JSON).build();
      }
  }

