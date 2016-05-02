package lib.easyjava.net.rest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * Used to limit the rate of calls to some function or API.
 *
 * @author Rob Rua
 */
public class SingleRateLimiter implements RateLimiter {
    private double callsLeft;
    private LocalDateTime check;
    private final double limit, callsPerMilli;
    private final long millisPerEpoch;
    private final Type type;

    /**
     * @param type
     *            ROLLING or WINDOW. ROLLING allows one call every
     *            (millisPerEpoch/callsPerEpoch)ms. WINDOW allows
     *            (callsPerEpoch) calls in each (millisPerEpoch)ms stretch.
     * @param callsPerEpoch
     *            the number of calls that can go through each epoch
     * @param millisPerEpoch
     *            the length of an epoch
     */
    public SingleRateLimiter(final Type type, final int callsPerEpoch, final long millisPerEpoch) {
        this.millisPerEpoch = millisPerEpoch;
        this.type = type;
        limit = callsPerEpoch;

        if(type == Type.ROLLING) {
            callsPerMilli = (double)callsPerEpoch / (double)millisPerEpoch;
        }
        else {
            callsPerMilli = 0;
        }

        callsLeft = limit;
        check = LocalDateTime.now();
    }

    @Override
    public synchronized <T> T attemptCall(final Supplier<T> call) {
        updateCallsLeft();
        T retVal = null;

        if(callsLeft == limit) {
            callsLeft--;
            retVal = call.get();
            check = LocalDateTime.now();
        }
        else if(callsLeft >= 1.0) {
            callsLeft--;
            retVal = call.get();
        }

        return retVal;
    }

    @Override
    public synchronized long millisUntilNextCall() {
        updateCallsLeft();
        if(callsLeft >= 1.0) {
            return 0;
        }

        long millisUntilNextCall = 0;
        if(type == Type.WINDOW) {
            final long millisPassed = Duration.between(check, LocalDateTime.now()).toMillis();
            millisUntilNextCall = millisPerEpoch - millisPassed;
        }
        else if(type == Type.ROLLING) {
            millisUntilNextCall = (long)Math.ceil((1.0 - callsLeft) * callsPerMilli);
        }

        return millisUntilNextCall;
    }

    @Override
    public synchronized int numCallsLeft() {
        updateCallsLeft();
        return (int)callsLeft;
    }

    private void updateCallsLeft() {
        final long millisPassed = Duration.between(check, LocalDateTime.now()).toMillis();

        if(type == Type.ROLLING) {
            callsLeft += callsPerMilli * millisPassed;
            if(callsLeft > limit) {
                callsLeft = limit;
            }
            check = LocalDateTime.now();
        }
        else if(type == Type.WINDOW) {
            if(millisPassed >= millisPerEpoch) {
                callsLeft = limit;
                check = LocalDateTime.now();
            }
        }
    }

    @Override
    public synchronized <T> T waitForCall(final Supplier<T> call) throws InterruptedException {
        T retVal = attemptCall(call);

        if(retVal == null) {
            Thread.sleep(millisUntilNextCall());
            retVal = attemptCall(call);
        }

        return retVal;
    }
}

