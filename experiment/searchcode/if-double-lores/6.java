List<String> lores = iM1.getLore();
Double d = (double)cP.getGoldPoints();
String s = lores.get(3).replace(&quot;@c&quot;, d.toString());
lores.remove(3);
lores.add(s);
iM1.setLore(lores);

