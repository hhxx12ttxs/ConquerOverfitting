private static final long serialVersionUID = 3405289957699767607L;

@Override
public int compare(Period p1, Period p2) {
if(p1 == p2) {
return 0;
}

if (p1 == null &amp;&amp; p2 != null) {
return -1;
}
if (p1 != null &amp;&amp; p2 == null) {

