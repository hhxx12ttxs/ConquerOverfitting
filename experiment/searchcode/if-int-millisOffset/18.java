public class ControlTimer {

long millisOffset;

int ms, s, m, h, d;

float _mySpeed = 1;

int current, previous;
current = (int) time();
if (current > previous + 10) {
ms = (int) (current * _mySpeed);
s = (int) (((current * _mySpeed) / 1000));

