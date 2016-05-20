/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package heartsgamejava;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.*;

/**
 *
 * @author NhatTan
 */
public class HeartsPlayer {
    
    private ArrayList<HeartsCard> listCardPlayer ;
    
    // listCardWin důng ?? l?u các quân bŕi ?n ???c trong 1 vňng ch?i
    // Ch? bao g?m các  QUÂN C? vŕ Q BÍCH
    // důng ?? tính ?i?m khi k?t thúc m?t vňng
    private ArrayList<HeartsCard> listCardWin;
    private String name;
    private int score;
    private String strListCardPlayer;
    
    private ArrayList<HeartsCard> threeCardToPass;
    
    // Quân bŕi khi ng??i ch?i click vŕo m?t lá bŕi ch?i
    private HeartsCard cardChosen;
    
    public HeartsPlayer(String Name)
    {
        this.name = Name;
        listCardPlayer = new ArrayList<HeartsCard>();
        this.score = 0;
    }
    
    public void newRound()
    {
        listCardPlayer = new ArrayList<HeartsCard>();
        listCardWin = new ArrayList<HeartsCard>();
    }
    
    public void newGame()
    {
        this.score = 0;
    }
    
    // C?p nh?t ?i?m khi k?t thúc m?t vňng ch?i
    public void updateScore()
    {
        score += getScoreOfRound();
    }
    
    // Tính ?i?m khi k?t thúc m?t vňng ch?i
    // Důng ?? hi?n th? ?i?m khi k?t thúc vňng ch?i vŕ c?p nh?t s? ?i?m c?a game
    public int getScoreOfRound()
    {
        int scoreOfRound = 0;
        for(int i = 0; i < listCardWin.size(); i++)
        {
            scoreOfRound += listCardWin.get(i).getScoreCard();
        }
        return scoreOfRound;
    }
    
    // Ki?m tra xem ng??i ch?i có ShootTheMoon
    // N?u có 14 quân: Q bích vŕ 13 quân c?
    public boolean checkShotTheMoon()
    {
        if(14 == listCardWin.size())
            return true;
        return false;
    }
    
