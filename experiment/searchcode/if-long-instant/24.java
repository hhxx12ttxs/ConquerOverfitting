Object value = get(key);
if (value instanceof Instant) {
return (Instant) value;
}
else if (value instanceof Number) {
long seconds = ((Number) value).longValue();
return Instant.ofEpochSecond(seconds);

