if (_na < _maxa) {
stepA();
// if not, try stepping up B
} else if (_nb < _maxb) {
stepB();
// if both are at the end, bail out
} else if (_na < _maxa) {
stepA();
// if both are at the end, bail out
} else {
_done = true;
}
}

if (_log.isTraceEnabled() &amp;&amp; _done) {

