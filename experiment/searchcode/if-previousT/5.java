returnTokenData.timeRemaining = x - returnTokenData.timeElapsed;

if (t == previousT) {
nextOtp = privGenToken(t + 1);
returnTokenData.nextOtp = nextOtp;
return returnTokenData;
}
if ((t == previousT + 1) &amp;&amp; (nextOtp != null)) {

