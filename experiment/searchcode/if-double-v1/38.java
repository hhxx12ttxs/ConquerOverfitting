public static double similarity(Vector2D v1, Vector2D v2) {

double mag1 = v1.getNorm();
double mag2 = v2.getNorm();

if (mag1 == 0 || mag2 == 0) {
double cs = CosineSimilarity.similarity(v1, v2);
if (Double.isNaN(cs)) {
return Double.NaN;
}

return 1 - (Math.acos(cs) / Math.PI);
}
}

