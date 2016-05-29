private void addStyleToRange(int start, int length, TextEditorStyle style)
{
if(length > 0)
{
splitStyleAtTextIndex(start);

int styleIndex = getStyleIndexFromTextIndex(start);
public void insertStylesToText(int textIndex, StyleList textStyles)
{
if(this.size() == 0)
{
this.addAll(textStyles);

