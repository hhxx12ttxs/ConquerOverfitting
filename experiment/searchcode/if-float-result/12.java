public static Corners cornerMerge(Corners first, Corners second){
Corners result = first;
if (second.b < result.b){
result.b = second.b;
}
if (second.t >result.t){
result.t =second.t;
}
if (second.l < result.l){
result.l = second.l;

