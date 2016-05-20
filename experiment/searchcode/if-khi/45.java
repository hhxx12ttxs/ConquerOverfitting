/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package heartsgamejava;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.*;


/**
 *
 * @author NhatTan
 */
public final class HeartsDeck {
    public static int nSuit = 4;
    public static int nFace = 13;
    public static int nCard = 52;
    public static int MaxScore = 100;
    
    private boolean HEARTS_BROKEN = false;
    
    private ArrayList<HeartsCard> Cards;
    private HeartsPlayer[] players;
    
    // V? trí ng??i ch?i ??u tięn
    private int firstPlayerPosition = 0;

   // l?u 4 lá bŕi ?ánh c?a 4 ng??i.
    private ArrayList<HeartsCard> fourCards;
    
    // t?o m?t l?n duy nh?t khi game b?t ??u
    // các l?n ti?p theo ch? c?n xŕo bŕi
    public HeartsDeck()
    {
        Cards = new ArrayList<HeartsCard>();
        players = new HeartsPlayer[4];
        fourCards = new ArrayList<HeartsCard>();
        
        players[0] = new HeartsPlayer("Bottom");
        players[1] = new HeartsPlayer("Left");
        players[2] = new HeartsPlayer("Right");
        players[3] = new HeartsPlayer("Top");

       // createListCard();
      // dealCardsToPlayer();
    }
    
    public ArrayList<HeartsCard> getCards()
    {
        return Cards;
    }

    // l?y ra m?t m?ng 4 ng??i ch?i
    public HeartsPlayer[] getPlayers()
    {
        return players;
    }

    // l?y ra 1 ng??i ch?i
    public HeartsPlayer getPlayers(int iSTTPlayer)
    {
        return players[iSTTPlayer];
    }
    // Chia bŕi t?i 4 ng??i ch?i
    public void dealCardsToPlayer()
    {
       shuffleCards();
       
       for(int i = 0 ; i < Cards.size(); i += 4)
       {
           players[0].addCardToHand(Cards.get(i));
           players[1].addCardToHand(Cards.get(i+1));
           players[2].addCardToHand(Cards.get(i+2));
           players[3].addCardToHand(Cards.get(i+3));
       }
       
        // X?p bŕi
       for(int j = 0; j< players.length; j++)
       {
           players[j].Sort();
       }

       // kh?i t?o v? trí
       for(int j = 0; j< players.length; j++)
       {
           players[j].setLocations();
       }
    }
    
    // Thi?t l?p 52 lá bŕi
    public void createListCard()
    {
        Cards = new ArrayList<HeartsCard>();
       for(int i = 0; i< nCard; i++)
       {
           int face = i%nFace;
           int suit = i/nFace;
           
           HeartsCard card = new HeartsCard(face, suit);   
           Cards.add(card);
       }
    }
    
    // Xŕo bŕi
    public void shuffleCards()
    {
       Random ran = new Random();
       for(int i = 0; i < nCard; i++)
       {
           int j = ran.nextInt(nCard);
           if(i != j)
           {
               HeartsCard temp = Cards.get(i);
               Cards.set(i, Cards.get(j));
               Cards.set(j, temp);
           }
       }
    }
    
    // Hŕm test th? t? listCard to String vŕ ng??c l?i
    public void TestStringToHeartCard()
    {
          for(int j = 0; j< players.length; j++)
       {
           players[j].test();
       }
    }
    
    // Těm vŕ ??nh v? trí ng??i ch?i ??u tięn
    public void setFirstPlayer()
    {
        for(int  position = 0; position < players.length; position++)
        {
            for(HeartsCard card: players[position].getCardsInHand() )
            {
                if(card.isSuitHearts())
                    setFirstPlayerPosition(position);
            }
        }
    }

