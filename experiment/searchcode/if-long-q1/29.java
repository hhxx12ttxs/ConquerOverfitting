P ip = intersect(ps[i], ps[i + 1], ps[j], ps[j + 1]);
// P ip = ss[i].intersection(ss[j]);
if (ip == null)
continue;

long di = ps[i].d + ps[i].dist(ip);
min = Math.min(di - dj, min);
break;
}
}
System.out.println(min);
}

P intersect(P p1, P p2, P q1, P q2) {
if (p1.equals(q1) || p1.equals(q2))

