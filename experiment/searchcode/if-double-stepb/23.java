logTa.append(&quot;「X->X+dX \n　dX->ωdX+C1r1(X0-X)+C2r2(Xg-X)」\n\n&quot;);
}
if(command.equals(&quot;Omega&quot;) || command.equals(&quot;Reset&quot;)){
double d=Double.valueOf(omegatf.getText());
if(0<=d &amp;&amp; d<=1)
omega=d;
logTa.append(&quot;慣性定数　ω=&quot;+omega+&quot; \n&quot;);

