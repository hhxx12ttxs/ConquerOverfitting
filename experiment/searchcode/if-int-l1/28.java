public class LengthofLastWord {
public int lengthOfLastWord(String s) {
if(s==null || s.length()==0) return 0;
int l1=s.length()-1;
if(l1 < 0)
return 0;
else if(l1 == 0)
return 1;

int l2 = l1-1;

