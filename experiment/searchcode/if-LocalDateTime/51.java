package sk.wladimiiir.intellihome.service.unit;

import org.springframework.scheduling.annotation.Scheduled;
import sk.wladimiiir.intellihome.dao.UnitDataDAO;
import sk.wladimiiir.intellihome.model.db.UnitData;
import sk.wladimiiir.intellihome.model.db.query.DurationEntity;
import sk.wladimiiir.intellihome.model.db.query.UnitDataEntity;
import sk.wladimiiir.intellihome.model.unit.Unit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Vladimir Hrusovsky
 */
@Named
@Singleton
public class UnitDataServiceImpl implements UnitDataService {
	@Inject
	private volatile UnitService unitService;
	@Inject
	private volatile UnitDataDAO unitDataDAO;

	private final Map<String, Unit.State> unitLastState = new HashMap<>();

	@Scheduled(fixedRate = 1000)
	private void storeUnitData() {
		unitService.getUnits().forEach(unit -> {
			final Unit.State state = unit.getState();
			final Unit.State lastState;

			if (unitLastState.containsKey(unit.getID())) {
				lastState = unitLastState.get(unit.getID());
			} else {
				lastState = getLastState(unit.getID());
				unitLastState.put(unit.getID(), lastState);
			}

			if (state != lastState) {
				unitDataDAO.save(new UnitData(unit.getID(), LocalDateTime.now(), state));
				unitLastState.put(unit.getID(), state);
			}
		});
	}

	private Unit.State getLastState(String unitID) {
		final Optional<UnitData> lastUnitData = unitDataDAO.getLastUnitData(unitID);
		return lastUnitData.isPresent() ? lastUnitData.get().getState() : Unit.State.SUSPENDED;
	}

	@Override
	public List<UnitDataEntity> getUnitData(String unitID, LocalDateTime fromTime, LocalDateTime toTime) {
		return unitDataDAO.getUnitData(unitID, fromTime, toTime.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : toTime);
	}

	@Override
	public List<DurationEntity> getHourlyGroupedData(String unitID, LocalDateTime fromTime, LocalDateTime toTime) {
		return unitDataDAO.getDurationData(unitID, fromTime, toTime.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : toTime, dateTime -> dateTime.plusHours(1));
	}

	@Override
	public List<DurationEntity> getDailyGroupedData(String unitID, LocalDateTime fromTime, LocalDateTime toTime) {
		return unitDataDAO.getDurationData(unitID, fromTime, toTime.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : toTime, dateTime -> dateTime.plusDays(1));
	}

	@Override
	public List<DurationEntity> getMonthlyGroupedData(String unitID, LocalDateTime fromTime, LocalDateTime toTime) {
		return unitDataDAO.getDurationData(unitID, fromTime, toTime.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : toTime, dateTime -> dateTime.plusMonths(1));
	}

	@Override
	public List<DurationEntity> getYearlyGroupedData(String unitID) {
		return unitDataDAO.getDurationData(unitID, LocalDate.of(2014, 1, 1).atStartOfDay(), LocalDateTime.now(), dateTime -> dateTime.plusYears(1));
	}

    @Override
    public void removeLastYearData() {
        unitDataDAO.removeDataBefore(Year.now().atDay(1).atStartOfDay());
    }

    @Override
	public void removeFutureData() {
		unitDataDAO.removeDataAfter(LocalDateTime.now());
	}

	@Override
	public void close() {
		unitLastState.entrySet()
				.stream()
				.filter(entry -> entry.getValue() != Unit.State.SUSPENDED)
				.map(Map.Entry::getKey)
				.forEach(unitID -> {
					unitDataDAO.save(new UnitData(unitID, LocalDateTime.now(), Unit.State.SUSPENDED));
					unitLastState.put(unitID, Unit.State.SUSPENDED);
				});
	}
}

