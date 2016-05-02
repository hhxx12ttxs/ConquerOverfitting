package trussoptimizater.Truss.Elements;

import trussoptimizater.Truss.JMatrix.JMatrix;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Observable;
import trussoptimizater.Truss.Events.ModelEvent;
import trussoptimizater.Truss.Materials.Material;
import trussoptimizater.Truss.Sections.TubularSection;

/**
 * This class represents a an element connecting two nodes. This class can be used in the
 * two following cases
 * <p>
 * <ul>
 * <li>Bar - pinned-pinned restraint - model axial force
 * <li>Beam - fixed - fixed restraint - model axial force, bending moment and shear force
 * </ul><p>
 * @author Chris
 */
public class Bar extends Element implements java.util.Observer {

    /**
     * The First node that the bar element is attached too
     */
    private Node n1;
    /**
     * The second node that the bar element is attached too
     */
    private Node n2;
    /**
     * This is the angle of the bar in radians. The Angle is measured positive
     * clockwise from zero at 3o'clock.
     */
    private double angle = -1;
    /**
     * Length of bar in cm.
     */
    private double length;
    /**
     * The section that has been assigned to a bar
     */
    private TubularSection section;
    /**
     * Default section that is used if a bar has not been assigned one
     */
    public static final TubularSection DEFAULT_SECTION = new TubularSection(2.38, 1.7, 3.2, 26.9, "CHS 26.9 x 3.2");
    /**
     * The bar restraint, this can be one of two possible values
     *<ul>
     * <li>Bar.PINNED_PINNED_RESTRAINT
     * <li>Bar.FIXED_FIXED_RESTRAINT
     * </ul>
     */
    private String restraint;
    /**
     * Type of restraint and means that the Bar is only subject to axial forces
     */
    public static final String PINNED_PINNED_RESTRAINT = "PINNED-PINNED";
    /**
     * Type of restraint and means that the Bar is subject to axial, moment and shear forces.
     */
    public static final String FIXED_FIXED_RESTRAINT = "FIXED-FIXED";
    /**
     * The material that the bar is made out of.
     */
    private Material material;
    /**
     * The transformation matrix is used to convert local coordinates to global coordinates and visa versa
     */
    private JMatrix transformationMatrix;
    /**
     * This vector represents the displacement of the bar. The length of this vector is defined by
     * FrameAnalyzer.NO_DEFLECTION_POINTS.
     */
    private JMatrix displacementVector;
    private JMatrix stiffnessMatrix;
    private JMatrix forceVector;
    /**
     *  Axial Force measured in KN
     *<ul>
     * <li>Positive axial force means it is in compression
     * <li>Negative axial force means it is in tension
     * </ul>
     * <p>
     */
    private double axialForce = 0;
    /**
     * Shear Force at the nodes of the bar in KN
     */
    private double[] shearForce = {0, 0};
    /**
     * Moment at the nodes of the bar in KNM
     */
    private double[] momentForce = {0, 0};
    /**
     * Degrees of freedom bar has when restraint is FIXED-FIXED
     */
    public static final int FIXED_DOF = 3;
    /**
     * Degrees of freedom bar has when restraint is PINNED-PINNED
     */
    public static final int PINNED_DOF = 2;
    /**
     * An array of points represented the deflected shape of the bar. The number of elements will be
     * equal to Bar.NO_DEFLECTION_POINTS.
     */
    private Point2D[] barDeflections;

    public Bar(int barNumber, Node n1, Node n2, Material material) {
        this(barNumber, n1, n2, material, Bar.PINNED_PINNED_RESTRAINT, Bar.DEFAULT_SECTION);
    }

    public Bar(int barNumber, Node n1, Node n2, Material material, String restraint) {
        this(barNumber, n1, n2, material, restraint, Bar.DEFAULT_SECTION);
    }

    public Bar(int barNumber, Node n1, Node n2, Material material, String restraint, TubularSection section) {
        super(barNumber);
        this.n1 = n1;
        this.n2 = n2;
        n1.addObserver(this);
        n2.addObserver(this);
        this.section = section;
        this.restraint = restraint;
        this.material = material;
        updateBar();
    }

    /**
     * update bar attributes such as length and angle when node cordinates are changes,
     * Bar observes node changes and this method is called after any node changes
     */
    private void updateBar() {
        updateAngle();
        updateLength();
    }

