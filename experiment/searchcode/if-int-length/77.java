public class StringCompare {

public int simpleDifference(String a, String b) {
int length = (a.length() > b.length()) ? b.length() : a.length();
int c = 0;
for(int i = 0; i < length; i++) {

