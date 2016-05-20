package trussoptimizater.Truss.Sections;

public class RHS extends TubularSection {
    /**
     * <p>
     * The section size should have the following format "D x B"
     * <\p>
     * where
     * <ul>
     * <li>D - Depth in mm
     * <li>B - Bredth in mm
     * </ul>
     *
     */
    private String sectionSize;

    /**
     * Bredth of section in mm
     */
    private double B;

    /**
     * Depth of section in mm
     */
    private double D;

    /**
     * Second moment of area around minor axis (y-y) in cm^4
     */
    private double Iyy;

    public RHS(String section, double B,double t,double D,double area, double Ixx, double Iyy){
        super(area,Ixx,t,D,"RHS "+section);
        this.sectionSize = section;
        this.B = B;
        this.D = D;
        this.Iyy = Iyy;
    }

    /**
     *
     * @return depth of section in mm
     */
    public double getDepth() {
        return D;
    }

    /**
     *
     * @return Second moment of area around the y-y axis (ie the minor axis) in cm^4
     */
    public double getIyy() {
        return Iyy;
    }

    /**
     *
     * @return bredth of section in mm
     */
    public double getBredth() {
        return B;
    }

    /**
     * For example if section was a RHS 50 x 30, this method would return "50 x 30"
     * @return RHS section size in mm
     */
    public String getSectionSize() {
        return sectionSize;
    }

    /*public void print(){
        System.out.println("Section: "+section+" B: "+B+" t: "+super.getThickness()+ " D: "+D+" Area: "+getArea()+" Ixx: "+super.getIxx()+" Iyy: "+Iyy);
    }*/

}//end of CHS class
