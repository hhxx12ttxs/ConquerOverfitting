Integer[] lh = new Integer[hist.length];
for(int i = 0, ih = 0; i < hist.length; ++i){
lh[i] = ih;
ih = max(hist[i], ih);
Integer[] rh = new Integer[hist.length];
for(int i = hist.length, ih = 0; i > 0; ){
rh[--i] = ih;

