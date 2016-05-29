int currentLower = 0;

if(lower < 1) {
isLower = false;
}

if(upper > 999999999) {
isUpper = false;
currentUpper = guesses[i] + answers[i];
currentLower = guesses[i] - answers[i];

if ((currentUpper == upper &amp;&amp; currentLower != lower)

