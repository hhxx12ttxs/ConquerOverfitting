// $Id$

package gov.nih.ncgc.descriptor;

import java.util.Map;
import java.util.ArrayList;

import chemaxon.struc.MolBond;
import chemaxon.struc.MolAtom;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;
import chemaxon.formats.MolImporter;

import gov.nih.ncgc.util.MolPath;
import gov.nih.ncgc.util.ChemUtil;

public class SparseDescriptorFactory {
    private SparseDescriptorFactory () {}

    public static double shortestPathsKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = 
	    new MolPath.ShortestPaths(m1).getSparseVector();
	Map<String, Integer> v2 = 
	    new MolPath.ShortestPaths(m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double apShortestPathsKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = 
	    new APShortestPaths(m1).getSparseVector();
	Map<String, Integer> v2 = 
	    new APShortestPaths(m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double allPathsKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = 
	    new MolPath.AllPaths(m1).getSparseVector();
	Map<String, Integer> v2 = 
	    new MolPath.AllPaths(m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double pattyShortestPathsKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = 
	    new PattyShortestPaths (m1).getSparseVector();
	Map<String, Integer> v2 = 
	    new PattyShortestPaths (m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double pattyAllPathsKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = new PattyAllPaths (m1).getSparseVector();
	Map<String, Integer> v2 = new PattyAllPaths (m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double ttKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = new TopologicalTorsion(m1).getSparseVector();
	Map<String, Integer> v2 = new TopologicalTorsion(m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double pattyTTKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = new PattyTopologicalTorsion(m1).getSparseVector();
	Map<String, Integer> v2 = new PattyTopologicalTorsion(m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double apKernelRobust (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = new AtomPair(m1).getSparseVector();
	Map<String, Integer> v2 = new AtomPair(m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double apKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = new AtomPair(m1).getSparseVector();
	Map<String, Integer> v2 = new AtomPair(m2).getSparseVector();
	return SparseVectorMetricFactory.sim(v1,v2);
    }

    public static double pattyAPKernel (Molecule m1, Molecule m2) {
	Map<String, Integer> v1 = new PattyAtomPair(m1).getSparseVector();
	Map<String, Integer> v2 = new PattyAtomPair(m2).getSparseVector();
	return SparseVectorMetricFactory.robust(v1,v2);
    }

    public static double tanimoto (Molecule m1, Molecule m2) {
	MolHandler mh1 = new MolHandler (m1);
	MolHandler mh2 = new MolHandler (m2);
	byte[] fp1 = mh1.generateFingerprintInBytes(16, 2, 6);
	byte[] fp2 = mh2.generateFingerprintInBytes(16, 2, 6);
	int ab = 0, a = 0, b = 0;
	for (int i = 0; i < fp1.length; ++i) {
	    ab += ChemUtil.countBits((byte)(fp1[i] & fp2[i]));
	    a += ChemUtil.countBits(fp1[i]);
	    b += ChemUtil.countBits(fp2[i]);
	}
	return (double)ab/(a+b-ab);
    }

    public static void main (String[] argv) throws Exception {
	if (argv.length == 0) {
	    System.err.println("Usage: SparseDescriptorFactory FILES...");
	    System.exit(1);
	}

	ArrayList<Molecule> db = new ArrayList<Molecule>();
	for (String file : argv) {
	    MolImporter mimp = new MolImporter (file);
	    for (Molecule mol; (mol = mimp.read()) != null; ) {
		mol.aromatize();
		mol.calcHybridization();
		for (Molecule m : db) {
		    System.out.println(m.getName() + " "+ mol.getName() 
				       + " " + allPathsKernel (m, mol)
				       + " " + shortestPathsKernel (m, mol)
				       + " " + apShortestPathsKernel (m, mol)
				       /*
				       + " " + pattyAllPathsKernel(m, mol)
				       + " " + pattyShortestPathsKernel(m, mol)
				       */
				       + " " + apKernel (m, mol)
				       + " " + apKernelRobust (m, mol)
				       //+ " " + pattyAPKernel (m, mol)
				       + " " + ttKernel (m, mol)
				       //+ " " + pattyTTKernel (m, mol)
				       + " " + tanimoto (m, mol));
		}
		db.add(mol);
	    }
	    mimp.close();
	}
    }
}

