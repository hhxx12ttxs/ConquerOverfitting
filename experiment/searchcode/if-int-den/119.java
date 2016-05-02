package okoelopoly;

/**
 * 
 * Diese Klasse dient als Kommunikationsobjekt zwischen der Simulatorklasse
 * Kybernetien und einer Spielstrategie (Individuum).
 * 
 * Fuer eine Iteration im Spiel wird von der Klasse Kybernetien der aktuelle
 * Status in ein Objekt der Klasse Punktverteilung kopiert und der Methode
 * wendeDieStrategieAn eines Individuums uebergeben. Auf der Basis von
 * Statusabfragen mit den get-Methoden dieser Klasse, trifft das Individuum
 * Entscheidungen, wo die verfuegbaren Aktionspunkte investiert werden sollen.
 * Diese Entscheidungen werden dem Objekt der Klasse Punkteverteilung ueber die
 * investiere-Methoden bekannt gemacht (bzw. iterativ aufgebaut). Anschließend
 * liest die Klasse Kybernetien diese Entscheidungen ueber die
 * getPunkte-Methoden aus und setzt sie im Simulator um.
 * 
 * @author Karsten Weicker
 * @version 12-12-08 22:34
 */

public class Punktverteilung {
    private int aktionspunkte;
    private int sanierungPunkte = 0;
    private int produktionPunkte = 0;
    private int aufklaerungPunkte = 0;
    private int lebensqualitaetPunkte = 0;
    private int vermehrungsratePunkte = 0;
    private int sanierung;
    private int produktion;
    private int aufklaerung;
    private int lebensqualitaet;
    private int vermehrungsrate;
    private int umweltbelastung;
    private int bevoelkerung;
    private int politik;
    private double bevSollWachsen = 0.0;

    Punktverteilung(int aktionspunkte, int sanierung, int produktion,
            int aufklaerung, int lebensqualitaet, int vermehrungsrate,
            int umweltbelastung, int bevoelkerung, int politik) {
        this.aktionspunkte = aktionspunkte;
        this.sanierung = sanierung;
        this.produktion = produktion;
        this.aufklaerung = aufklaerung;
        this.lebensqualitaet = lebensqualitaet;
        this.vermehrungsrate = vermehrungsrate;
        this.umweltbelastung = umweltbelastung;
        this.bevoelkerung = bevoelkerung;
        this.politik = politik;
    }

    /**
     * Liefert die Aktionspunkte, die bei der aktuellen Verteilung von Punkten
     * noch zur Verfuegung stehen. Diese Anzahl aendert sich durch Verteilung
     * der Punkte mit den investiere...-Methoden. Es muessen nicht alle Punkte
     * verteilt werden. Maximal sind 36 Aktionspunkte moeglich.
     * 
     * @return Anzahl der verfuegbaren Aktionspunkte
     */
    public int getAktionspunkte() {
        return aktionspunkte;
    }

    /**
     * Addiert die uebergebene Punktzahl zu den in Sanierung zu investierenden
     * Punkten. Die Aktionspunkte und die Gesamtpunktzahl der Sanierung werden
     * aktualisiert. Ueberschreitet die Punktzahl die verfuegbaren
     * Aktionspunkte, findet kein Transfer der Punkte statt. Wuerden die
     * investierten Punkte das Maximum von 29 Punkten ueberschreiten, wird nur
     * bis zu 29 Punkten aufgefuellt.
     * 
     * @param punkte
     *            In die Sanierung zu investierenden Punkte
     */
    public void investiereInSanierung(int punkte) {
        if (punkte > 0 && punkte <= aktionspunkte) {
            if (sanierung + punkte <= 29) {
                sanierungPunkte += punkte;
                sanierung += punkte;
                aktionspunkte -= punkte;
            } else {
                aktionspunkte -= 29 - sanierung;
                sanierungPunkte += 29 - sanierung;
                sanierung = 29;
            }
        }
    }

    /**
     * Analog zu investiereInSanierung mit Produktion statt Sanierung
     * 
     * @param punkte
     *            In die Produktion zu investierenden Punkte
     */
    public void investiereInProduktion(int punkte) {
        if (punkte > 0 && punkte <= aktionspunkte) {
            if (produktion + punkte <= 29) {
                produktion += punkte;
                produktionPunkte += punkte;
                aktionspunkte -= punkte;
            } else {
                aktionspunkte -= 29 - produktion;
                produktionPunkte += 29 - produktion;
                produktion = 29;
            }
        }
    }

    /**
     * Produktionskraft der Wirtschaft wird durch die Aktionspunkte verkleinert.
     * Die Produktion ist der einzige investierbare Bereich, in dem die
     * Punktzahl auch verringert werden kann. Ueberschreitet die Punktzahl die
     * verfuegbaren Aktionspunkte, findet kein Transfer der Punkte statt.
     * Wuerden die investierten Punkte das Minimum von 1 Punkten unterschreiten,
     * werden entsprechend weniger Punkte investiert.
     * 
     * @param punkte
     *            Gegen die Produktion zu investierenden Punkte
     */
    public void investiereGegenProduktion(int punkte) {
        if (punkte > 0 && punkte <= aktionspunkte) {
            if (produktion > punkte) {
                produktion -= punkte;
                produktionPunkte -= punkte;
                aktionspunkte -= punkte;
            } else {
                aktionspunkte -= (produktion - 1);
                produktionPunkte -= (produktion - 1);
                produktion = 1;
            }
        }
    }

