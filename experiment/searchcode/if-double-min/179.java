<<<<<<< HEAD
/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.plexiViewer.utils.Transform;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortProcessor;

import org.nrg.plexiViewer.io.PlexiFileOpener;
import org.nrg.plexiViewer.lite.io.PlexiImageFile;
import org.nrg.plexiViewer.utils.PlexiConstants;

public class IntensitySetter {
    private ImagePlus image;
    private boolean reset;
    double min, max;
    boolean RGBImage;
	static final int AUTO_THRESHOLD = 5000;
	int channels = 7; // RGB
	int autoThreshold = 0;
	double defaultMin=0, defaultMax=255;

    public IntensitySetter(ImagePlus img, boolean reset) {
        image = img;
		int type = image.getType();
        RGBImage = type==ImagePlus.COLOR_RGB;
        ImageProcessor ip = image.getProcessor();
        if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
			image.resetDisplayRange();
			defaultMin = ip.getMin();
			defaultMax = ip.getMax();

		} else {
			defaultMin = 0;
			defaultMax = 255;
		}

        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        this.reset = reset;
    }
    
    
    
	void setMinAndMax(ImagePlus imp, double min, double max) {
		if (channels!=7 && imp.getType()==ImagePlus.COLOR_RGB)
			imp.setDisplayRange(min, max, channels);
		else
			imp.setDisplayRange(min, max);
	}

    
	public void autoAdjust(ImagePlus imp, ImageProcessor ip) {
 		if (RGBImage)
			ip.reset();
		Calibration cal = imp.getCalibration();
		imp.setCalibration(null);
		ImageStatistics stats = imp.getStatistics(); // get uncalibrated stats
		imp.setCalibration(cal);
		int limit = stats.pixelCount/10;
		int[] histogram = stats.histogram;
		if (autoThreshold<10)
			autoThreshold = AUTO_THRESHOLD;
		else
			autoThreshold /= 2;
		int threshold = stats.pixelCount/autoThreshold;
		int i = -1;
		boolean found = false;
		int count;
		do {
			i++;
			count = histogram[i];
			if (count>limit) count = 0;
			found = count> threshold;
		} while (!found && i<255);
		int hmin = i;
		i = 256;
		do {
			i--;
			count = histogram[i];
			if (count>limit) count = 0;
			found = count > threshold;
		} while (!found && i>0);
		int hmax = i;
		Roi roi = imp.getRoi();
		if (hmax>=hmin) {
			if (RGBImage) imp.killRoi();
			min = stats.histMin+hmin*stats.binSize;
			max = stats.histMin+hmax*stats.binSize;
			if (min==max)
				{min=stats.min; max=stats.max;}
			setMinAndMax(imp, min, max);
			if (RGBImage && roi!=null) imp.setRoi(roi);
		} else {
			reset(imp, ip);
			return;
		}
		if (roi!=null) {
			ImageProcessor mask = roi.getMask();
			if (mask!=null)
				ip.reset(mask);
		}
	}

	void reset(ImagePlus imp, ImageProcessor ip) {
 		if (RGBImage)
			ip.reset();
		if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
			imp.resetDisplayRange();
			defaultMin = ip.getMin();
			defaultMax = ip.getMax();
		}
		min = defaultMin;
		max = defaultMax;
		setMinAndMax(imp, min, max);
		autoThreshold = 0;
	}

	
	/** Restore image outside non-rectangular roi. */
  	void doMasking(ImagePlus imp, ImageProcessor ip) {
		ImageProcessor mask = imp.getMask();
		if (mask!=null)
			ip.reset(mask);
	}

	
	void setMinAndMax(ImagePlus imp, ImageProcessor ip) {
		min = ip.getMin();
		max = ip.getMax();
		Calibration cal = imp.getCalibration();
		int digits = (ip instanceof FloatProcessor)||cal.calibrated()?2:0;
		double minValue = cal.getCValue(min);
		double maxValue = cal.getCValue(max);
		minValue = cal.getRawValue(minValue);
		maxValue = cal.getRawValue(maxValue);
		if (maxValue>=minValue) {
			min = minValue;
			max = maxValue;
			setMinAndMax(imp, min, max);
			if (RGBImage) doMasking(imp, ip);
		}
	}
    
    public static void main(String args[]) {
        try {
    	PlexiImageFile pf = new PlexiImageFile();
        pf.setURIAsString("file:/C:/data/archive/FTest2/arc001/061121_tc22923/PROCESSED/MPRAGE/T88_111/061121_tc22923_mpr_n2_111_t88_gfc.4dfp");
        pf.setXsiType(PlexiConstants.XNAT_IMAGERESOURCE);
        PlexiFileOpener pfo = new PlexiFileOpener("ANALYZE",pf);
 
		ImageJ ij = IJ.getInstance();
	     if (ij == null || !ij.quitting()) {	// initialize IJ and make a window
	    	 new ImageJ().exitWhenQuitting(false);
	    	 ij = IJ.getInstance();
	    	 ij.setVisible(true);
	     }

        
        ImagePlus img = pfo.getImagePlus();
        IntensitySetter i = new IntensitySetter(img, true);
        i.autoAdjust(img, img.getProcessor());
        
        PlexiImageOrientor pio=new PlexiImageOrientor(img,pf.getFormat());
        ImagePlus img1 = pio.getImage(pfo.getOrientation(),"CORONAL"+"F");


//        BitConverter converter = new BitConverter();
//        converter.convertToGrayscale(img);


        BitConverter converter = new BitConverter();
        converter.convertToGrayscale(img1);

        img1.show();

        
        //        System.out.println("Image 8 bit");
//        i.setMinMax(img);

        System.out.println(img.getProcessor().getMin() + "   " + img.getProcessor().getMax());

        //img.show();
        }catch(Exception e) {e.printStackTrace();}
    }
}   
=======
/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is the VAST team at the University of Alabama in Huntsville (UAH). <http://vast.uah.edu> Portions created by the Initial Developer are Copyright (C) 2007 the Initial Developer. All Rights Reserved. Please Contact Mike Botts <mike.botts@uah.edu> for more information.
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.sweCommon;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Hashtable;
import org.w3c.dom.*;
import org.vast.cdm.common.*;
import org.vast.data.*;
import org.vast.xml.*;
import org.vast.ogc.OGCRegistry;
import org.vast.ogc.gml.GMLUnitReader;
import org.vast.sweCommon.IntervalConstraint;
import org.vast.unit.Unit;
import org.vast.unit.UnitParserUCUM;
import org.vast.unit.UnitParserURI;
import org.vast.util.DateTimeFormat;


