public long InsertTest(int samplesize, int datasize) {
a = new ArrayList<Integer>();
Random rand = new Random();

for (int i = 0; i < samplesize; i++) {
Insert(rand.nextInt(datasize+1));
}

long start = System.nanoTime();

