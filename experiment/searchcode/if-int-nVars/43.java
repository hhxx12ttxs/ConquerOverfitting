public class VarDynamics implements ISsfDynamics {

private final VarDescriptor desc;
private final Matrix V0, T;
private final int nvars, nl, nlx;
return desc;
}

public int getLagsCount() {
return nlx;
}

private Matrix L() {
if (L == null) {

