public void ignoresPunctuationMixedWithSpaces () {
assertArrayEquals (nacoNormalizer.normalize (&quot;wharton, edith&quot;), nacoNormalizer.normalize (&quot;wharton edith&quot;));
assertArrayEquals (nacoNormalizer.normalize (&quot;st. john&quot;), nacoNormalizer.normalize (&quot;st john&quot;));

