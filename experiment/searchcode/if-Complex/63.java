<<<<<<< HEAD
/*
 *  $Id: OM2LaTeXVisitor.java 289 2008-11-20 17:50:34Z martinlaz $
 *
 *  Author:
 *     Armin Buch
 *     
 *  This file is part of the Gralej system
 *     http://code.google.com/p/gralej/
 *
 *  Gralej is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Gralej is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package gralej.parsers;

import java.util.Set;
import java.util.TreeSet;

import gralej.om.*;


// TODO Make output dependent on the visibility of nodes (if desired).
// Right now, the data is modelled and not what is seen on screen


public class OM2LaTeXVisitor extends AbstractVisitor {

    StringBuffer _out;
    Set<Integer> _reentrancies = new TreeSet<Integer>();

    public String output(IVisitable root) {
        _out = new StringBuffer();
        root.accept(this);
        return _out.toString();
    }

    private String texify(String in) {
        String out = in;
        out = out.replace("_", "\\_"); // more?
        return out;
    }

    public void visit(IVisitable visitable) {
        throw new RuntimeException("unknown visitable: " + visitable);
    }

    public void visit(IEntity entity) {
        throw new RuntimeException("unknown entity: " + entity);
    }

    public void visit(IFeatureValuePair featVal) {
//        if (featVal.isHidden())
//            return;
        _out.append(texify(featVal.feature()) + " & ");
        featVal.value().accept(this);
        _out.append("\\\\\n");
    }

    public void visit(IList ls) {
        _out.append("\\<");
        for (IEntity e : ls.elements())
            e.accept(this);
        _out.append("\\>\\\\\n");
    }

    public void visit(ITag tag) {
        
         if (_reentrancies.add(tag.number())) { _out.append("\\@{" +
         tag.number() + "}"); tag.target().accept(this); } else {
         _out.append("\\@{" + tag.number() + "}"); }
         
//        _out.append("\\@{" + tag.number() + "}");
//        if (tag.isExpanded()) {
//            tag.target().accept(this);
//        }
    }

    public void visit(IAny any) {
        _out.append(any.value()); // TODO vacuous
    }

    public void visit(ITypedFeatureStructure tfs) {
        boolean complex = tfs.featureValuePairs().iterator().hasNext();
        if (complex)
            _out.append("\\[\\tp{");
        _out.append(texify(tfs.typeName()));
        if (complex)
            _out.append("}");
        _out.append("\\\\\n");

        for (IFeatureValuePair featVal : tfs.featureValuePairs())
            featVal.accept(this);

        if (complex)
            _out.append("\\]\n");
    }

    public void visit(ITree tree) {
        if (!tree.isLeaf())
            _out.append("\\begin{bundle}{");
        _out.append("\\begin{Avm}{" + tree.label() + "}\n");
        tree.content().accept(this);
        _out.append("\\end{Avm}\n");
        if (!tree.isLeaf())
            _out.append("}\n");
        for (ITree child : tree.children()) {
            _out.append("\\chunk{");
            if (child instanceof ITree) {
                child.accept(this);
                
            } else if (child instanceof ITypedFeatureStructure) {
                _out.append("\\begin{Avm}\n");
                child.accept(this);
                _out.append("\\end{Avm}\n");
            }
            else {
            //TODO: add case to IList
            }
            _out.append("}\n");
        }
        if (!tree.isLeaf())
            _out.append("\\end{bundle}\n");

    }

}
=======
/*
 * Naam:            Steven Raaijmakers
 * Studentnummer:   10804242
 * Studie:          Bachelor Informatica
 * Omschrijving: 	Programma kan wiskundige functies met complexe getallen uitvoeren
*/

public class ComplexGetal implements ComplexGetalInterface {
    double a, b;

    ComplexGetal(double a, double b){
        this.a = a;
        this.b = b;
    }

    ComplexGetal(int a){
    }

    ComplexGetal(){
    }

    ComplexGetal(ComplexGetal Original){
    }

    public String toString(){
        /* Een "+ -"-situatie omzetten naar een enkele "-" */
        if(b < 0){
            return "" + a + " - " + b*-1 + "i";
        }
        else {
            return "" + a + " + " + b + "i";
        }
    }

    public ComplexGetal telop(ComplexGetal cg){
        double c = cg.a;
        double d = cg.b;

        /* Optellen d.m.v: (a + c) + (b + d)i */
        double nieuwereeel = a + c;
        double nieuweimaginair = b + d;

        ComplexGetal complex_getal = new ComplexGetal(nieuwereeel, nieuweimaginair);
        return complex_getal;
    }

    public ComplexGetal trekaf(ComplexGetal cg){
        double c = cg.a;
        double d = cg.b;

        /* Aftrekken d.m.v: (a - c) + (b - d)i */

        double nieuw_a = a - c;
        double nieuw_b = b - d;

        /* Wanneer aftrekken met negatief getal; dit optellen. */
        if(c < 0){
            nieuw_a = a + c *-1;
        }
        else if (d < 0){
            nieuw_b = b + d *-1;
        }

        ComplexGetal complex_getal = new ComplexGetal(nieuw_a, nieuw_b);
        return complex_getal;
    }

    public ComplexGetal vermenigvuldig(ComplexGetal cg){
        double c = cg.a;
        double d = cg.b;

        /* Complexgetal vermenigvuldigen d.m.v. formule: (a*c-b*d) + (a*d+b*c)i */
        double nieuw_a = a * c - b * d;
        double nieuw_b = a * d + b * c;

        ComplexGetal complex_getal = new ComplexGetal(nieuw_a, nieuw_b);

        return complex_getal;
    }

    public ComplexGetal deel(ComplexGetal cg){
        double c = cg.a;
        double d = cg.b;

        /* Complexgetal delen d.m.v: (a * c + b * d) / (c² + d²) */
        double nieuw_a_1 = a * c + b * d;
        double nieuw_a_2 = (c * c) + (d * d);
        double nieuw_a = nieuw_a_1 / nieuw_a_2;

        /* Complexgetal delen d.m.v: (b * c - a * d) / (c² + d²) */
        double nieuw_b_1 = (b * c) - (a * d);
        double nieuw_b_2 = (c * c) + (d * d);
        double nieuw_b = nieuw_b_1 / nieuw_b_2;

        ComplexGetal complex_getal = new ComplexGetal(nieuw_a, nieuw_b);

        return complex_getal;
    }

    public ComplexGetal omgekeerd(){
        /* Omgekeerde d.m.v: (a / (a² + b²)) + (-b / (a² + b²)) */

        double nieuw_a = a / (a*a) + (b*b);
        double nieuw_b = -b / (a*a) + (b*b);

        ComplexGetal complex_getal = new ComplexGetal(nieuw_a, nieuw_b);

        return complex_getal;
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

