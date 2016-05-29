package ex13_05;

public class Foo {
public String fooA(String source){
char[] chars = new char[source.length()];
StringBuffer sb = new StringBuffer();

for (int i = 0; i < source.length(); i++) {
if ((source.length() - i - 1) % 3 == 0 &amp;&amp; source.length() != i + 1)

