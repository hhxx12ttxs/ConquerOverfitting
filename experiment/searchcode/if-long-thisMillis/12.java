final long otherMillis = other.getDateTime().toInstant(ZoneOffset.ofHours(0)).toEpochMilli();
final long thisMillis = pass.getDateTime().toInstant(ZoneOffset.ofHours(0)).toEpochMilli();

final long timestampDelta = Math.abs(otherMillis - thisMillis);
if (other.getDirection() == CarPass.PassDirection.DIRECTION_A &amp;&amp;

