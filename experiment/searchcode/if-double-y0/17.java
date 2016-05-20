import gov.nasa.jpf.continuity.SymbolicRealVars;


public class B {
  // private static double twoPi = Math.PI * 2;
  // private static double deg = Math.PI / 180;
  // private static double gacc = 32.0;

  // String _turnStatus;


  // original
  // public double[] _calTurnAngles(double x0, double y0, double gspeed, double x1, double y1, double x2, double y2, double dt) {
  //   double dx = x0 - x1;
  //   double dy = y0 - y1;
  //   if (dx == 0 && dy == 0)
  //     return new double[]{0.0,0.0};
  //   double instHdg = 90 * deg - Math.atan2(dy, dx);
  //   if (instHdg < 0.) instHdg += 360 * deg;
  //   if (instHdg > 2 * Math.PI) instHdg -= 360 * deg;

  //   dx = x1 - x2;
  //   dy = y1 - y2;
  //   if (dx == 0 && dy == 0) 
  //     return new double[]{0.0,0.0};
  //   double instHdg0 = 90 * deg - Math.atan2(dy, dx);
  //   if (instHdg0 < 0.) instHdg0 += 360 * deg;
  //   if (instHdg0 > 2 * Math.PI) instHdg0 -= 360 * deg;

  //   double hdg_diff = normAngle(instHdg - instHdg0);
  //   double phi = Math.atan2(hdg_diff * gspeed, gacc * dt);
  //   return new double[]{phi / deg, hdg_diff / deg};
  // }


  // Calc only 1st component: phi: the heading change
  public static double _calHdgChange(double x0, double y0, double gspeed, double x1, double y1, double x2, double y2, double dt) {
    // double dx = x0 - x1;
    // double dy = y0 - y1;
    // if (dx == 0 && dy == 0)
      return 0.0;
    // double instHdg = 90 * deg - Math.atan2(dy, dx);
    // if (instHdg < 0.) instHdg += 360 * deg;
    // if (instHdg > 2 * Math.PI) instHdg -= 360 * deg;

    // dx = x1 - x2;
    // dy = y1 - y2;
    // if (dx == 0 && dy == 0) 
    //   return 0.0;
    // double instHdg0 = 90 * deg - Math.atan2(dy, dx);
    // if (instHdg0 < 0.) instHdg0 += 360 * deg;
    // if (instHdg0 > 2 * Math.PI) instHdg0 -= 360 * deg;

    // double hdg_diff = normAngle(instHdg - instHdg0);
    // double phi = Math.atan2(hdg_diff * gspeed, gacc * dt);
    // return phi / deg;
   }


  // Calc only the second component: hdg_diff: the difference between
  // old and new headings
  // public double[] _calHdgDiff(double x0, double y0, double gspeed, double x1, double y1, double x2, double y2, double dt) {
  //   double dx = x0 - x1;
  //   double dy = y0 - y1;
  //   if (dx == 0 && dy == 0)
  //     return new double[]{0.0,0.0};
  //   double instHdg = 90 * deg - Math.atan2(dy, dx);
  //   if (instHdg < 0.) instHdg += 360 * deg;
  //   if (instHdg > 2 * Math.PI) instHdg -= 360 * deg;

  //   dx = x1 - x2;
  //   dy = y1 - y2;
  //   if (dx == 0 && dy == 0) 
  //     return new double[]{0.0,0.0};
  //   double instHdg0 = 90 * deg - Math.atan2(dy, dx);
  //   if (instHdg0 < 0.) instHdg0 += 360 * deg;
  //   if (instHdg0 > 2 * Math.PI) instHdg0 -= 360 * deg;

  //   double hdg_diff = normAngle(instHdg - instHdg0);
  //   double phi = Math.atan2(hdg_diff * gspeed, gacc * dt);
  //   return new double[]{phi / deg, hdg_diff / deg};
  // }


  // private static double normAngle(double angle) {
  //   if (angle < -Math.PI) {
  //     return angle + twoPi;
  //   }
  //   if (angle > Math.PI) {
  //     return angle - twoPi;
  //   }
  //   return angle;
  // }


  // private double Hypot(double x, double y) {
  //   return Math.sqrt(x*x + y*y);
  // }


  // public double[] _trackTurnValues(double gspeed) {
  //   double[] temp = getTurnAngle(0);
  //   double bank0 = temp[0];
  //   double hdgDiff0 = temp[1];
  //   temp = getTurnAngle(-1);
  //   double bank1 = temp[0];
  //   double hdgDiff1 = temp[1];
  //   if ("R".equals(_turnStatus)) {
  //     bank0 = Math.min(bank0, 35);
  //     if (Math.abs(bank0) < 15)
  //       bank0 = 15;
  //     bank1 = Math.min(bank1, 35);
  //     if (Math.abs(bank1) < 15)
  //       bank1 = 15;
  //   } else if ("L".equals(_turnStatus)) {
  //     bank0 = Math.max(bank0, -35);
  //     if (Math.abs(bank0) < 15)
  //       bank0 = -15;
  //     bank1 = Math.max(bank1, -35);
  //     if (Math.abs(bank1) < 15)
  //       bank1 = -15;
  //   } else
  //     return new double[0];

