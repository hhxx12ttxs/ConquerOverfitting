public static final Ord<Instant> instantOrd = Ord.longOrd.comap( new F<Instant, Long>() {
@Override public Long f(Instant instant) {
return new Duration( new Long(integer) );
}
};



public static Instant quantumIncrement(Instant instant) {