/**
 * <p>
 * Reads SWE Components structures made of Scalar Parameters,
 * DataRecord, DataArray, etc. This is for version 1 of the standard.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @since Dec 19, 2006
 * @version 1.0
 */
public class SweComponentReaderV1 implements DataComponentReader
{
	protected final static String GML_NS = OGCRegistry.getNamespaceURI("GML");
	protected final static String tupleSeparator = " ";
	protected final static String tokenSeparator = ",";
    protected Hashtable<String, AbstractDataComponent> componentIds;
    protected AsciiDataParser asciiParser;
    protected GMLUnitReader unitReader = new GMLUnitReader();
    
    
    public SweComponentReaderV1()
    {
        componentIds = new Hashtable<String, AbstractDataComponent>();
        asciiParser = new AsciiDataParser();
    }


    public AbstractDataComponent readComponentProperty(DOMHelper dom, Element propertyElt) throws XMLReaderException
    {
        Element dataElement = dom.getFirstChildElement(propertyElt);
        String name = readPropertyName(dom, propertyElt);
        AbstractDataComponent container = readComponent(dom, dataElement);
        container.setName(name);
        
        return container;
    }
    
    
    public AbstractDataComponent readComponent(DOMHelper dom, Element componentElt) throws XMLReaderException
    {
        AbstractDataComponent container = null;
        String eltName = componentElt.getLocalName();
        
        if (dom.existElement(componentElt, "elementCount"))
        {
            container = readDataArray(dom, componentElt);
        }
        else if (eltName.endsWith("DataRecord")) // also handles SimpleDataRecord
        {
            container = readDataRecord(dom, componentElt);
        }
        else if (eltName.endsWith("Vector")) // handles everything endin with Vector
        {
            container = readDataRecord(dom, componentElt);
        }
        else if (eltName.endsWith("Range"))
        {
            container = readRange(dom, componentElt);
        }
        else 
        {
            container = readScalar(dom, componentElt);
        } 
        
        // add id to hashtable if present
        String id = dom.getAttributeValue(componentElt, "id");
        if (id != null)
        	componentIds.put(id, container);

        return container;
    }


