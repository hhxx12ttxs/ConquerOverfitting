return x1*x2 + y1*y2 + z1*z2;
}

public static double getDotProduct(ImmutableDoubleVector3 v1, ImmutableDoubleVector3 v2) {
return getDotProduct(v1.getX(), v1.getY(), v1.getZ(), v2.getX(), v2.getY(), v2.getZ());

