public class Performance<T> {

public double evaluateAccuracy(List<T> goldParse, List<T> parse) {
double accuracy = 0.0;
for (T parent : goldParse) {
if (parent.equals(parse.get(i))) {
accuracy += 1.0;
}
i += 1;
}
accuracy /= parse.size();
return accuracy;
}

}

