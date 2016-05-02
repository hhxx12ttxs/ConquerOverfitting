package sk.wladimiiir.intellihome.view.rest;

import sk.wladimiiir.intellihome.model.db.ThermometerData;
import sk.wladimiiir.intellihome.model.db.query.MinMaxAverageEntity;
import sk.wladimiiir.intellihome.model.exception.ThermometerException;
import sk.wladimiiir.intellihome.model.thermometer.Thermometer;
import sk.wladimiiir.intellihome.service.thermometer.ThermometerDataService;
import sk.wladimiiir.intellihome.service.thermometer.ThermometerService;
import sk.wladimiiir.intellihome.view.rest.entity.HistoryRequest;
import sk.wladimiiir.intellihome.view.rest.entity.temperature.TemperatureEntity;
import sk.wladimiiir.intellihome.view.rest.entity.temperature.TemperatureHistoryEntity;
import sk.wladimiiir.intellihome.view.rest.entity.temperature.TemperatureHistoryResponse;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Vladimir Hrusovsky
 */
@Named
@Singleton
@Path("temperature")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TemperatureResource {
	@Inject
	private volatile ThermometerService thermometerService;
	@Inject
	private volatile ThermometerDataService thermometerDataService;

	@GET
	public List<TemperatureEntity> getTemperatures() {
		return thermometerService.getThermometers().parallelStream()
				.map(thermometer -> new TemperatureEntity(thermometer.getID(), thermometer.getName(), getTemperature(thermometer)))
				.collect(Collectors.toList());
	}

	private float getTemperature(Thermometer thermometer) {
		try {
			return thermometer.getTemperature();
		} catch (ThermometerException e) {
			return 0;
		}
	}

	@Path("{id}/history")
	@POST
	public TemperatureHistoryResponse getHistory(@PathParam("id") String thermometerID, HistoryRequest request) {
		final Optional<Thermometer> thermometer = thermometerService.getThermometer(thermometerID);
		if (!thermometer.isPresent()) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		final ZonedDateTime date = request.getDate().toInstant().atZone(ZoneId.systemDefault());
		switch (request.getType()) {
			case HOUR: {
				final List<ThermometerData> data = thermometerDataService.getThermometerData(
						thermometerID,
						LocalDateTime.from(date),
						LocalDateTime.from(date).plusHours(1)
				);
				return new TemperatureHistoryResponse(
						data.parallelStream()
								.map(thermometerData -> new TemperatureHistoryEntity(Date.from(thermometerData.getDateTime().atZone(ZoneId.systemDefault()).toInstant()), thermometerData.getTemperature()))
								.collect(Collectors.toList())
				);
			}
			case DAY: {
				final LocalDateTime startOfDay = LocalDate.from(date).atStartOfDay();
				final List<MinMaxAverageEntity> data = thermometerDataService.getHourlyGroupedData(
						thermometerID,
						startOfDay,
						LocalDate.from(date).atTime(23, 59, 59)
				);
				return new TemperatureHistoryResponse(
						data.parallelStream()
								.map(entity -> new TemperatureHistoryEntity(
										Date.from(startOfDay.withHour(entity.getGroupValue()).atZone(ZoneId.systemDefault()).toInstant()),
										entity.getAverage(),
										entity.getMin(),
										entity.getMax()
								))
								.collect(Collectors.toList())
				);
			}
			case MONTH: {
				final LocalDateTime startOfMonth = YearMonth.from(date).atDay(1).atStartOfDay();
				final LocalDateTime endOfMonth = YearMonth.from(date).atEndOfMonth().atTime(23, 59, 59);
				final List<MinMaxAverageEntity> data = thermometerDataService.getDailyGroupedData(thermometerID, startOfMonth, endOfMonth);

				return new TemperatureHistoryResponse(
						data.parallelStream()
								.map(entity -> new TemperatureHistoryEntity(
										Date.from(startOfMonth.withDayOfMonth(entity.getGroupValue()).atZone(ZoneId.systemDefault()).toInstant()),
										entity.getAverage(),
										entity.getMin(),
										entity.getMax()
								))
								.collect(Collectors.toList())
				);
			}
			case YEAR: {
				final LocalDateTime startOfYear = Year.from(date).atMonth(1).atDay(1).atStartOfDay();
				final LocalDateTime endOfYear = Year.from(date).atMonth(12).atEndOfMonth().atTime(23, 59, 59);
				final List<MinMaxAverageEntity> data = thermometerDataService.getMonthlyGroupedData(thermometerID, startOfYear, endOfYear);

				return new TemperatureHistoryResponse(
						data.parallelStream()
								.map(entity -> new TemperatureHistoryEntity(
										Date.from(startOfYear.withMonth(entity.getGroupValue()).atZone(ZoneId.systemDefault()).toInstant()),
										entity.getAverage(),
										entity.getMin(),
										entity.getMax()
								))
								.collect(Collectors.toList())
				);
			}
			case YEARS: {
				final List<MinMaxAverageEntity> data = thermometerDataService.getYearlyGroupedData(thermometerID);

				return new TemperatureHistoryResponse(
						data.parallelStream()
								.map(entity -> new TemperatureHistoryEntity(
										Date.from(Year.of(entity.getGroupValue()).atMonth(1).atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()),
										entity.getAverage(),
										entity.getMin(),
										entity.getMax()
								))
								.collect(Collectors.toList())
				);
			}
			default:
				return new TemperatureHistoryResponse(
						Collections.emptyList()
				);
		}
	}

    @GET
    @Path("/history/clear_future")
    public Response clearFutureHistory() {
        thermometerDataService.removeFutureData();
        return Response.ok().build();
    }

    @GET
    @Path("/history/remove_last_year")
    public Response removeLastYearHistory() {
        thermometerDataService.removeLastYearData();
        return Response.ok().build();
    }
}

