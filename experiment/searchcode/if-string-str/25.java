public String atFirst(String str) {
if (str.length() <= 0) {
return &quot;@@&quot;;
}
if (str.length() <= 1) {
return str + &quot;@&quot;;
}
return str.substring(0, 2);
}

