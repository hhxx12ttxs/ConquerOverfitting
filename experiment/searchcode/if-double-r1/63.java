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

import be.ibiiztera.md.pmatrix.pushmatrix.MODObjet;
import be.ibiiztera.md.pmatrix.pushmatrix.Matrix33;
import be.ibiiztera.md.pmatrix.pushmatrix.Point3D;
import be.ibiiztera.md.pmatrix.pushmatrix.Position;
import be.ibiiztera.md.pmatrix.pushmatrix.Representable;
import be.ibiiztera.md.pmatrix.pushmatrix.TColor;
import be.ibiiztera.md.pmatrix.pushmatrix.generator.TRIObjetGenerateurAbstract;

/**
 *
 * @author Manuel DAHMEN
 */

public class CylindreDeRevolution extends ConeDeRevolution
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8176770203273172014L;

	public CylindreDeRevolution() {
	}
	
	public CylindreDeRevolution(double hauteur,double r1) {
		super();
		this.hauteur = hauteur;
		R1 = r1;
		R2 = r1;
	}

	public CylindreDeRevolution(int nRadial, int nHauteur, double hauteur,
			double r1, Matrix33 mrot, Position position) {
		super();
		this.nRadial = nRadial;
		this.nHauteur = nHauteur;
		this.hauteur = hauteur;
		R1 = r1;
		R2 = r1;
		this.mrot = mrot;
		this.position = position;
	}

	public CylindreDeRevolution(double hauteur, double r1,  Matrix33 mrot,
			Position position) {
		super();
		this.hauteur = hauteur;
		R1 = r1;
		R2 = r1;
		this.mrot = mrot;
		this.position = position;
	}

}

