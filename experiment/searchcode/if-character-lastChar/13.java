List<Character> list = new LinkedList<Character>();
StringBuffer sb = new StringBuffer();
char lastchar = s.charAt(0);
int lastcount = 1;
list.add(lastchar);
for (int i = 1; i < s.length(); i++) {
if (s.charAt(i) == lastchar)

