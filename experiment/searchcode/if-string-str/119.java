package open.dolphin.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import open.dolphin.infomodel.*;
import open.dolphin.session.KarteServiceBean;

/**
 * KarteResource
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author modified by masuda, Masuda Naika
 * @author modified by katoh, Hashimoto iin
 */
@Path("karte")
public class KarteResource extends AbstractResource {

    private static final boolean debug = false;
    
    @Inject
    private KarteServiceBean karteServiceBean;

    public KarteResource() {
    }

    @GET
    @Path("{ptId}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public Response getKarte(@PathParam("ptId") Long patientPK, 
            @QueryParam(FROM_DATE) String fromDateStr) {

        Date fromDate = parseDate(fromDateStr);

        KarteBean karte = karteServiceBean.getKarte(patientPK, fromDate);
        StreamingOutput so = getJsonOutStream(karte);

        return Response.ok(so).build();
    }

    //-------------------------------------------------------

//katoh^
    @GET
    @Path("docinfo/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public Response getDocumentList(@PathParam("id") Long karteId, 
            @QueryParam(FROM_DATE) String fromDateStr, 
            @QueryParam(TO_DATE) String toDateStr, 
            @QueryParam(INCLUDE_MODIFIED) Boolean includeModified) {

        Date fromDate = parseDate(fromDateStr);
        Date toDate = parseDate(toDateStr);

        List<DocInfoModel> result = karteServiceBean.getDocumentList(karteId, fromDate, toDate, includeModified);

        TypeReference typeRef = new TypeReference<List<DocInfoModel>>(){};
        StreamingOutput so = getGzipOutStream(result, typeRef);
        
        return Response.ok(so).build();
    }
//katoh$

    @GET
    @Path("document")
    @Produces(MEDIATYPE_JSON_UTF8)
    public Response getDocuments(@QueryParam(IDS) String ids) {

        List<Long> list = getConverter().toLongList(ids);

        List<DocumentModel> result = karteServiceBean.getDocuments(list);
        
        TypeReference typeRef = new TypeReference<List<DocumentModel>>(){};
        StreamingOutput so = getGzipOutStream(result, typeRef);
        
        return Response.ok(so).build();
    }

