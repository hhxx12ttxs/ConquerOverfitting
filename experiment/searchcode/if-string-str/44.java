public String delDel(String str) {
if(str.length()<4) return str;

if(str.substring(1,4).equals(&quot;del&quot;)) return str.charAt(0)+str.substring(4);

return str;

}

