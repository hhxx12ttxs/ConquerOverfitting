//random weight
interface Item {
double getWeight();
}

class RandomItemChooser {
public Item chooseOnWeight(List<Item> items) {
double completeWeight = 0.0;
for (Item item : items)

