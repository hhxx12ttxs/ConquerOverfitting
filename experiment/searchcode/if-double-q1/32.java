private final double demand;

private final double deltaCostAtWhichSwitchingIsCertain;

private double q1;
this.deltaCostAtWhichSwitchingIsCertain = deltaCostAtWhichSwitchingIsCertain;
this.setQ1(0.5 * demand);
}

@Override
public void setQ1(final double q1) {

