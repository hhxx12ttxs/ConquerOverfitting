package name.mjw.jamber.IO.AMBER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;

import name.mjw.jamber.IO.FortranFormat;

import org.apache.log4j.Logger;

/**
 * Representation of all AMBER parameters found both in a <a
 * href="http://ambermd.org/formats.html#parm.dat">parm</a> and a <a
 * href="http://ambermd.org/formats.html#frcmod">frcmod</a> file.
 * <p>
 * Due to the similarity of both formats, they are amalgamated into one parent
 * type, {@link ParameterStore} to facilitate the harvesting of parameters from
 * multiple parm and frcmod files.
 * 
 * @author mjw
 * 
 */
class ParameterStore {

	private final Logger LOG = Logger.getLogger(ParameterStore.class);
	private String title;

	private final ArrayList<AtomIdentifier> atomTypes;
	private final ArrayList<BondType> bondTypes;
	private final ArrayList<AngleType> angleTypes;
	private final ArrayList<DihedralType> dihedralTypes;

	ParameterStore() throws IOException {

		this.atomTypes = new ArrayList<AtomIdentifier>();
		this.bondTypes = new ArrayList<BondType>();
		this.angleTypes = new ArrayList<AngleType>();
		this.dihedralTypes = new ArrayList<DihedralType>();

	}

