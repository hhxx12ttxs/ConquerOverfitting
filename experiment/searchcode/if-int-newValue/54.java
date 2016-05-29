Object newValue = args[0];

if(&quot;&quot;.equals(newValue)){
newValue = 0;
} else {
newValue = Integer.valueOf((String) newValue);
Object newValue = args[0];
if(&quot;&quot;.equals(newValue)){
newValue = 0;
} else {
newValue = Integer.parseInt((String) newValue);

