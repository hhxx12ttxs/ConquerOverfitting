positionCompare.put(&quot;P&quot;, 10);
}

@Override
public int compare(String p1, String p2) {
if (positionCompare.get(p1) > positionCompare.get(p2))
return 1;
else if (Objects.equals(positionCompare.get(p1), positionCompare.get(p2)))