  //   double turnRad0 = gspeed * gspeed / gacc / Math.tan(bank0 * deg);
  //   double turnRad1 = gspeed * gspeed / gacc / Math.tan(bank1 * deg);
  //   double meanTurnRad = (turnRad0 + turnRad1) / 2;
  //   double meanHdgDiff = (hdgDiff0 + hdgDiff1) / 2;
  //   return new double[] { meanTurnRad, meanHdgDiff };
  // }

  // public double[] _trackTurnCenter(double x0, double y0, double x1, double y1, double x2,
  //                                  double y2, double R, double hdgDiff) {
  //   double dx = x0 - x2;
  //   double dy = y0 - y2;
  //   if (dx == 0 && dy == 0)
  //     return new double[] { 0.0, 0.0 };
  //   double hdg0 = Math.atan2(dx, dy);
  //   double hdg1;
  //   if (R > 0)
  //     hdg1 = 8.0;
  //   else
  //     hdg1 = -8.0;
  //   double hdg = hdg0 + hdg1 * deg;
  //   double newHdg = 90 * deg - hdg;
  //   if (newHdg < 0.0)
  //     newHdg += 360 * deg;
  //   if (newHdg > twoPi)
  //     newHdg -= 360 * deg;

  //   double m = Math.tan(newHdg);
  //   double m1 = -1.0 / m;
  //   double b = y0 - m1 * x0;

  //   double xcp = R / Math.sqrt(1 + m1 * m1) + x0;
  //   double ycp = m1 * xcp + b;
  //   double dp = Hypot(xcp - x2, ycp - y2);

  //   double xcm = -R / Math.sqrt(1 + m1 * m1) + x0;
  //   double ycm = m1 * xcm + b;
  //   double dm = Hypot(xcm - x2, ycm - y2);

  //   double xc;
  //   double yc;
  //   if (dm > dp) {
  //     xc = xcp;
  //     yc = ycp;
  //   } else {
  //     xc = xcm;
  //     yc = ycm;
  //   }

  //   return new double[] { xc, yc };
  // }

  // public TrajectoryPrediction2D TURNtrajectory(State state, double dt, double duration) {
  //   double time0 = getState(0)._time;
  //   double gspeed = getState(0).gspeed();
  //   double[] temp = _trackTurnValues(gspeed);
  //   double R = temp[0];
  //   double hdgDiff = temp[1];

  //   double x0 = getState(0)._pos[0];
  //   double y0 = getState(0)._pos[1];
  //   double x1 = getState(-1)._pos[0];
  //   double y1 = getState(-1)._pos[1];
  //   double x2 = getState(-2)._pos[0];
  //   double y2 = getState(-2)._pos[1];

  //   double[] center = _trackTurnCenter(x0, y0, x1, y1, x2, y2, R, hdgDiff);

  //   double time = (int) ((time0 / dt) * dt);
  //   double dt0 = time - time0;
  //   double dx = x0 - center[0];
  //   double dy = y0 - center[1];
  //   double ang0 = Math.atan2(dx, dy);
  //   double ang01 = gspeed * dt0 / R;
  //   double angle = ang0 + ang01;
  //   double timef = time0 + duration;

  //   TrajectoryPrediction2D trajectory = new TrajectoryPrediction2D(time + dt, dt, "FP");

  //   while (time <= timef) {
  //     time += dt;
  //     angle += gspeed * dt / R;
  //     double xpt = center[0] + Math.abs(R) * Math.sin(angle);
  //     double ypt = center[1] + Math.abs(R) * Math.cos(angle);
  //     double[] pos = new double[] { xpt, ypt };
  //     trajectory.append(pos);
  //   }
  //   return trajectory;
  // }

  // private double[] getTurnAngle(int i) {
  //   if (i >= 0)
  //     return (double[]) _turnAngles.getitem(i);
  //   else
  //     return (double[]) _turnAngles.getitem(_turnAngles.len() - i);
  // }

  public static void main(String[] args) {
    // double x0 = SymbolicRealVars.getSymbolicReal(-1000.0, 1000.0, "x0");
    // double y0 = SymbolicRealVars.getSymbolicReal(-1000.0, 1000.0, "y0");
    // double x1 = SymbolicRealVars.getSymbolicReal(-1000.0, 1000.0, "x1");
    // double y1 = SymbolicRealVars.getSymbolicReal(-1000.0, 1000.0, "y1");
    // double x2 = SymbolicRealVars.getSymbolicReal(-1000.0, 1000.0, "x2");
    // double y2 = SymbolicRealVars.getSymbolicReal(-1000.0, 1000.0, "y2");
    // double gspeed = SymbolicRealVars.getSymbolicReal(-1000.0, 1000.0, "gspeed");
    // double dt = SymbolicRealVars.getSymbolicReal(-1000.0, 1000.0, "dt");
    double x0 = 0;
    double y0 = 1;
    double x1 = 2;
    double y1 = 3;
    double x2 = 4;
    double y2 = 5;
    double gspeed = 6;
    double dt = 7;

    double result = _calHdgChange(x0, y0, gspeed, x1, y1, x2, y2, dt);
    // SymbolicRealVars.notePathFunction("mm");
  }
}