	private void readAtomTypeSection(BufferedReader br) throws IOException {

		String line;
		// Now, keep reading until we hit an empty line
		while (!(line = br.readLine()).matches("\\s*")) {

			try {
				// C 12.01 0.616 ! sp2 C carbonyl group
				FortranFormat formatter = new FortranFormat(
						"(A2,1X,F10.2,1X,F10.2)");
				ArrayList<Object> lineObjects = formatter.parse(line);

				AtomIdentifier atomType = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(0),
								(Double) lineObjects.get(1));

				if (atomTypes.contains(atomType)) {

					int indexOfAtomTypeToBeRemoved = atomTypes
							.indexOf(atomType);
					System.out.println("Replacing atomType: ");
					System.out.println(bondTypes
							.get(indexOfAtomTypeToBeRemoved));
					atomTypes.remove(indexOfAtomTypeToBeRemoved);
					System.out.println("with");
					System.out.println(atomType);
					atomTypes.add(atomType);

				}

				this.atomTypes.add(atomType);

			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}

		}

	}

	private void readHydrophobicAtomTypes(BufferedReader br) throws IOException {

		// C H HO N NA NB NC N2 NT N2 N3 N* O OH OS P O2

		// TODO Fix this
		// Skip because this seems to incorrectly formatted in parm99.dat
		br.readLine();
	}

	private void readBondTypeSection(BufferedReader br) throws IOException {

		String line;
		while (!(line = br.readLine()).matches("\\s*")) {

			try {
				// OW-HW 553.0 0.9572 ! TIP3P water
				FortranFormat formatter = new FortranFormat("(A2,1X,A2,2F10.2)");
				ArrayList<Object> lineObjects = formatter.parse(line);

				AtomIdentifier i = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(0));
				AtomIdentifier j = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(1));

				// Check to see if this atomType exists in what we have parsed
				// so far
				if (!atomTypes.contains(i)) {
					System.out.println("Unknown atom type " + i);
				}

				if (!atomTypes.contains(j)) {
					System.out.println("Unknown atom type " + j);
				}

				BondType bondType = new BondType(i, j,
						(Double) lineObjects.get(2),
						(Double) lineObjects.get(3));

				if (bondTypes.contains(bondType)) {

					int indexOfBondTypeToBeRemoved = bondTypes
							.indexOf(bondType);
					System.out.println("Replacing bondType: ");
					System.out.println(bondTypes
							.get(indexOfBondTypeToBeRemoved));
					bondTypes.remove(indexOfBondTypeToBeRemoved);
					System.out.println("with");
					System.out.println(bondType);
					bondTypes.add(bondType);
				}

				this.bondTypes.add(bondType);

			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}

		}

	}

	private void readAngleTypeSection(BufferedReader br) throws IOException {
		String line;
		while (!(line = br.readLine()).matches("\\s*")) {

			try {
				// HW-OW-HW 100. 104.52 TIP3P water
				FortranFormat formatter = new FortranFormat(
						"(A2,1X,A2,1X,A2,2F10.2)");
				ArrayList<Object> lineObjects = formatter.parse(line);

				AtomIdentifier i = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(0));
				AtomIdentifier j = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(1));
				AtomIdentifier k = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(2));

				if (!atomTypes.contains(i)) {
					System.out.println("Unknown atom type " + i);
				}

				if (!atomTypes.contains(j)) {
					System.out.println("Unknown atom type " + j);
				}
				if (!atomTypes.contains(k)) {
					System.out.println("Unknown atom type " + k);
				}

				AngleType angleType = new AngleType(i, j, k,
						(Double) lineObjects.get(3),
						(Double) lineObjects.get(4));

				/*
				 * Replace any existing angleTypes with any new ones.
				 */
				if (angleTypes.contains(angleType)) {

					int indexOfangleTypeToBeRemoved = angleTypes
							.indexOf(angleType);
					System.out.println("Replacing angleType: ");
					System.out.println(angleTypes
							.get(indexOfangleTypeToBeRemoved));
					angleTypes.remove(indexOfangleTypeToBeRemoved);
					System.out.println("with");
					System.out.println(angleType);
					angleTypes.add(angleType);

				}

				this.angleTypes.add(angleType);

			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}

		}

	}

	private void readProperDihedralTypeSection(BufferedReader br)
			throws IOException {
		String line;
		while (!(line = br.readLine()).matches("\\s*")) {

			try {
				// X -C -C -X 4 14.50 180.0 2. Junmei et al, 1999
				FortranFormat formatter = new FortranFormat(
						"(A2,1X,A2,1X,A2,1X,A2,I4,3F15.2)");
				ArrayList<Object> lineObjects = formatter.parse(line);

				AtomIdentifier i = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(0));
				AtomIdentifier j = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(1));
				AtomIdentifier k = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(2));
				AtomIdentifier l = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(3));

				if (!atomTypes.contains(i)) {
					System.out.println(line);
					System.out.println("Unknown atom type " + i);
				}

				if (!atomTypes.contains(j)) {
					System.out.println("Unknown atom type " + j);
				}

				if (!atomTypes.contains(k)) {
					System.out.println("Unknown atom type " + k);
				}

				if (!atomTypes.contains(l)) {
					System.out.println("Unknown atom type " + l);
				}

				ProperDihedralType properDihedralType = new ProperDihedralType(
						i, j, k, l, (Integer) lineObjects.get(4),
						(Double) lineObjects.get(5),
						(Double) lineObjects.get(6),
						(Double) lineObjects.get(7));

				if (dihedralTypes.contains(properDihedralType)) {
					int indexOfDihedralTypeToBeRemoved = dihedralTypes
							.indexOf(properDihedralType);
					System.out.println("Replacing dihedralType: ");
					System.out.println(dihedralTypes
							.get(indexOfDihedralTypeToBeRemoved));
					dihedralTypes.remove(indexOfDihedralTypeToBeRemoved);
					System.out.println("with");
					System.out.println(properDihedralType);
					dihedralTypes.add(properDihedralType);

				}

				// Multitermed dihedral; peroidicity is negative
				if (((Double) lineObjects.get(7)) < 0.0) {

					LOG.debug("Found multilined term" + properDihedralType);

					// Read the rest of the dihedral terms

					while (!(line = br.readLine()).matches("\\s*")) {

						LOG.debug("Adding sub term");
						lineObjects = formatter.parse(line);

						properDihedralType
								.setBarrierHeight((Double) lineObjects.get(5));
						properDihedralType
								.setPhase((Double) lineObjects.get(6));
						properDihedralType.setPeriodicity((Double) lineObjects
								.get(7));

						if (((Double) lineObjects.get(7)) > 0.0) {
							break;
						}

					}

				}

				this.dihedralTypes.add(properDihedralType);

			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}

		}

	}

	/**
	 * Many generated frcmod files do not adhere to the parm file fixed
	 * formatting, hence one cannot rely on FortranFormat here, therefore this
	 * method falls back to String.split().
	 * 
	 * @param br
	 *            BufferedReader of the frcmod section.
	 * @throws IOException
	 */
	private void readProperDihedralTypeSectionFreeForm(BufferedReader br)
			throws IOException {
		String words[];
		String line;
		while (!(line = br.readLine()).matches("\\s*")) {

			words = line.split("[\\-\\s]+");

			AtomIdentifier i = AtomIdentifierFactory
					.getAtomIdentifier(words[0]);
			AtomIdentifier j = AtomIdentifierFactory
					.getAtomIdentifier(words[1]);
			AtomIdentifier k = AtomIdentifierFactory
					.getAtomIdentifier(words[2]);
			AtomIdentifier l = AtomIdentifierFactory
					.getAtomIdentifier(words[3]);

			if (!atomTypes.contains(i)) {
				System.out.println(line);
				System.out.println("Unknown atom type " + i);
			}

			if (!atomTypes.contains(j)) {
				System.out.println("Unknown atom type " + j);
			}

			if (!atomTypes.contains(k)) {
				System.out.println("Unknown atom type " + k);
			}

			if (!atomTypes.contains(l)) {
				System.out.println("Unknown atom type " + l);
			}

			ProperDihedralType properDihedralType = new ProperDihedralType(i,
					j, k, l, Integer.valueOf(words[4]),
					Double.valueOf(words[5]), Double.valueOf(words[6]),
					Double.valueOf(words[7]));

			if (dihedralTypes.contains(properDihedralType)) {
				int indexOfDihedralTypeToBeRemoved = dihedralTypes
						.indexOf(properDihedralType);
				System.out.println("Replacing dihedralType: ");
				System.out.println(dihedralTypes
						.get(indexOfDihedralTypeToBeRemoved));
				dihedralTypes.remove(indexOfDihedralTypeToBeRemoved);
				System.out.println("with");
				System.out.println(properDihedralType);
				dihedralTypes.add(properDihedralType);
			}

			// Multitermed dihedral; peroidicity is negative
			if (Double.valueOf(words[7]) < 0.0) {

				LOG.debug("Found multilined term" + properDihedralType);

				// Read the rest of the dihedral terms

				while (!(line = br.readLine()).matches("\\s*")) {

					words = line.split("[\\-\\ ]");

					LOG.debug("Adding term");

					properDihedralType.setBarrierHeight(Double
							.valueOf(words[5]));
					properDihedralType.setPhase(Double.valueOf(words[6]));
					properDihedralType.setPeriodicity(Double.valueOf(words[7]));

					if (Double.valueOf(words[7]) > 0.0) {
						break;
					}

				}

			}
			this.dihedralTypes.add(properDihedralType);
		}

	}

	private void readImproperDihedralTypeSection(BufferedReader br)
			throws IOException {
		String line;
		while (!(line = br.readLine()).matches("\\s*")) {

			try {
				// X -X -C -O 10.5 180. 2. JCC,7,(1986),230
				FortranFormat formatter = new FortranFormat(
						"(A2,1X,A2,1X,A2,1X,A2,3F15.2)");
				ArrayList<Object> lineObjects = formatter.parse(line);

				AtomIdentifier i = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(0));
				AtomIdentifier j = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(1));
				AtomIdentifier k = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(2));
				AtomIdentifier l = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(3));

				if (!atomTypes.contains(i)) {
					System.out.println("Unknown atom type " + i);
				}

				if (!atomTypes.contains(j)) {
					System.out.println("Unknown atom type " + j);
				}

				if (!atomTypes.contains(k)) {
					System.out.println("Unknown atom type " + k);
				}

				if (!atomTypes.contains(l)) {
					System.out.println("Unknown atom type " + l);
				}

				ImproperDihedralType improperDihedralType = new ImproperDihedralType(
						i, j, k, l,
						1, // This is assumed
						(Double) lineObjects.get(4),
						(Double) lineObjects.get(5),
						(Double) lineObjects.get(6));

				if (dihedralTypes.contains(improperDihedralType)) {
					int indexOfDihedralTypeToBeRemoved = dihedralTypes
							.indexOf(improperDihedralType);
					System.out.println("Replacing dihedralType: ");
					System.out.println(dihedralTypes
							.get(indexOfDihedralTypeToBeRemoved));
					dihedralTypes.remove(indexOfDihedralTypeToBeRemoved);
					System.out.println("with");
					System.out.println(improperDihedralType);
					dihedralTypes.add(improperDihedralType);
				}

				this.dihedralTypes.add(improperDihedralType);

			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}

		}

	}

	private void readHbond1012(BufferedReader br) throws IOException {
		String line;
		while (!(line = br.readLine()).matches("\\s*")) {

			// TODO
			try {

				FortranFormat formatter = new FortranFormat(
						"(2X,A2,2X,A2,2X,4F10.2,I2)");

				ArrayList<Object> lineObjects = formatter.parse(line);

				LOG.debug(lineObjects);

			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}

		}

	}

	private void readEquivalenceAtomTypesFor612(BufferedReader br)
			throws IOException {
		String line;
		while (!(line = br.readLine()).matches("\\s*")) {

			try {

				FortranFormat formatter = new FortranFormat("(20(A2,2X))");
				ArrayList<Object> lineObjects = formatter.parse(line);

				// What we will point to
				AtomIdentifier toAtomType = AtomIdentifierFactory
						.getAtomIdentifier((String) lineObjects.get(0));
				LOG.debug("toAtomType is " + toAtomType);

				// AtomTypes we need to point to the target
				for (Object object : lineObjects) {

					AtomIdentifier fromAtomType = AtomIdentifierFactory
							.getAtomIdentifier((String) object);

					for (AtomIdentifier atomType : atomTypes) {
						if ((atomType.equals(fromAtomType))) {

							((AtomType) atomType)
									.setSixTwelvePotentialParametersEquivalence(toAtomType);

						}

					}

				}

			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}

		}
	}

	private void read612Parameters(BufferedReader br) throws IOException {

		String line;

		while (!(line = br.readLine()).matches("\\s*|^END")) {

			try {

				// H 0.6000 0.0157 !Ferguson base pair geom.
				FortranFormat formatter2 = new FortranFormat(
						"(2X,A2,10X,F6.4,2X,F6.4)");
				ArrayList<Object> lineObjects2 = formatter2.parse(line);

				LOG.debug(line);

				/*
				 * There can be empty lines in the region before reaching the
				 * END
				 */
				if (!((String) lineObjects2.get(0)).isEmpty()) {

					AtomIdentifier i = AtomIdentifierFactory
							.getAtomIdentifier((String) lineObjects2.get(0));

					if (!atomTypes.contains(i)) {
						System.out.println("Unknown atom type " + i
								+ " in -10- ");

					}

					for (AtomIdentifier atomType : atomTypes) {
						if ((atomType.equals(i))) {

							((AtomType) atomType)
									.setVdwRadius((Double) lineObjects2.get(1));
							((AtomType) atomType)
									.setVdwWellDepth((Double) lineObjects2
											.get(2));

							LOG.debug("Setting " + atomType + " VdwRadius to "
									+ lineObjects2.get(1)
									+ " and VdwWellDepth to "
									+ lineObjects2.get(2));

						}

					}
				}

			} catch (ParseException e) {
				System.out.println(line);
				e.printStackTrace();
			}

		}

		/*
		 * Assign the mapping to **** INPUT FOR EQUIVALENCING ATOM SYMBOLS FOR
		 * THE NON-BONDED 6-12 POTENTIAL PARAMETERS *****
		 */
		for (AtomIdentifier atomType : atomTypes) {

			// We have found an atomType that may need mapping
			if (((AtomType) atomType).getVdwRadius() == -1) {
				LOG.debug(atomType + " has VdwRadius "
						+ ((AtomType) atomType).getVdwRadius());

				// Get the atomType reference
				AtomType fromAtomType = (AtomType) ((AtomType) atomType)
						.getSixTwelvePotentialParametersEquivalence();

				// Now, pull out the actual object and reassign
				for (AtomIdentifier foo : atomTypes) {

					if ((foo.equals(fromAtomType))) {

						LOG.debug("Match " + foo);
						fromAtomType = (AtomType) foo;

					}

				}

				LOG.debug(atomType + " points to " + fromAtomType);

				if (fromAtomType != null) {

					LOG.debug("Pointing " + atomType + " to " + fromAtomType);

					LOG.debug("fromAtomType parameters:");
					LOG.debug(fromAtomType.getVdwRadius());
					LOG.debug(fromAtomType.getVdwWellDepth());

					((AtomType) atomType).setVdwRadius(fromAtomType
							.getVdwRadius());
					((AtomType) atomType).setVdwWellDepth(fromAtomType
							.getVdwWellDepth());

					// Unset this flag
					((AtomType) atomType)
							.setSixTwelvePotentialParametersEquivalence(null);

				} else {
					System.out.println("No VDW parameter for " + atomType);
				}

			}

		}

	}

	private void read612ParametersFreeForm(BufferedReader br)
			throws IOException {

		String words[];
		String line;

		while (!(line = br.readLine()).matches("\\s*|^END")) {

			// H 0.6000 0.0157 !Ferguson base pair geom.

			LOG.debug("read612ParametersFreeForm" + line);

			// Remove any spaces at the beginning of a line
			line = line.replaceAll("(?m)(?:^|\\G) ", "");

			LOG.debug("read612ParametersFreeForm" + line);
			words = line.split("[\\s]+");

			LOG.debug("words[0]: " + words[0]);
			LOG.debug("words[1]: " + words[1]);
			LOG.debug("words[2]: " + words[2]);

			/*
			 * There can be empty lines in the region before reaching the END
			 */
			if (!(words[0]).isEmpty()) {

				AtomIdentifier i = AtomIdentifierFactory
						.getAtomIdentifier(words[0]);

				if (!atomTypes.contains(i)) {
					System.out.println("Unknown atom type " + i + " in -10- ");

				}

				for (AtomIdentifier atomType : atomTypes) {
					if ((atomType.equals(i))) {

						((AtomType) atomType).setVdwRadius(Double
								.valueOf(words[1]));
						((AtomType) atomType).setVdwWellDepth(Double
								.valueOf(words[2]));

						LOG.debug("Setting " + atomType + " VdwRadius to "
								+ Double.valueOf(words[1])
								+ " and VdwWellDepth to "
								+ Double.valueOf(words[2]));

					}

				}
			}

		}

		/*
		 * Assign the mapping to **** INPUT FOR EQUIVALENCING ATOM SYMBOLS FOR
		 * THE NON-BONDED 6-12 POTENTIAL PARAMETERS *****
		 */
		for (AtomIdentifier atomType : atomTypes) {

			// We have found an atomType that may need mapping
			if (((AtomType) atomType).getVdwRadius() == -1) {
				LOG.debug(atomType + " has VdwRadius "
						+ ((AtomType) atomType).getVdwRadius());

				// Get the atomType reference
				AtomType fromAtomType = (AtomType) ((AtomType) atomType)
						.getSixTwelvePotentialParametersEquivalence();

				// Now, pull out the actual object and reassign
				for (AtomIdentifier foo : atomTypes) {

					if ((foo.equals(fromAtomType))) {

						LOG.debug("Match " + foo);
						fromAtomType = (AtomType) foo;

					}

				}

				LOG.debug(atomType + " points to " + fromAtomType);

				if (fromAtomType != null) {

					LOG.debug("Pointing " + atomType + " to " + fromAtomType);

					LOG.debug("fromAtomType parameters:");
					LOG.debug(fromAtomType.getVdwRadius());
					LOG.debug(fromAtomType.getVdwWellDepth());

					((AtomType) atomType).setVdwRadius(fromAtomType
							.getVdwRadius());
					((AtomType) atomType).setVdwWellDepth(fromAtomType
							.getVdwWellDepth());

					// Unset this flag
					((AtomType) atomType)
							.setSixTwelvePotentialParametersEquivalence(null);

				} else {
					System.out.println("No VDW parameter for " + atomType);
				}

			}

		}

	}

	/**
	 * Append parameter information from a parm file stream.
	 * 
	 * Main parameter set file for AMBER. For more details, please see
	 * http://ambermd.org/formats.html#parm.dat
	 * 
	 * @param is
	 *            Inputstream containing parm file
	 * @throws IOException
	 */
	public void readParm(InputStream is) throws IOException {
		if (is == null) {
			throw new RuntimeException("readParm: InputStream is null");
		}

		String line;

		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Section 1: Title
		setTitle(br.readLine());

		// Section 2: ***** INPUT FOR ATOM SYMBOLS AND MASSES *****
		readAtomTypeSection(br);

		// Section 3: ***** INPUT FOR ATOM SYMBOLS THAT ARE HYDROPHILIC *****
		readHydrophobicAtomTypes(br);

		// Section 4: ***** INPUT FOR BOND LENGTH PARAMETERS *****
		readBondTypeSection(br);

		// Section 5: ***** INPUT FOR BOND ANGLE PARAMETERS *****
		readAngleTypeSection(br);

		// Section 6: ***** INPUT FOR DIHEDRAL PARAMETERS *****
		readProperDihedralTypeSection(br);

		// Section 7: ***** INPUT FOR IMPROPER DIHEDRAL PARAMETERS *****
		readImproperDihedralTypeSection(br);

		// Section 8: ***** INPUT FOR H-BOND 10-12 POTENTIAL PARAMETERS *****
		readHbond1012(br);

		// Section 9: **** INPUT FOR EQUIVALENCING ATOM SYMBOLS FOR
		// THE NON-BONDED 6-12 POTENTIAL PARAMETERS *****
		readEquivalenceAtomTypesFor612(br);

		// Section 10: ***** INPUT FOR THE 6-12 POTENTIAL PARAMETERS *****
		line = br.readLine();

		try {

			// String label;
			String kindNB;

			// MOD4 RE
			FortranFormat formatter = new FortranFormat("(A4,6X,A2)");
			ArrayList<Object> lineObjects = formatter.parse(line);

			// label = (String) lineObjects.get(0);
			kindNB = (String) lineObjects.get(1);

			/*
			 * Section 10B: van der Waals radius and the potential well depth
			 * parameters are read
			 */
			if (kindNB.contentEquals("RE")) {
				read612Parameters(br);
			} else {
				System.out.println("Unsupported NonBonded Type: kindNB = "
						+ kindNB);
			}

		} catch (ParseException e) {
			System.out.println(line);
			e.printStackTrace();
		}

		// close the BufferedReaderer
		br.close();

	}

	/**
	 * Append parameter information from a frcmod file stream.
	 * 
	 * AMBER frcmod (Force field parameter modification file) file format.
	 * <p>
	 * Please see http://ambermd.org/formats.html#frcmod for more information.
	 * 
	 * @param is
	 *            Inputstream containing frcmod file
	 * @throws IOException
	 * @throws ParseException
	 */
	public void readFrcmod(InputStream is) throws IOException, ParseException {
		if (is == null) {
			throw new RuntimeException("readFrcmod: InputStream is null");
		}

		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		String line;
		String words[];

		// Title
		setTitle(br.readLine());

		// Capture MASS line
		line = br.readLine();
		words = line.split("\\.");

		LOG.debug(words[0]);

		if (!(words[0].matches("^MASS"))) {

			throw new RuntimeException("Expecting MASS term");
		}

		// Atom Types
		readAtomTypeSection(br);

		// Capture BOND line
		line = br.readLine();
		words = line.split("\\.");

		LOG.debug(words[0]);

		if (!(words[0].matches("^BOND"))) {

			throw new RuntimeException("Expecting BOND term");
		}

		// BOND parameters
		readBondTypeSection(br);

		// Capture ANGLE line
		line = br.readLine();
		words = line.split("\\.");

		LOG.debug(words[0]);

		if (!(words[0].matches("^ANGLE"))) {

			throw new RuntimeException("Expecting ANGLE term");
		}

		// Angle Parameters
		readAngleTypeSection(br);

		// Capture Dihedral line
		line = br.readLine();
		words = line.split("\\.");

		LOG.debug(words[0]);

		if (!(words[0].matches("^DIHE.*"))) {

			throw new RuntimeException("Expecting DIHEDRAL term");
		}

		readProperDihedralTypeSectionFreeForm(br);

		// Capture IMPROPER line
		line = br.readLine();
		words = line.split("\\.");

		LOG.debug(words[0]);

		if (!(words[0].matches("^IMPROPER"))) {

			throw new RuntimeException("Expecting IMPROPER term");
		}

		// TODO this is going to need a Broken reader like above.
		readImproperDihedralTypeSection(br);

		// Capture NONBON line
		line = br.readLine();
		words = line.split("\\.");

		LOG.debug(words[0]);

		if (!(words[0].matches("^NONBON"))) {

			throw new RuntimeException("Expecting NONBON term");
		}
		// TODO; Assumption here that type is RE

		read612ParametersFreeForm(br);

		// close the BufferedReaderer
		br.close();

	}

	public String getTitle() {
		return title;
	}

	void setTitle(String title) {
		this.title = title;
	}

	public int getAtomTypesSize() {
		return atomTypes.size();
	}

	public ArrayList<AtomIdentifier> getAtomTypes() {
		return atomTypes;
	}

	public AtomType getAtomByType(String type) {

		for (AtomIdentifier atomIdentifier : atomTypes) {
			AtomType atomType = ((AtomType) atomIdentifier);
			if (atomType.getName().equals(type)) {
				return atomType;
			}

		}
		return null;

	}

	public ArrayList<BondType> getBondTypes() {
		return bondTypes;
	}

	public ArrayList<AngleType> getAngleTypes() {
		return angleTypes;
	}

	public ArrayList<DihedralType> getDihedralTypes() {
		return dihedralTypes;
	}

	public ArrayList<ProperDihedralType> getProperDihedralTypes() {

		ArrayList<ProperDihedralType> properDihedralTypes = new ArrayList<ProperDihedralType>();

		for (DihedralType dihedralType : dihedralTypes) {

			if (dihedralType instanceof ProperDihedralType) {

				properDihedralTypes.add((ProperDihedralType) dihedralType);
			}

		}
		return properDihedralTypes;

	}

	public ArrayList<ImproperDihedralType> getImproperDihedralTypes() {

		ArrayList<ImproperDihedralType> improperDihedralTypes = new ArrayList<ImproperDihedralType>();

		for (DihedralType dihedralType : dihedralTypes) {

			if (dihedralType instanceof ImproperDihedralType) {

				improperDihedralTypes.add((ImproperDihedralType) dihedralType);
			}

		}
		return improperDihedralTypes;

	}

	public int getBondTypesSize() {
		return bondTypes.size();
	}

	public int getAngleTypesSize() {
		return angleTypes.size();
	}

	public int getDihedralTypesSize() {
		return dihedralTypes.size();
	}

}

