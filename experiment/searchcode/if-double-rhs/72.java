} else if (lhs instanceof Integer &amp;&amp; rhs instanceof Double) {
return ((Integer) lhs).doubleValue() != ((Double) rhs).doubleValue();
} else if (lhs instanceof Double &amp;&amp; rhs instanceof Integer) {
return ((Double) lhs).doubleValue() != ((Integer) rhs).doubleValue();
} else if (lhs instanceof Double &amp;&amp; rhs instanceof Double) {
return ((Double) lhs).doubleValue() != ((Double) rhs).doubleValue();

