/*
 *  Copyright (C) 2011-2012 GeoForge Project
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.geoforge.worldwind.render;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;
import org.geoforge.worldwind.handler.IGfrWwdHandlerRlr;
import org.geoforge.lang.IShrObj;
import org.geoforge.worldwind.util.DmsOperation;
import org.geoforge.lang.util.logging.FileHandlerLogger;
import org.geoforge.mdldat.event.GfrEvtMdlIdDtaRenamedTlo;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeAbs.ChangedGlobeDim;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeDimTwo;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeDimTwo.ChangedGlobeDimTwo;
import org.geoforge.mdl.event.GfrEvtMdlIdAbs;
import org.geoforge.wrpbasprsdsp.render.globe.WrpRenderGlobeDimTwo;

/**
 *
 * @author bantchao
 *
 * email: bantchao_AT_gmail.com
 * ... please remove "_AT_" from the above string to get the right email address
 *
 */
abstract public class GfrRndSurfacePolygonTloAbs extends SurfacePolygon implements
        IShrObj,
        IGfrWwdHandlerRlr,
        Observer
{
    // ----
    // begin: instantiate logger for this class
    final private static Logger _LOGGER_ = Logger.getLogger(GfrRndSurfacePolygonTloAbs.class.getName());

    static
    {
        GfrRndSurfacePolygonTloAbs._LOGGER_.addHandler(FileHandlerLogger.s_getInstance());
    }

    // end: instantiate logger for this class
    // ----
    
    
    
    private String _strId_ = null;
    
    protected GfrRndSurfacePolygonTloAbs(
            PropertyChangeListener lstShouldRedraw, 
            String strId,
            String strName,
            List<Position> lstPosition)
            throws Exception
    {
        super(lstPosition);
        
        this._strId_ = strId;
        
        _setAttributes_();
        _setName_(strName);
        _setTooltip_();
        
        if (lstShouldRedraw != null)
         super.addPropertyChangeListener(lstShouldRedraw);
        
    }
    
    @Override
    public void update(Observable obs, Object objEvt) 
    {
       // beg display
       
       if (objEvt instanceof EvtMdlDspRndGlobeDimTwo)
       {
          EvtMdlDspRndGlobeDimTwo evt = (EvtMdlDspRndGlobeDimTwo) objEvt;
          
          String strId = evt.getId();
          
          if (strId.compareTo(this._strId_) != 0)
             return;
          
          Object objWhat = evt.getWhat();
          
          if (objWhat == ChangedGlobeDim.TOOLTIP)
          {
             Boolean boo = (Boolean) evt.getValue();
             super.setValue(GfrRndKeys.STR_HAS_TOOLTIP, boo.booleanValue());
             // memo: no need to send event to redraw!
             return;
          }
          
          if (objWhat == ChangedGlobeDimTwo.COLOR_IN) // what about transparency?
          {
             Color col = (Color) evt.getValue();
             BasicShapeAttributes bsa = (BasicShapeAttributes) super.getAttributes();
             bsa.setInteriorMaterial(new Material(col));
             this.firePropertyChange(GfrRndShouldRedraw.STR, null, null);
             return;
          }
          
          if (objWhat == ChangedGlobeDimTwo.TRANSPARENCY_IN)
          {
             Float flo = (Float) evt.getValue();
             float fltAlpha = flo.floatValue();
             BasicShapeAttributes bsa = (BasicShapeAttributes) super.getAttributes();
             bsa.setInteriorOpacity((double) fltAlpha);
             this.firePropertyChange(GfrRndShouldRedraw.STR, null, null);
             return;
          }
          
          if (objWhat == ChangedGlobeDimTwo.COLOR_OUT) // what about transparency?
          {
             Color col = (Color) evt.getValue();
             BasicShapeAttributes bsa = (BasicShapeAttributes) super.getAttributes();
             bsa.setOutlineMaterial(new Material(col));
             this.firePropertyChange(GfrRndShouldRedraw.STR, null, null);
             return;
          }
          
          if (objWhat == ChangedGlobeDimTwo.TRANSPARENCY_OUT)
          {
             Float flo = (Float) evt.getValue();
             float fltAlpha = flo.floatValue();
             BasicShapeAttributes bsa = (BasicShapeAttributes) super.getAttributes();
             bsa.setOutlineOpacity((double) fltAlpha);
             this.firePropertyChange(GfrRndShouldRedraw.STR, null, null);
             return;
          }
          
          
          return;
       }
       
       // end display
       
       
       // beg data
       
       GfrEvtMdlIdAbs objId = (GfrEvtMdlIdAbs) objEvt;
       
       String strIdEvt = objId.getId();
       
       if (strIdEvt.compareTo(this._strId_) != 0)
          return;
       
       if (objId instanceof GfrEvtMdlIdDtaRenamedTlo)
       {
          GfrEvtMdlIdDtaRenamedTlo objRename = (GfrEvtMdlIdDtaRenamedTlo) objId;
          super.setValue(gov.nasa.worldwind.avlist.AVKey.DISPLAY_NAME, objRename.getNameNew());
         // memo: no need to redraw, coz of just handling toolTip
          return;
       }
       
       // end data
    }

    @Override
    public String getIdGfr() { return this._strId_;}

    @Override
    public Position getPositionBarycentric() throws Exception
    {
        Iterator<? extends LatLon> itr = super.getOuterBoundary().iterator();
        
        if (! itr.hasNext())
        {
            String str = "! itr.hasNext()";
            GfrRndSurfacePolygonTloAbs._LOGGER_.severe(str);
            throw new Exception(str);
        }
        
        double dblMinLat = 500;
        double dblMaxLat = -500;
        double dblMinLon = 500;
        double dblMaxLon = -500;
        
        while (itr.hasNext())
        {
            LatLon llnCur = itr.next();
            double[] dbls = llnCur.asDegreesArray();
            
            if (dbls[0] < dblMinLat)
                dblMinLat = dbls[0];
            
            if (dbls[1] < dblMinLon)
                dblMinLon = dbls[1];
            
            if (dbls[0] > dblMaxLat)
                dblMaxLat = dbls[0];
            
            if (dbls[1] > dblMaxLon)
                dblMaxLon = dbls[1];
        }
        
        double dblLat = dblMinLat;
        dblLat += dblMaxLat;
        dblLat /= 2;
        
        double dblLon = dblMinLon;
        dblLon += dblMaxLon;
        dblLon /= 2;

        return Position.fromDegrees(dblLat, dblLon);
    }
    
    
    
    @Override
    public boolean init() 
    {
        return true;
    }

    @Override
    public void destroy() 
    {
    }


   @Override
   public double getCharacteristicDimension() throws Exception
   {
        Iterator<? extends LatLon> itr = this.getLocations().iterator();
        
        if (! itr.hasNext())
        {
            String str = "! itr.hasNext()";
            GfrRndSurfacePolygonTloAbs._LOGGER_.severe(str);
            throw new Exception(str);
        }
        
        double dblMinLat = 500;
        double dblMaxLat = -500;
        double dblMinLon = 500;
        double dblMaxLon = -500;
        
        while (itr.hasNext())
        {
            LatLon llnCur = itr.next();
            double[] dbls = llnCur.asDegreesArray();
            
            if (dbls[0] < dblMinLat)//=a
                dblMinLat = dbls[0];
            
            if (dbls[1] < dblMinLon)//=c
                dblMinLon = dbls[1];
            
            if (dbls[0] > dblMaxLat)//=b
                dblMaxLat = dbls[0];
            
            if (dbls[1] > dblMaxLon)//=d
                dblMaxLon = dbls[1];
        }
        
        double dist = DmsOperation.s_getDistanceFromDeg(dblMinLat, dblMinLon, dblMaxLat, dblMaxLon);
        dist *= 1.5;
        return dist;
   }
   
   private void _setAttributes_() throws Exception
   {
      BasicShapeAttributes bsa = new BasicShapeAttributes();
      
      // beg out
      bsa.setDrawOutline(true);
      final double dblWidthOut = 1;
      bsa.setOutlineWidth(dblWidthOut);
      
      Color colRgbOut = WrpRenderGlobeDimTwo.getInstance().getColorOut(this._strId_);  
     bsa.setOutlineMaterial(new Material(colRgbOut));
     
     float fltAlphaOut = WrpRenderGlobeDimTwo.getInstance().getTransparencyOut(this._strId_);
     bsa.setOutlineOpacity((double) fltAlphaOut);
      // end out
      
      // beg in
     bsa.setDrawInterior(true);
     Color colRgbIn = WrpRenderGlobeDimTwo.getInstance().getColorIn(this._strId_);  
     bsa.setInteriorMaterial(new Material(colRgbIn));
     
     float fltAlphaIn = WrpRenderGlobeDimTwo.getInstance().getTransparencyIn(this._strId_);
     bsa.setInteriorOpacity((double) fltAlphaIn);
      // end in
     
     super.setAttributes(bsa);
   }
   
   private void _setName_(String strName)
    {
       super.setValue(gov.nasa.worldwind.avlist.AVKey.DISPLAY_NAME, strName);
       super.setValue(GfrRndKeys.STR_HAS_CONTEXTUAL_MENU, true); // always true
    }
    
    private void _setTooltip_() throws Exception
    {
       boolean blnIsToolTip = WrpRenderGlobeDimTwo.getInstance().isTooltip(this._strId_);
       super.setValue(GfrRndKeys.STR_HAS_TOOLTIP, blnIsToolTip);
    }
}

