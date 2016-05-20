/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resources.v1;

import com.google.gson.Gson;
import com.sun.jersey.spi.resource.Singleton;
import dao.GeoDao;
import database.DBUtils;
import java.sql.Connection;
import java.util.HashMap;

import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import util.ResponseConstants;
import service.CountryService;
import service.DerivedStatsService;
import service.WebService;
import shapefileloader.gcm.P_GcmConfig;
import shapefileloader.gcm.P_GcmConfigAreaDao;
import shapefileloader.gcm.P_GcmStatsProperties;
import util.RequestUtil;
import service.WebService.Delivery;

/**
 *
 * @author wb385924
 */



@Singleton
@Path("/v1/country")
public class P_CountryDataResource {

    
    private static final Logger log = Logger.getLogger(P_CountryDataResource.class.getName());

    @GET
    @Path("/bbox/kml/{iso3:\\w{3}}")
    @Produces(RequestUtil.APP_KML)
    public String getBboxKmlCountryBoundary(@PathParam("iso3") String iso3) {
        int countryId = -1;
        if (iso3 != null && iso3.trim().length() == 3) {
            Connection c = DBUtils.getConnection();
            try {
                countryId = CountryService.get().getId(iso3);
                String kml = GeoDao.getGeometryBboxAsKML(c, "boundary", "shape", "area_id", countryId);
                return kml;
            } finally {
                DBUtils.close(c);
            }
        }
        return "<kml></kml>";
    }




    @GET
    // The Java method will produce content identified by the MIME Media
    // type "text/plain"
    @Produces("application/json")
    public String getClichedMessage() {

        return new Gson().toJson(CountryService.get().getCountries());

    }

    

    @GET
    @Path("/{tempagg}/{gcm:\\w{8,15}}/{scenario:(?i)(20c3m|a2|b1)}/{stat}/{fyear}/{tyear}/{longitude},{latitude}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMonthlyData(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("latitude") double latitude,
            @PathParam("longitude") double longitude) {
        log.info("getting lat lon data");
        int countryId = DerivedStatsService.get().getCountryId(latitude, longitude);
        P_GcmStatsProperties ds = P_GcmStatsProperties.getInstance();

        P_GcmStatsProperties.climatestat stat = ds.getClimateStat(statName);
        P_GcmStatsProperties.gcm gcm = ds.getGcm(gcmName);
        P_GcmStatsProperties.scenario scenario = ds.getScenario(scenarioName);

        P_GcmConfig config = new P_GcmConfig(WebService.get().getStatType(tempagg), gcm, scenario, stat, fyear, tyear);
        if (!config.isCompleteIgnoringAreaValueMonth()) {
            return ResponseConstants.INVALID_PARAMS;
        }
        config.setAreaId(countryId);

        HashMap<Integer, Double> data = P_GcmConfigAreaDao.get().getAreaDataForTime(config, WebService.get().isAnnual(tempagg));
        Gson gson = new Gson();
        return gson.toJson(data);

    }

   

    @GET
    @Path("/kml/{threshold}/{iso3}")
    @Produces(RequestUtil.APP_KML)
    public String getKmlSimplifiedBoundary(@PathParam("iso3") String iso3, @PathParam("threshold") double threshold) {
        int countryId = -1;
        if (iso3 != null && iso3.trim().length() == 3) {
            Connection c = DBUtils.getConnection();
            try {
                countryId = CountryService.get().getId(iso3);
                String kml = GeoDao.getSimplifiedGeometryAsKML(c, "boundary", "shape", "area_id", countryId, threshold);
                StringBuilder sb = new StringBuilder();
                sb.append("<kml><Document><Placemark>");
                sb.append("<name>");
                sb.append(iso3);
                sb.append("</name>");
                sb.append(kml);
                sb.append("</Placemark></Document></kml>");
                return sb.toString();
            } finally {
                DBUtils.close(c);
            }
        }
        return "<kml></kml>";
    }

