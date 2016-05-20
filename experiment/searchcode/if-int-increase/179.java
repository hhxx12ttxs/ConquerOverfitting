
package pokeman;

import java.util.ArrayList;

/**
 *
 * @author Mark
 */
public class Turn implements Runnable{
    
    private Move yourMove;
    private Pokemon theirCurrent,yours;
    private BattleFrontEnd frontEnd;
    private boolean stop = true;
    private Character player;
    
    public Turn(Character c,Move yourMove,Pokemon theirCurrent,Pokemon yours,BattleFrontEnd frontEnd){
        this.yourMove = yourMove;
        this.theirCurrent = theirCurrent;
        this.yours = yours;
        this.frontEnd = frontEnd;
        player = c;
    }
    
    @SuppressWarnings("empty-statement")
    public void run(){
        //first, you check the speeds to see who goes first
        //then you do the calculation on the first one
        //you check if dead. if not, then do the second move
        //check if dead again.
        
        //subtracts pp, and if the move has no PP, it breaks out
        //we may modify the return to give more info to the frontend
        
        Move theirMove=null;
        while(theirMove==null){
            int movenumber = (int)(Math.random() * 4);        
             theirMove = theirCurrent.getMoves()[movenumber]; 
        }


        
        
        if(yourMove!=null){
            if(!yourMove.useMove())
                return;





            if (yours.getSpeed() >= theirCurrent.getSpeed())
            {
                performMove(yours,theirCurrent,yourMove);
                performMove(theirCurrent,yours,theirMove);

            } 
            else 
            {
                performMove(theirCurrent,yours,theirMove);
                performMove(yours,theirCurrent,yourMove);
            }
        }else{
                performMove(theirCurrent,yours,theirMove);
        }
    }
    
    private void performMove(Pokemon doer,Pokemon reciever,Move m){
        
        if(doer.getCurrentHP()!=0){
            

            frontEnd.setText(doer.getName()+" used "+m.name());   
            frontEnd.removeKeyListener();

            //now do their move
            if(m.attacksWhat()==Move.HP){
                if(m.raises()){
                    doer.heal(m.power());
                    frontEnd.setText(doer.getName()+" healed itself.");
                }else
                    reciever.takeDamage(calculateDamage(m, doer==yours));
            }else{
                if(m.raises())
                    changeStats(false,doer,m.attacksWhat(),m.power());
                else
                    changeStats(true,reciever,m.attacksWhat(),m.power());
            }

            while(frontEnd.waitingForHPAndExp() && stop)
                stop = !Thread.interrupted();
            frontEnd.addKeyListener();
            while(frontEnd.waiting() && stop)
                stop = !Thread.interrupted();
            if(reciever.getCurrentHP()==0)
                frontEnd.setText(reciever.getName()+" fainted");
            

        }
    }
    

    
    /**
     * Retuns false if it couldn't increase or decrease the stat
     * 
     * @param lower True if you want to lower the stats false if you want to raise it
     * @param p Pokemon to perform stat change on
     * @param what What to attack the constants from Move
     * @param ammount The ammount to raise or decrease
     * @return True if it could increase or decrease false otherwise
     */
    private boolean changeStats(boolean lower,Pokemon p,int what,int amount){
       boolean ret = false;
       boolean realRet = false;
       for(int i=0;i<amount;i++){
            if(what==Move.ATTACK){
               if(lower){
                   ret = p.reduceAttack();
                   frontEnd.setText(p.getName()+"'s attack fell.");
               }else{
                   ret = p.increaseAttack();
                   frontEnd.setText(p.getName()+"'s attack rose.");
               }
            }
            if(what==Move.DEFENSE){
                if(lower){
                    ret = p.reduceDefense();
                    frontEnd.setText(p.getName()+"'s defense fell.");
                }else{
                    ret = p.increaseDefense();
                    frontEnd.setText(p.getName()+"'s defense rose.");
                }
            }
            if(what==Move.SPECIAL){
                if(lower){
                    ret = p.reduceSpecial();
                    frontEnd.setText(p.getName()+"'s special fell.");
                }else{
                    ret = p.increaseSpecial();
                    frontEnd.setText(p.getName()+"'s special rose.");
                }
            }
            if(what==Move.SPEED){
                if(lower){
                    ret = p.reduceSpeed();
                    frontEnd.setText(p.getName()+"'s speed fell.");
                }else{
                    ret = p.increaseSpeed();
                    frontEnd.setText(p.getName()+"'s speed rose.");
                }
            }
            if(i==0)
            {
               realRet = ret;
            }
        }
        return realRet;
    }
    
    /**
     * returns the damage done, -1 if it misses
     * @param m the move to calculate damage with
     * @param isPerson true if its the human controlled one attacking the comp
     * @return the damage, -1 if it misses.
     */
    private int calculateDamage(Move m, boolean isPerson){
        
        System.out.println(isPerson);
        
        Pokemon attacker;
        Pokemon defender;
        if (isPerson){
            attacker = yours;
            defender = theirCurrent;
        } else {
            attacker = theirCurrent;
            defender = yours;
        }
        

        //int accuracything = (int) (attacker.getAccuracy() * m.accuracy() * 0.0256);
        int accuracything = (int) ( m.accuracy() * 2.56);
            
        if ((int)(Math.random() * 256) < accuracything)
        {
            //move hit, now calculate damage
            int level = attacker.getLevel();
            int attack = attacker.getAttack();
            int power = m.power();
            int defense = defender.getDefense();
            double stab;
            if (m.element() == attacker.getElement1() || m.element() == attacker.getElement2())
            {
                stab = 1.5;
            }
            else 
            {
                stab = 1;
            }
            double typeModifer = m.element().multiplerAgainst(defender.getElement1()) * m.element().multiplerAgainst(defender.getElement2()) * 10;

            //tell front end its super/not very effective?
            int randomNumber = (int)(Math.random() * 39) + 217;
            
            if(randomNumber>=248)
                frontEnd.setText("A critical hit!");
            
            if(typeModifer>=20)
                frontEnd.setText("It's super effective!");
            
            if(typeModifer<=5)
                frontEnd.setText("It's not very effective...");
            
            int damage = (int)(((((Math.min(((((2*level/5.0 + 2)*attack*power)/(double)Math.max(1, defense))/50.0), 997) + 2)*stab)*typeModifer)/10.0)*randomNumber)/255;
            System.out.println(damage);
            return damage;
        } else {
            return -1;
        }
    }
}

