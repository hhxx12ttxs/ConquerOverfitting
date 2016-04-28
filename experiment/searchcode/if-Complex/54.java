/*
Sketch Elements: Chemistry molecular diagram drawing tool.

(c) 2005 Dr. Alex M. Clark

Released as GNUware, under the Gnu Public License (GPL)

See www.gnu.org for details.
 */
package net.sf.sketchel;

import java.io.*;
import java.util.*;
import org.ingatan.io.IOManager;

// For obtaining the template list.
public class Templates {

    ArrayList<Molecule> templ = new ArrayList<Molecule>();
    ArrayList<String> names = new ArrayList<String>(); // reference filenames in .jar
    ArrayList<String> filenames = new ArrayList<String>(); // reference filenames in chem custom template folder

    public Templates(Class cls) {
        // read the list of molecules from the directory file, then create each one of them
        names.add("anthracene.el");
        names.add("benzene.el");
        names.add("cyclobutane.el");
        names.add("cycloheptane.el");
        names.add("cyclohexane_boat.el");
        names.add("cyclohexane_chair.el");
        names.add("cyclohexane.el");
        names.add("cyclooctane.el");
        names.add("cyclopentadiene.el");
        names.add("cyclopentane.el");
        names.add("cyclopentane_house.el");
        names.add("cyclopropane.el");
        names.add("dihydroindene.el");
        names.add("dioxan.el");
        names.add("fluoroscene.el");
        names.add("furan.el");
        names.add("morpholine.el");
        names.add("naphthalene.el");
        names.add("phenanthrene.el");
        names.add("piperidine.el");
        names.add("pyran.el");
        names.add("pyridine.el");
        names.add("pyrrolidine.el");
        names.add("tetrahydrofuran.el");
        names.add("thiofuran.el");
        names.add("arrow.el");
        names.add("half_arrow.el");
        names.add("half_arrow2.el");
        names.add("equilibrium.el");
        names.add("carbonyl.el");

        //check if there are any custom templates in the sketchel custom template director (default: .ingatan/chem_templates)
        File templatePath = new File(IOManager.getChemTemplatesPath());
        //if the collections path exists
        if (templatePath.exists()) {
            //list the files
            File[] templates = templatePath.listFiles();
            //add all non-directories as templates if they end with the .el extension
            for (int i = 0; i < templates.length; i++) {
                if ((templates[i].isFile()) && (templates[i].getName().endsWith(".el"))) {
                    filenames.add(templates[i].getAbsolutePath());
                }
            }
        } else {
            System.out.println("Custom template path does not exist.");
        }

        //load the bundled templates from the names array.
        try {
            InputStream istr;
            for (int n = 0; n < names.size(); n++) {
                istr = cls.getResourceAsStream("templ/" + names.get(n));
                Molecule mol = MoleculeReader.readNative(istr);
                templ.add(mol);
                istr.close();
            }
        } catch (IOException e) {
            System.out.println("Failed to obtain a template from ingatan.jar.\n" + e.toString());
            return;
        }

        //load the custom templates from the custom template directory
        try {
            InputStream istr;
            for (int n = 0; n < filenames.size(); n++) {
                istr = new FileInputStream(filenames.get(n));
                Molecule mol = MoleculeReader.readNative(istr);
                templ.add(mol);
                istr.close();
            }
        } catch (IOException e) {
            System.out.println("Failed to obtain a template from custom templates folder.\n" + e.toString());
            return;
        }

        // sort the molecules by an index of "complexity" (smaller molecules first, carbon-only favoured)
        int[] complex = new int[templ.size()];
        for (int n = 0; n < templ.size(); n++) {
            Molecule mol = templ.get(n);
            complex[n] = mol.numAtoms() * 100;
            boolean nonCH = false;
            for (int i = 1; i <= mol.numAtoms(); i++) {
                if (mol.atomElement(i).compareTo("C") != 0 && mol.atomElement(i).compareTo("H") != 0) {
                    nonCH = true;
                }
            }
            if (!nonCH) {
                complex[n] -= 1000;
            }
            for (int i = 1; i <= mol.numBonds(); i++) {
                complex[n] = complex[n] + mol.bondOrder(i);
            }
        }

        int p = 0;
        while (p < templ.size() - 1) {
            if (complex[p] > complex[p + 1]) {
                int i = complex[p];
                complex[p] = complex[p + 1];
                complex[p + 1] = i;
                Molecule mol = templ.get(p);
                templ.set(p, templ.get(p + 1));
                templ.set(p + 1, mol);
                String str = names.get(p);
                names.set(p, names.get(p + 1));
                names.set(p + 1, str);
                if (p > 0) {
                    p--;
                }
            } else {
                p++;
            }
        }
    }

    public int numTemplates() {
        return templ.size();
    }

    public Molecule getTemplate(int N) {
        return templ.get(N);
    }

    public String getName(int N) {
        return names.get(N);
    }

    public void addTemplate(Molecule Mol) {
        templ.add(Mol);
        names.add(null);
    }
}





