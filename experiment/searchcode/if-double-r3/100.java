/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 */

/*
 * AUTHORS (In addition to CIT):
 * 2008 Prodevelop S.L  main development
 */

package org.gvsig.normalization.operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.gvsig.normalization.patterns.NormalizationPattern;

import com.iver.cit.gvsig.fmap.layers.XMLException;

public class TestNormAlgorithm extends TestCase {

	private static final Logger log = Logger.getLogger(TestNormAlgorithm.class);

	public void setUp() {
	}

	public void testSplitChain() throws MarshalException, FileNotFoundException, UnsupportedEncodingException, ValidationException, XMLException {

		log.info("TestNormAlgorithm. Test splits strings");

		log.info("SubTest 1");
		String c1 = ";aaa,,bbb;;;ccc/ddd@eee##;";

		File f1 = new File(
				"src-test/org/gvsig/normalization/operations/testdata/patSplitChain.xml");
		assertNotNull(f1);

		List<String> r1 = parser(f1, c1);
		assertNotNull(r1);

		assertEquals("", (String) r1.get(0));
		assertEquals("aaa", (String) r1.get(1));
		assertEquals("", (String) r1.get(2));
		assertEquals("bbb", (String) r1.get(3));
		assertEquals("", (String) r1.get(4));
		assertEquals("", (String) r1.get(5));
		assertEquals("ccc", (String) r1.get(6));
		assertEquals("ddd", (String) r1.get(7));
		assertEquals("eee", (String) r1.get(8));
		assertEquals("", (String) r1.get(9));
		assertEquals(";", (String) r1.get(10));

		log.info("SubTest 2.");
		String c2 = "aaa bbb ccc ddd,76 %";

		File f2 = new File(
				"src-test/org/gvsig/normalization/operations/testdata/patSplitChain2.xml");
		assertNotNull(f2);

		List<String> r2 = parser(f2, c2);
		assertNotNull(r2);

		assertEquals("aaa", (String) r2.get(0));
		assertEquals("bbb", (String) r2.get(1));
		assertEquals("ccc", (String) r2.get(2));
		assertEquals("ddd", (String) r2.get(3));
		assertEquals("76", (String) r2.get(4));
		assertEquals("%", (String) r2.get(5));

		log.info("SubTest 3.");
		String c3 = "Av;Germanias;25;3;Moncada;Valencia";

		File f3 = new File(
				"src-test/org/gvsig/normalization/operations/testdata/patSplitChain3.xml");
		assertNotNull(f3);

		List<String> r3 = parser(f3, c3);
		assertNotNull(r3);

		assertEquals("Av", (String) r3.get(0));
		assertEquals("Germanias", (String) r3.get(1));
		assertEquals(25, Integer.parseInt((String) r3.get(2)));
		assertEquals(3, Integer.parseInt((String) r3.get(3)));
		assertEquals("Moncada", (String) r3.get(4));
		assertEquals("Valencia", (String) r3.get(5));

		log.info("SubTest 4.");
		String c4 = "Av. Germanias      15  2   Moncada   Valencia    ";

		File f4 = new File(
				"src-test/org/gvsig/normalization/operations/testdata/patSplitChain4.xml");
		assertNotNull(f4);

		List<String> r4 = parser(f4, c4);
		assertNotNull(r4);

		assertEquals("Av.", ((String) r4.get(0)).trim());
		assertEquals("Germanias", ((String) r4.get(1)).trim());
		assertEquals(15, Integer.parseInt(((String) r4.get(2)).trim()));
		assertEquals(2, Integer.parseInt(((String) r4.get(3)).trim()));
		assertEquals("Moncada", ((String) r4.get(4)).trim());
		assertEquals("Valencia", ((String) r4.get(5)).trim());

		log.info("SubTest 5.");
		String c5 = "Av;;Germanias;15;;2;Moncada;Valencia";

		File f5 = new File(
				"src-test/org/gvsig/normalization/operations/testdata/patSplitChain5.xml");
		assertNotNull(f5);

		List<String> r5 = parser(f5, c5);
		assertNotNull(r5);

		assertEquals("Av", (String) r5.get(0));
		assertEquals("Germanias", (String) r5.get(1));
		assertEquals(15, Integer.parseInt((String) r5.get(2)));
		assertEquals(2, Integer.parseInt((String) r5.get(3)));
		assertEquals("Moncada", (String) r5.get(4));
		assertEquals("Valencia", (String) r5.get(5));

		log.info("SubTest 6.");
		String c6 = "Av. Germanias 15-2 Moncada (Valencia)";

		File f6 = new File(
				"src-test/org/gvsig/normalization/operations/testdata/patSplitChain6.xml");
		assertNotNull(f6);

		List<String> r6 = parser(f6, c6);
		assertNotNull(r6);

		assertEquals("Av.", ((String) r6.get(0)).trim());
		assertEquals("Germanias", ((String) r6.get(1)).trim());
		assertEquals(15, Integer.parseInt(((String) r6.get(2)).trim()));
		assertEquals(2, Integer.parseInt(((String) r6.get(3)).trim()));
		assertEquals("Moncada", ((String) r6.get(4)).trim());
		assertEquals("Valencia", ((String) r6.get(5)).trim());

		log.info("SubTest 7.");
		String c7 = "Juana Aguirre;Piedras;No 623;Piso2;Dto.4;C1070AAM;Capital Federal;ARGENTINA";

		File f7 = new File(
				"src-test/org/gvsig/normalization/operations/testdata/patSplitChain7.xml");
		assertNotNull(f7);

		List<String> r7 = parser(f7, c7);
		assertNotNull(r7);

		assertEquals("Juana Aguirre", ((String) r7.get(0)).trim());
		assertEquals("Piedras", ((String) r7.get(1)).trim());
		assertEquals("No 623", ((String) r7.get(2)).trim());
		assertEquals("Piso2", ((String) r7.get(3)).trim());
		assertEquals("Dto.4", ((String) r7.get(4)).trim());
		assertEquals("C1070AAM", ((String) r7.get(5)).trim());
		assertEquals("Capital Federal", ((String) r7.get(6)).trim());
		assertEquals("ARGENTINA", ((String) r7.get(7)).trim());

		log.info("SubTest 8.");
		String c8 = "5.548\t5478.254\t0.24578457\t256.21450045";

		File f8 = new File(
				"src-test/org/gvsig/normalization/operations/testdata/patSplitChain8.xml");
		assertNotNull(f8);

		List<String> r8 = parser(f8, c8);
		assertNotNull(r8);

		assertEquals(5.548, Double.parseDouble(((String) r8.get(0)).trim()));
		assertEquals(5478.254, Double.parseDouble(((String) r8.get(1)).trim()));
		assertEquals(0.24578457, Double
				.parseDouble(((String) r8.get(2)).trim()));
		assertEquals(256.21450045, Double.parseDouble(((String) r8.get(3))
				.trim()));

		log.info("TestNormAlgorithm. Test finished");
	}

