/*

    Copyright (C) 2010-2013  DAHMEN, Manuel, Daniel

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

 */
package be.ibiiztera.md.pmatrix.pushmatrix.base;

import java.awt.Color;

import be.ibiiztera.emptycanvas.Application;
import be.ibiiztera.md.pmatrix.pushmatrix.MODObjet;
import be.ibiiztera.md.pmatrix.pushmatrix.Matrix33;
import be.ibiiztera.md.pmatrix.pushmatrix.Point3D;
import be.ibiiztera.md.pmatrix.pushmatrix.Position;
import be.ibiiztera.md.pmatrix.pushmatrix.Representable;
import be.ibiiztera.md.pmatrix.pushmatrix.TColor;
import be.ibiiztera.md.pmatrix.pushmatrix.TRI;
import be.ibiiztera.md.pmatrix.pushmatrix.TRIObject;
import be.ibiiztera.md.pmatrix.pushmatrix.generator.TRIGenerable;

/**
 * 
 * @author Manuel DAHMEN
 */
public class ConeDeRevolution implements Representable, TRIGenerable{
	protected int nRadial = 100;
	protected int nHauteur = 100;
	protected TRIObject tris;
	protected double hauteur;
	protected double R1;
	protected double R2;
	protected Matrix33 mrot;
	protected TColor texturec = new TColor(Color.black);
	protected Position position;
	
	
	{

		Object o;
		Integer nrTmp;
		try {
			o = Application.getConfigurator().getValue("ConeDeRevolution.nRadial");
			nrTmp = Integer.parseInt((String) o);
			nRadial = nrTmp;
		} catch (Exception ex) {
			System.err.println("Configuration exception");
		}
		try {
			o = Application.getConfigurator().getValue("ConeDeRevolution.nHauteur");
			nrTmp = Integer.parseInt((String) o);
			nHauteur = nrTmp;
		} catch (Exception ex) {
			System.err.println("Configuration exception");
		}
	}
	
	
	
	
	private static final long serialVersionUID = -7978727366546561024L;

	
	public ConeDeRevolution() {
	}
	
	public ConeDeRevolution(double hauteur,double r1, double r2) {
		super();
		this.hauteur = hauteur;
		R1 = r1;
		R2 = r2;
	}

	public ConeDeRevolution(int nRadial, int nHauteur, double hauteur,
			double r1, double r2, Matrix33 mrot, Position position) {
		super();
		this.nRadial = nRadial;
		this.nHauteur = nHauteur;
		this.hauteur = hauteur;
		R1 = r1;
		R2 = r2;
		this.mrot = mrot;
		this.position = position;
	}

	public ConeDeRevolution(double hauteur, double r1, double r2, Matrix33 mrot,
			Position position) {
		super();
		this.hauteur = hauteur;
		R1 = r1;
		R2 = r2;
		this.mrot = mrot;
		this.position = position;
	}

	@Override
	public String id() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setId(String id) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Representable place(MODObjet aThis) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void texture(TColor tc) {
		this.texturec = tc;
	}

	@Override
	public boolean supporteTexture() {
		return false;
	}
	public double r(double h)
	{
		return R1+(R2-R1)*h;
	}
		
		
	public Point3D formulaPoint(double h, double a)
	{
		Point3D p1 = new Point3D(Math.cos(a), Math.sin(a), 0);
		Point3D p2 = p1.mult(r(h));
		Point3D p3 = p2.plus(Point3D.Z.mult(h));
		Point3D pr = mrot==null ? p3 : mrot.mult(p3);
		Point3D pp = pr.plus(position==null ? Point3D.O0 : position.calculer(Point3D.O0));
		return pp;
	}
		
		
	public void generer()
	{
		tris = new TRIObject();
		
		for(int h=0; h<nHauteur; h++)
			for(int t=0; t<nRadial; t++)
			{
				TRI tri1, tri2 ;
				tri1 = new TRI(formula(h,t), formula(h+1,t), formula(h+1,t+1));
				tri2 = new TRI(formula(h,t), formula(h,t+1), formula(h+1,t+1));
				
				tri1.setCouleur(texturec.getCouleur());
				tri2.setCouleur(texturec.getCouleur());
				// TODO ajouter cooler et texture
				tris.add(tri1);
				tris.add(tri2);
				
			}
			
	}

	private Point3D formula(int h, int a) {
		return formulaPoint(1d*h/nHauteur, 1d*a/nRadial);
	}

	@Override
	public TRIObject generate() {
		generer();
		return tris;
	}

	@Override
	public void place(Position p) {
		this.position = p;
	}
}

