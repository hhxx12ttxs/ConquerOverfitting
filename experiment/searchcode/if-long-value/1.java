public class LongComparator implements Comparator<Long> {

public int compare(Long i, Long j) {
if (i.longValue() < j.longValue())
return -1;
else if (i.longValue() == j.longValue())
return 0;
else
return 1;
}

};

