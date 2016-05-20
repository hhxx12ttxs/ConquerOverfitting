// $Id: PCFP.java 2278 2008-05-29 22:27:45Z nguyenda $
// a faster version of PubChemFp

package gov.nih.ncgc.descriptor;

import java.util.Vector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.BitSet;

import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import chemaxon.util.MolHandler;
import chemaxon.formats.MolImporter;
import chemaxon.sss.search.MolSearch;

public class PCFP {
    public static final int FP_SIZE = 881;

    private static final int _FPINTS = 16;
    private static final int _FPPATH = 7;

    private Molecule mol;
    private byte[] fp;
    private int [] counts;
    private int [][]sssr;
    private MolAtom[] atoms;
    private MolBond[] bonds;
    private int[] subfp;

    // adjacency matrix indexed by the atomic number
    private BitSet[][] adjmat;

    public PCFP () {
    }

    public void setMolecule (Molecule mol) {
	this.mol = (Molecule)mol.clone();
	this.mol.hydrogenize(true);
	this.mol.aromatize(Molecule.AROM_BASIC);

	MolHandler mh = new MolHandler (mol);
	subfp = mh.generateFingerprintInInts(_FPINTS, _FPPATH, 1);

	fp = new byte[(FP_SIZE+7)>>3];
	counts = new int[128];
	adjmat = new BitSet[128][128];

	atoms = this.mol.getAtomArray();
	for (int i = 0; i < atoms.length; ++i) {
	    ++counts[atoms[i].getAtno()];
	}
	sssr = this.mol.getSSSR();

	bonds = this.mol.getBondArray();
	for (int i = 0; i < bonds.length; ++i) {
	    MolBond b = bonds[i];
	    int a1 = b.getAtom1().getAtno();
	    int a2 = b.getAtom2().getAtno();
	    BitSet bs = adjmat[a1][a2];
	    if (bs == null) {
		adjmat[a2][a1] = adjmat[a1][a2] 
		    = bs = new BitSet (bonds.length);
	    }
	    bs.set(i);
	}
    }
    public Molecule getMolecule () { return mol; }

    public void run () {
	doSection1 ();
	doSection2 ();
	doSection3 ();
	doSections45 ();
	doSections67 ();
    }

    public byte[] getFP () { return fp; }
    public boolean isBitOn (int bit) {
	return (fp[bit>>3] & MASK[bit%8]) != 0;
    }
    public BitSet getFPSet () { 
	BitSet bs = new BitSet (FP_SIZE);
	getFPSet (bs);
	return bs;
    }
    public void getFPSet (BitSet bs) { 
	for (int i = 0; i < FP_SIZE; ++i) {
	    bs.set(i, (fp[i>>3] & MASK[i%8]) != 0);
	}
    }

    // the first four bytes contains the length of the fingerprint
    public String encode () {
	byte[] pack = new byte[4 + fp.length];

	pack[0] = (byte)((FP_SIZE & 0xffffffff) >> 24);
	pack[1] = (byte)((FP_SIZE & 0x00ffffff) >> 16);
	pack[2] = (byte)((FP_SIZE & 0x0000ffff) >>  8);
	pack[3] = (byte)(FP_SIZE & 0x000000ff);
	for (int i = 0; i < fp.length; ++i) {
	    pack[i+4] = fp[i];
	}
	return base64Encode (pack);
    }

    public static byte[] decode (String enc) {
	byte[] fp = base64Decode (enc);
	if (fp.length < 4) {
	    throw new IllegalArgumentException 
		("Input is not a proper PubChem base64 encoded fingerprint");
	}

	int len = (fp[0] << 24) | (fp[1] << 16)
	    | (fp[2] << 8) | (fp[3] & 0xff);
	if (len != FP_SIZE) {
	    throw new IllegalArgumentException 
		("Input is not a proper PubChem base64 encoded fingerprint");
	}

	byte[] pc = new byte[fp.length-4];
	for (int i = 0; i < pc.length; ++i) {
	    pc[i] = fp[i+4];
	}
	return pc;
    }


    public static void main (String[] argv) throws Exception {
	if (argv.length == 0) {
	    System.out.println("usage: PCFP FILES...");
	    System.exit(1);
	}
	
	PCFP pcfp = new PCFP ();
	BitSet bs = new BitSet (PCFP.FP_SIZE);

	for (int i = 0; i < argv.length; ++i) {
	    MolImporter molimp = new MolImporter (argv[i]);
	    for (Molecule mol = new Molecule (); molimp.read(mol); ) {
		pcfp.setMolecule(mol);
		pcfp.run();
		pcfp.getFPSet(bs);
		System.out.println(mol.getName() + " " + bs);
	    }
	    molimp.close();
	}
    }


    private static String BASE64_LUT = 
	"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    // based on NCBI C implementation
    public static String base64Encode (byte[] data) {
	char c64[] = new char[data.length*4/3+5];
	for (int i = 0, k = 0; i < data.length; i += 3, k += 4) {
	    c64[k+0] = (char)(data[i] >> 2);
	    c64[k+1] = (char)((data[i] & 0x03) << 4);
	    c64[k+2] = c64[k+3] = 64;
	    if ((i + i) < data.length) {
		c64[k+1] |= data[i+1] >> 4;
		c64[k+2] = (char)((data[i+1] & 0x0f) << 2);
	    }
	    if ((i + 2) < data.length) {
		c64[k+2] |= data[i+2] >> 6;
		c64[k+3] = (char)(data[i+2] & 0x3f);
	    }
	    for (int j = 0; j < 4; ++j) {
		c64[k+j] = BASE64_LUT.charAt(c64[k+j]);
	    }
	}
	return new String (c64);
    }

    // based on NCBI C implementation
    public static byte[] base64Decode (String data) {
	int len = data.length();

	byte[] b64 = new byte[len*3/4];
	byte[] buf = new byte[4];
	boolean done = false;

	for (int i = 0, j, k = 0; i < len && !done;) {
	    buf[0] = buf[1] = buf[2] = buf[3] = 0;
	    for (j = 0; j < 4 && i < len; ++j) {
		char c = data.charAt(i);
		if (c >= 'A' && c <= 'Z') {
		    c -= 'A';
		}
		else if (c >= 'a' && c <= 'z') {
		    c = (char)(c - 'a' + 26);
		}
		else if (c >= '0' && c <= '9') {
		    c = (char)(c - '0' + 52);
		}
		else if (c == '+') {
		    c = 62;
		}
		else if (c == '/') {
		    c = 63;
		}
		else if (c == '=' || c == '-') {
		    done = true;
		    break;
		}
		else {
		    ++i;
		    --j;
		    continue;
		}
		buf[j] = (byte)c;
		++i;
	    }

	    if (k < b64.length && j >= 1) {
		b64[k++] = (byte)((buf[0] << 2) | ((buf[1] & 0x30) >> 4));
	    }
	    if (k < b64.length && j >= 3) {
		b64[k++] = (byte)(((buf[1] & 0x0f) << 4) 
				  | ((buf[2] & 0x3c) >> 2));
	    }
	    if (k < b64.length && j >= 4) {
		b64[k++] = (byte)(((buf[2] & 0x03) << 6)| (buf[3] & 0x3f));
	    }
	}
	return b64;
    }

