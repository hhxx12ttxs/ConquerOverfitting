log.info(&quot;Reducer adding to sumOfStripes for Initial. Key = {}  Value ={}&quot;, Integer.toString(i), Double.toString(val[i]));
sumOfStripes.put(new IntWritable(i), new DoubleWritable(val[i]));
}
} else if (isEmit) {
sumOfStripes.put(new IntWritable(i), new DoubleWritable(val[i]));
}
} else if (isTransit) {
Double[] val = new Double[nrOfHiddenStates];

