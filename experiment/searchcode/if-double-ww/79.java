/*
 * A class to serve JavaRendererServer
 */

package RenderMultipoints;
import ArmyC2.C2SD.Utilities.IPointConversion;
import ArmyC2.C2SD.Utilities.ShapeInfo;
import ArmyC2.C2SD.Utilities.MilStdSymbol;
import ArmyC2.C2SD.Utilities.ErrorLogger;
import ArmyC2.C2SD.Utilities.RendererException;
import ArmyC2.C2SD.Utilities.ModifiersTG;
import java.util.HashMap;
import java.util.Map;
import JavaLineArray.arraysupport;
import JavaLineArray.CELineArray;
import JavaLineArray.Channels;
import JavaLineArray.POINT2;
import JavaLineArray.ref;
import java.awt.Graphics2D;
import java.util.ArrayList;
import JavaLineArray.Shape2;
import JavaLineArray.DISMSupport;
import JavaLineArray.TacticalLines;
import JavaLineArray.lineutility;
import java.awt.image.BufferedImage;
import JavaTacticalRenderer.TGLight;
import JavaTacticalRenderer.Modifier2;
import JavaTacticalRenderer.clsChannelUtility;
import JavaTacticalRenderer.clsMETOC;
import JavaTacticalRenderer.P1;
import JavaTacticalRenderer.mdlGeodesic;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.Polygon;
//import javax.jws.soap.SOAPBinding.Style;
//import java.awt.Polygon;
import java.awt.geom.Line2D;





//import java.awt.font.TextLayout;
/**
 * Rendering class
 * @author Michael Deutch
 */
