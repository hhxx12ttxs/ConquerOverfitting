public void createPosition() {
int count = 0;
for( ; count < 9; count++) {
if(sessiondata.getSign(count) != Sign.Empty)
break;
}
if(count == 9) {
sessiondata.setSign(4, mySign);
return;
}
Sign yourSign = null;

