public double getPrevY() {return prevy;}

public double getVelocityX() {return (x-prevx)/GC.ACT_TIMER;}
public double getVelocityY() {return (y-prevy)/GC.ACT_TIMER;}
public void addVelocityX(double a_v_x) {prevx -= a_v_x*GC.ACT_TIMER;}