    /**
     * Reads a DataRecord structure and all its fields
     * @param recordElt Element
     * @throws CDMException
     * @return DataGroup
     */
    private DataGroup readDataRecord(DOMHelper dom, Element recordElt) throws XMLReaderException
    {
        // parse all fields (can be a different element name than "field" if hard typed!!)
        NodeList componentList = dom.getAllChildElements(recordElt);
        int childCount = componentList.getLength();
        
        // create DataGroup to hold field definitions
        DataGroup dataGroup = new DataGroup(2);
        
        // loop through all child elements
        for (int i = 0; i < childCount; i++)
        {
            Element childElt = (Element)componentList.item(i);
            
            // skip everything in GML namespace (gml:metadata, gml:description, gml:name)
            if (childElt.getNamespaceURI().contains(GML_NS))
                continue;
            
            // add field components
            DataComponent dataComponent = readComponentProperty(dom, childElt);
            dataGroup.addComponent(readPropertyName(dom, childElt), dataComponent);
        }        
        
        // error if no field present
        if (dataGroup.getComponentCount() == 0)
            throw new XMLReaderException("Invalid DataRecord: Must have AT LEAST ONE field");

        // read common stuffs
        readGmlProperties(dataGroup, dom, recordElt);
        readCommonAttributes(dataGroup, dom, recordElt);

        return dataGroup;
    }


    /**
     * Reads a DataArray structure the unique member
     * @param arrayElt Element
     * @throws CDMException
     * @return DataArray
     */
    private DataArray readDataArray(DOMHelper dom, Element arrayElt) throws XMLReaderException
    {
        int arraySize = 1;
        DataArray dataArray = null;
        
        // if elementCount is referencing another component
        String countId = dom.getAttributeValue(arrayElt, "elementCount/@ref");
        if (countId != null)
        {
            DataComponent sizeComponent = componentIds.get(countId);
            if (sizeComponent == null)
                throw new XMLReaderException("Invalid elementCount: The elementCount property must reference an existing Count component");
            dataArray = new DataArray((DataValue)sizeComponent, true);
        }
        
        // else if elementCount is given inline
        else
        {
            try
            {
            	Element countElt = dom.getElement(arrayElt, "elementCount/Count");
                DataValue sizeComponent = this.readScalar(dom, countElt);
                String countValue = dom.getElementValue(countElt, "value");
                
                if(countValue != null)
                {
                	arraySize = Integer.parseInt(countValue);
                	if (arraySize < 0)
                		throw new XMLReaderException("Invalid elementCount: The elementCount must specify a positive integer value");                
                	dataArray = new DataArray(arraySize);
                }
                else dataArray = new DataArray(sizeComponent, true);
            }
            catch (Exception e)
            {
                throw new XMLReaderException("Invalid elementCount: The elementCount must specify a positive integer value");
            }
        }
                        
        // read array component
        Element elementTypeElt = dom.getElement(arrayElt, "elementType");
        DataComponent dataComponent = readComponentProperty(dom, elementTypeElt);
        dataArray.addComponent(dataComponent);
        
        // read common stuffs
        readGmlProperties(dataArray, dom, arrayElt);
        readCommonAttributes(dataArray, dom, arrayElt);
        
        // read encoding and parse values (if both present) using the appropriate parser
        Element encodingElt = dom.getElement(arrayElt, "encoding");
        Element valuesElt = dom.getElement(arrayElt, "values");
        if (encodingElt != null && valuesElt != null)
        {
            try
            {
                SweEncodingReaderV1 encodingReader = new SweEncodingReaderV1();
                DataEncoding encoding = encodingReader.readEncodingProperty(dom, encodingElt);
                DataStreamParser parser = SWEFactory.createDataParser(encoding);
                parser.setParentArray(dataArray);            
                InputStream is = new DataSourceDOM(dom, valuesElt).getDataStream();
                parser.parse(is);
            }
            catch (IOException e)
            {
                throw new XMLReaderException("Error while parsing array values", arrayElt, e);
            }
        }
        
        return dataArray;
    }