    @POST
    @Path("document")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public Response postDocument(String json) {

        DocumentModel document = (DocumentModel) 
                getConverter().fromJson(json, DocumentModel.class);
        
        long result = karteServiceBean.addDocument(document);
        String pkStr = String.valueOf(result);
        debug(pkStr);

        return Response.ok(pkStr).build();
    }

    
    @PUT
    @Path("document/{id}")
    @Consumes(MEDIATYPE_TEXT_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public Response putTitle(@PathParam("id") String idStr, String title) {

        long id = Long.valueOf(idStr);

        int result = karteServiceBean.updateTitle(id, title);

        return Response.ok(String.valueOf(result)).build();
    }


    @DELETE
    @Path("document/{id}")
    public void deleteDocument(@PathParam("id") String idStr) {

        long id = Long.valueOf(idStr);

        int cnt = karteServiceBean.deleteDocument(id);
        String cntStr = String.valueOf(cnt);
        debug(cntStr);
    }

    //-------------------------------------------------------

    @GET
    @Path("modules/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public Response getModules(@PathParam("id") Long karteId,
            @QueryParam(FROMS) String fromStr,
            @QueryParam(TOS) String toStr,
            @QueryParam(ENTITY) String entity) {

        List<Date> fromList = new ArrayList<>();
        List<Date> toList = new ArrayList<>();
        String[] froms = fromStr.split(CAMMA);
        for (String str : froms) {
            fromList.add(parseDate(str));
        }
        String[] tos = toStr.split(CAMMA);
        for (String str : tos) {
            toList.add(parseDate(str));
        }

        List<List<ModuleModel>> result = karteServiceBean.getModules(karteId, entity, fromList, toList);
        TypeReference typeRef = new TypeReference<List<List<ModuleModel>>>(){};
        
        StreamingOutput so = getJsonOutStream(result, typeRef);
        
        return Response.ok(so).build();
    }


    @GET
    @Path("iamges/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public Response getImages(@PathParam("id") Long karteId,
            @QueryParam(FROMS) String fromStr,
            @QueryParam(TOS) String toStr) {

        List<Date> fromList = new ArrayList<>();
        List<Date> toList = new ArrayList<>();
        String[] froms = fromStr.split(CAMMA);
        for (String str : froms) {
            fromList.add(parseDate(str));
        }
        String[] tos = toStr.split(CAMMA);
        for (String str : tos) {
            toList.add(parseDate(str));
        }

        List<List<SchemaModel>> result = karteServiceBean.getImages(karteId, fromList, toList);

        TypeReference typeRef = new TypeReference<List<List<SchemaModel>>>(){};
        StreamingOutput so = getJsonOutStream(result, typeRef);
        
        return Response.ok(so).build();
    }

    
    @GET
    @Path("image/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public Response getImage(@PathParam("param") Long id) {

        SchemaModel result = karteServiceBean.getImage(id);

        StreamingOutput so = getJsonOutStream(result);
        
        return Response.ok(so).build();
    }

    //-------------------------------------------------------

    @GET
    @Path("diagnosis/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public Response getDiagnosis(@PathParam("id") Long karteId, 
            @QueryParam(FROM_DATE) String fromDateStr, 
            @QueryParam(ACTIVE_ONLY) Boolean activeOnly) {
        
        Date fromDate = parseDate(fromDateStr);

        List<RegisteredDiagnosisModel> list = karteServiceBean.getDiagnosis(karteId, fromDate, activeOnly);
        
        TypeReference typeRef = new TypeReference<List<RegisteredDiagnosisModel>>(){};
        StreamingOutput so = getJsonOutStream(list, typeRef);
        
        return Response.ok(so).build();
    }

    @POST
    @Path("diagnosis")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public Response postDiagnosis(String json) {
        
        TypeReference typeRef = new TypeReference<List<RegisteredDiagnosisModel>>(){};
        List<RegisteredDiagnosisModel> list = (List<RegisteredDiagnosisModel>)
                getConverter().fromJson(json, typeRef);
        
        List<Long> result = karteServiceBean.addDiagnosis(list);
        String text = getConverter().fromList(result);
        debug(text);

        return Response.ok(text).build();
    }

    @PUT
    @Path("diagnosis")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public Response putDiagnosis(String json) {

        TypeReference typeRef = new TypeReference<List<RegisteredDiagnosisModel>>(){};
        List<RegisteredDiagnosisModel> list = (List<RegisteredDiagnosisModel>)
                getConverter().fromJson(json, typeRef);

        int result = karteServiceBean.updateDiagnosis(list);
        String text = String.valueOf(result);
        debug(text);

        return Response.ok(text).build();
    }

    @DELETE
    @Path("diagnosis")
    public void deleteDiagnosis(@QueryParam(IDS) String ids) {

        List<Long> list = getConverter().toLongList(ids);
        int result = karteServiceBean.removeDiagnosis(list);

        debug(String.valueOf(result));
    }


    @POST
    @Path("observations")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public Response postObservations(String json) {
        
        TypeReference typeRef = new TypeReference<List<ObservationModel>>(){};
        List<ObservationModel> list = (List<ObservationModel>)
                getConverter().fromJson(json, typeRef);

        List<Long> result = karteServiceBean.addObservations(list);

        String text = getConverter().fromList(result);
        debug(text);

        return Response.ok(text).build();
    }

    @DELETE
    @Path("observations")
    public void deleteObservations(@QueryParam(IDS) String ids) {

        List<Long> list = getConverter().toLongList(ids);
        int result = karteServiceBean.removeObservations(list);

        debug(String.valueOf(result));
    }

    @PUT
    @Path("memo")
    @Consumes(MEDIATYPE_JSON_UTF8)
    @Produces(MEDIATYPE_TEXT_UTF8)
    public Response putPatientMemo(String json) {

        PatientMemoModel memo = (PatientMemoModel)
                getConverter().fromJson(json, PatientMemoModel.class);

        int result = karteServiceBean.updatePatientMemo(memo);
        String text = String.valueOf(result);
        debug(text);

        return Response.ok(text).build();
    }

    @GET
    @Path("appo/{id}")
    @Produces(MEDIATYPE_JSON_UTF8)
    public Response getAppoinmentList(@PathParam("id") Long karteId,
            @QueryParam(FROMS) String fromStr,
            @QueryParam(TOS) String toStr) {

        List<Date> fromList = new ArrayList<>();
        List<Date> toList = new ArrayList<>();
        String[] froms = fromStr.split(CAMMA);
        for (String str : froms) {
            fromList.add(parseDate(str));
        }
        String[] tos = toStr.split(CAMMA);
        for (String str : tos) {
            toList.add(parseDate(str));
        }

        List<List<AppointmentModel>> result = karteServiceBean.getAppointmentList(karteId, fromList, toList);

        TypeReference typeRef = new TypeReference<List<List<AppointmentModel>>>(){};
        StreamingOutput so = getJsonOutStream(result, typeRef);
        
        return Response.ok(so).build();
    }

    @Override
    protected void debug(String msg) {
        if (debug || DEBUG) {
            super.debug(msg);
        }
    }
}

