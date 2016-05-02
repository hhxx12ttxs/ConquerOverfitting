
package pubventure;

import java.util.ArrayList;
import java.util.Random;
import javax.swing.SwingUtilities;
import pubventure.enumit.InehmoEnum;
import pubventure.enumit.KomentoEnum;
import pubventure.enumit.PubiobjektiEnum;
import pubventure.gui.Kayttoliittyma;
import pubventure.ihmiset.Inehmo;
import pubventure.ihmiset.InehmojenHallinnointi;
import pubventure.ihmiset.Sankari;
import pubventure.reittialgot.Astar;
import pubventure.reittialgot.Dijkstra;
import pubventure.ymparisto.Pubi;
import pubventure.ymparisto.Pubiobjekti;

/**
 *
 * @author Santeri
 *
 * Luokka toimii pääasiallisena ohjelman toiminnan ohjaajana. Se luo Pubi-luokan
 * ja suorittaa sen ne metodit, jotka luovat pelikentän, sekä luo swing-ikkunaa
 * hallitsevan Kayttoliittyma-luokan.
 */
public class Logiikka {

    private Pubi pubi;
    private int siirtoja;
    private boolean asiakkaatLiikkuvat;
    private ArrayList<Inehmo> inehmot = new ArrayList<Inehmo>();
    private Random arpoja = new Random();
    private Kayttoliittyma kl;
    private KomentoEnum[] komennot;
    private Sankari sankari;
    private Astar astar;
    private Dijkstra dijkstra;
    private Pubiobjekti[] reitti;
    private boolean reititNakyvilla;
    private boolean kansoitettu;
    private InehmojenHallinnointi ih;
    private Pubiobjekti maali;
    private Pubiobjekti lahto;
    private boolean paivita = true;

    public Logiikka(int asiakkaita, int siirtoja, boolean asiakkaatLiikkuvat, boolean kansoitettu) {
        this.siirtoja = siirtoja;
        this.asiakkaatLiikkuvat = asiakkaatLiikkuvat;
        this.kansoitettu = kansoitettu;
        this.komennot = KomentoEnum.values();

        this.pubi = new Pubi(asiakkaita, inehmot);
        
        this.ih = new InehmojenHallinnointi(pubi, asiakkaita, inehmot, asiakkaatLiikkuvat, this);
        if (kansoitettu) {
            ih.luoHahmot();
        }
        
        this.inehmot = ih.getInehmot();
        this.sankari = (Sankari) inehmot.get(0);

        this.kl = new Kayttoliittyma(pubi, inehmot, sankari, ih, this);
        this.astar = new Astar(pubi);
        this.dijkstra = new Dijkstra(pubi);
        
        this.maali = pubi.etsiPubiobjekti(PubiobjektiEnum.PISUAARI);
    }

    /**
     * Aloittaa pelin suorittamisen luomalla käyttöliittymän
     */
    public void aloita() {

        kl.setViestiKentanSisalto(KomentoEnum.LIIKE, " ");
        SwingUtilities.invokeLater(kl);
    }

