public class TopicWithWeight implements Comparable<TopicWithWeight> {
private int id;
private double weight;

public TopicWithWeight(int id, double weight) {
public void setWeight(double weight) {
this.weight = weight;
}

public double getWeight() {

