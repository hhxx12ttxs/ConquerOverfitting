private double q1;
private double q2;
private double q3;

public Quaternion()
{
init();
}

public Quaternion(Quaternion q)
this.q0 = q.q0;
this.q1 = q.q1;
this.q2 = q.q2;
this.q3 = q.q3;
}

public void set(double q0, double q1, double q2, double q3)

