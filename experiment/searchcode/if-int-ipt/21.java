public static final String Wildcard_key = &quot;wildcard&quot;;
public static IptArg Wildcard = new IptKey(Int_.Max_value, Wildcard_key);
public static IptArg[] parse_ary_or_empty(String v) {
IptArg[] rv = IptArg_.parse_ary_(v);
int len = rv.length;
for (int i = 0; i < len; i++)

