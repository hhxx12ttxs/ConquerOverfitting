* Checks the DbCacheSize returns consistent results by comparing the
* calculated and measured values.  If this test fails, it probably means the
double actual,
double errorAllowed) {
if ((Math.abs(expected - actual) / expected) > errorAllowed) {

