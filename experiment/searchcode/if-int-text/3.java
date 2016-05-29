* Created by Madalina.Cozma on 2/27/2015.
*/
public class RailFence {

String encryption(String text, int lines) throws Exception {
int r = lines;
int length = text.length();
int c = length / lines;
char m[][] = new char[r][c];

