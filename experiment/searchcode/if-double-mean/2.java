public double mean(List<Double> list) {
double mean = 0;
if(!list.isEmpty()) {
for (Double item : list) {
mean += (double) item;
}
mean /= (double) list.size();
}
return mean;
}
}

