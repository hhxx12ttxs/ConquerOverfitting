mKernel = kernel;
mCharacter = character;
if (null == mKernel)
mKernel = &quot;&quot;;
ret = new StringBuffer (6 + 8 + 2); // max 8 in string
hex = Integer.toHexString (getCharacter ());
ret.append (&quot;\\u&quot;);
for (int i = hex.length (); i < 4; i++)

