public class ChangingCompFactor implements ICompFactorPlan {

private double compFactor;

public ChangingCompFactor() {
this.compFactor = 0;
}

@Override
public double getCompFactor() {
if (compFactor < 1) {
double ret = compFactor;
compFactor += 0.01;
return ret;
}

return 1;
}

}

