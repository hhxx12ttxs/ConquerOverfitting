for(;;){
int pj = j;
j = seqEnd(s, j);
sb.append(j-pj);
s = sb.toString();
}
return s;
}
int seqEnd(String s, int i){
char c = s.charAt(i);
for(i = i+1; i < s.length(); ++i){

