private final long millis;

private RefreshTimestamp(long millis) {
this.millis = millis;
return millis == sunTimestamp.millis;
}

@Override
public int hashCode() {
return (int) (millis ^ (millis >>> 32));
}

}

