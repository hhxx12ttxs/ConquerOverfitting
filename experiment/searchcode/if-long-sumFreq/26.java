// General test case;
long time1 = System.nanoTime();
for (int n = 0; n < 33; ++n) {
for (int k = 1; k < 10; ++k) {
brutalForce(n, k);
}
}
long time2 = System.nanoTime();
System.out.println((time2 - time1) / 1000000000.0);

