import static org.efix.util.format.DateFormatter.DAY_MILLIS;
import static org.efix.util.format.IntFormatter.format2DigitUInt;
int days = (int) (timestamp / DAY_MILLIS);

int millis = (int) (timestamp - days * DAY_MILLIS);
if (millis < 0)

