package pubventure.ymparisto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import pubventure.Sijainti;
import pubventure.TiedostonLukija;
import pubventure.enumit.PubiobjektiEnum;
import pubventure.ihmiset.Inehmo;
import pubventure.ihmiset.InehmojenHallinnointi;

/**
 *
 * @author Santeri
 *
 * Luokka hallinnoi pelikenttää luoden sen TiedostonLukija-luokan avulla,
 * sekä luo sen ihmishahmot (pelaajan, asiakkaat, tarjoilijat, portsarit)
 * InehmojenHallinnointi-luokan metodia kutsumalla.
 * 
 * @see TiedostonLukija
 * @see InehmojenHallinnointi
 * @see Kentanluoja
 */
public class Pubi {

    private int leveys;
    private int korkeus;
    private Pubiobjekti[][] kentta;
    /**
     * TiedostonLukija hoitaa pelikentän lukemisen kovakoodatusta osoitteesta
     * (pub2.txt-niminen tiedosto projektin juuressa)
     */
    private TiedostonLukija tiedostonLukija;
    /**
     * txt-tiedostosta luettu pelikenttä palautetaan tänne merkkijonona
     */
    private String pubiMerkkijonona;
    /**
     * Käytetään selvittämään pubin mittasuhteet
     */
    private Scanner lukija;
    private int asiakkaita;
    /**
     * Pelikentälle luodut hahmot kerätään ArrayListiin
     */
    private ArrayList<Inehmo> inehmot;
    /**
     * Satunnaislukujen lähde
     */
    private Random arpoja = new Random();

    private Inehmo sankari;
    /**
     * Inehmojen luominen on eriytetty omaksi luokakseen
     * @see InehmojenHallinnointi
     */
    private InehmojenHallinnointi ih;
    
    /**
     * Määrittää luodaanko kentälle ihmishahmoja vai ei
     */
    private boolean kansoitettu;
    
    /**
     * Kentanluoja hoitaa pubi.txt-tiedostosta luettujen merkkien tulkkaamisen
     * pubiobjekteiksi kentta-muuttujaan
     */
    private Kentanluoja kentanluoja;

    public Pubi(int asiakkaita, ArrayList<Inehmo> inehmot) {
        this.asiakkaita = asiakkaita;
        this.inehmot = inehmot;
        this.tiedostonLukija = new TiedostonLukija();

        //yritetään lukea tiedosto kutsumalla TiedostonLukija-luokan metodia
        try {
            this.pubiMerkkijonona = tiedostonLukija.lueTiedosto();
        } catch (IOException ex) {
            System.out.println("Tiedoston lukeminen epäonnistui.");
            Logger.getLogger(Pubi.class.getName()).log(Level.SEVERE, null, ex);
        }

        // etsitään mittasuhteet ja otetaan ne talteen
        etsiMittasuhteet();

        // luodaan kaksiulotteinen taulukko kentäksi
        this.kentta = new Pubiobjekti[korkeus][leveys];
        
        // luodaan kenttä em. taulukkoon Kentanluoja-luokan avulla
        this.kentanluoja = new Kentanluoja(pubiMerkkijonona, kentta);

        // luodaan inehmojen hallinnointi ja käsketään sitä luomaan hahmot,
        // mikäli niin halutaan
//        this.ih = new InehmojenHallinnointi(this, asiakkaita, inehmot);
//        if (kansoitettu) {
//            ih.luoHahmot();
//        }
    }

    /**
     * Etsii kentän mittasuhteet, jotta niitä voidaan tarpeen vaatiessa käyttää
     * helposti muissa metodeissa.
     */
    public final void etsiMittasuhteet() {
        this.lukija = new Scanner(pubiMerkkijonona);
        int riveja = 0;
        int riviKayty = 0;

        while (lukija.hasNext()) {
            String nykRivi = lukija.next();
            if (riviKayty == 0) {
                this.leveys = nykRivi.length();
                riviKayty = 1;
            }
            riveja++;
        }
        this.korkeus = riveja;
        this.lukija.close();
    }