public final class clsRenderer
{
    private static String _className="clsRenderer";
    /**
     * Set tg geo points from the client points
     * @param milStd
     * @param tg
     */
    private static void setClientCoords(MilStdSymbol milStd, 
            TGLight tg)
    {
        try
        {
            ArrayList<POINT2>latLongs=new ArrayList();
            int j=0;
            ArrayList<Point2D.Double>coords=milStd.getCoordinates();
            Point2D.Double pt2d=null;
            POINT2 pt2=null;
            for(j=0;j<coords.size();j++)
            {
                pt2d=coords.get(j);
                pt2=clsUtility.Point2DToPOINT2(pt2d);
                latLongs.add(pt2);
            }
            tg.set_LatLongs(latLongs);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"setClientCoords",
                    new RendererException("Failed to set geo points or pixels for " + milStd.getSymbolID(), exc));
        }
    }
    
    private static ArrayList<Point2D.Double> getClientCoords(TGLight tg)
    {
        ArrayList<Point2D.Double>coords=null;
        try
        {
            int j=0;
            Point2D.Double pt2d=null;
            POINT2 pt2=null;
            coords=new ArrayList();
            for(j=0;j<tg.LatLongs.size();j++)
            {
                pt2=tg.LatLongs.get(j);
                pt2d=new Point2D.Double(pt2.x,pt2.y);
                coords.add(pt2d);
            }            
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"getClientCoords",
                    new RendererException("Failed to set geo points or pixels for " + tg.get_SymbolId(), exc));
        }
        return coords;
    }
    /**
     * Create MilStdSymbol from tactical graphic
     * @param tg tactical graphic
     * @param converter geographic to pixels to converter
     * @return MilstdSymbol object
     */
    public static MilStdSymbol createMilStdSymboFromTGLight(TGLight tg,IPointConversion converter)
    {
        MilStdSymbol milStd=null;
        try
        {
            //String symbolId=milStd.getSymbolID();
            String symbolId=tg.get_SymbolId();            
            //int std=milStd.getSymbologyStandard();
            int std=tg.getSymbologyStandard();
            //tg.setSymbologyStandard(std);
            JavaTacticalRenderer.clsUtility.initializeLinetypes(std);
            //tg.set_SymbolId(symbolId);
            int lineType=JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            //tg.set_LineType(lineType);
            String status=tg.get_Status();
            //int linestyle=-1;
            if(status != null && status.equals("A"))
            {
                if(JavaTacticalRenderer.clsUtility.isBasicShape(lineType)==false)
                {
                    //tg.set_LineStyle(1);
                    //linestyle=-1;
                }
            }
            //tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            //setClientCoords(milStd,tg);
            //build tg.Pixels
            tg.Pixels=clsUtility.LatLongToPixels(tg.LatLongs, converter);
            boolean isClosedArea=JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);
            if(isClosedArea)
            {
                JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }
            
            ArrayList<Point2D.Double>coords=getClientCoords(tg);
            tg.set_Font(new Font("Arial",Font.PLAIN,12));
            //diagnostic
            //tg.set_FillColor(milStd.getFillColor());
            //tg.set_LineColor(milStd.getLineColor());
            //tg.set_LineThickness(milStd.getLineWidth());
            //tg.set_TexturePaint(milStd.getFillStyle());
            //tg.set_FontBackColor(Color.WHITE);
            //tg.set_TextColor(tg.get_LineColor());
            Map<String,String>modifiers=new HashMap();
//            if(milStd.getModifier(ModifiersTG.W_DTG_1) != null)
//            {
//                tg.set_DTG(milStd.getModifier(ModifiersTG.W_DTG_1));
//            }
            modifiers.put(ModifiersTG.W_DTG_1, tg.get_DTG());
//            if(milStd.getModifier(ModifiersTG.W1_DTG_2) != null)
//            {
//                tg.set_DTG1(milStd.getModifier(ModifiersTG.W1_DTG_2));
//            }
            modifiers.put(ModifiersTG.W1_DTG_2, tg.get_DTG1());
//            if(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1) != null)
//                tg.set_H(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1));
            modifiers.put(ModifiersTG.H_ADDITIONAL_INFO_1, tg.get_H());
//            if(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2) != null)
//                tg.set_H1(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2));
            modifiers.put(ModifiersTG.H1_ADDITIONAL_INFO_2, tg.get_H1());            
//            if(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3) != null)
//                tg.set_H2(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3));
            modifiers.put(ModifiersTG.H2_ADDITIONAL_INFO_3, tg.get_H2());            
//            if(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1) != null)
//                tg.set_Name(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1));
            modifiers.put(ModifiersTG.T_UNIQUE_DESIGNATION_1,tg.get_Name());
//            if(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2) != null)
//                tg.set_T1(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2));
            modifiers.put(ModifiersTG.T1_UNIQUE_DESIGNATION_2,tg.get_T1());
//            if(milStd.getModifier(ModifiersTG.Y_LOCATION) != null)
//                tg.set_Location(milStd.getModifier(ModifiersTG.Y_LOCATION));
            modifiers.put(ModifiersTG.Y_LOCATION,tg.get_Location());
//            if(milStd.getModifier(ModifiersTG.N_HOSTILE) != null)
//                tg.set_N(milStd.getModifier(ModifiersTG.N_HOSTILE));
            modifiers.put(ModifiersTG.N_HOSTILE,tg.get_N());
//

            //construct the H1 and H2 modifiers for sector from the mss AM, AN, and X arraylists
//            if(lineType==TacticalLines.RANGE_FAN_SECTOR)
//            {
//                ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
//                ArrayList<Double> AN=milStd.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
//                ArrayList<Double> X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
//                if(AM != null)
//                {
//                    int numSectors=AM.size()-1;
//                    if(AN.size()/2<numSectors)
//                        numSectors=AN.size()/2;
//
//                    double left,right,min=0,max=0;
//                    //construct left,right,min,max from the arraylists
//                    String strLeftRightMinMax="";
//                    String strH1="";
//                    for(int j=0;j<numSectors;j++)
//                    {
//                        left=AN.get(2*j);
//                        right=AN.get(2*j+1);
//                        min=AM.get(j);
//                        max=AM.get(j+1);
//                        strLeftRightMinMax+=Double.toString(left)+","+Double.toString(right)+","+Double.toString(min)+","+Double.toString(max);
//                        if(j<numSectors-1)
//                            strLeftRightMinMax+=",";
//
//                        if(X!=null && j<X.size())
//                        {
//                            strH1+=Double.toString(X.get(j));
//                            if(j<numSectors-1 && j<X.size()-1)
//                                strH1+=",";
//                        }
//                    }
//                    tg.set_H2(strLeftRightMinMax);
//                    tg.set_H1(strH1);
//                }
//            }
//            int j=0;
//            switch(lineType)
//            {
//                case TacticalLines.ROZ:
//                case TacticalLines.FAADZ:
//                case TacticalLines.HIDACZ:
//                case TacticalLines.MEZ:
//                case TacticalLines.LOMEZ:
//                case TacticalLines.HIMEZ:
//                case TacticalLines.ACA:
//                case TacticalLines.ACA_RECTANGULAR:
//                case TacticalLines.ACA_CIRCULAR:
//                    ArrayList<Double> X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
//                    if(X != null && X.size()>0)
//                        tg.set_H(Double.toString(X.get(0)));
//                    if(X != null && X.size()>1)
//                        tg.set_H1(Double.toString(X.get(1)));
//                    break;
//                case TacticalLines.UAV:
//                case TacticalLines.MRR:
//                case TacticalLines.UAV_USAS:
//                case TacticalLines.MRR_USAS:
//                case TacticalLines.LLTR:
//                case TacticalLines.AC:
//                case TacticalLines.SAAFR:
//                    POINT2 pt=tg.LatLongs.get(0);
//                    Point2D pt2d0=new Point2D.Double(pt.x,pt.y);
//                    Point2D pt2d0Pixels=converter.GeoToPixels(pt2d0);                    
//                    POINT2 pt0Pixels=new POINT2(pt2d0Pixels.getX(),pt2d0Pixels.getY());
//                    
//                    //get some point 10000 meters away from pt
//                    double dist=10000;  //should work for any scale                    
//                    POINT2 pt2=mdlGeodesic.geodesic_coordinate(pt, dist, 0);
//                    Point2D pt2d1=new Point2D.Double(pt2.x,pt2.y);
//                    Point2D pt2d1Pixels=converter.GeoToPixels(pt2d1);                    
//                    POINT2 pt1Pixels=new POINT2(pt2d1Pixels.getX(),pt2d1Pixels.getY());
//                    
//                    //calculate pixels per meter
//                    double distPixels=lineutility.CalcDistanceDouble(pt0Pixels, pt1Pixels);                    
//                    double pixelsPerMeter=distPixels/dist;                        
//                    
//                    ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
//                    if(AM != null)
//                    {
//                        String H2="";
//                        for(j=0;j<AM.size();j++)
//                        {
//                            H2+=AM.get(j).toString();
//                            if(j<AM.size()-1)
//                                H2+=",";                            
//                        }
//                        tg.set_H2(H2);
//                    }
//                    String[]strRadii=null;
//                    //get the widest vaule
//                    //the current requirement is to use the greatest width as the default width
//                    double maxWidth=0,temp=0;
//                    double maxWidthMeters=0;
//                    if(tg.get_H2()!=null && tg.get_H2().isEmpty()==false)
//                    {
//                        strRadii=tg.get_H2().split(",");
//                        if(strRadii!=null && strRadii.length>0)
//                        {
//                            for(j=0;j<strRadii.length;j++)
//                            {
//                                if(!Double.isNaN(Double.parseDouble(strRadii[j])))
//                                {
//                                    temp=Double.parseDouble(strRadii[j]);
//                                    if(temp>maxWidth)
//                                        maxWidth=temp;
//                                }
//                            }
//                            maxWidthMeters=maxWidth;
//                            maxWidth *= pixelsPerMeter/2;
//                        }                        
//                    }
//                    //double defaultPixels=maxWidth;
//                    //if AM is null we default to using the value from H2 to set tg.Pixels.get(j).style                                   
//                    //hopefully H2 was set, either by the client or by stuffing it from AM
//                    if(tg.get_H2()!=null && tg.get_H2().isEmpty()==false)
//                    {
//                        if(strRadii!=null && strRadii.length>0)
//                        {
//                            //assume it's a comma delimited string
//                            double pixels=0;
//                            for(j=0;j<tg.Pixels.size();j++)
//                            {
//                                //pixels=defaultPixels;
//                                if(tg.Pixels.size()>j)
//                                {
//                                    if(strRadii.length>j)
//                                    {
//                                        if(!Double.isNaN(Double.parseDouble(strRadii[j])))                                    
//                                        {
//                                            pixels=Double.parseDouble(strRadii[j])*pixelsPerMeter/2;                                        
//                                            tg.Pixels.get(j).style=(int)pixels;
//                                        }
//                                        else
//                                            tg.Pixels.get(j).style=(int)maxWidth;
//                                    }
//                                    else
//                                        tg.Pixels.get(j).style=(int)maxWidth;
//                                }
//                            }
//                        }
//                    }
//                    //now set tg.H2 to the max value so that the H2 modifier will display as the max vaule;
//                    tg.set_H2(Double.toString(maxWidthMeters));
//                    //use X, X1 to set tg.H, tg.H1
//                    X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
//                    if(X != null && X.size()>0)
//                        tg.set_H(Double.toString(X.get(0)));
//                    if(X != null && X.size()>1)
//                        tg.set_H1(Double.toString(X.get(1)));
//                    break;
//                default:
//                    break;
//            }
//            //killl box purple evidently uses the X modifier (Rev C)
//            switch(lineType)
//            {
//                case TacticalLines.KILLBOXPURPLE:
//                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
//                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
//                    ArrayList<Double> X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
//                    String strH1="";
//                    if(X!=null)
//                    {
//                        strH1=Double.toString(X.get(0));
//                        tg.set_H1(strH1);
//                    }
//                    break;
//                default:
//                    break;
//            }
//            //circular range fans
//            if(lineType==TacticalLines.RANGE_FAN)
//            {
//                ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
//                ArrayList<Double> X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
//                String strH2="";
//                String strH1="";
//                if(AM != null)
//                {
//                    for(j=0;j<AM.size();j++)
//                    {
//                        strH2 += Double.toString(AM.get(j));
//                        if(j < AM.size()-1)
//                            strH2 += ",";
//
//                        if(X!=null && j<X.size())
//                        {
//                            strH1+=Double.toString(X.get(j));
//                            if(j<X.size()-1)
//                                strH1+=",";
//                        }
//
//
//                        //rev C has a maxiimum of 3 circles
//                        if(j==2)
//                            break;
//                    }
//                }
//                tg.set_H2(strH2);
//                tg.set_H1(strH1);
//            }
//            //Mil-Std-2525C stuff
//            switch(lineType)
//            {
//                case TacticalLines.PAA_RECTANGULAR_REVC:
//                case TacticalLines.FSA_RECTANGULAR:
//                case TacticalLines.FFA_RECTANGULAR:
//                case TacticalLines.ACA_RECTANGULAR:
//                case TacticalLines.NFA_RECTANGULAR:
//                case TacticalLines.RFA_RECTANGULAR:
//                case TacticalLines.ATI_RECTANGULAR:
//                case TacticalLines.CFFZ_RECTANGULAR:
//                case TacticalLines.SENSOR_RECTANGULAR:
//                case TacticalLines.CENSOR_RECTANGULAR:
//                case TacticalLines.DA_RECTANGULAR:
//                case TacticalLines.CFZ_RECTANGULAR:
//                case TacticalLines.ZOR_RECTANGULAR:
//                case TacticalLines.TBA_RECTANGULAR:
//                case TacticalLines.TVAR_RECTANGULAR:
//                case TacticalLines.CIRCULAR:
//                case TacticalLines.FSA_CIRCULAR:
//                case TacticalLines.ACA_CIRCULAR:
//                case TacticalLines.FFA_CIRCULAR:
//                case TacticalLines.NFA_CIRCULAR:
//                case TacticalLines.RFA_CIRCULAR:
//                case TacticalLines.PAA_CIRCULAR:
//                case TacticalLines.ATI_CIRCULAR:
//                case TacticalLines.CFFZ_CIRCULAR:
//                case TacticalLines.SENSOR_CIRCULAR:
//                case TacticalLines.CENSOR_CIRCULAR:
//                case TacticalLines.DA_CIRCULAR:
//                case TacticalLines.CFZ_CIRCULAR:
//                case TacticalLines.ZOR_CIRCULAR:
//                case TacticalLines.TBA_CIRCULAR:
//                case TacticalLines.TVAR_CIRCULAR:
//                case TacticalLines.KILLBOXBLUE_CIRCULAR:
//                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
//                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
//                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
//                    ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
//                    if(AM != null && AM.size()>0)
//                    {
//                        String strT1=Double.toString(AM.get(0));
//                        //set width for rectangles or radius for circles
//                        tg.set_T1(strT1);
//                    }
//                    break;
//                default:
//                    break;
//            }
//            //Mil-std-2525C
//            if(lineType==TacticalLines.RECTANGULAR)
//            {
//                ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
//                ArrayList<Double> AN=milStd.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
//                //if all these conditions are not met we do not want to set any tg modifiers
//                if(AM != null && AM.size()>1 && AN != null && AN.size()>0)
//                {
//                    String strT1=Double.toString(AM.get(0));    //width
//                    String strH=Double.toString(AM.get(1));     //length
//                    //set width and length in meters for rectangular target
//                    tg.set_T1(strT1);
//                    tg.set_H(strH);
//                    //set attitude in mils
//                    String strH2=Double.toString(AN.get(0));
//                    tg.set_H2(strH2);
//                }
//            }
            milStd=new MilStdSymbol(symbolId,"1",coords,modifiers);
            //diagnostic
            //tg.set_FillColor(milStd.getFillColor());
            //tg.set_LineColor(milStd.getLineColor());
            //tg.set_LineThickness(milStd.getLineWidth());
            //tg.set_TexturePaint(milStd.getFillStyle());
            //tg.set_FontBackColor(Color.WHITE);
            //tg.set_TextColor(tg.get_LineColor());
            milStd.setFillColor(tg.get_FillColor());
            milStd.setLineColor(tg.get_LineColor());
            milStd.setLineWidth(tg.get_LineThickness());
            milStd.setFillStyle(tg.get_TexturePaint());            
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"createMilStdSymboFromTGLight",
                    new RendererException("Failed to set geo points or pixels for " + tg.get_SymbolId(), exc));
        }
        return milStd;
    }
    /**
     * Build a tactical graphic object from the client MilStdSymbol
     * @param milStd MilstdSymbol object
     * @param converter geographic to pixels converter
     * @return tactical graphic
     */
    public static TGLight createTGLightFromMilStdSymbol(MilStdSymbol milStd,
            IPointConversion converter)
    {
        TGLight tg=new TGLight();
        try
        {            
            String symbolId=milStd.getSymbolID();
            int std=milStd.getSymbologyStandard();
            tg.setSymbologyStandard(std);
            JavaTacticalRenderer.clsUtility.initializeLinetypes(std);
            tg.set_SymbolId(symbolId);
            //diagnostic 1-29-13
            boolean useLineInterpolation=milStd.getUseLineInterpolation();
            tg.set_UseLineInterpolation(useLineInterpolation);
            //end section
            int lineType=JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            tg.set_LineType(lineType);
            String status=tg.get_Status();
            if(status != null && status.equals("A"))
            {
                //lineStyle=GraphicProperties.LINE_TYPE_DASHED;
                if(JavaTacticalRenderer.clsUtility.isBasicShape(lineType)==false)
                    tg.set_LineStyle(1);
            }
            tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            setClientCoords(milStd,tg);
            //build tg.Pixels
            tg.Pixels=clsUtility.LatLongToPixels(tg.LatLongs, converter);
            tg.set_Font(new Font("Arial",Font.PLAIN,12));
            //diagnostic
            tg.set_FillColor(milStd.getFillColor());
            //tg.set_FillColor(Color.CYAN);
            tg.set_LineColor(milStd.getLineColor());
            tg.set_LineThickness(milStd.getLineWidth());
            tg.set_TexturePaint(milStd.getFillStyle());
            tg.set_FontBackColor(Color.WHITE);
            tg.set_TextColor(tg.get_LineColor());
            //tg.set_LineStyle(1);
            if(milStd.getModifier(ModifiersTG.W_DTG_1) != null)
            {
                tg.set_DTG(milStd.getModifier(ModifiersTG.W_DTG_1));
                //tg.set_DTG("MUCH LONGER W");
            }
            if(milStd.getModifier(ModifiersTG.W1_DTG_2) != null)
            {
                tg.set_DTG1(milStd.getModifier(ModifiersTG.W1_DTG_2));
                //tg.set_DTG1("MUCH LONGER W2");
            }
            if(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1) != null)
                tg.set_H(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1));
            if(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2) != null)
                tg.set_H1(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2));
            if(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3) != null)
                tg.set_H2(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3));
            if(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1) != null)
                tg.set_Name(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1));
            if(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2) != null)
                tg.set_T1(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2));
            if(milStd.getModifier(ModifiersTG.Y_LOCATION) != null)
                tg.set_Location(milStd.getModifier(ModifiersTG.Y_LOCATION));
            if(milStd.getModifier(ModifiersTG.N_HOSTILE) != null)
                tg.set_N(milStd.getModifier(ModifiersTG.N_HOSTILE));

            //int lineType=CELineArray.CGetLinetypeFromString(tg.get_SymbolId());
            //int lineType=JavaTacticalRenderer.clsUtility.GetLinetypeFromString(tg,null);
            boolean isClosedArea=JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);

            if(isClosedArea)
            {
                JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }

            //construct the H1 and H2 modifiers for sector from the mss AM, AN, and X arraylists
            if(lineType==TacticalLines.RANGE_FAN_SECTOR)
            {
                ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                ArrayList<Double> AN=milStd.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
                ArrayList<Double> X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                //double test=AM.get(0);//0th element must be 0, the min range is always 0
                //if(test != 0d)
                    //AM.add(0,0d);
                if(AM != null)
                {
                    int numSectors=AM.size()-1;
                    if(AN.size()/2<numSectors)
                        numSectors=AN.size()/2;

                    double left,right,min=0,max=0;
                    //construct left,right,min,max from the arraylists
                    String strLeftRightMinMax="";
                    String strH1="";
                    for(int j=0;j<numSectors;j++)
                    {
                        left=AN.get(2*j);
                        right=AN.get(2*j+1);
                        min=AM.get(j);
                        max=AM.get(j+1);
                        strLeftRightMinMax+=Double.toString(left)+","+Double.toString(right)+","+Double.toString(min)+","+Double.toString(max);
                        if(j<numSectors-1)
                            strLeftRightMinMax+=",";

                        if(X!=null && j<X.size())
                        {
                            strH1+=Double.toString(X.get(j));
                            if(j<numSectors-1 && j<X.size()-1)
                                strH1+=",";
                        }
                    }
                    tg.set_H2(strLeftRightMinMax);
                    tg.set_H1(strH1);
                }
            }
            int j=0;
            //these have a buffer value in meters which we'll stuff tg.H2
            //and use the style member of tg.Pixels to stuff the buffer width in pixels
            switch(lineType)
            {
                case TacticalLines.BBS_AREA:
                case TacticalLines.BBS_LINE:
                case TacticalLines.BBS_RECTANGLE:
                    String H2=null;
                    double dist=0;
                    POINT2 pt0;
                    POINT2 pt1;//45 is arbitrary
                    ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                    if(AM != null && AM.size()>0)
                    {
                        H2=AM.get(0).toString();
                        tg.set_H2(H2);
                    }                    
                    for(j=0;j<tg.LatLongs.size();j++)
                    {
                        if(tg.LatLongs.size()>j)
                        {
                            if(!Double.isNaN(Double.parseDouble(H2)))                                    
                            {
                                if(j==0)
                                {
                                    dist=Double.parseDouble(H2);
                                    pt0=tg.LatLongs.get(0);
                                    pt1=mdlGeodesic.geodesic_coordinate(pt0, dist, 45);//45 is arbitrary
                                    Point2D pt02d=new Point2D.Double(pt0.x,pt0.y);
                                    Point2D pt12d=new Point2D.Double(pt1.x,pt1.y);
                                    pt02d=converter.GeoToPixels(pt02d);
                                    pt12d=converter.GeoToPixels(pt12d);
                                    pt0.x=pt02d.getX();
                                    pt0.y=pt02d.getY();
                                    pt1.x=pt12d.getX();
                                    pt1.y=pt12d.getY();
                                    dist=lineutility.CalcDistanceDouble(pt0, pt1);
                                }
                                tg.Pixels.get(j).style=Math.round((float)dist);
                            }
                            else
                                tg.Pixels.get(j).style=0;
                        }
                    }
                    break;
                default:
                    break;
            }
            switch(lineType)
            {
                case TacticalLines.ROZ:
                case TacticalLines.FAADZ:
                case TacticalLines.HIDACZ:
                case TacticalLines.MEZ:
                case TacticalLines.LOMEZ:
                case TacticalLines.HIMEZ:
                case TacticalLines.ACA:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.ACA_CIRCULAR:
                    ArrayList<Double> X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                    if(X != null && X.size()>0)
                        tg.set_H(Double.toString(X.get(0)));
                    if(X != null && X.size()>1)
                        tg.set_H1(Double.toString(X.get(1)));
                    break;
                case TacticalLines.UAV:
                case TacticalLines.MRR:
                case TacticalLines.UAV_USAS:
                case TacticalLines.MRR_USAS:
                case TacticalLines.LLTR:
                case TacticalLines.AC:
                case TacticalLines.SAAFR:
                    POINT2 pt=tg.LatLongs.get(0);
                    Point2D pt2d0=new Point2D.Double(pt.x,pt.y);
                    Point2D pt2d0Pixels=converter.GeoToPixels(pt2d0);                    
                    POINT2 pt0Pixels=new POINT2(pt2d0Pixels.getX(),pt2d0Pixels.getY());
                    
                    //get some point 10000 meters away from pt
                    //10000 should work for any scale                    
                    double dist=10000;                      
                    POINT2 pt2=mdlGeodesic.geodesic_coordinate(pt, dist, 0);
                    Point2D pt2d1=new Point2D.Double(pt2.x,pt2.y);
                    Point2D pt2d1Pixels=converter.GeoToPixels(pt2d1);                    
                    POINT2 pt1Pixels=new POINT2(pt2d1Pixels.getX(),pt2d1Pixels.getY());                    
                    //calculate pixels per meter
                    double distPixels=lineutility.CalcDistanceDouble(pt0Pixels, pt1Pixels);                                        
                    double pixelsPerMeter=distPixels/dist;                        
                    
                    ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                    if(AM != null)
                    {
                        String H2="";
                        for(j=0;j<AM.size();j++)
                        {
                            H2+=AM.get(j).toString();
                            if(j<AM.size()-1)
                                H2+=",";                            
                        }
                        tg.set_H2(H2);
                    }
                    String[]strRadii=null;
                    //get the widest vaule
                    //the current requirement is to use the greatest width as the default width
                    double maxWidth=0,temp=0;
                    double maxWidthMeters=0;
                    if(tg.get_H2()!=null && tg.get_H2().isEmpty()==false)
                    {
                        strRadii=tg.get_H2().split(",");
                        if(strRadii!=null && strRadii.length>0)
                        {
                            for(j=0;j<strRadii.length;j++)
                            {
                                if(!Double.isNaN(Double.parseDouble(strRadii[j])))
                                {
                                    temp=Double.parseDouble(strRadii[j]);
                                    if(temp>maxWidth)
                                        maxWidth=temp;
                                }
                            }
                            maxWidthMeters=maxWidth;
                            maxWidth *= pixelsPerMeter/2;
                        }                        
                    }
                    //double defaultPixels=maxWidth;
                    //if AM is null we default to using the value from H2 to set tg.Pixels.get(j).style                                   
                    //hopefully H2 was set, either by the client or by stuffing it from AM
                    if(tg.get_H2()!=null && tg.get_H2().isEmpty()==false)
                    {
                        if(strRadii!=null && strRadii.length>0)
                        {
                            //assume it's a comma delimited string
                            //double pixels=Double.valueOf(tg.get_H2())*pixelsPerMeter;
                            double pixels=0;
                            //defaultPixels=Double.valueOf(strRadii[0])*pixelsPerMeter/2;
                            //for(j=0;j<strRadii.length;j++)
                            for(j=0;j<tg.Pixels.size();j++)
                            {
                                //pixels=defaultPixels;
                                if(tg.Pixels.size()>j)
                                {
                                    if(strRadii.length>j)
                                    {
                                        if(!Double.isNaN(Double.parseDouble(strRadii[j])))                                    
                                        {
                                            pixels=Double.parseDouble(strRadii[j])*pixelsPerMeter/2;                                        
                                            tg.Pixels.get(j).style=(int)pixels;
                                        }
                                        else
                                            tg.Pixels.get(j).style=(int)maxWidth;
                                    }
                                    else
                                        tg.Pixels.get(j).style=(int)maxWidth;
                                }
                            }
                        }
                    }
                    //now set tg.H2 to the max value so that the H2 modifier will display as the max vaule;
                    tg.set_H2(Double.toString(maxWidthMeters));
                    //use X, X1 to set tg.H, tg.H1
                    X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                    if(X != null && X.size()>0)
                        tg.set_H(Double.toString(X.get(0)));
                    if(X != null && X.size()>1)
                        tg.set_H1(Double.toString(X.get(1)));
                    break;
                default:
                    break;
            }
            //killl box purple evidently uses the X modifier (Rev C)
            switch(lineType)
            {
                case TacticalLines.KILLBOXPURPLE:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                    ArrayList<Double> X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                    String strH1="";
                    if(X!=null)
                    {
                        strH1=Double.toString(X.get(0));
                        tg.set_H1(strH1);
                    }
                    break;
                default:
                    break;
            }
            //circular range fans
            if(lineType==TacticalLines.RANGE_FAN)
            {
                ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                ArrayList<Double> X=milStd.getModifiers_AM_AN_X(ModifiersTG.X_ALTITUDE_DEPTH);
                String strH2="";
                String strH1="";
                if(AM != null)
                {
                    for(j=0;j<AM.size();j++)
                    {
                        strH2 += Double.toString(AM.get(j));
                        if(j < AM.size()-1)
                            strH2 += ",";

                        if(X!=null && j<X.size())
                        {
                            strH1+=Double.toString(X.get(j));
                            if(j<X.size()-1)
                                strH1+=",";
                        }


                        //rev C has a maxiimum of 3 circles
                        if(j==2)
                            break;
                    }
                }
                tg.set_H2(strH2);
                tg.set_H1(strH1);
            }
            //Mil-Std-2525C stuff
            switch(lineType)
            {
                case TacticalLines.PAA_RECTANGULAR_REVC:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                case TacticalLines.ATI_RECTANGULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                case TacticalLines.DA_RECTANGULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                case TacticalLines.TBA_RECTANGULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                case TacticalLines.CIRCULAR:
                case TacticalLines.FSA_CIRCULAR:
                case TacticalLines.ACA_CIRCULAR:
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.RFA_CIRCULAR:
                case TacticalLines.PAA_CIRCULAR:
                case TacticalLines.ATI_CIRCULAR:
                case TacticalLines.CFFZ_CIRCULAR:
                case TacticalLines.SENSOR_CIRCULAR:
                case TacticalLines.CENSOR_CIRCULAR:
                case TacticalLines.DA_CIRCULAR:
                case TacticalLines.CFZ_CIRCULAR:
                case TacticalLines.ZOR_CIRCULAR:
                case TacticalLines.TBA_CIRCULAR:
                case TacticalLines.TVAR_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                case TacticalLines.BBS_POINT:
                    ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                    if(AM != null && AM.size()>0)
                    {
                        String strT1=Double.toString(AM.get(0));
                        //set width for rectangles or radius for circles
                        tg.set_T1(strT1);
                    }
                    else if(lineType==TacticalLines.BBS_POINT && tg.LatLongs.size()>1)
                    {
                        double dist=mdlGeodesic.geodesic_distance(tg.LatLongs.get(0), tg.LatLongs.get(1), null, null);
                        String strT1=Double.toString(dist);
                        tg.set_T1(strT1);
                    }
                    break;
                default:
                    break;
            }
            //Mil-std-2525C
            if(lineType==TacticalLines.RECTANGULAR)
            {
                ArrayList<Double> AM=milStd.getModifiers_AM_AN_X(ModifiersTG.AM_DISTANCE);
                ArrayList<Double> AN=milStd.getModifiers_AM_AN_X(ModifiersTG.AN_AZIMUTH);
                //if all these conditions are not met we do not want to set any tg modifiers
                if(AM != null && AM.size()>1 && AN != null && AN.size()>0)
                {
                    String strT1=Double.toString(AM.get(0));    //width
                    String strH=Double.toString(AM.get(1));     //length
                    //set width and length in meters for rectangular target
                    tg.set_T1(strT1);
                    tg.set_H(strH);
                    //set attitude in mils
                    String strH2=Double.toString(AN.get(0));
                    tg.set_H2(strH2);
                }
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"createTGLightfromMilStdSymbol",
                    new RendererException("Failed to build multipoint TG for " + milStd.getSymbolID(), exc));
        }
        return tg;
    }
    /**
     * @deprecated 
     * @param milStd
     * @param converter
     * @param computeChannelPt
     * @return
     */
    public static TGLight createTGLightFromMilStdSymbol(MilStdSymbol milStd,
            IPointConversion converter,Boolean computeChannelPt)
    {
        TGLight tg=new TGLight();
        try
        {
            String symbolId=milStd.getSymbolID();
            tg.set_SymbolId(symbolId);
            String status=tg.get_Status();
            if(status != null && status.equals("A"))
            {
                //lineStyle=GraphicProperties.LINE_TYPE_DASHED;
                tg.set_LineStyle(1);
            }
            tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            setClientCoords(milStd,tg);
            //build tg.Pixels
            tg.Pixels=clsUtility.LatLongToPixels(tg.LatLongs, converter);
            tg.set_Font(new Font("Arial",Font.PLAIN,12));
            tg.set_FillColor(milStd.getFillColor());
            tg.set_LineColor(milStd.getLineColor());
            tg.set_LineThickness(milStd.getLineWidth());
            tg.set_TexturePaint(milStd.getFillStyle());
            tg.set_FontBackColor(Color.WHITE);
            tg.set_TextColor(tg.get_LineColor());

//            tg.set_DTG(milStd.getModifier(ModifiersTG.W_DTG_1));
//            tg.set_DTG1(milStd.getModifier(ModifiersTG.W1_DTG_2));
//            tg.set_H(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1));
//            tg.set_H1(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2));
//            tg.set_H2(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3));
//            tg.set_Name(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1));
//            tg.set_T1(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2));
//            tg.set_Location(milStd.getModifier(ModifiersTG.Y_LOCATION));
//            tg.set_N(ModifiersTG.N_HOSTILE);

            if(milStd.getModifier(ModifiersTG.W_DTG_1) != null)
                tg.set_DTG(milStd.getModifier(ModifiersTG.W_DTG_1));
            if(milStd.getModifier(ModifiersTG.W1_DTG_2) != null)
                tg.set_DTG1(milStd.getModifier(ModifiersTG.W1_DTG_2));
            if(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1) != null)
                tg.set_H(milStd.getModifier(ModifiersTG.H_ADDITIONAL_INFO_1));
            if(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2) != null)
                tg.set_H1(milStd.getModifier(ModifiersTG.H1_ADDITIONAL_INFO_2));
            if(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3) != null)
                tg.set_H2(milStd.getModifier(ModifiersTG.H2_ADDITIONAL_INFO_3));
            if(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1) != null)
                tg.set_Name(milStd.getModifier(ModifiersTG.T_UNIQUE_DESIGNATION_1));
            if(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2) != null)
                tg.set_T1(milStd.getModifier(ModifiersTG.T1_UNIQUE_DESIGNATION_2));
            if(milStd.getModifier(ModifiersTG.Y_LOCATION) != null)
                tg.set_Location(milStd.getModifier(ModifiersTG.Y_LOCATION));
            if(milStd.getModifier(ModifiersTG.N_HOSTILE) != null)
                tg.set_N(milStd.getModifier(ModifiersTG.N_HOSTILE));

            //int lineType=CELineArray.CGetLinetypeFromString(tg.get_SymbolId());
            //int rev=tg.getSymbologyStandard();
            int lineType=JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            boolean isClosedArea=JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);

            if(isClosedArea)
            {
                JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }

            //these channels need a channel point added
            if(computeChannelPt)
            switch (lineType)
            {
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.AAFNT:
                case TacticalLines.AAAAA:
                case TacticalLines.AIRAOA:
                case TacticalLines.MAIN:
                case TacticalLines.SPT:
                case TacticalLines.AXAD:
                        POINT2 ptPixels=JavaTacticalRenderer.clsUtility.ComputeLastPoint(tg.Pixels);
                        tg.Pixels.add(ptPixels);
                        Point pt=clsUtility.POINT2ToPoint(ptPixels);
                        //in case it needs the corresponding geo point
                        Point2D.Double ptGeo2d=converter.PixelsToGeo(pt);
                        POINT2 ptGeo=clsUtility.Point2DToPOINT2(ptGeo2d);
                        tg.LatLongs.add(ptGeo);
                    //}
                    break;
                default:
                    break;
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"createTGLightfromMilStdSymbol",
                    new RendererException("Failed to build multipoint TG for " + milStd.getSymbolID(), exc));
        }
        return tg;
    }
    /**
     * @deprecated
     * Render the symbol and set ShapeInfo data in the client symol
     * @param symbol
     * @param converter
     */
    public static void render(MilStdSymbol symbol, 
            IPointConversion converter)
    {
        try
        {
            TGLight tg=createTGLightFromMilStdSymbol(symbol,converter);
            ArrayList<ShapeInfo>shapeInfos=new ArrayList();
            ArrayList<ShapeInfo>modifierShapeInfos=new ArrayList();
            clsUtility.FilterAXADPoints(tg, converter);
            clsUtility.FilterPoints(tg);
            GetLineArray(tg,converter,shapeInfos,modifierShapeInfos);
            symbol.setModifierShapes(modifierShapeInfos);
            symbol.setSymbolShapes(shapeInfos);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"render",
                    new RendererException("Failed to render " + symbol.getSymbolID(), exc));
        }
        return;
    }
    /**
     * @deprecated 
     * Added this public interface so that TestJavaLineArray can test the general client
     * @param tg tactical graphic
     * @param converter geographic to pixels converter
     */
    public static void render_TG(TGLight tg, 
            IPointConversion converter,
            ArrayList<ShapeInfo>shapeInfos,
            ArrayList<ShapeInfo>modifierShapeInfos)
    {
        try
        {
            int lineType=-1;
            boolean isClosedArea=false;
            setHostileLC(tg);
            String symbolId=tg.get_SymbolId();
            lineType=clsMETOC.IsWeather(symbolId);
            //int rev=tg.getSymbologyStandard();
            if(lineType<0)
            {
                //lineType=CELineArray.CGetLinetypeFromString(tg.get_SymbolId());
                lineType=JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            }
            
            isClosedArea=JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);

            if(isClosedArea)
            {
                JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }
            clsUtility.FilterAXADPoints(tg, converter);
            clsUtility.FilterPoints(tg);
            GetLineArray(tg,converter,shapeInfos,modifierShapeInfos);
            //tg.setModifierShapes(modifierShapeInfos);
            //tg.setSymbolShapes(shapeInfos);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"render",
                    new RendererException("Failed to render " + tg.get_SymbolId(), exc));
        }
        return;
    }

    /**
     * @desc Render the symbol to return the ShapeInfo data using JavaLineArray. This
     * is for the generic client.
     * 
     * @param tg
     * @param converter
     * @param shapeInfos
     * @param modifierShapeInfos
     * @deprecated
     */
    private static void GetLineArray(TGLight tg,
            IPointConversion converter,
            ArrayList<ShapeInfo>shapeInfos,
            ArrayList<ShapeInfo>modifierShapeInfos)
    {
        try
        {
            ArrayList<Shape2> shapes= new ArrayList();//ShapeInfoToShape2(shapeInfos);
            ArrayList<Shape2> modifierShapes= new ArrayList();//ShapeInfoToShape2(shapeInfos);
            int lineType=tg.get_LineType();
            int minPoints2=JavaTacticalRenderer.clsUtility.GetMinPoints(lineType);
            ref<int[]> minPoints = new ref();
            ArrayList<POINT2> channelPoints=new ArrayList();
            boolean bolChange1 = JavaTacticalRenderer.clsUtility.IsChange1Area(lineType, minPoints);
            int bolMeTOC=JavaTacticalRenderer.clsMETOC.IsWeather(tg.get_SymbolId());


            tg.modifiers=new ArrayList();
            BufferedImage bi=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d=bi.createGraphics();
            Modifier2.AddModifiers(tg,g2d,null);
            int rev=tg.getSymbologyStandard();
            //Modifier2.AddModifiers(tg,g2d);//flipped only for 3d for change 1 symbols
            Shape2 hatchShape=null;
            if(converter==null)
                JavaTacticalRenderer.clsUtility.getHatchShape(tg,bi);
            
            if(tg.Pixels.size()<minPoints2)
            {
                lineType=TacticalLines.MIN_POINTS;
                bolChange1=false;
            }

            if (bolChange1)
            {
                tg.Pixels.clear();
                //fills tg.Pixels
                bolChange1 = RenderMultipoints.clsUtilityCPOF.Change1TacticalAreas(tg, lineType, converter, shapes);
                //points = tg.Pixels;
            }
            else if(bolMeTOC>0)
            {
                try
                {
                    clsMETOC.GetMeTOCShape(tg, shapes,rev);
                }
                catch(Exception ex)
                {
                    JavaTacticalRenderer.clsUtility.WriteFile("Error in ClsMETOC.GetMeTOCShape");
                }
            }
            else
            {
                if (CELineArray.CIsChannel(lineType) == 0)
                {
                    if(lineType != TacticalLines.BELT1)
                    {
                        tg.Pixels=arraysupport.GetLineArray2(lineType, tg.Pixels, shapes,null,rev);
                        Modifier2.GetIntegralTextShapes(tg, g2d, shapes);
                    }
                    //points = arraysupport.points;
                    //tg.Pixels=points;
                    if(lineType==TacticalLines.BELT1)
                    {
                        //get the partitions
                        ArrayList<Shape2> tempShapes= null;
                        ArrayList<P1>partitions=clsChannelUtility.GetPartitions2(tg);
                        ArrayList<POINT2>pixels=null;
                        int l=0,k=0;
                        for(l=0;l<partitions.size();l++)
                        {
                            tempShapes= new ArrayList();
                            pixels=new ArrayList();
                            for(k=partitions.get(l).start;k<=partitions.get(l).end_Renamed+1;k++)
                            {
                                pixels.add(tg.Pixels.get(k));
                            }
                            pixels=arraysupport.GetLineArray2(lineType, pixels, tempShapes,null,rev);
                            shapes.addAll(tempShapes);
                        }
                    }
                }
                else //channel type
                {
                    clsChannelUtility.DrawChannel(tg.Pixels, lineType, tg, shapes, channelPoints,rev);
                    tg.Pixels=channelPoints;
                }
            }
            //if(converter.get_BestFit()==true)
            //{
                //assumes tg.LatLongs is filled
            //    tg.LatLongs=clsUtility.PixelsToLatLong(tg.Pixels, converter);
            //}
            //BufferedImage bi=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
            JavaTacticalRenderer.clsUtility.SetShapeProperties(tg,shapes,bi);

            //at this point tg.Pixels has the points from CELineArray
            //the following line adds modifiers for those sybmols which require
            //the calculated points to use for the modifiers.
            //currentlly only BLOCK and CONTAIN use tg.Pixels for computing
            //the modifiers after the call to GetLineArray
            //so points will usually have nothing in it
            Modifier2.AddModifiers2(tg);
            //BestFitModifiers(tg,converter);
            //build the modifier shapes
            if(hatchShape != null)
                shapes.add(hatchShape);

            Shape2ToShapeInfo(shapeInfos,shapes);

            if(modifierShapeInfos != null)//else client is not using shapes to display modifiers
            {
                //bi=new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);
                //Graphics2D g2d=bi.createGraphics();
                Modifier2.DisplayModifiers2(tg, g2d, modifierShapes, false, converter);

                //convert to ShapeInfo ArrayLists
                Shape2ToShapeInfo(modifierShapeInfos, modifierShapes);
                bi.flush();
                g2d.dispose();
                bi=null;
                g2d=null;
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"GetLineArray",
                    new RendererException("Points calculator failed for " + tg.get_SymbolId(), exc));
        }
        //return tg;
        //return CELineArray.get_Shapes();
        return;
    }
    /**
     * To maintain independence from the ShapeInfo type assignments
     * @param shapeInfo
     * @param shape
     */
