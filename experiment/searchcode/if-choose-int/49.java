package mw.client.managers;

import java.awt.Color;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mw.client.dialogs.ChoiceDialog;
import mw.client.gui.MWClient;
import mw.client.gui.MWHandBeneath;
import mw.client.input.InputManager;
import mw.client.input.Input_ChoosePermanents;
import mw.client.input.Input_DiscardCard;
import mw.client.input.Input_Madness;
import mw.client.input.Input_Mulligan;
import mw.client.input.Input_PayAdditional;
import mw.client.input.Input_PayDiscardCard;
import mw.client.input.Input_PayManaCost;
import mw.client.input.Input_PlayFirst;
import mw.client.input.Input_TargetCreaturePlayer;
import mw.client.input.Input_TargetInPlay;
import mw.client.input.Input_YesNo;
import mw.mtgforge.Input;
import mw.server.model.Card;
import mw.server.model.SideboardInfo;
import mw.server.model.MagicWarsModel.PhaseName;
import mw.server.model.bean.CardBean;
import mw.server.model.bean.SpellBean;
import mw.server.model.cost.ManaCost;
import mw.utils.DateUtil;

import org.apache.log4j.Logger;

public class GameManager {

    private static final Logger log = Logger.getLogger(GameManager.class);
    
    /**
     * MTG Client singleton.
     */
    private static MWClient client = null;

    public static MWClient getClient() {
        if (client == null) {
            client = new MWClient();
            client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            //AudioManager.playMagicWarsTheme();
        }
        return client;
    }

    /**
     * Game manager. Used for managing all mtg zones in the game.
     */
    private static GameManager gameManager = null;

    public static GameManager getManager() {
        if (gameManager == null) {
            gameManager = new GameManager();
        }
        return gameManager;
    }

    private GameManager() {
    }
    
  
    /**
     * Ask about mulligan.
     */
    public void askAboutMulligan() {
    	getInputControl().setInput(new Input_Mulligan());
    }

    /**
     * Ask about playing first
     */
    public void askAboutPlayingFirst() {
    	getInputControl().setInput(new Input_PlayFirst());
    }
    
    /**
     * Ask player to pay for spell.
     */
    public void askPayForSpell(SpellBean spell) {
    	Input input = new Input_PayManaCost(spell);
    	setCurrentInput(input);
    	getInputControl().setInput(input);
    }
    
    /**
     * Ask player to choose target (creature or player\plainswalker).
     */
    public void askChooseTargetCreatureOrPlayer(SpellBean spell) {
    	getInputControl().setInput(new Input_TargetCreaturePlayer(spell));
    }
    
    /**
     * Ask player to choose Yes or No.
     */
    public void askChooseYesOrNo(SpellBean spell) {
    	getInputControl().setInput(new Input_YesNo(spell));
    }
    
    /**
     * Ask player to choose specific targets
     */
    public void askChooseTheTargets(SpellBean spell) {
    	getInputControl().setInput(new Input_TargetInPlay(spell));
    }
    
    /**
     * Ask player to choose specific targets
     */
    public void askChoosePermanents(SpellBean spell) {
    	getInputControl().setInput(new Input_ChoosePermanents(spell));
    }
    
    /**
     * Ask player to choose specific targets to pay
     */
    public void askChooseTheTargetsForPay(SpellBean spell) {
    	getInputControl().setInput(new Input_PayAdditional(spell));
    }
    
    /**
     * Ask player to choose specific card
     */
    public void askChooseSpecificCard(SpellBean spell) {
        DialogManager.getManager().showChoiceDialog(spell.getChoiceBeans(), spell.areTargetsOptional(), spell.isCancelStopsPlaying());
    }
    
    /**
     * Ask player to choose specific ability
     */
    public void askChooseSpecificAbility(SpellBean spell, ArrayList<SpellBean> list) {
        DialogManager.getManager().showAbilityChoiceDialog(list, spell.areTargetsOptional());
    }
    
    /**
     * Ask player to choose specific ability to play
     */
    public void askChooseSpecificAbilityToPlay(ArrayList<SpellBean> sp) {
    	ArrayList<Object> params = new ArrayList<Object>();
    	params.addAll(sp);
    	
    	GameManager.getManager().setChosenObject(null);
		DialogManager.getManager().showChooseDialog(params);
		while (GameManager.getManager().getChosenObject() == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				log.error(ie);
			}
		}

		SpellBean sb = (SpellBean)GameManager.getManager().getChosenObject();
		
