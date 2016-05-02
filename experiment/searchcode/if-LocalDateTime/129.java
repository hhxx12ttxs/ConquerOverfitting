package sk.wladimiiir.intellihome.view.rest;

import sk.wladimiiir.intellihome.model.db.TemperatureCalendarRuleEntityImpl;
import sk.wladimiiir.intellihome.model.db.UnitStateCalendarRuleEntityImpl;
import sk.wladimiiir.intellihome.model.exception.UnitException;
import sk.wladimiiir.intellihome.model.unit.UnitControl;
import sk.wladimiiir.intellihome.model.unit.control.TemperatureUnitControl;
import sk.wladimiiir.intellihome.model.unit.control.TimingUnitControl;
import sk.wladimiiir.intellihome.model.unit.control.UnitStateUnitControl;
import sk.wladimiiir.intellihome.model.unit.control.calendar.TemperatureCalendarRuleEntity;
import sk.wladimiiir.intellihome.model.unit.control.calendar.UnitStateCalendarRuleEntity;

import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Vladimir Hrusovsky
 */
@Named
@Singleton
public class UnitControlDataManager {
    public Map<String, Object> createData(UnitControl control) {
        switch (control.getType()) {
            case STATE:
                return createStateData((UnitStateUnitControl) control);
            case TEMPERATURE:
                return createTemperatureData((TemperatureUnitControl) control);
            case TIMING:
                return createTimingData((TimingUnitControl) control);
            default:
                return null;
        }
    }

    private Map<String, Object> createStateData(UnitStateUnitControl control) {
        final Map<String, Object> data = new HashMap<>();

        data.put("state", control.getUnit().getState().name());
        if (control.hasCalendarRules()) {
            data.put("calendarRules", control.getCalendarRules().stream()
                    .map(this::createUnitStateCalendarRule)
                    .collect(Collectors.toList()));
        }

        return data;
    }

    private Map<String, Object> createUnitStateCalendarRule(UnitStateCalendarRuleEntity rule) {
        final Map<String, Object> data = new HashMap<>();

        data.put("id", rule.getId());
        data.put("startTime", Date.from(rule.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        data.put("endTime", Date.from(rule.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        data.put("recurrenceRule", rule.getRecurrenceRule());
        data.put("running", rule.isRunning());

        return data;
    }

    private Map<String, Object> createTemperatureData(TemperatureUnitControl control) {
        final Map<String, Object> data = new HashMap<>();

        data.put("temperatureID", control.getThermometerID());
        data.put("min", control.getMin());
        data.put("max", control.getMax());
        data.put("manual", control.isManual());
        if (control.hasCalendarRules()) {
            data.put("calendarRules", control.getCalendarRules().stream()
                    .map(this::createTemperatureCalendarRule)
                    .collect(Collectors.toList()));
        }

        return data;
    }

    private Map<String, Object> createTemperatureCalendarRule(TemperatureCalendarRuleEntity rule) {
        final Map<String, Object> data = new HashMap<>();

        data.put("id", rule.getId());
        data.put("startTime", Date.from(rule.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        data.put("endTime", Date.from(rule.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        data.put("recurrenceRule", rule.getRecurrenceRule());
        data.put("minTemperature", rule.getMinTemperature());
        data.put("maxTemperature", rule.getMaxTemperature());

        return data;
    }

    private Map<String, Object> createTimingData(TimingUnitControl control) {
        final Map<String, Object> data = new HashMap<>();

        data.put("remaining", control.getRemaining().getSeconds());

        return data;
    }

    public void applyData(UnitControl control, Map<String, Object> data) {
        switch (control.getType()) {
            case STATE:
                applyStateData((UnitStateUnitControl) control, data);
                break;
            case TEMPERATURE:
                applyTemperatureData((TemperatureUnitControl) control, data);
                break;
            case TIMING:
                applyTimingData((TimingUnitControl) control, data);
                break;
        }
    }

    private void applyStateData(UnitStateUnitControl control, Map<String, Object> data) {
        if (data.containsKey("action")) {
            try {
                final String action = (String) data.get("action");
                switch (action) {
                    case "ON":
                        control.getUnit().forceRun();
                        break;
                    case "OFF":
                        control.getUnit().suspend();
                        break;
                    case "AUTO":
                        control.getUnit().resume();
                        break;

                }
            } catch (UnitException e) {
                e.printStackTrace();
            }
        }
        if (data.containsKey("calendarRules")) {
            //noinspection unchecked
            control.setCalendarRules(getUnitStateRules((List<Map<String, Object>>) data.get("calendarRules")));
        }
    }

    private void applyTemperatureData(TemperatureUnitControl control, Map<String, Object> data) {
        if (data.containsKey("manual")) {
            if ((Boolean) data.get("manual")) {
                control.setManual(((Number) data.get("min")).floatValue(), ((Number) data.get("max")).floatValue());
            } else {
                control.setAuto();
            }
        }
        if (data.containsKey("calendarRules")) {
            //noinspection unchecked
            control.setCalendarRules(getTemperatureRules((List<Map<String, Object>>) data.get("calendarRules")));
        }
    }

    private List<TemperatureCalendarRuleEntity> getTemperatureRules(List<Map<String, Object>> ruleObjects) {
        return ruleObjects.stream()
                .map(object -> new TemperatureCalendarRuleEntityImpl(
                                LocalDateTime.ofInstant(Instant.parse(object.get("startTime").toString()), ZoneId.systemDefault()),
                                LocalDateTime.ofInstant(Instant.parse(object.get("endTime").toString()), ZoneId.systemDefault()),
                                (String) object.get("recurrenceRule"),
                                ((Number) object.get("minTemperature")).floatValue(),
                                ((Number) object.get("maxTemperature")).floatValue()
                        )
                )
                .collect(Collectors.toList());
    }

    private void applyTimingData(TimingUnitControl control, Map<String, Object> data) {
        final long runTime = ((Number) data.get("runTimeMinutes")).longValue();
        if (runTime > 0) {
            control.startFor(Duration.ofMinutes(runTime));
        } else {
            control.cancel();
        }
    }

    public List<UnitStateCalendarRuleEntity> getUnitStateRules(List<Map<String, Object>> ruleObjects) {
        return ruleObjects.stream()
                .map(object -> new UnitStateCalendarRuleEntityImpl(
                                LocalDateTime.ofInstant(Instant.parse(object.get("startTime").toString()), ZoneId.systemDefault()),
                                LocalDateTime.ofInstant(Instant.parse(object.get("endTime").toString()), ZoneId.systemDefault()),
                                (String) object.get("recurrenceRule"),
                                (Boolean) object.get("running")
                        )
                )
                .collect(Collectors.toList());
    }
}