    @GET
    @Path("/kml/{iso3}")
    @Produces(RequestUtil.APP_KML)
    public String getKmlCountryBoundary(@PathParam("iso3") String iso3) {
        int countryId = -1;
        if (iso3 != null && iso3.trim().length() == 3) {
            Connection c = DBUtils.getConnection();
            try {
                countryId = CountryService.get().getId(iso3);
                String kml = GeoDao.getGeometryAsKML(c, "boundary", "shape", "area_id", countryId);
                StringBuilder sb = new StringBuilder();
                sb.append("<kml><Document><Placemark>");
                sb.append("<name>");
                sb.append(iso3);
                sb.append("</name>");
                sb.append(kml);
                sb.append("</Placemark></Document></kml>");
                return sb.toString();
            } finally {
                DBUtils.close(c);
            }
        }
        return "<kml></kml>";
    }

    @GET
    @Path("/kmlpart/{iso3}")
    @Produces(MediaType.APPLICATION_XML)
    public String getKmlPartBoundary(@PathParam("iso3") String iso3) {
        int countryId = -1;
        if (iso3 != null && iso3.trim().length() == 3) {
            Connection c = DBUtils.getConnection();
            try {
                countryId = CountryService.get().getId(iso3);
                String kml = GeoDao.getGeometryAsKML(c, "boundary", "shape", "area_id", countryId);
                StringBuilder sb = new StringBuilder();
//                sb.append("<name>");
//                sb.append(iso3);
//                sb.append("</name>");
                sb.append(kml);
                return sb.toString();
            } finally {
                DBUtils.close(c);
            }
        }
        return "<kml></kml>";
    }

    @GET
    @Path("/kmlpart/{threshold}/{iso3}")
    @Produces(MediaType.APPLICATION_XML)
    public String getKmlPartSimplifiedBoundary(@PathParam("iso3") String iso3, @PathParam("threshold") double threshold) {
        int countryId = -1;
        if (iso3 != null && iso3.trim().length() == 3) {
            Connection c = DBUtils.getConnection();
            try {
                countryId = CountryService.get().getId(iso3);
                String kml = GeoDao.getSimplifiedGeometryAsKML(c, "boundary", "shape", "area_id", countryId, threshold);
                StringBuilder sb = new StringBuilder();
//                sb.append("<name>");
//                sb.append(iso3);
//                sb.append("</name>");
                sb.append(kml);
                return sb.toString();
            } finally {
                DBUtils.close(c);
            }
        }
        return "<kml></kml>";
    }