	public void testSplitFixed() {

		log.info("TestNormAlgorithm. Test tokens fixed");

		String chain = "esto;/es;;; una_prueba;  de un/   split de una cadena_de texto";

		int parts = 4;
		boolean join = true;
		String[] separators = { " " };
		List<String> result = NormAlgorithm.splitChainBySeparators(chain,
				parts, separators, join);
		log.info("Cadena inicial: " + chain);
		for (int i = 0; i < result.size(); i++) {
			log.info("Subcadena" + i + ": " + (String) result.get(i));
		}
		assertEquals("esto;/es;;;", result.get(0));
		assertEquals("una_prueba;", result.get(1));
		assertEquals("de", result.get(2));
		assertEquals("un/   split de una cadena_de texto", result.get(3));

		log.info("TestNormAlgorithm. Test tokens fixed finished");

	}

	public void testSplitSeparators() {

		log.info("TestNormAlgorithm. Test tokens with separators");

		String chain = "esto;/es;;; una_prueba;  de un/   split de una cadena_de texto";

		int parts = 4;
		boolean join = true;
		String[] separators = { " " };
		List<String> result = NormAlgorithm.splitChainBySeparators(chain,
				parts, separators, join);
		System.out.println("Cadena inicial: " + chain);
		for (int i = 0; i < result.size(); i++) {
			System.out.println("Subcadena" + i + ": " + (String) result.get(i));
		}
		assertEquals("esto;/es;;;", result.get(0));
		assertEquals("una_prueba;", result.get(1));
		assertEquals("de", result.get(2));
		assertEquals("un/   split de una cadena_de texto", result.get(3));

		log.info("TestNormAlgorithm. Test tokens with separators finished");

	}

	private List<String> parser(File f, String chain) throws MarshalException, FileNotFoundException, UnsupportedEncodingException, ValidationException, XMLException {
		NormalizationPattern pat = new NormalizationPattern();
		pat.loadFromXML(f);
		NormAlgorithm na = new NormAlgorithm(pat);

		List<String> result = na.splitChain(chain);
		return result;

	}

}