		try {
            ConnectionManager.sendAddChosenAbilityToPlay(sb);
        } catch (RemoteException re) {
            re.printStackTrace();
        }
        
        /*if (sp.get(0).isChooseTwo()) {
        	params.remove(sb); // remove already chosen
        	
        	GameManager.getManager().setChosenObject(null);
    		DialogManager.getManager().showChooseDialog(params);
    		while (GameManager.getManager().getChosenObject() == null) {
    			try {
    				Thread.sleep(10);
    			} catch (InterruptedException ie) {
    				log.error(ie);
    			}
    		}

    		sb = (SpellBean)GameManager.getManager().getChosenObject();
    		
    		try {
                ConnectionManager.sendAddChosenAbilityToPlay(sb);
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }*/
    }
    
    public void askChooseValue(ArrayList<Serializable> values) {
    	askChooseValue(values, null);
    }
    
    public void askChooseValue(ArrayList<Serializable> values, String title) {
    	ArrayList<Object> params = new ArrayList<Object>();
    	params.addAll(values);
    	
    	GameManager.getManager().setChosenObject(null);
		DialogManager.getManager().showChooseDialog(params, title);
		while (GameManager.getManager().getChosenObject() == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				log.error(ie);
			}
		}

		Serializable result = (Serializable)GameManager.getManager().getChosenObject();
		
		try {
            ConnectionManager.sendResult(result);
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }
    
    /**
     * Ask player to choose mana symbol to use to pay for the spell
     */
    public void askChooseManaSymbol(ArrayList<String> choices) {
        
        String manaSymbol = "0";
        HashSet<String> manaSet = new HashSet<String>();
        
        boolean hasHybrid = false;
        for (String choice : choices) {
            if (choice.contains("\\")) {
                hasHybrid = true;
                break;
            }
            manaSet.add(choice);
        }
        
        if (!hasHybrid) {
            GameManager.getManager().setChosenManaSymbol(null);
            DialogManager.getManager().showManaChoiceDialog(manaSet);
            manaSymbol = GameManager.getManager().getChosenManaSymbol();
            try {
                while (manaSymbol == null) {
                    Thread.sleep(0);
                    manaSymbol = GameManager.getManager().getChosenManaSymbol();
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        } else {
            manaSymbol = (String) getChoice("Choose mana symbol to pay for", choices.toArray());       
        }
        
        try {
            Input input = GameManager.getInputControl().getInput();
            Input_PayManaCost payInput = null; 
            if (input instanceof Input_PayManaCost) {
                payInput = (Input_PayManaCost) input;
            }
            /* commented: bug fix [25.10.2008:ancient lotus]
            if (payInput == null) {
                return;
            }
            */
            
            ManaCost newManaCost = ConnectionManager.sendChosenManaSymbol(manaSymbol);
            if (newManaCost != null && payInput != null) {
                    payInput.updateManaCost(newManaCost);
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
    }
    
    /**
     * Ask player to choose a planeswalker or player to attack
     */
    public String askChoosePlayersToAttack(String[] names) {
        return (String)getChoice("Choose", names);
    }
    
    public void askChooseX() {
    	askChooseX(false);
    }
    
    /**
     * Ask player to choose X for the spell
     */
    public void askChooseX(boolean isXValue2) {
        final JEditorPane pane = new JEditorPane();

        String s = "";
        Integer x = null;
        while(x == null)
        {
            int result = JOptionPane.showConfirmDialog(null, pane, "Type X or press Cancel", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.CANCEL_OPTION) {
            	if (isXValue2) {
            		try {
                    	ConnectionManager.sendXValue2(0);
                    } catch (RemoteException e) {
                        log.error(e.getStackTrace());
                    }
            	}
                break;
            }
            s = pane.getText().trim();
            try {
                x = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                log.error("Wrong number format. Try again.");
                JOptionPane.showMessageDialog(null, "Wrong number format. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                pane.setText("");
            }
            if (x != null && x > 99) {
                log.warn("Number is too big (should be less than 100). Try again.");
                JOptionPane.showMessageDialog(null, "Number is too big (should be less than 100). Try again.", "Warning", JOptionPane.WARNING_MESSAGE);
                pane.setText("");
                x = null;
            }
        }
        
        if (x != null) {
            try {
            	if (isXValue2) {
            		ConnectionManager.sendXValue2(x);
            	} else {
            		ConnectionManager.sendXValue(x);
            	}
            } catch (RemoteException e) {
                log.error(e.getStackTrace());
            }
        }
    }
    
    /**
     * Ask player to choose player
     */
    /*public void askChooseTargetPlayer() {
    	getInputControl().setInput(new Input_TargetPlayer());
    }*/
    
    /**
     * Ask player to choose card to discard
     */
    public void askChooseCardToDiscard(SpellBean spell) {
    	getInputControl().setInput(new Input_DiscardCard(spell));
    }
    
    /**
     * Ask player to play discarded cards for their madness cost
     */
    public void askPlayMadness() {
    	getInputControl().setInput(new Input_Madness());
    }

    /**
     * Ask player to choose card to discard to pay for a spell
     */
    public void askChooseCardToDiscardToPay(SpellBean spell) {
    	getInputControl().setInput(new Input_PayDiscardCard(spell));
    }
    
    /**
     * Get player's hand size
     */
    
    public int getMyHandSize() {
    	int handsize = 0;
    	try {
    		handsize = ConnectionManager.getRMIConnection().getHandSize(ProfileManager.getMyId());
    	} catch (RemoteException re) {
    		re.printStackTrace();
    	}
    	return handsize;
    }

    public PhaseName getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(PhaseName currentPhase) {
        this.currentPhase = currentPhase;
        
        if (isCurrentPhase(PhaseName.combat_declare_attackers)) {
			WindowManager.getManager().getBeforeDamageStack().setVisible(true);
			WindowManager.getManager().getAfterDamageStack().setVisible(true);
		} else if (isCurrentPhase(PhaseName.combat_declare_blockers)) {
			WindowManager.getManager().getBeforeDamageStack().setVisible(true);
			WindowManager.getManager().getAfterDamageStack().setVisible(true);
			if (priorityPID == ProfileManager.getMyId()) {
				WindowManager.getGuideWindow().reset();
				ClientCombatManager.getManager().getCombatFromServer();
			} else {
				GameManager.getInputControl().blankInput();
			}
		} else if (isCurrentPhase(PhaseName.main2)) {
			WindowManager.getManager().getBeforeDamageStack().setVisible(false);
			WindowManager.getManager().getAfterDamageStack().setVisible(false);
			WindowManager.getManager().clearArrows();
		}
        
        boolean isOpponentTurn = false;
        if (priorityPID == ProfileManager.getMyId()) {
			GameManager.getInputControl().updateInput();
			if (currentPhase.equals(PhaseName.at_endofturn)) {
				isOpponentTurn = true; // special case, display it on opponent phase window
			}
		} else {
			GameManager.getInputControl().blankInput();
			isOpponentTurn = true;
		}

        /**
         * Update phases drawing
         */
        if (isOpponentTurn) {
			WindowManager.getManager().getMyPhases().setCurrentPhase(PhaseName.blank);
			WindowManager.getManager().getOpponentPhases().setCurrentPhase(currentPhase);
        } else {
        	WindowManager.getManager().getMyPhases().setCurrentPhase(currentPhase);
			WindowManager.getManager().getOpponentPhases().setCurrentPhase(PhaseName.blank);
        }

		if (isCurrentPhase(PhaseName.cleanup)) {
			WindowManager.getManager().getMyTable().repaintAllCards();
			WindowManager.getManager().getOpponentTable().repaintAllCards();
			WindowManager.getManager().closeAllRevealWindows();
			if (SettingsManager.getManager().getHandType() == 2) {
				WindowManager.getManager().getHandBeneath().repaintHand();
			}
			String time = "[" + DateUtil.now("H:mm:ss") + "]";
			WindowManager.getDisplayWindow().addMessage(time + " ", cyanColor);
			WindowManager.getDisplayWindow().addMessage("Next turn\n", cyanColor);
			WindowManager.getDisplayWindow().showText("");
			WindowManager.getDisplayWindow().showInfo("");
			AudioManager.playEndTurn();
		} else {
			AudioManager.playNextPhase();
		}
    }
    
    public static Object getChoice(String message, Object choices[])
    {
        final JList list = new JList(choices);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if(choices[0] instanceof Card)
        {
            list.addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent ev)
                {
                  if(list.getSelectedValue() instanceof Card) {
                      WindowManager.getDisplayWindow().showCard((CardBean)list.getSelectedValue());
                  }
                }
            });
        }
        Object o = list.getSelectedValue();
        while(o == null)
        {
            JOptionPane.showMessageDialog(null, new JScrollPane(list), message, JOptionPane.OK_OPTION);
            o = list.getSelectedValue();
        }

        return o;
    }
    
    public boolean isCurrentPhase(PhaseName phaseName) {
    	return this.currentPhase.equals(phaseName);
    }
    
    public void setHasPriority(int pid) {
    	log.debug("setPriorityPID: " + pid);
        priorityPID = pid;
    }
    
    public int getPriorityPID() {
    	log.debug("getPriorityPID: " + priorityPID);
        return priorityPID;
    }

    public CardBean getChosenCard() {
        if (chosenCards.size() == 0) {
            return null;
        }
        return chosenCards.get(0);
    }

    public void setChosenCard(CardBean card) {
        if (chosenCards.size() > 0) {
            CardBean c = chosenCards.get(0);
            if (!c.equals(card)) {
                currentChoiceDlg.turnCardBorderOff(c);
            }
            chosenCards.clear();
        }
        chosenCards.add(card);
        currentChoiceDlg.changeTitle(card.getName());
    }

    public void resetChosenCards() {
        if (chosenCards.size() > 0) {
            currentChoiceDlg.turnCardBorderOff(chosenCards.get(0));
        }
        chosenCards.clear();
        if (currentChoiceDlg != null) {
            currentChoiceDlg.changeTitle("none");
        }
    }
    
    public void addChosenCard(CardBean card) {
        chosenCards.add(card);
    }
    

    public ChoiceDialog getCurrentChoiceDlg() {
        return currentChoiceDlg;
    }

    public void setCurrentChoiceDlg(ChoiceDialog currentChoiceDlg) {
        this.currentChoiceDlg = currentChoiceDlg;
    }
    
    public String getChosenManaSymbol() {
        return chosenManaSymbol;
    }

    public void setChosenManaSymbol(String chosenManaSymbol) {
        this.chosenManaSymbol = chosenManaSymbol;
    }
    
	public String getChosenDeck() {
		return chosenDeck;
	}

	public void setChosenDeck(String chosenDeck) {
		this.chosenDeck = chosenDeck;
	}
	
	public String getChosenDeckAI() {
		return chosenDeckAI;
	}

	public void setChosenDeckAI(String chosenDeckAI) {
		this.chosenDeckAI = chosenDeckAI;
	}

	public Object getChosenObject() {
		return chosenObject;
	}

	public void setChosenObject(Object chosenObject) {
		this.chosenObject = chosenObject;
	}
	
	public boolean isSolitareGame() {
		return solitareGame;
	}

	public void setSolitareGame(boolean value) {
		this.solitareGame = value;
	}
	
	public boolean isGameCreated() {
		return gameCreated;
	}

	public void setGameCreated(boolean gameCreated) {
		log.info("setGameCreated:"+gameCreated);
		this.gameCreated = gameCreated;
	}

	public static InputManager getInputControl() {
		return inputControl;
	}
	
	public static void setInputControl(InputManager im) {
		inputControl = im;
	}
	
	public boolean isLocked() {
		return lock;
	}

	public void setLocked(boolean locked) {
		this.lock = locked;
	}
	
	public Input getCurrentInput() {
		return currentInput;
	}

	public void setCurrentInput(Input currentInput) {
		this.currentInput = currentInput;
	}
	
    public boolean isSideboarding() {
		return sideboarding;
	}

	public void setSideboarding(boolean sideboarding) {
		this.sideboarding = sideboarding;
	}
	
	public SideboardInfo getSideboarded() {
		return sideboarded;
	}

	public void setSideboarded(SideboardInfo sideboarded) {
		this.sideboarded = sideboarded;
	}
	
	/****************************************************************************
     * DATA 
     */
    
    /**
     * ID of active player
     */
    private int priorityPID;
    
    /**
     * Input manager.
     */
    private static InputManager inputControl = null;
    
    /**
     * Phase name
     */
    private PhaseName currentPhase = PhaseName.blank;
    
    /**
     * Chosen cards;
     */
    private ArrayList<CardBean> chosenCards = new ArrayList<CardBean>();
    
    private String chosenManaSymbol = null;

    private ChoiceDialog currentChoiceDlg;
    
    private Color cyanColor = new Color(0,100,100,150);
    
    private String chosenDeck;
    private String chosenDeckAI;
    private Object chosenObject;
    private SideboardInfo sideboarded;
    
	private boolean solitareGame = false;
    private boolean gameCreated = false;
    private boolean sideboarding = false;
   
	private boolean lock;
    private Input currentInput;
}

