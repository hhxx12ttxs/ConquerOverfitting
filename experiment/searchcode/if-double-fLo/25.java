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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Observable;
import java.util.logging.Logger;
import org.geoforge.lang.util.logging.FileHandlerLogger;
import org.geoforge.mdldat.event.GfrEvtMdlIdDtaRenamedMlo;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeAbs.ChangedGlobeDim;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeDimOneAbs.ChangedGlobeDimOne;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeDimOneLine;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeDimOneLine.ChangedGlobeDimOneLine;
import org.geoforge.wrpbasprsdsp.render.globe.GfrWrpRenderGlobeDimOneLine;

/**
 *
 * @author bantchao
 *
 * email: bantchao_AT_gmail.com
 * ... please remove "_AT_" from the above string to get the right email address
 *
 */
abstract public class GfrRndSurfacePolylineMloAbs extends GfrRndSurfacePolylineAbs
{
    // ----
    // begin: instantiate logger for this class
    final private static Logger _LOGGER_ = Logger.getLogger(GfrRndSurfacePolylineMloAbs.class.getName());

    static
    {
        GfrRndSurfacePolylineMloAbs._LOGGER_.addHandler(FileHandlerLogger.s_getInstance());
    }

    // end: instantiate logger for this class
    // ----
    
    protected String _strIdParent_ = null;
    
    public String getIdParentGtr() { return this._strIdParent_; }
    
    
    protected GfrRndSurfacePolylineMloAbs(
            PropertyChangeListener lstShouldRedraw, 
            String strId,
            String strName,
            List<Position> lstPosition,
            String strIdParent)
            throws Exception
    {
        super(lstShouldRedraw, strId, strName, lstPosition);
        
        this._strIdParent_ = strIdParent;
    }
    
    @Override
    public void update(Observable obs, Object objEvt) 
    {
       // beg display
       
       if (objEvt instanceof EvtMdlDspRndGlobeDimOneLine)
       {
          EvtMdlDspRndGlobeDimOneLine evt = (EvtMdlDspRndGlobeDimOneLine) objEvt;
          
          String strId = evt.getId();
          
          if (strId.compareTo(this._strIdParent_) != 0)
             return;
          
          Object objWhat = evt.getWhat();
          
          if (objWhat == ChangedGlobeDim.TOOLTIP)
          {
             Boolean boo = (Boolean) evt.getValue();
             super.setValue(GfrRndKeys.STR_HAS_TOOLTIP, boo.booleanValue());
             // memo: no need to send event to redraw!
             return;
          }
          

          
          if (objWhat == ChangedGlobeDimOne.COLOR)
          {
             Color col = (Color) evt.getValue();
             BasicShapeAttributes bsa = (BasicShapeAttributes) super.getAttributes();
             bsa.setOutlineMaterial(new Material(col));
             this.firePropertyChange(GfrRndShouldRedraw.STR, null, null);
             return;
          }
          
          if (objWhat == ChangedGlobeDimOne.TRANSPARENCY)
          {
             Float flo = (Float) evt.getValue();
             float fltAlpha = flo.floatValue();
             BasicShapeAttributes bsa = (BasicShapeAttributes) super.getAttributes();
             bsa.setOutlineOpacity((double) fltAlpha);
             this.firePropertyChange(GfrRndShouldRedraw.STR, null, null);
             return;
          }
          
          if (objWhat == ChangedGlobeDimOneLine.THICKNESS)
          {
             Integer itg = (Integer) evt.getValue();
             BasicShapeAttributes bsa = (BasicShapeAttributes) super.getAttributes();
             bsa.setOutlineWidth(itg.doubleValue());
             this.firePropertyChange(GfrRndShouldRedraw.STR, null, null);
             return;
          }
          
          
          return;
       }
       
       // end display
       
       
       // beg data
       
       if (objEvt instanceof GfrEvtMdlIdDtaRenamedMlo)
      {
         GfrEvtMdlIdDtaRenamedMlo evt = (GfrEvtMdlIdDtaRenamedMlo) objEvt;
         String strIdChild = evt.getId();
         
         if (strIdChild.compareTo(super._strId) != 0)
            return;
         
         String strNameNewChild = evt.getNameNew();
         super.setValue(gov.nasa.worldwind.avlist.AVKey.DISPLAY_NAME, strNameNewChild);
         // memo: no need to redraw, coz of just handling toolTip
         
         return;
      }
       
       // end data
    }
    
    @Override
    protected void _setAttributes() throws Exception
    {
       BasicShapeAttributes bsa = new BasicShapeAttributes();

       bsa.setDrawOutline(true);
       double dblWidthOut = (double) GfrWrpRenderGlobeDimOneLine.getInstance().getThickness(this._strIdParent_);
       bsa.setOutlineWidth(dblWidthOut);
      
       Color colRgbOut = GfrWrpRenderGlobeDimOneLine.getInstance().getColor(this._strIdParent_);  
       bsa.setOutlineMaterial(new Material(colRgbOut));
     
       float fltAlphaOut = GfrWrpRenderGlobeDimOneLine.getInstance().getTransparency(this._strIdParent_);
       bsa.setOutlineOpacity((double) fltAlphaOut);
     
       super.setAttributes(bsa);
    }
    
    @Override
    protected void _setTooltip() throws Exception
    {
       boolean blnIsToolTip = GfrWrpRenderGlobeDimOneLine.getInstance().isTooltip(this._strIdParent_);
       super.setValue(GfrRndKeys.STR_HAS_TOOLTIP, blnIsToolTip);
    }
}