    /**
     * Etsii annetun tyyppisen pubiobjekti-luokan olion ja palauttaa sen
     * sijainnin
     *
     * @param haettuTyyppi metodian kutsutaan sillä tyypillä, mitä ollaan
     * etsimässä
     * @return palautetaan joko pubiobjekti, tai null, mikäli haluttua tyyppiä
     * ei löytynyt
     */
    public List<Sijainti> etsiPubiobjektit(PubiobjektiEnum haettuTyyppi) {
        List<Sijainti> lista = new ArrayList<Sijainti>();
        for (int y = 0; y < korkeus; y++) {
            for (int x = 0; x < leveys; x++) {
                if (kentta[y][x].getTyyppi().equals(haettuTyyppi)) {
                    lista.add(new Sijainti(x, y));
                }
            }
        }
        if (lista.isEmpty()) {
            return null;
        } else {
            return lista;
        }
    }

    /**
     * Etsii haetun tyyppin Pubiobjektin ja palauttaa sen. Mahdollisesti
     * löytyvä objekti on ensimmäinen joka tulee vastaan.
     * @param haettuTyyppi on haettavan objektin PubiobjektiEnum
     * @return palauttaa löydetyn objektin
     */
    public Pubiobjekti etsiPubiobjekti(PubiobjektiEnum haettuTyyppi) {
        List<Pubiobjekti> lista = new ArrayList<Pubiobjekti>();
        for (int y = 0; y < korkeus; y++) {
            for (int x = 0; x < leveys; x++) {
                if (kentta[y][x].getTyyppi().equals(haettuTyyppi)) {
                    lista.add(kentta[y][x]);
                }
            }
        }
        if (lista.isEmpty()) {
            System.out.println("Pubiobjektia ei löytynyt!");
            return null;
        } else {
            return lista.get(0);
//            return lista.get(luku.nextInt(lista.size()));
        }
    }

    /**
     * Tutkitaan onko annetuissa koordinaateissa este
     *
     * @param sijainti on tutkittava sijainti
     * @return palauttaa true, mikäli parametrien mukaisessa paikassa on este,
     * muuten false
     */
    public boolean tormaako(Sijainti sijainti) {
        if (getObjekti(sijainti).getEste()) {
            return true;
        }
        for (Inehmo inehmo : inehmot) {
            if (inehmo.getSijainti().getX() == sijainti.getX()
                    && inehmo.getSijainti().getY() == sijainti.getY()) {
                return true;
            }

        }
        return false;
    }

    /**
     * Palauttaa pubiobjektien ulkomuodot ennalleen muuttamalla väliaikaisten
     * ulkomuotojen String-objekteiksi null
     */
    public void palautaPOUlkomuodot() {
        for (int i = 0; i < korkeus; i++) {
            for (int j = 0; j < leveys; j++) {
                kentta[i][j].setVAUlkonako(null);
            }
        }
    }

//    public void setInehmojenNakyvyys(boolean arvo) {
//        ih.setInehmojenNakyvyys(arvo);
//    }
//
//    public boolean getInehmojenNakyvyys() {
//        return ih.getInehmojenNakyvyys();
//    }

    /**
     * Toimii luokan satunnaislukugeneraattorina
     *
     * @param maksimi on maksimiarvo, jonka satunnaisluku voi saada
     * @return palauttaa arvotun kokonaisluvun
     */
    public int arvoLuku(int maksimi) {
        return arpoja.nextInt(maksimi);
    }

    public Sijainti arvoSijainti() {
        int x = arvoLuku(leveys - 1);
        int y = arvoLuku(korkeus - 1);
        return new Sijainti(x, y);
    }

    public Pubiobjekti getObjekti(Sijainti sijainti) {
        return kentta[sijainti.getY()][sijainti.getX()];
    }

    public Pubiobjekti getObjekti(int y, int x) {
        return kentta[y][x];
    }

    public int getLeveys() {
        return this.leveys;
    }

    public int getKorkeus() {
        return this.korkeus;
    }

    public ArrayList<Inehmo> getInehmot() {
        return this.inehmot;
    }

    public int getAsiakkaita() {
        return this.asiakkaita;
    }

    public Pubiobjekti[][] getKentta() {
        return this.kentta;
    }
    
    public InehmojenHallinnointi getInehmojenHallinnointi() {
        return this.ih;
    }
}
