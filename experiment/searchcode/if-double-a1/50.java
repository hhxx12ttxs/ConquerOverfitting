public class ABVComparator implements Comparator<Beer> {
public int compare(Beer o1, Beer o2) {
double a1,a2;
a1 = o1.abv();
a2 = o2.abv();
if(a1>a2)return -1;
else if(a1 < a2) return 1;
return 0;
}
}

