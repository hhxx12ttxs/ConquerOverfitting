System.out.println(&quot;Error: &quot; + (gyroCorrection.get() - angle));
if (angle > 0)
return gyroCorrection.get() > angle;
else
return gyroCorrection.get() < angle;
}

@Override
protected void onStart() {
if(gyro != null)
gyroCorrection = gyro.getCorrection();

