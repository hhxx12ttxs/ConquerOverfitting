public int lengthOfLastWord(String s) {
if (s == null || s.length() == 0) {
return 0;
}
int endIndex = s.length() - 1;
endIndex--;
}
if (endIndex < 0) {
return 0;
}
int end = endIndex;