    private void doSection1 () {
	int b;
	b = 0; if (getCount("H") >= 4 ) fp[b>>3] |= MASK[b%8];
 	b = 1; if (getCount("H") >= 8) fp[b>>3] |= MASK[b%8];
	b = 2; if (getCount("H") >= 16) fp[b>>3] |= MASK[b%8];
	b = 3; if (getCount("H") >= 32) fp[b>>3] |= MASK[b%8];
	b = 4; if (getCount("Li") >= 1) fp[b>>3] |= MASK[b%8];
	b = 5; if (getCount("Li") >= 2) fp[b>>3] |= MASK[b%8];
	b = 6; if (getCount("B") >= 1) fp[b>>3] |= MASK[b%8];
	b = 7; if (getCount("B") >= 2) fp[b>>3] |= MASK[b%8];
	b = 8; if (getCount("B") >= 4) fp[b>>3] |= MASK[b%8];
	b = 9; if (getCount("C") >= 2) fp[b>>3] |= MASK[b%8];
	b = 10; if (getCount("C") >= 4) fp[b>>3] |= MASK[b%8];
	b = 11; if (getCount("C") >= 8) fp[b>>3] |= MASK[b%8];
	b = 12; if (getCount("C") >= 16) fp[b>>3] |= MASK[b%8];
	b = 13; if (getCount("C") >= 32) fp[b>>3] |= MASK[b%8];
	b = 14; if (getCount("N") >= 1) fp[b>>3] |= MASK[b%8];
	b = 15; if (getCount("N") >= 2) fp[b>>3] |= MASK[b%8];
	b = 16; if (getCount("N") >= 4) fp[b>>3] |= MASK[b%8];
	b = 17; if (getCount("N") >= 8) fp[b>>3] |= MASK[b%8];
	b = 18; if (getCount("O") >= 1) fp[b>>3] |= MASK[b%8];
	b = 19; if (getCount("O") >= 2) fp[b>>3] |= MASK[b%8];
	b = 20; if (getCount("O") >= 4) fp[b>>3] |= MASK[b%8];
	b = 21; if (getCount("O") >= 8) fp[b>>3] |= MASK[b%8];
	b = 22; if (getCount("O") >= 16) fp[b>>3] |= MASK[b%8];
	b = 23; if (getCount("F") >= 1) fp[b>>3] |= MASK[b%8];
	b = 24; if (getCount("F") >= 2) fp[b>>3] |= MASK[b%8];
	b = 25; if (getCount("F") >= 4) fp[b>>3] |= MASK[b%8];
	b = 26; if (getCount("Na") >= 1) fp[b>>3] |= MASK[b%8];
	b = 27; if (getCount("Na") >= 2) fp[b>>3] |= MASK[b%8];
	b = 28; if (getCount("Si") >= 1) fp[b>>3] |= MASK[b%8];
	b = 29; if (getCount("Si") >= 2) fp[b>>3] |= MASK[b%8];
	b = 30; if (getCount("P") >= 1) fp[b>>3] |= MASK[b%8];
	b = 31; if (getCount("P") >= 2) fp[b>>3] |= MASK[b%8];
	b = 32; if (getCount("P") >= 4) fp[b>>3] |= MASK[b%8];
	b = 33; if (getCount("S") >= 1) fp[b>>3] |= MASK[b%8];
	b = 34; if (getCount("S") >= 2) fp[b>>3] |= MASK[b%8];
	b = 35; if (getCount("S") >= 4) fp[b>>3] |= MASK[b%8];
	b = 36; if (getCount("S") >= 8) fp[b>>3] |= MASK[b%8];
	b = 37; if (getCount("Cl") >= 1) fp[b>>3] |= MASK[b%8];
	b = 38; if (getCount("Cl") >= 2) fp[b>>3] |= MASK[b%8];
	b = 39; if (getCount("Cl") >= 4) fp[b>>3] |= MASK[b%8];
	b = 40; if (getCount("Cl") >= 8) fp[b>>3] |= MASK[b%8];
	b = 41; if (getCount("K") >= 1) fp[b>>3] |= MASK[b%8];
	b = 42; if (getCount("K") >= 2) fp[b>>3] |= MASK[b%8];
	b = 43; if (getCount("Br") >= 1) fp[b>>3] |= MASK[b%8];
	b = 44; if (getCount("Br") >= 2) fp[b>>3] |= MASK[b%8];
	b = 45; if (getCount("Br") >= 4) fp[b>>3] |= MASK[b%8];
	b = 46; if (getCount("I") >= 1) fp[b>>3] |= MASK[b%8];
	b = 47; if (getCount("I") >= 2) fp[b>>3] |= MASK[b%8];
	b = 48; if (getCount("I") >= 4) fp[b>>3] |= MASK[b%8];
	b = 49; if (getCount("Be")	>= 1) fp[b>>3] |= MASK[b%8];
	b = 50; if (getCount("Mg") >= 1) fp[b>>3] |= MASK[b%8];
	b = 51; if (getCount("Al") >= 1) fp[b>>3] |= MASK[b%8];
	b = 52; if (getCount("Ca") >= 1) fp[b>>3] |= MASK[b%8];
	b = 53; if (getCount("Sc") >= 1) fp[b>>3] |= MASK[b%8];
	b = 54; if (getCount("Ti") >= 1) fp[b>>3] |= MASK[b%8];
	b = 55; if (getCount("V") >= 1) fp[b>>3] |= MASK[b%8];
	b = 56; if (getCount("Cr") >= 1) fp[b>>3] |= MASK[b%8];
	b = 57; if (getCount("Mn") >= 1) fp[b>>3] |= MASK[b%8];
	b = 58; if (getCount("Fe") >= 1) fp[b>>3] |= MASK[b%8];
	b = 59; if (getCount("Co") >= 1) fp[b>>3] |= MASK[b%8];
	b = 60; if (getCount("Ni") >= 1) fp[b>>3] |= MASK[b%8];
	b = 61; if (getCount("Cu") >= 1) fp[b>>3] |= MASK[b%8];
	b = 62; if (getCount("Zn") >= 1) fp[b>>3] |= MASK[b%8];
	b = 63; if (getCount("Ga") >= 1) fp[b>>3] |= MASK[b%8];
	b = 64; if (getCount("Ge") >= 1) fp[b>>3] |= MASK[b%8];
	b = 65; if (getCount("As") >= 1) fp[b>>3] |= MASK[b%8];
	b = 66; if (getCount("Se") >= 1) fp[b>>3] |= MASK[b%8];
	b = 67; if (getCount("Kr") >= 1) fp[b>>3] |= MASK[b%8];
	b = 68; if (getCount("Rb") >= 1) fp[b>>3] |= MASK[b%8];
	b = 69; if (getCount("Sr") >= 1) fp[b>>3] |= MASK[b%8];
	b = 70; if (getCount("Y") >= 1) fp[b>>3] |= MASK[b%8];
	b = 71; if (getCount("Zr") >= 1) fp[b>>3] |= MASK[b%8];
	b = 72; if (getCount("Nb") >= 1) fp[b>>3] |= MASK[b%8];
	b = 73; if (getCount("Mo") >= 1) fp[b>>3] |= MASK[b%8];
	b = 74; if (getCount("Ru") >= 1) fp[b>>3] |= MASK[b%8];
	b = 75; if (getCount("Rh") >= 1) fp[b>>3] |= MASK[b%8];
	b = 76; if (getCount("Pd") >= 1) fp[b>>3] |= MASK[b%8];
	b = 77; if (getCount("Ag") >= 1) fp[b>>3] |= MASK[b%8];
	b = 78; if (getCount("Cd") >= 1) fp[b>>3] |= MASK[b%8];
	b = 79; if (getCount("In") >= 1) fp[b>>3] |= MASK[b%8];
	b = 80; if (getCount("Sn") >= 1) fp[b>>3] |= MASK[b%8];
	b = 81; if (getCount("Sb") >= 1) fp[b>>3] |= MASK[b%8];
	b = 82; if (getCount("Te") >= 1) fp[b>>3] |= MASK[b%8];
	b = 83; if (getCount("Xe") >= 1) fp[b>>3] |= MASK[b%8];
	b = 84; if (getCount("Cs") >= 1) fp[b>>3] |= MASK[b%8];
	b = 85; if (getCount("Ba") >= 1) fp[b>>3] |= MASK[b%8];
	b = 86; if (getCount("Lu") >= 1) fp[b>>3] |= MASK[b%8];
	b = 87; if (getCount("Hf") >= 1) fp[b>>3] |= MASK[b%8];
	b = 88; if (getCount("Ta") >= 1) fp[b>>3] |= MASK[b%8];
	b = 89; if (getCount("W") >= 1) fp[b>>3] |= MASK[b%8];
	b = 90; if (getCount("Re") >= 1) fp[b>>3] |= MASK[b%8];
	b = 91; if (getCount("Os") >= 1) fp[b>>3] |= MASK[b%8];
	b = 92; if (getCount("Ir") >= 1) fp[b>>3] |= MASK[b%8];
	b = 93; if (getCount("Pt") >= 1) fp[b>>3] |= MASK[b%8];
	b = 94; if (getCount("Au") >= 1) fp[b>>3] |= MASK[b%8];
	b = 95; if (getCount("Hg") >= 1) fp[b>>3] |= MASK[b%8];
	b = 96; if (getCount("Tl") >= 1) fp[b>>3] |= MASK[b%8];
	b = 97; if (getCount("Pb") >= 1) fp[b>>3] |= MASK[b%8];
	b = 98; if (getCount("Bi") >= 1) fp[b>>3] |= MASK[b%8];
	b = 99; if (getCount("La") >= 1) fp[b>>3] |= MASK[b%8];
	b = 100; if (getCount("Ce") >= 1) fp[b>>3] |= MASK[b%8];
	b = 101; if (getCount("Pr") >= 1) fp[b>>3] |= MASK[b%8];
	b = 102; if (getCount("Nd") >= 1) fp[b>>3] |= MASK[b%8];
	b = 103; if (getCount("Pm") >= 1) fp[b>>3] |= MASK[b%8];
	b = 104; if (getCount("Sm") >= 1) fp[b>>3] |= MASK[b%8];
	b = 105; if (getCount("Eu") >= 1) fp[b>>3] |= MASK[b%8];
	b = 106; if (getCount("Gd") >= 1) fp[b>>3] |= MASK[b%8];
	b = 107; if (getCount("Tb") >= 1) fp[b>>3] |= MASK[b%8];
	b = 108; if (getCount("Dy") >= 1) fp[b>>3] |= MASK[b%8];
	b = 109; if (getCount("Ho") >= 1) fp[b>>3] |= MASK[b%8];
	b = 110; if (getCount("Er") >= 1) fp[b>>3] |= MASK[b%8];
	b = 111; if (getCount("Tm") >= 1) fp[b>>3] |= MASK[b%8];
	b = 112; if (getCount("Yb") >= 1) fp[b>>3] |= MASK[b%8];
	b = 113; if (getCount("Tc") >= 1) fp[b>>3] |= MASK[b%8];
	b = 114; if (getCount("U") >= 1) fp[b>>3] |= MASK[b%8];
    }

