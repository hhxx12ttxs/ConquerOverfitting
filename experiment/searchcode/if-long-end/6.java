public class Range {

private final long end;

private final long start;

public Range(final long start, final long end) {
if (start >= end) {
throw new IllegalArgumentException(&quot;start must be strictly less&quot;

