public String getMatName () {
return matname;
}
public String getMatNameMain() {
String invalidchars=&quot;:\\/,.;&#39;!@#$%^&amp;*()-_+={}[]><?|`~ &quot;;
String m=matname;
for (int i=0;i<invalidchars.length();i++) {
int pos=m.indexOf(invalidchars.charAt(i));