    /**
     * Kutsuu komennon tyypin edellyttämää metodia, sekä piirtää kentän
     * uudestaan.
     *
     * @param komento on Nappaimistonkuuntelijalta Kayttoliittyman kautta tullut
     * komento
     */
    public void kasitteleKomento(KomentoEnum komento) {
        if (komento == KomentoEnum.POHJOINEN || komento == KomentoEnum.ITA
                || komento == KomentoEnum.ETELA || komento == KomentoEnum.LANSI
                || komento == KomentoEnum.KOILLINEN || komento == KomentoEnum.KAAKKO
                || komento == KomentoEnum.LOUNAS || komento == KomentoEnum.LUODE
                || komento == KomentoEnum.ODOTUS) {
            if (reititNakyvilla) {
                pubi.palautaPOUlkomuodot();
            }
            if (!ih.getInehmojenNakyvyys()) {
                kl.setInehmotNakyviksi();
            }
            kl.setViestiKentanSisalto(KomentoEnum.LIIKE, "");
            kasitteleLiikekomento(komento, inehmot.get(0));

        }

        switch (komento) {
            /**
             * Tässä tapauksessa jäädään odottamaan, että käyttäjä antaa
             * kaksivaiheisen komennon jälkimmäisen osan, eli suunnan.
             * Viestikenttään päivitetään kehotus suunnan antamiseen.
             */
            case SUUNTA:
                if (komento == KomentoEnum.SUUNTA) {
                    kl.setViestiKentanSisalto(KomentoEnum.SUUNTA, "");
                }
                break;
            case ODOTUS:
                kl.setViestiKentanSisalto(KomentoEnum.ODOTUS, "");
                break;
            case OHJE:
                kl.setViestiKentanSisalto(KomentoEnum.OHJE, "");
                return;
            case JUO:
                if (sankari.getOnkoRakkoTaynna()) {
                    kerroMikaHatana();
                } else {
                    if (sankari.getJuomat() > 0) {
                        sankari.setJuomat(-1);
                        sankari.setJuomatVatsassa(1);
                        kl.kirjoitaPelaajanTiedot();
                        kl.setViestiKentanSisalto("Hörp.");
                    } else {
                        kl.setViestiKentanSisalto("Tuoppisi on tyhjä!");
                    }
                }
                break;
            case PERU:
                kl.kirjoitaPelaajanTiedot();
                kl.setViestiKentanSisalto(KomentoEnum.LIIKE, "");
                return;
            case ASTAR:
                if (reititNakyvilla) {
                    pubi.palautaPOUlkomuodot();
                }
                long aikaAlussa = System.currentTimeMillis();
                
                this.lahto = pubi.getObjekti(sankari.getSijainti());
                this.astar.etsiReitti(lahto, maali);

                long aikaLopussa = System.currentTimeMillis();
                kl.setViestiKentanSisalto("A*: " + (aikaLopussa - aikaAlussa) + "ms");
                kl.setInehmotNakymattomiksi();
                kl.piirraAlue();
                this.reititNakyvilla = true;
                System.gc();
                break;
            case DIJKSTRA:
                if (reititNakyvilla) {
                    pubi.palautaPOUlkomuodot();
                }
                long dAikaAlussa = System.currentTimeMillis();
                
                this.lahto = pubi.getObjekti(sankari.getSijainti());
                this.dijkstra.etsiReitti(lahto, maali);
                
                long dAikaLopussa = System.currentTimeMillis();
                kl.setViestiKentanSisalto("Dijkstra: " + (dAikaLopussa - dAikaAlussa) + "ms");
                kl.setInehmotNakymattomiksi();
                kl.piirraAlue();
                this.reititNakyvilla = true;
                System.gc();
                break;
            case PYYHI:
//                if (reititNakyvilla) {
                    pubi.palautaPOUlkomuodot();
//                }
                if (!ih.getInehmojenNakyvyys()) {
                    kl.setInehmotNakyviksi();
                }
                break;
            case TIEDOT:
                kl.setViestiKentanSisalto("Oma sijainti &nbsp;&nbsp;&nbsp;&nbsp;"
                        + "X: " + sankari.getSijainti().getX() + " "
                        + "Y: " + sankari.getSijainti().getY()
                        + "<br>"
                        + "Maalin sijainti &nbsp;"
                        + "X: " + maali.getSijainti().getX() + " "
                        + "Y: " + maali.getSijainti().getX());
                this.paivita = false;
                break;
        }
        if (paivita) {
            paivita();
        } else {
            this.paivita = true;
        }
        
    }

    /**
     * Käsitellään kaksivaiheiset komennot
     *
     * @param komento on NappaimistonKuuntelijan Kayttoliittyma-luokan kautta
     * välittämä komento
     * @see
     * Pubventure.gui.NappaimistonKuuntelija#keyPressed(java.awt.event.KeyEvent)
     * @see
     * Pubventure.gui.Kayttoliittyma#valitaKaksivaiheinenKomento(Pubventure.enumit.KomentoEnum,
     * Pubventure.enumit.KomentoEnum)
     *
     * @param sijainti on Kayttoliittyma-luokan etsimä Sijainti
     * @see Pubventure.Sijainti
     */
    public void kasitteleKaksivaiheinenKomento(KomentoEnum komento, Sijainti sijainti) {
        Inehmo kohde = etsiInehmoAnnetussaSijainnissa(sijainti);
        switch (komento) {
            case OSTA:
                komentoOsta(sijainti);
                break;
            case KUSE:
                komentoKuse(sijainti);
                break;
            case PUMMI:
                komentoPummi(kohde);
                break;
            case PUHU:
                komentoPuhu(kohde);
                break;
            case TUTKI:
                komentoTutki(kohde, sijainti);
                break;
            case PERU:
                kl.setViestiKentanSisalto(KomentoEnum.LIIKE, "");
                break;
            case VONKAA:
                komentoVonkaa(kohde);
                break;
            case MAALI:
                komentoAsetaMaali(sijainti);
                break;
//            case ANNA:
//                break;
        }
    }

