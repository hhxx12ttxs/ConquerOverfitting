int style = 0;
for (int flag : flags)
style |= flag;
if (style == 0)
return normal;
else
return new FontStyle(style);
public FontStyle with(int flag, boolean value) {
int style = this.style;
if (value)
style |= flag;
else
style &amp;= ~flag;