    /**
     * updates angle of bar if nodes cordinates change, cant use methods such as
     * isVertical() and isLocalZAxisPositive() as they use the bars angle
     */
    private void updateAngle() {
        double ydiff = n2.z - n1.z;
        double xdiff = n2.x - n1.x;
        int quadrant = getQuadrant(xdiff, ydiff);

        if (quadrant == 1) {
            angle = Math.toRadians(270) + Math.atan(Math.abs(xdiff / ydiff));
        } else if (quadrant == 2) {
            angle = Math.atan(Math.abs(ydiff / xdiff));
        } else if (quadrant == 3) {
            angle = Math.toRadians(90) + Math.atan(Math.abs(xdiff / ydiff));
        } else if (quadrant == 4) {
            angle = Math.toRadians(180) + Math.atan(Math.abs(ydiff / xdiff));
        } else if (ydiff == 0 && xdiff > 0) {//ie horizntal going left --> right
            angle = Math.toRadians(0);
        } else if (ydiff == 0 && xdiff < 0) {//ie horizntal going  right--> left
            angle = Math.toRadians(180);
        } else if (xdiff == 0 && ydiff > 0) {//ie vertical going top --> bottom
            angle = Math.toRadians(90);
        } else if (xdiff == 0 && ydiff < 0) {//ie vertical goign bottom --> top
            angle = Math.toRadians(270);
        }
    }

    /**
     * 
     * @param xdiff  difference between x cordinates of nodes
     * @param ydiff  difference between y cordinates of nodes
     * @return quadrant bar is in. If Bar is vertical or horizontal returns -1.
     */
    private int getQuadrant(double xdiff, double ydiff) {
        int quadrant = -1;
        if (xdiff > 0 && ydiff < 0) {//ie in quadrant 1 - correct
            quadrant = 1;
        } else if (xdiff > 0 && ydiff > 0) {//ie quandrant 2 - correct
            quadrant = 2;
        } else if (xdiff < 0 && ydiff > 0) {//ie quandrant 3
            quadrant = 3;
        } else if (xdiff < 0 && ydiff < 0) {//ie quadrant 4 - correct
            quadrant = 4;
        }
        return quadrant;
    }

    /**
     * Update the length of the bar if node cordinates change
     */
    private void updateLength() {
        this.length = Math.sqrt(Math.pow(n2.x - n1.x, 2) + Math.pow(n2.z - n1.z, 2));
    }

    /**
     * Clone the bar
     * @return clone
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Bar barClone = (Bar) super.clone();
        //Need to clone both nodes and also section
        barClone.n1 = (Node) this.n1.clone();
        barClone.n2 = (Node) this.n2.clone();
        barClone.material = (Material) this.material.clone();
        barClone.section = (TubularSection) this.section;
        return barClone;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj == this){
            return true;
        }

        if(obj instanceof Bar){
            Bar b = (Bar)obj;
            if(this.n1.equals(b.n1) &&
                    this.n2.equals(b.n2) &&
                    this.angle == b.angle &&
                    this.length == b.length &&
                    this.restraint.equals(b.restraint) &&
                    this.number == b.number){
                return true;
            }
        }

        return false;


    }

    @Override
    public int hashCode() {
        return this.n1.hashCode() + this.n2.hashCode() + this.number;
    }

    /**
     *
     * @return String representation of bar
     */
    @Override
    public String toString() {
        DecimalFormat DF = new DecimalFormat("#.##");
        return "[Bar, node1, node2, length, angle, selected, section]  "
                + "[" + this.number + ", "
                + n1.getNumber() + ", "
                + n2.getNumber() + ", "
                + DF.format(length / 100) + ", "
                + DF.format(Math.toDegrees(angle)) + ", "
                + this.selected + ", "
                + this.section + " ]";
    }

