while (j < word.length() - 1 &amp;&amp; word.charAt(j) == word.charAt(j + 1)) {
j++;
}
final int csLen = j - i + 1;
if (lcsMap.containsKey(curr)) { // seen before, replace the exiting counter if needed
lcsMap.put(curr, csLen);
}
i = j + 1;
}

int maxLcsLength = 0;

