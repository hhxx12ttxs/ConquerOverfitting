package org.gbif.ecat.parser;

import org.gbif.ecat.model.ParsedName;
import org.gbif.ecat.voc.NameType;
import org.gbif.ecat.voc.NomenclaturalCode;
import org.gbif.ecat.voc.NothoRank;
import org.gbif.ecat.voc.Rank;
import org.gbif.utils.file.FileUtils;
import org.gbif.utils.file.InputStreamUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.regex.Matcher;

import junit.framework.TestCase;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test various scientific and non scientific names to parse.
 * TODO: Add tests for the following names found unparsed in CLB:
 * (?) Physignathus cochinchinensis Guérin, 1829
 * (Acmaeops)
 * (Acmaeops) rufula Gardiner, 1970
 * (Amsel, 1935)
 * (Antopocerus)
 * (Arocephalus) languidus (Flor, 1861)
 * (Athysanella) salsa (Ball & Beamer, 1940)
 * (Erdianus) nigricans (Kirschbaum, 1868)
 * ? Callidium
 * ? Callidium (Phymatodes)
 * ? Callidium (Phymatodes) semicircularis Bland, 1862
 * ? Compsa
 * ? Compsa flavofasciata Martins & Galileo, 2002
 */
public class NameParserTest {

  public static final int CURRENTLY_FAIL = 26;
  public static final int CURRENTLY_FAIL_EXCL_AUTHORS = 19;
  public static final String NAMES_TEST_FILE = "scientific_names.txt";
  public static final String MONOMIALS_FILE = "monomials.txt";
  private final int SLOW = 100; // in milliseconds
  public static final InputStreamUtils streamUtils = new InputStreamUtils();
  private static NameParser parser;

