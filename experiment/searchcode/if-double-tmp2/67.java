double tmp1 = this.x * other.x + this.y * other.y;
double tmp2 = Math.sqrt(this.x*this.x + this.y*this.y) * Math.sqrt(other.x*other.x + other.y*other.y);
double angle = Math.acos(tmp1/tmp2);
//drehrichtung
//TODO webair für was ist orientation? geht auch ohne!

