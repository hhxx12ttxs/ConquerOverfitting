package net.empuly.thegame.command.impl.ddd.eventstore;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.empuly.thegame.command.impl.ddd.event.Event;
import net.empuly.thegame.command.impl.ddd.eventsource.EventSource;
import net.empuly.thegame.command.impl.ddd.eventsource.EventSourceFactory;
import net.empuly.thegame.command.impl.ddd.eventsource.IdMetVersie;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EventStoreViaSql implements EventStore {

	private final class JdbcEventDeserializerRowMapper implements ParameterizedRowMapper<Event> {
		@Override
		public Event mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			return EventStoreViaSql.this.eventSerializer.deserialize(rs.getString(KOLOM_EVENT_DATA));
		}
	}

	private final static class JdbcEventSourceRowMapper implements ParameterizedRowMapper<JdbcEventSourceRij> {
		@Override
		public JdbcEventSourceRij mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			try {
				return JdbcEventSourceRij.rijVoorEventSource(
						UUID.fromString(rs.getString(KOLOM_EVENT_SOURCE_ID)),
						Class.forName(rs.getString(KOLOM_EVENT_SOURCE_TYPE)),
						rs.getLong(KOLOM_EVENT_SOURCE_VERSIE),
						rs.getLong(KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER));
			} catch (final ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static final String NIEUWE_WAARDE_SUFFIX = "_NIEUWE_WAARDE";
	private static final String TABEL_EVENT = "event";
	private static final String TABEL_EVENT_SOURCE = "eventSource";
	private static final String KOLOM_EVENT_DATA = "data";
	private static final String KOLOM_EVENT_TIJDSTIP_TOEVOEGING = "tijdstipToevoeging";
	private static final String KOLOM_EVENT_VOLGNUMMER = "eventVolgnummer";
	private static final String KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER = "volgendVrijEventVolgnummer";
	private static final String KOLOM_EVENT_SOURCE_VERSIE = "versie";
	private static final String KOLOM_EVENT_SOURCE_TYPE = "eventSourceType";
	private static final String KOLOM_EVENT_SOURCE_ID = "eventSourceId";
	private static final String SELECT_DATA_EVENTS_SQL = "select " +
			KOLOM_EVENT_DATA +
			" from " +
			TABEL_EVENT +
			" where " +
			KOLOM_EVENT_SOURCE_ID +
			" = :" +
			KOLOM_EVENT_SOURCE_ID +
			" order by " +
			KOLOM_EVENT_VOLGNUMMER;
	private static final String SELECT_VERSIE_EVENT_SOURCE_SQL = "select " +
			KOLOM_EVENT_SOURCE_VERSIE +
			" from " +
			TABEL_EVENT_SOURCE +
			" where " +
			KOLOM_EVENT_SOURCE_ID +
			" = :" +
			KOLOM_EVENT_SOURCE_ID;
	private static final String UPDATE_EVENT_SOURCE_SQL = "update " +
			TABEL_EVENT_SOURCE +
			" set " +
			KOLOM_EVENT_SOURCE_VERSIE +
			" = :" +
			KOLOM_EVENT_SOURCE_VERSIE + NIEUWE_WAARDE_SUFFIX +
			", " +
			KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER +
			" = :" +
			KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER +
			" where " +
			KOLOM_EVENT_SOURCE_ID +
			" = :" +
			KOLOM_EVENT_SOURCE_ID +
			" and " +
			KOLOM_EVENT_SOURCE_VERSIE +
			" = :" +
			KOLOM_EVENT_SOURCE_VERSIE;
	private static final String SELECT_FROM_EVENT_SOURCE_SQL = "select " +
			KOLOM_EVENT_SOURCE_ID +
			", " +
			KOLOM_EVENT_SOURCE_TYPE +
			", " +
			KOLOM_EVENT_SOURCE_VERSIE +
			", " +
			KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER +
			" from " +
			TABEL_EVENT_SOURCE +
			" where " +
			KOLOM_EVENT_SOURCE_ID +
			" = :" +
			KOLOM_EVENT_SOURCE_ID;
	private static final String INSERT_INTO_EVENT_SQL = "insert into " +
			TABEL_EVENT +
			" (" +
			KOLOM_EVENT_SOURCE_ID +
			", " +
			KOLOM_EVENT_VOLGNUMMER +
			", " +
			KOLOM_EVENT_TIJDSTIP_TOEVOEGING +
			", " +
			KOLOM_EVENT_DATA +
			") values (:" +
			KOLOM_EVENT_SOURCE_ID +
			", :" +
			KOLOM_EVENT_VOLGNUMMER +
			", :" +
			KOLOM_EVENT_TIJDSTIP_TOEVOEGING +
			", :" +
			KOLOM_EVENT_DATA +
			")";
	private static final String INSERT_INTO_EVENT_SOURCE_SQL = "insert into " +
			TABEL_EVENT_SOURCE +
			" (" +
			KOLOM_EVENT_SOURCE_ID +
			", " +
			KOLOM_EVENT_SOURCE_TYPE +
			", " +
			KOLOM_EVENT_SOURCE_VERSIE +
			", " +
			KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER +
			") values (:" +
			KOLOM_EVENT_SOURCE_ID +
			", :" +
			KOLOM_EVENT_SOURCE_TYPE +
			", :" +
			KOLOM_EVENT_SOURCE_VERSIE +
			", :" +
			KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER +
			")";

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private final EventSerializer eventSerializer;

	@Autowired
	public EventStoreViaSql(final NamedParameterJdbcTemplate namedParameterJdbcTemplate, final EventSerializer eventSerializer) {
		checkNotNull(namedParameterJdbcTemplate, eventSerializer);
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
		this.eventSerializer = eventSerializer;
	}

	@Override
	public void bewaarEventSource(final EventSource<? extends Event> eventSourceOmTeBewaren) {
		final List<? extends Event> nogTePersisterenEvents = eventSourceOmTeBewaren.nogTePersisterenEvents();
		if (!nogTePersisterenEvents.isEmpty()) {
			final IdMetVersie idMetVersie = eventSourceOmTeBewaren.idMetVersie();
			if (idMetVersie.isInitieleVersie()) {
				bewaarNieuweEventSource(eventSourceOmTeBewaren);
			} else {
				bewaarBestaandeEventSource(eventSourceOmTeBewaren, nogTePersisterenEvents);
			}
		}
	}

	@Override
	public <T extends EventSource<? extends Event>> T laadEventSourceViaId(final Class<T> typeVanEventSourceDatJeVerwacht,
			final UUID idVanDeEventSource) {
		final JdbcEventSourceRij eventSourceRij = bestaandeEventSourceRijOpBasisVanId(idVanDeEventSource);
		final List<Event> events = eventsVanEventSourceMetId(idVanDeEventSource);
		T eventSource = new EventSourceFactory<T>().maakEventSource(typeVanEventSourceDatJeVerwacht, eventSourceRij, events);
		return eventSource;
	}

	private JdbcEventSourceRij bestaandeEventSourceRij(final EventSource<? extends Event> eventSource) {
		final UUID eventSourceId = eventSource.idMetVersie().id();
		return bestaandeEventSourceRijOpBasisVanId(eventSourceId);
	}

	private JdbcEventSourceRij bestaandeEventSourceRijOpBasisVanId(final UUID eventSourceId) {
		final HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(KOLOM_EVENT_SOURCE_ID, eventSourceId.toString());

		return this.namedParameterJdbcTemplate
				.queryForObject(SELECT_FROM_EVENT_SOURCE_SQL,
						parameters,
						new JdbcEventSourceRowMapper());
	}

	private void bewaarBestaandeEventSource(final EventSource<? extends Event> bestaandeEventSource,
			final List<? extends Event> tePersisterenEvents) {
		final JdbcEventSourceRij bestaandeEventSourceRij = bestaandeEventSourceRij(bestaandeEventSource);
		final long volgendVrijEventVolgnummer = bewaarEvents(bestaandeEventSourceRij.eventSourceId(),
				bestaandeEventSourceRij.volgendEventSequenceNummer(),
				tePersisterenEvents);
		updateEventSourceRij(bestaandeEventSource, volgendVrijEventVolgnummer);
	}

	private void bewaarEvent(final UUID eventSourceId, final long eventVolgNummer, final LocalDateTime tijdstipToevoegingEvents,
			final Event event) {

		final HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(KOLOM_EVENT_SOURCE_ID, eventSourceId.toString());
		parameters.put(KOLOM_EVENT_VOLGNUMMER, eventVolgNummer);
		parameters.put(KOLOM_EVENT_TIJDSTIP_TOEVOEGING, new Timestamp(tijdstipToevoegingEvents.toDateTime().getMillis()));
		parameters.put(KOLOM_EVENT_DATA, this.eventSerializer.serialize(event));

		this.namedParameterJdbcTemplate.update(INSERT_INTO_EVENT_SQL, parameters);
	}

	private long bewaarEvents(final UUID eventSourceId, long volgendEventSequenceNummer, final List<? extends Event> tePersisterenEvents) {
		final LocalDateTime tijdstipToevoegingEvents = new LocalDateTime();
		for (final Event event : tePersisterenEvents) {
			bewaarEvent(eventSourceId, volgendEventSequenceNummer++, tijdstipToevoegingEvents, event);
		}
		return volgendEventSequenceNummer;
	}

	private void bewaarEventSourceRij(final JdbcEventSourceRij eventSourceRij, final long initieleWaardeVoorVolgendEventSequenceNummer) {
		final HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(KOLOM_EVENT_SOURCE_ID, eventSourceRij.eventSourceId().toString());
		parameters.put(KOLOM_EVENT_SOURCE_TYPE, eventSourceRij.typeVanDeEventSource().getName());
		parameters.put(KOLOM_EVENT_SOURCE_VERSIE, eventSourceRij.versie());
		parameters.put(KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER, initieleWaardeVoorVolgendEventSequenceNummer);
		this.namedParameterJdbcTemplate.update(INSERT_INTO_EVENT_SOURCE_SQL, parameters);
	}

	private void bewaarNieuweEventSource(final EventSource<? extends Event> nieuweEventSource) {
		final JdbcEventSourceRij eventSourceRij = nieuweEventSourceRij(nieuweEventSource);
		final List<? extends Event> tePersisterenEvents = nieuweEventSource.nogTePersisterenEvents();
		final int verwachtVolgendVrijEventVolgnummer = verwachtVolgendVrijEventVolgnummer(tePersisterenEvents);
		bewaarEventSourceRij(eventSourceRij, verwachtVolgendVrijEventVolgnummer);
		final long effectiefVolgendVrijEventVolgnummer = bewaarEvents(eventSourceRij.eventSourceId(),
				eventSourceRij.volgendEventSequenceNummer(),
				tePersisterenEvents);

		if (effectiefVolgendVrijEventVolgnummer != verwachtVolgendVrijEventVolgnummer) {
			throw new IllegalStateException(
					"Mismatch tussen op voorhand berekend vrij volgnummer en het effectieve na toevoegen van alle events: "
							+ effectiefVolgendVrijEventVolgnummer + " vs " + verwachtVolgendVrijEventVolgnummer);
		}
	}

	private List<Event> eventsVanEventSourceMetId(final UUID idVanDeEventSource) {
		final HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(KOLOM_EVENT_SOURCE_ID, idVanDeEventSource.toString());
		return this.namedParameterJdbcTemplate.query(SELECT_DATA_EVENTS_SQL, parameters, new JdbcEventDeserializerRowMapper());
	}

	private JdbcEventSourceRij nieuweEventSourceRij(final EventSource<? extends Event> nieuweEventSource) {
		return JdbcEventSourceRij.rijVoorNieuweEventSource(nieuweEventSource.idMetVersie().id(),
				nieuweEventSource.getClass());
	}

	private void throwConcurrentModificationFailureException(final EventSource<? extends Event> bestaandeEventSource,
			final long effectieveVersieInDeDatabank,
			final long versieVerwachtInDatabank) {
		final String boodschap = "Concurrent modification voor event source met id '" + bestaandeEventSource.idMetVersie().id().toString()
				+ "', type '"
				+ bestaandeEventSource.getClass().getName() + "'. " +
				"Effectieve versie in databank: " + effectieveVersieInDeDatabank + ", versie verwacht in databank: "
				+ versieVerwachtInDatabank;
		throw new OptimisticLockingFailureException(boodschap);
	}

	private void updateEventSourceRij(final EventSource<? extends Event> bestaandeEventSource, final long volgendVrijEventVolgnummer) {
		final long huidigeVersie = bestaandeEventSource.idMetVersie().versie();
		final long versieVerwachtInDatabank = huidigeVersie - 1;

		final HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(KOLOM_EVENT_SOURCE_VERSIE + NIEUWE_WAARDE_SUFFIX, huidigeVersie);
		parameters.put(KOLOM_EVENT_SOURCE_VOLGEND_VRIJ_EVENT_VOLGNUMMER, volgendVrijEventVolgnummer);
		parameters.put(KOLOM_EVENT_SOURCE_ID, bestaandeEventSource.idMetVersie().id().toString());
		parameters.put(KOLOM_EVENT_SOURCE_VERSIE, versieVerwachtInDatabank);

		final int aantalRijenAangepast = this.namedParameterJdbcTemplate.update(UPDATE_EVENT_SOURCE_SQL,
				parameters);
		if (aantalRijenAangepast != 1) {
			parameters.clear();
			parameters.put(KOLOM_EVENT_SOURCE_ID, bestaandeEventSource.idMetVersie().id().toString());
			final long effectieveVersieInDeDatabank = this.namedParameterJdbcTemplate.queryForLong(SELECT_VERSIE_EVENT_SOURCE_SQL,
					parameters);
			throwConcurrentModificationFailureException(bestaandeEventSource, effectieveVersieInDeDatabank, versieVerwachtInDatabank);
		}
	}

	private int verwachtVolgendVrijEventVolgnummer(final List<? extends Event> tePersisterenEvents) {
		return tePersisterenEvents.size();
	}

}
