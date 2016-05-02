/**
 * ExternalTournamentDocument.java
 */
package gotha;

import java.io.*;
import java.rmi.RemoteException;

import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * ExternalTournamentDocument enables importing Players and Games from a file
 * 
 * @author Luc Vannier
 */
public class ExternalTournamentDocument {
    public final static int DT_UNDEFINED = 0;
    public final static int DT_H9 = 1;
    public final static int DT_TOU = 2;
    public final static int DT_XML = 3;

    /**
     * import from a TOU file
     * File should be in TOU format
     */
    public static void importPlayersAndGamesFromTouFile(File f, ArrayList<Player> alPlayers, ArrayList<Game> alGames) {
        // def values are from FFG99 format  
        int posNaFi = 5;    // name, firstname
        int nbcNaFi = 25;
        int posRank = 30;
        int nbcRank = 3;
        int posClub = 42;
        int nbcClub = 4;
        int posGame = 47;
        int nbcGame = 288;  // Max is 32 X 9 (" 9999+w0c"), c being an additional reserved character (for ! ... ?)        try{

        ArrayList<String> alLines = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream(f);
            com.glaforge.i18n.io.SmartEncodingInputStream smartIS = new com.glaforge.i18n.io.SmartEncodingInputStream(fis);
            Reader reader = smartIS.getReader();
            BufferedReader d = new BufferedReader(reader);

            String s;
            do {
                s = d.readLine();
                if (s != null) {
                    alLines.add(s);
                }
            } while (s != null);
            d.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Parse tournament lines

        // Parse player lines
        ArrayList<PotentialHalfGame> alPotentialHalfGames = new ArrayList<PotentialHalfGame>();
        for (String strLine : alLines) {
            if (strLine.length() < 10) {
                continue;
            }
            if (strLine.charAt(0) == ';') {
                continue;
            }
            String strNaFi = strLine.substring(posNaFi, Math.min(posNaFi + nbcNaFi, strLine.length()));
            String strRank = strLine.substring(posRank, Math.min(posRank + nbcRank, strLine.length()));
            String strClub = strLine.substring(posClub, Math.min(posClub + nbcClub, strLine.length()));
            String strGame = strLine.substring(posGame, Math.min(posGame + nbcGame, strLine.length()));


            while (strNaFi.indexOf("  ") >= 0) {
                strNaFi = strNaFi.replace("  ", " ");
            }
            String[] tabStr = strNaFi.trim().split(" ");

            Player p = null;
            try {
                p = new Player(
                        tabStr[0],
                        tabStr[1],
                        "",
                        strClub,
                        "", // EGF Pin
                        "", // FFG Licence
                        Player.convertKDToInt(strRank),
                        Player.convertKDToInt(strRank) * 100,
                        "INI",
                        0,
                        "FIN");
                boolean[] bPart = new boolean[Gotha.MAX_NUMBER_OF_ROUNDS];
                for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
                    bPart[i] = true;
                }
                p.setParticipating(bPart);
            } catch (PlayerException pe) {
                JOptionPane.showMessageDialog(null, pe.getMessage(), "Player Exception", JOptionPane.ERROR_MESSAGE);
                return;
            }
            alPlayers.add(p);
            // Parse result line =================================================================================
            int currentPlayerNumber = alPlayers.size() - 1;
            parseResultLine(currentPlayerNumber, strGame, alPotentialHalfGames);
        }

