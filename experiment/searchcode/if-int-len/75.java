public class Solution {
public String longestPalindrome(String s) {

int maxLen=0, start=0, end=0;

for (int i=0; i<s.length(); i++) {

int lenA=searchCenter(s, i, i);

