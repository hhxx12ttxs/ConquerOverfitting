public void insert(Factor f, PriorityQueue<Factor> q1, PriorityQueue<Factor> q2, PriorityQueue<Factor> q3) {
if ((f.a >= f.b) &amp;&amp; (f.a >= f.c)) q1.add(f);
else if ((f.a < f.b) &amp;&amp; (f.b >= f.c)) q2.add(f);
else q3.add(f);
}

public int nthUglyNumber(int n) {

