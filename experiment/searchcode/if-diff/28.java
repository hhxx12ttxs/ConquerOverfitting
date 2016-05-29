for (DATA to : toList) {
if (fromList.contains(to)) continue;
DIFF diff = differ.diff(null, to);
if (diff != null &amp;&amp; diff.getType() != Diff.Type.EQUAL) result.add(diff);

