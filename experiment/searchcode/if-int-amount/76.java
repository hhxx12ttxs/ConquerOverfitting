private int amount;
private String description;

public RewardD(int amount, String description) {
this.description = description;
}

@Override
public int compareTo(RewardD r) {
if (this.getpAmount() < r.getpAmount()) {

