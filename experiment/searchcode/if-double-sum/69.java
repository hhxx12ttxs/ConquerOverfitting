package de.berlin.arzt;

public class Math2 {
public static <T extends Number> double sum(Iterable<T> i) {
double sum = 0;
for (Number s : i) {
if (s != null) {
sum += s.doubleValue();
}
}
return sum;
}
}