  @BeforeClass
  public static void setup() {
    parser = new NameParser();
    Set<String> mon;
    try {
      mon = FileUtils.streamToSet(streamUtils.classpathStream(MONOMIALS_FILE));
      parser.setMonomials(mon);
      System.out.println("Parser setup. Read " + mon.size() + " known monomials");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void assertHybridFormula(String name) {
    try {
      parser.parse(name);
    } catch (UnparsableException e) {
      assertEquals(NameType.hybrid, e.type);
    }
  }

  private void assertHybridName(ParsedName pn) {
    // System.out.println(pn.canonicalNameWithMarker());
    // System.out.println(pn.fullName());
    assertTrue(NameType.hybrid != pn.type);
    assertTrue(pn.canonicalNameWithMarker().length() > 8);
  }

  private void assertUnparsableType(NameType type, String name) {
    try {
      parser.parse(name);
      fail("Name should be unparsable: " + name);
    } catch (UnparsableException e) {
      assertEquals(type, e.type);
    }
  }

  private void assertUnparsableWithoutType(String name) {
    try {
      parser.parse(name);
      fail("Name should be unparsable: " + name);
    } catch (UnparsableException e) {
      assertNull(e.type);
    }
  }

  private void allowUnparsable(String name) {
    try {
      parser.parse(name);
    } catch (UnparsableException e) {
    }
  }

  // 0scientificName 1genusOrAbove 2infraGeneric 3specificEpithet 4infraSpecificEpithet 5authorship 6year
  // 7bracketAuthorship 8bracketYear 9rank 10nomcode
  // 11nothotype 12nametype
  private ParsedName buildParsedNameFromTabRow(String[] cols) {
    return new ParsedName(null, NomenclaturalCode.fromString(StringUtils.trimToNull(cols[10])),
      NameType.fromString(StringUtils.trimToNull(cols[12])), StringUtils.trimToNull(cols[1]),
      StringUtils.trimToNull(cols[2]), StringUtils.trimToNull(cols[3]), StringUtils.trimToNull(cols[4]),
      NothoRank.fromString(StringUtils.trimToNull(cols[11])), StringUtils.trimToNull(cols[9]),
      StringUtils.trimToNull(cols[5]), StringUtils.trimToNull(cols[6]), StringUtils.trimToNull(cols[7]),
      StringUtils.trimToNull(cols[8]), null, null, null, null);
  }

  private String extractNomNote(String name) {
    String nomNote = null;
    Matcher matcher = NameParser.EXTRACT_NOMSTATUS.matcher(name);
    if (matcher.find()) {
      nomNote = (StringUtils.trimToNull(matcher.group(1)));
    }
    return nomNote;
  }

  @Test
  @Ignore
  public void test4PartedNames() throws Exception {
    parser.debug = false;

    ParsedName n = parser.parse("Bombus sichelii alticola latofasciatus");
    assertEquals("Bombus sichelii infrasubsp. latofasciatus", n.canonicalNameWithMarker());
    assertEquals("Bombus sichelii latofasciatus", n.canonicalName());

    n = parser.parse("Bombus sichelii alticola latofasciatus Vogt, 1909");
    assertEquals("Bombus sichelii infrasubsp. latofasciatus", n.canonicalNameWithMarker());
    assertEquals("Bombus sichelii latofasciatus", n.canonicalName());
  }

  private boolean testAuthorship(String author) {
    Matcher m = NameParser.AUTHOR_TEAM_PATTERN.matcher(author);
    if (m.find()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Avoid NPEs and other exceptions for very short non names and other extremes found in occurrences.
   */
  @Test
  public void testAvoidNPE() throws Exception {
    allowUnparsable(null);
    allowUnparsable(" ");
    allowUnparsable("");
    allowUnparsable("\"");
    allowUnparsable("\\");
    allowUnparsable(".");
    allowUnparsable("a");
    allowUnparsable("'");
    allowUnparsable("von");
    allowUnparsable("X");
    allowUnparsable("@");
    allowUnparsable("&nbsp;");
    allowUnparsable("\"? gryphoidis");
  }

  @Test
  public void testAuthorshipPattern() throws Exception {
    assertTrue(this.testAuthorship("L."));
    assertTrue(this.testAuthorship("Lin."));
    assertTrue(this.testAuthorship("Linné"));
    assertTrue(this.testAuthorship("DC."));
    assertTrue(this.testAuthorship("de Chaudoir"));
    assertTrue(this.testAuthorship("Hilaire"));
    assertTrue(this.testAuthorship("St. Hilaire"));
    assertTrue(this.testAuthorship("Geoffroy St. Hilaire"));
    assertTrue(this.testAuthorship("Acev.-Rodr."));
    assertTrue(this.testAuthorship("Steyerm., Aristeg. & Wurdack"));
    assertTrue(this.testAuthorship("Du Puy & Labat"));
    assertTrue(this.testAuthorship("Baum.-Bod."));
    assertTrue(this.testAuthorship("Engl. & v. Brehmer"));
    assertTrue(this.testAuthorship("F. v. Muell."));
    assertTrue(this.testAuthorship("W.J.de Wilde & Duyfjes"));
    assertTrue(this.testAuthorship("C.E.M.Bicudo"));
    assertTrue(this.testAuthorship("Alves-da-Silva"));
    assertTrue(this.testAuthorship("Alves-da-Silva & C.E.M.Bicudo"));
    assertTrue(this.testAuthorship("Kingdon-Ward"));
    assertTrue(this.testAuthorship("Merr. & L.M.Perry"));
    assertTrue(this.testAuthorship("Calat., Nav.-Ros. & Hafellner"));
    assertTrue(this.testAuthorship("Barboza du Bocage"));
    assertTrue(this.testAuthorship("Arv.-Touv. ex Dörfl."));
    assertTrue(this.testAuthorship("Payri & P.W.Gabrielson"));
    assertTrue(this.testAuthorship("N'Yeurt, Payri & P.W.Gabrielson"));
    assertTrue(this.testAuthorship("VanLand."));
    assertTrue(this.testAuthorship("MacLeish"));
    assertTrue(this.testAuthorship("Monterosato ms."));
    assertTrue(this.testAuthorship("Arn. ms., Grunow"));
    assertTrue(this.testAuthorship("Mosely in Mosely & Kimmins"));
    assertTrue(this.testAuthorship("Choi,J.H.; Im,W.T.; Yoo,J.S.; Lee,S.M.; Moon,D.S.; Kim,H.J.; Rhee,S.K.; Roh,D.H."));
    assertTrue(this.testAuthorship("da Costa Lima"));
    assertTrue(this.testAuthorship("Krapov., W.C.Greg. & C.E.Simpson"));
    assertTrue(this.testAuthorship("de Jussieu"));
    assertTrue(this.testAuthorship("Griseb. ex. Wedd."));
    assertTrue(this.testAuthorship("van-der Land"));
    assertTrue(this.testAuthorship("van der Land"));
    assertTrue(this.testAuthorship("van Helmsick"));
    assertTrue(this.testAuthorship("Xing, Yan & Yin"));
    assertTrue(this.testAuthorship("Xiao & Knoll"));
  }

  @Test
  public void testNotNames() throws Exception {
    parser.debug = false;
    ParsedName pn = parser.parse("Diatrypella favacea var. favacea (Fr.) Ces. & De Not.");
    assertEquals("Diatrypella", pn.genusOrAbove);
    assertEquals("favacea", pn.specificEpithet);
    assertEquals("var.", pn.rank);
    assertEquals("favacea", pn.infraSpecificEpithet);
    assertEquals("Ces. & De Not.", pn.authorship);
    assertEquals("Fr.", pn.bracketAuthorship);

    pn = parser.parse("Protoventuria rosae (De Not.) Berl. & Sacc.");
    assertEquals("Protoventuria", pn.genusOrAbove);
    assertEquals("rosae", pn.specificEpithet);
    assertEquals("Berl. & Sacc.", pn.authorship);
    assertEquals("De Not.", pn.bracketAuthorship);

    pn = parser.parse("Hormospora De Not.");
    assertEquals("Hormospora", pn.genusOrAbove);
    assertEquals("De Not.", pn.authorship);
  }

  @Test
  public void testCanonNameParserSubgenera() throws Exception {
    parser.debug = false;
    ParsedName pn = parser.parse("Polygonum subgen. Bistorta (L.) Zernov");
    assertTrue(pn.notho == null);
    assertEquals("Polygonum", pn.genusOrAbove);
    assertEquals("Bistorta", pn.infraGeneric);
    assertEquals("subgen.", pn.rank);
    assertEquals("Zernov", pn.authorship);
    assertEquals("L.", pn.bracketAuthorship);

    ParsedName n = parser.parse("Arrhoges (Antarctohoges)");
    assertEquals("Arrhoges", n.genusOrAbove);
    assertEquals("Antarctohoges", n.infraGeneric);
    assertTrue(n.rank == null);
    // assertEquals(NomenclaturalCode.Zoological, n.code);
    assertTrue(n.notho == null);

    pn = parser.parse("Festuca subg. Schedonorus (P. Beauv. ) Peterm.");
    assertEquals("Festuca", pn.genusOrAbove);
    assertEquals("Schedonorus", pn.infraGeneric);
    assertEquals("subgen.", pn.rank);
    assertEquals("Peterm.", pn.authorship);
    assertEquals("P. Beauv.", pn.bracketAuthorship);

    n = parser.parse("Catapodium subg.Agropyropsis  Trab.");
    assertEquals("Catapodium", n.genusOrAbove);
    assertEquals("Agropyropsis", n.infraGeneric);
    assertEquals("subgen.", n.rank);

    n = parser.parse(" Gnaphalium subg. Laphangium Hilliard & B. L. Burtt");
    assertEquals("Gnaphalium", n.genusOrAbove);
    assertEquals("Laphangium", n.infraGeneric);
    assertEquals("subgen.", n.rank);

    n = parser.parse("Woodsiaceae (Hooker) Herter");
    assertEquals("Woodsiaceae", n.genusOrAbove);
    assertTrue(n.infraGeneric == null);
    assertTrue(n.rank == null);

  }

  @Test
  public void testClean1() throws Exception {
    assertEquals("", NameParser.preClean(""));
    assertEquals("Hallo Spencer", NameParser.preClean("Hallo Spencer "));
    assertEquals("Hallo Spencer", NameParser.preClean("' 'Hallo Spencer"));
    assertEquals("Hallo Spencer 1982", NameParser.preClean("'\" Hallo  Spencer 1982'"));
  }

  private boolean testCultivar(String cultivar) {
    Matcher m = NameParser.CULTIVAR.matcher(cultivar);
    if (m.find()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @see <a href="http://dev.gbif.org/issues/browse/GBIFCOM-11">GBIFCOM-11</a>
   */
  @Test
  public void testExNames() throws Exception {
    parser.debug = false;

    ParsedName pn = assertExAuthorName(parser.parse("Abies brevifolia cv. ex Dallim."));
    pn = assertExAuthorName(parser.parse("Abies brevifolia hort. ex Dallim."));
    pn = assertExAuthorName(parser.parse("Abutilon bastardioides Baker f. ex Rose"));
    pn = assertExAuthorName(parser.parse("Acacia truncata (Burm. f.) hort. ex Hoffmanns."));
    pn = assertExAuthorName(parser.parse("Anoectocalyx ex Cogn. 'Triana'"));
    pn = assertExAuthorName(parser.parse("Anoectocalyx \"Triana\" ex Cogn.  "));
    pn = assertExAuthorName(parser.parse("Aukuba ex Koehne 'Thunb'   "));
    pn = assertExAuthorName(parser.parse("Crepinella subgen. Marchal ex Oliver  "));
    pn = assertExAuthorName(parser.parse("Echinocereus sect. Triglochidiata ex Bravo"));
    pn = assertExAuthorName(parser.parse("Hadrolaelia sect. Sophronitis ex Chiron & V.P.Castro"));

    pn = assertExAuthorName(parser.parse("Abutilon ×hybridum cv. ex Voss"));
  }

  private ParsedName assertExAuthorName(ParsedName pn) {
    assertFalse("ex".equalsIgnoreCase(pn.specificEpithet));
    assertFalse("ex".equalsIgnoreCase(pn.infraSpecificEpithet));
    assertFalse("ex".equalsIgnoreCase(pn.infraGeneric));
    assertNotNull(pn.genusOrAbove);
    return pn;
  }

  @Test
  public void testCultivarNames() throws Exception {
    parser.debug = false;

    ParsedName pn = parser.parse("Abutilon 'Kentish Belle'");
    assertEquals("Abutilon", pn.genusOrAbove);
    assertEquals(NameType.cultivar, pn.type);

    pn = parser.parse("Abutilon 'Nabob'");
    assertEquals("Abutilon", pn.genusOrAbove);
    assertEquals(NameType.cultivar, pn.type);

    pn = parser.parse("Verpericola megasoma \"Dall\" Pils.");
    assertEquals("Verpericola", pn.genusOrAbove);
    assertEquals("megasoma", pn.specificEpithet);
    assertEquals(NameType.cultivar, pn.type);

    pn = parser.parse("Sorbus americana Marshall cv. 'Belmonte'");
    assertEquals("Sorbus", pn.genusOrAbove);
    assertEquals("americana", pn.specificEpithet);
    assertEquals(NameType.cultivar, pn.type);

    pn = parser.parse("Sorbus hupehensis C.K.Schneid. cv. 'November pink'");
    assertEquals("Sorbus", pn.genusOrAbove);
    assertEquals("hupehensis", pn.specificEpithet);
    assertEquals(NameType.cultivar, pn.type);

    pn = parser.parse("Symphoricarpos albus (L.) S.F.Blake cv. 'Turesson'");
    assertEquals("Symphoricarpos", pn.genusOrAbove);
    assertEquals("albus", pn.specificEpithet);
    assertEquals(NameType.cultivar, pn.type);
    assertEquals(Rank.Cultivar, pn.getRank());

    pn = parser.parse("Symphoricarpos sp. cv. 'mother of pearl'");
    assertEquals("Symphoricarpos", pn.genusOrAbove);
    assertEquals(NameType.cultivar, pn.type);
    assertEquals(Rank.Cultivar, pn.getRank());

  }

  @Test
  public void testCultivarPattern() throws Exception {
    assertTrue(this.testCultivar("'Kentish Belle'"));
    assertTrue(this.testCultivar("'Nabob'"));
    assertTrue(this.testCultivar("\"Dall\""));
    assertTrue(this.testCultivar(" cv. 'Belmonte'"));
    assertTrue(this.testCultivar("Sorbus hupehensis C.K.Schneid. cv. 'November pink'"));
    assertTrue(this.testCultivar("Symphoricarpos albus (L.) S.F.Blake cv. 'Turesson'"));
    assertTrue(this.testCultivar("Symphoricarpos sp. cv. 'mother of pearl'"));

  }

  @Test
  public void testEscaping() throws Exception {
    parser.debug = false;
    assertEquals("Caloplaca poliotera (Nyl.) J. Steiner",
      parser.normalize("Caloplaca poliotera (Nyl.) J. Steiner\\r\\n\\r\\n"));

    ParsedName pn = parser.parse("Caloplaca poliotera (Nyl.) J. Steiner\\r\\n\\r\\n.");
    assertTrue(pn.notho == null);
    assertEquals("Caloplaca", pn.genusOrAbove);
    assertEquals("poliotera", pn.specificEpithet);
    assertEquals("Nyl.", pn.bracketAuthorship);
    assertEquals("J. Steiner", pn.authorship);
  }

  @Test
  public void testHybridFormulas() throws Exception {
    parser.debug = false;
    assertHybridFormula("Arthopyrenia hyalospora X Hydnellum scrobiculatum");
    assertHybridFormula("Arthopyrenia hyalospora (Banker) D. Hall X Hydnellum scrobiculatum D.E. Stuntz");
    assertHybridFormula("Arthopyrenia hyalospora × ? ");
    assertHybridFormula("Agrostis L. × Polypogon Desf. ");
    assertHybridFormula("Agrostis stolonifera L. × Polypogon monspeliensis (L.) Desf. ");
    assertHybridFormula("Asplenium rhizophyllum X A. ruta-muraria E.L. Braun 1939");
    assertHybridFormula("Asplenium rhizophyllum DC. x ruta-muraria E.L. Braun 1939");
    assertHybridFormula("Asplenium rhizophyllum x ruta-muraria");
    assertHybridFormula("Salix aurita L. × S. caprea L.");
    assertHybridFormula("Mentha aquatica L. × M. arvensis L. × M. spicata L.");
    assertHybridFormula("Polypodium vulgare subsp. prionodes (Asch.) Rothm. × subsp. vulgare");
    assertHybridFormula("Tilletia caries (Bjerk.) Tul. × T. foetida (Wallr.) Liro.");

    assertEquals("Polypodium vulgare nothosubsp. mantoniae (Rothm.) Schidlay",
      parser.parse("Polypodium  x vulgare nothosubsp. mantoniae (Rothm.) Schidlay ").fullName());
    assertHybridName(parser.parse("Arthopyrenia hyalospora x "));
  }

  @Test
  public void testHybridAlikeNames() throws Exception {
    parser.debug = false;

    ParsedName n = parser.parse("Huaiyuanella Xing, Yan & Yin, 1984");
    assertNull(n.notho);
    assertEquals("Huaiyuanella", n.genusOrAbove);
    assertNull(n.specificEpithet);
    assertEquals("Xing, Yan & Yin", n.authorship);
    assertEquals("1984", n.year);

    n = parser.parse("Caveasphaera Xiao & Knoll, 2000");
    assertNull(n.notho);
    assertEquals("Caveasphaera", n.genusOrAbove);
    assertNull(n.specificEpithet);
    assertEquals("2000", n.year);

  }

  @Test
  public void testHybridNames() throws Exception {
    parser.debug = false;

    ParsedName n = parser.parse("+ Pyrocrataegus willei L.L.Daniel");
    assertEquals(NothoRank.Generic, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("×Pyrocrataegus willei L.L.Daniel");
    assertEquals(NothoRank.Generic, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertTrue(n.infraSpecificEpithet == null);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse(" × Pyrocrataegus willei L.L.Daniel");
    assertEquals(NothoRank.Generic, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertTrue(n.infraSpecificEpithet == null);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse(" X Pyrocrataegus willei L.L.Daniel");
    assertEquals(NothoRank.Generic, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertTrue(n.infraSpecificEpithet == null);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("Pyrocrataegus ×willei L.L.Daniel");
    assertEquals(NothoRank.Specific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertTrue(n.infraSpecificEpithet == null);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("Pyrocrataegus × willei L.L.Daniel");
    assertEquals(NothoRank.Specific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertTrue(n.infraSpecificEpithet == null);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("Pyrocrataegus x willei L.L.Daniel");
    assertEquals(NothoRank.Specific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertTrue(n.infraSpecificEpithet == null);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("Pyrocrataegus X willei L.L.Daniel");
    assertEquals(NothoRank.Specific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertTrue(n.infraSpecificEpithet == null);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("Pyrocrataegus willei ×libidi L.L.Daniel");
    assertEquals(NothoRank.Infraspecific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertEquals("libidi", n.infraSpecificEpithet);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("×Pyrocrataegus ×willei ×libidi L.L.Daniel");
    assertEquals(NothoRank.Infraspecific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertEquals("libidi", n.infraSpecificEpithet);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("+ Pyrocrataegus willei ×libidi L.L.Daniel");
    assertEquals(NothoRank.Infraspecific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertEquals("libidi", n.infraSpecificEpithet);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("Pyrocrataegus willei nothosubsp. libidi L.L.Daniel");
    assertEquals(NothoRank.Infraspecific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertEquals("libidi", n.infraSpecificEpithet);
    assertEquals("L.L.Daniel", n.authorship);

    n = parser.parse("+ Pyrocrataegus willei nothosubsp. libidi L.L.Daniel");
    assertEquals(NothoRank.Infraspecific, n.notho);
    assertEquals("Pyrocrataegus", n.genusOrAbove);
    assertEquals("willei", n.specificEpithet);
    assertEquals("libidi", n.infraSpecificEpithet);
    assertEquals("L.L.Daniel", n.authorship);
  }

  @Test
  public void testIndetParsering() throws Exception {
    parser.debug = false;
    ParsedName pn = parser.parse("Polygonum spec.");
    assertEquals("Polygonum", pn.genusOrAbove);
    assertNull(pn.specificEpithet);
    assertEquals(Rank.SPECIES, pn.getRank());
    assertEquals(NameType.informal, pn.getType());

    pn = parser.parse("Polygonum vulgaris ssp.");
    assertEquals("Polygonum", pn.genusOrAbove);
    assertEquals("vulgaris", pn.specificEpithet);
    assertEquals(Rank.SUBSPECIES, pn.getRank());
    assertEquals(NameType.informal, pn.getType());

    pn = parser.parse("Mesocricetus sp.");
    assertEquals("Mesocricetus", pn.genusOrAbove);
    assertNull(pn.specificEpithet);
    assertEquals(Rank.SPECIES, pn.getRank());
    assertEquals(NameType.informal, pn.getType());

    // but dont treat these bacterial names as indets
    pn = parser.parse("Bartonella sp. RN, 10623LA");
    assertEquals("Bartonella", pn.genusOrAbove);
    assertEquals(Rank.SPECIES, pn.getRank());
    assertTrue(NameType.informal != pn.getType());

    // and dont treat these authorships as forms
    pn = parser.parse("Dioscoreales Hooker f.");
    assertEquals("Dioscoreales", pn.genusOrAbove);
    assertEquals("Hooker f.", pn.authorship);
    assertEquals(Rank.ORDER, pn.getRank());
    assertTrue(NameType.wellformed == pn.getType());

    pn = parser.parse("Melastoma vacillans Blume var.");
  }

  @Test
  @Ignore
  public void testMonomialRsGbifOrg() throws Exception {
    NameParser np = new NameParser();
    np.readMonomialsRsGbifOrg();
  }

  @Test
  public void testNameFile() throws Exception {
    parser.debug = false;
    System.out.print("\n\nSTARTING FULL PARSER\n");
    Reader reader = FileUtils.getInputStreamReader(streamUtils.classpathStream(NAMES_TEST_FILE));
    LineIterator iter = new LineIterator(reader);

    int parseFails = 0;
    int parseNoAuthors = 0;
    int parseErrors = 0;
    int cnt = 0;
    int lineNum = 0;
    long start = System.currentTimeMillis();

    while (iter.hasNext()) {
      lineNum++;
      String line = iter.nextLine();
      if (lineNum == 1 || line == null || line.startsWith("#") || line.trim().isEmpty()) {
        continue;
      }
      cnt++;
      String[] cols = FileUtils.TAB_DELIMITED.split(line);
      String name = cols[0];
      // scientificName genusOrAbove infraGeneric specificEpithet infraSpecificEpithet authorship year bracketAuthorship
      // bracketYear rank nomcode nothotype
      // nametype
      if (cols.length < 13) {
        System.err.println("Too short line in names test file:");
        System.err.println(line);
      }
      ParsedName expected = buildParsedNameFromTabRow(cols);
      ParsedName n;
      try {
        n = parser.parse(name);
        if (!n.authorsParsed) {
          parseNoAuthors++;
        }
        if (!n.equals(expected)) {
          parseErrors++;
          System.err.println("WRONG\t " + name + "\tEXPECTED: " + expected + "\tPARSED: " + n);
        }
      } catch (Exception e) {
        parseFails++;
        System.err.println("FAIL\t " + name + "\tEXPECTED: " + expected);
      }
    }
    long end = System.currentTimeMillis();
    int successfulParses = cnt - parseFails - parseErrors;
    System.out.println("\n\nNames tested: " + cnt);
    System.out.println("Names success: " + successfulParses);
    System.out.println("Names parse fail: " + parseFails);
    System.out.println("Names parse errors: " + parseErrors);
    System.out.println("Names ignoring authors: " + parseNoAuthors);

    System.out.println("Total time: " + (end - start));
    System.out.println("Average per name: " + (((double) end - start) / cnt));

    if ((parseFails + parseErrors) > CURRENTLY_FAIL) {
      TestCase.fail(
        "We are getting worse, not better. Currently failing: " + (parseFails + parseErrors) + ". Was passing:"
        + CURRENTLY_FAIL);
    } else if (parseFails + parseErrors < CURRENTLY_FAIL) {
      System.out.println("We are getting better! Used to fail : " + CURRENTLY_FAIL);
    }
    if ((parseFails + parseNoAuthors) > CURRENTLY_FAIL_EXCL_AUTHORS) {
      TestCase.fail(
        "We are getting worse, not better. Currently failing ignroing authors: " + (parseFails + parseNoAuthors)
        + ". Was passing:" + CURRENTLY_FAIL_EXCL_AUTHORS);
    } else if (parseFails + parseNoAuthors < CURRENTLY_FAIL_EXCL_AUTHORS) {
      System.out.println("We are getting better! Used to fail without authors: " + CURRENTLY_FAIL_EXCL_AUTHORS);
    }

  }

  @Test
  public void testNameParserCanonical() throws Exception {
    parser.debug = false;

    assertEquals("Abelia grandifolia", parser.parseToCanonical("Abelia grandifolia Villarreal, 2000 Villarreal, 2000"));
    assertEquals("Abelia macrotera",
      parser.parseToCanonical("Abelia macrotera (Graebn. & Buchw.) Rehder (Graebn. et Buchw.) Rehder"));
    assertEquals("Abelia mexicana", parser.parseToCanonical("Abelia mexicana Villarreal, 2000 Villarreal, 2000"));
    assertEquals("Abelia serrata", parser.parseToCanonical("Abelia serrata Abelia serrata Siebold & Zucc."));
    assertEquals("Abelia spathulata colorata", parser.parseToCanonical(
      "Abelia spathulata colorata (Hara & Kurosawa) Hara & Kurosawa (Hara et Kurosawa) Hara et Kurosawa"));
    assertEquals("Abelia tetrasepala",
      parser.parseToCanonical("Abelia tetrasepala (Koidz.) H.Hara & S.Kuros. (Koidz.) H.Hara et S.Kuros."));
    assertEquals("Abelia tetrasepala",
      parser.parseToCanonical("Abelia tetrasepala (Koidz.) Hara & Kurosawa (Koidz.) Hara et Kurosawa"));
    assertEquals("Abelmoschus esculentus",
      parser.parseToCanonical("Abelmoschus esculentus Abelmoschus esculentus (L.) Moench"));
    assertEquals("Abelmoschus moschatus",
      parser.parseToCanonical("Abelmoschus moschatus Abelmoschus moschatus Medik."));
    assertEquals("Abelmoschus moschatus tuberosus",
      parser.parseToCanonical("Abelmoschus moschatus subsp. tuberosus (Span.) Borss.Waalk."));
    assertEquals("Abia fasciata", parser.parseToCanonical("Abia fasciata (Linnaeus, 1758) (Linnaeus, 1758)"));
    assertEquals("Abia fasciata", parser.parseToCanonical("Abia fasciata Linnaeus, 1758 Linnaeus, 1758"));
    assertEquals("Abida secale", parser.parseToCanonical("Abida secale (Draparnaud, 1801) (Draparnaud) , 1801"));
    assertEquals("Abida secale brauniopsis", parser.parseToCanonical("Abida secale brauniopsis Bofill i Poch, Artur"));
    assertEquals("Abida secale inis", parser.parseToCanonical("Abida secale inis Bofill i Poch, Artur"));
    assertEquals("Abida secale meridionalis",
      parser.parseToCanonical("Abida secale meridionalis Martínez Ortí, AlbertoGómez, BenjamínFaci, Guill"));
    assertEquals("Abies alba", parser.parseToCanonical("Abies alba Abies alba Mill."));
    assertEquals("Abies alba", parser.parseToCanonical("Abies alba Abies alba Miller"));
    assertEquals("Abies alba", parser.parseToCanonical("Abies alba Mill. (species) : Mill."));
    assertEquals("Abies amabilis",
      parser.parseToCanonical("Abies amabilis Abies amabilis (Dougl. ex Loud.) Dougl. ex Forbes"));
    assertEquals("Abies amabilis", parser.parseToCanonical("Abies amabilis Abies amabilis Douglas ex Forbes"));
    assertEquals("Abies arizonica", parser.parseToCanonical("Abies arizonica Abies arizonica Merriam"));
    assertEquals("Abies balsamea", parser.parseToCanonical("Abies balsamea (L.) Mill. (species) : (L.) Mill."));
    assertEquals("Abies balsamea", parser.parseToCanonical("Abies balsamea (L.) Mill. / Ledeb."));
    assertEquals("Abies balsamea", parser.parseToCanonical("Abies balsamea Abies balsamea (L.) Mill."));
    assertEquals("Abelia grandiflora", parser.parseToCanonical("Abelia grandiflora Rehd (species) : Rehd"));
  }

  @Test
  public void testAutonyms() throws Exception {
    assertParsedParts("Panthera leo leo (Linnaeus, 1758)", "Panthera", "leo", "leo", null, null, null, "Linnaeus",
      "1758");
    assertParsedParts("Abies alba subsp. alba L.", "Abies", "alba", "alba", "subsp.", "L.");
    //TODO: improve parser to extract autonym authors, http://dev.gbif.org/issues/browse/GBIFCOM-10
    //    assertParsedParts("Abies alba L. subsp. alba", "Abies", "alba", "alba", "subsp.", "L.");
    // this is a wrong name! autonym authors are the species authors, so if both are given they must be the same!
    //    assertParsedParts("Abies alba L. subsp. alba Mill.", "Abies", "alba", "alba", "subsp.", "L.");
  }

  @Test
  public void testNameParserFull() throws Exception {
    parser.debug = false;
    assertParsedParts("Abies alba L.", "Abies", "alba", null, null, "L.");
    assertParsedParts("Abies alba var. kosovo", "Abies", "alba", "kosovo", "var.");
    assertParsedParts("Abies alba subsp. parafil", "Abies", "alba", "parafil", "subsp.");
    assertParsedParts("Abies   alba L. ssp. parafil DC.", "Abies", "alba", "parafil", "subsp.", "DC.");

    assertParsedParts("Nuculoidea behrens var.christoph Williams & Breger [1916]  ", "Nuculoidea", "behrens",
      "christoph", "var.", "Williams & Breger", "1916");
    assertParsedParts(" Nuculoidea Williams et  Breger 1916  ", "Nuculoidea", null, null, null, "Williams & Breger",
      "1916", null, null);
    assertParsedParts("Nuculoidea behrens v.christoph Williams & Breger [1916]  ", "Nuculoidea", "behrens", "christoph",
      "var.", "Williams & Breger", "1916");
    assertParsedParts("Nuculoidea behrens var.christoph Williams & Breger [1916]  ", "Nuculoidea", "behrens",
      "christoph", "var.", "Williams & Breger", "1916");
    assertParsedParts(" Megacardita hornii  calafia", "Megacardita", "hornii", "calafia", null);
    assertParsedParts(" Megacardita hornii  calafia", "Megacardita", "hornii", "calafia", null);
    assertParsedParts(" A. anthophora acervorum", "A.", "anthophora", "acervorum", null);
    assertParsedParts(" x Festulolium nilssonii Cugnac & A. Camus", "Festulolium", null, "nilssonii", null, null,
      NothoRank.Generic, "Cugnac & A. Camus", null, null, null, null);
    assertParsedParts("x Festulolium nilssonii Cugnac & A. Camus", "Festulolium", null, "nilssonii", null, null,
      NothoRank.Generic, "Cugnac & A. Camus", null, null, null, null);
    assertParsedParts("Festulolium x nilssonii Cugnac & A. Camus", "Festulolium", null, "nilssonii", null, null,
      NothoRank.Specific, "Cugnac & A. Camus", null, null, null, null);

    assertParsedParts("Ges Klaus 1895", "Ges", null, null, null, "Klaus", "1895", null, null);
    assertParsedParts("Ge Nicéville 1895", "Ge", null, null, null, "Nicéville", "1895", null, null);
    assertParsedParts("dicnemus capensis", "dicnemus", "capensis", null, null, null);
    assertParsedParts("Zophosis persis (Chatanay, 1914)", "Zophosis", "persis", null, null, null, null, "Chatanay",
      "1914");
    assertParsedParts("Caulerpa cupressoides forma nuda", "Caulerpa", "cupressoides", "nuda", "f.", null);
    assertParsedParts("Agalinis purpurea (L.) Briton var. borealis (Berg.) Peterson 1987", "Agalinis", "purpurea",
      "borealis", "var.", "Peterson", "1987", "Berg.", null);
    assertParsedParts("Agaricus squamula Berk. & M. A. Curtis 1860", "Agaricus", "squamula", null, null,
      "Berk. & M. A. Curtis", "1860", null, null);
    assertParsedParts("Agaricus squamula Berk. & M.A. Curtis 1860", "Agaricus", "squamula", null, null,
      "Berk. & M.A. Curtis", "1860", null, null);
    assertParsedParts("Cladoniicola staurospora Diederich, van den Boom & Aptroot 2001", "Cladoniicola", "staurospora",
      null, null, "Diederich, van den Boom & Aptroot", "2001", null, null);
    assertParsedParts(" Pompeja psorica Herrich-Schöffer [1854]", "Pompeja", "psorica", null, null, "Herrich-Schöffer",
      "1854", null, null);
    assertParsedParts(" Gloveria sphingiformis Barnes & McDunnough 1910", "Gloveria", "sphingiformis", null, null,
      "Barnes & McDunnough", "1910", null, null);
    assertParsedParts("Gastromega badia Saalmüller 1877/78", "Gastromega", "badia", null, null, "Saalmüller", "1877/78",
      null, null);
    assertParsedParts("Hasora coulteri Wood-Mason & de Nicóville 1886", "Hasora", "coulteri", null, null,
      "Wood-Mason & de Nicóville", "1886");
    assertParsedParts("Pithauria uma De Nicóville 1888", "Pithauria", "uma", null, null, "De Nicóville", "1888");
    assertParsedParts(" Lepidostoma quila Bueno-Soria & Padilla-Ramirez 1981", "Lepidostoma", "quila", null, null,
      "Bueno-Soria & Padilla-Ramirez", "1981");
    assertParsedParts(" Dinarthrum inerme McLachlan 1878", "Dinarthrum", "inerme", null, null, "McLachlan", "1878");

    assertParsedParts(" Triplectides tambina Mosely in Mosely & Kimmins 1953", "Triplectides", "tambina", null, null,
      "Mosely in Mosely & Kimmins", "1953");
    assertParsedParts(" Oxyothespis sudanensis Giglio-Tos 1916", "Oxyothespis", "sudanensis", null, null, "Giglio-Tos",
      "1916");
    assertParsedParts(" Parastagmatoptera theresopolitana (Giglio-Tos 1914)", "Parastagmatoptera", "theresopolitana",
      null, null, null, null, "Giglio-Tos", "1914");
    assertParsedParts(" Oxyothespis nilotica nilotica Giglio-Tos 1916", "Oxyothespis", "nilotica", "nilotica", null,
      "Giglio-Tos", "1916");
    assertParsedParts(" Photina Cardioptera burmeisteri (Westwood 1889)", "Photina", "burmeisteri", null, null, null,
      null, "Westwood", "1889");
    assertParsedParts(" Syngenes inquinatus (Gerstaecker, [1885])", "Syngenes", "inquinatus", null, null, null, null,
      "Gerstaecker", "1885");
    assertParsedParts(" Myrmeleon libelloides var. nigriventris A. Costa, [1855]", "Myrmeleon", "libelloides",
      "nigriventris", "var.", "A. Costa", "1855");
    assertParsedParts("Ascalaphus nigripes (van der Weele, [1909])", "Ascalaphus", "nigripes", null, null, null, null,
      "van der Weele", "1909");
    assertParsedParts(" Ascalaphus guttulatus A. Costa, [1855]", "Ascalaphus", "guttulatus", null, null, "A. Costa",
      "1855");
    assertParsedParts("Dichochrysa medogana (C.-K. Yang et al in Huang et al. 1988)", "Dichochrysa", "medogana", null,
      null, null, null, "C.-K. Yang et al. in Huang et al.", "1988");
    assertParsedParts(" Dichochrysa vitticlypea (C.-K. Yang & X.-X. Wang 1990)", "Dichochrysa", "vitticlypea", null,
      null, null, null, "C.-K. Yang & X.-X. Wang", "1990");
    assertParsedParts(" Dichochrysa qingchengshana (C.-K. Yang et al. 1992)", "Dichochrysa", "qingchengshana", null,
      null, null, null, "C.-K. Yang et al.", "1992");
    assertParsedParts(" Colomastix tridentata LeCroy 1995", "Colomastix", "tridentata", null, null, "LeCroy", "1995");
    assertParsedParts(" Sunamphitoe pelagica (H. Milne Edwards 1830)", "Sunamphitoe", "pelagica", null, null, null,
      null, "H. Milne Edwards", "1830");

    // TO BE CONTINUED
    assertParsedParts(" Brotogeris jugularis (Statius Muller 1776)", "Brotogeris", "jugularis", null, null, null, null,
      "Statius Muller", "1776");
    assertParsedParts(" Coracopsis nigra sibilans Milne-Edwards & OuStalet 1885", "Coracopsis", "nigra", "sibilans",
      null, "Milne-Edwards & OuStalet", "1885");
    assertParsedParts(" Trichoglossus haematodus deplanchii J. Verreaux & Des Murs 1860", "Trichoglossus", "haematodus",
      "deplanchii", null, "J. Verreaux & Des Murs", "1860");
    assertParsedParts(" Nannopsittaca dachilleae O'Neill, Munn & Franke 1991", "Nannopsittaca", "dachilleae", null,
      null, "O'Neill, Munn & Franke", "1991");
    assertParsedParts(" Ramphastos brevis Meyer de Schauensee 1945", "Ramphastos", "brevis", null, null,
      "Meyer de Schauensee", "1945");
    assertParsedParts(" Touit melanonota (Wied-Neuwied 1820)", "Touit", "melanonota", null, null, null, null,
      "Wied-Neuwied", "1820");
    assertParsedParts(" Trachyphonus darnaudii (Prevost & Des Murs 1847)", "Trachyphonus", "darnaudii", null, null,
      null, null, "Prevost & Des Murs", "1847");
    assertParsedParts(" Anolis porcatus aracelyae PEREZ-BEATO 1996", "Anolis", "porcatus", "aracelyae", null,
      "Perez-Beato", "1996");
    assertParsedParts(" Luzonichthys taeniatus Randall & McCosker 1992", "Luzonichthys", "taeniatus", null, null,
      "Randall & McCosker", "1992");
    assertParsedParts("Actinia stellula Hemprich and Ehrenberg in Ehrenberg 1834", "Actinia", "stellula", null, null,
      "Hemprich & Ehrenberg in Ehrenberg", "1834");
    assertParsedParts("Anemonia vagans (Less.) Milne Edw.", "Anemonia", "vagans", null, null, "Milne Edw.", null,
      "Less.", null);
    assertParsedParts("Epiactis fecunda (Verrill 1899b)", "Epiactis", "fecunda", null, null, null, null, "Verrill",
      "1899b");
    assertParsedParts(" Pseudocurimata Fernandez-Yepez 1948", "Pseudocurimata", null, null, null, "Fernandez-Yepez",
      "1948");
    assertParsedParts(" Hershkovitzia Guimarăes & d'Andretta 1957", "Hershkovitzia", null, null, null,
      "Guimarăes & d'Andretta", "1957");
    assertParsedParts(" Plectocolea (Mitten) Mitten in B.C. Seemann 1873", "Plectocolea", null, null, null,
      "Mitten in B.C. Seemann", "1873", "Mitten", null);
    assertParsedParts(" Discoporella d'Orbigny 1852", "Discoporella", null, null, null, "d'Orbigny", "1852");
    assertParsedParts(" Acripeza Guérin-Ménéville 1838", "Acripeza", null, null, null, "Guérin-Ménéville", "1838");
    assertParsedParts(" Subpeltonotus Swaraj Ghai, Kailash Chandra & Ramamurthy 1988", "Subpeltonotus", null, null,
      null, "Swaraj Ghai, Kailash Chandra & Ramamurthy", "1988");
    assertParsedParts(" Boettcherimima De Souza Lopes 1950", "Boettcherimima", null, null, null, "De Souza Lopes",
      "1950");
    assertParsedParts(" Surnicou Des Murs 1853", "Surnicou", null, null, null, "Des Murs", "1853");
    assertParsedParts(" Cristocypridea Hou MS. 1977", "Cristocypridea", null, null, null, "Hou MS.", "1977");
    assertParsedParts("Lecythis coriacea DC.", "Lecythis", "coriacea", null, null, "DC.");
    assertParsedParts(" Anhuiphyllum Yu Xueguang 1991", "Anhuiphyllum", null, null, null, "Yu Xueguang", "1991");
    assertParsedParts(" Zonosphaeridium minor Tian Chuanrong 1983", "Zonosphaeridium", "minor", null, null,
      "Tian Chuanrong", "1983");
    assertParsedParts(" Oscarella microlobata Muricy, Boury-Esnault, Bézac & Vacelet 1996", "Oscarella", "microlobata",
      null, null, "Muricy, Boury-Esnault, Bézac & Vacelet", "1996");
    assertParsedParts(" Neoarctus primigenius Grimaldi de Zio, D'Abbabbo Gallo & Morone de Lucia 1992", "Neoarctus",
      "primigenius", null, null, "Grimaldi de Zio, D'Abbabbo Gallo & Morone de Lucia", "1992");
    assertParsedParts(" Phaonia wenshuiensis Zhang, Zhao Bin & Wu 1985", "Phaonia", "wenshuiensis", null, null,
      "Zhang, Zhao Bin & Wu", "1985");
    assertParsedParts(" Heteronychia helanshanensis Han, Zhao-Gan & Ye 1985", "Heteronychia", "helanshanensis", null,
      null, "Han, Zhao-Gan & Ye", "1985");
    assertParsedParts(" Solanophila karisimbica ab. fulvicollis Mader 1941", "Solanophila", "karisimbica",
      "fulvicollis", "ab.", "Mader", "1941");
    assertParsedParts(" Tortrix Heterognomon aglossana Kennel 1899", "Tortrix", "aglossana", null, null, "Kennel",
      "1899");
    assertParsedParts(" Leptochilus (Neoleptochilus) beaumonti Giordani Soika 1953", "Leptochilus", "beaumonti", null,
      null, "Giordani Soika", "1953");
    assertParsedParts(" Lutzomyia (Helcocyrtomyia) rispaili Torres-Espejo, Caceres & le Pont 1995", "Lutzomyia",
      "rispaili", null, null, "Torres-Espejo, Caceres & le Pont", "1995");
    assertParsedParts("Gastropacha minima De Lajonquiére 1979", "Gastropacha", "minima", null, null, "De Lajonquiére",
      "1979");
    assertParsedParts("Lithobius elongipes Chamberlin (1952)", "Lithobius", "elongipes", null, null, null, null,
      "Chamberlin", "1952");
    assertParsedParts("Maxillaria sect. Multiflorae Christenson", "Maxillaria", null, null, "sect.", "Christenson");
    assertParsedParts("Maxillaria allenii L.O.Williams in Woodson & Schery", "Maxillaria", "allenii", null, null,
      "L.O.Williams in Woodson & Schery");
    assertParsedParts("Masdevallia strumosa P.Ortiz & E.Calderón", "Masdevallia", "strumosa", null, null,
      "P.Ortiz & E.Calderón");
    assertParsedParts("Neobisium (Neobisium) carcinoides balcanicum Hadi 1937", "Neobisium", "carcinoides",
      "balcanicum", null, "Hadi", "1937");
    assertParsedParts("Nomascus concolor subsp. lu Delacour, 1951", "Nomascus", "concolor", "lu", "subsp.", "Delacour",
      "1951");
    assertParsedParts("Polygonum subgen. Bistorta (L.) Zernov", "Polygonum", null, null, "subgen.", "Zernov", null,
      "L.", null);
    assertParsedParts("Stagonospora polyspora M.T. Lucas & Sousa da Câmara, 1934", "Stagonospora", "polyspora", null,
      null, "M.T. Lucas & Sousa da Câmara", "1934");

    assertParsedParts("Euphorbiaceae de Jussieu, 1789", "Euphorbiaceae", null, null, null, "de Jussieu", "1789");
    assertParsedParts("Leucanitis roda Herrich-Schäffer (1851) 1845", "Leucanitis", "roda", null, null, null, "1845",
      "Herrich-Schäffer", "1851");

    ParsedName pn = parser.parse("Loranthus incanus Schumach. & Thonn. subsp. sessilis Sprague");
    assertEquals("Loranthus", pn.getGenusOrAbove());
    assertEquals("incanus", pn.getSpecificEpithet());
    assertEquals("sessilis", pn.getInfraSpecificEpithet());
    assertEquals("Sprague", pn.getAuthorship());

    pn = parser.parse("Mascagnia brevifolia  var. paniculata Nied.,");
    assertEquals("Mascagnia", pn.getGenusOrAbove());
    assertEquals("brevifolia", pn.getSpecificEpithet());
    assertEquals("paniculata", pn.getInfraSpecificEpithet());
    assertEquals("Nied.", pn.getAuthorship());
    assertEquals(Rank.VARIETY, pn.getRank());

    pn = parser.parse("Leveillula jaczewskii U. Braun (ined.)");
    assertEquals("Leveillula", pn.getGenusOrAbove());
    assertEquals("jaczewskii", pn.getSpecificEpithet());
    assertEquals("U. Braun", pn.getAuthorship());
    assertEquals(Rank.SPECIES, pn.getRank());

    pn = parser.parse("Heteropterys leschenaultiana fo. ovata Nied.");
    assertEquals("Heteropterys", pn.getGenusOrAbove());
    assertEquals("leschenaultiana", pn.getSpecificEpithet());
    assertEquals("ovata", pn.getInfraSpecificEpithet());
    assertEquals("Nied.", pn.getAuthorship());
    assertEquals(Rank.Form, pn.getRank());

    pn = parser.parse("Cymbella mendosa f. apiculata (Hust.) VanLand.");
    assertEquals("Cymbella", pn.getGenusOrAbove());
    assertEquals("mendosa", pn.getSpecificEpithet());
    assertEquals("apiculata", pn.getInfraSpecificEpithet());
    assertEquals("VanLand.", pn.getAuthorship());
    assertEquals(Rank.Form, pn.getRank());

    pn = parser.parse("Lasioglossum channelense McGinley, 1986");
    assertEquals("Lasioglossum", pn.getGenusOrAbove());
    assertEquals("channelense", pn.getSpecificEpithet());
    assertEquals("McGinley", pn.getAuthorship());
    assertEquals("1986", pn.getYear());
    assertEquals(Rank.SPECIES, pn.getRank());

    pn = parser.parse("Liolaemus hermannunezi PINCHEIRA-DONOSO, SCOLARO & SCHULTE 2007");
    assertEquals("Liolaemus", pn.getGenusOrAbove());
    assertEquals("hermannunezi", pn.getSpecificEpithet());
    assertEquals("Pincheira-Donoso, Scolaro & Schulte", pn.getAuthorship());
    assertEquals("2007", pn.getYear());
    assertEquals(Rank.SPECIES, pn.getRank());

    pn = parser.parse("Liolaemus hermannunezi Pincheira-Donoso, Scolaro & Schulte, 2007");
    assertEquals("Liolaemus", pn.getGenusOrAbove());
    assertEquals("hermannunezi", pn.getSpecificEpithet());
    assertEquals("Pincheira-Donoso, Scolaro & Schulte", pn.getAuthorship());
    assertEquals("2007", pn.getYear());
    assertEquals(Rank.SPECIES, pn.getRank());

    pn = parser.parse("Pseudoeryx relictualis SCHARGEL, RIVAS-FUENMAYOR, BARROS & P.FAUR 2007");
    assertEquals("Pseudoeryx", pn.getGenusOrAbove());
    assertEquals("relictualis", pn.getSpecificEpithet());
    assertEquals("Schargel, Rivas-Fuenmayor, Barros & P.Faur", pn.getAuthorship());
    assertEquals("2007", pn.getYear());
    assertEquals(Rank.SPECIES, pn.getRank());

    pn = parser.parse("Cyrtodactylus phongnhakebangensis ZIEGLER, RÖSLER, HERRMANN & THANH 2003");
    assertEquals("Cyrtodactylus", pn.getGenusOrAbove());
    assertEquals("phongnhakebangensis", pn.getSpecificEpithet());
    assertEquals("Ziegler, Rösler, Herrmann & Thanh", pn.getAuthorship());
    assertEquals("2003", pn.getYear());
    assertEquals(Rank.SPECIES, pn.getRank());

    pn = parser.parse(
      "Cnemidophorus mumbuca Colli, Caldwell, Costa, Gainsbury, Garda, Mesquita, Filho, Soares, Silva, Valdujo, Vieira, Vitt, Wer");
    assertEquals("Cnemidophorus", pn.getGenusOrAbove());
    assertEquals("mumbuca", pn.getSpecificEpithet());
    assertEquals("Colli, Caldwell, Costa, Gainsbury, Garda, Mesquita, Filho, Soares, Silva, Valdujo, Vieira, Vitt, Wer",
      pn.getAuthorship());
    assertEquals(Rank.SPECIES, pn.getRank());

    pn = parser.parse(
      "Cnemidophorus mumbuca COLLI, CALDWELL, COSTA, GAINSBURY, GARDA, MESQUITA, FILHO, SOARES, SILVA, VALDUJO, VIEIRA, VITT, WER");
    assertEquals("Cnemidophorus", pn.getGenusOrAbove());
    assertEquals("mumbuca", pn.getSpecificEpithet());
    assertEquals("Colli, Caldwell, Costa, Gainsbury, Garda, Mesquita, Filho, Soares, Silva, Valdujo, Vieira, Vitt, Wer",
      pn.getAuthorship());
    assertEquals(Rank.SPECIES, pn.getRank());

    // TODO: fix this, da Costa Lima should be the author, no epithet
    // assertParsedParts(" Pseudophorellia da Costa Lima 1934", "Pseudophorellia", null, null, null,
    // "da Costa Lima","1934");

  }

  @Test
  public void testUnsupportedAuthors() throws Exception {
    // NOT YET COMPLETELY PARSING THE AUTHOR
    assertParsedParts(" Anolis marmoratus girafus LAZELL 1964: 377", "Anolis", "marmoratus", "girafus", null);
    assertParsedParts(" Chorististium maculatum (non Bloch 1790)", "Chorististium", "maculatum", null, null);
    assertParsedParts(" Pikea lunulata (non Guichenot 1864)", "Pikea", "lunulata", null, null);
    assertParsedParts(" Puntius stoliczkae (non Day 1871)", "Puntius", "stoliczkae", null, null);
    assertParsedParts(" Puntius arulius subsp. tambraparniei (non Silas 1954)", "Puntius", "arulius", "tambraparniei",
      "subsp.", null);
  }

  @Test
  public void testImprintYear() throws Exception {
    assertParsedParts(" Pompeja psorica Herrich-Schöffer [1854]", "Pompeja", "psorica", null, null, "Herrich-Schöffer",
      "1854", null, null);
    assertParsedParts(" Syngenes inquinatus (Gerstaecker, [1885])", "Syngenes", "inquinatus", null, null, null, null,
      "Gerstaecker", "1885");
    assertParsedParts(" Myrmeleon libelloides var. nigriventris A. Costa, [1855]", "Myrmeleon", "libelloides",
      "nigriventris", "var.", "A. Costa", "1855");
    assertParsedParts("Ascalaphus nigripes (van der Weele, [1909])", "Ascalaphus", "nigripes", null, null, null, null,
      "van der Weele", "1909");
    assertParsedParts(" Ascalaphus guttulatus A. Costa, [1855]", "Ascalaphus", "guttulatus", null, null, "A. Costa",
      "1855");
  }

  @Test
  public void testNameParserIssue() throws Exception {
    parser.debug = false;
    // issue42
    ParsedName pn = parser.parse("Abacetus laevicollis de Chaudoir, 1869");
    assertEquals("Abacetus laevicollis", pn.canonicalNameWithMarker());
    assertEquals("de Chaudoir", pn.getAuthorship());
    assertEquals("1869", pn.getYear());
    assertTrue(1869 == pn.getYearInt());
    // issue50
    pn = parser.parse("Deinococcus-Thermus");
    assertEquals("Deinococcus-Thermus", pn.canonicalNameWithMarker());
    assertTrue(pn.getAuthorship() == null);
    assertTrue(pn.getYear() == null);
    // issue51
    pn = parser.parse("Alectis alexandrinus (Geoffroy St. Hilaire, 1817)");
    assertEquals("Alectis alexandrinus", pn.canonicalNameWithMarker());
    assertEquals("Geoffroy St. Hilaire", pn.getBracketAuthorship());
    assertEquals("1817", pn.getBracketYear());
    assertTrue(1817 == pn.getBracketYearInt());
    // issue60
    pn = parser.parse("Euphorbiaceae de Jussieu, 1789");
    assertEquals("Euphorbiaceae", pn.canonicalName());
    assertEquals("Euphorbiaceae", pn.genusOrAbove);
    assertEquals("de Jussieu", pn.getAuthorship());
    assertEquals("1789", pn.getYear());
  }

  @Test
  public void testNameParserRankMarker() throws Exception {
    parser.debug = false;
    assertEquals(Rank.SUBSPECIES, parser.parse("Coccyzuz americanus ssp.").getRank());
    assertEquals(Rank.SUBSPECIES, parser.parse("Coccyzuz ssp").getRank());
    assertEquals(Rank.SPECIES, parser.parse("Asteraceae spec.").getRank());

    ParsedName cn = parser.parse("Asteraceae spec.");
    assertEquals(Rank.SPECIES, cn.getRank());
    assertEquals("sp.", cn.getRankMarker());

    cn = parser.parse("Callideriphus flavicollis morph. reductus Fuchs 1961");
    assertEquals(Rank.InfrasubspecificName, cn.getRank());
    assertEquals("morph.", cn.getRankMarker());

    ParsedName pn = parser.parse("Euphrasia rostkoviana Hayne subvar. campestris (Jord.) Hartl");
    assertEquals(Rank.InfrasubspecificName, cn.getRank());
    assertEquals("subvar.", pn.getRankMarker());
    assertEquals("Euphrasia", pn.getGenusOrAbove());
    assertEquals("rostkoviana", pn.getSpecificEpithet());
    assertEquals("campestris", pn.getInfraSpecificEpithet());
    assertEquals("Hartl", pn.getAuthorship());
    assertEquals("Jord.", pn.getBracketAuthorship());

  }

  /**
   * test true bad occurrence names
   */
  @Test
  public void testNameParserSingleDirty() throws Exception {
    parser.debug = false;
    assertEquals("Verrucaria foveolata", parser.parseToCanonical("Verrucaria foveolata /Flörke) A. Massal."));
    assertEquals("Limatula gwyni", parser.parse("LImatula gwyni	(Sykes, 1903)").canonicalNameWithMarker());
    assertEquals("×Fatshedera lizei", parser.parse("× fatshedera lizei").canonicalNameWithMarker());
    assertEquals("Fatshedera lizei", parser.parse("fatshedera lizei ").canonicalNameWithMarker());
  }

  private ParsedName assertParsedParts(String name, String genus, String subgenus, String epithet, String infraepithet,
    String rank, NothoRank notho, String author, String year, String basAuthor, String basYear, String nomStatus)
    throws UnparsableException {
    ParsedName pn = parser.parse(name);
    assertEquals(genus, pn.getGenusOrAbove());
    assertEquals(epithet, pn.getSpecificEpithet());
    assertEquals(infraepithet, pn.getInfraSpecificEpithet());
    assertEquals(rank, pn.getRankMarker());
    assertEquals(notho, pn.getNotho());
    assertEquals(author, pn.getAuthorship());
    assertEquals(year, pn.getYear());
    assertEquals(basAuthor, pn.getBracketAuthorship());
    assertEquals(basYear, pn.getBracketYear());
    assertEquals(nomStatus, pn.getNomStatus());
    return pn;
  }

  private void assertParsedName(String name, NameType type, String genus, String epithet, String infraepithet,
    String rank) {
    try {
      ParsedName pn =
        assertParsedParts(name, genus, null, epithet, infraepithet, rank, null, null, null, null, null, null);
      assertEquals("Wrong name type", type, pn.type);
    } catch (UnparsableException e) {
      assertEquals("Wrong name type", type, e.type);
    }
  }

  private void assertParsedParts(String name, String genus, String epithet, String infraepithet, String rank,
    String author, String year, String basAuthor, String basYear) throws UnparsableException {
    assertParsedParts(name, genus, null, epithet, infraepithet, rank, null, author, year, basAuthor, basYear, null);
  }

  private void assertParsedParts(String name, String genus, String epithet, String infraepithet, String rank)
    throws UnparsableException {
    assertParsedParts(name, genus, epithet, infraepithet, rank, null, null, null, null);
  }

  private void assertParsedParts(String name, String genus, String epithet, String infraepithet, String rank,
    String author) throws UnparsableException {
    assertParsedParts(name, genus, epithet, infraepithet, rank, author, null, null, null);
  }

  private void assertParsedParts(String name, String genus, String epithet, String infraepithet, String rank,
    String author, String year) throws UnparsableException {
    assertParsedParts(name, genus, epithet, infraepithet, rank, author, year, null, null);
  }

  @Test
  public void testInfragenericRanks() throws Exception {
    parser.debug = false;
    assertEquals("Bodotria subgen. Vertebrata", parser.parse("Bodotria (Vertebrata)").canonicalNameWithMarker());
    assertEquals("Bodotria", parser.parse("Bodotria (Goodsir)").canonicalNameWithMarker());
    assertEquals("Latrunculia subgen. Biannulata", parser.parse("Latrunculia (Biannulata)").canonicalNameWithMarker());
    assertEquals("Chordata subgen. Cephalochordata",
      parser.parse("Chordata (Cephalochordata)").canonicalNameWithMarker());

    assertParsedParts("Saperda (Saperda) candida m. bipunctata Breuning, 1952", "Saperda", "Saperda", "candida",
      "bipunctata", "m.", null, "Breuning", "1952", null, null, null);

    assertParsedParts("Carex section Acrocystis", "Carex", "Acrocystis", null, null, "sect.", null, null, null, null,
      null, null);

    assertParsedParts("Juncus subgenus Alpini", "Juncus", "Alpini", null, null, "subgen.", null, null, null, null, null,
      null);

    assertParsedParts("Solidago subsection Triplinervae", "Solidago", "Triplinervae", null, null, "subsect.", null,
      null, null, null, null, null);

    assertParsedParts("Eleocharis series Maculosae", "Eleocharis", "Maculosae", null, null, "ser.", null, null, null,
      null, null, null);

  }

  @Test
  public void testNameType() throws Exception {
    parser.debug = false;
    assertUnparsableType(NameType.blacklisted, "unknown Plantanus");
    assertUnparsableType(NameType.blacklisted, "Macrodasyida incertae sedis");
    assertUnparsableType(NameType.blacklisted, " uncertain Plantanus");
    assertUnparsableType(NameType.blacklisted, "Unknown Cyanobacteria");
    assertUnparsableType(NameType.blacklisted, "Unknown methanomicrobium (strain EBac)");
    assertUnparsableType(NameType.blacklisted, "Demospongiae incertae sedis");

    assertUnparsableType(NameType.hybrid, "Asplenium rhizophyllum DC. x ruta-muraria E.L. Braun 1939");
    assertUnparsableType(NameType.hybrid, "Agrostis L. × Polypogon Desf. ");

    assertUnparsableType(NameType.virus, "Cactus virus 2");

    assertEquals(NameType.wellformed, parser.parse("Neobisium carcinoides subsp. balcanicum Hadi, 1937").type);
    assertEquals(NameType.wellformed, parser.parse("Festulolium nilssonii Cugnac & A. Camus").type);
    assertEquals(NameType.wellformed, parser.parse("Coccyzuz americanus").type);
    assertEquals(NameType.wellformed, parser.parse("Tavila indeterminata Walker, 1869").type);
    assertEquals(NameType.wellformed, parser.parse("Phyllodonta indeterminata Schaus, 1901").type);
    assertEquals(NameType.wellformed, parser.parse("×Festulolium nilssonii Cugnac & A. Camus").type);

    // not using the multiplication sign, but an simple x
    assertEquals(NameType.sciname, parser.parse("x Festulolium nilssonii Cugnac & A. Camus").type);
    assertEquals(NameType.sciname, parser.parse("xFestulolium nilssonii Cugnac & A. Camus").type);
    assertEquals(NameType.sciname, parser.parse("nuculoidea behrens subsp. behrens var.christoph").type);
    assertEquals(NameType.sciname, parser.parse("Neobisium (Neobisium) carcinoides balcanicum Hadi 1937").type);
    assertEquals(NameType.sciname, parser.parse("Neobisium carcinoides ssp. balcanicum Hadi, 1937").type);
    assertEquals(NameType.sciname,
      parser.parse("Valsa hypodermia sensu Berkeley & Broome (Not. Brit. Fungi no., 862)").type);
    assertEquals(NameType.sciname, parser.parse("Solanum aculeatissimum auct. not Jacq.").type);

    assertEquals(NameType.informal, parser.parse("Coccyzuz cf americanus").type);
    assertEquals(NameType.informal, parser.parse("Coccyzuz americanus ssp.").type);
    assertEquals(NameType.informal, parser.parse("Asteraceae spec").type);

    assertEquals(NameType.doubtful, parser.parse("Callitrichia pilosa (Jocquż & Scharff, 1986)").type);
    assertEquals(NameType.doubtful, parser.parse("Sinopoda exspectata Jżger & Ono, 2001").type);
    assertEquals(NameType.doubtful, parser.parse("Callitrichia pilosa 1986 ?").type);
    assertEquals(NameType.doubtful,
      parser.parse("Scaphytopius acutus delongi Young, 1952c:, 248 [n.sp. , not nom. nov.]").type);

  }

  @Test
  public void testUnparsables() throws Exception {
    parser.debug = false;
    assertUnparsableWithoutType("");
    assertUnparsableWithoutType(" ");
    assertUnparsableWithoutType(" .");
    assertUnparsableWithoutType("pilosa 1986 ?");
    assertUnparsableWithoutType("H4N2 subtype");
    assertUnparsableWithoutType("endosymbiont 'C3 71' of Calyptogena sp. JS, 2002");
  }

  @Test
  public void testLowerCaseMonomials() throws Exception {
    parser.debug = false;
    assertUnparsableWithoutType("tree");
    assertUnparsableWithoutType(" tree");
    assertUnparsableWithoutType("tim");
    assertUnparsableWithoutType("abies");
    assertUnparsableWithoutType("abies (Thats it)");
  }

  @Test
  public void testSensuParsing() throws Exception {
    assertEquals("sensu Baker f.", parser.<Integer>parse("Trifolium repens sensu Baker f.").sensu);
    assertEquals("sensu latu", parser.<Integer>parse("Achillea millefolium sensu latu").sensu);
    assertEquals("sec. Greuter, 2009", parser.<Integer>parse("Achillea millefolium sec. Greuter 2009").sensu);
  }

  @Test
  public void testNomenclaturalNotesPattern() throws Exception {
    assertEquals("nom. illeg.", extractNomNote("Vaucheria longicaulis var. bengalensis Islam, nom. illeg."));
    assertEquals("nom. correct", extractNomNote("Dorataspidae nom. correct"));
    assertEquals("nom. transf.", extractNomNote("Ethmosphaeridae nom. transf."));
    assertEquals("nom. ambig.", extractNomNote("Fucus ramosissimus Oeder, nom. ambig."));
    assertEquals("nom. nov.", extractNomNote("Myrionema majus Foslie, nom. nov."));
    assertEquals("nom. utique rej.", extractNomNote("Corydalis bulbosa (L.) DC., nom. utique rej."));
    assertEquals("nom. cons. prop.", extractNomNote("Anthoceros agrestis var. agrestis Paton nom. cons. prop."));
    assertEquals("nom. superfl.",
      extractNomNote("Lithothamnion glaciale forma verrucosum (Foslie) Foslie, nom. superfl."));
    assertEquals("nom.rejic.",
      extractNomNote("Pithecellobium montanum var. subfalcatum (Zoll. & Moritzi)Miq., nom.rejic."));
    assertEquals("nom. inval",
      extractNomNote("Fucus vesiculosus forma volubilis (Goodenough & Woodward) H.T. Powell, nom. inval"));
    assertEquals("nom. nud.", extractNomNote("Sao hispanica R. & E. Richter nom. nud. in Sampelayo 1935"));
    assertEquals("nom.illeg.", extractNomNote("Hallo (nom.illeg.)"));
    assertEquals("nom. super.", extractNomNote("Calamagrostis cinnoides W. Bart. nom. super."));
    assertEquals("nom. nud.", extractNomNote("Iridaea undulosa var. papillosa Bory de Saint-Vincent, nom. nud."));
    assertEquals("nom. inval",
      extractNomNote("Sargassum angustifolium forma filiforme V. Krishnamurthy & H. Joshi, nom. inval"));
    assertEquals("nomen nudum", extractNomNote("Solanum bifidum Vell. ex Dunal, nomen nudum"));
    assertEquals("nomen invalid.",
      extractNomNote("Schoenoplectus ×scheuchzeri (Bruegger) Palla ex Janchen, nomen invalid."));
    assertEquals("nom. nud.",
      extractNomNote("Cryptomys \"Kasama\" Kawalika et al., 2001, nom. nud. (Kasama, Zambia) ."));
    assertEquals("nom. super.", extractNomNote("Calamagrostis cinnoides W. Bart. nom. super."));
    assertEquals("nom. dub.", extractNomNote("Pandanus odorifer (Forssk.) Kuntze, nom. dub."));
    assertEquals("nom. rejic.", extractNomNote("non Clarisia Abat, 1792, nom. rejic."));
    assertEquals("nom. cons", extractNomNote(
      "Yersinia pestis (Lehmann and Neumann, 1896) van Loghem, 1944 (Approved Lists, 1980) , nom. cons"));
    assertEquals("nom. rejic.",
      extractNomNote("\"Pseudomonas denitrificans\" (Christensen, 1903) Bergey et al., 1923, nom. rejic."));
    assertEquals("nom. nov.", extractNomNote("Tipula rubiginosa Loew, 1863, nom. nov."));
    assertEquals("nom. prov.", extractNomNote("Amanita pruittii A.H.Sm. ex Tulloss & J.Lindgr., nom. prov."));
    assertEquals("nom. cons.", extractNomNote("Ramonda Rich., nom. cons."));
    assertEquals("nom. cons.", extractNomNote(
      "Kluyver and van Niel, 1936 emend. Barker, 1956 (Approved Lists, 1980) , nom. cons., emend. Mah and Kuhn, 1984"));
    assertEquals("nom. superfl.", extractNomNote("Coccocypselum tontanea (Aubl.) Kunth, nom. superfl."));
    assertEquals("nom. ambig.", extractNomNote("Lespedeza bicolor var. intermedia Maxim. , nom. ambig."));
    assertEquals("nom. praeoccup.", extractNomNote("Erebia aethiops uralensis Goltz, 1930 nom. praeoccup."));

    assertEquals("comb. nov. ined.", extractNomNote("Ipomopsis tridactyla (Rydb.) Wilken, comb. nov. ined."));
    assertEquals("sp. nov. ined.", extractNomNote("Orobanche riparia Collins, sp. nov. ined."));
    assertEquals("gen. nov.", extractNomNote("Anchimolgidae gen. nov. New Caledonia-Rjh-, 2004"));
    assertEquals("gen. nov. ined.", extractNomNote("Stebbinsoseris gen. nov. ined."));
    assertEquals("var. nov.", extractNomNote("Euphorbia rossiana var. nov. Steinmann, 1199"));

  }

  @Test
  public void testNormalizeName() {
    assertEquals("Nuculoidea Williams et Breger, 1916", NameParser.normalize("Nuculoidea Williams et  Breger 1916  "));
    assertEquals("Nuculoidea behrens var. christoph Williams & Breger [1916]",
      NameParser.normalize("Nuculoidea behrens var.christoph Williams & Breger [1916]  "));
    assertEquals("Nuculoidea behrens var. christoph Williams & Breger [1916]",
      NameParser.normalize("Nuculoidea behrens var.christoph Williams & Breger [1916]  "));
    assertEquals(NameParser.normalize("Nuculoidea Williams & Breger, 1916  "),
      NameParser.normalize("Nuculoidea   Williams& Breger, 1916"));
    assertEquals("Asplenium ×inexpectatum (E.L. Braun, 1940) Morton (1956)",
      NameParser.normalize("Asplenium X inexpectatum (E.L. Braun 1940)Morton (1956) "));
    assertEquals("×Agropogon", NameParser.normalize(" × Agropogon"));
    assertEquals("Salix ×capreola Andersson", NameParser.normalize("Salix × capreola Andersson"));
    assertEquals("Leucanitis roda Herrich-Schäffer (1851), 1845",
      NameParser.normalize("Leucanitis roda Herrich-Schäffer (1851) 1845"));

    assertEquals("Huaiyuanella Xing, Yan & Yin, 1984", NameParser.normalize("Huaiyuanella Xing, Yan&Yin, 1984"));

  }

  @Test
  public void testNormalizeStrongName() {
    assertEquals("Nuculoidea Williams & Breger, 1916",
      NameParser.normalize(NameParser.normalizeStrong("Nuculoidea Williams et  Breger 1916  ")));
    assertEquals("Nuculoidea behrens var. christoph Williams & Breger, 1916",
      NameParser.normalize(NameParser.normalizeStrong("Nuculoidea behrens var.christoph Williams & Breger [1916]  ")));
    assertEquals("N. behrens Williams & Breger, 1916",
      NameParser.normalize(NameParser.normalizeStrong("  N.behrens Williams &amp;  Breger , 1916  ")));
    assertEquals("Nuculoidea Williams & Breger, 1916",
      NameParser.normalize(NameParser.normalizeStrong(" 'Nuculoidea Williams & Breger, 1916'")));
    assertEquals("Malacocarpus schumannianus (Nicolai, 1893) Britton & Rose",
      NameParser.normalize(NameParser.normalizeStrong("Malacocarpus schumannianus (Nicolai (1893)) Britton & Rose")));
    assertEquals("Photina (Cardioptera) burmeisteri (Westwood, 1889)",
      NameParser.normalize(NameParser.normalizeStrong("Photina Cardioptera burmeisteri (Westwood 1889)")));
    assertEquals("Suaeda forsskahlei Schweinf.",
      NameParser.normalize(NameParser.normalizeStrong("Suaeda forsskahlei Schweinf. ms.")));
    assertEquals("Acacia bicolor Bojer", NameParser.normalize(NameParser.normalizeStrong("Acacia bicolor Bojer ms.")));

    assertEquals("Leucanitis roda (Herrich-Schäffer, 1851), 1845",
      NameParser.normalize(NameParser.normalizeStrong("Leucanitis roda Herrich-Schäffer (1851) 1845")));
  }

  @Test
  public void testOccNameFile() throws Exception {
    parser.debug = false;
    Reader reader = FileUtils.getInputStreamReader(streamUtils.classpathStream("parseNull.txt"));
    LineIterator iter = new LineIterator(reader);

    int parseFails = 0;
    int parseAuthorless = 0;
    int lineNum = 0;
    long start = System.currentTimeMillis();

    while (iter.hasNext()) {
      lineNum++;
      String name = iter.nextLine();
      ParsedName n;
      try {
        n = parser.parse(name);
        if (!n.authorsParsed) {
          parseAuthorless++;
          System.err.println("NO AUTHORS\t " + name);
        }
      } catch (UnparsableException e) {
        parseFails++;
        System.err.println("FAIL\t " + name);
      }
    }
    long end = System.currentTimeMillis();
    System.out.println("\n\nNames tested: " + lineNum);
    System.out.println("Names parse fail: " + parseFails);
    System.out.println("Names parse no authors: " + parseAuthorless);

    System.out.println("Total time: " + (end - start));
    System.out.println("Average per name: " + (((double) end - start) / lineNum));

    int currFail = 2;
    if ((parseFails) > currFail) {
      TestCase
        .fail("We are getting worse, not better. Currently failing: " + (parseFails) + ". Was passing:" + currFail);
    }
    int currNoAuthors = 109;
    if ((parseAuthorless) > currNoAuthors) {
      TestCase.fail(
        "We are getting worse, not better. Currently without authors: " + (parseAuthorless) + ". Was:" + currNoAuthors);
    }
  }

  @Test
  public void testPeterDesmetNPE() throws Exception {
    ParsedName n = null;
    boolean parsed = false;
    try {
      n = parser.parse("(1-SFS) Aster lowrieanus Porter");
      parsed = true;
    } catch (UnparsableException e) {
      // nothing, expected to fail
    }
    try {
      n = parser.parse("(1-SFS)%20Aster%20lowrieanus%20Porter");
      parsed = true;
    } catch (UnparsableException e) {
      // nothing, expected to fail
    }
  }

  /**
   * These names have been parsing extremely slowely before - make sure this doesnt happen again
   */
  @Test
  public void testSlowNames() throws Exception {
    parser.debug = false;

    long start = System.currentTimeMillis();
    ParsedName pn = parser.parse("\"Acetobacter aceti var. muciparum\" (sic) (Hoyer) Frateur, 1950");
    assertEquals("Acetobacter", pn.genusOrAbove);
    assertEquals("aceti", pn.specificEpithet);
    assertEquals("muciparum", pn.infraSpecificEpithet);
    assertEquals("var.", pn.rank);
    assertTrue(System.currentTimeMillis() - start < SLOW);

    start = System.currentTimeMillis();
    pn = parser.parse("\"Acetobacter melanogenum (sic) var. malto-saccharovorans\" Frateur, 1950");
    assertEquals("Acetobacter", pn.genusOrAbove);
    assertEquals("melanogenum", pn.specificEpithet);
    assertTrue(System.currentTimeMillis() - start < SLOW);

    start = System.currentTimeMillis();
    pn = parser.parse("\"Acetobacter melanogenum (sic) var. maltovorans\" Frateur, 1950");
    assertEquals("Acetobacter", pn.genusOrAbove);
    assertEquals("melanogenum", pn.specificEpithet);
    assertTrue(System.currentTimeMillis() - start < SLOW);

    start = System.currentTimeMillis();
    pn = parser.parse("'Abelmoschus esculentus' bunchy top phytoplasma");
    assertEquals("Abelmoschus", pn.genusOrAbove);
    assertEquals("esculentus", pn.specificEpithet);
    assertTrue(System.currentTimeMillis() - start < SLOW);

    assertNotSlow("'38/89' designation is probably a typo");
    assertNotSlow("Argyropelecus d'Urvillei Valenciennes, 1849");
    assertNotSlow("Batillipes africanus Morone De Lucia, D'Addabbo Gallo and Grimaldi de Zio, 1988");
    assertNotSlow("Blainville's beaked whale gammaherpesvirus");
    assertNotSlow("Abrotanellinae H.Rob., G.D.Carr, R.M.King & A.M.Powell");
    assertNotSlow("Acidomyces B.J. Baker, M.A. Lutz, S.C. Dawson, P.L. Bond & Banfield");
    assertNotSlow("Acidomyces richmondensis B.J. Baker, M.A. Lutz, S.C. Dawson, P.L. Bond & Banfield, 2004");
    assertNotSlow("Acrodictys liputii L. Cai, K.Q. Zhang, McKenzie, W.H. Ho & K.D. Hyde");
    assertNotSlow("×Attabignya minarum M.J.Balick, A.B.Anderson & J.T.de Medeiros-Costa");
    assertNotSlow(
      "Paenibacillus donghaensis Choi,J.H.; Im,W.T.; Yoo,J.S.; Lee,S.M.; Moon,D.S.; Kim,H.J.; Rhee,S.K.; Roh,D.H.");

    // TODO: fix parser so that these names are faster!
    //assertNotSlow("Yamatocallis obscura (Ghosh, M.R., A.K. Ghosh & D.N. Raychaudhuri, 1971");
    //assertNotSlow("Xanthotrogus tadzhikorum Nikolajev, 2008");
    //assertNotSlow("Xylothamia G.L. Nesom, Y.B. Suh, D.R. Morgan & B.B. Simpson, 1990");
    //assertNotSlow("Virginianthus E.M. Friis, H. Eklund, K.R. Pedersen & P.R. Crane, 1994");

  }

  /**
   * @return true if name could be parsed
   */
  private boolean assertNotSlow(String name) {
    long start = System.currentTimeMillis();
    boolean parsed = false;
    try {
      ParsedName pn = parser.parse(name);
      parsed = true;
    } catch (UnparsableException e) {
      // TODO: Handle exception
    }
    assertTrue(System.currentTimeMillis() - start < SLOW);
    return parsed;
  }

  @Test
  public void testSpacelessAuthors() throws Exception {
    parser.debug = false;

    ParsedName pn = parser.parse("Abelmoschus moschatus Medik. subsp. tuberosus (Span.) Borss.Waalk.");
    assertEquals("Abelmoschus", pn.genusOrAbove);
    assertEquals("moschatus", pn.specificEpithet);
    assertEquals("tuberosus", pn.infraSpecificEpithet);
    assertEquals("Borss.Waalk.", pn.authorship);
    assertEquals("Span.", pn.bracketAuthorship);
    assertEquals(NameType.sciname, pn.type);

    pn = parser.parse("Acalypha hochstetteriana Müll.Arg.");
    assertEquals("Acalypha", pn.genusOrAbove);
    assertEquals("hochstetteriana", pn.specificEpithet);
    assertEquals("Müll.Arg.", pn.authorship);
    assertEquals(NameType.wellformed, pn.type);

    pn = parser.parse("Caloplaca variabilis (Pers.) Müll.Arg.");
    assertEquals("Caloplaca", pn.genusOrAbove);
    assertEquals("variabilis", pn.specificEpithet);
    assertEquals("Müll.Arg.", pn.authorship);
    assertEquals("Pers.", pn.bracketAuthorship);
    assertEquals(NameType.wellformed, pn.type);

    pn = parser.parse("Tridax imbricatus Sch.Bip.");
    assertEquals("Tridax", pn.genusOrAbove);
    assertEquals("imbricatus", pn.specificEpithet);
    assertEquals("Sch.Bip.", pn.authorship);
    assertEquals(NameType.wellformed, pn.type);

  }

  @Test
  public void testViralNames() throws Exception {
    parser.debug = false;
    assertTrue(isViralName("Vibrio phage 149 (type IV)"));
    assertTrue(isViralName("Cactus virus 2"));
    assertTrue(isViralName("Suid herpesvirus 3 Ictv"));
    assertTrue(isViralName("Tomato yellow leaf curl Mali virus Ictv"));
    assertTrue(isViralName("Not Sapovirus MC10"));
    assertTrue(isViralName("Diolcogaster facetosa bracovirus"));
    assertTrue(isViralName("Human papillomavirus"));
    assertTrue(isViralName("Sapovirus Hu/GI/Nsc, 150/PA/Bra/, 1993"));
    assertTrue(isViralName("Aspergillus mycovirus, 1816"));
    assertTrue(isViralName("Hantavirus sdp2 Yxl-, 2008"));
    assertTrue(isViralName("Norovirus Nizhny Novgorod /, 2461 / Rus /, 2007"));
    assertTrue(isViralName("Carrot carlavirus WM-, 2008"));
    assertTrue(isViralName("C2-like viruses"));
    assertTrue(isViralName("C1 bacteriophage"));
    assertTrue(isViralName("C-terminal Gfp fusion vector pUG23"));
    assertTrue(isViralName("C-terminal Gfp fusion vector"));
    assertTrue(isViralName("CMVd3 Flexi Vector pFN24K (HaloTag 7)"));
    assertTrue(isViralName("bacteriophage, 315.6"));
    assertTrue(isViralName("bacteriophages"));
    assertTrue(isViralName("\"T1-like viruses\""));

    assertFalse(isViralName("Forcipomyia flavirustica Remm, 1968"));

  }

  @Test
  public void testBacteriaNames() throws Exception {
    parser.debug = false;
    assertUnparsableWithoutType("HMWdDNA-degrading marine bacterium 10");
    assertUnparsableWithoutType("SAR11 cluster bacterium enrichment culture clone Pshtc-, 019");
    assertUnparsableWithoutType("SR1 bacterium canine oral taxon, 369");
    assertUnparsableWithoutType("bacterium 'glyphosate'");
    assertUnparsableWithoutType("bacterium 'hafez'");
    assertUnparsableWithoutType("bacterium A1");
  }

  public boolean isViralName(String name) {
    parser.debug = false;
    try {
      ParsedName pn = parser.parse(name);
    } catch (UnparsableException e) {
      // swallow
      if (NameType.virus == e.type) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testWhitespaceEpitheta() throws Exception {
    parser.debug = false;

    ParsedName n = parser.parse("Nupserha van rooni usambarica");
    assertEquals("Nupserha", n.getGenusOrAbove());
    assertTrue(n.getInfraGeneric() == null);
    assertEquals("van rooni", n.getSpecificEpithet());
    assertEquals("usambarica", n.getInfraSpecificEpithet());

    n = parser.parse("Sargassum flavicans van pervillei");
    assertEquals("Sargassum", n.getGenusOrAbove());
    assertTrue(n.getInfraGeneric() == null);
    assertEquals("flavicans", n.getSpecificEpithet());
    assertEquals("van pervillei", n.getInfraSpecificEpithet());

    n = parser.parse("Salix novae angliae lingulata ");
    assertEquals("Salix", n.getGenusOrAbove());
    assertTrue(n.getInfraGeneric() == null);
    assertEquals("novae angliae", n.getSpecificEpithet());
    assertEquals("lingulata", n.getInfraSpecificEpithet());

    n = parser.parse("Ilex collina van trompii");
    assertEquals("Ilex", n.getGenusOrAbove());
    assertTrue(n.getInfraGeneric() == null);
    assertEquals("collina", n.getSpecificEpithet());
    assertEquals("van trompii", n.getInfraSpecificEpithet());

    n = parser.parse("Gaultheria depressa novae zealandiae");
    assertEquals("Gaultheria", n.getGenusOrAbove());
    assertTrue(n.getInfraGeneric() == null);
    assertEquals("depressa", n.getSpecificEpithet());
    assertEquals("novae zealandiae", n.getInfraSpecificEpithet());

    n = parser.parse("Caraguata van volxemi gracilior");
    assertEquals("Caraguata", n.getGenusOrAbove());
    assertTrue(n.getInfraGeneric() == null);
    assertEquals("van volxemi", n.getSpecificEpithet());
    assertEquals("gracilior", n.getInfraSpecificEpithet());

    n = parser.parse("Ancistrocerus agilis novae guineae");
    assertEquals("Ancistrocerus", n.getGenusOrAbove());
    assertTrue(n.getInfraGeneric() == null);
    assertEquals("agilis", n.getSpecificEpithet());
    assertEquals("novae guineae", n.getInfraSpecificEpithet());

    // also test the authorless parsing
    n = parser.parse("Ancistrocerus agilis novae guineae");
    assertEquals("Ancistrocerus", n.getGenusOrAbove());
    assertTrue(n.getInfraGeneric() == null);
    assertEquals("agilis", n.getSpecificEpithet());
    assertEquals("novae guineae", n.getInfraSpecificEpithet());
  }

}