    /**
     * Analog zu investiereInSanierung mit Aufklaerung statt Sanierung
     * 
     * @param punkte
     *            In die Aufklaerung zu investierenden Punkte
     */
    public void investiereInAufklaerung(int punkte) {
        if (punkte > 0 && punkte <= aktionspunkte) {
            if (aufklaerung + punkte <= 29) {
                aufklaerung += punkte;
                aufklaerungPunkte += punkte;
                aktionspunkte -= punkte;
            } else {
                aktionspunkte -= 29 - aufklaerung;
                aufklaerungPunkte += 29 - aufklaerung;
                aufklaerung = 29;
            }
        }
    }

    /**
     * Analog zu investiereInSanierung mit Lebensqualitaet statt Sanierung
     * 
     * @param punkte
     *            In die Lebensqualitaet zu investierenden Punkte
     */
    public void investiereInLebensqualitaet(int punkte) {
        if (punkte > 0 && punkte <= aktionspunkte) {
            if (lebensqualitaet + punkte <= 29) {
                lebensqualitaet += punkte;
                lebensqualitaetPunkte += punkte;
                aktionspunkte -= punkte;
            } else {
                aktionspunkte -= 29 - lebensqualitaet;
                lebensqualitaetPunkte += 29 - lebensqualitaet;
                lebensqualitaet = 29;
            }
        }
    }

    /**
     * Analog zu investiereInSanierung mit Vermehrungsrate statt Sanierung
     * 
     * @param punkte
     *            In die Vermehrungsrate zu investierenden Punkte
     */
    public void investiereInVermehrungsrate(int punkte) {
        if (punkte > 0 && punkte <= aktionspunkte) {
            if (vermehrungsrate + punkte <= 29) {
                vermehrungsrate += punkte;
                vermehrungsratePunkte += punkte;
                aktionspunkte -= punkte;
            } else {
                aktionspunkte -= 29 - vermehrungsrate;
                vermehrungsratePunkte += 29 - vermehrungsrate;
                vermehrungsrate = 29;
            }
        }
    }

    /**
     * Durch Setzen eines Werts mit dieser Methode kann in bestimmten
     * Situationen im Simulator beeinflusst werden, ob der hohe Grad der Bildung
     * fuer oder gegen ein Bevoelkerungswachstum eingesetzt werden soll. Der
     * Wert liegt zwischen -1.0 und 1.0. Dabei steht -1.0 fuer einen maximalen
     * Einsatz, die Vermehrungsrate negativ zu beeinflussen. 1.0 steht fuer den
     * maximalen Einsatz, die Vermehrungsrate zu vergrößern. Der Wert 0.0
     * bedeutet, dass in den gegebenen Situationen die Bildung nicht zur
     * Beeinflussung der Vermehrungsrate benutzt wird. Alle Zwischenwerte sind
     * erlaubt.
     * 
     * @param grad
     *            Faktor zur Auswirkung der Bildung
     */
    public void nutzeAufklaerungFuerBevoelkerungsWachstum(double grad) {
        if (grad < -1.0)
            bevSollWachsen = -1.0;
        else {
            if (grad > 1.0)
                bevSollWachsen = 1.0;
            else
                bevSollWachsen = grad;
        }
    }

    /**
     * Liefert die Masszahl fuer den Zustand des Lebensraums. Fasst Aspekte wie
     * Umweltschutz, Recycling, Biotechnik, sanfte Energie, Landschaftserhaltung
     * und Humanisierung der Arbeitswelt zusammen. Niedrigster Wert 1 entspricht
     * der vollstaendigen Ausbeutung des Lebensraums. Hoechster Wert 29
     * entspricht einem intakten Lebensraum mit angepasster Technologie.
     * 
     * @return Zustand des Lebensraums
     */
    public int getSanierung() {
        return sanierung;
    }

    /**
     * Liefert die Masszahl fuer den Zustand der Wirtschaft. Fasst Aspekte wie
     * Industrie, Handwerk, Landwirtschaft und Dienstleistung zusammen.
     * Niedrigster Wert 1 entspricht dem Stillstand der Wirtschaft. Hoechster
     * Wert 29 entspricht Überkapazitäten und Konsumterror.
     * 
     * @return Zustand der Wirtschaft
     */
    public int getProduktion() {
        return produktion;
    }

