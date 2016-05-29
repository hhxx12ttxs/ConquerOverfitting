private static int total = 0;

private int index;
private double weight;

public Edge(double weight) {
index = total++;
this.weight = weight;
}

public double getWeight() {
return weight;
}

@Override
public int compareTo(Edge e) {

