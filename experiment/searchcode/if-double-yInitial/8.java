private String[] colour = {&quot;blue&quot;, &quot;green&quot;, &quot;red&quot;, &quot;yellow&quot;, &quot;orange&quot;, &quot;pink&quot;, &quot;cyan&quot;, &quot;purple&quot;};

public LaunchTube(double xPos, double yPos, double xVel, double yVel,
double newExitV, double newAngle, double variation)
//Velocity equals velocity of the emitter + the relative velocity of the spark (exit velocity)
double vXInitial = velocity[X] + getExitVelocity()*Math.sin(angle);
double vYInitial = velocity[Y] + getExitVelocity()*Math.cos(angle);

