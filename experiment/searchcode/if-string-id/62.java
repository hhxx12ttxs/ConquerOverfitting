private final String name;

public IdNamePair(final int id, final String name) {
if (id < 0)
throw new IllegalArgumentException(&quot;id must be zero or positive&quot;);
this.name = name;
}

public IdNamePair(final long id, final String name) {
if (id < 0)
throw new IllegalArgumentException(&quot;id must be zero or positive&quot;);

