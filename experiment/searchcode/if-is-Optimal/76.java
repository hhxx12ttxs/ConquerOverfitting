public static <T> void checkOptimalPath(List<T> oneOptimal, List<T> actual, boolean uniqueOptimal) {
if (uniqueOptimal)
Assert.assertFalse(&quot;Actual path is empty but expected path not&quot;, actual.isEmpty() &amp;&amp; !oneOptimal.isEmpty());

if (!oneOptimal.isEmpty()) {
Assert.assertEquals(&quot;Actual path starts with the wrong element&quot;, oneOptimal.get(0), actual.get(0));