    /**
     * Hoitaa ostotapahtuman. Tämä onnistuu, mikäli hahmolla on tarpeeksi rahaa,
     * ja mikäli kohteena on baaritiski
     *
     * @param sijainti on toiminnan kohde
     */
    private void komentoOsta(Sijainti sijainti) {
        if (pakkoMennaKuselle()) {
            kerroMikaHatana();
        } else {
            if (pubi.getObjekti(sijainti).getTyyppi() == PubiobjektiEnum.TISKI) {
                if (sankari.maksa(-5)) {
                    sankari.setJuomat(5);
                    kl.kirjoitaPelaajanTiedot();
                    kl.setViestiKentanSisalto("Ostit oluen!");
                    paivita();
                } else {
                    kl.setViestiKentanSisalto("Sinulla ei ole varaa!");
                }

            } else {
                kl.setViestiKentanSisalto("Et ole baaritiskillä.");
            }
        }
    }

    /**
     * Hoitaa ns. veden heiton. Onnistuu mikäli annetussa suunnassa on joko
     * wc-pytty tai pisuaari. Lienee turha mainita, että muidenkin kohteiden
     * sallimista on mietitty.
     *
     * @param sijainti on toiminnan kohde
     */
    private void komentoKuse(Sijainti sijainti) {
        if (pubi.getObjekti(sijainti).getTyyppi() == PubiobjektiEnum.PISUAARI
                || pubi.getObjekti(sijainti).getTyyppi() == PubiobjektiEnum.WCPYTTYM
                || pubi.getObjekti(sijainti).getTyyppi() == PubiobjektiEnum.WCPYTTYN) {
            if (sankari.getRakko() > 20) {
                sankari.setRakko(0);
                sankari.setRakkoTaynna(false);
                paivita();
                kl.setViestiKentanSisalto("Aaahh...");
            } else {
                kl.setViestiKentanSisalto("Nyt ei vielä oikein irtoa.");
            }
        }
    }

    /**
     * Hoitaa yritykset pummia rahaa. Onnistuu, mikäli kohteena olevan inehmon
     * asenne on tarpeeksi hyvä ja tämä ei ole portsari.
     *
     * @param kohde on toiminnan kohde
     */
    private void komentoPummi(Inehmo kohde) {
        if (pakkoMennaKuselle()) {
            kerroMikaHatana();
        } else {
            if (kohde == null) {
                kl.setViestiKentanSisalto("Ei siinä ole ketään.");
            } else if (kohde.getTyyppi().equals(InehmoEnum.PORTSARI)) {
                kl.setViestiKentanSisalto("Taitaa olla huono idea.");
            } else {
                if (kohde.getAsenne() > 90) {
                    sankari.otaRahaaVastaan(5);
                    kl.setViestiKentanSisalto("Sait pummittua vitosen!");
                    kohde.setAsenne(-70);
                    kl.kirjoitaPelaajanTiedot();
                    paivita();
                } else {
                    kl.setViestiKentanSisalto("Eipä onnistunut.<br>Kenties sinun pitäisi yrittää vaikuttaa häneen jotenkin?");
                    paivita();
                }
            }
        }
    }

