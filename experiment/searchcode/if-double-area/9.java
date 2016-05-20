package com.iver.cit.gvsig.project.documents.table.operators;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.cresques.cts.IProjection;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.ExpressionFieldExtension;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeometryUtilities;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.tools.geo.Geo;
import com.iver.cit.gvsig.project.documents.table.GraphicOperator;
import com.iver.cit.gvsig.project.documents.table.IOperator;
import com.iver.cit.gvsig.project.documents.table.Index;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
/**
 * @author Vicente Caballero Navarro
 */
public class Area extends GraphicOperator{

	public String addText(String s) {
		return s.concat(toString()+"()");
	}
	public double process(Index index) throws DriverIOException {
		ReadableVectorial adapter = getLayer().getSource();
	   	IGeometry geom=null;
		try {
			geom = adapter.getShape(index.get());
		} catch (ExpansionFileReadException e) {
			throw new DriverIOException(e);
		} catch (ReadDriverException e) {
			throw new DriverIOException(e);
		}
		return GeometryUtilities.getArea(getLayer(), geom);
	}
	public void eval(BSFManager interpreter) throws BSFException {
		interpreter.declareBean("jarea",this,Area.class);
//		interpreter.eval(ExpressionFieldExtension.BEANSHELL,null,-1,-1,"double area(){return area.process(indexRow);};");
		interpreter.exec(ExpressionFieldExtension.JYTHON,null,-1,-1,"def area():\n" +
				"  return jarea.process(indexRow)");
	}
	public String toString() {
		return "area";
	}
	public boolean isEnable() {
		if (getLayer()==null)
			return false;
		ReadableVectorial adapter = getLayer().getSource();
		int type=FShape.POINT;
		try {
			type=adapter.getShapeType();
		} catch (ReadDriverException e) {
			NotificationManager.addError(e);
		}
		return (getType()==IOperator.NUMBER && type==FShape.POLYGON);
	}

	public String getTooltip(){
		return PluginServices.getText(this,"operator")+":  "+addText("")+"\n"+getDescription();
	}
	public String getDescription() {
        return PluginServices.getText(this, "returns") + ": " +
        PluginServices.getText(this, "numeric_value") + "\n" +
        PluginServices.getText(this, "description") + ": " +
        "Returns the area of polygon geometry of this row.";
    }
}

