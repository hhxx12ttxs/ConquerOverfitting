this.annotation = annotation.getAnnotations();
}

public double getAgreement(int amount) {
int number = 0;
for (Entry<String, Map<String, Integer>> id : annotation.entrySet()) {
for (Entry<String, Integer> entry : id.getValue().entrySet()) {
if (entry.getValue() == amount) {
number++;
}
}
}
double percentage = (double) number / (double) annotation.size();