//    private static void setShape2Type(ShapeInfo shapeInfo, Shape2 shape)
//    {
//        if(shapeInfo.getShapeType()==ShapeInfo.SHAPE_TYPE_FILL)
//            shape.set_ShapeType(Shape2.SHAPE_TYPE_FILL);
//        else if(shapeInfo.getShapeType()==ShapeInfo.SHAPE_TYPE_MODIFIER)
//            shape.set_ShapeType(Shape2.SHAPE_TYPE_MODIFIER);
//        else if(shapeInfo.getShapeType()==ShapeInfo.SHAPE_TYPE_MODIFIER_FILL)
//            shape.set_ShapeType(Shape2.SHAPE_TYPE_MODIFIER_FILL);
//        else
//            shape.set_ShapeType(Shape2.SHAPE_TYPE_POLYLINE);
//    }
    private static void setShapeInfoType(ShapeInfo shapeInfo, Shape2 shape)
    {
        if(shape.getShapeType()==Shape2.SHAPE_TYPE_FILL)
            shapeInfo.setShapeType(ShapeInfo.SHAPE_TYPE_FILL);
        else if(shape.getShapeType()==Shape2.SHAPE_TYPE_MODIFIER)
            shapeInfo.setShapeType(ShapeInfo.SHAPE_TYPE_MODIFIER);
        else if(shape.getShapeType()==Shape2.SHAPE_TYPE_MODIFIER_FILL)
            shapeInfo.setShapeType(ShapeInfo.SHAPE_TYPE_MODIFIER_FILL);
        else
            shapeInfo.setShapeType(ShapeInfo.SHAPE_TYPE_POLYLINE);
    }
