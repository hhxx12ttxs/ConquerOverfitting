public static ArrayList<Double[]> compute() {
double currX = 0;
double currY = 0;
double time = 0.001;
ArrayList<Double[]> dataList = new ArrayList<Double[]>();
time += Constants.TIME_DELTA;
if (currY > 0) {
dataList.add(new Double[] { currX, currY });
}
}
return dataList;
}
}

