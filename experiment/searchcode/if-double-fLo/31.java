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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;
import org.geoforge.lang.IShrObj;
import org.geoforge.lang.util.logging.FileHandlerLogger;
import org.geoforge.mdldat.event.GfrEvtMdlIdDtaRenamedTlo;
import org.geoforge.worldwind.handler.IGfrWwdHandlerRlr;
import org.geoforge.wrpbasprsdsp.render.globe.WrpRenderGlobeDimOnePoint;
import org.geoforge.mdldsp.event.render.globe.*;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeAbs.ChangedGlobeDim;
import org.geoforge.mdldsp.event.render.globe.EvtMdlDspRndGlobeDimOneAbs.ChangedGlobeDimOne;
import org.geoforge.mdl.event.GfrEvtMdlIdAbs;

/**
 *
 * @author Amadeus.Sowerby
 *
 * email: Amadeus.Sowerby_AT_gmail.com
 * ... please remove "_AT_" from the above string to get the right email address
 */
abstract public class GfrRndPointPlacemarkTloAbs extends PointPlacemark implements 
        IShrObj,
        IGfrWwdHandlerRlr,
        Observer
{
   // ----
    // begin: instantiate logger for this class
    final private static Logger _LOGGER_ = Logger.getLogger(GfrRndPointPlacemarkTloAbs.class.getName());

    static
    {
        GfrRndPointPlacemarkTloAbs._LOGGER_.addHandler(FileHandlerLogger.s_getInstance());
    }

    // end: instantiate logger for this class
    // ----
    
    private String _strId_ = null;
    
    protected GfrRndPointPlacemarkTloAbs(
            PropertyChangeListener lstShouldRedraw,
            String strId,
            String strName,
            Position pos
            ) 
            throws Exception
    {
        super(pos);
        
        this._strId_ = strId;
        
        _setAttributes_();
        _setName_(strName);
        _setTooltip_();
        
        if (lstShouldRedraw != null)
         super.addPropertyChangeListener(lstShouldRedraw);
        
        super.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
    }
    
    
    @Override
    public void update(Observable obs, Object objEvt) 
    {
       // beg display
       
       if (objEvt instanceof EvtMdlDspRndGlobeDimOnePoint)
       {
          EvtMdlDspRndGlobeDimOnePoint evt = (EvtMdlDspRndGlobeDimOnePoint) objEvt;
          
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
          
          if (objWhat == ChangedGlobeDimOne.COLOR) // what about transparency?
          {
             Color col = (Color) evt.getValue();
             
             PointPlacemarkAttributes ppa = super.getAttributes();
             ppa.setLineMaterial(new Material(col));
             this.firePropertyChange(GfrRndShouldRedraw.STR, null, null);
             return;
          }
          
          if (objWhat == ChangedGlobeDimOne.TRANSPARENCY)
          {
             Float flo = (Float) evt.getValue();
             float fltAlpha = flo.floatValue();
             fltAlpha *=100;
             fltAlpha *= 2.55;
             int intAlpha = Math.round(fltAlpha);
             
             PointPlacemarkAttributes ppa = super.getAttributes();
             Color colOld = ppa.getLineMaterial().getDiffuse();
             Color colNew = new Color(colOld.getRed(), colOld.getGreen(), colOld.getBlue(), intAlpha);
             ppa.setLineMaterial(new Material(colNew));
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
    public Position getPositionBarycentric() throws Exception
    {
        return super.getReferencePosition();
    }

    @Override
    public double getCharacteristicDimension()
    {
       return 10000D; // !!!!!!!!!!
    }
    
    @Override
    public String getIdGfr()    
    { 
        return this._strId_;
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

    private void _setAttributes_() throws Exception
    {
       PointPlacemarkAttributes ppa = new PointPlacemarkAttributes();
        Color colRgb = WrpRenderGlobeDimOnePoint.getInstance().getColor(this._strId_);
        
        float fltAlpha = WrpRenderGlobeDimOnePoint.getInstance().getTransparency(this._strId_);
        fltAlpha *=100;
        fltAlpha *= 2.5;
        int intAlpha = Math.round(fltAlpha);
        Color col = new Color(colRgb.getRed(), colRgb.getGreen(), colRgb.getBlue(), intAlpha);
        
        
        ppa.setLineMaterial(new Material(col));
        ppa.setScale(7d);
        ppa.setUsePointAsDefaultImage(true);    
        super.setAttributes(ppa);
    }
    
    private void _setName_(String strName)
    {
       super.setValue(gov.nasa.worldwind.avlist.AVKey.DISPLAY_NAME, strName);
       super.setValue(GfrRndKeys.STR_HAS_CONTEXTUAL_MENU, true); // always true
    }
    
    private void _setTooltip_() throws Exception
    {
       boolean blnIsToolTip = WrpRenderGlobeDimOnePoint.getInstance().isTooltip(this._strId_);
       super.setValue(GfrRndKeys.STR_HAS_TOOLTIP, blnIsToolTip);
    }
}

