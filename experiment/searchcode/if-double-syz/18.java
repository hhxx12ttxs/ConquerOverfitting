public IVec4I mirror(double centerX, double centerY, double centerZ, double planeX, double planeY, double planeZ);


/** shear operation */
public IVec4I shear(double sxy, double syx, double syz,
double szy, double szx, double sxz);
public IVec4I shear(IVecI center, double sxy, double syx, double syz,
double szy, double szx, double sxz);

