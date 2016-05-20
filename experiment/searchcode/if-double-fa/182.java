/*---

    iGeo - http://igeo.jp

    Copyright (c) 2002-2012 Satoru Sugihara

    This file is part of iGeo.

    iGeo is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, version 3.

    iGeo is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with iGeo.  If not, see <http://www.gnu.org/licenses/>.

---*/

package igeo;

import java.util.ArrayList;
import java.awt.Color;

/**
   Class of IDynamicObject to simulate tension force between two particles.
   
   @author Satoru Sugihara
   @version 0.7.0.0;
*/
public class ITensionLineOnCurve extends ICurve implements ITensionI{
    
    public ITensionOnCurve tension;
    
    public ITensionLineOnCurve(IParticleOnCurve p1, IParticleOnCurve p2){
	super(p1.pos(), p2.pos());
	initTensionLine(p1,p2);
    }
    
    public ITensionLineOnCurve(IParticleOnCurve p1, IParticleOnCurve p2, double tension){
	super(p1.pos(), p2.pos());
	initTensionLine(p1,p2);
	tension(tension);
    }
    
    public void initTensionLine(IParticleOnCurve p1, IParticleOnCurve p2){
	tension = new ITensionOnCurve(p1,p2,this);
	addDynamics(tension);
    }
    
    public double tension(){ return tension.tension(); }
    public ITensionLineOnCurve tension(double tensionIntensity){
	tension.tension(tensionIntensity); return this;
    }
    
    public boolean constant(){ return tension.constant(); }
    public ITensionLineOnCurve constant(boolean cnst){
	tension.constant(cnst); return this;
    }
    
    /** getting end point. i==0 or i==1 */
    public IParticleI pt(int i){ return tension.pt(i); }
    /** alias of pt(int) */
    public IParticleI particle(int i){ return pt(i); }
    /** position of particle(i) */
    public IVec pos(int i){ return tension.pos(i); }
    
    /** getting end point1. */
    public IParticleI pt1(){ return tension.pt1(); }
    /** alias of pt1() */
    public IParticleI particle1(){ return pt1(); }
    /** position of particle1 */
    public IVec pos1(){ return tension.pos1(); }
    
    /** getting end point2. */
    public IParticleI pt2(){ return tension.pt2(); }
    /** alias of pt2() */
    public IParticleI particle2(){ return pt2(); }
    /** position of particle1 */
    public IVec pos2(){ return tension.pos2(); }
    



    /******************************************************************************
     * IObject methods
     ******************************************************************************/
    
    public ITensionLineOnCurve name(String nm){ super.name(nm); return this; }
    public ITensionLineOnCurve layer(ILayer l){ super.layer(l); return this; }
    public ITensionLineOnCurve layer(String l){ super.layer(l); return this; }
    
    public ITensionLineOnCurve attr(IAttribute at){ super.attr(at); return this; }
    
    
    public ITensionLineOnCurve hide(){ super.hide(); return this; }
    public ITensionLineOnCurve show(){ super.show(); return this; }
    
    public ITensionLineOnCurve clr(Color c){ super.clr(c); return this; }
    public ITensionLineOnCurve clr(Color c, int alpha){ super.clr(c,alpha); return this; }
    public ITensionLineOnCurve clr(int gray){ super.clr(gray); return this; }
    public ITensionLineOnCurve clr(float fgray){ super.clr(fgray); return this; }
    public ITensionLineOnCurve clr(double dgray){ super.clr(dgray); return this; }
    public ITensionLineOnCurve clr(int gray, int alpha){ super.clr(gray,alpha); return this; }
    public ITensionLineOnCurve clr(float fgray, float falpha){ super.clr(fgray,falpha); return this; }
    public ITensionLineOnCurve clr(double dgray, double dalpha){ super.clr(dgray,dalpha); return this; }
    public ITensionLineOnCurve clr(int r, int g, int b){ super.clr(r,g,b); return this; }
    public ITensionLineOnCurve clr(float fr, float fg, float fb){ super.clr(fr,fg,fb); return this; }
    public ITensionLineOnCurve clr(double dr, double dg, double db){ super.clr(dr,dg,db); return this; }
    public ITensionLineOnCurve clr(int r, int g, int b, int a){ super.clr(r,g,b,a); return this; }
    public ITensionLineOnCurve clr(float fr, float fg, float fb, float fa){ super.clr(fr,fg,fb,fa); return this; }
    public ITensionLineOnCurve clr(double dr, double dg, double db, double da){ super.clr(dr,dg,db,da); return this; }
    public ITensionLineOnCurve hsb(float h, float s, float b, float a){ super.hsb(h,s,b,a); return this; }
    public ITensionLineOnCurve hsb(double h, double s, double b, double a){ super.hsb(h,s,b,a); return this; }
    public ITensionLineOnCurve hsb(float h, float s, float b){ super.hsb(h,s,b); return this; }
    public ITensionLineOnCurve hsb(double h, double s, double b){ super.hsb(h,s,b); return this; }
    
    public ITensionLineOnCurve setColor(Color c){ super.setColor(c); return this; }
    public ITensionLineOnCurve setColor(Color c, int alpha){ super.setColor(c,alpha); return this; }
    public ITensionLineOnCurve setColor(int gray){ super.setColor(gray); return this; }
    public ITensionLineOnCurve setColor(float fgray){ super.setColor(fgray); return this; }
    public ITensionLineOnCurve setColor(double dgray){ super.setColor(dgray); return this; }
    public ITensionLineOnCurve setColor(int gray, int alpha){ super.setColor(gray,alpha); return this; }
    public ITensionLineOnCurve setColor(float fgray, float falpha){ super.setColor(fgray,falpha); return this; }
    public ITensionLineOnCurve setColor(double dgray, double dalpha){ super.setColor(dgray,dalpha); return this; }
    public ITensionLineOnCurve setColor(int r, int g, int b){ super.setColor(r,g,b); return this; }
    public ITensionLineOnCurve setColor(float fr, float fg, float fb){ super.setColor(fr,fg,fb); return this; }
    public ITensionLineOnCurve setColor(double dr, double dg, double db){ super.setColor(dr,dg,db); return this; }
    public ITensionLineOnCurve setColor(int r, int g, int b, int a){ super.setColor(r,g,b,a); return this; }
    public ITensionLineOnCurve setColor(float fr, float fg, float fb, float fa){ super.setColor(fr,fg,fb,fa); return this; }
    public ITensionLineOnCurve setColor(double dr, double dg, double db, double da){ super.setColor(dr,dg,db,da); return this; }
    public ITensionLineOnCurve setHSBColor(float h, float s, float b, float a){ super.setHSBColor(h,s,b,a); return this; }
    public ITensionLineOnCurve setHSBColor(double h, double s, double b, double a){ super.setHSBColor(h,s,b,a); return this; }
    public ITensionLineOnCurve setHSBColor(float h, float s, float b){ super.setHSBColor(h,s,b); return this; }
    public ITensionLineOnCurve setHSBColor(double h, double s, double b){ super.setHSBColor(h,s,b); return this; }
    
    public ITensionLineOnCurve weight(double w){ super.weight(w); return this; }
    public ITensionLineOnCurve weight(float w){ super.weight(w); return this; }
    
    
}

