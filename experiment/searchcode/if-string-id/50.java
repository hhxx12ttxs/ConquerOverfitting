stringId = stringId.toUpperCase();
if (stringId == &quot;A&quot; || stringId == &quot;C&quot; || stringId == &quot;D&quot; || stringId == &quot;K&quot; || stringId == &quot;N&quot;) {
mIntId = null;
} else if (stringId == &quot;0L&quot; || stringId == &quot;0P&quot;) {
mType = Type.TRAM;
mStringId = stringId.charAt(1);

