/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package heartsgamejava;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author NhatTan
 */
public final class HeartsCard {
    
   
    
    public static String PICTURE_EXTEND=".jpg";
    public static String PICTURE_FOLDER="card/";
    public static String PICTURE_BACK = "card/b.jpg";

    	//nuoc cua la bai: 2..10, j, q, k, a
	public static final String[] Face = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k", "a"};
	//public static final String[] faceText = {"TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEEN", "JACK", "QUEEN", "KING", "ACE"};

	//chat cua la bai(bich, chuon, ro, co): s(spade), c(club), d(diamond), h(heart)
	public static final String[] Suit = {"s", "c", "d", "h"};
	public static final String[] suitText = {"Bich", "Chuon", "Ro", "Co"};


    private int iFace;
    private int iSuit;
    private String urlImage;
    private Image image;
    private int iStatusDownBack;
    private int iStatusPlay;
    private int iLocation;


    
    public HeartsCard(int face, int suit)
    {
        this.iFace = face;
        this.iSuit = suit;
        this.image = null;
        this.urlImage = PICTURE_FOLDER + Face[face] + Suit[suit] + PICTURE_EXTEND;
        //ImageIcon imageIcon = new ImageIcon(this.getClass().getResource(getUrlImage()));
        //image = imageIcon.getImage();
    }

    /**
     * @return the iFace
     */
    public int getiFace() {
        return iFace;
    }

    /**
     * @param iFace the iFace to set
     */
    public void setiFace(int face) {
        this.iFace = face;
    }

    /**
     * @return the iSuit
     */
    public int getiSuit() {
        return iSuit;
    }

    /**
     * @param iSuit the iSuit to set
     */
    public void setiSuit(int suit) {
        this.iSuit = suit;
    }

    /**
     * @return the urlImage
     */
    public String getUrlImage() {
        return urlImage;
    }

    /**
     * @param urlImage the urlImage to set
     */
    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(Image image) {
        this.image = image;
    }


    
    // Xét các tr??ng h?p ??c bi?t khi ch?i
    // c? vŕ q bích ???c ???c ?ánh ??u
    // 2 chu?ng ?? ch?i l??t ??u tięn
    public boolean isSuitHearts()
    {
        if("h".equals(Suit[getiSuit()]))
            return true;
        else
            return false;
    }
    
    public int getScoreCard()
    {
        int score = 0;
        if(this.isSuitHearts())
        {
            score = 1;
        }
        if(this.isQSpade())
        {
            score = 13;
        }
        
        return score;
    }
    
    public boolean isQSpade()
    {
        if("q".equals(Face[getiFace()]) && "s".equals(Suit[getiSuit()]) )
            return true;
        else
            return false;
    }
    
    public boolean isTwoClub()
    {
        if("2".equals(Face[getiFace()]) && "c".equals(Suit[getiSuit()]))
            return true;
        return false;
    }

    public boolean isGreatThan(HeartsCard card)
    {
         if(this.getiSuit() == card.getiSuit())
         {
             if(this.getiFace() > card.getiFace())
                 return true;
             return false;
         }
         else
             return false;
    }

    void drawCard(Graphics dbGraphic, Toolkit toolkit, Point point,  int iPlayer)
    {
        // n?u tr?ng thái ch?i c? lŕ 0
        // t?c lŕ các lá bŕi ?ă ch?i
        if(getiStatusPlay() == 0)
        {
            //if(getiStatusDownBack() == 0)
            {
               image =toolkit.getImage(urlImage);
               dbGraphic.drawImage(image, point.x, point.y, null);
            }/*
            else
            {
                // các quân bŕi úp
                if(getiStatusDownBack() == 1)
                {
                    image =toolkit.getImage(PICTURE_BACK);
                    dbGraphic.drawImage(image, point.x, point.y, null);
                }


            }*/
        }
        else
        {

            if(getiStatusPlay() == 1)
            {
                //setiStatusPlay(2);
                if(iPlayer == 0)
                {
                    image =toolkit.getImage(urlImage);
                    dbGraphic.drawImage(image, 410, 310, null);
                }
                if(iPlayer == 1)
                {
                    image =toolkit.getImage(urlImage);
                    dbGraphic.drawImage(image, 330, 250, null);
                }
                if(iPlayer == 2)
                {
                    image =toolkit.getImage(urlImage);
                    dbGraphic.drawImage(image, 410, 190, null);
                }
                if(iPlayer == 3)
                {
                    image =toolkit.getImage(urlImage);
                    dbGraphic.drawImage(image, 490, 250, null);
                }

            }
            else
            {
                if(getiStatusPlay() == 2)
                {

                }
            }

        }

    }

    /**
     * @return the iStatusPlay
     */
    public int getiStatusPlay() {
        return iStatusPlay;
    }

    /**
     * @param iStatusPlay the iStatusPlay to set
     */
    public void setiStatusPlay(int iStatusPlay) {
        this.iStatusPlay = iStatusPlay;
    }

    /**
     * @return the iStatusDownBack
     */
    public int getiStatusDownBack() {
        return iStatusDownBack;
    }

    /**
     * @param iStatusDownBack the iStatusDownBack to set
     */
    public void setiStatusDownBack(int iStatusDownBack) {
        this.iStatusDownBack = iStatusDownBack;
    }
    
    public String heartsCardToString()
    {
        return Face[this.getiFace()] + Suit[this.getiSuit()]; 
    }
    
    // Tr? v? m?t HeartsCart khi bi?t chu?i tęn c?a nó "kc" => HeartsCard(iFace, iSuit)
    public HeartsCard stringToHeartsCard(String str)
    {
        String face = null, suit = null;
        if(3 == str.length())
        {
             face = str.substring(0, 2);
             suit = str.substring(2,3);
        }
        if(2 == str.length())
        {
             face = str.substring(0, 1);
             suit = str.substring(1,2);
        }
        HeartsCard heart = new HeartsCard(indexFace(face),indexSuit(suit)) ;
        return heart;
    }
    
    // Tr? v? th? t? c?a face khi bi?t face ?ó. vd: k => 11
    public int indexFace(String face)
    {
        int index = -1 ;
        for(int i = 0; i < Face.length; i++)
        {
            if(Face[i].equals(face))
                index = i;
        }
        return index;
    }
    
     // Tr? v? ch? s? c?a suit khi bi?t suit ?ó. vd: s => 0
     public int indexSuit(String suit)
    {
        int index = -1;
        for(int i = 0; i < Suit.length; i++)
        {
            if(Suit[i].equals(suit))
                index = i;
        }
        return index;
    }


     // m?i thęm 21/6
    /**
     * @return the iLocation
     */
    public int getiLocation() {
        return iLocation;
    }

    /**
     * @param iLocation the iLocation to set
     */
    public void setiLocation(int iLocation) {
        this.iLocation = iLocation;
    }
}
