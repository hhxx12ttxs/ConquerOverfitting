private static final String strState;//=toStringState(state);
private static final int intVersion;
StringUtil.addZeros(releases,1)+&#39;.&#39;+
StringUtil.addZeros(patches,3);
intVersion=(major*1000000)+(minor*10000)+(releases*100)+patches;

