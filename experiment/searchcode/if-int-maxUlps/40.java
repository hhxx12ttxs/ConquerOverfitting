*            {@code (maxUlps - 1)} is the number of floating point values between
*            {@code x} and {@code y}.
* @return {@code true} if there are less than {@code maxUlps} floating point values
*         between {@code this} and {@code other}
*/
boolean tolerantlyEquals(T other, int maxUlps);
}

