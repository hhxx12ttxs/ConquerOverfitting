_gradient = grad;
}

public boolean isValid(){
if(Double.isNaN(_objVal))
return false;
double minStep = 1;
for(double d:direction) {
d = Math.abs(1e-4/d);
if(d < minStep) minStep = d;

