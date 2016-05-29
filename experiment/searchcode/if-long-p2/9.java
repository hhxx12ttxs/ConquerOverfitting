List<Integer> ps = NTLib.primeList(1000000);
int n = 1;
for (int p : ps) {
long p2 = (long) p * p;
long m = 0;
m += BigInteger.valueOf(p + 1).modPow(n_, p2_).longValue();
m %= p2;
if (m > 10000000000L) {
System.out.println(n);
break;
}
n++;
}
}
}

