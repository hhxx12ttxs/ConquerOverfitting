import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.lang3.StringUtils;

public class Value implements Serializable {
private final LongAdder longValue;

private final LongAdder doubleValue;

public static final int PRECISION = 1000;