    /*
     * This method sets the the bar stiffness matrix which depends on the cordinates
     * of the nodes and the angle of the bar
     */
    public void calculateBeamStiffnessMatrix() {
        JMatrix localstiffnessMatrix = new JMatrix(Bar.FIXED_DOF * 2, Bar.FIXED_DOF * 2);
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        transformationMatrix = new JMatrix(Bar.FIXED_DOF * 2, Bar.FIXED_DOF * 2);


        transformationMatrix.set(0, 0, c);
        transformationMatrix.set(0, 1, s);
        transformationMatrix.set(1, 0, -s);
        transformationMatrix.set(1, 1, c);
        transformationMatrix.set(2, 2, 1);
        transformationMatrix.set(3, 3, c);
        transformationMatrix.set(3, 4, s);
        transformationMatrix.set(4, 3, -s);
        transformationMatrix.set(4, 4, c);
        transformationMatrix.set(5, 5, 1);

        double A = this.section.getArea() * 0.0001;//m^2
        double I = this.section.getIxx() * 0.00000001;//m^4
        double E = material.getYoungsModulus();//KN/m^2
        double L = length / 100;//m

        localstiffnessMatrix.set(0, 0, E * A / L);
        localstiffnessMatrix.set(0, 3, -E * A / L);
        localstiffnessMatrix.set(3, 0, -E * A / L);
        localstiffnessMatrix.set(3, 3, E * A / L);

        //only need to add this matrix elements if the bar is being modelled as a beam
        if (this.restraint.equals(Bar.FIXED_FIXED_RESTRAINT)) {
            localstiffnessMatrix.set(1, 1, 12 * E * I / Math.pow(L, 3));
            localstiffnessMatrix.set(1, 2, -6 * E * I / Math.pow(L, 2));
            localstiffnessMatrix.set(1, 4, -12 * E * I / Math.pow(L, 3));
            localstiffnessMatrix.set(1, 5, -6 * E * I / Math.pow(L, 2));

            localstiffnessMatrix.set(2, 1, -6 * E * I / Math.pow(L, 2));
            localstiffnessMatrix.set(2, 2, 4 * E * I / L);
            localstiffnessMatrix.set(2, 4, 6 * E * I / Math.pow(L, 2));
            localstiffnessMatrix.set(2, 5, 2 * E * I / L);

            localstiffnessMatrix.set(4, 1, -12 * E * I / Math.pow(L, 3));
            localstiffnessMatrix.set(4, 2, 6 * E * I / Math.pow(L, 2));
            localstiffnessMatrix.set(4, 4, 12 * E * I / Math.pow(L, 3));
            localstiffnessMatrix.set(4, 5, 6 * E * I / Math.pow(L, 2));

            localstiffnessMatrix.set(5, 1, -6 * E * I / Math.pow(L, 2));
            localstiffnessMatrix.set(5, 2, 2 * E * I / L);
            localstiffnessMatrix.set(5, 4, 6 * E * I / Math.pow(L, 2));
            localstiffnessMatrix.set(5, 5, 4 * E * I / L);
        }

        //stiffness matrix = T-transpose *  local * T 
        stiffnessMatrix = transformationMatrix.transpose().times(localstiffnessMatrix).times(transformationMatrix);

    }


    /*
     * This method sets the the bar stiffness matrix which depends on the cordinates
     * of the nodes and the angle of the bar
     */
    public void calculateBarStiffnessMatrix() {

        double c = Math.cos(angle);
        double s = Math.sin(angle);
        double A = this.section.getArea() * 0.0001;//m^2
        double E = material.getYoungsModulus();//KN/m^2
        double L = length / 100;//m


        double[][] stiffnessMatrixArray = {{Math.pow(c, 2), c * s, -Math.pow(c, 2), -c * s},
            {c * s, Math.pow(s, 2), -c * s, -Math.pow(s, 2)},
            {-Math.pow(c, 2), -c * s, Math.pow(c, 2), c * s},
            {-c * s, -Math.pow(s, 2), c * s, Math.pow(s, 2)}};

        transformationMatrix = new JMatrix(stiffnessMatrixArray);
        stiffnessMatrix = transformationMatrix.times(A * E / L);


    }