    /**
     * Hoitaa hahmojen välisen kommunikoinnin. Tulos on satunnainen, mutta
     * muutoksen suuruuteen vaikuttaa se kuinka kaukana neutraalista (50)
     * suhtautumisesta ollaan asteikolla 0 (hyvin negatiivinen) - 100 (hyvin
     * positiivinen). Jos kohteen asenne pelaajaan laskee tarpeeksi alas, tämä
     * ei enää suostu puhumaan pelaajalle lainkaan.
     *
     * @param kohde on toiminnan kohde
     */
    private void komentoPuhu(Inehmo kohde) {
        if (pakkoMennaKuselle()) {
            kerroMikaHatana();
        } else {
            if (kohde == null) {
                // jos ketään ei ole annetussa suunnassa
                kl.setViestiKentanSisalto("Puhut itseksesi.");
            } else if (kohde.getTyyppi().equals(InehmoEnum.PORTSARI)) {
                // jos yritetään puhua portsarille
                kl.setViestiKentanSisalto("Portsaria ei tunnu kiinnostavan mitä sinulla on sanottavana.");
            } else {
                // mikäli kohteen asenne on yli 10, eli halukas puhumaan...
                if (kohde.getHalukasPuhumaan()) {
                    double kohteenAsenne = kohde.getAsenne();
                    double vertailuluku = Math.abs(50.0 - kohteenAsenne);
                    int reaktio = arpoja.nextInt(11);
                    // jos arvottu reaktio on hyvä
                    if (reaktio > 4) {
                        kohde.setAsenne(arpoja.nextInt(30) - (vertailuluku / 10));
                        String vastaus = "Keskustelu sujuu hyvin<br>" + kohde.getAsenneKuvaus();
                        kl.setViestiKentanSisalto(vastaus);
                    } // jos arvottu reaktio on huono
                    else {
                        // muutetaan asennetta
                        kohde.setAsenne((vertailuluku / 10) - arpoja.nextInt(25));
                        // jos kyseessä on mies ja asenne on yhä yli 10
                        if (kohde.getSukupuoli().equals(InehmoEnum.MIES) && kohde.getAsenne() > 10) {
                            String vastaus = "Ette oikein löydä yhteistä säveltä.<br>" + kohde.getAsenneKuvaus();
                            kl.setViestiKentanSisalto(vastaus);
                        } // jos kyseessä on nainen ja asenne on yhä yli 10
                        else if (kohde.getSukupuoli().equals(InehmoEnum.NAINEN) && kohde.getAsenne() > 10) {
                            String vastaus = "Kemianne eivät oikein kohtaa.<br>" + kohde.getAsenneKuvaus();
                            kl.setViestiKentanSisalto(vastaus);
                        }
                        if (kohde.getAsenne() <= 10) {
                            kohde.setHalukasPuhumaan(false);
                            kl.setViestiKentanSisalto("Hän kääntyy poispäin.");
                        }
                    }
                } else {
                    kl.setViestiKentanSisalto("Hän ei halua puhua kanssasi.");
                }

            }
            paivita();
        }
    }

    /**
     * Hoitaa niin ympäristön kuin inehmojenkin tutkimisen. Mikäli tutkittavassa
     * suunnassa on joku, kerrotaan tämän tietoja. Jos ei, kerrotaan mitä siinä
     * kohtaa on, esim. lattia, tuoli jne.
     *
     * @param kohde on toiminnan kohde. Voi olla null.
     * @param sijainti on tutkittava sijainti
     */
    private void komentoTutki(Inehmo kohde, Sijainti sijainti) {
        String selite = "Siinä on ";
        if (kohde != null) {
            for (Inehmo inehmo : inehmot) {
                if (inehmo.getSijainti().equals(sijainti)) {
                    selite += inehmo.getSelite() + "<br>" + inehmo.getAsenneKuvaus();
                    break;
                }
            }
            kl.setViestiKentanSisalto(selite);
        } else {
            kl.setViestiKentanSisalto(selite + pubi.getObjekti(sijainti).getSelite());
        }
    }

    /**
     * Hoitaa ns. vonkauksen. Koska tämä on heteronormatiivinen peli, ainoastaan
     * naisia voi yrittää saada mukaansa. Onnistuminen vaatii paitsi onnea, myös
     * parhaimman mahdollisen asenteen pelaajaa kohtaan.
     *
     * @param kohde on toiminnan kohde
     */
    private void komentoVonkaa(Inehmo kohde) {
        if (pakkoMennaKuselle()) {
            kerroMikaHatana();
        } else {
            if (kohde != null) {
                if (kohde.getSukupuoli().equals(InehmoEnum.NAINEN)
                        && kohde.getTyyppi().equals(InehmoEnum.ASIAKAS)
                        && sankari.getAsenne() >= 80) {
                    int satunnaisluku = arpoja.nextInt(2);
                    if (satunnaisluku == 1 && kohde.getAsenne() == 100) {
                        kohde.setVosuus(true);
                        sankari.setVosu(kohde);
                        sankari.setAsenne(100);
                        kl.setViestiKentanSisalto("No niin, nainen lähtee mukaasi! Tämähän alkaa näyttää hyvältä.");
                    } else {
                        kohde.setAsenne(-40);
                        kl.setViestiKentanSisalto("Nainen ei vaikuta pitävän lähentelystäsi.");
                    }
                } else if (kohde.getSukupuoli().equals(InehmoEnum.NAINEN)
                        && kohde.getTyyppi().equals(InehmoEnum.ASIAKAS)
                        && sankari.getAsenne() < 80) {
                    kl.setViestiKentanSisalto("Sinulla ei ole tarpeeksi rohkeutta.");
                } else if (kohde.getSukupuoli().equals(InehmoEnum.MIES)) {
                    kl.setViestiKentanSisalto("Olet liian hetero siihen.");
                } else {
                    kl.setViestiKentanSisalto("Tuntuu huonolta idealta.");
                }
                paivita();
            } else {
                kl.setViestiKentanSisalto("Teet mielikuvaharjoittelua.");
            }
        }
    }

