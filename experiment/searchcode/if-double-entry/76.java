public Double increase(T t, double value) {
Double score = get(t);
if (score == null) {
score = 0d;
}
score += value;
put(t, score);
put(t, score);
return score;
}

public Entry<T, Double>[] getHits() {
Entry<T, Double>[] hits = (Entry<T, Double>[])entrySet().toArray(new Entry[size()]);