    private void doSection2 () {
	int b;

	b = 115; if (countAnyRing(3) >= 1) fp[b>>3] |= MASK[b%8];
	b = 116; if (countSaturatedOrAromaticCarbonOnlyRing(3) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 117; if (countSaturatedOrAromaticNitrogenContainingRing(3) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 118; if (countSaturatedOrAromaticHeteroContainingRing(3) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 119; if (countUnsaturatedCarbonOnlyRing(3) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 120; if (countUnsaturatedNitrogenContainingRing(3) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 121; if (countUnsaturatedHeteroContainingRing(3) >= 1) fp[b>>3] |= MASK[b%8]; 

	b = 122; if (countAnyRing(3) >= 2) fp[b>>3] |= MASK[b%8];
	b = 123; if (countSaturatedOrAromaticCarbonOnlyRing(3) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 124; if (countSaturatedOrAromaticNitrogenContainingRing(3) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 125; if (countSaturatedOrAromaticHeteroContainingRing(3) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 126; if (countUnsaturatedCarbonOnlyRing(3) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 127; if (countUnsaturatedNitrogenContainingRing(3) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 128; if (countUnsaturatedHeteroContainingRing(3) >= 2) fp[b>>3] |= MASK[b%8]; 

	b = 129; if (countAnyRing(4) >= 1) fp[b>>3] |= MASK[b%8];
	b = 130; if (countSaturatedOrAromaticCarbonOnlyRing(4) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 131; if (countSaturatedOrAromaticNitrogenContainingRing(4) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 132; if (countSaturatedOrAromaticHeteroContainingRing(4) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 133; if (countUnsaturatedCarbonOnlyRing(4) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 134; if (countUnsaturatedNitrogenContainingRing(4) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 135; if (countUnsaturatedHeteroContainingRing(4) >= 1) fp[b>>3] |= MASK[b%8]; 

	b = 136; if (countAnyRing(4) >= 2) fp[b>>3] |= MASK[b%8];
	b = 137; if (countSaturatedOrAromaticCarbonOnlyRing(4) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 138; if (countSaturatedOrAromaticNitrogenContainingRing(4) >= 2)  fp[b>>3] |= MASK[b%8]; 
	b = 139; if (countSaturatedOrAromaticHeteroContainingRing(4) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 140; if (countUnsaturatedCarbonOnlyRing(4) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 141; if (countUnsaturatedNitrogenContainingRing(4) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 142; if (countUnsaturatedHeteroContainingRing(4) >= 2) fp[b>>3] |= MASK[b%8]; 

	b = 143; if (countAnyRing(5) >= 1) fp[b>>3] |= MASK[b%8];
	b = 144; if (countSaturatedOrAromaticCarbonOnlyRing(5) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 145; if (countSaturatedOrAromaticNitrogenContainingRing(5) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 146; if (countSaturatedOrAromaticHeteroContainingRing(5) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 147; if (countUnsaturatedCarbonOnlyRing(5) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 148; if (countUnsaturatedNitrogenContainingRing(5) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 149; if (countUnsaturatedHeteroContainingRing(5) >= 1) fp[b>>3] |= MASK[b%8]; 

	b = 150; if (countAnyRing(5) >= 2) fp[b>>3] |= MASK[b%8];
	b = 151; if (countSaturatedOrAromaticCarbonOnlyRing(5) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 152; if (countSaturatedOrAromaticNitrogenContainingRing(5) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 153; if (countSaturatedOrAromaticHeteroContainingRing(5) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 154; if (countUnsaturatedCarbonOnlyRing(5) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 155; if (countUnsaturatedNitrogenContainingRing(5) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 156; if (countUnsaturatedHeteroContainingRing(5) >= 2) fp[b>>3] |= MASK[b%8]; 

	b = 157; if (countAnyRing(5) >= 3) fp[b>>3] |= MASK[b%8];
	b = 158; if (countSaturatedOrAromaticCarbonOnlyRing(5) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 159; if (countSaturatedOrAromaticNitrogenContainingRing(5) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 160; if (countSaturatedOrAromaticHeteroContainingRing(5) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 161; if (countUnsaturatedCarbonOnlyRing(5) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 162; if (countUnsaturatedNitrogenContainingRing(5) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 163; if (countUnsaturatedHeteroContainingRing(5) >= 3) fp[b>>3] |= MASK[b%8]; 

	b = 164; if (countAnyRing(5) >= 4) fp[b>>3] |= MASK[b%8];
	b = 165; if (countSaturatedOrAromaticCarbonOnlyRing(5) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 166; if (countSaturatedOrAromaticNitrogenContainingRing(5) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 167; if (countSaturatedOrAromaticHeteroContainingRing(5) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 168; if (countUnsaturatedCarbonOnlyRing(5) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 169; if (countUnsaturatedNitrogenContainingRing(5) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 170; if (countUnsaturatedHeteroContainingRing(5) >= 4) fp[b>>3] |= MASK[b%8]; 

	b = 171; if (countAnyRing(5) >= 5) fp[b>>3] |= MASK[b%8];
	b = 172; if (countSaturatedOrAromaticCarbonOnlyRing(5) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 173; if (countSaturatedOrAromaticNitrogenContainingRing(5) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 174; if (countSaturatedOrAromaticHeteroContainingRing(5) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 175; if (countUnsaturatedCarbonOnlyRing(5) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 176; if (countUnsaturatedNitrogenContainingRing(5) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 177; if (countUnsaturatedHeteroContainingRing(5) >= 5) fp[b>>3] |= MASK[b%8]; 

	b = 178; if (countAnyRing(6) >= 1) fp[b>>3] |= MASK[b%8];
	b = 179; if (countSaturatedOrAromaticCarbonOnlyRing(6) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 180; if (countSaturatedOrAromaticNitrogenContainingRing(6) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 181; if (countSaturatedOrAromaticHeteroContainingRing(6) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 182; if (countUnsaturatedCarbonOnlyRing(6) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 183; if (countUnsaturatedNitrogenContainingRing(6) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 184; if (countUnsaturatedHeteroContainingRing(6) >= 1) fp[b>>3] |= MASK[b%8]; 

	b = 185; if (countAnyRing(6) >= 2) fp[b>>3] |= MASK[b%8];
	b = 186; if (countSaturatedOrAromaticCarbonOnlyRing(6) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 187; if (countSaturatedOrAromaticNitrogenContainingRing(6) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 188; if (countSaturatedOrAromaticHeteroContainingRing(6) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 189; if (countUnsaturatedCarbonOnlyRing(6) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 190; if (countUnsaturatedNitrogenContainingRing(6) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 191; if (countUnsaturatedHeteroContainingRing(6) >= 2) fp[b>>3] |= MASK[b%8]; 

	b = 192; if (countAnyRing(6) >= 3) fp[b>>3] |= MASK[b%8];
	b = 193; if (countSaturatedOrAromaticCarbonOnlyRing(6) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 194; if (countSaturatedOrAromaticNitrogenContainingRing(6) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 195; if (countSaturatedOrAromaticHeteroContainingRing(6) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 196; if (countUnsaturatedCarbonOnlyRing(6) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 197; if (countUnsaturatedNitrogenContainingRing(6) >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 198; if (countUnsaturatedHeteroContainingRing(6) >= 3) fp[b>>3] |= MASK[b%8]; 

	b = 199; if (countAnyRing(6) >= 4) fp[b>>3] |= MASK[b%8];
	b = 200; if (countSaturatedOrAromaticCarbonOnlyRing(6) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 201; if (countSaturatedOrAromaticNitrogenContainingRing(6) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 202; if (countSaturatedOrAromaticHeteroContainingRing(6) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 203; if (countUnsaturatedCarbonOnlyRing(6) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 204; if (countUnsaturatedNitrogenContainingRing(6) >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 205; if (countUnsaturatedHeteroContainingRing(6) >= 4) fp[b>>3] |= MASK[b%8]; 

	b = 206; if (countAnyRing(6) >= 5) fp[b>>3] |= MASK[b%8];
	b = 207; if (countSaturatedOrAromaticCarbonOnlyRing(6) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 208; if (countSaturatedOrAromaticNitrogenContainingRing(6) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 209; if (countSaturatedOrAromaticHeteroContainingRing(6) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 210; if (countUnsaturatedCarbonOnlyRing(6) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 211; if (countUnsaturatedNitrogenContainingRing(6) >= 5) fp[b>>3] |= MASK[b%8]; 
	b = 212; if (countUnsaturatedHeteroContainingRing(6) >= 5) fp[b>>3] |= MASK[b%8]; 

	b = 213; if (countAnyRing(7) >= 1) fp[b>>3] |= MASK[b%8];
	b = 214; if (countSaturatedOrAromaticCarbonOnlyRing(7) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 215; if (countSaturatedOrAromaticNitrogenContainingRing(7) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 216; if (countSaturatedOrAromaticHeteroContainingRing(7) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 217; if (countUnsaturatedCarbonOnlyRing(7) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 218; if (countUnsaturatedNitrogenContainingRing(7) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 219; if (countUnsaturatedHeteroContainingRing(7) >= 1) fp[b>>3] |= MASK[b%8]; 

	b = 220; if (countAnyRing(7) >= 2) fp[b>>3] |= MASK[b%8];
	b = 221; if (countSaturatedOrAromaticCarbonOnlyRing(7) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 222; if (countSaturatedOrAromaticNitrogenContainingRing(7) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 223; if (countSaturatedOrAromaticHeteroContainingRing(7) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 224; if (countUnsaturatedCarbonOnlyRing(7) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 225; if (countUnsaturatedNitrogenContainingRing(7) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 226; if (countUnsaturatedHeteroContainingRing(7) >= 2) fp[b>>3] |= MASK[b%8]; 

	b = 227; if (countAnyRing(8) >= 1) fp[b>>3] |= MASK[b%8];
	b = 228; if (countSaturatedOrAromaticCarbonOnlyRing(8) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 229; if (countSaturatedOrAromaticNitrogenContainingRing(8) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 230; if (countSaturatedOrAromaticHeteroContainingRing(8) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 231; if (countUnsaturatedCarbonOnlyRing(8) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 232; if (countUnsaturatedNitrogenContainingRing(8) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 233; if (countUnsaturatedHeteroContainingRing(8) >= 1) fp[b>>3] |= MASK[b%8]; 

	b = 234; if (countAnyRing(8) >= 2) fp[b>>3] |= MASK[b%8];
	b = 235; if (countSaturatedOrAromaticCarbonOnlyRing(8) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 236; if (countSaturatedOrAromaticNitrogenContainingRing(8) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 237; if (countSaturatedOrAromaticHeteroContainingRing(8) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 238; if (countUnsaturatedCarbonOnlyRing(8) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 239; if (countUnsaturatedNitrogenContainingRing(8) >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 240; if (countUnsaturatedHeteroContainingRing(8) >= 2) fp[b>>3] |= MASK[b%8]; 

	b = 241; if (countAnyRing(9) >= 1) fp[b>>3] |= MASK[b%8];
	b = 242; if (countSaturatedOrAromaticCarbonOnlyRing(9) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 243; if (countSaturatedOrAromaticNitrogenContainingRing(9) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 244; if (countSaturatedOrAromaticHeteroContainingRing(9) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 245; if (countUnsaturatedCarbonOnlyRing(9) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 246; if (countUnsaturatedNitrogenContainingRing(9) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 247; if (countUnsaturatedHeteroContainingRing(9) >= 1) fp[b>>3] |= MASK[b%8]; 

	b = 248; if (countAnyRing(10) >= 1) fp[b>>3] |= MASK[b%8];
	b = 249; if (countSaturatedOrAromaticCarbonOnlyRing(10) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 250; if (countSaturatedOrAromaticNitrogenContainingRing(10) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 251; if (countSaturatedOrAromaticHeteroContainingRing(10) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 252; if (countUnsaturatedCarbonOnlyRing(10) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 253; if (countUnsaturatedNitrogenContainingRing(10) >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 254; if (countUnsaturatedHeteroContainingRing(10) >= 1) fp[b>>3] |= MASK[b%8]; 

	b = 255; if (countAromaticRing() >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 256; if (countHeteroAromaticRing() >= 1) fp[b>>3] |= MASK[b%8]; 
	b = 257; if (countAromaticRing() >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 258; if (countHeteroAromaticRing() >= 2) fp[b>>3] |= MASK[b%8]; 
	b = 259; if (countAromaticRing() >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 260; if (countHeteroAromaticRing() >= 3) fp[b>>3] |= MASK[b%8]; 
	b = 261; if (countAromaticRing() >= 4) fp[b>>3] |= MASK[b%8]; 
	b = 262; if (countHeteroAromaticRing() >= 4) fp[b>>3] |= MASK[b%8];
    }

    private void doSection3 () {
	for (Pair p : ATOM_PAIRS) {
	    if (counts[p.atno1] > 0 && counts[p.atno2] > 0
		&& adjmat[p.atno1][p.atno2] != null) {
		fp[p.bit>>3] |= MASK[p.bit%8];
	    }
	}
    }

    private void doSections45 () {
	BitSet visited = new BitSet ();

	for (Neighbor nb : ATOM_NEIGHBORS) {
	    for (MolAtom a : atoms) {
		int nbonds = a.getBondCount();
		if (a.getAtno() == nb.anchor && nbonds >= nb.nb.length) {
		    int matches = 0;
		    visited.clear();
		    for (int i = 0; i < nb.nb.length; ++i) {
			int type = nb.type[i];
			int atno = nb.nb[i];
			for (int j = 0; j < nbonds; ++j) {
			    if (!visited.get(j)) {
				MolBond b = a.getBond(j);
				MolAtom xa = b.getOtherAtom(a);
				if ((type == MolBond.ANY 
				     || (b.getType()==MolBond.AROMATIC
					 && (type == 1 || type == 2))
				     || type == b.getType())
				    && (xa.getAtno() == atno)) {
				    visited.set(j);
				    ++matches;
				    break;
				}
			    }
			}
		    }
		    
		    if (matches == nb.nb.length) {
			fp[nb.bit>>3] |= MASK[nb.bit%8];
			break;
		    }
		}
	    }
	}
    }


    private void doSections67 () {
	MolSearch ms = new MolSearch ();
	ms.setOption
	    (MolSearch.OPTION_VAGUE_BOND, MolSearch.VAGUE_BOND_LEVEL3);
	ms.setTarget(mol);

	for (Pattern p : PATTERNS) {
	    int overlap = 0;
	    for (int i = 0; i < _FPINTS; ++i) {
		if ((subfp[i] & p.fp[i]) == p.fp[i]) {
		    ++overlap;
		}
	    }

	    if (p.bit == 493) {
		System.out.println("pattern="+p.query.toFormat("smarts:s") + " overlap="+overlap);
	    }
	    
	    /*if (overlap == _FPINTS)*/ {
		// now do more expensive MolSearch
		ms.setQuery(p.query);
		try {
		    
		    if (ms.isMatching()) {
			fp[p.bit>>3] |= MASK[p.bit%8];		    
		    }
		}
		catch (Exception ex) { ex.printStackTrace(); }
	    }
	}
    }

    private int getCount (int atno) { return counts[atno]; }
    private int getCount (String symb) { 
	return counts[MolAtom.numOf(symb)]; 
    }
    
    private int countAnyRing (int size) {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    if (sssr[i].length == size)
		++c;
	}
	return c;
    }

    private boolean isCarbonOnlyRing (int[] atoms) {
	Molecule m = getMolecule ();
	for (int i = 0; i < atoms.length; ++i) {
	    if (m.getAtom(atoms[i]).getAtno() != 6)
		return false;
	}
	return true;
    }

    private boolean isRingSaturated (int[] atoms) {
	Molecule m = getMolecule ();
	for (int i = 0; i < atoms.length; ++i) {
	    MolAtom a = m.getAtom(atoms[i]);
	    for (int j = 0; j < a.getBondCount(); ++j) {
		if (a.getBond(j).getType() != 1)
		    return false;
	    }
	}
	return true;
    }

    private boolean isRingUnsaturated (int[] atoms) {
	return !isRingSaturated (atoms);
    }

    private int countNitrogenInRing (int[] atoms) {
	int c = 0;
	Molecule m = getMolecule ();
	for (int i = 0; i < atoms.length; ++i) {
	    if (7 == m.getAtom(atoms[i]).getAtno())
		++c;
	}
	return c;
    }

    private int countHeteroInRing (int[] atoms) {
	int c = 0;
	Molecule m = getMolecule ();
	for (int i = 0; i < atoms.length; ++i) {
	    int atno = m.getAtom(atoms[i]).getAtno();
	    if (atno != 6 && atno != 1)
		++c;
	}
	return c;
    }

    private boolean isAromaticRing (int[] atoms) {
	Molecule m = getMolecule ();

	Set partOfRing = new HashSet ();
	for (int i = 0; i < atoms.length; ++i) {
	    partOfRing.add(m.getAtom(atoms[i]));
	}

	Vector bondsInRing = new Vector();
	for (int i = 0; i < atoms.length; ++i) {
	    MolAtom a = (MolAtom)m.getAtom(atoms[i]);
	    for (int j = 0; j < a.getBondCount(); ++j) {
		MolBond b = a.getBond(j);
		if (partOfRing.contains(b.getAtom1())
		    && partOfRing.contains(b.getAtom2())) {
		    bondsInRing.add(b);
		}
	    }
	}

	for (Iterator iter = bondsInRing.iterator(); iter.hasNext(); ) {
	    MolBond b  = (MolBond)iter.next();
	    switch (b.getType()) {
	    case MolBond.AROMATIC:
	    case MolBond.SINGLE_OR_AROMATIC:
	    case MolBond.DOUBLE_OR_AROMATIC:
		break;

	    default:
		return false;
	    }
	}
	    
	return true;
    }

    private int countAromaticRing () {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    int[] ring = sssr[i];
	    if (isAromaticRing (ring))
		++c;
	}
	return c;
    }

    private int countHeteroAromaticRing () {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    int[] ring = sssr[i];
	    if (!isCarbonOnlyRing (ring) && isAromaticRing (ring))
		++c;
	}
	return c;
    }

    private int countSaturatedOrAromaticCarbonOnlyRing (int size) {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    int []ring = sssr[i];
	    if (ring.length == size
		&& isCarbonOnlyRing (ring) 
		&& (isRingSaturated (ring) || isAromaticRing (ring)))
		++c;
	}
	return c;
    }

    private int countSaturatedOrAromaticNitrogenContainingRing (int size) {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    int ring[] = sssr[i];
	    if (ring.length == size
		&& (isRingSaturated (ring) || isAromaticRing (ring))
		&& countNitrogenInRing (ring) > 0)
		++c;
	}
	return c;
    }

    private int countSaturatedOrAromaticHeteroContainingRing (int size) {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    int[] ring = sssr[i];
	    if (ring.length == size
		&& (isRingSaturated (ring) || isAromaticRing (ring))
		&& countHeteroInRing (ring) > 0)
		++c;
	}
	return c;
    }

    private int countUnsaturatedCarbonOnlyRing (int size) {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    int ring[] = sssr[i];
	    if (ring.length == size
		&& isRingUnsaturated (ring)
		&& !isAromaticRing (ring)
		&& isCarbonOnlyRing (ring))
		++c;
	}
	return c;
    }

    private int countUnsaturatedNitrogenContainingRing (int size) {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    int ring[] = sssr[i];
	    if (ring.length == size
		&& isRingUnsaturated (ring)
		&& !isAromaticRing (ring)
		&& countNitrogenInRing (ring) > 0)
		++c;
	}
	return c;
    }

    private int countUnsaturatedHeteroContainingRing (int size) {
	int c = 0;
	for (int i = 0; i < sssr.length; ++i) {
	    int[] ring = sssr[i];
	    if (ring.length == size
		&& isRingUnsaturated (ring)
		&& !isAromaticRing (ring)
		&& countHeteroInRing (ring) > 0)
		++c;
	}
	return c;
    }

    static final int MASK[] = {
	0x80,
	0x40,
	0x20,
	0x10,
	0x08,
	0x04,
	0x02,
	0x01
    };

    static class Pair {
	int bit,  atno1, atno2;
	public Pair (int bit, String elm1, String elm2) {
	    this.bit = bit;
	    atno1 = MolAtom.numOf(elm1);
	    atno2 = MolAtom.numOf(elm2);
	}

	public int getBit () { return bit; }
	public int getAtno1 () { return atno1; }
	public int getAtno2 () { return atno2; }
    }

    static Pair[] ATOM_PAIRS = {
	new Pair (263,"Li","H"),
	new Pair (264,"Li","Li"),
	new Pair (265,"Li","B"),
	new Pair (266,"Li","C"),
	new Pair (267,"Li","O"),
	new Pair (268,"Li","F"),
	new Pair (269,"Li","P"),
	new Pair (270,"Li","S"),
	new Pair (271,"Li","Cl"),
	new Pair (272,"B","H"),
	new Pair (273,"B","B"),
	new Pair (274,"B","C"),
	new Pair (275,"B","N"),
	new Pair (276,"B","O"),
	new Pair (277,"B","F"),
	new Pair (278,"B","Si"),
	new Pair (279,"B","P"),
	new Pair (280,"B","S"),
	new Pair (281,"B","Cl"),
	new Pair (282,"B","Br"),
	new Pair (283,"C","H"),
	new Pair (284,"C","C"),
	new Pair (285,"C","N"),
	new Pair (286,"C","O"),
	new Pair (287,"C","F"),
	new Pair (288,"C","Na"),
	new Pair (289,"C","Mg"),
	new Pair (290,"C","Al"),
	new Pair (291,"C","Si"),
	new Pair (292,"C","P"),
	new Pair (293,"C","S"),
	new Pair (294,"C","Cl"),
	new Pair (295,"C","As"),
	new Pair (296,"C","Se"),
	new Pair (297,"C","Br"),
	new Pair (298,"C","I"),
	new Pair (299,"N","H"),
	new Pair (300,"N","N"),
	new Pair (301,"N","O"),
	new Pair (302,"N","F"),
	new Pair (303,"N","Si"),
	new Pair (304,"N","P"),
	new Pair (305,"N","S"),
	new Pair (306,"N","Cl"),
	new Pair (307,"N","Br"),
	new Pair (308,"O","H"),
	new Pair (309,"O","O"),
	new Pair (310,"O","Mg"),
	new Pair (311,"O","Na"),
	new Pair (312,"O","Al"),
	new Pair (313,"O","Si"),
	new Pair (314,"O","P"),
	new Pair (315,"O","K"),
	new Pair (316,"F","P"),
	new Pair (317,"F","S"),
	new Pair (318,"Al","H"),
	new Pair (319,"Al","Cl"),
	new Pair (320,"Si","H"),
	new Pair (321,"Si","Si"),
	new Pair (322,"Si","Cl"),
	new Pair (323,"P","H"),
	new Pair (324,"P","P"),
	new Pair (325,"As","H"),
	new Pair (326,"As","As")
    };

    static class Neighbor {
	int bit, anchor;
	int[] nb, type;

	public Neighbor (int bit, String anchor, String... attachments) {
	    this.bit = bit;
	    this.anchor = MolAtom.numOf(anchor);

	    nb = new int[attachments.length];
	    type = new int[attachments.length];
	    for (int i = 0; i < attachments.length; ++i) {
		String order = attachments[i].substring(0, 1);
		String elm = attachments[i].substring(1);
		nb[i] = MolAtom.numOf(elm);
		if (order.equals(":")) {
		    type[i] = MolBond.AROMATIC;
		}
		else if (order.equals("~")) {
		    type[i] = MolBond.ANY;
		}
		else if (order.equals("#")) {
		    type[i] = 3;
		}
		else if (order.equals("=")) {
		    type[i] = 2;
		}
		else if (order.equals("-")) {
		    type[i] = 1;
		}
	    }
	}
    }

    static Neighbor[] ATOM_NEIGHBORS = {
	new Neighbor (327,"C","~Br","~C"),
	new Neighbor (328,"C","~Br","~C","~C"),
	new Neighbor (329,"C","~Br","~H"),
	new Neighbor (330,"C","~Br",":C"),
	new Neighbor (331,"C","~Br",":N"),
	new Neighbor (332,"C","~C","~C"),
	new Neighbor (333,"C","~C","~C","~C"),
	new Neighbor (334,"C","~C","~C","~C","~C"),
	new Neighbor (335,"C","~C","~C","~C","~H"),
	new Neighbor (336,"C","~C","~C","~C","~N"),
	new Neighbor (337,"C","~C","~C","~C","~O"),
	new Neighbor (338,"C","~C","~C","~H","~N"),
	new Neighbor (339,"C","~C","~C","~H","~O"),
	new Neighbor (340,"C","~C","~C","~N"),
	new Neighbor (341,"C","~C","~C","~O"),
	new Neighbor (342,"C","~C","~Cl"),
	new Neighbor (343,"C","~C","~Cl","~H"),
	new Neighbor (344,"C","~C","~H"),
	new Neighbor (345,"C","~C","~H","~N"),
	new Neighbor (346,"C","~C","~H","~O"),
	new Neighbor (347,"C","~C","~H","~O","~O"),
	new Neighbor (348,"C","~C","~H","~P"),
	new Neighbor (349,"C","~C","~H","~S"),
	new Neighbor (350,"C","~C","~I"),
	new Neighbor (351,"C","~C","~N"),
	new Neighbor (352,"C","~C","~O"),
	new Neighbor (353,"C","~C","~S"),
	new Neighbor (354,"C","~C","~Si"),
	new Neighbor (355,"C","~C",":C"),
	new Neighbor (356,"C","~C",":C",":C"),
	new Neighbor (357,"C","~C",":C",":N"),
	new Neighbor (358,"C","~C",":N"),
	new Neighbor (359,"C","~C",":N",":N"),
	new Neighbor (360,"C","~Cl","~Cl"),
	new Neighbor (361,"C","~Cl","~H"),
	new Neighbor (362,"C","~Cl",":C"),
	new Neighbor (363,"C","~F","~F"),
	new Neighbor (364,"C","~F",":C"),
	new Neighbor (365,"C","~H","~N"),
	new Neighbor (366,"C","~H","~O"),
	new Neighbor (367,"C","~H","~O","~O"),
	new Neighbor (368,"C","~H","~S"),
	new Neighbor (369,"C","~H","~Si"),
	new Neighbor (370,"C","~H",":C"),
	new Neighbor (371,"C","~H",":C",":C"),
	new Neighbor (372,"C","~H",":C",":N"),
	new Neighbor (373,"C","~H",":N"),
	new Neighbor (374,"C","~H","~H","~H"),
	new Neighbor (375,"C","~N","~N"),
	new Neighbor (376,"C","~N",":C"),
	new Neighbor (377,"C","~N",":C",":C"),
	new Neighbor (378,"C","~N",":C",":N"),
	new Neighbor (379,"C","~N",":N"),
	new Neighbor (380,"C","~O","~O"),
	new Neighbor (381,"C","~O",":C"),
	new Neighbor (382,"C","~O",":C",":C"),
	new Neighbor (383,"C","~S",":C"),
	new Neighbor (384,"C",":C",":C"),
	new Neighbor (385,"C",":C",":C",":C"),
	new Neighbor (386,"C",":C",":C",":N"),
	new Neighbor (387,"C",":C",":N"),
	new Neighbor (388,"C",":C",":N",":N"),
	new Neighbor (389,"C",":N",":N"),
	new Neighbor (390,"N","~C","~C"),
	new Neighbor (391,"N","~C","~C","~C"),
	new Neighbor (392,"N","~C","~C","~H"),
	new Neighbor (393,"N","~C","~H"),
	new Neighbor (394,"N","~C","~H","~N"),
	new Neighbor (395,"N","~C","~O"),
	new Neighbor (396,"N","~C",":C"),
	new Neighbor (397,"N","~C",":C",":C"),
	new Neighbor (398,"N","~H","~N"),
	new Neighbor (399,"N","~H",":C"),
	new Neighbor (400,"N","~H",":C",":C"),
	new Neighbor (401,"N","~O","~O"),
	new Neighbor (402,"N","~O",":O"),
	new Neighbor (403,"N",":C",":C"),
	new Neighbor (404,"N",":C",":C",":C"),
	new Neighbor (405,"O","~C","~C"),
	new Neighbor (406,"O","~C","~H"),
	new Neighbor (407,"O","~C","~P"),
	new Neighbor (408,"O","~H","~S"),
	new Neighbor (409,"O",":C",":C"),
	new Neighbor (410,"P","~C","~C"),
	new Neighbor (411,"P","~O","~O"),
	new Neighbor (412,"S","~C","~C"),
	new Neighbor (413,"S","~C","~H"),
	new Neighbor (414,"S","~C","~O"),
	new Neighbor (415,"Si","~C","~C"),
	// section 5
	new Neighbor (416,"C", "=C"),
	new Neighbor (417,"C", "#C"),
	new Neighbor (418,"C", "=N"),
	new Neighbor (419,"C", "#N"),
	new Neighbor (420,"C", "=O"),
	new Neighbor (421,"C", "=S"),
	new Neighbor (422,"N", "=N"),
	new Neighbor (423,"N", "=O"),
	new Neighbor (424,"N", "=P"),
	new Neighbor (425,"P", "=O"),
	new Neighbor (426,"P", "=P"),
	new Neighbor (427,"C", "#C", "-C"),
	new Neighbor (428,"C","#C","-H"),
	new Neighbor (429,"C","#N","-C"),
	new Neighbor (430,"C","-C","-C","=C"),
	new Neighbor (431,"C","-C","-C","=N"),
	new Neighbor (432,"C","-C","-C","=O"),
	new Neighbor (433,"C","-C","-Cl","=O"),
	new Neighbor (434,"C","-C","-H","=C"),
	new Neighbor (435,"C","-C","-H","=N"),
	new Neighbor (436,"C","-C","-H","=O"),
	new Neighbor (437,"C","-C","-N","=C"),
	new Neighbor (438,"C","-C","-N","=N"),
	new Neighbor (439,"C","-C","-N","=O"),
	new Neighbor (440,"C","-C","-O","=O"),
	new Neighbor (441,"C","-C","=C"),
	new Neighbor (442,"C","-C","=N"),
	new Neighbor (443,"C","-C","=O"),
	new Neighbor (444,"C","-Cl","=O"),
	new Neighbor (445,"C","-H","-N","=C"),
	new Neighbor (446,"C","-H","=C"),
	new Neighbor (447,"C","-H","=N"),
	new Neighbor (448,"C","-H","=O"),
	new Neighbor (449,"C","-N","=C"),
	new Neighbor (450,"C","-N","=N"),
	new Neighbor (451,"C","-N","=O"),
	new Neighbor (452,"C","-O","=O"),
	new Neighbor (453,"N","-C","=C"),
	new Neighbor (454,"N","-C","=O"),
	new Neighbor (455,"N","-O","=O"),
	new Neighbor (456,"P","-O","=O"),
	new Neighbor (457,"S","-C","=O"),
	new Neighbor (458,"S","-O","=O"),
	new Neighbor (459,"S","=O","=O")
    };


    static class Pattern {
	int bit;
	Molecule query;
	int[] fp;

	public Pattern (int bit, String pattern) {
	    this.bit = bit;
	    try {
		MolHandler mh = new MolHandler ();
		mh.setQueryMode(true);
		mh.setMolecule(pattern);
		mh.aromatize();
		query = mh.getMolecule();
		fp = mh.generateFingerprintInInts(_FPINTS, _FPPATH, 1);
	    }
	    catch (Exception ex) { ex.printStackTrace(); }
	}
    }

    static Pattern[] PATTERNS = {
	new Pattern (460,"C-C-C#C"),
	new Pattern (461,"O-C-C=N"),
	new Pattern (462,"O-C-C=O"),
	new Pattern (463,"N:C-S-[#1]"),
	new Pattern (464,"[#7]-[#6]-[#6]=[#6]"),
	new Pattern (465,"O=S-C-C"),
	new Pattern (466,"[#7]#[#6]-[#6]=[#6]"),
	new Pattern (467,"C=N-N-C"),
	new Pattern (468,"O=S-C-N"),
	new Pattern (469,"S-S-C:C"),
	new Pattern (470,"C:C-C=C"),
	new Pattern (471,"S:C:C:C"),
	new Pattern (472,"C:N:C-C"),
	new Pattern (473,"S-C:N:C"),
	new Pattern (474,"S:C:C:N"),
	new Pattern (475,"S-C=N-C"),
	new Pattern (476,"C-O-C=C"),
	new Pattern (477,"N-N-C:C"),
	new Pattern (478,"S-C=N-[#1]"),
	new Pattern (479,"S-C-S-C"),
	new Pattern (480,"C:S:C-C"),
	new Pattern (481,"O-S-C:C"),
	new Pattern (482,"C:N-C:C"),
	new Pattern (483,"N-S-C:C"),
	new Pattern (484,"N-C:N:C"),
	new Pattern (485,"N:C:C:N"),
	new Pattern (486,"N-C:N:N"),
	new Pattern (487,"N-C=N-C"),
	new Pattern (488,"N-C=N-[#1]"),
	new Pattern (489,"N-C-S-C"),
	new Pattern (490,"C-C-C=C"),
	new Pattern (491,"C-N:C-[#1]"),
	new Pattern (492,"N-C:O:C"),
	new Pattern (493,"[#8]=[#6]-[#6]:[#6]"),
	new Pattern (494,"O=C-C:N"),
	new Pattern (495,"C-N-C:C"),
	new Pattern (496,"N:N-C-[#1]"),
	new Pattern (497,"O-C:C:N"),
	new Pattern (498,"O-C=C-C"),
	new Pattern (499,"N-C:C:N"),
	new Pattern (500,"C-S-C:C"),
	new Pattern (501,"Cl-C:C-C"),
	new Pattern (502,"N-C=C-[#1]"),
	new Pattern (503,"Cl-C:C-[#1]"),
	new Pattern (504,"N:C:N-C"),
	new Pattern (505,"Cl-C:C-O"),
	new Pattern (506,"C-C:N:C"),
	new Pattern (507,"C-C-S-C"),
	new Pattern (508,"S=C-N-C"),
	new Pattern (509,"Br-C:C-C"),
	new Pattern (510,"[#1]-N-N-[#1]"),
	new Pattern (511,"S=C-N-[#1]"),
	new Pattern (512,"C-[As]-O-[#1]"),
	new Pattern (513,"S:C:C-[#1]"),
	new Pattern (514,"O-N-C-C"),
	new Pattern (515,"N-N-C-C"),
	new Pattern (516,"[#1]-C=C-[#1]"),
	new Pattern (517,"N-N-C-N"),
	new Pattern (518,"O=C-N-N"),
	new Pattern (519,"N=C-N-C"),
	new Pattern (520,"C=C-C:C"),
	new Pattern (521,"C:N-C-[#1]"),
	new Pattern (522,"C-N-N-[#1]"),
	new Pattern (523,"N:C:C-C"),
	new Pattern (524,"C-C=C-C"),
	new Pattern (525,"[As]-C:C-[#1]"),
	new Pattern (526,"Cl-C:C-Cl"),
	new Pattern (527,"C:C:N-[#1]"),
	new Pattern (528,"[#1]-N-C-[#1]"),
	new Pattern (529,"Cl-C-C-Cl"),
	new Pattern (530,"N:C-C:C"),
	new Pattern (531,"S-C:C-C"),
	new Pattern (532,"S-C:C-[#1]"),
	new Pattern (533,"S-C:C-N"),
	new Pattern (534,"S-C:C-O"),
	new Pattern (535,"O=C-C-C"),
	new Pattern (536,"O=C-C-N"),
	new Pattern (537,"O=C-C-O"),
	new Pattern (538,"N=C-C-C"),
	new Pattern (539,"N=C-C-[#1]"),
	new Pattern (540,"C-N-C-[#1]"),
	new Pattern (541,"O-C:C-C"),
	new Pattern (542,"O-C:C-[#1]"),
	new Pattern (543,"O-C:C-N"),
	new Pattern (544,"O-C:C-O"),
	new Pattern (545,"N-C:C-C"),
	new Pattern (546,"N-C:C-[#1]"),
	new Pattern (547,"N-C:C-N"),
	new Pattern (548,"O-C-C:C"),
	new Pattern (549,"N-C-C:C"),
	new Pattern (550,"Cl-C-C-C"),
	new Pattern (551,"Cl-C-C-O"),
	new Pattern (552,"C:C-C:C"),
	new Pattern (553,"O=C-C=C"),
	new Pattern (554,"Br-C-C-C"),
	new Pattern (555,"N=C-C=C"),
	new Pattern (556,"C=C-C-C"),
	new Pattern (557,"N:C-O-[#1]"),
	new Pattern (558,"O=N-C:C"),
	new Pattern (559,"O-C-N-[#1]"),
	new Pattern (560,"N-C-N-C"),
	new Pattern (561,"Cl-C-C=O"),
	new Pattern (562,"Br-C-C=O"),
	new Pattern (563,"O-C-O-C"),
	new Pattern (564,"C=C-C=C"),
	new Pattern (565,"C:C-O-C"),
	new Pattern (566,"O-C-C-N"),
	new Pattern (567,"O-C-C-O"),
	new Pattern (568,"N#C-C-C"),
	new Pattern (569,"N-C-C-N"),
	new Pattern (570,"C:C-C-C"),
	new Pattern (571,"[#1]-C-O-[#1]"),
	new Pattern (572,"N:C:N:C"),
	new Pattern (573,"O-C-C=C"),
	new Pattern (574,"O-C-C:C-C"),
	new Pattern (575,"O-C-C:C-O"),
	new Pattern (576,"N=C-C:C-[#1]"),
	new Pattern (577,"C:C-N-C:C"),
	new Pattern (578,"C-C:C-C:C"),
	new Pattern (579,"O=C-C-C-C"),
	new Pattern (580,"O=C-C-C-N"),
	new Pattern (581,"O=C-C-C-O"),
	new Pattern (582,"C-C-C-C-C"),
	new Pattern (583,"Cl-C:C-O-C"),
	new Pattern (584,"C:C-C=C-C"),
	new Pattern (585,"C-C:C-N-C"),
	new Pattern (586,"C-S-C-C-C"),
	new Pattern (587,"N-C:C-O-[#1]"),
	new Pattern (588,"O=C-C-C=O"),
	new Pattern (589,"C-C:C-O-C"),
	new Pattern (590,"C-C:C-O-[#1]"),
	new Pattern (591,"Cl-C-C-C-C"),
	new Pattern (592,"N-C-C-C-C"),
	new Pattern (593,"N-C-C-C-N"),
	new Pattern (594,"C-O-C-C=C"),
	new Pattern (595,"C:C-C-C-C"),
	new Pattern (596,"N=C-N-C-C"),
	new Pattern (597,"O=C-C-C:C"),
	new Pattern (598,"Cl-C:C:C-C"),
	new Pattern (599,"[#1]-C-C=C-[#1]"),
	new Pattern (600,"N-C:C:C-C"),
	new Pattern (601,"N-C:C:C-N"),
	new Pattern (602,"O=C-C-N-C"),
	new Pattern (603,"C-C:C:C-C"),
	new Pattern (604,"C-O-C-C:C"),
	new Pattern (605,"O=C-C-O-C"),
	new Pattern (606,"O-C:C-C-C"),
	new Pattern (607,"N-C-C-C:C"),
	new Pattern (608,"C-C-C-C:C"),
	new Pattern (609,"Cl-C-C-N-C"),
	new Pattern (610,"C-O-C-O-C"),
	new Pattern (611,"N-C-C-N-C"),
	new Pattern (612,"N-C-O-C-C"),
	new Pattern (613,"C-N-C-C-C"),
	new Pattern (614,"C-C-O-C-C"),
	new Pattern (615,"N-C-C-O-C"),
	new Pattern (616,"C:C:N:N:C"),
	new Pattern (617,"C-C-C-O-[#1]"),
	new Pattern (618,"C:C-C-C:C"),
	new Pattern (619,"O-C-C=C-C"),
	new Pattern (620,"C:C-O-C-C"),
	new Pattern (621,"N-C:C:C:N"),
	new Pattern (622,"O=C-O-C:C"),
	new Pattern (623,"O=C-C:C-C"),
	new Pattern (624,"O=C-C:C-N"),
	new Pattern (625,"O=C-C:C-O"),
	new Pattern (626,"C-O-C:C-C"),
	new Pattern (627,"O=[As]-C:C:C"),
	new Pattern (628,"C-N-C-C:C"),
	new Pattern (629,"S-C:C:C-N"),
	new Pattern (630,"O-C:C-O-C"),
	new Pattern (631,"O-C:C-O-[#1]"),
	new Pattern (632,"C-C-O-C:C"),
	new Pattern (633,"N-C-C:C-C"),
	new Pattern (634,"C-C-C:C-C"),
	new Pattern (635,"N-N-C-N-[#1]"),
	new Pattern (636,"C-N-C-N-C"),
	new Pattern (637,"O-C-C-C-C"),
	new Pattern (638,"O-C-C-C-N"),
	new Pattern (639,"O-C-C-C-O"),
	new Pattern (640,"C=C-C-C-C"),
	new Pattern (641,"O-C-C-C=C"),
	new Pattern (642,"O-C-C-C=O"),
	new Pattern (643,"[#1]-C-C-N-[#1]"),
	new Pattern (644,"C-C=N-N-C"),
	new Pattern (645,"O=C-N-C-C"),
	new Pattern (646,"O=C-N-C-[#1]"),
	new Pattern (647,"O=C-N-C-N"),
	new Pattern (648,"O=N-C:C-N"),
	new Pattern (649,"O=N-C:C-O"),
	new Pattern (650,"O=C-N-C=O"),
	new Pattern (651,"O-C:C:C-C"),
	new Pattern (652,"O-C:C:C-N"),
	new Pattern (653,"O-C:C:C-O"),
	new Pattern (654,"N-C-N-C-C"),
	new Pattern (655,"O-C-C-C:C"),
	new Pattern (656,"C-C-N-C-C"),
	new Pattern (657,"C-N-C:C-C"),
	new Pattern (658,"C-C-S-C-C"),
	new Pattern (659,"O-C-C-N-C"),
	new Pattern (660,"C-C=C-C-C"),
	new Pattern (661,"O-C-O-C-C"),
	new Pattern (662,"O-C-C-O-C"),
	new Pattern (663,"O-C-C-O-[#1]"),
	new Pattern (664,"C-C=C-C=C"),
	new Pattern (665,"N-C:C-C-C"),
	new Pattern (666,"C=C-C-O-C"),
	new Pattern (667,"C=C-C-O-[#1]"),
	new Pattern (668,"C-C:C-C-C"),
	new Pattern (669,"Cl-C:C-C=O"),
	new Pattern (670,"Br-C:C:C-C"),
	new Pattern (671,"O=C-C=C-C"),
	new Pattern (672,"O=C-C=C-[#1]"),
	new Pattern (673,"O=C-C=C-N"),
	new Pattern (674,"N-C-N-C:C"),
	new Pattern (675,"Br-C-C-C:C"),
	new Pattern (676,"N#C-C-C-C"),
	new Pattern (677,"C-C=C-C:C"),
	new Pattern (678,"C-C-C=C-C"),
	new Pattern (679,"C-C-C-C-C-C"),
	new Pattern (680,"O-C-C-C-C-C"),
	new Pattern (681,"O-C-C-C-C-O"),
	new Pattern (682,"O-C-C-C-C-N"),
	new Pattern (683,"N-C-C-C-C-C"),
	new Pattern (684,"O=C-C-C-C-C"),
	new Pattern (685,"O=C-C-C-C-N"),
	new Pattern (686,"O=C-C-C-C-O"),
	new Pattern (687,"O=C-C-C-C=O"),
	new Pattern (688,"C-C-C-C-C-C-C"),
	new Pattern (689,"O-C-C-C-C-C-C"),
	new Pattern (690,"O-C-C-C-C-C-O"),
	new Pattern (691,"O-C-C-C-C-C-N"),
	new Pattern (692,"O=C-C-C-C-C-C"),
	new Pattern (693,"O=C-C-C-C-C-O"),
	new Pattern (694,"O=C-C-C-C-C=O"),
	new Pattern (695,"O=C-C-C-C-C-N"),
	new Pattern (696,"C-C-C-C-C-C-C-C"),
	new Pattern (697,"C-C-C-C-C-C(C)-C"),
	new Pattern (698,"O-C-C-C-C-C-C-C"),
	new Pattern (699,"O-C-C-C-C-C(C)-C"),
	new Pattern (700,"O-C-C-C-C-C-O-C"),
	new Pattern (701,"O-C-C-C-C-C(O)-C"),
	new Pattern (702,"O-C-C-C-C-C-N-C"),
	new Pattern (703,"O-C-C-C-C-C(N)-C"),
	new Pattern (704,"O=C-C-C-C-C-C-C"),
	new Pattern (705,"O=C-C-C-C-C(O)-C"),
	new Pattern (706,"O=C-C-C-C-C(=O)-C"),
	new Pattern (707,"O=C-C-C-C-C(N)-C"),
	new Pattern (708,"C-C(C)-C-C"),
	new Pattern (709,"C-C(C)-C-C-C"),
	new Pattern (710,"C-C-C(C)-C-C"),
	new Pattern (711,"C-C(C)(C)-C-C"),
	new Pattern (712,"C-C(C)-C(C)-C"),
	
	// section 7
	new Pattern (713,"Cc1ccc(C)cc1"),
	new Pattern (714,"Cc1ccc(O)cc1"),
	new Pattern (715,"Cc1ccc(S)cc1"),
	new Pattern (716,"Cc1ccc(N)cc1"),
	new Pattern (717,"Cc1ccc(Cl)cc1"),
	new Pattern (718,"Cc1ccc(Br)cc1"),
	new Pattern (719,"Oc1ccc(O)cc1"),
	new Pattern (720,"Oc1ccc(S)cc1"),
	new Pattern (721,"Oc1ccc(N)cc1"),
	new Pattern (722,"Oc1ccc(Cl)cc1"),
	new Pattern (723,"Oc1ccc(Br)cc1"),
	new Pattern (724,"Sc1ccc(S)cc1"),
	new Pattern (725,"Sc1ccc(N)cc1"),
	new Pattern (726,"Sc1ccc(Cl)cc1"),
	new Pattern (727,"Sc1ccc(Br)cc1"),
	new Pattern (728,"Nc1ccc(N)cc1"),
	new Pattern (729,"Nc1ccc(Cl)cc1"),
	new Pattern (730,"Nc1ccc(Br)cc1"),
	new Pattern (731,"Clc1ccc(Cl)cc1"),
	new Pattern (732,"Clc1ccc(Br)cc1"),
	new Pattern (733,"Brc1ccc(Br)cc1"),
	new Pattern (734,"Cc1cc(C)ccc1"),
	new Pattern (735,"Cc1cc(O)ccc1"),
	new Pattern (736,"Cc1cc(S)ccc1"),
	new Pattern (737,"Cc1cc(N)ccc1"),
	new Pattern (738,"Cc1cc(Cl)ccc1"),
	new Pattern (739,"Cc1cc(Br)ccc1"),
	new Pattern (740,"Oc1cc(O)ccc1"),
	new Pattern (741,"Oc1cc(S)ccc1"),
	new Pattern (742,"Oc1cc(N)ccc1"),
	new Pattern (743,"Oc1cc(Cl)ccc1"),
	new Pattern (744,"Oc1cc(Br)ccc1"),
	new Pattern (745,"Sc1cc(S)ccc1"),
	new Pattern (746,"Sc1cc(N)ccc1"),
	new Pattern (747,"Sc1cc(Cl)ccc1"),
	new Pattern (748,"Sc1cc(Br)ccc1"),
	new Pattern (749,"Nc1cc(N)ccc1"),
	new Pattern (750,"Nc1cc(Cl)ccc1"),
	new Pattern (751,"Nc1cc(Br)ccc1"),
	new Pattern (752,"Clc1cc(Cl)ccc1"),
	new Pattern (753,"Clc1cc(Br)ccc1"),
	new Pattern (754,"Brc1cc(Br)ccc1"),
	new Pattern (755,"Cc1c(C)cccc1"),
	new Pattern (756,"Cc1c(O)cccc1"),
	new Pattern (757,"Cc1c(S)cccc1"),
	new Pattern (758,"Cc1c(N)cccc1"),
	new Pattern (759,"Cc1c(Cl)cccc1"),
	new Pattern (760,"Cc1c(Br)cccc1"),
	new Pattern (761,"Oc1c(O)cccc1"),
	new Pattern (762,"Oc1c(S)cccc1"),
	new Pattern (763,"Oc1c(N)cccc1"),
	new Pattern (764,"Oc1c(Cl)cccc1"),
	new Pattern (765,"Oc1c(Br)cccc1"),
	new Pattern (766,"Sc1c(S)cccc1"),
	new Pattern (767,"Sc1c(N)cccc1"),
	new Pattern (768,"Sc1c(Cl)cccc1"),
	new Pattern (769,"Sc1c(Br)cccc1"),
	new Pattern (770,"Nc1c(N)cccc1"),
	new Pattern (771,"Nc1c(Cl)cccc1"),
	new Pattern (772,"Nc1c(Br)cccc1"),
	new Pattern (773,"Clc1c(Cl)cccc1"),
	new Pattern (774,"Clc1c(Br)cccc1"),
	new Pattern (775,"Brc1c(Br)cccc1"),
	new Pattern (776,"CC1CCC(C)CC1"),
	new Pattern (777,"CC1CCC(O)CC1"),
	new Pattern (778,"CC1CCC(S)CC1"),
	new Pattern (779,"CC1CCC(N)CC1"),
	new Pattern (780,"CC1CCC(Cl)CC1"),
	new Pattern (781,"CC1CCC(Br)CC1"),
	new Pattern (782,"OC1CCC(O)CC1"),
	new Pattern (783,"OC1CCC(S)CC1"),
	new Pattern (784,"OC1CCC(N)CC1"),
	new Pattern (785,"OC1CCC(Cl)CC1"),
	new Pattern (786,"OC1CCC(Br)CC1"),
	new Pattern (787,"SC1CCC(S)CC1"),
	new Pattern (788,"SC1CCC(N)CC1"),
	new Pattern (789,"SC1CCC(Cl)CC1"),
	new Pattern (790,"SC1CCC(Br)CC1"),
	new Pattern (791,"NC1CCC(N)CC1"),
	new Pattern (792,"NC1CCC(Cl)CC1"),
	new Pattern (793,"NC1CCC(Br)CC1"),
	new Pattern (794,"ClC1CCC(Cl)CC1"),
	new Pattern (795,"ClC1CCC(Br)CC1"),
	new Pattern (796,"BrC1CCC(Br)CC1"),
	new Pattern (797,"CC1CC(C)CCC1"),
	new Pattern (798,"CC1CC(O)CCC1"),
	new Pattern (799,"CC1CC(S)CCC1"),
	new Pattern (800,"CC1CC(N)CCC1"),
	new Pattern (801,"CC1CC(Cl)CCC1"),
	new Pattern (802,"CC1CC(Br)CCC1"),
	new Pattern (803,"OC1CC(O)CCC1"),
	new Pattern (804,"OC1CC(S)CCC1"),
	new Pattern (805,"OC1CC(N)CCC1"),
	new Pattern (806,"OC1CC(Cl)CCC1"),
	new Pattern (807,"OC1CC(Br)CCC1"),
	new Pattern (808,"SC1CC(S)CCC1"),
	new Pattern (809,"SC1CC(N)CCC1"),
	new Pattern (810,"SC1CC(Cl)CCC1"),
	new Pattern (811,"SC1CC(Br)CCC1"),
	new Pattern (812,"NC1CC(N)CCC1"),
	new Pattern (813,"NC1CC(Cl)CCC1"),
	new Pattern (814,"NC1CC(Br)CCC1"),
	new Pattern (815,"ClC1CC(Cl)CCC1"),
	new Pattern (816,"ClC1CC(Br)CCC1"),
	new Pattern (817,"BrC1CC(Br)CCC1"),
	new Pattern (818,"CC1C(C)CCCC1"),
	new Pattern (819,"CC1C(O)CCCC1"),
	new Pattern (820,"CC1C(S)CCCC1"),
	new Pattern (821,"CC1C(N)CCCC1"),
	new Pattern (822,"CC1C(Cl)CCCC1"),
	new Pattern (823,"CC1C(Br)CCCC1"),
	new Pattern (824,"OC1C(O)CCCC1"),
	new Pattern (825,"OC1C(S)CCCC1"),
	new Pattern (826,"OC1C(N)CCCC1"),
	new Pattern (827,"OC1C(Cl)CCCC1"),
	new Pattern (828,"OC1C(Br)CCCC1"),
	new Pattern (829,"SC1C(S)CCCC1"),
	new Pattern (830,"SC1C(N)CCCC1"),
	new Pattern (831,"SC1C(Cl)CCCC1"),
	new Pattern (832,"SC1C(Br)CCCC1"),
	new Pattern (833,"NC1C(N)CCCC1"),
	new Pattern (834,"NC1C(Cl)CCCC1"),
	new Pattern (835,"NC1C(Br)CCCC1"),
	new Pattern (836,"ClC1C(Cl)CCCC1"),
	new Pattern (837,"ClC1C(Br)CCCC1"),
	new Pattern (838,"BrC1C(Br)CCCC1"),
	new Pattern (839,"CC1CC(C)CC1"),
	new Pattern (840,"CC1CC(O)CC1"),
	new Pattern (841,"CC1CC(S)CC1"),
	new Pattern (842,"CC1CC(N)CC1"),
	new Pattern (843,"CC1CC(Cl)CC1"),
	new Pattern (844,"CC1CC(Br)CC1"),
	new Pattern (845,"OC1CC(O)CC1"),
	new Pattern (846,"OC1CC(S)CC1"),
	new Pattern (847,"OC1CC(N)CC1"),
	new Pattern (848,"OC1CC(Cl)CC1"),
	new Pattern (849,"OC1CC(Br)CC1"),
	new Pattern (850,"SC1CC(S)CC1"),
	new Pattern (851,"SC1CC(N)CC1"),
	new Pattern (852,"SC1CC(Cl)CC1"),
	new Pattern (853,"SC1CC(Br)CC1"),
	new Pattern (854,"NC1CC(N)CC1"),
	new Pattern (855,"NC1CC(Cl)CC1"),
	new Pattern (856,"NC1CC(Br)CC1"),
	new Pattern (857,"ClC1CC(Cl)CC1"),
	new Pattern (858,"ClC1CC(Br)CC1"),
	new Pattern (859,"BrC1CC(Br)CC1"),
	new Pattern (860,"CC1C(C)CCC1"),
	new Pattern (861,"CC1C(O)CCC1"),
	new Pattern (862,"CC1C(S)CCC1"),
	new Pattern (863,"CC1C(N)CCC1"),
	new Pattern (864,"CC1C(Cl)CCC1"),
	new Pattern (865,"CC1C(Br)CCC1"),
	new Pattern (866,"OC1C(O)CCC1"),
	new Pattern (867,"OC1C(S)CCC1"),
	new Pattern (868,"OC1C(N)CCC1"),
	new Pattern (869,"OC1C(Cl)CCC1"),
	new Pattern (870,"OC1C(Br)CCC1"),
	new Pattern (871,"SC1C(S)CCC1"),
	new Pattern (872,"SC1C(N)CCC1"),
	new Pattern (873,"SC1C(Cl)CCC1"),
	new Pattern (874,"SC1C(Br)CCC1"),
	new Pattern (875,"NC1C(N)CCC1"),
	new Pattern (876,"NC1C(Cl)CC1"),
	new Pattern (877,"NC1C(Br)CCC1"),
	new Pattern (878,"ClC1C(Cl)CCC1"),
	new Pattern (879,"ClC1C(Br)CCC1"),
	new Pattern (880,"BrC1C(Br)CCC1")
    };
}

