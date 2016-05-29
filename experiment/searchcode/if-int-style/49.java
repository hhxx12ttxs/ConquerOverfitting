public static FontStyle fontStyle(int... flags) {
int style = 0;
for (int flag : flags)
style |= flag;
if (style == 0)
* Sets the given flag, like Bold, to the given value.
*/
public FontStyle with(int flag, boolean value) {
int style = this.style;
if (value)

