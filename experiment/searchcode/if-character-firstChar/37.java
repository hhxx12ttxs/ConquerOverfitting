default:
return false;
}
j++;
}

int i = 0;
char firstChar = &#39; &#39;;
while(i < length){
// If the character is in an even index set firstChar
if((i &amp; 1) == 0) {
firstChar = s.charAt(i);

