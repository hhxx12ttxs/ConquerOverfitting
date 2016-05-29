public class bits{
private long Bits;
private int col;
bits(int _i){
col=_i;
Bits=0L;
}
bits(long _Bits,int _i){
col=_i;
Bits=_Bits;
}
boolean shr(){
Bits=Bits<<1L;