    // ================= gcm scneario all vars =======================
    @GET
    @Path("/{tempagg}/{gcm:\\w{8,15}}/{scenario:(?i)(20c3m|a2|b1)}/{fyear}/{tyear}/{iso3}")
    public Response getGcmScenarioAllVars(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("scenario") String scenarioName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        log.info("getting gcm scenario all vars");

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getGcmScenarioAllVariables(gcmName, scenarioName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // ================= download gcm scneario all vars =======================
    @GET
    @Path("/dl/{tempagg}/{gcm:\\w{8,15}}/{scenario:(?i)(20c3m|a2|b1)}/{fyear}/{tyear}/{iso3}")
    public Response downloadGcmScenarioAllVars(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("scenario") String scenarioName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        log.info("getting gcm scenario all vars");

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getGcmScenarioAllVariables(gcmName, scenarioName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);
    }

    // ================= gcm all vars =======================
    @GET
    @Path("/{tempagg}/{gcm:\\w{8,15}}/{fyear}/{tyear}/{iso3}")
    public Response getGcmAllVars(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        log.info("getting gcm scenario all vars");

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getGcmAllVariables(gcmName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // ================= download gcm all vars =======================
    @GET
    @Path("/dl/{tempagg}/{gcm:\\w{8,15}}/{fyear}/{tyear}/{iso3}")
    public Response downloadGcmAllVars(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        log.info("getting gcm scenario all vars");

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getGcmAllVariables(gcmName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);
    }

    // ================= gcm and scenario single var =======================
    @GET
    @Path("/{tempagg}/{gcm:\\w{8,15}}/{scenario:(?i)(20c3m|a2|b1)}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response getGcmScenario(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        log.info("getting gcm scenario");

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getGcmScenario(gcmName, scenarioName, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // =================  download gcm and scenario single var =======================
    @GET
    @Path("/dl/{tempagg}/{gcm:\\w{8,15}}/{scenario:(?i)(20c3m|a2|b1)}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response downloadGcmScenario(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {


        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getGcmScenario(gcmName, scenarioName, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(iso3), mediaType, Delivery.DOWNLOAD);
    }

    // ===================== scenario single var  ============================//
    @GET
    @Path("/{tempagg}/{scenario:(?i)(20c3m|a2|b1)}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response getScenarioData(
            @PathParam("tempagg") String tempagg,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {
        log.info("getting single scenario ");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getSingleScenarioData(scenarioName, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, WebService.Delivery.WEB);//getScenarioDataDelegate(scenarioName, statName, fyear, tyear, iso3, mediaType);
    }

    // ===================== download scenario single var ============================//
    @GET
    @Path("/dl/{tempagg}/{scenario:(?i)(20c3m|a2|b1)}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response downloadScenarioData(
            @PathParam("tempagg") String tempagg,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getSingleScenarioData(scenarioName, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, WebService.Delivery.DOWNLOAD);//getScenarioDataDelegate(scenarioName, statName, fyear, tyear, iso3, mediaType);
    }

    // ===================== model single var   ============================//
    @GET
    @Path("/{tempagg}/{gcm:\\w{8,15}}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response getGcmData(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {
        log.info("getting single gcm");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getSingleGcmData(gcmName, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, WebService.Delivery.WEB);//getScenarioDataDelegate(scenarioName, statName, fyear, tyear, iso3, mediaType);
    }

    // ===================== download model single var ============================//
    @GET
    @Path("/dl/{tempagg}/{gcm:\\w{8,15}}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response downloadGcmData(
            @PathParam("tempagg") String tempagg,
            @PathParam("gcm") String gcmName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getSingleGcmData(gcmName, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, WebService.Delivery.DOWNLOAD);//getScenarioDataDelegate(scenarioName, statName, fyear, tyear, iso3, mediaType);
    }

    // =================  ensemble scenario percentile var =============================
    @GET
    @Path("/{tempagg}/ensemble/{scenario:(?i)(20c3m|a2|b1)}/{percentile}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response getEnsembleScenario(
            @PathParam("tempagg") String tempagg,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("percentile") int percentile,
            @PathParam("iso3") String iso3) {
        log.info("getting ensemble scenario percentile");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getEnsembleScenario(scenarioName, statName, fyear, tyear, percentile, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // ================= download ensemble scenario percentile var =============================
    @GET
    @Path("/dl/{tempagg}/ensemble/{scenario:(?i)(20c3m|a2|b1)}/{percentile}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response downloadEnsembleScenario(
            @PathParam("tempagg") String tempagg,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("percentile") int percentile,
            @PathParam("iso3") String iso3) {

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getEnsembleScenario(scenarioName, statName, fyear, tyear, percentile, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);
    }

    // =================  ensemble single scenario all percentile =============================
    @GET
    @Path("/{tempagg}/ensemble/{scenario:(?i)(20c3m|a2|b1)}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response getSingleScenarioAllPercentile(
            @PathParam("tempagg") String tempagg,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {
        log.info("getting single scenario all percentile");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getSingleScenarioAllPercentile(scenarioName, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // =================  download ensemble single scenario all percentile =============================
    @GET
    @Path("/dl/{tempagg}/ensemble/{scenario:(?i)(20c3m|a2|b1)}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response downloadSingleScenarioAllPercentile(
            @PathParam("tempagg") String tempagg,
            @PathParam("scenario") String scenarioName,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getSingleScenarioAllPercentile(scenarioName, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);
    }

    // =================  get ensemble single percentile all scenario =============================
    @GET
    @Path("/{tempagg}/ensemble/{percentile:\\d{2}}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response getSinglePercentileAllScenario(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("percentile") int percentile,
            @PathParam("iso3") String iso3) {
        log.info("getting ensemble single percentile all scenario");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getSinglePercentileAllScenario(percentile, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // =================  download ensemble single percentile all scenario =============================
    @GET
    @Path("/dl/{tempagg}/ensemble/{percentile:\\d{2}}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response downloadSinglePercentileAllScenario(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("percentile") int percentile,
            @PathParam("iso3") String iso3) {

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getSinglePercentileAllScenario(percentile, statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);
    }

    // =================  ensemble all scenario all percentile  =============================
    @GET
    @Path("/{tempagg}/ensemble/{stat}/{fyear}/{tyear}/{iso3}")
    public Response getAllScenarioEnsemble(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        log.fine("getting ensemble all scenario all percentile");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }
        return WebService.get().getAllScenarioEnsemble(statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // =================  download ensemble all scenario all percentile =============================
    @GET
    @Path("/dl/{tempagg}/ensemble/{stat}/{fyear}/{tyear}/{iso3}")
    public Response downloadAllScenarioEnsemble(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }

        return WebService.get().getAllScenarioEnsemble(statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);

    }

    // ===============  variable projections  ==================
    @GET
    @Path("/{tempagg}/{stat}/projection/{iso3}")
    public Response getGcmProjections(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("iso3") String iso3) {
        log.info("getting all projections");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }


        return WebService.get().getGcmProjections(statName, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }
    // ===============  download variable projections  ==================

    @GET
    @Path("/dl/{tempagg}/{stat}/projection/{iso3}")
    public Response downloadGcmProjections(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("iso3") String iso3) {
        log.info("getting all projections");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }


        return WebService.get().getGcmProjections(statName, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);
    }

    // ===============  ensemble projections  ==================
    @GET
    @Path("/{tempagg}/ensemble/{stat}/projection/{iso3}")
    public Response getEnsembleProjections(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("iso3") String iso3) {
        log.info("getting all projections");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }


        return WebService.get().getEnsembleProjections(statName, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // ===============  download ensemble projections  ==================
    @GET
    @Path("/dl/{tempagg}/ensemble/{stat}/projection/{iso3}")
    public Response downloadEnsembleProjections(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("iso3") String iso3) {
        log.info("getting all projections");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }


        return WebService.get().getEnsembleProjections(statName, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);
    }

    // ===============  L3  Gcm  ==================
    @GET
    @Path("/{tempagg}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response getAllGcmAllScenarioAllGcmMonthlyDataIso(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {
        log.info("getting all gcm all scenario");
        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }


        return WebService.get().getAllGcmAllScenario(statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.WEB);
    }

    // ========  L3 Gcm Download  ===================  //
    @GET
    @Path("/dl/{tempagg}/{stat}/{fyear}/{tyear}/{iso3}")
    public Response downloadAllGcmAllScenarioAllGcmMonthlyDataIso(
            @PathParam("tempagg") String tempagg,
            @PathParam("stat") String statName,
            @PathParam("fyear") int fyear,
            @PathParam("tyear") int tyear,
            @PathParam("iso3") String iso3) {

        String mediaType = RequestUtil.getResponseType(iso3);
        iso3 = RequestUtil.getIdentifier(iso3);
        int countryId = CountryService.get().getId(iso3);
        if (countryId == -1) {
            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
        }
        Response r = WebService.get().getAllGcmAllScenario(statName, fyear, tyear, countryId, WebService.get().getStatType(tempagg), WebService.get().geTempAgg(tempagg), mediaType, Delivery.DOWNLOAD);
        return r;
    }
//    private Response getScenarioDataDelegate(
//            String scenarioName,
//            String statName,
//            int fyear,
//            int tyear,
//            String iso3, String format) {
//        int countryId = CountryService.get().getId(iso3);
//        log.log(Level.INFO, "getting country id is {0}", countryId);
//        P_GcmStatsProperties ds = P_GcmStatsProperties.getInstance();
//        P_GcmStatsProperties.climatestat stat = ds.getClimateStat(statName);
//        P_GcmStatsProperties.scenario scenario = ds.getScenario(scenarioName);
//
//        //@TODO validate the config here
//        P_GcmConfig config = new P_GcmConfig(P_GcmStatsProperties.stat_type.mean, null, scenario, stat, fyear, tyear);
//        if (scenario == null || stat == null) {
//            String errorResponse = DataFormatHelper.formatData(format, ResponseConstants.INVALID_PARAMS);
//            return Response.ok(errorResponse, format).build();
//        }
//        config.setAreaId(countryId);
//        List<GcmDatum> gcms = P_GcmConfigAreaDao.get().getAllGcmAreaData(config, false);
//        String formattedData = DataFormatHelper.formatGcms(format, gcms);
//
//        Response.ResponseBuilder builder = Response.ok(formattedData, format);
//        return builder.build();
//    }
//    private Response getAllScenarioEnsembleDelegate( String statName, int fyear, int tyear, int percentile, String iso3, String format) {
//        int countryId = CountryService.get().getId(iso3);
//        log.log(Level.INFO, "getting country id is {0}", countryId);
//        P_GcmStatsProperties ds = P_GcmStatsProperties.getInstance();
//        P_GcmStatsProperties.climatestat stat = ds.getClimateStat(statName);
//
//        P_EnsembleConfig config = new P_EnsembleConfig(P_GcmStatsProperties.stat_type.mean, null, stat, fyear, tyear, percentile);
//        if (stat == null ) {
//            String errorResponse = DataFormatHelper.formatData(format, ResponseConstants.INVALID_PARAMS);
//            return Response.ok(errorResponse, format).build();
//        }
//        config.setAreaId(countryId);
//        List<EnsembleDatum> ensembles =  P_EnsembleConfigAreaDao.get().getAllScenarioAreaDataForTime(config, false);
//
//        String formattedData = DataFormatHelper.formatEnsembles(format, ensembles);
//        Response.ResponseBuilder builder = Response.ok(formattedData, format);
//
//        return builder.build();
//    }
    // ============  aggregate across all gcms ==================//
//    @GET
//    @Path("/{scenario}/{stat}/{fyear}/{tyear}/{iso3}")
//    public Response getAllMonthlyDataIso(
//            @PathParam("scenario") String scenarioName,
//            @PathParam("stat") String statName,
//            @PathParam("fyear") int fyear,
//            @PathParam("tyear") int tyear,
//            @PathParam("iso3") String iso3) {
//
//        if (iso3 == null || iso3.length() < 3) {
//            return Response.ok(ResponseConstants.INVALID_ISO, MediaType.APPLICATION_JSON).build();
//        }
//        if (iso3 != null && (iso3.contains(ResponseConstants.xml) || iso3.contains(ResponseConstants.XML))) {
//            return getAllMonthlyDataIsoDelegate(scenarioName, statName, fyear, tyear, iso3.substring(0, iso3.indexOf(ResponseConstants.DOT)), MediaType.APPLICATION_XML);
//        }
//
//        return getAllMonthlyDataIsoDelegate(scenarioName, statName, fyear, tyear, iso3, MediaType.APPLICATION_JSON);
//    }
//    private Response getAllGcmAllScenarioMonthlyDataIsoDelegate(
//            String statName,
//            int fyear,
//            int tyear,
//            String iso3, String format, boolean download) {
//        int countryId = CountryService.get().getId(iso3);
//        log.log(Level.INFO, "getting country id is {0}", countryId);
//        P_GcmStatsProperties ds = P_GcmStatsProperties.getInstance();
//        P_GcmStatsProperties.climatestat stat = ds.getClimateStat(statName);
//
//        //@TODO validate the config here
//        P_GcmConfig config = new P_GcmConfig(P_GcmStatsProperties.stat_type.mean, null, null, stat, fyear, tyear);
//        if ( stat == null) {
//            String errorResponse = DataFormatHelper.formatData(format, ResponseConstants.INVALID_PARAMS);
//            return Response.ok(errorResponse, format).build();
//        }
//        config.setAreaId(countryId);
//        List<GcmDatum> gcms = P_GcmConfigAreaDao.get().getAllScenarioAllGcmData(config, false);
//        String formattedData = DataFormatHelper.formatGcms(format, gcms);
//        Response.ResponseBuilder r = null;
//        if(!download){
//            r =  Response.ok(formattedData, format);
//        }
//        else{
//            r = Response.ok(formattedData, format).header("Content-Disposition", "attachement; filename=download.csv");
//        }
//        return r.build();
//    }
}