    /**
     * Determines whether the bar is horizontal using global axis
     * @return boolean 
     */
    public boolean isHorizontal() {
        if (this.angle == Math.toRadians(0) || this.angle == Math.toRadians(180)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines whether the bar is vertical using global axis
     * @return boolean 
     */
    public boolean isVertical() {
        if (this.angle == Math.toRadians(90) || this.angle == Math.toRadians(270)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Deterimine if bar is in tension ie Axial Force < 0
     * @return boolean
     */
    public boolean isInTension() {
        if (this.axialForce < 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if local x axis of bar is positive ie left --> right
     * @return boolean
     */
    public boolean isLocalXAxisPositive() {
        if (angle >= Math.toRadians(0) && angle < Math.toRadians(90)) {
            return true;
        } else if (angle > Math.toRadians(270) && angle <= Math.toRadians(360)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if local z axis of bar is positive ie top --> bottom
     * @return boolean
     */
    public boolean isLocalZAxisPositive() {
        if (angle > Math.toRadians(0) && angle < Math.toRadians(180)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return node 1
     */
    public Node getNode1() {
        return n1;
    }

    /**
     *
     * @return node 2
     */
    public Node getNode2() {
        return n2;
    }

    /**
     *
     * @return the mid point of the bar
     */
    public Point2D.Double getMidPoint() {
        return new Point2D.Double(n1.x + (n2.x - n1.x) / 2, n2.z - (n2.z - n1.z) / 2);
    }

    /**
     *
     * @return length of beam in cm
     */
    public double getLength() {
        return length;
    }

    /**
     *
     * @return force vector
     */
    public JMatrix getForceVector() {
        return forceVector;
    }

    /**
     *
     * @return stifness matrix of order 6 by 6
     */
    public JMatrix getStiffnessMatrix() {
        return stiffnessMatrix;
    }

    /**
     *
     * @return Section that has been assigned to bar
     */
    public TubularSection getSection() {
        return section;
    }

    /**
     * If bar is in tension or compression stress should be less than TrussModel.fyo
     * @return bar stress in KN/mm^2
     */
    public double getStress() {
        return axialForce / (this.section.getArea() * 100);
    }

    /**
     * If bar is in compression check that axialforce is less than the max
     * @return maximum allowable compression force that a bar section and length can witstand before buckling in KN
     */
    public double getMaxCompressionAxialForce() {
        /*double effectiveLength = 0.8 * (length * 10); //mm

        double radiusGyration = Math.sqrt((section.getIxx() * 10000) / (section.getArea() * 100));
        double lambda = effectiveLength / radiusGyration;
        double lambdaBar = lambda / (Math.PI * Math.sqrt(material.getYoungsModulus() * Math.pow(10, -6) / material.getYieldStrength()));
        //As using closed hot finsihed sections buckling curve is always curve a [see eurocodes Table 6.1]
        double alphaLT = 0.21;
        double Io = 0.5 * (1 + alphaLT * (lambdaBar - 0.2) + Math.pow(lambdaBar, 2));
        double Xy = 1 / (Io + Math.sqrt(Math.pow(Io, 2) - Math.pow(lambdaBar, 2)));
        double Pc = (section.getArea() * 100) * Xy * material.getYieldStrength();
        return Pc;*/
        return this.getEulerBucklingForce();
    }

    /**
     * Returns the maximum axial load in KN that this beam can carry without buckling. Uses
     * the following formula to work it out:
     * <p>
     * F = pi^2*E*I / (K*L)^2
     * </p>
     *<p>
     * where
     * <ul>
     * <li>pi = 3.14
     * <li> E = youngs modulus
     * <li> I = Second moment of area
     * <li> K = column effective length factor
     * <li> L = unsupported length of column
     * </ul>
     * </p>
     * @return pi^2*E*I/ (K*L)^2 in KN
     */
    public double getEulerBucklingForce() {
        //k is the column effective length
        double k = 1;
        if (this.restraint.equals(Bar.FIXED_FIXED_RESTRAINT)) {
            k = 0.5;
        } else if (this.restraint.equals(Bar.PINNED_PINNED_RESTRAINT)) {
            k = 1.0;
        }

        double Ixx = section.getIxx() * Math.pow(10, -8); //m^4
        double L = this.length / 100; //m
        //System.out.println("top line is "+Math.pow(Math.PI,2)*Ixx );
        //System.out.println("Euler buckling force for bar "+this.number+" is "+Math.pow(Math.PI,2)*material.getYoungsModulus()*Ixx / Math.pow(k*L,2));

        return Math.pow(Math.PI, 2) * material.getYoungsModulus() * Ixx / Math.pow(k * L, 2);

    }

    /**
     * Returns the maximum stress allowed in this bar depending on section dimensions
     * <p>
     * Uses formula stress = F/A  + My/I
     * <p>
     *<ul>
     * <li>F - axial Force
     * <li>A - cross sectional area of section
     * <li>M - Moment at node
     * <li>y - distance to neutral axis
     * <li>I - Second moment of area of section
     * </ul>
     * <p>
     * Assumes section is symmetrical
     * @return maximum allowed stress in KN/mm^2
     */
    public double[] getMaxStress() {
        double[] maxStress = new double[2];
        maxStress[0] = this.getStress() + this.momentForce[0] * Math.pow(10, 3) * (this.section.getDiameter() / 2) / (this.section.getIxx() * Math.pow(10, 4));
        maxStress[1] = this.getStress() + this.momentForce[1] * Math.pow(10, 3) * (this.section.getDiameter() / 2) / (this.section.getIxx() * Math.pow(10, 4));
        return maxStress;
    }

    /**
     * Returns the minimum stress allowed in this bar depending on section dimensions
     * <p>
     * Uses formula stress = F/A  - My/I
     * <p>
     *<ul>
     * <li>F - axial Force
     * <li>A - cross sectional area of section
     * <li>M - Moment at node
     * <li>y - distance to neutral axis
     * <li>I - Second moment of area of section
     * </ul>
     * <p>
     * Assumes section is symmetrical
     * @return minimum allowed stress in KN/mm^2
     */
    public double[] getMinStress() {
        double[] minStress = new double[2];
        minStress[0] = this.getStress() - this.momentForce[0] * Math.pow(10, 3) * (this.section.getDiameter() / 2) / (this.section.getIxx() * Math.pow(10, 4));
        minStress[1] = this.getStress() - this.momentForce[1] * Math.pow(10, 3) * (this.section.getDiameter() / 2) / (this.section.getIxx() * Math.pow(10, 4));
        return minStress;
    }

    /**
     * Returns the maximum stress allowed in this bar depending on section dimensions
     * <p>
     * Uses formula stress = My/I
     * <p>
     *<ul>
     * <li>M - Moment at node
     * <li>y - distance to neutral axis
     * <li>I - Second moment of area of section
     * </ul>
     * <p>
     * Assumes section is symmetrical
     * @return maximum allowed stress in KN/mm^2
     */
    public double[] getMaxMomentStress() {
        double[] maxStress = new double[2];
        maxStress[0] = this.momentForce[0] * Math.pow(10, 3) * (this.section.getDiameter() / 2) / (this.section.getIxx() * Math.pow(10, 4));
        maxStress[1] = this.momentForce[1] * Math.pow(10, 3) * (this.section.getDiameter() / 2) / (this.section.getIxx() * Math.pow(10, 4));
        return maxStress;
    }

    /**
     * 
     * @return axial forxe in KN
     */
    public double getAxialForce() {
        return axialForce;
    }

    /**
     *
     * @return Transformation matrix used to transform local cordinates to global and visa-versa
     */
    public JMatrix getTransformMatrix() {
        return transformationMatrix;
    }

    /**
     *
     * @return moments for node 1 and node 2 in KNm
     */
    public double[] getMomentForce() {
        return momentForce;
    }

    /**
     *
     * @return shear force for node 1 and node 2 in KN
     */
    public double[] getShearForce() {
        return this.shearForce;
    }

    /**
     *
     * @return angle of bar in radians
     */
    public double getAngle() {
        return angle;
    }

    /**
     *
     * @return etiher Bar.FIXED or Bar.PINNED depending whether the bar is being modelled as a BAR or a BEAM
     */
    public String getRestraint() {
        return restraint;
    }

    /**
     *
     * @param deflectionScale
     * @return an array of 2D points representing the deflected shape of the bar/beam in cm
     */
    public Point2D[] getBarDeflectedPoints(double deflectionScale) {
        /*if(barDeflections == null){
        return null;
        }*/

        Point2D[] tempDeflectedPoints = new Point2D.Double[barDeflections.length];
        for (int i = 0; i < tempDeflectedPoints.length; i++) {
            //distance along beam
            double x = (length / (tempDeflectedPoints.length - 1)) * i;
            //try{
            tempDeflectedPoints[i] = new Point2D.Double(getPoint2DAtXAlongBeam(x).x + (barDeflections[i].getX() / 10) * deflectionScale, getPoint2DAtXAlongBeam(x).y + (barDeflections[i].getY() / 10) * deflectionScale);
            //}catch(NullPointerException ex){
            //  tempDeflectedPoints[i] = new Point2D.Double(getPoint2DAtXAlongBeam(x).x +deflectionScale , getPoint2DAtXAlongBeam(x).y +deflectionScale);
            //}
        }
        return tempDeflectedPoints;
    }

    public JMatrix getDisplacementVector() {
        return displacementVector;
    }

    /**
     * Returns a point along the beam in cm
     * @param x - distance along beam in cm
     * @return the point at x cm along the beam in double precision
     */
    public Point2D.Double getPoint2DAtXAlongBeam(double x) {

        double px = Math.abs(n2.x - n1.x) * x / length;
        double py = Math.abs(n2.z - n1.z) * x / length;


        //for horizontal bars
        if (isHorizontal() && isLocalXAxisPositive()) {
            return new Point2D.Double(n1.x + px, n1.z + py);
        } else if (isHorizontal() && !isLocalXAxisPositive()) {
            return new Point2D.Double(n1.x - px, n1.z + py);
        } //For vertical Bars
        else if (isVertical() && isLocalZAxisPositive()) {
            return new Point2D.Double(n1.x, n1.z + py);
        } else if (isVertical() && !isLocalZAxisPositive()) {
            return new Point2D.Double(n1.x, n1.z - py);
        } //For inclined bars
        else if (isLocalXAxisPositive() && isLocalZAxisPositive()) {
            return new Point2D.Double(n1.x + px, n1.z + py);
        } else if (isLocalXAxisPositive() && !isLocalZAxisPositive()) {
            return new Point2D.Double(n1.x + px, n1.z - py);
        } else if (!isLocalXAxisPositive() && isLocalZAxisPositive()) {
            return new Point2D.Double(n1.x - px, n1.z + py);
        } else if (!isLocalXAxisPositive() && !isLocalZAxisPositive()) {
            return new Point2D.Double(n1.x - px, n1.z - py);
        }

        return null;
        //return new Point2D.Double(n1.x + isLocalXAxisPositive() * px, n1.z + isLocalZAxisPositive() * py);
    }

    public Point getPointAtXAlongBeam(double x) {
        return new Point((int) getPoint2DAtXAlongBeam(x).x, (int) getPoint2DAtXAlongBeam(x).y);
    }

    /**
     * 
     * @return mass of bar in Kg
     */
    public double getMass() {

        return (length / 100) * (this.section.getArea() / 10000) * material.getDensity();
    }

    public Material getMaterial() {
        return material;
    }

    /**
     * 
     * @return weight of bar in KN
     */
    public double getWeight() {
        return getMass() * material.getGravity() / 1000;

    }

    /**
     *
     * @param s1 Shear force at node 1 in KN
     * @param s2 Shear force at node 2 in KN
     */
    public void setShearForce(double s1, double s2) {
        double[] shear = {s1, s2};
        this.shearForce = shear;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    /**
     *
     * @param m1 Moment at node 1 in KNM
     * @param m2 Moment at node 2 in KNM
     */
    public void setMomentForce(double m1, double m2) {
        double[] moments = {m1, m2};
        this.momentForce = moments;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    /**
     *
     * @param axialForce in KN
     */
    public void setAxialForce(double axialForce) {
        this.axialForce = axialForce;
    }

    /**
     * Only used by the joint analyser class which uses clone of real bars,
     * <p>
     * DO NOT USE THIS METHOD OTHERWISE as it will not update nodes, bar length etc.
     * @param angle
     */
    public void setAngle(double angle) {
        this.angle = angle;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    public void setDisplacementVector(JMatrix displacementVector) {
        this.displacementVector = displacementVector;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    /**
     * Set bar deflections in mm
     * @param barDeflections
     */
    public void setBarDeflections(Point2D[] barDeflections) {
        this.barDeflections = barDeflections;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));

    }

    /**
     *
     *
     * @param Restraint Should be one of the following values
     * <p>
     * <ul>
     * <li>Bar.FIXED_FIXED_RESTRAINT
     * <li>Bar.PINNED_PINNED_RESTRAINT
     * </ul>
     *
     */
    public void setRestraint(String Restraint) {
        this.restraint = Restraint;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    public void setNode2(Node n2) {
        this.n2 = n2;
        updateBar();
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    public void setNode1(Node n1) {
        this.n1 = n1;
        updateBar();
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    public void setForceVector(JMatrix forceVector) {
        this.forceVector = forceVector;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    /**
     *
     * @param Setion
     */
    public void setSection(TubularSection Setion) {
        this.section = Setion;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    public void setMaterial(Material material) {
        this.material = material;
        setChanged();
        notifyObservers(new ModelEvent(this, ModelEvent.ELEMENT_MODIFICATION));
    }

    /**
     * A Bar object observes its nodes and updates attributes such as length and angle when node cordinates are changed
     * @param o
     * @param arg
     */
    public void update(Observable o, Object arg) {
        updateBar();
    }
}//end of Line 


