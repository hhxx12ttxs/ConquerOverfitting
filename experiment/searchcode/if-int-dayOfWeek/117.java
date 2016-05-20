package com.marimon.vodafonecosts.pipes;

import java.util.Calendar;
import java.util.NoSuchElementException;

import com.marimon.vodafonecosts.domain.Call;
import com.tinkerpop.pipes.AbstractPipe;

public class PlanSSizePipe extends AbstractPipe<Call, Call> {

    private static final int FREE_SECONDS = 350 * 60;

    private static final int VIP_COST = 0;

    private static final int MINUTE_COST = 1990;

    private static final int SETUP_COST = 1500;

    private static final int SMS_COST = 1500;

    private static final int A2_COST = 1800;

    private static final int A2_SMS_COST = 900;

    private int _cumulativeTime = 0;

    @Override
    protected Call processNextStart() throws NoSuchElementException {
        while (starts.hasNext()) {
            Call c = starts.next();

            _cumulativeTime += c.getDuration();

            int setup = 0;
            int duration = 0;

            switch (c.getType()) {
            case VIP:
                setup = VIP_COST;
                break;
            case SMS:
                setup = SMS_COST;
                break;
            case A2:
                setup = A2_COST;
                break;
            case A2_SMS:
                setup = A2_SMS_COST;
                break;
            case VODAFONE:
            case LANDLINE:
                if (requiresPayment(c)) {
                    setup = SETUP_COST;
                    duration =
                        Math.round((float) (c.getDuration() * MINUTE_COST)
                            / (float) 60);
                }
                break;
            case MOBILE:
                if (requiresPayment(c)) {
                    setup = 1500;
                    duration =
                        Math.round((float) (c.getDuration() * MINUTE_COST)
                            / (float) 60);
                }
                break;
            case VODAFONE_FAMILY:
            case HAPPY:
            case SPECIAL:
            default:
                setup = 0;
                duration = 0;
                break;
            }
            c.setCost(setup + duration);

            return c;
        }
        throw new NoSuchElementException();
    }

    private boolean requiresPayment(final Call c) {
        return (_cumulativeTime > FREE_SECONDS) || outOfWindow(c);
    }

    private boolean outOfWindow(final Call call) {
        Calendar c = Calendar.getInstance();
        c.setTime(call.getDate());
        return isWorkDay(c) && isWorkHour(c);
    }

    private boolean isWorkHour(final Calendar c) {
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        return (hourOfDay > 8) && (hourOfDay < 18);
    }

    private boolean isWorkDay(final Calendar c) {
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek != Calendar.SUNDAY)
            && (dayOfWeek != Calendar.SATURDAY);
    }
}

