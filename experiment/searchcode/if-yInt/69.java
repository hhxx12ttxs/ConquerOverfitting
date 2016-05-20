package org.gvsig.baseclasses;

import java.awt.geom.PathIterator;
import java.util.ArrayList;

import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
/**
 * @author Vicente Caballero Navarro
 */
public abstract class GraphicOperator extends AbstractOperator{
	private FLyrVect lv=null;
	public void setLayer(FLyrVect lv) {
		this.lv=lv;
	}
	public FLyrVect getLayer() {
		return lv;
	}
	public abstract double process(Index index) throws DriverIOException;
	protected ArrayList getXY(IGeometry geometry) {
        ArrayList xs = new ArrayList();
        ArrayList ys = new ArrayList();
        ArrayList parts=new ArrayList();
        double[] theData = new double[6];

        //double[] aux = new double[6];
        PathIterator theIterator;
        int theType;
        int numParts = 0;
        boolean close=false;
        // boolean bFirst = true;
        // int xInt, yInt, antX = -1, antY = -1;
        theIterator = geometry.getPathIterator(null,FConverter.FLATNESS); //, flatness);

        // int numSegmentsAdded = 0;
        while (!theIterator.isDone()) {
            theType = theIterator.currentSegment(theData);

            switch (theType) {
            case PathIterator.SEG_MOVETO:
                if (numParts==0){
                	xs.add(new Double(theData[0]));
                	ys.add(new Double(theData[1]));
                }else{
                	 Double[] x = (Double[]) xs.toArray(new Double[0]);
                     Double[] y = (Double[]) ys.toArray(new Double[0]);
                     parts.add(new Double[][] { x, y });
                     xs.clear();
                     ys.clear();
                     xs.add(new Double(theData[0]));
                 	 ys.add(new Double(theData[1]));
                }
                numParts++;

                break;

            case PathIterator.SEG_LINETO:
                xs.add(new Double(theData[0]));
                ys.add(new Double(theData[1]));


                break;


            case PathIterator.SEG_CLOSE:
                xs.add(new Double(theData[0]));
                ys.add(new Double(theData[1]));
                Double[] x = (Double[]) xs.toArray(new Double[0]);
                Double[] y = (Double[]) ys.toArray(new Double[0]);
                parts.add(new Double[][] { x, y });
                xs.clear();
                ys.clear();
                close=true;
                break;
            } //end switch

            theIterator.next();
        } //end while loop

//        Double[] x = (Double[]) xs.toArray(new Double[0]);
//        Double[] y = (Double[]) ys.toArray(new Double[0]);
        if (!close){
        	xs.add(new Double(theData[0]));
            ys.add(new Double(theData[1]));
            Double[] x = (Double[]) xs.toArray(new Double[0]);
            Double[] y = (Double[]) ys.toArray(new Double[0]);
            parts.add(new Double[][] { x, y });
            xs.clear();
            ys.clear();
        }
        return parts;

    }

}

