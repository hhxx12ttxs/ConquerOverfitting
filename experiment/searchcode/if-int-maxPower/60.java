//out(&quot;perc:&quot;+perc+&quot; maxPower:&quot;+maxPower+&quot; powerleft:&quot;+powerLeft+&quot; &quot;+powerLeft/maxPower);
if (perc>20)
public int batteryComsumption(int e) {
if (powerLeft>=e){
powerLeft-=e;
e=0;
} else {
e-=(int)powerLeft;
powerLeft=0;

