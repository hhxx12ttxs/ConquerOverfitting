getSideInfo();
int flushMain = br.getBitCount() &amp; 7;
if (flushMain != 0) {
br.getBits(8 - flushMain);
ScaleFactor sfc = scaleFactors[ch];
int[] sfl = sfc.l;
int[][] sfs = sfc.s;
if (gi.windowSwitching &amp;&amp; gi.blockType == 2) {

