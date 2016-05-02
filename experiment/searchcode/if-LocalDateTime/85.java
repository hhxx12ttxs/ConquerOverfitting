package sk.wladimiiir.intellihome.view.rest;

import sk.wladimiiir.intellihome.model.db.query.DurationEntity;
import sk.wladimiiir.intellihome.model.db.query.UnitDataEntity;
import sk.wladimiiir.intellihome.model.unit.Unit;
import sk.wladimiiir.intellihome.service.unit.UnitControlService;
import sk.wladimiiir.intellihome.service.unit.UnitDataService;
import sk.wladimiiir.intellihome.service.unit.UnitService;
import sk.wladimiiir.intellihome.view.rest.entity.HistoryRequest;
import sk.wladimiiir.intellihome.view.rest.entity.unit.UnitEntity;
import sk.wladimiiir.intellihome.view.rest.entity.unit.UnitHistoryEntity;
import sk.wladimiiir.intellihome.view.rest.entity.unit.UnitHistoryResponse;
import sk.wladimiiir.intellihome.view.rest.entity.unit.control.UnitControl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Vladimir Hrusovsky
 */
@Named
@Singleton
@Path("unit")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UnitResource {
    @Inject
    private volatile UnitService service;
    @Inject
    private volatile UnitDataService dataService;
    @Inject
    private volatile UnitControlService controlService;
    @Inject
    private volatile UnitControlDataManager controlDataManager;

    @GET
    public List<UnitEntity> getUnits() {
        return service.getUnits().stream()
                .map(unit -> new UnitEntity(unit.getID(), unit.getName(), unit.getState().getName(), unit.getState().name()))
                .collect(Collectors.toList());
    }

    @POST
    @Path("/{unitID}/history")
    public UnitHistoryResponse getUnitHistory(@PathParam("unitID") String unitID, HistoryRequest request) {
        final Optional<Unit> unit = service.getUnit(unitID);
        if (!unit.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        final ZonedDateTime date = request.getDate().toInstant().atZone(ZoneId.systemDefault());
        switch (request.getType()) {
            case HOUR: {
                final List<UnitDataEntity> data = dataService.getUnitData(
                        unitID,
                        LocalDateTime.from(date),
                        LocalDateTime.from(date).plusHours(1)
                );
                return new UnitHistoryResponse(
                        data.parallelStream()
                                .map(unitData -> new UnitHistoryEntity(
                                        Date.from(unitData.getFromTime().atZone(ZoneId.systemDefault()).toInstant()),
                                        Date.from(unitData.getToTime().atZone(ZoneId.systemDefault()).toInstant()),
                                        unitData.getState().getName()
                                ))
                                .collect(Collectors.toList())
                );
            }
            case DAY: {
                final LocalDateTime startOfDay = LocalDate.from(date).atStartOfDay();
                final List<UnitDataEntity> data = dataService.getUnitData(
                        unitID,
                        startOfDay,
                        LocalDate.from(date).atTime(23, 59, 59)
                );
                return new UnitHistoryResponse(
                        data.parallelStream()
                                .map(unitData -> new UnitHistoryEntity(
                                        Date.from(unitData.getFromTime().atZone(ZoneId.systemDefault()).toInstant()),
                                        Date.from(unitData.getToTime().atZone(ZoneId.systemDefault()).toInstant()),
                                        unitData.getState().getName()
                                ))
                                .collect(Collectors.toList())
                );
            }
            case MONTH: {
                final LocalDateTime startOfMonth = YearMonth.from(date).atDay(1).atStartOfDay();
                final LocalDateTime endOfMonth = YearMonth.from(date).atEndOfMonth().atTime(23, 59, 59);
                final List<DurationEntity> data = dataService.getDailyGroupedData(unitID, startOfMonth, endOfMonth);

                return new UnitHistoryResponse(
                        data.parallelStream()
                                .map(entity -> new UnitHistoryEntity(
                                        Date.from(entity.getGroupTime().atZone(ZoneId.systemDefault()).toInstant()),
                                        entity.getDuration().toMinutes()
                                ))
                                .collect(Collectors.toList())
                );
            }
            case YEAR: {
                final LocalDateTime startOfYear = Year.from(date).atMonth(1).atDay(1).atStartOfDay();
                final LocalDateTime endOfYear = Year.from(date).atMonth(12).atEndOfMonth().atTime(23, 59, 59);
                final List<DurationEntity> data = dataService.getMonthlyGroupedData(unitID, startOfYear, endOfYear);

                return new UnitHistoryResponse(
                        data.parallelStream()
                                .map(entity -> new UnitHistoryEntity(
                                        Date.from(entity.getGroupTime().atZone(ZoneId.systemDefault()).toInstant()),
                                        entity.getDuration().toMinutes()
                                ))
                                .collect(Collectors.toList())
                );
            }
            case YEARS: {
                final List<DurationEntity> data = dataService.getYearlyGroupedData(unitID);

                return new UnitHistoryResponse(
                        data.parallelStream()
                                .map(entity -> new UnitHistoryEntity(
                                        Date.from(entity.getGroupTime().atZone(ZoneId.systemDefault()).toInstant()),
                                        entity.getDuration().toMinutes()
                                ))
                                .collect(Collectors.toList())
                );
            }
            default:
                return new UnitHistoryResponse(
                        Collections.emptyList()
                );
        }
    }

    @GET
    @Path("/history/clear_future")
    public Response clearFutureHistory() {
        dataService.removeFutureData();
        return Response.ok().build();
    }

    @GET
    @Path("/history/remove_last_year")
    public Response removeLastYearHistory() {
        dataService.removeLastYearData();
        return Response.ok().build();
    }

    @GET
    @Path("/{unitID}/controls")
    public List<UnitControl> getControls(@PathParam("unitID") String unitID) {
        return controlService.getUnitControls(unitID).stream()
                .map(unitControl -> toUnitControl(unitID, unitControl))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{unitID}/controls/{controlID}")
    public UnitControl getControl(@PathParam("unitID") String unitID, @PathParam("controlID") String controlID) {
        final Optional<sk.wladimiiir.intellihome.model.unit.UnitControl> unitControl = controlService.getUnitControl(unitID, controlID);
        if (unitControl.isPresent()) {
            return toUnitControl(unitID, unitControl.get());
        } else {
            throw new NotFoundException();
        }
    }

    @POST
    @Path("/{unitID}/controls/{controlID}")
    public Response updateControl(@PathParam("unitID") String unitID, @PathParam("controlID") String controlID, Map<String, Object> controlData) {
        final Optional<sk.wladimiiir.intellihome.model.unit.UnitControl> unitControl = controlService.getUnitControl(unitID, controlID);
        if (!unitControl.isPresent()) {
            throw new NotFoundException();
        }
        controlDataManager.applyData(unitControl.get(), controlData);
        return Response.ok().build();
    }

    private UnitControl toUnitControl(String unitID, sk.wladimiiir.intellihome.model.unit.UnitControl control) {
        return new UnitControl(control.getID(), control.getType().name(), unitID, controlDataManager.createData(control));
    }
}

