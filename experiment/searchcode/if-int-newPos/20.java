String cropped = title;
if(title.length() > Constants.PAC_STRING_MAX_SYMBOLS) {
int spacepos=0; int newpos = 0;
public static String crop(String title, int len) {
String cropped = title;
if(title.length() > len) {
int spacepos=0; int newpos = 0;

