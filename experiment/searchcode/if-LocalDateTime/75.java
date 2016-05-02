package sk.wladimiiir.intellihome.model.unit.control.calendar;

import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.RRule;

import java.text.ParseException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * @author Vladimir Hrusovsky
 */
public abstract class AbstractCalendarRule<E extends CalendarRuleEntity> {
    private List<E> rules = new ArrayList<>();

    public List<E> getRules() {
        return rules;
    }

    public void setRules(List<E> rules) {
        this.rules = rules;
    }

    protected Optional<E> getRuleInNowTime() {
        return rules.parallelStream()
                .filter(this::isRuleInNowTime)
                .sorted((o1, o2) -> {
                    if (o1.getRecurrenceRule() == null && o2.getRecurrenceRule() != null) {
                        return -1;
                    } else if (o1.getRecurrenceRule() != null && o2.getRecurrenceRule() == null) {
                        return 1;
                    } else {
                        return 0;
                    }
                })
                .findFirst();
    }

    private boolean isRuleInNowTime(E rule) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime startTime = rule.getStartTime();
        final LocalDateTime endTime = rule.getEndTime();

        if (!startTime.isAfter(now) && !endTime.isBefore(now)) {
            return true;
        }
        if (rule.getRecurrenceRule() == null) {
            return false;
        }

        final long diff = endTime.toEpochSecond(ZoneOffset.UTC) - startTime.toEpochSecond(ZoneOffset.UTC);
        final LocalDateTime utcNow = LocalDateTime.now(Clock.systemUTC());
        try {
            final RRule rRule = new RRule(getAdjustRecurrenceRule(rule.getRecurrenceRule()));
            final RecurrenceIterator iterator = RecurrenceIteratorFactory.createRecurrenceIterator(
                    rRule,
                    new DateTimeValueImpl(startTime.getYear(), startTime.getMonth().getValue(), startTime.getDayOfMonth(), startTime.getHour(), startTime.getMinute(), startTime.getSecond()),
                    TimeZone.getDefault()
            );
            while (iterator.hasNext()) {
                final DateTimeValue value = (DateTimeValue) iterator.next();
                final LocalDateTime start = LocalDateTime.of(value.year(), value.month(), value.day(), value.hour(), value.minute(), value.second());
                final LocalDateTime end = start.plusSeconds(diff);
                if (start.isAfter(utcNow)) {
                    break;
                }
                if (!end.isBefore(utcNow)) {
                    return true;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String getAdjustRecurrenceRule(String recurrenceRule) {
        if (!recurrenceRule.startsWith("RRULE:")) {
            recurrenceRule = "RRULE:" + recurrenceRule;
        }
        recurrenceRule = recurrenceRule.replaceAll("DTSTART=\\d+T?\\d*Z?;", "");
        recurrenceRule = recurrenceRule.replace("COUNT=1000;", "");
        recurrenceRule = recurrenceRule.replace("BYDAY=MO,TU,WE,TH,FR,SA,SU;", "");
        return recurrenceRule;
    }
}

