* TokenStream created from a term vector field. The term vector requires positions and/or offsets (either). If you
* want payloads add PayloadAttributeImpl (as you would normally) but don&#39;t assume the attribute is already added just
* because you know the term vector has payloads, since the first call to incrementToken() will observe if you asked
* for them and if not then won&#39;t get them.  This TokenStream supports an efficient {@link #reset()}, so there&#39;s