//    private static ArrayList<Shape2>ShapeInfoToShape2(ArrayList<ShapeInfo>shapeInfos)
//    {
//        if(shapeInfos==null)
//            return null;
//        if(shapeInfos.size()==0)
//            return new ArrayList();
//
//        int j=0;
//        ShapeInfo shapeInfo=null;
//        Shape2 shape=null;
//        ArrayList shapes=new ArrayList();
//        for(j=0;j<shapeInfos.size();j++)
//        {
//            shapeInfo=shapeInfos.get(j);
//            shape=new Shape2(shapeInfo.getShapeType());
//            shape.set_AffineTransform(shapeInfo.getAffineTransform());
//            setShape2Type(shapeInfo,shape);
//            shape.set_FillColor(shapeInfo.getFillColor());
//            shape.set_LineColor(shapeInfo.getLineColor());
//            shape.set_Stroke(shapeInfo.getStroke());
//            shape.set_Shape(shapeInfo.getShape());
//            shape.set_TexturePaint(shapeInfo.getTexturePaint());
//            shapes.add(shape);
//        }
//        return shapes;
//    }
    private static void Shape2ToShapeInfo(ArrayList<ShapeInfo> shapeInfos,ArrayList<Shape2>shapes)
    {
        //ShapeInfo shapeInfo=null;
        try
        {
            int j=0;
            Shape2 shape=null;
            if(shapes==null || shapeInfos==null || shapes.size()==0)
                return;
            
            for(j=0;j<shapes.size();j++)
            {
                shape=shapes.get(j);
                shapeInfos.add((ShapeInfo)shape);
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"Shape2ToShapeInfo",
                    new RendererException("Failed to build ShapeInfo ArrayList", exc));
        }
        //return shapeInfos;
    }
    
    private static boolean setGenericLine(TGLight tg,
            IPointConversion converter,
            Rectangle2D clipBounds)
    {
        boolean result=false;
        try
        {
            //if clipbounds is null the line does not get clipped anyway, do nothing
            if(clipBounds==null)
                return false;
            
            double scale=getScale(tg,converter,clipBounds);
            double mbrDist=0;
            POINT2 ptUl=new POINT2(),ptUr=new POINT2(),ptLr=new POINT2(),ptLl=new POINT2();
            if(scale<500)
            {                                
                //if the tg.Pixels mbr is large then the tg was zoomed in from the
                //original render, else it may be an original render at the current zoom level,
                //in which case we do not want to set the line to generic
                Modifier2.GetMBR(tg, ptUl, ptUr, ptLr, ptLl);
                mbrDist=lineutility.CalcDistanceDouble(ptUl, ptLr);
                if(mbrDist>1000)
                {
                    tg.set_SymbolId("GENERIC---****X");
                    tg.set_LineType(TacticalLines.GENERIC);
                    result=true;
                }
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"setGenericLine",
                    new RendererException("Failed inside setGenericLine", exc));
        }
        return result;        
    }
    /**
     * @deprecated
     * @param clipPoints 
     */
    private static void shiftClipPoints(ArrayList<Point2D>clipPoints)
    {
        int j=0;
        Point2D pt0=null,pt1=null;
        for(j=0;j<clipPoints.size()-1;j++)
        {
            pt0=clipPoints.get(j);
            pt1=clipPoints.get(j+1);
            if(pt0.getX()==pt1.getX())
                pt1.setLocation(pt1.getX()+1, pt1.getY());
            if(pt0.getY()==pt1.getY())
                pt1.setLocation(pt1.getX(), pt1.getY()+1);            
        }
        pt0=clipPoints.get(0);
        Point2D ptLast=clipPoints.get(clipPoints.size()-1);        
    }
    /**
     * GoogleEarth renderer uses polylines for rendering
     * @param mss MilStdSymbol object
     * @param converter the geographic to pixels coordinate converter
     * @param clipArea the clip bounds
     */
    public static void renderWithPolylines(MilStdSymbol mss,
            IPointConversion converter,
            Object clipArea)
    {
        try
        {
            TGLight tg=clsRenderer.createTGLightFromMilStdSymbol(mss, converter);
            ArrayList<ShapeInfo>shapeInfos=new ArrayList();
            ArrayList<ShapeInfo>modifierShapeInfos=new ArrayList();
            render_GE(tg,shapeInfos,modifierShapeInfos,converter,clipArea);
            mss.setSymbolShapes(shapeInfos);
            mss.setModifierShapes(modifierShapeInfos);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"renderWithPolylines",
                    new RendererException("Failed inside renderWithPolylines", exc));
        }
    }
    /**
     * @deprecated 
     * @param tg 
     */
    private static void reversePoints(TGLight tg)
    {
        int j=0;
        ArrayList pts=null;
        switch(tg.get_LineType())
        {
            case TacticalLines.SINGLEC://reverse single concertina
            case TacticalLines.SINGLEC2:
                if(tg.Pixels != null)
                {
                    pts=(ArrayList<POINT2>)tg.Pixels.clone();
                    for(j=0;j<tg.Pixels.size();j++)
                        tg.Pixels.set(j, (POINT2)pts.get(pts.size()-j-1));
                }
                if(tg.LatLongs != null)
                {
                    pts=(ArrayList<POINT2>)tg.LatLongs.clone();
                    for(j=0;j<tg.LatLongs.size();j++)
                        tg.LatLongs.set(j, (POINT2)pts.get(pts.size()-j-1));
                }
                break;
            default:
                break;
        }
    }
    /**
     * Generic Tester says Tiger but use right mouse down. Refresh button says SECRenderer or GE.
     * Google Earth renderer tester: Called by TestJavaLineArray thru SECRenderer
     * This is the public interface for Google Earth renderer
     * assumes tg.Pixels is filled
     * assumes the caller instantiated the ShapeInfo arrays
     *
     * @param tg tactical graphic
     * @param shapeInfos symbol ShapeInfo array
     * @param modifierShapeInfos modifier ShapeInfo array
     * @param converter geographic to pixels coordinate converter
     * @param clipBounds clipping bounds
     */
    public static void render_GE(TGLight tg,
            ArrayList<ShapeInfo>shapeInfos,
            ArrayList<ShapeInfo>modifierShapeInfos,
            IPointConversion converter,
            Object clipArea) //was Rectangle2D
    {
        try
        {
            Rectangle2D clipBounds=null;            
            //diagnostic 1-9-13
            //uncomment line to trun off shift lines
            //Channels.setShiftLines(false);
            CELineArray.setClient("ge");
            //reversePoints(tg);
            //save original points if segment data is set
            //we don't want to clip if segment data is set            
            ArrayList<POINT2>origPixels=null;
            ArrayList<POINT2>origLatLongs=null;
            if(clsUtilityGE.segmentColorsSet(tg))
            {
                origPixels=(ArrayList<POINT2>)tg.Pixels.clone();
                origLatLongs=(ArrayList<POINT2>)tg.LatLongs.clone();                
            }
            
            boolean shiftLines=Channels.getShiftLines();
            if(shiftLines)
            {
                //Channels.setClient("ge");
                //tg.set_Affiliation("H");
                //tg.set_Affiliation("F");
                String affiliation=tg.get_Affiliation();
                Channels.setAffiliation(affiliation);
            }
            //end section
            CELineArray.setMinLength(2.5);    //2-27-2013
            
            ArrayList<Point2D> clipPoints=null; 
            
            //diagnostic
            //tg.set_Affiliation("H");
            //end section
            
            if(clipArea != null)
            {
                if(clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class))
                    clipBounds=(Rectangle2D.Double)clipArea;
                else if(clipArea.getClass().isAssignableFrom(Rectangle.class))
                    clipBounds=(Rectangle2D)clipArea;
                else if(clipArea.getClass().isAssignableFrom(ArrayList.class))
                {
                    clipPoints=(ArrayList<Point2D>)clipArea;            
                }
            }   
            //add sub-section to test clipArea if client passes the rectangle
            boolean useClipPoints=false;
            if(useClipPoints==true && clipBounds != null)
            {
                double x=clipBounds.getMinX();
                double y=clipBounds.getMinY();
                double width=clipBounds.getWidth();
                double height=clipBounds.getHeight();
                clipPoints=new ArrayList();
                clipPoints.add(new Point2D.Double(x,y));
                clipPoints.add(new Point2D.Double(x+width,y));
                clipPoints.add(new Point2D.Double(x+width,y+height));
                clipPoints.add(new Point2D.Double(x,y+height));
                clipPoints.add(new Point2D.Double(x,y));
                clipBounds=null;
            }            
            //end section
                                    
            if(tg.get_Client()==null || tg.get_Client().isEmpty())
                tg.set_client("ge");
            
            RenderMultipoints.clsUtility.RemoveDuplicatePoints(tg);
            
            //diagnostic until they add Revision to the Mil-Std-2525 class
            int rev=tg.getSymbologyStandard();
            JavaTacticalRenderer.clsUtility.initializeLinetypes(rev);
            JavaTacticalRenderer.clsUtility.setRevC(tg);
                        
            //diagnostic
            int linetype=tg.get_LineType();
            if(linetype<0)
            {
                linetype=JavaTacticalRenderer.clsUtility.GetLinetypeFromString(tg.get_SymbolId());            //clsUtilityCPOF.SegmentGeoPoints(tg, converter);
                tg.set_LineType(linetype);
            }
            //end section
            
            Boolean isTextFlipped=false;
            ArrayList<Shape2> shapes=null;   //use this to collect all the shapes
            clsUtilityGE.setSplineLinetype(tg);
            setHostileLC(tg);
                                    
            clsUtilityCPOF.SegmentGeoPoints(tg, converter);
            if(clipBounds != null || clipPoints !=null)
            {
                //ArrayList<POINT2>originalPixels=(ArrayList<POINT2>)tg.Pixels.clone();
                if (clsUtilityCPOF.canClipPoints(tg))
                {
                   //check assignment
                   if(clipBounds != null)
                       clsClipPolygon2.ClipPolygon(tg, clipBounds);
                   else if(clipPoints!=null)
                       clsClipQuad.ClipPolygon(tg, clipPoints);

                   clsUtilityGE.removeTrailingPoints(tg,clipArea);
                   tg.LatLongs=clsUtility.PixelsToLatLong(tg.Pixels, converter);
                }
            }           
            
            //diagnostic 4-26-13
            //if MSR segment data set use original pixels unless tg.Pixels is empty from clipping
            if(origPixels != null)
            {
                if(tg.Pixels.isEmpty())
                    return;
                else
                {
                    tg.Pixels=origPixels;
                    tg.LatLongs=origLatLongs;
                    clipArea=null;
                }
            }
            
            //diagnostic 1-28-13
            JavaTacticalRenderer.clsUtility.InterpolatePixels(tg);
            
            tg.modifiers=new ArrayList();
            BufferedImage bi=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d=bi.createGraphics();
            Modifier2.AddModifiers(tg,g2d,clipArea);
            
            clsUtilityCPOF.FilterPoints2(tg,converter);
            JavaTacticalRenderer.clsUtility.FilterVerticalSegments(tg);
            clsUtility.FilterAXADPoints(tg, converter);
            clsUtilityCPOF.ClearPixelsStyle(tg);                        
            
            ArrayList<Shape2> linesWithFillShapes=null;
            
            //check assignment
            if(clipBounds != null)
                linesWithFillShapes=clsClipPolygon2.LinesWithFill(tg, clipBounds);
            else if(clipPoints != null)
                linesWithFillShapes=clsClipQuad.LinesWithFill(tg, clipPoints);
            else if(clipArea==null)
                linesWithFillShapes=clsClipPolygon2.LinesWithFill(tg, clipBounds);
            
            //add section M. Deutch 11-4-2011
            ArrayList rangeFanFillShapes=null;
            //do not fill the original shapes for circular range fans
            int savefillStyle=tg.get_FillStyle();
            if(linetype==TacticalLines.RANGE_FAN)
                tg.set_Fillstyle(0);
            
            //check assignment (pass which clip object is not null)
            if(clipBounds != null)
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds); //takes clip object           
            else if(clipPoints != null)
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipPoints);
            else if(clipArea==null)
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds);        
                        
            switch(linetype)
            {
                case TacticalLines.RANGE_FAN:
                case TacticalLines.RANGE_FAN_SECTOR:
                    TGLight tg1=clsUtilityCPOF.GetCircularRangeFanFillTG(tg);
                    tg1.set_Fillstyle(savefillStyle);
                    //check assignment (pass which clip object is not null)
                    if(clipBounds != null)
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped,clipBounds);
                    else if(clipPoints != null)
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped,clipPoints);
                    else if (clipArea==null)
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped,clipBounds);
                    
                    if(rangeFanFillShapes != null)   
                    {
                        shapes.addAll(0, rangeFanFillShapes);                
                    }
                    break;
                default:
                    clsRenderer2.getAutoshapeFillShape(tg, shapes);
                    break;
            }
            //end section
            
            //System.out.println("after GetLineArray");
            //undo any fillcolor for lines with fill
            clsUtilityCPOF.LinesWithSeparateFill(tg.get_LineType(), shapes);
            clsClipPolygon2.addAbatisFill(tg, shapes);

            //if this line is commented then the extra line in testbed goes away
            if(shapes != null && linesWithFillShapes != null && linesWithFillShapes.size()>0)
                    shapes.addAll(0,linesWithFillShapes);

            if(clsUtilityCPOF.canClipPoints(tg)==false && clipBounds != null)
                shapes=clsUtilityCPOF.postClipShapes(tg,shapes, clipBounds);
            else if(clsUtilityCPOF.canClipPoints(tg)==false && clipPoints != null)
                shapes=clsUtilityCPOF.postClipShapes(tg,shapes, clipPoints);            

            //CPOF 6.0 diagnostic
            //returns early if textSpecs are null
            //currently the client is ignoring these
            if(modifierShapeInfos != null)
            {
                //BufferedImage bi=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
                //Graphics2D g2d=bi.createGraphics();
                ArrayList<Shape2>textSpecs=new ArrayList();
                JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecs, isTextFlipped,converter);
                Shape2ToShapeInfo(modifierShapeInfos,textSpecs);
            }
            Shape2ToShapeInfo(shapeInfos,shapes);
            //GE has no utility for building hatch fills from the texturepaint
            clsUtilityGE.buildHatchFills(tg,shapeInfos);
            
            //check assignment (pass which clip object is not null)
            if(clipBounds != null)
                clsUtilityGE.SetShapeInfosPolylines(tg,shapeInfos, clipBounds);//takes a clip object            
            else if(clipPoints != null)
                clsUtilityGE.SetShapeInfosPolylines(tg,shapeInfos, clipPoints);            
            else if(clipArea==null)
                clsUtilityGE.SetShapeInfosPolylines(tg,shapeInfos, clipBounds);       
        }
        catch (Exception exc)
        {
                ErrorLogger.LogException(_className, "render_GE",
                        new RendererException("Failed inside render_GE", exc));

        }
        return;
    }
    /**
     * to follow right hand rule for LC when affiliation is hostile.
     * also fixes MSDZ point order and maybe various other wayward symbols
     * @param tg
     */
    private static void setHostileLC(TGLight tg)
    {
        try
        {
            Boolean usas1314=true;
            ArrayList<POINT2>pts=new ArrayList();
            int j=0;
            switch(tg.get_LineType())
            {
                case TacticalLines.LC:
                    if(usas1314 == false)
                        break;
                    if(tg.get_Affiliation() != null && !tg.get_Affiliation().equals("H"))
                        break;                    
                    pts=(ArrayList<POINT2>)tg.Pixels.clone();
                    for(j=0;j<tg.Pixels.size();j++)
                        tg.Pixels.set(j, pts.get(pts.size()-j-1));
                    //reverse the latlongs also
                    pts=(ArrayList<POINT2>)tg.LatLongs.clone();
                    for(j=0;j<tg.LatLongs.size();j++)
                        tg.LatLongs.set(j, pts.get(pts.size()-j-1));
                    break;
                case TacticalLines.LINE:    //CPOF client requests reverse orientation
                    pts=(ArrayList<POINT2>)tg.Pixels.clone();
                    for(j=0;j<tg.Pixels.size();j++)
                        tg.Pixels.set(j, pts.get(pts.size()-j-1));
                    //reverse the latlongs also
                    pts=(ArrayList<POINT2>)tg.LatLongs.clone();
                    for(j=0;j<tg.LatLongs.size();j++)
                        tg.LatLongs.set(j, pts.get(pts.size()-j-1));
                    break;
//                case TacticalLines.DOUBLEC:    //CPOF client requests reverse orientation
//                    if(Channels.getShiftLines()==false)
//                        break;
//                    pts=(ArrayList<POINT2>)tg.Pixels.clone();
//                    for(j=0;j<tg.Pixels.size();j++)
//                        tg.Pixels.set(j, pts.get(pts.size()-j-1));
//                    //reverse the latlongs also
//                    pts=(ArrayList<POINT2>)tg.LatLongs.clone();
//                    for(j=0;j<tg.LatLongs.size();j++)
//                        tg.LatLongs.set(j, pts.get(pts.size()-j-1));
//                    break;
                default:
                    return;
            }
        }
        catch (Exception exc)
        {
                ErrorLogger.LogException(_className, "setHostileLC",
                        new RendererException("Failed inside setHostileLC", exc));

        }
    }   
    /**
     * Calculates the scale using the converter and the clipbounds
     * and sets overhead wire type based on the scale
     * @param tg
     * @param converter
     * @param clipBounds
     * @return 
     */
    protected static double getScale(TGLight tg, 
            IPointConversion converter,
            Object clipBounds)
    {
        double scale=0;
        try
        {
            if(clipBounds==null || converter==null)
                return 0;
            
            Rectangle2D clipRect=null;
            ArrayList<Point2D> clipArray=null;            
            if(clipBounds.getClass().isAssignableFrom(Rectangle2D.Double.class))
            {
                clipRect=(Rectangle2D)clipBounds;
            }
            else if(clipBounds.getClass().isAssignableFrom(Rectangle2D.class))
            {
                clipRect=(Rectangle2D)clipBounds;
            }
            else if(clipBounds.getClass().isAssignableFrom(Rectangle.class))
            {
                clipRect=(Rectangle2D)clipBounds;
            }
            else if(clipBounds.getClass().isAssignableFrom(ArrayList.class))
            {
                clipArray=(ArrayList<Point2D>)clipBounds;
                clipRect=RenderMultipoints.clsUtility.getMBR(clipArray);
            }
            
            double left=clipRect.getMinX();
            double right=clipRect.getMaxX();
            double distanceInPixels=Math.abs(right-left);
            double top=clipRect.getMinY();
            Point2D ul=new Point2D.Double(left,top);
            Point2D ur=new Point2D.Double(right,top);
            Point2D ulGeo = converter.PixelsToGeo(ul);
            Point2D urGeo = converter.PixelsToGeo(ur);
            POINT2 pt2ulGeo=new POINT2(ulGeo.getX(),ulGeo.getY());
            POINT2 pt2urGeo=new POINT2(urGeo.getX(),urGeo.getY());
            double distanceInMeters=mdlGeodesic.geodesic_distance(pt2ulGeo, pt2urGeo, null, null);
            //sccale=(distanceInPixels pixels/distanceInMeters meters)*(1 inch/96 pixels)*(1 meter/ 39.37 inch)
            scale=(distanceInPixels/distanceInMeters)*(1.0d/96.0d)*(1.0d/39.37d);
            scale=1.0d/scale;
            //reset the linetype for overhead wire if the sclae is large
            int lineType=tg.get_LineType();
            if(lineType==TacticalLines.OVERHEAD_WIRE && scale >=250000)
                tg.set_LineType(TacticalLines.OVERHEAD_WIRE_LS);            
        }
        catch (Exception exc)
        {
                ErrorLogger.LogException(_className, "getScale",
                        new RendererException("Failed inside getScale", exc));

        }
        return scale;
    }    
    /**
     * set the clip rectangle as an arraylist or a Rectangle2D depending on the object
     * @param clipBounds
     * @param clipRect
     * @param clipArray
     * @return 
     */
    private static boolean setClip(Object clipBounds, Rectangle2D clipRect, ArrayList<Point2D>clipArray)
    {
        try
        {
            if(clipBounds==null)
            {
                return false;
            }
            else if(clipBounds.getClass().isAssignableFrom(Rectangle2D.Double.class))
            {
                clipRect.setRect((Rectangle2D)clipBounds);
            }
            else if(clipBounds.getClass().isAssignableFrom(Rectangle2D.class))
            {
                clipRect.setRect((Rectangle2D)clipBounds);
            }
            else if(clipBounds.getClass().isAssignableFrom(Rectangle.class))
            {
                clipRect.setRect((Rectangle2D)clipBounds);
            }
            else if(clipBounds.getClass().isAssignableFrom(ArrayList.class))
            {
                clipArray.addAll((ArrayList)clipBounds);                
            }
        }
        catch (Exception exc)
        {
                ErrorLogger.LogException(_className, "setClip",
                        new RendererException("Failed inside setClip", exc));

        }
        return true;
    }
    /**
     * public render function transferred from JavaLineArrayCPOF project. Use this function
     * to replicate CPOF renderer functionality.
     * @param mss the milStdSymbol object
     * @param converter the geographic to pixels coordinate converter
     * @param clipBounds the pixels based clip bounds
     */
    public static void render(MilStdSymbol mss,
            IPointConversion converter,
            Object clipBounds)
    {
        try
        {
            ArrayList<ShapeInfo>shapeInfos=new ArrayList();
            ArrayList<ShapeInfo>modifierShapeInfos=new ArrayList();
            render(mss,converter,shapeInfos,modifierShapeInfos,clipBounds);
        }
        catch (Exception exc)
        {
                ErrorLogger.LogException(_className, "render",
                        new RendererException("render", exc));

        }
    }    
    /**
     * Generic tester button says Tiger or use JavaRendererSample.
     * Generic renderer testers: called by JavaRendererSample and TestJavaLineArray
     * public render function transferred from JavaLineArrayCPOF project. Use this function
     * to replicate CPOF renderer functionality.
     * @param tg tactical graphic
     * @param converter geographic to pixels converter
     * @param shapeInfos ShapeInfo array
     * @param modifierShapeInfos modifier ShapeInfo array
     * @param clipBounds clip bounds
     */
    public static void render(MilStdSymbol mss,
            IPointConversion converter,
            ArrayList<ShapeInfo>shapeInfos,
            ArrayList<ShapeInfo>modifierShapeInfos,
            Object clipBounds)
    {
        try 
        {            
            boolean shiftLines=Channels.getShiftLines();
            //end section
            
            Rectangle2D clipRect=new Rectangle2D.Double();
            ArrayList<Point2D> clipArray=new ArrayList();
            setClip(clipBounds,clipRect,clipArray);
            
            int rev=mss.getSymbologyStandard();
            JavaTacticalRenderer.clsUtility.initializeLinetypes(rev);
            TGLight tg=createTGLightFromMilStdSymbol(mss,converter);
            CELineArray.setClient("generic");
            if(shiftLines)
            {
                //Channels.setClient("generic");
                String affiliation =tg.get_Affiliation();
                Channels.setAffiliation(affiliation);
            }
            CELineArray.setMinLength(2.5);    //2-27-2013
            
            //if(rev==RendererSettings.Symbology_2525C)
            //{
                JavaTacticalRenderer.clsUtility.setRevC(tg);
            //}
            //resets the linetypes for overhead wire and other rev c symbols
            double scale=getScale(tg,converter,clipBounds);
            
            int linetype=tg.get_LineType();
            //replace calls to MovePixels
            RenderMultipoints.clsUtility.RemoveDuplicatePoints(tg);
            
            setHostileLC(tg);
                        
            //diagnostic
//            tg.modifiers=new ArrayList();
            BufferedImage bi=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d=bi.createGraphics();
//            if(clsUtility.addModifiersBeforeClipping(tg))
//                 Modifier2.AddModifiers(tg,g2d,clipBounds);
            //end section
                        
            clsUtilityCPOF.SegmentGeoPoints(tg, converter);
            clsUtility.FilterAXADPoints(tg, converter);
                                   
            //prevent vertical segments for oneway, twoway, alt
            JavaTacticalRenderer.clsUtility.FilterVerticalSegments(tg);
            boolean isChange1Area = JavaTacticalRenderer.clsUtility.IsChange1Area(linetype, null);
            boolean isTextFlipped = false;
            //for 3d change 1 symbols we do not transform the points

            //if it is world view then we want to flip the far points about
            //the left and right sides to get two symbols
            ArrayList<POINT2> farLeftPixels=new ArrayList();
            ArrayList<POINT2> farRightPixels=new ArrayList();
            if(isChange1Area==false)
                clsUtilityCPOF.GetFarPixels(tg,converter,farLeftPixels,farRightPixels);

            ArrayList<Shape2> shapesLeft=new ArrayList();
            ArrayList<Shape2> shapesRight=new ArrayList();
            ArrayList<Shape2> shapes=null;   //use this to collect all the shapes

            //CPOF 6.0 diagnostic
            ArrayList<Shape2>textSpecsLeft=null;
            ArrayList<Shape2>textSpecsRight=null;
                //Note: DisplayModifiers3 returns early if textSpecs are null
            textSpecsLeft=new ArrayList();
            textSpecsRight=new ArrayList();

            if(farLeftPixels.size()>0)
            {
                tg.Pixels=farLeftPixels;
                shapesLeft = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds);
                //CPOF 6.0
                //returns early if textSpecs are null
                //clsModifier.DisplayModifiers3(tg, g2d, textSpecsLeft, isTextFlipped);
                JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecsLeft, isTextFlipped, null);
            }
            if(farRightPixels.size()>0)
            {
                tg.Pixels=farRightPixels;
                shapesRight = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds);
                //CPOF 6.0
                //returns early if textSpecs are null
                //clsModifier.DisplayModifiers3(tg, g2d, textSpecsRight, isTextFlipped);
                JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecsRight, isTextFlipped,null);
            }

            //CPOF 6.0 diagnostic
            //ArrayList<TextSpec>textSpecs=new ArrayList();
            ArrayList<Shape2>textSpecs=new ArrayList();

            if(shapesLeft.isEmpty() || shapesRight.isEmpty())
            {
                ArrayList<Shape2> linesWithFillShapes=null;
                //linesWithFillShapes=clsClipPolygon2.LinesWithFill(tg, clipBounds);
                if(clipArray!=null && !clipArray.isEmpty())
                    linesWithFillShapes=clsClipQuad.LinesWithFill(tg, clipArray);
                else if(clipRect != null && clipRect.getWidth()!=0)
                    linesWithFillShapes=clsClipPolygon2.LinesWithFill(tg, clipRect);
                else
                    linesWithFillShapes=clsClipPolygon2.LinesWithFill(tg, null);

                //diagnostic: comment two lines if using the WW tester
                if (clsUtilityCPOF.canClipPoints(tg) && clipBounds != null)
                {
                    if(clipArray!=null && !clipArray.isEmpty())
                        clsClipQuad.ClipPolygon(tg, clipArray);
                    else if (clipRect!=null && clipRect.getWidth()!=0)
                        clsClipPolygon2.ClipPolygon(tg, clipRect);
                    
                    tg.LatLongs=clsUtility.PixelsToLatLong(tg.Pixels, converter);
                }
                
                //diagnostic 1-28-13
                JavaTacticalRenderer.clsUtility.InterpolatePixels(tg);
                
                tg.modifiers=new ArrayList();
                //if(clsUtility.addModifiersBeforeClipping(tg))
                 Modifier2.AddModifiers(tg,g2d,clipBounds);

                clsUtilityCPOF.FilterPoints2(tg,converter);
                clsUtilityCPOF.ClearPixelsStyle(tg);
                //shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds);
                //add section to replace preceding line M. Deutch 11-4-2011
                ArrayList rangeFanFillShapes=null;
                //do not fill the original shapes for circular range fans
                int savefillStyle=tg.get_FillStyle();
                if(linetype==TacticalLines.RANGE_FAN)
                    tg.set_Fillstyle(0);
                
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds);
                
                switch(linetype)
                {
                    case TacticalLines.RANGE_FAN:
                    case TacticalLines.RANGE_FAN_SECTOR:
                        TGLight tg1=clsUtilityCPOF.GetCircularRangeFanFillTG(tg);
                        tg1.set_Fillstyle(savefillStyle);
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped,clipBounds);
                        
                        if(rangeFanFillShapes != null)   
                        {
                            shapes.addAll(0, rangeFanFillShapes);                
                        }
                        break;
                    default:
                        break;
                }
                                                
                //undo any fillcolor for lines with fill
                clsUtilityCPOF.LinesWithSeparateFill(tg.get_LineType(), shapes);
                clsClipPolygon2.addAbatisFill(tg, shapes);

                //if this line is commented then the extra line in testbed goes away
                if(shapes != null && linesWithFillShapes != null && linesWithFillShapes.size()>0)
                        shapes.addAll(0,linesWithFillShapes);


                if(shapes !=null && shapes.size()>0)
                {
                    JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecs, isTextFlipped,null);
                    Shape2ToShapeInfo(modifierShapeInfos,textSpecs);
                    mss.setModifierShapes(modifierShapeInfos);
                }
            }
            else    //symbol was more than 180 degrees wide, use left and right symbols
            {
                shapes=shapesLeft;
                shapes.addAll(shapesRight);

                if(textSpecs != null)
                {
                    textSpecs.addAll(textSpecsLeft);
                    textSpecs.addAll(textSpecsRight);
                }
            }
            //post-clip the points if the tg could not be pre-clipped
            if(clsUtilityCPOF.canClipPoints(tg)==false && clipBounds != null)
            {
                shapes=clsUtilityCPOF.postClipShapes(tg,shapes, clipBounds);
            }

           Shape2ToShapeInfo(shapeInfos,shapes);
           mss.setSymbolShapes(shapeInfos);
        }
    catch (Exception exc)
    {
            ErrorLogger.LogException(_className, "render",
                    new RendererException("Failed inside render", exc));

            //throw exc;
        }
        return;
    }
    /**
     * @deprecated 
     * public render function added primarily for METOCs because at the time
     * cannot instantiate a MilStdSymbol from a METOC symbol code
     * other than that it is identical to the same named function that takes mss
     * as the 1st parameter
     * @param tg
     * @param converter
     * @param shapeInfos
     * @param modifierShapeInfos
     * @param clipBounds
     */
    public static void render(TGLight tg,
            IPointConversion converter,
            ArrayList<ShapeInfo>shapeInfos,
            ArrayList<ShapeInfo>modifierShapeInfos,
            Rectangle2D clipBounds)
    {
        //Graphic g = null;
        try {
            //build a new TGLight from props
            //TacticalGraphicProperties tgProps = (TacticalGraphicProperties) props;

            //String symbolid = tgProps.get2525Code();
            //TGLight tg=createTGLightFromMilStdSymbol(mss,converter);

            //int linetype = CELineArray.CGetLinetypeFromString(symbolid);
            setHostileLC(tg);
            boolean isChange1Area = JavaTacticalRenderer.clsUtility.IsChange1Area(tg.get_LineType(), null);
            //int bolMETOC=JavaTacticalRenderer.clsMETOC.IsWeather(tg.get_SymbolId());
            //TGLight tg = new TGLight();
            //clsUtilityCPOF.CPOFTGLight(tg, tgProps, converter);

            boolean isTextFlipped = false;
            //for 3d change 1 symbols we do not transform the points

            //if (props.getConfiguration().getYAxisDirection()==YAxisDirection.UP && isChange1Area == false) {
            //    clsUtilityCPOF.TransformPixels(tg, clipBounds);
            //}

            //if (props.getConfiguration().getYAxisDirection()==YAxisDirection.UP) {
            //    isTextFlipped = true;
            //}
            //if it is world view then we want to flip the far points about
            //the left and right sides to get two symbols
            ArrayList<POINT2> farLeftPixels=new ArrayList();
            ArrayList<POINT2> farRightPixels=new ArrayList();
            if(isChange1Area==false)
                clsUtilityCPOF.GetFarPixels(tg,converter,farLeftPixels,farRightPixels);

            //CPOF 6.0 TextSpecs diagnostic
            BufferedImage bi=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d=bi.createGraphics();

            ArrayList<Shape2> shapesLeft=new ArrayList();
            ArrayList<Shape2> shapesRight=new ArrayList();
            ArrayList<Shape2> shapes=null;   //use this to collect all the shapes

            //CPOF 6.0 diagnostic
            ArrayList<Shape2>textSpecsLeft=null;
            ArrayList<Shape2>textSpecsRight=null;
            //if(tg.get_FontBackColor()!=null)
            //{
                //Note: DisplayModifiers3 returns early if textSpecs are null
                textSpecsLeft=new ArrayList();
                textSpecsRight=new ArrayList();
            //}
            if(farLeftPixels.size()>0)
            {
                tg.Pixels=farLeftPixels;
                shapesLeft = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds);
                //CPOF 6.0
                //returns early if textSpecs are null
                //clsModifier.DisplayModifiers3(tg, g2d, textSpecsLeft, isTextFlipped);
                JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecsLeft, isTextFlipped,null);
            }
            if(farRightPixels.size()>0)
            {
                tg.Pixels=farRightPixels;
                shapesRight = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds);
                //CPOF 6.0
                //returns early if textSpecs are null
                //clsModifier.DisplayModifiers3(tg, g2d, textSpecsRight, isTextFlipped);
                JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecsRight, isTextFlipped,null);
            }

            //CPOF 6.0 diagnostic
            //ArrayList<TextSpec>textSpecs=new ArrayList();
            ArrayList<Shape2>textSpecs=new ArrayList();

            if(shapesLeft.isEmpty() || shapesRight.isEmpty())
            {
                ArrayList linesWithFillShapes=clsClipPolygon2.LinesWithFill(tg, clipBounds);

                //diagnostic: comment two lines if using the WW tester
                if (clsUtilityCPOF.canClipPoints(tg) && clipBounds != null)
                   clsClipPolygon2.ClipPolygon(tg, clipBounds);

                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped,clipBounds);

                //undo any fillcolor for lines with fill
                clsUtilityCPOF.LinesWithSeparateFill(tg.get_LineType(), shapes);
                clsClipPolygon2.addAbatisFill(tg, shapes);

                //if this line is commented then the extra line in testbed goes away
                if(shapes != null && linesWithFillShapes != null && linesWithFillShapes.size()>0)
                        shapes.addAll(0,linesWithFillShapes);


                //CPOF 6.0 diagnostic
                //returns early if textSpecs are null
                if(shapes !=null && shapes.size()>0)
                {
                    //clsModifier.DisplayModifiers3(tg, g2d, textSpecs, isTextFlipped);
                    JavaTacticalRenderer.Modifier2.DisplayModifiers2(tg, g2d, textSpecs, isTextFlipped,null);
                    Shape2ToShapeInfo(modifierShapeInfos,textSpecs);
                }
            }
            else    //symbol was more than 180 degrees wide, use left and right symbols
            {
                shapes=shapesLeft;
                shapes.addAll(shapesRight);

                //CPOF 6.0 diagnostic
                if(textSpecs != null)
                {
                    textSpecs.addAll(textSpecsLeft);
                    textSpecs.addAll(textSpecsRight);
                }
            }
            //post-clip the points if the tg could not be pre-clipped
            if(clsUtilityCPOF.canClipPoints(tg)==false && clipBounds != null)
                shapes=clsUtilityCPOF.postClipShapes(tg,shapes, clipBounds);

            //ShapeSpec[] shapeSpecs = Shape2ToShapeSpec(shapes);
           Shape2ToShapeInfo(shapeInfos,shapes);

            //6.0 change
            //if (props.getConfiguration().getYAxisDirection()==YAxisDirection.UP && isChange1Area == false /** && useGeoPoints==false **/) {
              //  TransformShapeSpecs(shapeSpecs, clipBounds);
            //}

            //shapeSpecs = clsUtilityCPOF.ValidateShapeSpecs(shapeSpecs, clipBounds);

            //CPOF 6.0 diagnostic TextSpec
            //change this to instantiate with new Graphic(TextSpecs[],ShapeSpecs[],category))
            //g = new Graphic(shapeSpecs, GraphicCategory.OPEN_LINE_GRAPHIC);
            //CPOF 6.0 diagnostic
            //TextSpec[]textSpecs2=null;

            //if(textSpecs != null && textSpecs.size()>0)
            //    textSpecs2=textSpecs.toArray(new TextSpec[textSpecs.size()]);
            //if(textSpecs2==null)
            //    textSpecs2=new TextSpec[0];


            //if (props.getConfiguration().getYAxisDirection()==YAxisDirection.UP && isChange1Area == false /** && useGeoPoints==false **/) {
             //   TransformTextSpecs(textSpecs2, clipBounds);
            //}

            //we have the shapes and the text, can build the graphic
            //g = new Graphic(textSpecs2, shapeSpecs, GraphicCategory.OPEN_LINE_GRAPHIC);

            //if (g == null || g.getShapeSpecs() == null)
            //{
            //    throw new GraphicFactoryException("No ShapeSpecs");
            //}

        }
    catch (Exception exc)
    {
            ErrorLogger.LogException(_className, "render",
                    new RendererException("Failed inside render", exc));

            //throw exc;
        }
        //return g;
    }

}

