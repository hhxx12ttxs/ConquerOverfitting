* @author <a href=&quot;mailto:thomas.weckert@nexum.de&quot;>Thomas Weckert</a>
*/
public class CharacterUtils {

public static boolean isNewLine(Character c) {
public static void getLinePosition(CharSequence str, int position, Position line, Position linePosition) {

line.setPosition(1);
linePosition.setPosition(1);

Character lastChar = null;
int lastNewLinePos = 0;

