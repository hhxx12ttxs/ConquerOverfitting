uk = uk_prev + ts.SampleRate/2 * (ek + ek_prev);
ts.commandedPower = KI*(ek) + KP*(uk);
if(ts.commandedPower > maxEnginePower){
//System.out.println(&quot;ek: &quot; + ek + &quot; uk: &quot; + uk);
ek_prev = ek;
uk_prev = uk;
return ts.commandedPower;
}
public int shouldBrake(TrainState ts){ // either 1,2,or 0

