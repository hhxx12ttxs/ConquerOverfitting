this.inlineStyle = inlineStyle;
}
public CSSValue getProperty(int index) {
CSSValue v = inlineStyle.getProperty(index);
if ( v != null)
{
return v;
}
if ( style != null)
{
return style.getProperty(index);

