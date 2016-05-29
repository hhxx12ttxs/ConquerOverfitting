private double upperBound;
private double Sxy, Sxz, Syx, Syz, Szx, Szy;
private double SxxpSyy, Szz, mxEigenV, SyzmSzy,SxzmSzx, SxymSyx;
* @return root mean square deviation for superposition of y onto x
*/
public double getRmsd() {
if (! rmsdCalculated) {
calcRmsd(x, y);
}
return rmsd;