    /**
     * Reads a scalar value and atributes (Quantity, Count, Term...)
     * @param scalarElt
     * @return DataValue encapsulating the value
     * @throws SMLReaderException
     */
    private DataValue readScalar(DOMHelper dom, Element scalarElt) throws XMLReaderException
    {
        DataValue dataValue = null;              
        String eltName = scalarElt.getLocalName();
        
        // Create DataValue Object with appropriate type
    	if (eltName.equals("Quantity") || eltName.equals("Time"))
    	    dataValue = new DataValue(DataType.DOUBLE);
        else if (eltName.equals("Count"))
            dataValue = new DataValue(DataType.INT);
        else if (eltName.equals("Boolean"))
        	dataValue = new DataValue(DataType.BOOLEAN);
        else if (eltName.equals("Category") || eltName.equals("Text"))
        	dataValue = new DataValue(DataType.UTF_STRING);
        else
            throw new XMLReaderException("Invalid scalar component", scalarElt);
        
    	// read common stuffs
        dataValue.setProperty(SweConstants.COMP_QNAME, eltName);
        readGmlProperties(dataValue, dom, scalarElt);
    	readCommonAttributes(dataValue, dom, scalarElt);
        readUom(dataValue, dom, scalarElt);
        readCodeSpace(dataValue, dom, scalarElt);
        readQuality(dataValue, dom, scalarElt);
        readConstraints(dataValue, dom, scalarElt);
        
        // Parse the value
        String value = dom.getElementValue(scalarElt, "value");
        if (value != null)
        {
        	dataValue.assignNewDataBlock();
            try
            {
                asciiParser.parseToken(dataValue, value, '\0');
            }
            catch (IOException e)
            {
                throw new XMLReaderException(e.getMessage(), scalarElt);
            }
        }
        
        return dataValue;
    }
    
    
    private DataGroup readRange(DOMHelper dom, Element rangeElt) throws XMLReaderException
    {
        DataValue paramVal = null;
        DataGroup rangeValues = new DataGroup(2);
        String eltName = rangeElt.getLocalName();
        
        // Create Data component Object
        if (eltName.startsWith("Quantity"))
        {
            paramVal = new DataValue(DataType.DOUBLE);
            paramVal.setProperty(SweConstants.COMP_QNAME, "Quantity");
        }
        else if (eltName.startsWith("Count"))
        {
            paramVal = new DataValue(DataType.INT);
            paramVal.setProperty(SweConstants.COMP_QNAME, "Count");
        }
        else if (eltName.startsWith("Time"))
        {
            paramVal = new DataValue(DataType.DOUBLE);
            paramVal.setProperty(SweConstants.COMP_QNAME, "Time");
        }
        else
            throw new XMLReaderException("Only Quantity, Time and Count ranges are allowed", rangeElt);
        
        // read attributes
        readGmlProperties(rangeValues, dom, rangeElt);
        
        // assign attributes to scalar value
        readCommonAttributes(paramVal, dom, rangeElt);
        readUom(paramVal, dom, rangeElt);
        readCodeSpace(paramVal, dom, rangeElt);
        readQuality(paramVal, dom, rangeElt);
        readConstraints(paramVal, dom, rangeElt);
                
        // add params to DataGroup
        rangeValues.addComponent(SweConstants.MIN_VALUE_NAME, paramVal);
        rangeValues.addComponent(SweConstants.MAX_VALUE_NAME, paramVal.copy());
        
        // Parse the two values
        String valueText = dom.getElementValue(rangeElt, "value");
        if (valueText != null)
        {
            rangeValues.assignNewDataBlock();
            try
            {
                String[] vals = valueText.split(" ");
                asciiParser.parseToken((DataValue)rangeValues.getComponent(0), vals[0], '\0');
                asciiParser.parseToken((DataValue)rangeValues.getComponent(1), vals[1], '\0');
            }
            catch (Exception e)
            {
                throw new XMLReaderException("Error while parsing range values", rangeElt, e);
            }
        }
        
        return rangeValues;
    }
    
    
    /**
     * Reads name from element name or 'name' attribute
     * @param propertyElt
     * @return
     */
    private String readPropertyName(DOMHelper dom, Element propertyElt)
    {
        String name = dom.getAttributeValue(propertyElt, "name");
        
        if (name == null)
            name = propertyElt.getLocalName();
        
        return name;
    }
    
    
    /**
     * Reads gml properties and attributes common to all SWE components
     * @param dataComponent
     * @param dom
     * @param componentElt
     * @throws CDMException
     */
    private void readGmlProperties(DataComponent dataComponent, DOMHelper dom, Element componentElt) throws XMLReaderException
    {
        dom.addUserPrefix("gml", GML_NS);
        
        // gml metadata?
        
        // gml description
        String description = dom.getElementValue(componentElt, "gml:description");
        if (description != null)
            dataComponent.setProperty(SweConstants.DESC, description);
        
        // gml names
        String name = dom.getElementValue(componentElt, "gml:name");
        if (name != null)
            dataComponent.setProperty(SweConstants.NAME, name);
    }
    
    
    /**
     * Reads common component properties 
     * (definition uri, reference frame, axisID, unit...)
     * @param dataComponent DataContainer
     * @param dataElement Element
     */
    private void readCommonAttributes(DataComponent dataComponent, DOMHelper dom, Element componentElt) throws XMLReaderException
    {
        // definition URI
        String defUri = readComponentDefinition(dom, componentElt);
        if (defUri != null)
            dataComponent.setProperty(SweConstants.DEF_URI, defUri);
        
        // reference frame
        String refFrame = dom.getAttributeValue(componentElt, "referenceFrame");
        if (refFrame != null)
            dataComponent.setProperty(SweConstants.REF_FRAME, refFrame);
        
        // reference time
        try
        {
            String refTime = dom.getAttributeValue(componentElt, "referenceTime");
            if (refTime != null)
                dataComponent.setProperty(SweConstants.REF_TIME, DateTimeFormat.parseIso(refTime));
        }
        catch (ParseException e)
        {
            throw new XMLReaderException("Invalid reference time", componentElt, e);
        }
        
        // local frame
        String locFrame = dom.getAttributeValue(componentElt, "localFrame");
        if (locFrame != null)
            dataComponent.setProperty(SweConstants.LOCAL_FRAME, locFrame);
        
        // read axis code attribute
        String axisCode = dom.getAttributeValue(componentElt, "axisID");
        if (axisCode != null)
        	dataComponent.setProperty(SweConstants.AXIS_CODE, axisCode);
    }
    
    
    /**
     * Derives parameter definition URN from element name
     * @param componentElement
     * @throws SMLReaderException
     */
    private String readComponentDefinition(DOMHelper dom, Element componentElement) throws XMLReaderException
    {
        String defUri = dom.getAttributeValue(componentElement, "definition");
        return defUri;
    }
    
    
    /**
     * Reads the uom code, href or inline content for the given scalar component
     * @param dataComponent
     * @param dom
     * @param componentElt
     */
    private void readUom(DataComponent dataComponent, DOMHelper dom, Element scalarElt) throws XMLReaderException
    {
        if (!dom.existElement(scalarElt, "uom"))
            return;
        
        String ucumCode = dom.getAttributeValue(scalarElt, "uom/@code");
        String href = dom.getAttributeValue(scalarElt, "uom/@href");           
        Unit unit = null;
        
        // uom code        
        if (ucumCode != null)
        {
            dataComponent.setProperty(SweConstants.UOM_CODE, ucumCode);
            
            // also create unit object
            UnitParserUCUM unitParser = new UnitParserUCUM();
            unit = unitParser.getUnit(ucumCode);
        }
        
        // if no code, read href
        else if (href != null)
        {
            dataComponent.setProperty(SweConstants.UOM_URI, href);
            
            // also create unit object
            UnitParserURI unitParser = new UnitParserURI();
            unit = unitParser.getUnit(href);            
        }
        
        // inline unit
        else
        {
            Element unitElt = dom.getElement(scalarElt, "uom/*");            
            unit = unitReader.readUnit(dom, unitElt);
        }
        
        // assign unit object to component
        if (unit != null)
            dataComponent.setProperty(SweConstants.UOM_OBJ, unit);
    }
    
    
    /**
     * Reads codeSpace URI in a Category
     * @param dataComponent
     * @param dom
     * @param scalarElt
     * @throws CDMException
     */
    private void readCodeSpace(DataComponent dataComponent, DOMHelper dom, Element scalarElt) throws XMLReaderException
    {
        // codeSpace URI
        String codeSpaceUri = dom.getAttributeValue(scalarElt, "codeSpace/@href");
        if (codeSpaceUri != null)
            dataComponent.setProperty(SweConstants.DIC_URI, codeSpaceUri);
    }
    
    
    /**
     * Reads the quality component if present inline
     * @param dom
     * @param scalarElement
     * @throws CDMException
     */
    private void readQuality(DataComponent dataComponent, DOMHelper dom, Element scalarElt) throws XMLReaderException
    {
        if (!dom.existElement(scalarElt, "quality"))
            return;
        
        Element qualityElt = dom.getElement(scalarElt, "quality/*");
        DataComponent quality = readScalar(dom, qualityElt);
        quality.setName("quality");
        
        dataComponent.setProperty(SweConstants.QUALITY, quality);
    }
    
    
    /**
     * Reads the constrain list for the given scalar component
     * @param dom
     * @param parameterElement
     * @return
     * @throws CDMException
     */
    private void readConstraints(DataValue dataValue, DOMHelper dom, Element scalarElement) throws XMLReaderException
    {
    	NodeList constraintElts = dom.getElements(scalarElement, "constraint");
    	ConstraintList constraintList = new ConstraintList();
    	
    	for (int i=0; i<constraintElts.getLength(); i++)
    	{
    		DataConstraint constraint = null;
    		Element constraintElt = (Element)constraintElts.item(i);
    		
    		if (dom.existElement(constraintElt, "AllowedValues/interval"))
    			constraint = readIntervalConstraint(dom, constraintElt);
    		else if (dom.existElement(constraintElt, "AllowedValues/valueList"))
    			constraint = readNumberEnumConstraint(dom, constraintElt);
    		else if (dom.existElement(constraintElt, "AllowedValues/min"))
    			constraint = readMinMaxConstraint(dom, constraintElt);
    		else if (dom.existElement(constraintElt, "AllowedValues/max"))
    			constraint = readMinMaxConstraint(dom, constraintElt);
    		else if (dom.existElement(constraintElt, "AllowedTokens/valueList"))
    			constraint = readTokenEnumConstraint(dom, constraintElt);
    		
    		if (constraint != null)
    			constraintList.add(constraint);
    	}
    	
    	if (!constraintList.isEmpty())
    	    dataValue.setConstraints(constraintList);
    }
    
    
    /**
     * Reads a numerical interval constraint
     * @param dom
     * @param constraintElement
     * @return
     */
    private IntervalConstraint readIntervalConstraint(DOMHelper dom, Element constraintElement) throws XMLReaderException
    {
    	String rangeText = dom.getElementValue(constraintElement, "AllowedValues/interval");
    	
		try
		{
			String[] rangeValues = rangeText.split(" ");
			double min = Double.parseDouble(rangeValues[0]);
			double max = Double.parseDouble(rangeValues[1]);
			return new IntervalConstraint(min, max);
		}
		catch (Exception e)
		{
			throw new XMLReaderException("Invalid interval constraint: " + rangeText, constraintElement);
		}
    }
    
    
    private IntervalConstraint readMinMaxConstraint(DOMHelper dom, Element constraintElement) throws XMLReaderException
    {
    	String minText = dom.getElementValue(constraintElement, "AllowedValues/min");
    	String maxText = dom.getElementValue(constraintElement, "AllowedValues/max");
    	
		try
		{
			double min, max;
			
			if (minText != null)
				min = Double.parseDouble(minText);
			else
				min = Double.NEGATIVE_INFINITY;
			
			if (maxText != null)
				max = Double.parseDouble(maxText);
			else
				max = Double.POSITIVE_INFINITY;
			
			return new IntervalConstraint(min, max);
		}
		catch (Exception e)
		{
			throw new XMLReaderException("Invalid interval constraint: min=" + minText + ", max=" + maxText, constraintElement);
		}
    }
    
    
    private EnumNumberConstraint readNumberEnumConstraint(DOMHelper dom, Element constraintElement) throws XMLReaderException
    {
    	String values = dom.getElementValue(constraintElement, "AllowedValues/valueList");
    	
    	try
		{
			String[] valueList = values.split(" ");
			double[] valueArray = new double[valueList.length];
			
			for (int i=0; i<valueArray.length; i++)
				valueArray[i] = Double.parseDouble(valueList[i]);
			
			return new EnumNumberConstraint(valueArray);
		}
		catch (Exception e)
		{
			throw new XMLReaderException("Invalid number enumeration constraint: " + values, constraintElement);
		}
    }
    
    
    private EnumTokenConstraint readTokenEnumConstraint(DOMHelper dom, Element constraintElement) throws XMLReaderException
    {
    	String values = dom.getElementValue(constraintElement, "AllowedTokens/valueList");
    	String[] valueList = values.split(" ");
		return new EnumTokenConstraint(valueList);
    }    
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

