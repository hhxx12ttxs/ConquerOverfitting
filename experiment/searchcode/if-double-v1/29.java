public void set(double x, double y) {
set(x, y, 0, 0);
}


public static double dot(Vector v0, Vector v1) {
return (v0.x * v1.x + v0.y * v1.y + v0.z * v1.z + v0.w * v1.w);
}

/*
* If you were creating an artificial 3D vector by substituting z with some w, make sure z is actually what it&#39;s supposed to be