    private void komentoAsetaMaali(Sijainti kohde) {
        this.maali = pubi.getObjekti(kohde);
        kl.setViestiKentanSisalto("Asetit reitinhaulle uuden maalin.");
    }
    
    /**
     * Tutkii josko on hätä
     *
     * @return palauttaa true, jos on aika käydä eriön puolella
     */
    private boolean pakkoMennaKuselle() {
        if (sankari.getOnkoRakkoTaynna()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Kertoo miksi toiminto ei onnistu (rakko on täynnä)
     */
    private void kerroMikaHatana() {
        kl.setViestiKentanSisalto("Sinua kusettaa ihan liikaa!");
    }

    /**
     * Tämä metodi hoitaa kootusti erilaisia tapahtumien ja muuttujien
     * päivityksiä
     */
    public void paivita() {
        //liikutetaan muita kuin sankaria, mikäli ei odoteta jatkokomentoa
        if (!kl.getNappaimistonKuuntelija().getOdotetaanSuuntaKomentoa()) {
            ih.paivita();
//            liikutaHahmoja();
        }
        //päivitetään sankarin muuttujia, kuten humala, rakko jne
        sankari.paivitaMuuttujat();
        //kirjoitetaan pelaajan tiedot
        kl.kirjoitaPelaajanTiedot();
        //päivitetään pelikentän ulkonäkö
        kl.piirraAlue();
    }

    /**
     * Mikäli kyseessä ei ole pelin sankari, ja mikäli inehmon attribuutti
     * sallii liikkumisen, sitä liikutetaan kutsumalla liikekomennon
     * käsittelevää metodia. Mikäli mukana kulkee nainen, huolehditaan siitä
     * että tämä pysyy ns. iholla.
     *
     * @see
     * Pubventure.Logiikka#kasitteleLiikekomento(Pubventure.enumit.KomentoEnum,
     * Pubventure.ihmiset.Inehmo)
     */
    public void liikutaHahmoja() {
        if (asiakkaatLiikkuvat) {
            for (Inehmo inehmo : inehmot) {
                int satunnaisluku = this.arpoja.nextInt(10);
                if (inehmo.getSankaruus() == false
                        && inehmo.getLiikkuvuus() == true
                        && satunnaisluku > 7
                        && inehmo.getVosuus() == false) {
                    kasitteleLiikekomento(arvoLiikesuunta(), inehmo);
                }
                if (inehmo.getVosuus()) {
                    if (onkoSiinaJoku(sankari.getEdellinenSijainti())) {
                        Inehmo edessaOlija = etsiInehmoAnnetussaSijainnissa(sankari.getEdellinenSijainti());
                        edessaOlija.setSijainti(inehmo.getSijainti());
                        inehmo.setSijainti(sankari.getEdellinenSijainti());
                    } else {
                        inehmo.setSijainti(sankari.getEdellinenSijainti());
                    }
                }
            }
        }
    }

    /**
     * Käsittelee liikekomennon. Mikäli halutussa liikkumissuunnassa ei ole
     * estettä, tarkistetaan josko siinä on joku inehmo. Mikäli kumpikaan
     * ehdoista ei täyty, muutetaan ko. inehmon sijaintia. Poikkeuksen tekee
     * komento-enum ODOTUS, joka ei muuta sijaintia mitenkään.
     *
     * @see
     * Pubventure.Logiikka#annaSijaintiHalutussaSuunnassa(Pubventure.enumit.KomentoEnum,
     * Pubventure.ihmiset.Inehmo)
     * @see Pubventure.Logiikka#onkoSiinaJoku(Pubventure.Sijainti)
     * @see Pubventure.Logiikka#tormaakoEsteeseen(Pubventure.Sijainti)
     * @see
     * Pubventure.Logiikka#etsiInehmoAnnetussaSijainnissa(Pubventure.Sijainti)
     *
     * @param komento on halutun liikkumissuunnan komentoenum
     */
    private void kasitteleLiikekomento(KomentoEnum komento, Inehmo inehmo) {
        if (komento != KomentoEnum.ODOTUS) {

            Sijainti sijaintiAnnetussaSuunnassa = annaSijaintiHalutussaSuunnassa(komento, inehmo);
            boolean suunnassaOnJoku = onkoSiinaJoku(sijaintiAnnetussaSuunnassa);

            if (!tormaakoEsteeseen(sijaintiAnnetussaSuunnassa)) {
                if (!onkoSiinaVastakkaisenSukupuolenVessa(inehmo, sijaintiAnnetussaSuunnassa)) {


                    if (!suunnassaOnJoku) {
                        // otetaan sijainti talteen
                        inehmo.setEdellinenSijainti(inehmo.getSijainti());
                        inehmo.setSijainti(sijaintiAnnetussaSuunnassa);
                    } else if (suunnassaOnJoku && inehmo.getSankaruus()) {
                        Inehmo toinen = etsiInehmoAnnetussaSijainnissa(sijaintiAnnetussaSuunnassa);
                        // jos toinen inehmo on liikkuvaa sorttia (ts. ei portsari), vaihdetaan näiden paikkaa päikseen
                        if (toinen.getLiikkuvuus()) {
                            inehmo.setEdellinenSijainti(inehmo.getSijainti());
                            Sijainti apuSijainti = new Sijainti(sankari.getSijainti().getX(), sankari.getSijainti().getY());
                            sankari.setSijainti(toinen.getSijainti());
                            toinen.setSijainti(apuSijainti);
                        }
                    }
                } else {
                    if (inehmo.getSankaruus()) {
                        kl.setViestiKentanSisalto("Siellä on naisten vessa. Jotain rajaa, hei!");
                    }
                }

            }
            // tutkitaan sankarin kohdalla ollaanko uloskäynnissä
            if (inehmo.getSankaruus()) {
                ollaankoUloskaynnissa(inehmo, sijaintiAnnetussaSuunnassa);
            }
        }
    }

    private boolean onkoSiinaVastakkaisenSukupuolenVessa(Inehmo inehmo, Sijainti sijainti) {
        if (inehmo.getSukupuoli() == InehmoEnum.MIES) {
            if (pubi.getObjekti(sijainti).getTyyppi() == PubiobjektiEnum.NVESSA) {
                return true;
            }
        } else if (inehmo.getSukupuoli() == InehmoEnum.NAINEN) {
            if (pubi.getObjekti(sijainti).getTyyppi() == PubiobjektiEnum.MVESSA) {
                return true;
            }
        }
        return false;
    }

    /**
     * Apumetodi metodille kasitteleLiikekomento. Hoitaa uloskäynnin toiminnot,
     * eli pelin lopetusrutiinien käynnistämisen, tai vaihtoehtoisesti estää
     * lähtemästä jos on vielä juomia jäljellä.
     *
     * @param inehmo on sankarimme
     * @param sijaintiAnnetussaSuunnassa on suunta johon edettiin
     */
    private void ollaankoUloskaynnissa(Inehmo inehmo, Sijainti sijaintiAnnetussaSuunnassa) {
        if (pubi.getObjekti(sijaintiAnnetussaSuunnassa).getTyyppi().equals(PubiobjektiEnum.ULOSKAYNTI)) {
            if (sankari.getJuomat() > 0) {
                kl.setViestiKentanSisalto("Portsari: <i>Hei! Eipäs viedä juomia pihalle!</i>");
            } else {
                kl.getNappaimistonKuuntelija().setOdotetaanKyllaVaiEiKysymysta(true);
                kl.setViestiKentanSisalto("Lopeta peli? &#40;k &#47; e&#41;<br>");
            }

        }
    }

    /**
     * Etsitään kuka on annetussa sijainnissa
     *
     * @param sijainti on tutkittava sijainti
     * @return palauttaa true, mikäli inehmo löytyi
     */
    private Inehmo etsiInehmoAnnetussaSijainnissa(Sijainti sijainti) {
        for (Inehmo inehmo : inehmot) {
            if (inehmo.getSijainti().equals(sijainti)) {
                return inehmo;
            }
        }
        return null;
    }

    /**
     * Luodaan halutun suunnan koordinaatit omaava Sijainti-olio
     *
     * @param suunta on suunta mitä tutkitaan
     * @param inehmo on tutkinnan laukaiseva Inehmo-olio (ml. Sankari)
     * @return palauttaa uuden Sijainti-olion
     */
    public Sijainti annaSijaintiHalutussaSuunnassa(KomentoEnum suunta, Inehmo inehmo) {
        Sijainti sijaintiAnnetussaSuunnassa = null;
        switch (suunta) {
            case POHJOINEN:
                sijaintiAnnetussaSuunnassa = new Sijainti(inehmo.getSijainti().getX(), inehmo.getSijainti().getY() - 1);
                break;
            case ITA:
                sijaintiAnnetussaSuunnassa = new Sijainti(inehmo.getSijainti().getX() + 1, inehmo.getSijainti().getY());
                break;
            case ETELA:
                sijaintiAnnetussaSuunnassa = new Sijainti(inehmo.getSijainti().getX(), inehmo.getSijainti().getY() + 1);
                break;
            case LANSI:
                sijaintiAnnetussaSuunnassa = new Sijainti(inehmo.getSijainti().getX() - 1, inehmo.getSijainti().getY());
                break;
            case KOILLINEN:
                sijaintiAnnetussaSuunnassa = new Sijainti(inehmo.getSijainti().getX() + 1, inehmo.getSijainti().getY() - 1);
                break;
            case KAAKKO:
                sijaintiAnnetussaSuunnassa = new Sijainti(inehmo.getSijainti().getX() + 1, inehmo.getSijainti().getY() + 1);
                break;
            case LOUNAS:
                sijaintiAnnetussaSuunnassa = new Sijainti(inehmo.getSijainti().getX() - 1, inehmo.getSijainti().getY() + 1);
                break;
            case LUODE:
                sijaintiAnnetussaSuunnassa = new Sijainti(inehmo.getSijainti().getX() - 1, inehmo.getSijainti().getY() - 1);
                break;
        }
        return sijaintiAnnetussaSuunnassa;
    }

    /**
     * Testaa josko annetussa sijainnissa on joku hahmo
     *
     * @param sijainti
     * @return palauttaa true, mikäli sijainnissa on joku
     */
    public boolean onkoSiinaJoku(Sijainti sijainti) {
        for (Inehmo inehmo : inehmot) {
            if (inehmo.getSijainti().equals(sijainti)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Testaa josko annetussta sijainnista löytyvän pubiobjektin esteattribuutti
     * on true.
     *
     * @param sijainti on sijainti jonka esteellisyyttä tutkitaan
     * @return true tai false sen mukaan oliko koordinaateissa este vai ei
     */
    public boolean tormaakoEsteeseen(Sijainti sijainti) {
        if (pubi.getObjekti(sijainti).getEste()) {
            return true;
        }
        return false;
    }

    /**
     * Poimitaan satunnainen liikesuunta
     *
     * @return palauttaa arvotun suunnan
     */
    public KomentoEnum arvoLiikesuunta() {
        int satunnainen = arpoja.nextInt(9);
        return komennot[satunnainen];
    }

    public Sijainti getSankarinSijainti() {
        return pubi.getInehmot().get(0).getSijainti();
    }

    public Pubi getPubi() {
        return this.pubi;
    }

    public int getSiirtoja() {
        return this.siirtoja;
    }

    public boolean getAsiakkaatLiikkuvat() {
        return this.asiakkaatLiikkuvat;
    }

//    public void setAsiakkaita(int asiakkaita) {
//        this.asiakkaita = asiakkaita;
//    }
//    
    public void setSiirtoja(int siirtoja) {
        this.siirtoja = siirtoja;
    }

    public void setAsiakkaatLiikkuvat(boolean juuTaiEi) {
        this.asiakkaatLiikkuvat = juuTaiEi;
    }

    public Inehmo getInehmo(int luku) {
        return inehmot.get(luku);
    }

    public ArrayList<Inehmo> getInehmot() {
        return this.inehmot;
    }
}

