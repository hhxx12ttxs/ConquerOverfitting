private Quaternion _q0;
private Quaternion _q1;
private double _theta;
private double _sin_theta;
this._q0 = Q0.normalized();
this._q1 = Q1.normalized();

double cos_theta = this._q0.dotProduct( this._q1 );

if ( cos_theta < -1.0 )

