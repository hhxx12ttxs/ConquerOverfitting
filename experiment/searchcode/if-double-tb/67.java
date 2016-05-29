TBuilder tb = new TBuilder();
tb.newLine();
tb.addProperty20(RnColumns.STORE_N, storeN, 6);
if (missing || added) {
if (missing) { tb.add(&quot;Missing, &quot;);}
if (added) { tb.add(&quot;Added&quot;);}
tb.newLine();

