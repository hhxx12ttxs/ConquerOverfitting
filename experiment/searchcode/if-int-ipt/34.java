, Up	= new IptMouseWheel(&quot;wheel.up&quot;)
, Down	= new IptMouseWheel(&quot;wheel.down&quot;);
public static IptMouseWheel parse(String raw) {
if		(String_.Eq(raw, None.Key()))	return None;
else if	(String_.Eq(raw, Down.Key()))	return Down;
else throw Err_.new_parse_type(IptMouseWheel.class, raw);

