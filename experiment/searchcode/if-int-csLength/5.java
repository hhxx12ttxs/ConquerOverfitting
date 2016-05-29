return this == cs || (cs.length() == length &amp;&amp; regionMatches(0, cs, length));
}

public boolean startsWith(CharSequence cs) {
int csLength = cs.length();
int csLength = cs.length();