    // Tr? v? danh sách lá bŕi c?a ng??i ch?i
    public ArrayList<HeartsCard> getCardsInHand()
    {
        return this.listCardPlayer;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public void setScore(int Score)
    {
        this.score = Score;
    }
    
    public int getScore()
    {
        return this.score;
    }
    
    public void addCardToHand(HeartsCard card)
    {
        listCardPlayer.add(card);
    }
    
    
    // S? d?ng khi chuy?n ??i 3 quân bŕi
    public void addCardToHand(ArrayList<HeartsCard> cards)
    {
        listCardPlayer.addAll(cards);
    }
    
    // Thęm các quân bŕi ?n ???c vŕo listCardWin
    // Ch? thęm QUÂN C? HO?C Q BÍCH
    public void addCardWinToListCardWin(ArrayList<HeartsCard> cards)
    {
        for(int i = 0; i < cards.size(); i++)
        {
            if(cards.get(i).isQSpade() || cards.get(i).isSuitHearts())
                listCardWin.add(cards.get(i));
        }
    }
      
    // Xét ng??i ch?i nŕy có 2 chu?ng ?? ch?i l??t ??u tięn
    // Khi m?t vňng ch?i m?i b?t ??u, s? g?i hŕm nŕy
    public boolean hasTwoClub()
    {
        boolean hasTwoClub = false;
        for(int i = 0 ; i < listCardPlayer.size(); i++)
        {
            if(listCardPlayer.get(i).isTwoClub())
                hasTwoClub = true;
        }
        return hasTwoClub;
    }
    void translate(Point pTranslate,int iSTTCardPlayer)
    {
        if(listCardPlayer.get(iSTTCardPlayer).getiStatusPlay() != 2)
        {
              pTranslate.translate(60, 0);

        }
    }

    void drawOnePlayer(Graphics dbGraphic, Toolkit toolkit, int iPlayer) throws InterruptedException {

        Point point = new Point(10,10);
        switch(iPlayer)
        {
            // dăy bŕi ? d??i
            case 0:
                point.setLocation(20,500);

                break;
                //dăy ? bęn trái
            case 1:
                point.setLocation(50,0);
                break;

            case 2:// dăy tręn
                point.setLocation(220,10);
                break;
            case 3: // dăy bęn ph?i
                 point.setLocation(830,0);
                break;


            }


        for(int i = 0 ; i < listCardPlayer.size() ; ++i)
        {


            switch(iPlayer)
            {
                case 0:// dăy d??i
                    if(listCardPlayer.get(i).getiStatusPlay() == 0)
                    {
                        point.translate(60, 0);


                    }
                    //this.translate(point, i)
                    listCardPlayer.get(i).setiStatusDownBack(0);
                    listCardPlayer.get(i).drawCard(dbGraphic, toolkit, point,0);
                    break;
                case 1:// dăy bęn trái
                    listCardPlayer.get(i).setiStatusDownBack(1);
                    if(listCardPlayer.get(i).getiStatusPlay() == 0)
                    {
                        point.translate(0, 20);

                    }
                    listCardPlayer.get(i).drawCard(dbGraphic, toolkit, point,1);
                    break;
                case 2:// dăy tręn

                    listCardPlayer.get(i).setiStatusDownBack(1);
                    if(listCardPlayer.get(i).getiStatusPlay() == 0)
                    {
                        point.translate(30, 0);

                    }
                    listCardPlayer.get(i).drawCard(dbGraphic, toolkit, point,2);
                    break;
                case 3:// dăy bęn ph?i
                    listCardPlayer.get(i).setiStatusDownBack(1);
                    if(listCardPlayer.get(i).getiStatusPlay() == 0)
                    {
                        point.translate(0, 20);

                    }
                    listCardPlayer.get(i).drawCard(dbGraphic, toolkit, point,3);
                    break;


            }

        }
    }
    
     
    // S?p x?p bŕi
    public void Sort()
    {
        int i,j;
      
        // S?p x?p theo Suit
        for(i = 0 ; i < listCardPlayer.size() - 1; i++)
        {
            for(j = i+1; j < listCardPlayer.size(); j++ )
                if(listCardPlayer.get(i).getiSuit() >  listCardPlayer.get(j).getiSuit())
                {
                    HeartsCard card = listCardPlayer.get(i);
                    listCardPlayer.set(i, listCardPlayer.get(j));
                    listCardPlayer.set(j, card);
                }
        }
        
          // S?p x?p theo Face
        for(i = 0 ; i < listCardPlayer.size() - 1; i++)
        {
            for(j = i+1; j < listCardPlayer.size(); j++ )
                if(listCardPlayer.get(i).getiFace() >  listCardPlayer.get(j).getiFace() && listCardPlayer.get(i).getiSuit() ==  listCardPlayer.get(j).getiSuit() )
                {
                    HeartsCard card = listCardPlayer.get(i);
                    listCardPlayer.set(i, listCardPlayer.get(j));
                    listCardPlayer.set(j, card);
                }
        }
        
    }
    
    
    // Hŕm chuy?n ??i t? listCard sang chu?i
    public String listCardToString()
    {
        String str = "";
        for(int i = 0; i < listCardPlayer.size(); i++)
        {
            str += listCardPlayer.get(i).heartsCardToString();
            str +=" ";
        }
        str = str.trim();
        // strListCardPlayer = str;
        return str;
    }
    
    // Chuy?n ??i t? String sang listCard
    public ArrayList<HeartsCard> stringToListCard(String str)
    {
        ArrayList<HeartsCard> listCards = new ArrayList<HeartsCard>();
        
          //C?t chu?i String str = "a b c"  =>  String[] arrStr = {"a","b","c"}
        StringTokenizer Tok = new StringTokenizer(str);
        while(Tok.hasMoreElements())
        {
            String subStr = Tok.nextElement().toString();  
            HeartsCard heart = new HeartsCard(1,2) ;
            heart =   heart.stringToHeartsCard(subStr); // Chuy?n chu?i con sang ki?u HeartsCard
            listCards.add(heart); // Thęm vŕo listCards
        }
        // listCardsPlayer = listCards
        return listCards;
    }

    
    // hŕm test th? chuy?n ??i
    public void test()
    {
        ArrayList<HeartsCard> listCards = stringToListCard("2c 2d 4d ks");
        for(int i = 0; i < listCards.size(); i++)
        {
            System.out.printf("\n" + HeartsCard.Face[listCards.get(i).getiFace()] + HeartsCard.Suit[listCards.get(i).getiSuit()]);
        }
        
        String str = listCardToString();
        
        System.out.printf("\n\n\""+str+"\"");
    }
    

    /**
     * @return the strListCardPlayer
     */
    public String getStrListCardPlayer() {
        return strListCardPlayer;
    }

    /**
     * @param strListCardPlayer the strListCardPlayer to set
     */
    public void setStrListCardPlayer(String strListCardPlayer) {
        this.strListCardPlayer = strListCardPlayer;
    }

    /**
     * @return the threeCardToPass
     */
    public ArrayList<HeartsCard> getThreeCardToPass() {
        return threeCardToPass;
    }

    /**
     * @param threeCardToPass the threeCardToPass to set
     */
    public void setThreeCardToPass(ArrayList<HeartsCard> threeCardToPass) {
        this.threeCardToPass = threeCardToPass;
    }
    
    // L?y 3 lá bŕi t? ng??i ch?i c?n chuy?n
    public void getThreeCardFromOtherPlayer(HeartsPlayer otherPlayer)
    {
        this.addCardToHand(otherPlayer.getThreeCardToPass());
    }


    // m?i thęm vŕo

    // těm v? trí c?a m?t lá bŕi
    // ??u vŕo lŕ m?t s? nguyęn - s? nguyęn nŕy lŕ v? trí c?a nó
    // ??u ra lŕ s? th? t? c?a nó trong danh sách
    int findLocation(int iLocation)
    {
        for(int i = 0 ; i < listCardPlayer.size(); ++i )
        {
            if(listCardPlayer.get(i).getiLocation() == iLocation)
            {
                return i;
            }
        }
        return -1;
    }

    void  setLocations(int iLocation)
    {
        for(int i = 0 ; i < listCardPlayer.size(); ++i )
        {
            if(listCardPlayer.get(i).getiLocation() == iLocation+1)
            {
                listCardPlayer.get(i).setiLocation(iLocation);
                ++iLocation;
            }
        }

    }
    void  setLocations()
    {
        for(int i = 0 ; i < listCardPlayer.size(); ++i )
        {
                listCardPlayer.get(i).setiLocation(i);
        }

    }

    int isLastCard(int iSTT)
    {
        for(int i = iSTT+1 ; i < listCardPlayer.size(); ++i )
        {
            if(listCardPlayer.get(i).getiLocation() != -1)
            {
                return 0;
            }
        }
        return 1;
    }

    /**
     * @return the cardChosen
     */
    public HeartsCard getCardChosen() {
        return cardChosen;
    }

    /**
     * @param cardChosen the cardChosen to set
     */
    public void setCardChosen(HeartsCard cardChosen) {
        this.cardChosen = cardChosen;
    }
}   



