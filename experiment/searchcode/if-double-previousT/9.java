public static void startMocking( final LocationManager locationManager, GpsManager gpsManager ) {
if (isOn) {
return;
try {
if (previousT < 0) {
Thread.sleep(2000);