        // Build alGames
        buildALGames(alPotentialHalfGames, alPlayers, alGames);
    }

    public static void importPlayersAndGamesFromH9File(File f, ArrayList<Player> alPlayers, ArrayList<Game> alGames) {
        int nbcGames = 288;  // Max is 32 X 9 (" 9999+w0c"), c being an additional reserved character (for ! ... ?)        try{
        ArrayList<String> alLines = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream(f);
            com.glaforge.i18n.io.SmartEncodingInputStream smartIS = new com.glaforge.i18n.io.SmartEncodingInputStream(fis);
            Reader reader = smartIS.getReader();
            BufferedReader d = new BufferedReader(reader);

            String s;
            do {
                s = d.readLine();
                if (s != null) {
                    alLines.add(s);
                }
            } while (s != null);
            d.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Parse tournament lines

        // Parse player lines
        ArrayList<PotentialHalfGame> alPotentialHalfGames = new ArrayList<PotentialHalfGame>();
        for (String strLine : alLines) {
            if (strLine.length() < 10) {
                continue;
            }
            if (strLine.charAt(0) == ';') {
                continue;
            }
            String strNaFi = strLine.substring(4, 27);
            String strRemaining = strLine.substring(27);
            strNaFi = strNaFi.trim();
            String[] array = strNaFi.split(" ");
            String strNa = array[0];
            String strFi = array[1];

            strRemaining = strRemaining.trim();
            while (strRemaining.indexOf("  ") >= 0) {
                strRemaining = strRemaining.replace("  ", " ");
            }
            
            array = strRemaining.split(" ", 8);
            
            String strRank      = array[0];
            String strCountry   = array[1];
            String strClub      = array[2];
            String strGames     = array[7];

            Player p = null;
            try {
                p = new Player(
                        strNa,
                        strFi,
                        strCountry,
                        strClub,
                        "", // EGF Pin
                        "", // FFG Licence
                        Player.convertKDToInt(strRank),
                        Player.convertKDToInt(strRank) * 100,
                        "INI",
                        0,
                        "FIN");
                boolean[] bPart = new boolean[Gotha.MAX_NUMBER_OF_ROUNDS];
                for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
                    bPart[i] = true;
                }
                p.setParticipating(bPart);
            } catch (PlayerException pe) {
                JOptionPane.showMessageDialog(null, pe.getMessage(), "Player Exception", JOptionPane.ERROR_MESSAGE);
                return;
            }
            alPlayers.add(p);
            // Parse result line =================================================================================
            int currentPlayerNumber = alPlayers.size() - 1;
            parseResultLine(currentPlayerNumber, strGames, alPotentialHalfGames);
        }

        // Build alGames
        buildALGames(alPotentialHalfGames, alPlayers, alGames);
    }

    public static Document getDocumentFromXMLFile(File sourceFile) {
        DocumentBuilder docBuilder;
        Document doc = null;
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setIgnoringElementContentWhitespace(true);
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.out.println("Wrong parser configuration: " + e.getMessage());
            return null;
        }
        try {
            doc = docBuilder.parse(sourceFile);
        } catch (SAXException e) {
            System.out.println("Wrong XML file structure: " + e.getMessage());
            return null;
        } catch (IOException e) {
            System.out.println("Could not read source file: " + e.getMessage());
        }
        return doc;
    }

    public static ArrayList<Player> importPlayersFromXMLFile(File sourceFile) {
        Document doc = getDocumentFromXMLFile(sourceFile);
        if (doc == null) {
            return null;
        }
        ArrayList<Player> alPlayers = new ArrayList<Player>();

        NodeList nl = doc.getElementsByTagName("Player");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            NamedNodeMap nnm = n.getAttributes();

            String name = extractNodeValue(nnm, "name", "");
            String firstName = extractNodeValue(nnm, "firstName", "");
            String country = extractNodeValue(nnm, "country", "");
            String club = extractNodeValue(nnm, "club", "");
            String egfPin = extractNodeValue(nnm, "egfPin", "");
            String ffgLicence = extractNodeValue(nnm, "ffgLicence", "");
            String strRank = extractNodeValue(nnm, "rank", "30K");
            int rank = Player.convertKDToInt(strRank);
            String strRating = extractNodeValue(nnm, "rating", "-2950");
            int rating = new Integer(strRating).intValue();
            String ratingOrigin = extractNodeValue(nnm, "ratingOrigin", "");
            String strSmmsCorrection = extractNodeValue(nnm, "smmsCorrection", "0");
            int smmsCorrection = new Integer(strSmmsCorrection).intValue();
            String strDefaultParticipating = "";
            for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
                strDefaultParticipating += "1";
            }
            String strParticipating = extractNodeValue(nnm, "participating", strDefaultParticipating);
            boolean[] participating = new boolean[Gotha.MAX_NUMBER_OF_ROUNDS];
            for (int r = 0; r < participating.length; r++) {
                try {
                    char cPart = strParticipating.charAt(r);
                    if (cPart == '0') {
                        participating[r] = false;
                    } else {
                        participating[r] = true;
                    }
                } catch (IndexOutOfBoundsException e) {
                    participating[r] = true;
                }
            }
            String registeringStatus = extractNodeValue(nnm, "registeringStatus", "FIN");
            Player p = null;
            try {
                p = new Player(name, firstName, country, club, egfPin, ffgLicence, rank, rating, ratingOrigin, smmsCorrection, registeringStatus);
            } catch (PlayerException ex) {
                Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
            }
            p.setParticipating(participating);

            alPlayers.add(p);
        }
        return alPlayers;
    }

    public static TournamentParameterSet importTournamentParameterSetFromXMLFile(File sourceFile) {
        Document doc = getDocumentFromXMLFile(sourceFile);
        if (doc == null) {
            return null;
        }

        TournamentParameterSet tps = new TournamentParameterSet();

        //GPS
        GeneralParameterSet gps = new GeneralParameterSet();
        NodeList nlGPS = doc.getElementsByTagName("GeneralParameterSet");
        Node nGPS = nlGPS.item(0);
        NamedNodeMap nnmGPS = nGPS.getAttributes();

        String shortName = extractNodeValue(nnmGPS, "shortName", "defaultshortname");
        gps.setShortName(shortName);
        String name = extractNodeValue(nnmGPS, "name", "default Name");
        gps.setName(name);
        String location = extractNodeValue(nnmGPS, "location", "Sotteville les Rouen");
        gps.setLocation(location);
        String strBeginDate = extractNodeValue(nnmGPS, "beginDate", "2000-01-01");
        try {
            gps.setBeginDate(new SimpleDateFormat("yyyy-MM-dd").parse(strBeginDate));
        } catch (ParseException ex) {
            Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
        String strEndDate = extractNodeValue(nnmGPS, "endDate", "2000-01-01");
        try {
            gps.setEndDate(new SimpleDateFormat("yyyy-MM-dd").parse(strEndDate));
        } catch (ParseException ex) {
            Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
        String strTime = extractNodeValue(nnmGPS, "time", "60");
        gps.setStrTime(strTime);
        String strByoYomi = extractNodeValue(nnmGPS, "byoYomi", "true");
        gps.setBByoYomi(Boolean.valueOf(strByoYomi).booleanValue());
        String strSize = extractNodeValue(nnmGPS, "size", "19");
        gps.setStrSize(strSize);
        String strKomi = extractNodeValue(nnmGPS, "komi", "7.5");
        gps.setStrKomi(strKomi);
        String strNumberOfRounds = extractNodeValue(nnmGPS, "numberOfRounds", "5");
        gps.setNumberOfRounds(new Integer(strNumberOfRounds).intValue());
        String strNumberOfCategories = extractNodeValue(nnmGPS, "numberOfCategories", "1");
        int nbCategories = new Integer(strNumberOfCategories).intValue();
        gps.setNumberOfCategories(nbCategories);

        NodeList nl = doc.getElementsByTagName("Category");
        int[] lowerLimits = new int[nbCategories - 1];
        for (int c = 0; c < nl.getLength(); c++) {
            Node n = nl.item(c);
            NamedNodeMap nnm = n.getAttributes();
            String strNumber = extractNodeValue(nnm, "number", "1");
            String strLowerLimit = extractNodeValue(nnm, "lowerLimit", "30K");
            int numCat = new Integer(strNumber).intValue() - 1;
            lowerLimits[numCat] = Player.convertKDToInt(strLowerLimit);
        }
        gps.setLowerCategoryLimits(lowerLimits);

        String strGenMMFloor = extractNodeValue(nnmGPS, "genMMFloor", "20K");
        gps.setGenMMFloor(Player.convertKDToInt(strGenMMFloor));
        String strGenMMBar = extractNodeValue(nnmGPS, "genMMFBar", "4D");
        gps.setGenMMBar(Player.convertKDToInt(strGenMMBar));
        String strGenNBW2ValueAbsent = extractNodeValue(nnmGPS, "genNBW2ValueAbsent", "0");
        gps.setGenNBW2ValueAbsent(Player.convertKDToInt(strGenNBW2ValueAbsent));
        String strGenNBW2ValueBye = extractNodeValue(nnmGPS, "genNBW2ValueBye", "0");
        gps.setGenNBW2ValueBye(Player.convertKDToInt(strGenNBW2ValueBye));
        String strGenMMS2ValueAbsent = extractNodeValue(nnmGPS, "genMMS2ValueAbsent", "0");
        gps.setGenMMS2ValueAbsent(Player.convertKDToInt(strGenMMS2ValueAbsent));
        String strGenMMS2ValueBye = extractNodeValue(nnmGPS, "genMMS2ValueBye", "0");
        gps.setGenMMS2ValueBye(Player.convertKDToInt(strGenMMS2ValueBye));

        tps.setGeneralParameterSet(gps);

        // HPS
        HandicapParameterSet hps = new HandicapParameterSet();
        NodeList nlHPS = doc.getElementsByTagName("HandicapParameterSet");
        Node nHPS = nlHPS.item(0);
        NamedNodeMap nnmHPS = nHPS.getAttributes();

        String strHdBasedOnMMS = extractNodeValue(nnmHPS, "hdBasedOnMMS", "true");
        hps.setHdBasedOnMMS(Boolean.valueOf(strHdBasedOnMMS).booleanValue());
        String strHdNoHdRankThreshold = extractNodeValue(nnmHPS, "hdNoHdRankThreshold", "1D");
        hps.setHdNoHdRankThreshold(Player.convertKDToInt(strHdNoHdRankThreshold));
        String strHdCorrection = extractNodeValue(nnmHPS, "hdCorrection", "1");
        hps.setHdCorrection(new Integer(strHdCorrection).intValue());
        String strHdCeiling = extractNodeValue(nnmHPS, "hdCeiling", "9");
        hps.setHdCeiling(new Integer(strHdCeiling).intValue());
        tps.setHandicapParameterSet(hps);

        // PS
        PlacementParameterSet pps = new PlacementParameterSet();
        NodeList nlCrit = doc.getElementsByTagName("PlacementCriterion");

        int[] plaC = new int[PlacementParameterSet.PLA_MAX_NUMBER_OF_CRITERIA];
        for (int nC = 0; nC < PlacementParameterSet.PLA_MAX_NUMBER_OF_CRITERIA; nC++) {
            plaC[nC] = PlacementParameterSet.PLA_CRIT_NUL;
        }

        for (int c = 0; c < nlCrit.getLength(); c++) {
            Node n = nlCrit.item(c);
            NamedNodeMap nnm = n.getAttributes();
            String strNumber = extractNodeValue(nnm, "number", "1");
            int number = new Integer(strNumber).intValue();
            String strName = extractNodeValue(nnm, "name", "NULL");
            for (int nPC = 0; nPC < PlacementParameterSet.allPlacementCriteria.length; nPC++) {
                PlacementCriterion pC = PlacementParameterSet.allPlacementCriteria[nPC];
                if (strName.equals(pC.longName)) {
                    plaC[number - 1] = pC.uid;
                    break;
                }
            }
        }
        pps.setPlaCriteria(plaC);

        tps.setPlacementParameterSet(pps);

        //paiPS
        PairingParameterSet paiPS = new PairingParameterSet();
        NodeList nlPaiPS = doc.getElementsByTagName("PairingParameterSet");
        Node nPaiPS = nlPaiPS.item(0);
        NamedNodeMap nnmPaiPS = nPaiPS.getAttributes();

        paiPS.setPaiStandardNX1Factor(new Double(extractNodeValue(nnmPaiPS, "paiStandardNX1Factor", "0.5")).doubleValue());
        paiPS.setPaiBaAvoidDuplGame(new Long(extractNodeValue(nnmPaiPS, "paiBaAvoidDuplGame", "500000000000000")).longValue());
        paiPS.setPaiBaRandom(new Long(extractNodeValue(nnmPaiPS, "paiBaRandom", "0")).longValue());
        paiPS.setPaiBaDeterministic(Boolean.valueOf(extractNodeValue(nnmPaiPS, "paiBaDeterministic", "true")).booleanValue());
        paiPS.setPaiBaBalanceWB(new Long(extractNodeValue(nnmPaiPS, "paiBaBalanceWB", "1000")).longValue());
        paiPS.setPaiMaAvoidMixingCategories(new Long(extractNodeValue(nnmPaiPS, "paiMaAvoidMixingCategories", "20000000000000")).longValue());
        paiPS.setPaiMaMinimizeScoreDifference(new Long(extractNodeValue(nnmPaiPS, "paiMaMinimizeScoreDifference", "100000000000")).longValue());
        paiPS.setPaiMaDUDDWeight(new Long(extractNodeValue(nnmPaiPS, "paiMaDUDDWeight", "100000000")).longValue());

        String strDUDDU = extractNodeValue(nnmPaiPS, "paiMaDUDDUpperMode", "MID");
        int duddu = PairingParameterSet.PAIMA_DUDD_MID;
        if (strDUDDU.equals("TOP")) {
            duddu = PairingParameterSet.PAIMA_DUDD_TOP;
        }
        if (strDUDDU.equals("MID")) {
            duddu = PairingParameterSet.PAIMA_DUDD_MID;
        }
        if (strDUDDU.equals("BOT")) {
            duddu = PairingParameterSet.PAIMA_DUDD_BOT;
        }
        paiPS.setPaiMaDUDDUpperMode(duddu);

        String strDUDDL = extractNodeValue(nnmPaiPS, "paiMaDUDDLowerMode", "MID");
        int duddl = PairingParameterSet.PAIMA_DUDD_MID;
        if (strDUDDL.equals("TOP")) {
            duddl = PairingParameterSet.PAIMA_DUDD_TOP;
        }
        if (strDUDDL.equals("MID")) {
            duddl = PairingParameterSet.PAIMA_DUDD_MID;
        }
        if (strDUDDL.equals("BOT")) {
            duddl = PairingParameterSet.PAIMA_DUDD_BOT;
        }
        paiPS.setPaiMaDUDDLowerMode(duddl);
        paiPS.setPaiMaMaximizeSeeding(new Long(extractNodeValue(nnmPaiPS, "paiMaMaximizeSeeding", "5000000")).longValue());
        paiPS.setPaiMaLastRoundForSeedSystem1(new Integer(extractNodeValue(nnmPaiPS, "paiMaLastRoundForSeedSystem1", "2")).intValue() - 1);

        String strS1 = extractNodeValue(nnmPaiPS, "paiMaSeedSystem1", "SPLITANDRANDOM");
        int s1 = PairingParameterSet.PAIMA_SEED_SPLITANDRANDOM;
        if (strS1.equals("SPLITANDRANDOM")) {
            s1 = PairingParameterSet.PAIMA_SEED_SPLITANDRANDOM;
        }
        if (strS1.equals("SPLITANDFOLD")) {
            s1 = PairingParameterSet.PAIMA_SEED_SPLITANDFOLD;
        }
        if (strS1.equals("SPLITANDSLIP")) {
            s1 = PairingParameterSet.PAIMA_SEED_SPLITANDSLIP;
        }
        paiPS.setPaiMaSeedSystem1(s1);

        String strS2 = extractNodeValue(nnmPaiPS, "paiMaSeedSystem2", "SPLITANDFOLD");
        int s2 = PairingParameterSet.PAIMA_SEED_SPLITANDRANDOM;
        if (strS2.equals("SPLITANDRANDOM")) {
            s2 = PairingParameterSet.PAIMA_SEED_SPLITANDRANDOM;
        }
        if (strS2.equals("SPLITANDFOLD")) {
            s2 = PairingParameterSet.PAIMA_SEED_SPLITANDFOLD;
        }
        if (strS2.equals("SPLITANDSLIP")) {
            s2 = PairingParameterSet.PAIMA_SEED_SPLITANDSLIP;
        }
        paiPS.setPaiMaSeedSystem2(s2);

        String strAddCrit1 = extractNodeValue(nnmPaiPS, "paiMaAdditionalPlacementCritSystem1", "RATING");
        int aCrit1 = PlacementParameterSet.PLA_CRIT_RATING;
        for (int nPC = 0; nPC < PlacementParameterSet.allPlacementCriteria.length; nPC++) {
            PlacementCriterion pC = PlacementParameterSet.allPlacementCriteria[nPC];
            if (strAddCrit1.equals(pC.longName)) {
                aCrit1 = pC.uid;
                break;
            }
        }
        paiPS.setPaiMaAdditionalPlacementCritSystem1(aCrit1);

        String strAddCrit2 = extractNodeValue(nnmPaiPS, "paiMaAdditionalPlacementCritSystem2", "NULL");
        int aCrit2 = PlacementParameterSet.PLA_CRIT_NUL;
        for (int nPC = 0; nPC < PlacementParameterSet.allPlacementCriteria.length; nPC++) {
            PlacementCriterion pC = PlacementParameterSet.allPlacementCriteria[nPC];
            if (strAddCrit2.equals(pC.longName)) {
                aCrit2 = pC.uid;
                break;
            }
        }
        paiPS.setPaiMaAdditionalPlacementCritSystem2(aCrit2);

        paiPS.setPaiSeRankThreshold(Player.convertKDToInt(extractNodeValue(nnmPaiPS, "paiSeRankThreshold", "4D")));
        paiPS.setPaiSeNbWinsThresholdActive(Boolean.valueOf(extractNodeValue(nnmPaiPS, "paiSeNbWinsThresholdActive", "true")).booleanValue());
        paiPS.setPaiSeDefSecCrit(new Long(extractNodeValue(nnmPaiPS, "paiSeDefSecCrit", "100000000000")).longValue());
        paiPS.setPaiSeMinimizeHandicap(new Long(extractNodeValue(nnmPaiPS, "paiSeMinimizeHandicap", "0")).longValue());
        paiPS.setPaiSeAvoidSameGeo(new Long(extractNodeValue(nnmPaiPS, "paiSeAvoidSameGeo", "100000000000")).longValue());
        paiPS.setPaiSePreferMMSDiffRatherThanSameCountry(new Integer(extractNodeValue(nnmPaiPS, "paiSePreferMMSDiffRatherThanSameCountry", "1")).intValue());
        paiPS.setPaiSePreferMMSDiffRatherThanSameClub(new Integer(extractNodeValue(nnmPaiPS, "paiSePreferMMSDiffRatherThanSameClub", "3")).intValue());

        tps.setPairingParameterSet(paiPS);

        return tps;
    }

    public static String extractNodeValue(NamedNodeMap nnm, String attributeName, String defaultValue) {
        String value = defaultValue;
        Node node = nnm.getNamedItem(attributeName);
        if (node != null) {
            value = node.getNodeValue();
        }
        return value;
    }

    public static ArrayList<Game> importGamesFromXMLFile(File sourceFile, TournamentInterface tournament) {
        Document doc = getDocumentFromXMLFile(sourceFile);
        if (doc == null) {
            return null;
        }

        ArrayList<Game> alGames = new ArrayList<Game>();
        NodeList nl = doc.getElementsByTagName("Game");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            NamedNodeMap nnm = n.getAttributes();

            String strRoundNumber = extractNodeValue(nnm, "roundNumber", "1");
            int roundNumber = new Integer(strRoundNumber).intValue() - 1;
            String strTableNumber = extractNodeValue(nnm, "tableNumber", "1");
            int tableNumber = new Integer(strTableNumber).intValue() - 1;
            String strWhitePlayer = extractNodeValue(nnm, "whitePlayer", "");
            String strBlackPlayer = extractNodeValue(nnm, "blackPlayer", "");
            Player wP;
            Player bP;
            try {
                wP = tournament.getPlayerByCanonicalName(strWhitePlayer);
                if (wP == null) {
                    continue;
                }
                bP = tournament.getPlayerByCanonicalName(strBlackPlayer);
                if (bP == null) {
                    continue;
                }
            } catch (RemoteException ex) {
                Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            String strKnownColor = extractNodeValue(nnm, "strKnownColor", "true");
            boolean knownColor = true;
            if (strKnownColor.equals("false")) {
                knownColor = false;
            }
            String strHandicap = extractNodeValue(nnm, "handicap", "0");
            int handicap = new Integer(strHandicap).intValue();
            String strResult = extractNodeValue(nnm, "result", "RESULT_UNKNOWN");
            int result = Game.RESULT_UNKNOWN;
            if (strResult.equals("RESULT_WHITEWINS")) {
                result = Game.RESULT_WHITEWINS;
            }
            if (strResult.equals("RESULT_BLACKWINS")) {
                result = Game.RESULT_BLACKWINS;
            }
            if (strResult.equals("RESULT_EQUAL")) {
                result = Game.RESULT_EQUAL;
            }
            if (strResult.equals("RESULT_BOTHLOSE")) {
                result = Game.RESULT_BOTHLOSE;
            }
            if (strResult.equals("RESULT_BOTHWIN")) {
                result = Game.RESULT_BOTHWIN;
            }
            if (strResult.equals("RESULT_WHITEWINS_BYDEF")) {
                result = Game.RESULT_WHITEWINS_BYDEF;
            }
            if (strResult.equals("RESULT_BLACKWINS_BYDEF")) {
                result = Game.RESULT_BLACKWINS_BYDEF;
            }
            if (strResult.equals("RESULT_EQUAL_BYDEF")) {
                result = Game.RESULT_EQUAL_BYDEF;
            }
            if (strResult.equals("RESULT_BOTHLOSE_BYDEF")) {
                result = Game.RESULT_BOTHLOSE_BYDEF;
            }
            if (strResult.equals("RESULT_BOTHWIN_BYDEF")) {
                result = Game.RESULT_BOTHWIN_BYDEF;
            }

            Game g = new Game(roundNumber, tableNumber, wP, bP, true, handicap, result);
            g.setKnownColor(knownColor);
            alGames.add(g);
        }
        return alGames;
    }

    public static Player[] importByePlayersFromXMLFile(File sourceFile, TournamentInterface tournament) {
        Document doc = getDocumentFromXMLFile(sourceFile);

        Player[] byePlayers = new Player[Gotha.MAX_NUMBER_OF_ROUNDS];
        for (int r = 0; r < byePlayers.length; r++) {
            byePlayers[r] = null;
        }

        NodeList nl = doc.getElementsByTagName("ByePlayer");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            NamedNodeMap nnm = n.getAttributes();
            String strRoundNumber = extractNodeValue(nnm, "roundNumber", "1");
            int roundNumber = new Integer(strRoundNumber).intValue() - 1;
            String strPlayer = extractNodeValue(nnm, "player", "");
            Player p;
            try {
                p = tournament.getPlayerByCanonicalName(strPlayer);
                if (p == null) {
                    continue;
                }
            } catch (RemoteException ex) {
                Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            byePlayers[roundNumber] = p;
        }

        return byePlayers;
    }

    /**
     * Gathers potential half-games to build games into a ArrayList<Game>.
     * If necessary, reaffects round numbers so that a given game has been played in one only round, 
     * and so that a given player may not have played 2 games in the same round. 
     **/
    private static void buildALGames(ArrayList<PotentialHalfGame> alPotentialHalfGames, ArrayList<Player> alPlayers, ArrayList<Game> alGames) {
        // Initialize tabGames
        Game[][] tabGames = new Game[Gotha.MAX_NUMBER_OF_ROUNDS][alPotentialHalfGames.size()]; // Of course, it is over-dimensionned
        for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
            for (int ng = 0; ng < alPotentialHalfGames.size(); ng++) {
                tabGames[r][ng] = new Game();
            }
        }

        // vBProcessedHalfGames is mapped like alPotentialHalfGames. 
        // An element of vBProcessedPHG is set to true when processed
        Vector<Boolean> vBProcessedPHG = new Vector<Boolean>();
        for (PotentialHalfGame phg : alPotentialHalfGames) {
            vBProcessedPHG.add(false);
        }

        // Process every PotentialHalfGame
        for (int numPHG = 0; numPHG < alPotentialHalfGames.size(); numPHG++) {
            PotentialHalfGame phg = alPotentialHalfGames.get(numPHG);
            if (phg.opponentNumber == -1) {
                continue;
            }
            if (vBProcessedPHG.get(numPHG)) {
                continue;
            }
            int roundNumber = phg.roundNumber;
            int chosenRoundNumber = 0;
            Player p1 = alPlayers.get(phg.playerNumber);
            Player p2 = null;
            p2 = alPlayers.get(phg.opponentNumber);
            if (isASuitableRound(roundNumber, p1, p2, tabGames)) {
                chosenRoundNumber = roundNumber;
            } else {
                for (roundNumber = 0; roundNumber < Gotha.MAX_NUMBER_OF_ROUNDS; roundNumber++) {
                    if (isASuitableRound(roundNumber, p1, p2, tabGames)) {
                        chosenRoundNumber = roundNumber;
                        break;
                    }
                }
            }

            int chosenGameNumber = -1;
            for (int numG = 0; numG < tabGames[chosenRoundNumber].length; numG++) {
                Game game = tabGames[chosenRoundNumber][numG];
                if (game.getWhitePlayer() == null && game.getBlackPlayer() == null) {
                    chosenGameNumber = numG;
                    break;
                }
            }

            // Store this PotentialHalfGame into a Game
            Game g = tabGames[chosenRoundNumber][chosenGameNumber];
            Player player = alPlayers.get(phg.playerNumber);
            Player opponent = alPlayers.get(phg.opponentNumber);

            if (phg.color == 'w') {
                g.setWhitePlayer(player);
                g.setBlackPlayer(opponent);
                g.setResult(Game.RESULT_UNKNOWN);
                if (phg.result == 1) {
                    g.setResult(Game.RESULT_WHITEWINS);
                } else if (phg.result == -1) {
                    g.setResult(Game.RESULT_BLACKWINS);
                }
                g.setKnownColor(true);
            } else if (phg.color == 'b') {
                g.setWhitePlayer(opponent);
                g.setBlackPlayer(player);
                g.setResult(Game.RESULT_UNKNOWN);
                if (phg.result == 1) {
                    g.setResult(Game.RESULT_BLACKWINS);
                } else if (phg.result == -1) {
                    g.setResult(Game.RESULT_WHITEWINS);
                }
                g.setKnownColor(true);
            } else {
                g.setWhitePlayer(player);
                g.setBlackPlayer(opponent);
                g.setResult(Game.RESULT_UNKNOWN);
                if (phg.result == 1) {
                    g.setResult(Game.RESULT_WHITEWINS);
                } else if (phg.result == -1) {
                    g.setResult(Game.RESULT_BLACKWINS);
                }
                g.setKnownColor(false);
            }
            g.setHandicap(phg.handicap);
            g.setRoundNumber(chosenRoundNumber);

            // Choose a table number
            int tN = 0;
            for (int gN = 0; gN < tabGames[g.getRoundNumber()].length; gN++) {
                Game game = tabGames[g.getRoundNumber()][gN];
                if (tN <= game.getTableNumber()) {
                    tN = game.getTableNumber() + 1;
                }
            }
            g.setTableNumber(tN);

            // Freeze the potential halfGame or both potential halfGames just processed
            vBProcessedPHG.setElementAt(true, numPHG);
            for (int nPHG_work = 0; nPHG_work < alPotentialHalfGames.size(); nPHG_work++) {
                PotentialHalfGame phg_work = alPotentialHalfGames.get(nPHG_work);
                if (vBProcessedPHG.get(nPHG_work)) {
                    continue;
                }
                if (phg_work.playerNumber == phg.opponentNumber && phg_work.opponentNumber == phg.playerNumber) {
                    vBProcessedPHG.setElementAt(true, nPHG_work);
                    break;
                }
            }
        }

        // Store the games into alGames
        for (int numR = 0; numR < Gotha.MAX_NUMBER_OF_ROUNDS; numR++) {
            for (int numG = 0; numG < alPotentialHalfGames.size(); numG++) {
                Game g = tabGames[numR][numG];
                if (g.getWhitePlayer() != null && g.getBlackPlayer() != null) {
                    alGames.add(g);
                }
            }
        }
    }

    /**
     * isASuitableRound tests whether the referenced game by game can be inserted
     * in round number roundNumber. id est :
     * <br>If a game has already been played by either player of the pair in candidateRoundNumber, returns false.
     * <br>If not, returns true;
     */
    private static boolean isASuitableRound(int candidateRoundNumber, Player p1, Player p2, Game[][] tabGames) {
        for (int ng = 0; ng < tabGames[candidateRoundNumber].length; ng++) {
            Game g = tabGames[candidateRoundNumber][ng];
            if (p1.hasSameKeyString(g.getWhitePlayer())) {
                return false;
            }
            if (p1.hasSameKeyString(g.getBlackPlayer())) {
                return false;
            }
            if (p2.hasSameKeyString(g.getWhitePlayer())) {
                return false;
            }
            if (p2.hasSameKeyString(g.getBlackPlayer())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the half-games Vector vHalfGames is round-structured.
     * <br> Returns true if, for each pair of half-games, the round number is the same 
     * for both players.
     * <br> Beware : vHalfGames has to be coherent ccording to coherence given by isCoherent method.
     * If not, result is impredictible
     */
    private static boolean isRoundStructured(ArrayList<PotentialHalfGame> alPotentialHalfGames) {
        for (int nPHG1 = 0; nPHG1 < alPotentialHalfGames.size(); nPHG1++) {
            PotentialHalfGame phg1 = alPotentialHalfGames.get(nPHG1);
            int numPl1 = phg1.playerNumber;
            int numOp1 = phg1.opponentNumber;
            int numRd1 = phg1.roundNumber;
            if (numOp1 == -1) {
                continue;
            }
            boolean bFound = false;
            for (int nPHG2 = 0; nPHG2 < alPotentialHalfGames.size(); nPHG2++) {
                PotentialHalfGame phg2 = alPotentialHalfGames.get(nPHG2);
                if (phg2.playerNumber == numOp1 && phg2.opponentNumber == numPl1 && phg2.roundNumber == numRd1) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks coherenceof  alPotentialHalfGames.
     * <br> Coherence is reached if every half-game phg1 matches an other half-gam phg2, such as :
     * <br> phg2.playerNumber = phg1.opponentNumber
     * <br> phg2.opponentNumber = phg1.playerNumber
     * <br> phg2.result = -phg1.result
     * <br> (phg1.color, phg2.color) = ('w', 'b') ou ('b', 'w') ou ('?', '?')
     * <br> phg2.handicap = phg1.handicap
     * <br> A game without opponent does not trigger incoherence
     * @return true if coherent
     */
    private static boolean isCoherent(ArrayList<PotentialHalfGame> alPotentialHalfGames) {
        Vector<Boolean> opponentFound = new Vector<Boolean>();
        for (int nPHG = 0; nPHG < alPotentialHalfGames.size(); nPHG++) {
            opponentFound.add(false);
        }
        for (int nPHG1 = 0; nPHG1 < alPotentialHalfGames.size(); nPHG1++) {
            if (opponentFound.get(nPHG1)) {
                continue;
            }
            PotentialHalfGame phg1 = alPotentialHalfGames.get(nPHG1);
            if (phg1.opponentNumber == -1) {
                opponentFound.set(nPHG1, true); // Because it is OK if no opponent
            }
            for (int nPHG2 = nPHG1 + 1; nPHG2 < alPotentialHalfGames.size(); nPHG2++) {
                if (opponentFound.get(nPHG2)) {
                    continue;
                }
                PotentialHalfGame phg2 = alPotentialHalfGames.get(nPHG2);
                if (phg2.playerNumber != phg1.opponentNumber) {
                    continue;
                }
                if (phg2.opponentNumber != phg1.playerNumber) {
                    continue;
                }
                if (phg2.result != -phg1.result) {
                    continue;
                }
                if (phg2.color == 'w' && phg1.color != 'b') {
                    continue;
                }
                if (phg2.color == 'b' && phg1.color != 'w') {
                    continue;
                }
                if (phg2.color == '?' && phg1.color != '?') {
                    continue;
                }
                if (phg2.handicap != phg1.handicap) {
                    continue;
                }
                // Tous obstacles franchis. C'est bon
                opponentFound.set(nPHG1, true);
                opponentFound.set(nPHG2, true);
                break;
            }
        }
        for (int nPHG = 0; nPHG < alPotentialHalfGames.size(); nPHG++) {
            if (!opponentFound.get(nPHG)) {
//                PotentialHalfGame phg = alPotentialHalfGames.get(nPHG);
                return false;
            }
        }
        return true;
    }

    public static void generateTouFile(TournamentInterface tournament, File f) {
        TournamentParameterSet tps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return;
        }
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        PlacementParameterSet pps = tps.getPlacementParameterSet();

        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(f));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // Headers       
        try {
            String shortName = gps.getShortName();

            output.write(";name=" + shortName);
            output.write("\n;date=" + new SimpleDateFormat("dd/MM/yyyy").format(gps.getBeginDate()));
            output.write("\n;vill=" + gps.getLocation());
            output.write("\n;comm=" + gps.getName());
            output.write("\n;prog=" + Gotha.getGothaVersionnedName());
            output.write("\n;size=" + gps.getStrSize());
            String strT = gps.getStrTime() + (gps.isBByoYomi() ? "+b" : "");
            output.write("\n;time=" + strT);
            output.write("\n;komi=" + gps.getStrKomi());
            output.write("\n;Num Nom Prénom               Niv Licence Club");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Contents
        ArrayList<ScoredPlayer> alOrderedScoredPlayers = null;
        int roundNumber = gps.getNumberOfRounds() - 1;
        try {
            alOrderedScoredPlayers = tournament.orderedScoredPlayersList(roundNumber, pps);
            // Eliminate non-players
            for (Iterator<ScoredPlayer> it = alOrderedScoredPlayers.iterator(); it.hasNext();) {
                ScoredPlayer sP = it.next();
                if (!tournament.isPlayerImplied(sP)) {
                    it.remove();
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        String[][] hG = ScoredPlayer.halfGamesStrings(alOrderedScoredPlayers, roundNumber, tps);
        String[] strPlace = ScoredPlayer.positionStrings(alOrderedScoredPlayers, roundNumber, tps);
        for (int iSP = 0; iSP < alOrderedScoredPlayers.size(); iSP++) {
            ScoredPlayer sP = alOrderedScoredPlayers.get(iSP);

            String strLine = "";

            String strPl = "    " + strPlace[iSP];
            strPl = strPl.substring(strPl.length() - 4);
            strLine += strPl;

            String strNF = sP.fullUnblankedName() + "                         ";
            strNF = strNF.substring(0, 25);
            strLine += " " + strNF;

            String strRank = "   " + Player.convertIntToKD(sP.getRank());
            strRank = strRank.substring(strRank.length() - 3);
            strLine += strRank;

            String strLic = "       " + sP.getFfgLicence();
            strLic = strLic.substring(strLic.length() - 7);
            strLine += " " + strLic;

            String strClub = "    " + sP.getClub();
            strClub = strClub.substring(strClub.length() - 4);
            strLine += " " + strClub;
            for (int r = 0; r <= roundNumber; r++) {
                String strHG = "        " + hG[r][iSP];
                strHG = strHG.replace("/", "");
                strHG = strHG.replace("!", "");

                strHG = strHG.substring(strHG.length() - 8);
                strLine += strHG;
            }

            try {
                output.write("\n" + strLine);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            output.write("\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void generateH9File(TournamentInterface tournament, File f, boolean bKeepByDefResults) {
        TournamentParameterSet tps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return;
        }
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        PlacementParameterSet pps = tps.getPlacementParameterSet();

        // Prepare tabCrit from pps
        int[] tC = pps.getPlaCriteria();
        int nbC = 0;
        for (int c = 0; c < tC.length; c++) {
            if (tC[c] != PlacementParameterSet.PLA_CRIT_NUL) {
                nbC++;
            }
        }
        int[] tabCrit;
        if (nbC == 0) {
            tabCrit = new int[1];
            tabCrit[0] = PlacementParameterSet.PLA_CRIT_NUL;
        } else {
            tabCrit = new int[nbC];
            tabCrit[0] = PlacementParameterSet.PLA_CRIT_NUL;
            int crit = 0;
            for (int c = 0; c < tC.length; c++) {
                if (tC[c] != PlacementParameterSet.PLA_CRIT_NUL) {
                    tabCrit[crit++] = tC[c];
                }
            }
        }

        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(f));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // Headers       
        try {
            output.write("; " + gps.getName());
            output.write("\n; " + new SimpleDateFormat("dd/MM/yyyy").format(gps.getBeginDate()));
            output.write("\n;");
            output.write("\n; Pl Name                   Grd Co Club");
            for (int c = 0; c < tabCrit.length; c++) {
                // Make strings with exactly 4 characters
                String strCrit = PlacementParameterSet.criterionShortName(tabCrit[c]);
                strCrit = strCrit.trim();
                if (strCrit.length() > 4) {
                    strCrit = strCrit.substring(0, 4);
                }
                while (strCrit.length() < 4) {
                    strCrit = " " + strCrit;
                }
                output.write(" " + strCrit);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Contents
        ArrayList<ScoredPlayer> alOrderedScoredPlayers = null;
        int roundNumber = gps.getNumberOfRounds() - 1;
        try {
            alOrderedScoredPlayers = tournament.orderedScoredPlayersList(roundNumber, pps);
            // Eliminate non-players
            for (Iterator<ScoredPlayer> it = alOrderedScoredPlayers.iterator(); it.hasNext();) {
                ScoredPlayer sP = it.next();
                if (!tournament.isPlayerImplied(sP)) {
                    it.remove();
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        String[][] hG = ScoredPlayer.halfGamesStrings(alOrderedScoredPlayers, roundNumber, tps);
        String[] strPlace = ScoredPlayer.positionStrings(alOrderedScoredPlayers, roundNumber, tps);
        for (int iSP = 0; iSP < alOrderedScoredPlayers.size(); iSP++) {
            ScoredPlayer sP = alOrderedScoredPlayers.get(iSP);

            String strLine = "";

            String strPl = "    " + strPlace[iSP];
            strPl = strPl.substring(strPl.length() - 4);
            strLine += strPl;

            String strNF = sP.fullUnblankedName() + "                         ";
            strNF = strNF.substring(0, 22);
            strLine += " " + strNF;

            String strRank = "   " + Player.convertIntToKD(sP.getRank());
            strRank = strRank.substring(strRank.length() - 3);
            strLine += strRank;


            String strCountry = sP.getCountry().trim();
            if (strCountry.length() < 2 ) strCountry = "XX";
            strCountry = "  " + strCountry;
            strCountry = strCountry.substring(strCountry.length() - 2);
            strLine += " " + strCountry;

            String strClub = sP.getClub().trim();
            if (strClub.length() < 2 ) strClub = "xxxx";
            strClub = "    " + strClub;
            strClub = strClub.substring(strClub.length() - 4);
            strLine += " " + strClub;

            for (int c = 0; c < tabCrit.length; c++) {
                String strCritValue = sP.formatScore(tabCrit[c], roundNumber);
                // Make strings with exactly 4 characters
                strCritValue = strCritValue.trim();
                if (strCritValue.length() > 4) {
                    strCritValue = strCritValue.substring(0, 4);
                }
                while (strCritValue.length() < 4) {
                    strCritValue = " " + strCritValue;
                }

                strLine += " " + strCritValue;
            }
            // If tabCrit.length < 4, fill with dummy values
            for (int c = tabCrit.length; c < 4; c++) {
                strLine += " 0";
            }

            for (int r = 0; r <= roundNumber; r++) {
                String strHG = hG[r][iSP];
                strHG = "        " + strHG;
                strHG = strHG.substring(strHG.length() - 8);
                // Drop the game if by def and !bKeepByDefResults
                if (strHG.indexOf("!") >= 0 && !bKeepByDefResults) {
                    strHG = "      0=";
                }
                strLine += " " + strHG;

            }

            try {
                output.write("\n" + strLine);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            output.write("\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void generatePlayersCSVFile(TournamentInterface tournament, File f) {
        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(f));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        ArrayList<Player> alPlayers = null;
        try {
            alPlayers = tournament.playersList();
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return;
        }
        PlayerComparator playerComparator = new PlayerComparator(PlayerComparator.RANK_ORDER);
        Collections.sort(alPlayers, playerComparator);
        // Column names

        String strLine = "";
        strLine += "Name" + ";";
        strLine += "FirstName" + ";";
        strLine += "Rank" + ";";
        strLine += "Country" + ";";
        strLine += "Club" + ";";

        try {
            output.write("\n" + strLine);
        } catch (IOException ex) {
            ex.printStackTrace();
        }



        for (Player p : alPlayers) {
            strLine = "";
            strLine += p.getName() + ";";
            strLine += p.getFirstName() + ";";
            strLine += Player.convertIntToKD(p.getRank()) + ";";
            strLine += p.getCountry() + ";";
            strLine += p.getClub() + ";";

            try {
                output.write("\n" + strLine);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            output.write("\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void generateHTMLFile(TournamentInterface tournament, File f) {
        TournamentParameterSet tps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return;
        }
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        PlacementParameterSet pps = tps.getPlacementParameterSet();

        // Prepare tabCrit from pps
        int[] tC = pps.getPlaCriteria();
        int nbC = 0;
        for (int c = 0; c < tC.length; c++) {
            if (tC[c] != PlacementParameterSet.PLA_CRIT_NUL) {
                nbC++;
            }
        }
        int[] tabCrit;
        if (nbC == 0) {
            tabCrit = new int[1];
            tabCrit[0] = PlacementParameterSet.PLA_CRIT_NUL;
        } else {
            tabCrit = new int[nbC];
            tabCrit[0] = PlacementParameterSet.PLA_CRIT_NUL;
            int crit = 0;
            for (int c = 0; c < tC.length; c++) {
                if (tC[c] != PlacementParameterSet.PLA_CRIT_NUL) {
                    tabCrit[crit++] = tC[c];
                }
            }
        }

        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(f));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // Headers       
        try {
            output.write("<html>");
            output.write("<head>");
            output.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
            output.write("<title>" + gps.getName() + "</title>");
            output.write("<link href=\"current.css\" rel=\"stylesheet\" type=\"text/css\">");
            output.write("</head>");

            output.write("<body>");
            output.write("<h1 align=\"center\">" + gps.getName() + "</h1>");
            output.write("<table align=\"center\" class=\"simple\">");
            output.write("\n<th class=\"left\">&nbsp;Pl&nbsp;</th>" + "<th class=\"middle\">&nbsp;Name&nbsp;</th>" + "<th class=\"middle\">&nbsp;Rank&nbsp;</th>" + "<th class=\"middle\">&nbsp;Co </th>" + "<th class=\"middle\">&nbsp;Club&nbsp;</th>" + "<th class=\"middle\">&nbsp;NbW&nbsp;</th>");
            for (int r = 0; r < gps.getNumberOfRounds(); r++) {
                output.write("<th class=\"middle\">R&nbsp;" + (r + 1) + "&nbsp;</th>");
            }
            for (int c = 0; c < tabCrit.length - 1; c++) {
                output.write("<th class=\"middle\">" + PlacementParameterSet.criterionShortName(tabCrit[c]) + "</th>");
            }
            output.write("<th class=\"right\">" + PlacementParameterSet.criterionShortName(tabCrit[tabCrit.length - 1]) + "</th>");

            output.write("\n</tr>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Contents
        ArrayList<ScoredPlayer> alOrderedScoredPlayers = null;
        int roundNumber = gps.getNumberOfRounds() - 1;
        try {
            alOrderedScoredPlayers = tournament.orderedScoredPlayersList(roundNumber, pps);
            // Eliminate non-players
            for (Iterator<ScoredPlayer> it = alOrderedScoredPlayers.iterator(); it.hasNext();) {
                ScoredPlayer sP = it.next();
                if (!tournament.isPlayerImplied(sP)) {
                    it.remove();
                }
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }

        String[][] hG = ScoredPlayer.halfGamesStrings(alOrderedScoredPlayers, roundNumber, tps);
        String[] strPlace = ScoredPlayer.positionStrings(alOrderedScoredPlayers, roundNumber, tps);
        try {
            for (int iSP = 0; iSP < alOrderedScoredPlayers.size(); iSP++) {
                output.write("\n<tr>");
                String strPar = "pair";
                ScoredPlayer sP = alOrderedScoredPlayers.get(iSP);
                if (iSP % 2 == 0) {
                    strPar = "impair";
                }
                String strAlCenter = " align=\"center\"";

                output.write("<td class=" + strPar + " align=\"right\">" + strPlace[iSP] + "&nbsp;</td>");
                String strNF = sP.fullName();
                output.write("<td class=" + strPar + ">" + strNF + "</td>");
                String strRank = Player.convertIntToKD(sP.getRank());
                output.write("<td class=" + strPar + strAlCenter + ">" + strRank + "</td>");
                String strCountry = sP.getCountry();
                output.write("<td class=" + strPar + strAlCenter + ">" + strCountry + "</td>");
                String strClub = sP.getClub();
                output.write("<td class=" + strPar + strAlCenter + ">" + strClub + "</td>");
                output.write("<td class=" + strPar + strAlCenter + ">" + sP.formatScore(PlacementParameterSet.PLA_CRIT_NBW, roundNumber) + "</td>");

                for (int r = 0; r <= roundNumber; r++) {
                    String strHG = hG[r][iSP];
                    if (strHG.indexOf('+') > 0) {
                        strHG = "<b>" + strHG + "</b>";
                    }
                    output.write("<td class=" + strPar + strAlCenter + ">" + strHG + "</td>");
                }
                for (int c = 0; c < tabCrit.length; c++) {
                    String strCritValue = sP.formatScore(tabCrit[c], roundNumber);
                    output.write("<td class=" + strPar + strAlCenter + ">" + strCritValue + "</td>");

                }

                output.write("</tr>");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            output.write("\n</table>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {

            output.write("<h4 align=center>" + Gotha.getGothaVersionnedName() + "<br>" + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()) + "</h4>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            output.write("\n</body></html>");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void generateXMLFile(TournamentInterface tournament, File xmlFile) {
        DocumentBuilderFactory documentBuilderFactory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document document = documentBuilder.newDocument();

        Element rootElement = document.createElement("Tournament");
        document.appendChild(rootElement);

        // Include players
        ArrayList<Player> alPlayers = null;
        try {
            alPlayers = tournament.playersList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        Element emPlayers = generateXMLPlayersElement(document, alPlayers);
        rootElement.appendChild(emPlayers);

        // Include games
        ArrayList<Game> alGames = null;
        try {
            alGames = tournament.gamesList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        Element emGames = generateXMLGamesElement(document, alGames);
        rootElement.appendChild(emGames);

        // Include bye players if any
        Player[] byePlayers = null;
        try {
            byePlayers = tournament.getByePlayers();
        } catch (RemoteException ex) {
            Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
        Element emByePlayers = generateXMLByePlayersElement(document, byePlayers);
        if (emByePlayers != null) {
            rootElement.appendChild(emByePlayers);
        }

        // Include tournament parameters
        TournamentParameterSet tps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
        Element emTournamentParameterSet = generateXMLTournamentParameterSetElement(document, tps);
        rootElement.appendChild(emTournamentParameterSet);

        // Transform document into a DOM source
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "tournament.dtd");
            transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        DOMSource source = new DOMSource(document);

        // generate file
        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(xmlFile));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        StreamResult result = new StreamResult(output);
        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(ExternalTournamentDocument.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Generates an xml players Liste Element and includes all players from alPlayers
     * returns the Element
     */
    private static Element generateXMLPlayersElement(Document document, ArrayList<Player> alPlayers) {
        Element emPlayers = document.createElement("Players");
        for (Player p : alPlayers) {
            String strName = p.getName();
            String strFirstName = p.getFirstName();
            String strCountry = p.getCountry();
            String strClub = p.getClub();
            String strRank = Player.convertIntToKD(p.getRank());
            String strRating = Integer.valueOf(p.getRating()).toString();
            String strRatingOrigin = p.getRatingOrigin();
            String strSMMSCorrection = Integer.valueOf(p.getSmmsCorrection()).toString();
            boolean[] part = p.getParticipating();
            String strParticipating = "";
            for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
                if (part[r]) {
                    strParticipating += "1";
                } else {
                    strParticipating += "0";
                }
            }
            String strRegisteringStatus = p.getRegisteringStatus();

            Element emPlayer = document.createElement("Player");
            emPlayer.setAttribute("name", strName);
            emPlayer.setAttribute("firstName", strFirstName);
            emPlayer.setAttribute("country", strCountry);
            emPlayer.setAttribute("club", strClub);
            emPlayer.setAttribute("rank", strRank);
            emPlayer.setAttribute("rating", strRating);
            emPlayer.setAttribute("ratingOrigin", strRatingOrigin);
            emPlayer.setAttribute("smmsCorrection", strSMMSCorrection);
            emPlayer.setAttribute("participating", strParticipating);
            emPlayer.setAttribute("registeringStatus", strRegisteringStatus);

            emPlayer.appendChild(document.createTextNode(p.canonicalName()));
            emPlayers.appendChild(emPlayer);
        }

        return emPlayers;

    }

    /**
     * Generates an xml games list Element and includes all players from alPlayers()
     * returns the Element
     */
    private static Element generateXMLGamesElement(Document document, ArrayList<Game> alGames) {
        Element emGames = document.createElement("Games");
        for (Game g : alGames) {
            String strRoundNumber = Integer.valueOf(g.getRoundNumber() + 1).toString();
            String strTableNumber = Integer.valueOf(g.getTableNumber() + 1).toString();
            String strWhitePlayer = g.getWhitePlayer().canonicalName();
            String strBlackPlayer = g.getBlackPlayer().canonicalName();
            String strKnownColor = g.isKnownColor() ? "true" : "false";
            String strHandicap = Integer.valueOf(g.getHandicap()).toString();
            String strResult = "RESULT_UNKNOWN";
            switch (g.getResult()) {
                case Game.RESULT_WHITEWINS:
                    strResult = "RESULT_WHITEWINS";
                    break;
                case Game.RESULT_BLACKWINS:
                    strResult = "RESULT_BLACKWINS";
                    break;
                case Game.RESULT_EQUAL:
                    strResult = "RESULT_EQUAL";
                    break;
                case Game.RESULT_BOTHWIN:
                    strResult = "RESULT_BOTHWIN";
                    break;
                case Game.RESULT_BOTHLOSE:
                    strResult = "RESULT_BOTHLOSE";
                    break;
                case Game.RESULT_WHITEWINS_BYDEF:
                    strResult = "RESULT_WHITEWINS_BYDEF";
                    break;
                case Game.RESULT_BLACKWINS_BYDEF:
                    strResult = "RESULT_BLACKWINS_BYDEF";
                    break;
           
