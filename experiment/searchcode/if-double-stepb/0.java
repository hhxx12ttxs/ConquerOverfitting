package sdt.stepb;


public class stepb_cartesian_point extends stepb_object
{


private math_vector3d      math_vector;
public stepb_cartesian_point(double ix, double iy, double iz)
{
super();
math_vector = new math_vector3d(ix,iy,iz);