    // Hŕm chuy?n 3 quân bŕi qua l?i
    public void passThree(int direction)
    {
        switch(direction)
        {
            // Chuy?n 3 lá bŕi sang trái
            case 0:
                players[0].getThreeCardFromOtherPlayer(players[3]);
                players[1].getThreeCardFromOtherPlayer(players[0]);
                players[2].getThreeCardFromOtherPlayer(players[1]);
                players[3].getThreeCardFromOtherPlayer(players[2]);
            // Chuy?n 3 lá bŕi sang ph?i
            case 1:
                players[0].getThreeCardFromOtherPlayer(players[1]);
                players[1].getThreeCardFromOtherPlayer(players[2]);
                players[2].getThreeCardFromOtherPlayer(players[3]);
                players[3].getThreeCardFromOtherPlayer(players[0]);
            // Chuy?n 3 lá bŕi cho ng??i ??i di?n
            case 2:
                players[0].getThreeCardFromOtherPlayer(players[2]);
                players[1].getThreeCardFromOtherPlayer(players[3]);
                players[2].getThreeCardFromOtherPlayer(players[0]);
                players[3].getThreeCardFromOtherPlayer(players[1]);
            case 3: // nothing
                
        }
    }
    
    public void play()
    {
        
    }
    
    public void checkPlay()
    {
    }
    
    // Tr? v? card th?ng trong m?t n??c bŕi
    public HeartsCard getCardWinTrick()
    {
        HeartsCard maxCard = getFourCards().get(0);
        for(int i = 1; i < getFourCards().size(); i++)
        {
          //  if(getFourCards().get(i).isGreatThan(maxCard))
                maxCard = getFourCards().get(i);
        }
        
        return maxCard;
    }
    
    // Tr? v? s? ?i?m trong m?t l??t ch?i
  /*  public int getScoreDeal(ArrayList<HeartsCard> fourCard)
    {
        int score = 0;
        for(int i = 0 ; i < fourCard.size(); i++)
        {
            score += fourCard.get(i).getScoreCard();
        }
        return score;
    }*/
    
    // Khi có ng??i ch?i ??t 100 ?i?m
    public boolean checkEndGame()
    {
        for(int i = 0; i < players.length; i++)
        {
            if(players[i].getScore() >= MaxScore)
                return true;
        }
        return false;
    }

    void drawFourPlayer(Graphics dbGraphic, Toolkit toolkit) throws InterruptedException {
        players[0].drawOnePlayer(dbGraphic, toolkit,0);
        players[1].drawOnePlayer(dbGraphic, toolkit,1);
        players[2].drawOnePlayer(dbGraphic, toolkit,2);
        players[3].drawOnePlayer(dbGraphic, toolkit,3);
    }

    /**
     * @return the firstPlayerPosition
     */
    public int getFirstPlayerPosition() {
        return firstPlayerPosition;
    }

    /**
     * @param firstPlayerPosition the firstPlayerPosition to set
     */
    public void setFirstPlayerPosition(int firstPlayerPosition) {
        this.firstPlayerPosition = firstPlayerPosition;
    }

    /**
     * @return the fourCards
     */
    public ArrayList<HeartsCard> getFourCards() {
        return fourCards;
    }

    /**
     * @param fourCards the fourCards to set
     */
    public void setFourCards(ArrayList<HeartsCard> fourCards) {
        this.fourCards = fourCards;
    }
    
    // Hŕm nh?n m?t chu?i 52 lá bŕi t? server sao ?ó xé ra 4 chu?i con vŕ chuy?n sang danh sách lá bŕi
    // nh?n t? server: "2s 3c 4d 5h... | 2s 3c 4d 5h... | 2s 3c 4d 5h...| 2s 3c 4d 5h..."
    // Chia ra m?i ng??i ch?i lŕ m?t chu?i con: "2s 3c 4d 5h"
    // tách chu?i con nŕy sang danh sách các lá bŕi
    public void StringToListCardFor4Player(String s)
    {
        String []subString = s.split("\\|");
        for(int  i = 0; i< subString.length; i++)
        {
           ArrayList<HeartsCard> listcard = players[i].stringToListCard(subString[i].trim());
           players[i].addCardToHand(listcard);
        }
    }
       
    // Hŕm chuy?n ??i chu?i 4 quân bŕi nh?n t? server
    public void StringTo4Card(String s)
    {
        setFourCards(players[0].stringToListCard(s));
    }
}

