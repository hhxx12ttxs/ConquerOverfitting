public class ThresholdActivator implements Activator {

private double threshold = 0;

/**
* Tworzy progową funkcję aktywującą z progiem równym 0 (funkcja znakowa)
@Override
public double eval(double in) {
if (in < threshold) {
return -1;
} else {
return 1;
}
}

}