    /**
     * Liefert die Masszahl fuer den Zustand der Bildung. Fasst Aspekte wie
     * gesunde Lebensweise, Selbstverwirklichung, Schulen, Erwachsenenbildung,
     * Umweltbewusstsein, sinnvolle Freizeit, Buergerinitiativen und
     * Geburtenkontrolle zusammen. Niedrigster Wert 1 entspricht kurzsichtigem
     * und unvernetzten Denken. Hoechster Wert 29 entspricht kybernetischer
     * Einsicht und hohem Informationsstand.
     * 
     * @return Zustand der Bildung
     */
    public int getAufklaerung() {
        return aufklaerung;
    }

    /**
     * Liefert die Masszahl fuer den Zustand der Lebensqualitaet. Fasst Aspekte
     * wie Gesundheit, Sicherheit, sinnvolle Arbeit, Wohnqualitaet, Naherhholung
     * und Freizeitangebote zusammen. Niedrigster Wert 1 entspricht dem
     * Zusammenbruch der Sozialstruktur (Hunger und Not). Hoechster Wert 29
     * entspricht hoechstem Wohlbefinden und sinnerfuelltem Leben.
     * 
     * @return Zustand der Lebensqualitaet
     */
    public int getLebensqualitaet() {
        return lebensqualitaet;
    }

    /**
     * Liefert die Masszahl fuer die Veraenderung der Bevoelkerungsgroesse.
     * Fasst Aspekte wie Geburten, Sterbefaelle, Unfaelle und Zu- und
     * Abwanderung zusammen. Niedrigster Wert 1 entspricht einer stark
     * ruecklaeufigen Bevoelkerung. Hoechster Wert 29 entspricht einer
     * Bevoelkerungsexplosion.
     * 
     * @return Zustand der Vermehrungsrate
     */
    public int getVermehrungsrate() {
        return vermehrungsrate;
    }

    /**
     * Liefert die Masszahl fuer die Umweltbelastung. Fasst Aspekte wie Abgase,
     * Abwaesser, Abwaerme, Laerm, Raubbau, Landschaftszerstoerung, Trennung
     * natuerlicher Kreislaeufe, Verkehrschaos und Staedtezerfall zusammen.
     * Niedrigster Wert 1 entspricht ungestoerten Ökosystemen. Hoechster Wert
     * 29 entspricht dem Zusammenbruch der natuerlichen Regulation.
     * 
     * @return Zustand der Umweltbelastung
     */
    public int getUmweltbelastung() {
        return umweltbelastung;
    }

    /**
     * Liefert die Masszahl fuer die Bevoelkerung selbst. Fasst Aspekte wie
     * Bevoelkerungszahl, Menschendichte, Altersaufbau, Arbeitskraefte und
     * Sozialstruktur zusammen. Niedrigster Wert 1 entspricht einem
     * menschenleeren Raum. Hoechster Wert 48 entspricht einer extremen
     * Bevoelkerungsdichte und ueberbevoelkerten Ballungsgebieten.
     * 
     * @return Zustand der Bevoelkerung
     */
    public int getBevoelkerung() {
        return bevoelkerung;
    }

    /**
     * Liefert die Masszahl fuer das politische Bewusstsein. Fasst Aspekte wie
     * Weitsicht, Autoritaet, Beliebtheit, einsichtige Programme und
     * Entscheidungsgewalt zusammen. Niedrigster Wert -10 entspricht einer
     * Entmachtung durch einen Staatsstreich. Hoechster Wert 37 entspricht einer
     * perfekten Regierung.
     * 
     * @return Zustand der Politik
     */
    public int getPolitik() {
        return politik;
    }

    /**
     * Liefert die aktuell durch investiere...-Methoden angesammelte Punktzahl.
     * 
     * @return Fuer die Sanierung aktuell benutzte Aktionspunkte
     */
    int getPunkteSanierung() {
        return sanierungPunkte;
    }

    /**
     * Liefert die aktuell durch investiere...-Methoden angesammelte Punktzahl.
     * 
     * @return Fuer die Aufklaerung aktuell benutzte Aktionspunkte
     */
    int getPunkteAufklaerung() {
        return aufklaerungPunkte;
    }

    /**
     * Liefert die aktuell durch investiere...-Methoden angesammelte Punktzahl.
     * 
     * @return Fuer die Produktion aktuell benutzte Aktionspunkte
     */
    int getPunkteProduktion() {
        return produktionPunkte;
    }

    /**
     * Liefert die aktuell durch investiere...-Methoden angesammelte Punktzahl.
     * 
     * @return Fuer die Lebensqualitaet aktuell benutzte Aktionspunkte
     */
    int getPunkteLebensqualitaet() {
        return lebensqualitaetPunkte;
    }

    /**
     * Liefert die aktuell durch investiere...-Methoden angesammelte Punktzahl.
     * 
     * @return Fuer die Vermehrungsrate aktuell benutzte Aktionspunkte
     */
    int getPunkteVermehrungsrate() {
        return vermehrungsratePunkte;
    }

    /**
     * Gibt dem Simulator die Information, in welche Richtung bei Wahlfreiheit
     * die Vermehrungsrate beeinflusst werden soll.
     * 
     * @return Grad des Wachsens/Schrumpfens
     */
    double getBevSollWachsen() {
        return bevSollWachsen;
    }
}

