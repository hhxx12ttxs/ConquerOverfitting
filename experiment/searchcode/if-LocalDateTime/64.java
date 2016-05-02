package sk.wladimiiir.intellihome.dao;

import sk.wladimiiir.intellihome.model.db.UnitData;
import sk.wladimiiir.intellihome.model.db.query.DurationEntity;
import sk.wladimiiir.intellihome.model.db.query.UnitDataEntity;
import sk.wladimiiir.intellihome.model.unit.Unit;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Vladimir Hrusovsky
 */
@Named
@Singleton
@Transactional
public class UnitDataDAOImpl implements UnitDataDAO {
	@PersistenceContext
	private volatile EntityManager entityManager;

	@Override
	public void save(UnitData uniData) {
		entityManager.persist(uniData);
	}

	@Override
	public List<UnitDataEntity> getUnitData(String unitID, LocalDateTime fromTime, LocalDateTime toTime) {
		@SuppressWarnings("unchecked")
		final List<Object[]> resultData = entityManager.createNativeQuery(
				"SELECT ud.dateTime, " +
						"COALESCE((SELECT TOP 1 DATETIME FROM unit_data next_ud WHERE next_ud.unitID = :unitID AND next_ud.dateTime > ud.dateTime AND next_ud.dateTime <= :toTime), :toTime)" +
						", ud.state " +
						"FROM unit_data ud " +
						"WHERE ud.unitID = :unitID AND ud.dateTime >= :fromTime AND ud.dateTime <= :toTime " +
						"ORDER BY ud.dateTime"
		)
				.setParameter("unitID", unitID)
				.setParameter("fromTime", Timestamp.valueOf(fromTime))
				.setParameter("toTime", Timestamp.valueOf(toTime))
				.getResultList();

		final Optional<UnitData> firstUnitDataBefore = getFirstUnitDataBefore(unitID, fromTime);
		if (firstUnitDataBefore.isPresent()) {
			resultData.add(0, new Object[]{
					Timestamp.valueOf(fromTime),
					resultData.isEmpty() ? Timestamp.valueOf(toTime) : resultData.get(0)[0],
					firstUnitDataBefore.get().getState().ordinal()
			});
		}

		return resultData.parallelStream()
				.map(data -> new UnitDataEntity(((Timestamp) data[0]).toLocalDateTime(), ((Timestamp) data[1]).toLocalDateTime(), Unit.State.values()[(Integer) data[2]]))
				.collect(Collectors.toList());
	}

	@Override
	public List<DurationEntity> getDurationData(String unitID, LocalDateTime fromTime, LocalDateTime toTime, Function<LocalDateTime, LocalDateTime> stepFunction) {
		final List<DurationEntity> durationEntities = new ArrayList<>();

		while (fromTime.isBefore(toTime)) {
			durationEntities.add(new DurationEntity(fromTime, getDuration(unitID, fromTime, stepFunction.apply(fromTime))));
			fromTime = stepFunction.apply(fromTime);
		}

		return durationEntities;
	}

	private Duration getDuration(String unitID, LocalDateTime fromTime, LocalDateTime toTime) {
		final List<Integer> runStates = Arrays.asList(Unit.State.FORCE_RUN.ordinal(), Unit.State.STARTED.ordinal());

		@SuppressWarnings("unchecked")
		final List<BigDecimal> result = entityManager.createNativeQuery(
				"SELECT SUM(DATEDIFF('second', ud.dateTime, " +
						"COALESCE(" +
						"(SELECT TOP 1 DATETIME FROM unit_data next_ud WHERE next_ud.unitID = :unitID AND next_ud.dateTime > ud.dateTime AND next_ud.dateTime <= :toTime)" +
						", :toTime))) " +
						"FROM unit_data ud " +
						"WHERE ud.unitID = :unitID AND ud.dateTime >= :fromTime AND ud.dateTime <= :toTime AND ud.state IN (:runStates)"
		)
				.setParameter("unitID", unitID)
				.setParameter("fromTime", Timestamp.valueOf(fromTime))
				.setParameter("toTime", Timestamp.valueOf(toTime))
				.setParameter("runStates", runStates)
				.getResultList();

		int seconds = result.isEmpty() || result.get(0) == null ? 0 : result.get(0).intValue();
		final Optional<UnitData> firstUnitDataBefore = getFirstUnitDataBefore(unitID, fromTime);
		if (firstUnitDataBefore.isPresent() && runStates.contains(firstUnitDataBefore.get().getState().ordinal())) {
			final Optional<UnitData> nextUnit = getNextUnit(firstUnitDataBefore.get());
			if (nextUnit.isPresent()) {
				seconds += Duration.between(fromTime, nextUnit.get().getDateTime().isAfter(toTime) ? toTime : nextUnit.get().getDateTime()).getSeconds();
			}
		}

		return Duration.ofSeconds(seconds);
	}

	@Override
	public Optional<UnitData> getLastUnitData(String unitID) {
		final List<UnitData> unitDatas = entityManager.createQuery("SELECT ud FROM UnitData as ud " +
				"WHERE ud.unitID = :unitID ORDER BY ud.dateTime DESC", UnitData.class)
				.setParameter("unitID", unitID)
				.setMaxResults(1)
				.getResultList();

		return unitDatas.isEmpty() ? Optional.<UnitData>empty() : Optional.of(unitDatas.get(0));
	}

	private Optional<UnitData> getFirstUnitDataBefore(String unitID, LocalDateTime time) {
		final List<UnitData> unitDatas = entityManager.createQuery("SELECT ud FROM UnitData as ud " +
				"WHERE ud.unitID = :unitID AND ud.dateTime < :time ORDER BY ud.dateTime DESC", UnitData.class)
				.setParameter("unitID", unitID)
				.setParameter("time", time)
				.setMaxResults(1)
				.getResultList();

		return unitDatas.isEmpty() ? Optional.<UnitData>empty() : Optional.of(unitDatas.get(0));
	}

	private Optional<UnitData> getNextUnit(UnitData unitData) {
		final List<UnitData> resultList = entityManager.createQuery("SELECT ud FROM UnitData ud WHERE ud.unitID = :unitID AND ud.id > :id ORDER BY ud.id", UnitData.class)
				.setParameter("unitID", unitData.getUnitID())
				.setParameter("id", unitData.getId())
				.setMaxResults(1)
				.getResultList();

		return resultList.stream().findFirst();
	}

	@Override
	public void removeDataAfter(LocalDateTime after) {
		entityManager.createQuery("DELETE FROM UnitData as ud " +
				"WHERE ud.dateTime > :after")
				.setParameter("after", after)
				.executeUpdate();
	}

    @Override
    public void removeDataBefore(LocalDateTime before) {
        entityManager.createQuery("DELETE FROM UnitData as ud " +
                "WHERE ud.dateTime < :before")
                .setParameter("before", before)
                .executeUpdate();
    }
}

