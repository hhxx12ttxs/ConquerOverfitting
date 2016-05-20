public class Time2 {
	
    private int seconds;

    public Time2() {
        this(0, 0, 0);
    }

    public Time2(int h) {
        this(h, 0, 0);
    }

    public Time2(int h, int m) {
        this(h, m, 0);
    }

    public Time2(int h, int m, int s) {
        setTime(h, m, s);
    }

    public Time2(Time2 time) {
        this(time.getHour(), time.getMinute(), time.getSecond());
    }

    public void setTime(int h, int m, int s) {
        if ( !setSecond(s) ) {
			System.out.printf("Invalid second (%d) .\n", s);
		}
        if ( !setMinute(m) ) {
			System.out.printf("Invalid minute (%d) .\n", m);
		}
		if ( !setHour(h) ) {
			System.out.printf("Invalid hour (%d) .\n", h);
		}
    }

    public boolean setHour(int h) {
        if (h >= 0 && h < 24) {
            seconds += (h -  getHour()) * 3600;
			return true;
        } else {
            return false;
        }
    }

    public boolean setMinute(int m) {
        seconds %= 60;
        if (m >= 0 && m < 60) {
            seconds += (m -  getMinute()) * 60;
			return true;
        } else {
            return false;
        }
    }

    public boolean setSecond(int s) {
        seconds = 0;
        if (s >= 0 && s < 60) {
            seconds += (s -  getSecond());
			return true;
        } else {
            return false;
        }
    }

    public int getHour() {
        return seconds / 3600;
    }

    public int getMinute() {
        return (seconds % 3600) / 60;
    }

    public int getSecond() {
        return (seconds % 3600) % 60;
    }

    public void tick() {
        seconds = (seconds + 1) % (24 * 3600);
    }

    public void incrementMinute() {
        seconds = (seconds + 60) % (24 * 3600);
    }

    public void incrementHour() {
        seconds = (seconds + 3600) % (24 * 3600);
    }

    public String toUniversalString() {
        return String.format(
                "%02d:%02d:%02d", getHour(), getMinute(), getSecond());
    }

    public String toString() {
        return String.format("%d:%02d:%02d %s",
                ((getHour() == 0 || getHour() == 12) ? 12 : getHour() % 12),
                getMinute(), getSecond(), (getHour() < 12 ? "AM" : "PM"));
    }
}

