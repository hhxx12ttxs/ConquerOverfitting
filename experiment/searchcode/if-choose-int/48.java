package mw.server.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import mw.mtgforge.CombatUtil;
import mw.mtgforge.Constant;
import mw.mtgforge.Deck;
import mw.mtgforge.PayManaCostUtil;
import mw.mtgforge.WinLose;
import mw.mtgforge.Constant.DeckType;
import mw.server.CardManager;
import mw.server.GameManager;
import mw.server.MWPlayer;
import mw.server.MWProfile;
import mw.server.MWStack;
import mw.server.MWStackBean;
import mw.server.card.ability.AbilityList;
import mw.server.core.ai.MWAIHelper;
import mw.server.core.edit.GameState;
import mw.server.core.edit.action.TapAction;
import mw.server.event.EventParam;
import mw.server.list.CardBeanList;
import mw.server.list.CardList;
import mw.server.model.Card;
import mw.server.model.Combat;
import mw.server.model.CounterType;
import mw.server.model.Damage;
import mw.server.model.DeckInfo;
import mw.server.model.MagicWarsModel;
import mw.server.model.ManaPool;
import mw.server.model.SideboardInfo;
import mw.server.model.SpellAbility;
import mw.server.model.MagicWarsModel.GameZone;
import mw.server.model.MagicWarsModel.PhaseName;
import mw.server.model.ability.AbilityActivated;
import mw.server.model.ability.ManaAbility;
import mw.server.model.bean.CardBean;
import mw.server.model.bean.CombatBean;
import mw.server.model.bean.SpellBean;
import mw.server.model.cost.AdditionalCost;
import mw.server.model.cost.Cost;
import mw.server.model.cost.CostList;
import mw.server.model.cost.DiscardCost;
import mw.server.model.cost.ExileCost;
import mw.server.model.cost.LoyaltyCost;
import mw.server.model.cost.ManaCost;
import mw.server.model.cost.PayLifeCost;
import mw.server.model.cost.RemoveCounterCost;
import mw.server.model.cost.SacrificeCost;
import mw.server.model.cost.TapCost;
import mw.server.model.spell.Flashback;
import mw.server.model.spell.Madness;
import mw.server.model.spell.Unearth;
import mw.server.model.zone.PlayerZone;
import mw.server.msg.MWMessage;
import mw.server.msg.MWMessageManager;
import mw.server.msg.MWMessage.MessageID;
import mw.server.socket.ZippedObject;
import mw.server.socket.ZippedObjectImpl;
import mw.server.thread.MWMultiServerThread;
import mw.utils.CommonUtil;
import mw.utils.SpellUtility;
import mw.utils.log4j.ComputerLevel;

import org.apache.log4j.Logger;

public class MWGameThread implements Runnable {

	private static final Logger log = Logger.getLogger(MWGameThread.class);

	private GameManager game;
	private GameState gameState;

	private MWMessageManager messages;

	// ////////////////////////////////////////////////////////////////////
	// main loop
	private volatile boolean bWaitingForAnswer;
	private volatile boolean bSecondLoop;
	// ////////////////////////////////////////////////////////////////////

	// players
	private final int pid1, pid2;
	private MWPlayer playerWithPriority;
	private MWPlayer opponentPlayer;
	private boolean bPriorityWasSwapped = false;
	// ////////////////////////////////////////////////////////////////////

	private int nStartGame = 0;

	private SpellAbility[] choices;
	private int turnCheck = 0;

	/**
	 * Default constructor
	 */
	public MWGameThread(int playerID1, int playerID2, int game_id, HashMap<Integer, MWMultiServerThread> listeners) {
		// this.game_id = game_id;
		pid1 = playerID1;
		pid2 = playerID2;

		messages = new MWMessageManager(listeners);
		nStartGame = 0;

		CardManager.reloadManager();
		
		/**
		 * Initialize game manager
		 */
		game = new GameManager();
		CardManager.getManager(game);
		game.setGameActive(true);
		
		gameState = game.getGameState();

		// ////////////////////////////////////////////////////////////////
		// init - first player skips draw phase
		game.setCurrentPhase(PhaseName.draw);
		bWaitingForAnswer = false;
		bSecondLoop = false;
		// ////////////////////////////////////////////////////////////////

		// ////////////////////////////////////////////////////////////////
		// init players
		game.setPlayer1(new MWPlayer(pid1, game));
		game.setPlayer2(new MWPlayer(pid2, game));
		game.setGameID(game_id);
		game.addThread(pid1, this);
		game.addThread(pid2, this);
		// ////////////////////////////////////////////////////////////////

        List<MWPlayer> players = new ArrayList<MWPlayer>();
        players.add(game.getPlayer1());
        players.add(game.getPlayer2());
		game.loadRules(players);
		
		// ////////////////////////////////////////////////////////////////
		playerWithPriority = game.getPlayer1();
		opponentPlayer = game.getPlayer2();
		game.givePriorityToPlayer(this.pid1);
		// ////////////////////////////////////////////////////////////////

		addServerObservers(game);
		game.loadRulePlugins();
	}

	private void addServerObservers(final GameManager game) {
		game.initGameStateObserver();

		final Observer systemMessageObserver = new Observer() {
			public void update(Observable o, Object arg) {
				System.err.println("System message observer");
				publishAllSystemMessages();
			}
		};
		game.getEventManager().getUpdateSystemMessageEvent().addObserver(systemMessageObserver);

		Observer addTableObserver = new Observer() {	
			public void update(Observable o, Object arg) {
				EventParam eventParam = (EventParam) arg;
				//PlayerZone from = (PlayerZone) eventParam.getParams().get(0);
				PlayerZone to = (PlayerZone) eventParam.getParams().get(1);

				if (to.getGameZone().equals(GameZone.Battlefield)) {
					Card card = (Card) eventParam.getParams().get(2);
					MWPlayer player = game.getPlayerById(card.getControllerID());
					if (player != null) {
						if (card.isCreature()) {
							player.getGameStatistics().addCreatureID(card.getTableID());
						}
						player.getGameStatistics().addPermanent(card);
					}
				}
			}
		};
		game.getEventManager().getMoveToZoneEvent().addObserver(addTableObserver);

		Observer revealObserver = new Observer() {
			public void update(Observable o, Object arg) {
				messages.publishMessage(MessageID.SM_REVEALED_CHANGED);
			}
		};
		game.getEventManager().getRevealCardEvent().addObserver(revealObserver);

		Observer lookAtCardObserver = new Observer() {
			public void update(Observable o, Object arg) {
				if (arg instanceof Card) {
					messages.publishMessageForUser(MessageID.SM_VIEWED_CHANGED, ((Card) arg).getControllerID());
				}
			}
		};
		game.getEventManager().getLookAtCardEvent().addObserver(lookAtCardObserver);
	}

	// ////////////////////////////////////////////////////////////////////
	// main loop
	public void run() {

		/**
		 * Ask about playing first.
		 */
		messages.publishMessageForUser(MWMessage.MessageID.SM_GAME_ASK_PLAY_FIRST, game.getPriorityPID());
		bWaitingForAnswer = true;
		game.setWaitingPlayerID(game.getPriorityPID());

		while (bWaitingForAnswer == true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		
		checkChangesAndStateEffects();

		/**
		 * First player mulligan.
		 */
		messages.publishMessageForUser(MWMessage.MessageID.SM_GAME_ASK_MULLIGAN, game.getPriorityPID());
		bWaitingForAnswer = true;
		game.setWaitingPlayerID(game.getPriorityPID());

		while (bWaitingForAnswer == true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

		/**
		 * Second player mulligan.
		 */
		swapPriority();
		messages.publishMessageForUser(MWMessage.MessageID.SM_GAME_ASK_MULLIGAN, game.getPriorityPID());
		bWaitingForAnswer = true;
		game.setWaitingPlayerID(game.getPriorityPID());

		while (bWaitingForAnswer == true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

		/**
		 * Swap back.
		 */
		swapPriority();
		game.setActivePlayerId(game.getPriorityPID());

		/**
		 * Main loop
		 */
		while (game.isGameActive() == true) {

			// //////////////////////////////////////////////////////////////////
			// have asked player, waiting for response
			while (bWaitingForAnswer == true && game.isGameActive() == true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			// //////////////////////////////////////////////////////////////////

			if (game.isGameActive() == false) {
				break;
			}

			game.goToNextPhase();
						
			/**
			 * If it is main2 phase and we do need repeat combat after first
			 * strike combat change phase number
			 */
			if (game.isCurrentPhase(PhaseName.main2) && game.wasNeedFirstStrikeCombat()) {
				messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid1, "usual combat");
				messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid2, "usual combat");
				game.getCombat().setFirstStrikeCombat(false);
				game.setCurrentPhase(PhaseName.combat_predamage_player);
			}

			if (game.isCurrentPhase(PhaseName.before_combat)) {
				phaseBeforeCombat();
			} else if (game.isCurrentPhase(PhaseName.combat_declare_blockers)) {
				phaseDeclareBlockers();
			} else if (game.isCurrentPhase(PhaseName.combat_predamage_opponent)) {
				phaseCombatInstantsOpponent();
			} else if (game.isCurrentPhase(PhaseName.combat_stack_damage_player)) {
				phaseDamageStackPlayer();
			} else if (game.isCurrentPhase(PhaseName.combat_stack_damage_opponent)) {
				phaseDamageStackOpponent();
			} else if (game.isCurrentPhase(PhaseName.at_endofturn)) {
				phaseAtEndOfTurn();
			}

			// //////////////////////////////////////////////////////////////////
			// publish broadcast message
			if (bWaitingForAnswer == false) {
				int activePlayerId = game.getActivePlayerId();
				int priorityPID = playerWithPriority.getPlayerId();
				PhaseName phase = game.getCurrentPhaseName();
				if (!playerWithPriority.isMarkExists(phase) && !phase.equals(PhaseName.combat_stack_damage_player)) {
					//skip = true;
				}
				messages.publishMessageWith3Params(MWMessage.MessageID.SM_PHASE_CHANGED, game.getCurrentPhaseName(), activePlayerId, priorityPID);
			}
			// //////////////////////////////////////////////////////////////////

			// //////////////////////////////////////////////////////////////////
			if (game.isCurrentPhase(PhaseName.nextturn)) {
				phaseNextTurn();
			} else if (game.isCurrentPhase(PhaseName.untap)) {
				phaseUntap();
			} else if (game.isCurrentPhase(PhaseName.upkeep)) {
				phaseUpkeep();
			} else if (game.isCurrentPhase(PhaseName.draw)) {
				phaseDraw();
			} else if (game.isCurrentPhase(PhaseName.main1)) {
				phaseMain1();
			} else if (game.isCurrentPhase(PhaseName.combat_declare_attackers)) {
				phaseDeclareAttackers();
			} else if (game.isCurrentPhase(PhaseName.combat_predamage_player)) {
				phaseCombatInstantsPlayer();
			} else if (game.isCurrentPhase(PhaseName.combat_damage)) {
				phaseCombatDamage();
			} else if (game.isCurrentPhase(PhaseName.main2)) {
				phaseMain2();
			} else if (game.isCurrentPhase(PhaseName.endofturn)) {
				phaseEndOfTurn();
			} else if (game.isCurrentPhase(PhaseName.cleanup)) {
				phaseCleanUp();
			}
			// //////////////////////////////////////////////////////////////////

			// //////////////////////////////////////////////////////////////////
			// ask player about playing spells if this phase is marked
			checkMark(game.getCurrentPhaseName());
			// //////////////////////////////////////////////////////////////////

			/**
			 * Send messages about game updates if needed
			 */
			checkChangesAndStateEffects();

			// //////////////////////////////////////////////////////////////////
			// pause between phases
			try {
				Thread.sleep(100);
				log.info("Current phase: " + game.getCurrentPhaseName());
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			// //////////////////////////////////////////////////////////////////

		}

		/**
		 * Game over.
		 */
		messages.publishMessage(MWMessage.MessageID.SM_GAME_ENDED);
		log.info("Game ended. Waiting for players to continue or quit.");

		/**
		 * Second loop
		 */
		game.setGameActive(true);
		while (game.isGameActive() == true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			if (game.isGameActive() == false) {
				break;
			}
		}

		/**
		 * Match also is over.
		 */
		// messages.publishMessage(MWMessage.MessageID.SM_MATCH_ENDED);
		log.info("Match is over. Closing the session.");
	}

	/**
	 * Next turn
	 */
	private void phaseNextTurn() {
		game.setCurrentPhase(PhaseName.blank);

		/**
		 * Check extra turn
		 */
		if (playerWithPriority.hasExtraTurns()) {
			publishSystemMessage(game.getNicknameById(playerWithPriority.getPlayerId()) + " takes an extra turn.");
			playerWithPriority.spendAnExtraTurn();
		} else if (opponentPlayer.isSkipNextTurn()) { 
			publishSystemMessage(game.getNicknameById(opponentPlayer.getPlayerId()) + " skips his or her turn.");
			opponentPlayer.takeAnExtraTurnAfterThisOne();
		} else {
			swapPriority();
			game.setActivePlayerId(game.getPriorityPID());
		}
		
		game.getEventManager().getChangeTurnEvent().notifyObservers();
	}

	/**
	 * Untap phase.
	 */
	private void phaseUntap() {
		game.setTurnNumber(game.getTurnNumber() + 1);
		playerWithPriority.resetLandPlayed();
		game.untap();
		checkChangesAndStateEffects();
	}

	private void swapPriority() {
		opponentPlayer = game.getPlayer(game.getPriorityPID());
		if (game.getPriorityPID() == pid1) {
			game.givePriorityToPlayer(pid2);
		} else {
			game.givePriorityToPlayer(pid1);
		}
		playerWithPriority = game.getPlayer(game.getPriorityPID());
	}

	private void phaseUpkeep() {
		game.upkeep();
		checkChangesAndStateEffects();

		if (game.getStack().size() > 0) {
			bWaitingForAnswer = true;
			game.setWaitingPlayerID(opponentPlayer.getPlayerId());
			checkStackForBeingEmpty(0);
		}
	}

	private void phaseDraw() {
		if (!playerWithPriority.getGameStatistics().isSkipYourDrawStep()) {
			for (int i = 0; i < game.getRules(game.getActivePlayerId()).getDrawsPerTurn(); i++) {
				playerWithPriority.drawCard();
			}
		}

		checkChangesAndStateEffects();
		
		if (game.getStack().size() > 0) {
			bWaitingForAnswer = true;
			game.setWaitingPlayerID(opponentPlayer.getPlayerId());
			checkStackForBeingEmpty(0);
		}		
	}

	private void phaseMain1() {
		if (game.checkPrecombatMainPhaseCommands()) {
			checkChangesAndStateEffects();
			if (game.getStack().size() > 0) {
				bWaitingForAnswer = true;
				//game.setWaitingPlayerID(opponentPlayer.getPlayerId());
				checkStackForBeingEmpty(0);
			}
		}
	}

	private void phaseBeforeCombat() {
		swapPriority();
		bPriorityWasSwapped = true;
	}

	private void phaseDeclareAttackers() {
		if (bPriorityWasSwapped) {
			swapPriority(); // swap back
			bPriorityWasSwapped = false;
		}

		for (Combat combat : game.getCombats()) {
			combat.setAttackingPlayerId(playerWithPriority.getPlayerId());
			combat.setDefendingPlayerId(opponentPlayer.getPlayerId());
		}

		game.executeAttackEffects();
		if (game.getCombat().getAttackersAddedOnServer() > 0) {
			checkChangesAndStateEffects();
		}
	}

	private void phaseDeclareBlockers() {
		game.executeBlockEffects();
		if (game.getAttackerCount() > 0) {

			game.declareBlockers();
			checkChangesAndStateEffects();

			if (game.getStack().size() > 0) {
				bSecondLoop = true;
				game.setWaitingPlayerID(opponentPlayer.getPlayerId());
				checkStackForBeingEmpty(0);

				while (bSecondLoop == true && game.isGameActive() == true) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
				bWaitingForAnswer = false;
			}

			swapPriority();
			bPriorityWasSwapped = true;

		} else {
			game.setCurrentPhase(PhaseName.main2);
		}
	}

	public Serializable showModal(int playerId, String question, ArrayList<Serializable> values) {
		if (playerId != pid1 && playerId != pid2) {
			return "error, wrong player id: " + playerId;
		}
		game.setWaitingPlayerID(playerId);
		game.resetResult();
		messages.publishMessageForUser(MessageID.SM_CHOOSE_STRING_VALUE, playerId, question, values);
		while (game.isGameActive() == true && game.noResult()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		return game.getResult();
	}

	public void setModalResult(int playerId, Serializable result) {
		if (playerId == game.getWaitingPlayerID()) {
			game.setResult(result);
		}
	}

	private void checkChangesAndStateEffects() {
		game.checkChangesAndStateEffects();
		if (game.isUpdateAvailable()) {
			messages.publishUpdateMessage(game.getGameStateObserver().peekCurrentAtoms());
		}
	}

	private void publishSystemMessage(String msg) {
		int pid = game.getPriorityPID();
		messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid, msg);
		messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, game.getOpponentID(pid), msg);
	}

	private void phaseCombatInstantsPlayer() {
		if (bPriorityWasSwapped) {
			swapPriority(); // swap back
			bPriorityWasSwapped = false;
			messages.publishMessageForUser(MessageID.SM_BLOCKERS_CHANGED, game.getPriorityPID());
		}

		game.executeBlockedCommands();
		game.executeBlocksCommands();

		if (game.getStack().size() > 0) {
			bSecondLoop = true;
			bWaitingForAnswer = true;
			game.setWaitingPlayerID(opponentPlayer.getPlayerId());
			checkStackForBeingEmpty(0);

			while (bSecondLoop == true && game.isGameActive() == true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			bWaitingForAnswer = false;
		}

		if (game.isNeedFirstStrikeCombat()) {
			messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid1, "first strike combat");
			messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid2, "first strike combat");
		}
	}

	private void phaseCombatInstantsOpponent() {
		game.verifyCreaturesInPlay();
		if (!game.isNeedFirstStrikeCombat()) {
			game.checkFirstStrike();
			if (game.isNeedFirstStrikeCombat()) {
				messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid1, "first strike combat");
				messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid2, "first strike combat");
			}
		}
		if (game.getAttackerCount() > 0) {
			swapPriority();
			bPriorityWasSwapped = true;
			messages.publishMessageForUser(MessageID.SM_BLOCKERS_CHANGED, game.getPriorityPID());
		} else {
			game.setCurrentPhase(PhaseName.main2);
		}

	}

	private void phaseDamageStackPlayer() {
		game.verifyCreaturesInPlay();
		if (!game.isNeedFirstStrikeCombat()) {
			game.checkFirstStrike();
			if (game.isNeedFirstStrikeCombat()) {
				messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid1, "first strike combat");
				messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, pid2, "first strike combat");
			}
		}
		setAssignedDamageAuto();
		if (bPriorityWasSwapped) {
			swapPriority(); // swap back
			bPriorityWasSwapped = false;
		}
	}

	private void phaseDamageStackOpponent() {
		game.verifyCreaturesInPlay();
		if (game.getAttackerCount() > 0) {
			swapPriority();
			bPriorityWasSwapped = true;
		} else {
			game.goToNextPhase();
		}
	}

	private void phaseCombatDamage() {

		if (bPriorityWasSwapped) {
			swapPriority(); // swap back
			bPriorityWasSwapped = false;
		}

		game.verifyCreaturesInPlay();
		game.damageCreaturesAndPlayers(playerWithPriority.getPlayerId(), opponentPlayer.getPlayerId());

		game.checkDamagePlayerEffects();
		game.checkStateEffects();

		// publishAllSystemMessages();

		/**
		 * Reset combat only if it wasn't first strike combat
		 */
		if (game.isNeedFirstStrikeCombat()) {
			game.setIsNeedFirstStrikeCombat(false);
			game.setWasNeedFirstStrikeCombat(true);
		} else {
			game.getEndOfCombat().executeAt();
			game.getEndOfCombat().executeAtEachTurn();
			game.getEndOfCombat().executeUntil();
			
			game.setWasNeedFirstStrikeCombat(false);
			game.resetCombat();
		}

		checkChangesAndStateEffects();
		//messages.publishMessage(MessageID.SM_CLEAR_INFO);

		/**
		 * Look into the stack to check if there are any Spell Abilities to play
		 */
		if (game.getStack().size() > 0) {
			bWaitingForAnswer = true;
			game.setWaitingPlayerID(opponentPlayer.getPlayerId());
			checkStackForBeingEmpty(0);
		}
	}

	private void phaseMain2() {
		if (playerWithPriority.getGameStatistics().isThereAnotherCombat()) {
			game.setCurrentPhase(PhaseName.combat_declare_attackers); 
			//TODO: may be here should be setCurrentPhase(PhaseName.before_combat)) to call second pre combat phase 
			playerWithPriority.getGameStatistics().setThereIsAnotherCombat(false);
			playerWithPriority.getGameStatistics().setFirstCombat(false);
			messages.publishMessageWith2Params(MWMessage.MessageID.SM_PHASE_CHANGED, game.getCurrentPhaseName(), game.getPriorityPID());
		} else {
			game.setCurrentPhase(PhaseName.main2);
		}
	}

	private void phaseAtEndOfTurn() {
		swapPriority();
		bPriorityWasSwapped = true;
		if (!game.getPlayer(game.getPriorityPID()).isPlayAtEndOfTurn()) {
			phaseEndOfTurn();
			game.goToNextPhase();
			if (!bWaitingForAnswer) {
				game.goToNextPhase();
			}
		}
	}

	private void phaseEndOfTurn() {	
		if (bPriorityWasSwapped == true) {
			swapPriority(); // swap back
			bPriorityWasSwapped = false;
		}

		// this is a hack to solve interaction problems current turn structure implementation has
		// FIXME: this should be removed when turn structure is implemented correctly
		if (turnCheck == game.getTurnNumber()) {
			return;
		}
		turnCheck = game.getTurnNumber();
			
		game.endOfTurn();
		checkChangesAndStateEffects();

		if (game.getStack().size() > 0) {
			bWaitingForAnswer = true;
			game.setWaitingPlayerID(opponentPlayer.getPlayerId());
			checkStackForBeingEmpty(0);
		}
	}

	private void phaseCleanUp() {
		if (bPriorityWasSwapped == true) {
			swapPriority(); // swap back
			bPriorityWasSwapped = false;
		}

		checkChangesAndStateEffects();

		if (game.getPlayerById(game.getPriorityPID()).getHand().size() > game.getRules(game.getActivePlayerId()).getMaxHandSize()) {
			messages.publishMessageForUser(MWMessage.MessageID.SM_CLEANUP_DISCARD, game.getPriorityPID());
			bWaitingForAnswer = true;
			game.setWaitingPlayerID(game.getPriorityPID());
		} else {
			bWaitingForAnswer = false;
			// waitingPID = 0;
		}

		game.getPlayer1().getGameStatistics().resetScopeTurn();
		game.getPlayer2().getGameStatistics().resetScopeTurn();

		game.getRevealedCardsToPlay().clear();
		game.setPreventCombatDamageThisTurn(false);
	}

	/**
	 * Check if player set activity mark for current phase.
	 * 
	 * @param phase current phase
	 */
	private void checkMark(PhaseName phase) {

		/**
		 * Check first if current phase is marked. If not then go to the next
		 * phase.
		 */
		if (!playerWithPriority.isMarkExists(phase) && !phase.equals(PhaseName.combat_stack_damage_player)) {
			if (bPriorityWasSwapped) {
				swapPriority(); // swap back
				bPriorityWasSwapped = false;
			}
			return;
		}

		/**
		 * Waiting for active player.
		 */
		game.setWaitingPlayerID(game.getPriorityPID());

		/**
		 * Mark we are waiting for his response.
		 */
		bWaitingForAnswer = true;

		/**
		 * Log.
		 */
		System.out.println("[Server] Waiting for response!");
	}

	// ////////////////////////////////////////////////////////////////////

	public PhaseName getCurrentPhase() {
		return game.getCurrentPhaseName();
	}

	public int getActivePID() {
		return game.getPriorityPID();
	}

	public void stopTheGame() {
		log.info("stopTheGame()");
		game.setGameActive(false);
	}

	/**
	 * Got response from a player. Check if he is waiting player. If he is then
	 * go next phase by setting corresponding flag.
	 * 
	 * @param playerID
	 */
	public void next(int playerID, PhaseName phase) {
		log.debug("Next phase call (phaseID=" + game.getCurrentPhaseName() + ", playerID = " + playerID + ", waitingPID = " + game.getWaitingPlayerID() + ")");
		if (playerID == game.getWaitingPlayerID() && phase.equals(game.getCurrentPhaseName())) {

			game.removeMadnessCardsIfAny(playerID);

			/**
			 * Check mana pool
			 */
			MWPlayer player = game.getPlayer(playerID);
			if (player != null) {
				ManaPool mp = player.getManaPool();
				int mana_count = mp.black + mp.blue + mp.green + mp.red + mp.white + mp.uncolored;
				if (mana_count > 0) {
					ManaPool emptyManaPool = new ManaPool();
					player.setManaPool(emptyManaPool);
					checkChangesAndStateEffects();
				}
			}

			/**
			 * Check opponent's mana pool
			 */
			MWPlayer opponent = game.getOpponentById(playerID);
			if (opponent != null) {
				ManaPool mp = opponent.getManaPool();
				int mana_count = mp.black + mp.blue + mp.green + mp.red + mp.white + mp.uncolored;
				if (mana_count > 0) {
					ManaPool emptyManaPool = new ManaPool();
					opponent.setManaPool(emptyManaPool);
					checkChangesAndStateEffects();
				}
			}
			
			if (bPriorityWasSwapped) {
				swapPriority(); // swap back
				bPriorityWasSwapped = false;
			}
			bWaitingForAnswer = false;
		} else {
			log.warn("request was ignored, " + playerID + " vs. " + game.getWaitingPlayerID());
		}
	}

	// ////////////////////////////////////////////////////////////////////

	public int getPlayerID1() {
		return pid1;
	}

	public int getPlayerID2() {
		return pid2;
	}

	public int incAgreement() {
		return ++nStartGame;
	}

	public ZippedObjectImpl<CardBeanList> getHand(int playerId) {
		MWPlayer player = game.getPlayer(playerId);

		return new ZippedObjectImpl<CardBeanList>(player.getHandBeans());
	}

	public ZippedObjectImpl<CardBeanList> getHand(int playerId, int offset, int count) {
		MWPlayer player = game.getPlayer(playerId);

		return new ZippedObjectImpl<CardBeanList>(player.getHandBeans(offset, count));
	}

	public int getHandSize(int playerID) {
		MWPlayer player = game.getPlayer(playerID);
		return player.getHand().size();
	}

	public ManaPool getManaPool(int playerId) {
		MWPlayer player = game.getPlayer(playerId);
		return player.getManaPool();
	}

	public int getLifeCount(int playerId) {
		MWPlayer player = game.getPlayer(playerId);
		return player.getLifeCount();
	}

	public void setPhaseMarks(int playerId, EnumSet<PhaseName> marks) {
		game.getPlayer(playerId).setPhaseMarks(marks);
	}

	public void setCombatPhaseMarks(int playerId, EnumSet<PhaseName> marks) {
		game.getPlayer(playerId).setCombatPhaseMarks(marks);
	}

	public boolean isActivePlayer(int playerId) {
		if (playerId == game.getPriorityPID()) {
			return true;
		} else {
			return false;
		}
	}

	public ZippedObject<CardBeanList> getTableInformation() {
		HashMap<Integer, CardBean> h = game.getTableBeans();
		CardBeanList list = new CardBeanList();

		for (Integer key : h.keySet()) {
			CardBean b = h.get(key);
			list.add(b);
		}

		return new ZippedObjectImpl<CardBeanList>(list);
	}

	public void mulligan(int playerID) {
		MWPlayer p = game.getPlayer(playerID);

		int handsize = p.getHand().size();
		if (handsize > 0) {
			Card[] cards = p.getHandCards();

			for (int i = 0; i < cards.length; i++) {
				p.library.add(cards[i]);
				p.getHand().remove(cards[i]);
			}

			/**
			 * Shuffle several times.
			 */
			for (int i = 0; i < 10; i++) {
				p.shuffleLibrary();
			}

			/**
			 * Draw new cards.
			 */
			for (int i = 0; i < handsize - 1; i++) {
				p.drawCard();
			}
			
			checkChangesAndStateEffects();
		}

		publishSystemMessage(game.getNicknameById(playerID) + " decided to mulligan (" + (handsize - 1) + ")");

		//messages.publishMessage(MWMessage.MessageID.SM_HAND_CHANGED);
	}

	public void noMulligan(int playerID) {
		if (playerID == game.getWaitingPlayerID()) {
			bWaitingForAnswer = false;
		}
	}

	public void drawFirst(int playerID) {
		if (playerID == game.getWaitingPlayerID()) {
			bWaitingForAnswer = false;
			swapPriority();
		}
	}

	public void playFirst(int playerID) {
		if (playerID == game.getWaitingPlayerID()) {
			bWaitingForAnswer = false;
		}
	}

	public int playCard(int playerID, CardBean cardBean, GameZone zone) {
		log.info("MWGameThread.playCard():" + cardBean.getName());
		Card card = game.findCard(playerID, cardBean, zone);

		if (card == null) {
			log.error("playCard: couldn't find the card: " + cardBean + ", zone:" + zone);
			throw new IllegalArgumentException("playCard: couldn't find the card: " + cardBean + ", zone:" + zone);
		}

		return castSpell(playerID, card, zone);
	}
	
	public int castSpell(int playerId, Card card, GameZone zone) {
		log.info("MWGameThread.castSpell():" + card.getName());

		/**
		 * Remark: card.getSpellAbility().length < 2 this clause is used for
		 * lands that have abilities (such as Mutavault): for a while, if land
		 * has abilities, then it CAN be used to put mana to manapool only
		 * during paying for spell or ability (so you can't add 1 to your
		 * manapool by Mutavault but you can use it to pay 1 WHEN playing spell
		 * or ability)
		 */
		//TODO:remove this part
		if ((card.isLand() || card.getManaAbilities().size() > 0) && zone.equals(GameZone.Battlefield) && !card.isTapped() && (card.getSpellAbility().length < 2)
				&& !card.isToken()) {

			String color = getManaColor(card, null);
			if (color == null) {
				return 0;
			}

			Card c = game.getBattlefield().getPermanent(card.getTableID());

			if (color.equals("choose")) {
				c.tap();
				checkChangesAndStateEffects();
				return 0;
			}

			c.tap();
			game.getPlayer(playerId).getManaPool().add(color);
			checkChangesAndStateEffects();
		} else {

			/**
			 * Set card zone. Used by gameManager.
			 */
			game.setCardZone(zone);

			choices = SpellUtility.canPlaySpellAbility(game, card, card.getSpellAbility(), zone, playerId);
			SpellAbility sa = null;

			if (choices.length > 0) {
				card.updateAspectValue(MagicWarsModel.ASPECT_WAS_CAST_FROM_ZONE, zone);
			}
			
			if (choices.length == 0) {
				log.warn("can't play the card: no choice");
				return -1;
			} else if (choices.length == 1) {
				log.debug("choice: one");
				sa = choices[0];
			} else {
				if (card.hasAspect(MagicWarsModel.ASPECT_AUTO_CAST) && choices.length == 2) {
					game.rememberSpellAbililty(choices[1]);
					// Funeral Contest
					return playSpellAbility(choices[0], card.getControllerID(), true);
				}
				log.debug("choice: multi");
				messages.publishMessageForUser(MWMessage.MessageID.SM_CHOOSE_SPECIFIC_ABILITY_PLAY, choices[0].getSourceCard().getControllerID(), choices, false);
			}

			if (sa == null) {
				return 0;
			}

			gameState.remember();
			
			return playSpellAbility(sa, card.getControllerID(), true);
		}

		return 0;
	}

	public int playSpellAbility(SpellAbility sa, int playerID, boolean isNewSpell) {
		log.debug("MWGameThread.playSpellAbility()");
		if (isNewSpell) {
			game.setCurrentSpellAbility(sa);
			
			if (!sa.isAbility()) {
				if (!sa.hasAspect(MagicWarsModel.ASPECT_WAS_CASTED)) {
					sa.addAspect(MagicWarsModel.ASPECT_WAS_CASTED);
				}
			}
			
			if (!game.getCardZone().equals(GameZone.Cascade) && !sa.getSourceCard().hasAspect(MagicWarsModel.ASPECT_CAST_NO_MANACOST)) {
				String manaCost = game.applyManaCostEffects(sa);
				sa.setManaToPlay(manaCost);
				sa.getCost().reset();
				if (!sa.hasAspect(MagicWarsModel.ASPECT_SPELL_PLAYED_BY_AI)) {
					sa.setTargetCard(null);
				}
			}
			
			if (sa.getSourceCard().hasAspect(MagicWarsModel.ASPECT_CAST_NO_MANACOST)) {
				sa.setManaToPlay("0");
			}
			
			if (sa.isNeedsToChooseX()) {
				// for spells played by Cascade X is always 0
				if (game.getCardZone().equals(GameZone.Cascade)) { 
					sa.setXValue(0);
				} else {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_X_VALUE, playerID);
					return 0;
				}
			}

			if (sa.isAbilityOptional()) {
				game.getStack().add(sa);
				if (sa.getPlayerIdToAsk() == 0) {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_YES_NO, sa.getSourceCard().getControllerID(), sa);
				} else {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_YES_NO, sa.getPlayerIdToAsk(), sa);
				}
				return 0;
			}
		}

		if (sa.isNeedsTargetCreaturePlayer()) {
			sa.setPossibleTargets(game.filterOutTargetsWithProtection(game.getBattlefield().getAllCreatures(), sa));
			sa.setChooseTargetDescription("Select target Creature or Player.");
			messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, playerID, sa);
		} else if (sa.isNeedsTargetCreature()) {
			sa.setPossibleTargets(game.filterOutTargetsWithProtection(game.getBattlefield().getAllCreatures(), sa));
			sa.setChooseTargetDescription("Select target Creature.");
			messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, playerID, sa);
		} else if (sa.isNeedsTargetPlayer()) {
			sa.setChooseTargetDescription("Select target Player.");
			messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, playerID, sa);
		} else if (sa.isNeedsTargetOpponent()) {
			sa.setChooseTargetDescription("Select target Opponent.");
			messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, playerID, sa);
		} else if (sa.isNeedsTargetPermanent()) {
			sa.setPossibleTargets(game.filterOutTargetsWithProtection(game.getBattlefield().getAllPermanents(), sa));
			sa.setChooseTargetDescription("Select target permanent.");
			messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, playerID, sa);
		} else if (sa.isNeedsChooseSpecific()) { // FIXME: choose -->
													// targetSpecific, refactor
													// all cards that used this
			sa.formChoice();
			if (sa.getPlayerIdToAsk() > 0) {
				messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, sa.getPlayerIdToAsk(), sa);
			} else {
				messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, playerID, sa);
			}
		} else if (sa.isNeedsToChooseSpecificPermanent()) {
			sa.formChoice();
			messages.publishMessageForUser(MessageID.SM_CHOOSE_PERMANENTS, playerID, sa);
		} else if (sa.isNeedsToChooseAbility()) {
			game.setCurrentSpellAbility(sa);
			sa.formAbilityChoice();
			messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_ABILITY, sa.getSourceCard().getControllerID(), sa, true);
		} else if (sa.isNeedsDiscardCardToPay()) {
			log.debug("MWGameThread.playSpellAbility():sa.isNeedsDiscardCardToPay()");
			sa.formPayChoice();
			messages.publishMessageForUser(MessageID.SM_CHOOSE_CARD_TO_DISCARD_PAY, playerID, sa);
		} else if (sa.isNeedsToChooseCard()) {
			game.setCurrentSpellAbility(sa);
			sa.formChoice();
			if (sa.getPlayerIdToAsk() > 0) {
				messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_CARD, sa.getPlayerIdToAsk(), sa);
			} else {
				messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_CARD, sa.getSourceCard().getControllerID(), sa);
			}
		} else if (sa.isNeedsToChooseManaSymbol()) {
			ArrayList<String> mana = new ArrayList<String>();
			mana.add("W");
			mana.add("U");
			mana.add("B");
			mana.add("R");
			mana.add("G");
			messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_MANASYMBOL, sa.getSourceCard().getControllerID(), mana);
		} else {

			if (checkSpellIsPaid(sa, playerID, false, true) != 0) {
				return 0;
			}
			completePlayingSpellAbility(playerID);
		}

		return 0;
	}

	/**
	 * Tap permanent for mana
	 * 
	 * @param permanentID
	 */
	public ManaCost tapPermanentForMana(int playerID, int permanentID, ManaCost spellManaCost) {

		SpellAbility spellAbility = game.getCurrentSpellAbility();
		if (spellAbility == null) {
			log.error("tapPermanentForMana, sa is null.");
			return spellManaCost;
		}

		Card card = game.getBattlefield().getPermanent(permanentID);

		if (spellAbility.isTapAbility() && spellAbility.getSourceCard().equals(card)) {
			log.info("Player pays manacost for tap ability. Can't tap the same permanent ability belongs to.");
			return spellManaCost;
		}

		if (card == null) {
			log.error("tapPermanentForMana, card is null.");
			return spellManaCost;
		}

		if (card.getControllerID() != playerID) {
			log.error("tapPermanentForMana: you doesn't own the permanent. " + card.getControllerID() + " vs. " + playerID);
			return null;
		}

		/**
		 * Determine automatically mana color to get from the permanent
		 */
		String color = getManaColor(card, spellManaCost);

		/**
		 * There is no suitable mana
		 */
		if (color == null) {
			for (SpellAbility m : card.getSpellAbilities()) {
				if (m instanceof ManaAbility && m.canPlay()) {
					messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, playerID, "You can use {" + card + "} to put mana to your mana pool only. Cancel playing spell, then tap {" + card
							+ "}.");
					break;
				}
			}
			log.error("tapPermanentForMana: no suitable mana to use for spell");
			return null;
		}

		/**
		 * Couldn't determine automatically. Player has been asked to choose in
		 * getManaColor(Card, ManaCost).
		 * 
		 * @see getManaColor
		 * @see setChosenManaSymbol
		 */
		if (color.equals("choose")) {
			//card.tap();
			gameState.add(new TapAction(game, card, "pay cost"));
			checkChangesAndStateEffects();
			log.info("tapPermanentForMana: choose mana.");
			return null;
		}

		/**
		 * Spend mana
		 */
		ManaCost manaCostNew = spendManaForSpell(card.getControllerID(), color, spellManaCost);

		/**
		 * Mana has been spent
		 */
		if (manaCostNew != null) {
			// fixes bug with Viashino Skeleton
			spellAbility.setManaToPlay(manaCostNew.toString());
			
			//card.tap();
			gameState.add(new TapAction(game, card, "pay cost"));

			checkChangesAndStateEffects();

			if (manaCostNew.isPaid()) {
				if (spellAbility.isNeedsDiscardCardToPay()) {
					spellAbility.formPayChoice();
					messages.publishMessageForUser(MessageID.SM_CHOOSE_CARD_TO_DISCARD_PAY, playerID, spellAbility);
				} else if (spellAbility.isNeedsAdditionalCostToPlay()) {
					spellAbility.formPayChoice();
					messages.publishMessageForUser(MessageID.SM_CHOOSE_TARGETS_AS_ADDITIONAL_COST, playerID, spellAbility);
				} else {

					CostList costToPlay = spellAbility.getCost();
					while (costToPlay.hasNextCost()) {
						Cost cost = costToPlay.getNextCost();
						if (cost instanceof TapCost) {
							cost.pay(spellAbility.getSourceCard());
							checkChangesAndStateEffects();
						} else if (cost instanceof SacrificeCost) {
							cost.pay(spellAbility.getSourceCard());
							checkChangesAndStateEffects();
						} else if (cost instanceof DiscardCost) {
							spellAbility.setChoiceDetailedDescription(spellAbility.getSourceCard() + ": discard a card.");
							spellAbility.setChoice(game.getPlayer(spellAbility.getSourceCard().getController()).getHandList());
							messages.publishMessageForUser(MessageID.SM_CHOOSE_CARD_TO_DISCARD_PAY, playerID, spellAbility);
							return manaCostNew;
						} else if (cost instanceof ExileCost) {
							spellAbility.setDiscardEqualToRemove(true); //TODO: rework this part
							spellAbility.setChoiceDetailedDescription(spellAbility.getSourceCard() + ": exile a card.");
							spellAbility.setChoice(game.getPlayer(spellAbility.getSourceCard().getController()).getHandList());
							messages.publishMessageForUser(MessageID.SM_CHOOSE_CARD_TO_DISCARD_PAY, playerID, spellAbility);
							return manaCostNew;
						} else if (cost instanceof LoyaltyCost) {
							cost.pay(spellAbility.getSourceCard());
							checkChangesAndStateEffects();
						} else if (cost instanceof RemoveCounterCost) {
							cost.pay(spellAbility.getSourceCard());
							checkChangesAndStateEffects();
						} else if (cost instanceof AdditionalCost) {
							
							if (((AdditionalCost)cost).hasChoice()) {
								bSecondLoop = true;
								game.setWaitingPlayerID(playerWithPriority.getPlayerId());
								game.setCurrentCost(cost);
	
								spellAbility.setChoice(((AdditionalCost) cost).getChoice());
								spellAbility.setChoiceDescription(((AdditionalCost) cost).getDescription());
								messages.publishMessageForUser(MessageID.SM_CHOOSE_TARGETS_AS_ADDITIONAL_COST, playerID, spellAbility);
	
								return manaCostNew;
							} else {
								cost.pay(spellAbility.getSourceCard());
								checkChangesAndStateEffects();
							}

						} else if (cost instanceof PayLifeCost) {
							cost.pay(spellAbility.getSourceCard());
							checkChangesAndStateEffects();
						}
					}

					if (checkMultikicker(spellAbility, playerID)) {
						return manaCostNew;
					}

					completePlayingSpellAbility(playerID);
				}
			}
		}

		return manaCostNew;
	}

	/**
	 * Automatically choose mana color to pay for manacost. If it's ambiguous,
	 * ask player to choose.
	 * 
	 * @param card
	 *            card that produces mana
	 * @param manaCost
	 *            manacost to pay for
	 * @return chosen mana color
	 */
	private String getManaColor(Card card, ManaCost manaCost) {
		// ArrayList<String> mana = (ArrayList<String>) Input_PayManaCostUtil
		// .getManaAbilities(card);
		ArrayList<String> mana = card.getManaAbilities();

		if (mana.isEmpty() || (card.isCreature() && card.hasSickness())) {
			return null;
		}

		ArrayList<String> choices = new ArrayList<String>();
		String color;
		/**
		 * Player clicked permanent for mana without playing any spell (to put
		 * it into mana pool)
		 */
		if (manaCost == null) {
			for (int i = 0; i < mana.size(); i++) {
				color = mana.get(i);
				choices.add(color);
			}
		} else {
			for (int i = 0; i < mana.size(); i++) {
				color = mana.get(i);
				if (manaCost.isColoredNeeded(color)) {
					choices.add(color);
				}
			}
		}

		/**
		 * Choose one mana color to pay for the manacost
		 */
		if (choices.size() > 1 && !card.getName().equals("Ancient Lotus")) { // no
																				// choice
																				// for
																				// Ancient
																				// Lotus
			messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_MANASYMBOL, card.getControllerID(), choices);
			game.setCurrentManaCost(manaCost);

			return "choose";
		} else if (choices.size() == 1 || (card.getName().equals("Ancient Lotus") && choices.size() > 0)) { // no
																											// choice
																											// or
																											// it's
																											// Ancient
																											// Lotus
			color = choices.get(0);
		} else {
			/**
			 * There is no mana color that suits for manacost. So we should try
			 * to pay (any of them) for colorless part
			 */
			color = mana.get(0);
		}

		return color;
	}

	/**
	 * Spend specific mana color for spell (manacost)
	 * 
	 * @param playerID
	 * @param color mana to spend. may contain multi value, e.g. "GG"
	 * @param manaCost
	 *            manacost to pay for
	 * @return changed ManaCost if mana can be payed for it otherwise null
	 */
	private ManaCost spendManaForSpell(int playerID, String color, ManaCost manaCost) {

		int count = 0;
		if (CommonUtil.checkIsNumber(color)) {
			count = Integer.parseInt(color);
		}
		boolean isSubtracted = false; 
		if (count > 0) {
			boolean result;
			for (int i = 0; i < count; i++) {
				result = manaCost.subtract(Constant.Color.Colorless);
				if (isSubtracted && !result) { // we pay "3" for "1 G", so add "2" to mana pool
					game.getPlayer(playerID).getManaPool().add(Constant.Color.Colorless);
				} else {
					//ManaPool mp = game.getGlobalManaPool();
					//mp.add(Constant.Color.Colorless);
				}
				isSubtracted |= result;
			}
		} else {
			String _color = color.replaceAll(" ", "");
			ArrayList<String> notUsedMana = new ArrayList<String>();
			for (int i = 0; i < _color.length(); i++) {
				// if we can use this mana for spell, then add it to global mana pool
				// that is temporary store that is used to return mana back to mana pool
				// when playing spell is canceled
				if (manaCost.subtract(String.valueOf(color.charAt(i)))) {
					isSubtracted = true;
					//ManaPool mp = game.getGlobalManaPool();
					//mp.add(color);
				} else {
					notUsedMana.add(String.valueOf(color.charAt(i)));
				}
		    }
			// if we used at least one mana for spell, then add unused to manapool
			if (isSubtracted) {
				for (String mana : notUsedMana) {
					game.getPlayer(playerID).getManaPool().add(mana);
				}
			}
		}
		
		if (isSubtracted == false && manaCost.getChoices() == null) {
			return null;
		}
		else if (manaCost.getChoices() != null) {
			game.setCurrentManaColor(color);
			game.setCurrentManaCost(manaCost);
			messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_MANASYMBOL, playerID, manaCost.getChoices());
		}

		return manaCost;
	}

	/**
	 * AllZone.Stack.add(spell); stopSetNext(new ComputerAI_StackNotEmpty());
	 */
	protected void completePlayingSpellAbility(int playerID) {
		// cost was paid so we can reset global mana pool
		// it is used to store mana spent for a spell and to return it if user
		// presses cancel
		game.getGlobalManaPool().reset();

		SpellAbility sa = game.getCurrentSpellAbility();
		GameZone zone = game.getCardZone();

		if (sa instanceof Flashback || sa instanceof Unearth) {
			if (zone.equals(GameZone.Graveyard)) {
				game.getGraveyard().remove(sa.getSourceCard());
			}
		} else {
			Card spellCard = sa.getSourceCard();
			if (zone != null) {
				if (zone.equals(GameZone.Hand)) {
					if (!game.getPlayer(playerID).getHand().remove(spellCard)) {
						game.getOpponentById(playerID).getHand().remove(spellCard);
						game.getRevealedCardsToPlay().remove(spellCard);
					}
				} else if (zone.equals(GameZone.Library)) {
					game.getPlayer(playerID).getLibrary().remove(spellCard);
				}
			}
			game.getCascadeCards().remove(spellCard);
		}
		
		if (sa.hasAspect(MagicWarsModel.ASPECT_WAS_CASTED)) {
			if (sa.isSpell() && sa.getSourceCard().isCreature()) {
				game.getPlayer(sa.getSourceCard().getControllerID()).getGameStatistics().setPlayedCreatureSpellThisTurn(true);
				game.getPlayer(sa.getSourceCard().getControllerID()).getGameStatistics().increaseCreatureSpellsCastedThisTurn();
			}
			if (sa.isSpell() || sa instanceof Flashback || sa instanceof Unearth) {
				game.getPlayer(sa.getSourceCard().getControllerID()).getGameStatistics().setPlayedSpellThisTurn(true);
			}
		}
		
		/**
		 * If the spell ability has madness, it will put corresponding spell
		 * into the stack itself on resolve depending on the type of source card
		 * (Spell or Spell_Permanent)
		 * 
		 * @see Spell_Madness
		 */
		int beforeCast = game.getStack().size();
		if (sa instanceof Madness) {
			Card madnessCard = sa.getSourceCard();
			madnessCard.removeAspect(MagicWarsModel.ASPECT_PLAY_MADNESS);
			game.getMadnessCards().remove(madnessCard);
			try {
				sa.resolve();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				messages.publishException(e);
			}
			/*
			 * if (sa.getSpellChain() != null) { checkChangesAndStateEffects();
			 * sa.getSpellChain().execute(); }
			 */
		} else {
			Card spellCard = sa.getSourceCard();
			if (game.getMadnessCards().contains(spellCard)) {
				game.getMadnessCards().remove(spellCard);
			}
			
			if (!game.getStack().contains(sa)) {
				game.getStack().add(sa);
				beforeCast++;
				// otherwise Kor Fireworker and Shock will make an infinite loop
				sa.removeAspect(MagicWarsModel.ASPECT_NEW_SPELL_ABILITY);
			}
		}
		
		if (sa.getTargetCards() != null) {
			CardList list = sa.getTargetCards();
			for (Card c : list) {
				EventParam event = new EventParam();
				event.addParam(c);
				event.addParam(sa);
				game.getEventManager().getTargetedEvent().notifyObservers(event);
			}
		}
		
		if (sa.hasAspect(MagicWarsModel.ASPECT_WAS_CASTED)) {
			//if (!sa.isInvisible()) {
				game.getPlayer(sa.getSourceCard().getControllerID()).getGameStatistics().addCardCastThisTurn(sa.getSourceCard());
				int beforeEvent = game.getStack().size();
				game.getEventManager().getCastingSpellEvent().notifyObservers(sa);
				publishMoveToZone(sa, playerID);
				sa.getSourceCard().whenPlayCommand();
				if (game.getStack().size() > beforeEvent) {
					checkStackForBeingEmpty(beforeEvent);
					return;
				}
			//}
		}

		game.setCurrentSpellAbility(null);
		game.setCurrentManaColor("");
		game.setCurrentManaCost(null);

		if (game.getStack().size() > beforeCast) {
			checkStackForBeingEmpty(beforeCast);
			return;
		}
		
		/**
		 * For "choose two"
		 */
		if (game.getRememberedSpellAbililty() != null) {
			SpellAbility sa2 = game.getRememberedSpellAbililty();
			game.rememberSpellAbililty(null);
			sa2.setManaToPlay("0");
			playSpellAbility(sa2, sa2.getSourceCard().getControllerID(), true);
			return;
		}

		/**
		 * If it's mana ability or it was marked as invisible, then don't need
		 * to ask to accept stack
		 */
		if (sa instanceof ManaAbility) {
			acceptStack();
			return;
		}
		if (sa.isInvisible()) {
			if (!checkAttachedSpellsExist(sa)) {
				return;
			}
		}

		/**
		 * If Cascade was put into the stack, then first need to decide to play
		 * or not the found card
		 */
		sa = game.getStack().peek();
		if ((!sa.hasAspect(MagicWarsModel.ASPECT_WAS_CASTED) && sa.hasAspect(MagicWarsModel.ASPECT_MAY_HAVE_NEW_TARGETS))) {
			checkStackForBeingEmpty(game.getStack().size() - 1);
			return;
		}

		if (extractAttachedSpells(sa)) {
			return;
		}

		//messages.publishMessageForUser(MessageID.SM_STACK_CHANGED, sa.getSourceCard().getControllerID());
		if (sa.getPlayerIdToAsk() == 0) {
			messages.publishMessageForUser(MWMessage.MessageID.SM_STACK_CHANGED, sa.getSourceCard().getControllerID());
		} else {
			messages.publishMessageForUser(MWMessage.MessageID.SM_STACK_CHANGED, sa.getPlayerIdToAsk());
		}
	}
	
	private void publishMoveToZone(SpellAbility sa, int playerId) {
		EventParam params = new EventParam();
		params.addParam(new PlayerZone(GameZone.Hand, playerId));
		params.addParam(new PlayerZone(GameZone.Stack, playerId));
		params.addParam(sa.getSourceCard());
		params.addParam(sa);
		game.getEventManager().getMoveToZoneEvent().notifyObservers(params);
	}
	
	private boolean checkAttachedSpellsExist(SpellAbility sa) {
		if (sa.hasAspect(MagicWarsModel.ASPECT_ALREADY_PROCESSED)) {
			acceptStack();
			return false;
		}

		if (!sa.hasAspect(MagicWarsModel.ASPECT_HAS_ATTACHED_SPELLS)) {
			if (sa.hasAspect(MagicWarsModel.ASPECT_ATTACHED)) {
				reverseAttachedSpellsInTheStack();
				return true;
			}
			acceptStack();
			return false;
		} else {
			SpellAbility attachedSpell = (SpellAbility) sa.getSpellChain().getParam();
			game.getStack().add(attachedSpell);
			checkStackForBeingEmpty(game.getStack().size() - 1);
		}
		return false;
	}

	private boolean extractAttachedSpells(SpellAbility sa) {
		if (sa.hasAspect(MagicWarsModel.ASPECT_ALREADY_PROCESSED)) {
			return false;
		}

		if (sa.hasAspect(MagicWarsModel.ASPECT_HAS_ATTACHED_SPELLS)) {
			SpellAbility attachedSpell = (SpellAbility) sa.getSpellChain().getParam();
			game.getStack().add(attachedSpell);
			checkStackForBeingEmpty(game.getStack().size() - 1);
			return true;
		}
		return false;
	}

	private void reverseAttachedSpellsInTheStack() {
		MWStack stack = game.getStack();
		SpellAbility sa = stack.pop();
		int uid = sa.getSourceCard().getUniqueNumber();
		ArrayList<SpellAbility> spells = new ArrayList<SpellAbility>();
		spells.add(sa);
		while (stack.size() > 0 && stack.peek().getSourceCard().getUniqueNumber() == uid) {
			sa = stack.pop();
			spells.add(sa);
		}
		for (SpellAbility spell : spells) {
			// SpellAbility copy = spell.getCopy();
			// copy.addAspect(MagicWarsModel.ASPECT_ALREADY_PROCESSED);
			// stack.add(copy);
			spell.addAspect(MagicWarsModel.ASPECT_ALREADY_PROCESSED);
			stack.add(spell);
		}
	}

	public boolean addAttacker(int playerID, int permanentID, GameZone zone, String player) {

		Card card = game.getBattlefield().getPermanent(permanentID);

		if (card == null) {
			return false;
		}

		if (zone.equals(GameZone.Battlefield) && card.isCreature() && card.isUntapped() && CombatUtil.canAttack(card)) {

			card.setAttacking(true);

			if (!card.hasKeyword(MagicWarsModel.KEYWORD_VIGILANCE_SA)) {
				card.tap();
				//messages.publishMessageWithParam(MessageID.SM_TABLE_CHANGED_TAPPED, card.getTableID());
				checkChangesAndStateEffects();
			}
			

			/**
			 * Attack a player
			 */
			if (player.equals("Player")) {
				game.getCombat().addAttacker(game, card);
				if (card.hasKeyword(MagicWarsModel.KEYWORD_FIRST_STRIKE_SA) || card.hasKeyword(MagicWarsModel.KEYWORD_DOUBLE_STRIKE_SA)) {
					game.setIsNeedFirstStrikeCombat(true);
					game.getCombat().setFirstStrikeCombat(true);
				}
			} else {
				/**
				 * Attack a planeswalker
				 */
				int index1 = player.indexOf("(");
				int index2 = player.indexOf(")");
				try {
					Integer id = Integer.parseInt(player.substring(index1 + 1, index2));
					System.out.println("Planeswakler ID: " + id);
					game.getCombat(id).addAttacker(game, card);

					if (card.hasKeyword(MagicWarsModel.KEYWORD_FIRST_STRIKE_SA) || card.hasKeyword(MagicWarsModel.KEYWORD_DOUBLE_STRIKE_SA)) {
						game.setIsNeedFirstStrikeCombat(true);
						game.getCombat().setFirstStrikeCombat(true);
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			return true;

		}

		return false;
	}

	public ZippedObjectImpl<CardBeanList> getAttackers() {
		return new ZippedObjectImpl<CardBeanList>(new CardBeanList(game.getAttackerList()));
	}

	public ZippedObjectImpl<CombatBean> getCombat() {
		return new ZippedObjectImpl<CombatBean>(new CombatBean(game, game.getCombat()));
	}

	public ZippedObjectImpl<HashMap<Integer, CombatBean>> getPWCombat() {
		HashMap<Integer, Combat> pwCombat = game.getPWCombat();
		HashMap<Integer, CombatBean> pwCombatBean = new HashMap<Integer, CombatBean>();

		for (Integer key : pwCombat.keySet()) {
			pwCombatBean.put(key, new CombatBean(game, pwCombat.get(key)));
		}

		return new ZippedObjectImpl<HashMap<Integer, CombatBean>>(pwCombatBean);
	}

	public void setCombat(CombatBean combat) {
		game.updateCombat(combat);
		// publishAllSystemMessages();
	}

	public void setPWCombat(HashMap<Integer, CombatBean> pwCombat) {
		game.updatePWCombat(pwCombat);
		// publishAllSystemMessages();
	}

	private void publishAllSystemMessages() {
		if (game.getSystemMessages().size() > 0) {
			for (String msg : game.getSystemMessages()) {
				publishSystemMessage(msg);
			}
			game.resetSystemMessages();
		}
	}

	/**
	 * Automatically assigns damage between attackers and blockers.
	 * 
	 * @see setAssignedDamage(CardList)
	 */
	public void setAssignedDamageAuto() {
		game.verifyCreaturesInPlay();
		game.acceptAssignedDamage();
	}

	public ZippedObjectImpl<MWStackBean> getStack() {
		return new ZippedObjectImpl<MWStackBean>(new MWStackBean(game.getStack()));
	}

	public void acceptStack() {

		log.debug("MWGameThread.acceptStack()");
		if (game.getStack().size() == 0) {
			log.debug("accept stack: stack is empty. exiting.");
			return;
		}

		SpellAbility sa = game.getStack().pop();
		Card c = sa.getSourceCard();

		if ((c.isInstant() || c.isSorcery()) && (!(sa instanceof Flashback || sa instanceof Unearth || sa.isInvisible()))) {
			//TODO: later may be reimplement this with more common replacement effect
			if (c.hasAspect(MagicWarsModel.ASPECT_EXILE_INSTEAD_ON_RESOLVE)) {
				game.getExile().add(c);
			} else {
				game.getGraveyard().add(c);
			}
		}

		int beforeResolveCount = game.getStack().size();

		if (checkTargetIsLegal(game, sa)) {

			try {
				sa.resolve();
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				messages.publishException(e);
			}
			
			checkChangesAndStateEffects();
		
			if (game.getRevealedCardsToPlay().size() > 0) {
				messages.publishMessageForUser(MessageID.SM_REVEALED_CHANGED_TO_PLAY, sa.getSourceCard().getControllerID());
			}
			
		} else {
			game.fizzleSpell(sa);
		}

		/**
		 * Look into the stack to check if there are any Spell Abilities to play
		 */
		checkStackForBeingEmpty(beforeResolveCount);
	}
	
	public static boolean checkTargetIsLegal(GameManager game, SpellAbility sa) {
		boolean fizzled = false;
		if (sa.getTargetCount() == 1 && !sa.hasAspect(MagicWarsModel.ASPECT_CHOOSING_NOT_TARGETING)) {

			Card target = sa.getTargetCard();
			if (target != null) {
				if (sa.isNeedsChooseSpecific()) {
					sa.formChoice();
					if (!sa.getChoice().contains(sa.getTargetCard())) {
						fizzled = true;
					}
				}

				if (sa.isNeedsTargetCreature() || sa.isNeedsTargetCreaturePlayer() || sa.isNeedsTargetPermanent() || sa.isNeedsChooseSpecific()) {
					if (game.checkProtection(target, sa.getSourceCard()) || game.checkShroud(target, sa.getSourceCard())) {
						fizzled = true;
					}
					if (!game.getBattlefield().isCardInPlay(target)) {
						fizzled = true;
					}
				}

			}
		}
		return !fizzled;
	}

	private void checkStackForBeingEmpty(int beforeResolveCount) {

		SpellAbility sa = game.getStack().peek();

		if (sa != null && sa.hasAspect(MagicWarsModel.ASPECT_NEW_SPELL_ABILITY) && sa.isAbilityOptional()) {
			//game.getStack().pop();
			game.setCurrentSpellAbility(sa);
			if (sa.getPlayerIdToAsk() == 0) {
				messages.publishMessageForUser(MessageID.SM_CHOOSE_YES_NO, sa.getSourceCard().getControllerID(), sa);
			} else {
				messages.publishMessageForUser(MessageID.SM_CHOOSE_YES_NO, sa.getPlayerIdToAsk(), sa);
			}
			return;
		}

		/**
		 * New spell ability appeared
		 */
		boolean flag = false;
		if (sa != null) {
			if (sa.isInvisible() || sa.hasAspect(MagicWarsModel.ASPECT_MAY_HAVE_NEW_TARGETS) || sa.hasAspect(MagicWarsModel.ASPECT_NEW_SPELL_ABILITY)) {
				// sa.isInvisible is used instead of
				// gameManager.getStack().add(sacrificeAbility1, true) in
				// Fleshbag Marauder
				flag = true;
			}
		}

		if (game.getStack().size() > beforeResolveCount || flag) {

			if (game.getStack().size() <= beforeResolveCount) { // that means we
																// need to
																// remove aspect
				// sa.getSourceCard().removeAspect(MagicWarsModel.ASPECT_NEW_SPELL_ABILITY);
				sa.removeAspect(MagicWarsModel.ASPECT_NEW_SPELL_ABILITY);
			}

			if (sa.hasAspect(MagicWarsModel.ASPECT_MAY_HAVE_NEW_TARGETS)) {
				sa.removeAspect(MagicWarsModel.ASPECT_MAY_HAVE_NEW_TARGETS);
			}

			if (sa.isAbilityOptional()) {
				game.getStack().pop();
				game.setCurrentSpellAbility(sa);
				if (sa.getPlayerIdToAsk() == 0) {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_YES_NO, sa.getSourceCard().getControllerID(), sa);
				} else {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_YES_NO, sa.getPlayerIdToAsk(), sa);
				}
				return;
			} else if (sa.isNeedsToChooseX2()) {
				game.setCurrentSpellAbility(sa);
				if (sa.getPlayerIdToAsk() == 0) {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_X_VALUE2, sa.getSourceCard().getCollectorID());
				} else {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_X_VALUE2, sa.getPlayerIdToAsk());
				}
				return;
			} else if (sa.isNeedsTargetCreature()) {
				game.getStack().pop();
				game.setCurrentSpellAbility(sa);
				sa.setPossibleTargets(game.filterOutTargetsWithProtection(game.getBattlefield().getAllCreatures(), sa));
				sa.setChooseTargetDescription("Select target Creature.");
				messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, sa.getSourceCard().getControllerID(), sa);
				// messages.publishMessageForUser(MessageID.SM_CHOOSE_TARGET_CREATURE,
				// sa.getSourceCard().getControllerID(), sa);
				return;
			} else if (sa.isNeedsTargetPlayer()) {
				game.getStack().pop();
				game.setCurrentSpellAbility(sa);
				sa.setChooseTargetDescription("Select target Player.");
				messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, sa.getSourceCard().getControllerID(), sa);
				// messages.publishMessageForUser(MessageID.SM_CHOOSE_TARGET_PLAYER,
				// sa.getSourceCard().getControllerID(), sa);
				return;
			} else if (sa.isNeedsTargetOpponent()) {
				game.getStack().pop();
				game.setCurrentSpellAbility(sa);
				sa.setChooseTargetDescription("Select target Opponent.");
				messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, sa.getSourceCard().getControllerID(), sa);
				return;
			} else if (sa.isNeedsTargetCreaturePlayer()) {
				game.getStack().pop();
				game.setCurrentSpellAbility(sa);
				sa.setPossibleTargets(game.filterOutTargetsWithProtection(game.getBattlefield().getAllCreatures(), sa));
				sa.setChooseTargetDescription("Select target Creature or Player.");
				messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, sa.getSourceCard().getControllerID(), sa);
				// messages.publishMessageForUser(MessageID.SM_CHOOSE_TARGET_CREATURE_OR_PLAYER,
				// sa.getSourceCard().getControllerID(), sa);
				return;
			} else if (sa.isNeedsToChooseCard()) {
				game.setCurrentSpellAbility(sa);
				sa.formChoice();
				if (sa.getPlayerIdToAsk() > 0) {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_CARD, sa.getPlayerIdToAsk(), sa);
				} else {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_CARD, sa.getSourceCard().getControllerID(), sa);
				}
				return;
			} else if (sa.isNeedsToChooseAbility()) {
				game.getStack().pop();
				game.setCurrentSpellAbility(sa);
				sa.formAbilityChoice();
				messages.publishMessageForUser(MessageID.SM_CHOOSE_SPECIFIC_ABILITY, sa.getSourceCard().getControllerID(), sa, true);
				return;
			} else if (sa.isNeedsChooseSpecific()) {
				//game.getStack().pop();
				game.setCurrentSpellAbility(sa);
				sa.formChoice();
				if (sa.getTargetPlayerID() == -1) {
					sa.setTargetPlayerID(sa.getSourceCard().getControllerID());
				}
				if (sa.getPlayerIdToAsk() > 0) {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, sa.getPlayerIdToAsk(), sa);
				} else {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_THE_TARGETS, sa.getTargetPlayerID(), sa);
				}
				return;
			} else if (sa.isNeedsToChooseSpecificPermanent()) {
				//game.getStack().pop();
				game.setCurrentSpellAbility(sa);
				sa.formChoice();
				if (sa.getTargetPlayerID() == -1) {
					sa.setTargetPlayerID(sa.getSourceCard().getControllerID());
				}
				if (sa.getPlayerIdToAsk() > 0) {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_PERMANENTS, sa.getPlayerIdToAsk(), sa);
				} else {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_PERMANENTS, sa.getTargetPlayerID(), sa);
				}
				return;
			} else if (sa.isNeedsToChooseAbilityToPlay()) {
				game.getStack().pop();
				choices = SpellUtility.canPlaySpellAbility(game, sa.getSourceCard(), sa.getSourceCard().getSpellAbility(), GameZone.Battlefield, sa.getSourceCard().getControllerID());
				messages.publishMessageForUser(MWMessage.MessageID.SM_CHOOSE_SPECIFIC_ABILITY_PLAY, sa.getSourceCard().getControllerID(), choices, false);
				return;
			} else if (sa.isNeedsDiscardCard()) {
				game.setCurrentSpellAbility(sa);
				int handsize = 0;
				if (sa.getTargetPlayerID() > 0) {
					MWPlayer player = game.getPlayer(sa.getTargetPlayerID());
					if (player == null) {
						addChosenCard(0, null);
						return;
					}
					handsize = player.getHandSize();
				}
				if (handsize > 0) {
					messages.publishMessageForUser(MessageID.SM_CHOOSE_CARD_TO_DISCARD, sa.getTargetPlayerID(), sa);
				} else {
					game.addSystemMessage(game.getNicknameById(sa.getTargetPlayerID()) + " has no cards to choose between.");
					addChosenCard(sa.getTargetPlayerID(), null);
				}
				return;
			}
		}

		if (game.getCascadeCards().size() > 0) {
			CardBean card = new CardBean(game.getCascadeCards().get(0));
			playCard(card.getControllerID(), card, GameZone.Cascade);
			return;
		}
		if (game.getMadnessCards().size() > 0) {
			CardBean card = new CardBean(game.getMadnessCards().get(0));
			playCard(card.getControllerID(), card, GameZone.Effect);
			return;
		}

		if (game.getStack().size() == 0) {
			messages.publishMessageForUser(MessageID.SM_STACK_EMPTY, game.getPriorityPID());
			game.setWaitingPlayerID(game.getPriorityPID());
			if (game.isCurrentPhase(PhaseName.combat_damage)) {
				bWaitingForAnswer = false;
				bSecondLoop = false;
			}
			if (game.isCurrentPhase(PhaseName.upkeep)) {
				bWaitingForAnswer = false;
			}
			if (game.isCurrentPhase(PhaseName.draw)) {
				bWaitingForAnswer = false;
			}
			if (game.isCurrentPhase(PhaseName.endofturn)) {
				game.setCurrentPhase(PhaseName.cleanup);
				phaseCleanUp();
			}
			if (game.isCurrentPhase(PhaseName.cleanup)) {
				game.setCurrentPhase(PhaseName.cleanup);
				phaseCleanUp();
			}
			if (game.isCurrentPhase(PhaseName.combat_declare_attackers)) {
				// why do we need this???
				// Knotvine Paladin doesn't work properly because of this line
				// you can't assign another than Paladin creature to attack
				// bWaitingForAnswer = false;
			}
			if (game.isCurrentPhase(PhaseName.combat_declare_blockers)) {
				// why do we need this???
				// it breaks declare blocker phase
				// play any instant spell before assigning blockers and you
				// won't be able to assign any blocker at all
				// bWaitingForAnswer = false;

				// for Novablast Wurm
				bSecondLoop = false;
			}
			if (game.isCurrentPhase(PhaseName.combat_predamage_player)) {
				// we need this for Vedalken Ghoul (when blocked)
				bSecondLoop = false;
			}
		} else {
			sa = game.getStack().peek();
			if (sa.isInvisible()) {
				if (sa.isAbilityOptional()) {
					game.getStack().pop();
					game.setCurrentSpellAbility(sa);
					if (sa.getPlayerIdToAsk() == 0) {
						messages.publishMessageForUser(MessageID.SM_CHOOSE_YES_NO, sa.getSourceCard().getControllerID(), sa);
					} else {
						messages.publishMessageForUser(MessageID.SM_CHOOSE_YES_NO, sa.getPlayerIdToAsk(), sa);
					}
					return;
				}

			}

			if (sa.isInvisible()) {
				if (!checkAttachedSpellsExist(sa)) {
					return;
				}
				sa = game.getStack().peek();
			}

			if (sa.isNeedsToChooseAbilityToPlay()) {
				game.getStack().pop();
				choices = SpellUtility.canPlaySpellAbility(game, sa.getSourceCard(), sa.getSourceCard().getSpellAbility(), GameZone.Battlefield, sa.getSourceCard().getControllerID());
				messages.publishMessageForUser(MWMessage.MessageID.SM_CHOOSE_SPECIFIC_ABILITY_PLAY, sa.getSourceCard().getControllerID(), choices, false);
				return;
			}

			//messages.publishMessageForUser(MessageID.SM_STACK_CHANGED, sa.getSourceCard().getControllerID());
			if (sa.getPlayerIdToAsk() == 0) {
				messages.publishMessageForUser(MWMessage.MessageID.SM_STACK_CHANGED, sa.getSourceCard().getControllerID());
			} else {
				messages.publishMessageForUser(MWMessage.MessageID.SM_STACK_CHANGED, sa.getPlayerIdToAsk());
			}
		}
	}

	public void setXValue(int playerID, int xValue) {
		SpellAbility sa = game.getCurrentSpellAbility();
		sa.setXValue(xValue);
		if (sa.getManaCost().contains("X")) {
			String manaToPlay = game.applyManaCostEffects(sa);
			if (sa.getSourceCard().hasAspect(MagicWarsModel.ASPECT_SPEND_ONLY_BLACK_MANA)) {
				String blackMana = "";
				for (int i = 0; i < xValue; i++) {
					blackMana += "B ";
				}
				blackMana = blackMana.trim();
				manaToPlay = manaToPlay.replaceAll("X", blackMana);
			} else {
				manaToPlay = manaToPlay.replaceAll("X", xValue + "");
			}
			sa.setManaToPlay(manaToPlay);
			sa.getCost().reset();
		}
		for (Cost cost : sa.getCost()) {
			if (cost instanceof LoyaltyCost) {
				LoyaltyCost lcost = (LoyaltyCost)cost;
				if (lcost.isNeedX()) { 
					lcost.setLoyaltyToPay(xValue);
					if (!lcost.canPayForCard(sa.getSourceCard())) {
						cancelChoosingSpecific(playerID);
						return;
					}
				}
			}
		}
		playSpellAbility(sa, playerID, false);
	}

	public void setXValue2(int playerId, int xValue) {
		SpellAbility sa = game.getCurrentSpellAbility();
		sa.setXValue2(xValue);
		completePlayingSpellAbility(playerId);
	}
	
	public void addTargetPlayer(int playerId, int targetPlayerID) {
		SpellAbility sa = game.getCurrentSpellAbility();

		String playerName = new MWProfile(targetPlayerID).getNickName();

		sa.setTargetPlayer(playerName);
		sa.setTargetPlayerID(targetPlayerID);

		if (checkSpellIsPaid(sa, playerId, true, false) != 0) {
			return;
		}

		completePlayingSpellAbility(playerId);
	}

	public void discardCard(int playerID, CardBean card) {

		Card handCard = game.findCard(playerID, card, GameZone.Hand);

		if (handCard == null) {
			log.debug("Couldn't find the card.");
			return;
		}

		game.discardCard(playerID, handCard);
		checkChangesAndStateEffects();
		
		if (game.getMadnessCards().size() > 0) {
			checkStackForBeingEmpty(0);
			return;
		}

		/**
		 * If clean up phase, then continue to discard
		 */
		if (game.isCurrentPhase(PhaseName.cleanup)) {
			phaseCleanUp();
		}
	}

	public void payDiscardCard(int playerID, CardBean cardBean) {
		SpellAbility sa = game.getCurrentSpellAbility();
		checkManaPool(playerID, sa);
		Card card = game.findCard(playerID, cardBean, GameZone.Hand);

		if (card == null) {
			log.error("Couldn't find the card.");
			return;
		}

		if (sa == null) {
			log.error("SpellAbility is null.");
			return;
		}

		log.debug("MWGameThread.payDiscardCard, sa: " + sa);
		if (!sa.isDiscardEqualToRemove()) {
			game.discardCard(playerID, card);
		} else {
			game.getPlayerById(playerID).getHand().remove(card);
		}
		
		if (checkSpellIsPaid(sa, playerID, false, false) != 0) {
			return;
		}

		checkChangesAndStateEffects();
		completePlayingSpellAbility(playerID);
	}

	private void checkManaPool(int playerID, SpellAbility sa) {
		ManaPool mp = game.getPlayer(playerID).getManaPool();
		if (mp.getConverted() > 0) {

			String remainsToPlay = mp.spendManaForSpell(sa.getManaToPlay(), 0);

			if (remainsToPlay.equals("")) {
				sa.setManaToPlay("0");
			} else {
				sa.setManaToPlay(remainsToPlay);
			}

			game.setGlobalManaPool(mp.getSpentMana());
			checkChangesAndStateEffects();
		}
	}

	public void setPlayAtEndOfTurn(int playerID, boolean isToPlay) {
		MWPlayer player = game.getPlayer(playerID);
		if (player != null) {
			player.setPlayAtEndOfTurn(isToPlay);
		}
	}

	public void setPlayBeforeCombat(int playerID, boolean isToPlay) {
		MWPlayer player = game.getPlayer(playerID);
		if (player != null) {
			player.setPlayBeforeCombat(isToPlay);
		}
	}

	public void cancelPlayingCard(int playerID) {
		SpellAbility sa = game.getCurrentSpellAbility();

		if (sa != null) {

			if (sa.getPlayerIdToAsk() == 0 && sa.getSourceCard().getControllerID() != playerID) {
				log.error("ERROR, player (id=" + playerID + ") can't cancel playing spell. Only controller can do this, id: " + sa.getSourceCard().getControllerID());
				return;
			}
			if (sa.getPlayerIdToAsk() != 0 && sa.getPlayerIdToAsk() != playerID) {
				log.error("ERROR, player (id=" + playerID + ") can't cancel playing spell. Expected id: " + sa.getPlayerIdToAsk() + ".");
				return;
			}
			
			sa.reset();
			game.getPlayer(playerID).getManaPool().addFromManaPool(game.getGlobalManaPool());
			game.setCurrentSpellAbility(null);
			game.setCurrentManaColor("");
			game.setCurrentManaCost(null);
			game.rememberSpellAbililty(null);
			//messages.publishMessage(MessageID.SM_MANAPOOL_CHANGED);

			sa.cancelCommand();
			gameState.rollback();
			
			checkChangesAndStateEffects();

			/**
			 * Check next spellability
			 */
			if (game.getStack().size() > 0) {
				bWaitingForAnswer = true;
				game.setWaitingPlayerID(opponentPlayer.getPlayerId());

			}
			checkStackForBeingEmpty(game.getStack().size());
		}
	}

	/**
	 * This method is used when damage is assigned manually: there are multi
	 * blockers and player divides attacking damage between them.
	 * 
	 * @param cardList
	 *            - list of blockers with assigned by attacking player damage
	 *            counters
	 * @see setAssignedDamageAuto
	 */
	public void setAssignedDamage(CardBeanList cardList) {

		game.verifyCreaturesInPlay();

		/**
		 * Assign
		 */
		CardList blockers = game.getCombat().getAllBlockers();

		for (int i = 0; i < cardList.size(); i++) {
			CardBean damagedBlocker = cardList.get(i);

			/**
			 * Find this blocker on the table
			 */
			if (!damagedBlocker.isPlayer()) {
				for (Card blocker : blockers) {
					if (blocker.getTableID() == damagedBlocker.getTableID()) {
						Damage d = damagedBlocker.getAssignedDamage();
						d.convertBeansToCards(game);
						blocker.setAssignedDamage(d);
					}
				}
			}
		}

		for (Combat combat : game.getPWCombat().values()) {
			blockers = combat.getAllBlockers();

			for (int i = 0; i < cardList.size(); i++) {
				CardBean damagedBlocker = cardList.get(i);

				/**
				 * Find this blocker on the table
				 */
				if (!damagedBlocker.isPlayer()) {
					for (Card blocker : blockers) {
						if (blocker.getTableID() == damagedBlocker.getTableID()) {
							Damage d = damagedBlocker.getAssignedDamage();
							d.convertBeansToCards(game);
							blocker.setAssignedDamage(d);
						}
					}
				}
			}
		}
	}

	/**
	 * Choose card. Unsafe without checking:
	 * sa.getSourceCard().getControllerID() == playerID
	 * 
	 * @param playerID
	 * @param cardBean
	 */
	public int addChosenCard(int playerID, CardBean cardBean) {
		SpellAbility sa = game.getCurrentSpellAbility();

		if (sa != null
		// && sa.getSourceCard().getControllerID() == playerID
		) {

			/**
			 * cardBean can be null in cases when player pressed Cancel in
			 * choice dialog
			 */
			if (cardBean == null) {
				log.info("User canceled to choose a card.");
				if (sa.getTargetCount() == 1 && sa.getChoiceCount() == 1) {
					sa.setTargetCard(null);
				} else {
					sa.addTargetCard(null);
				}
				sa.cancelCommand();
				acceptStack();
				return 0;
			}

			log.debug("Searching for card.");

			Card card = game.findCard(playerID, cardBean, GameZone.Hand);

			//FIXME: why not just use game.findCard
			if (card == null) {
				int oppID = game.getOpponentID(playerID);
				log.debug("Couldn't find the card. Will also search in opponent's hand.");
				card = game.findCard(oppID, cardBean, GameZone.Hand);
				if (card == null) {
					log.debug("Couldn't find the card. Will also search in player's library");
					card = game.findCard(playerID, cardBean, GameZone.Library);
					if (card == null) {
						log.debug("Couldn't find the card. Will also search in opponent's library");
						card = game.findCard(oppID, cardBean, GameZone.Library);
						if (card == null) {
							log.debug("Couldn't find the card. Will also search in player's graveyard");
							card = game.findCard(playerID, cardBean, GameZone.Graveyard);
							if (card == null) {
								log.debug("Couldn't find the card. Will also search in opponents's graveyard");
								card = game.findCard(oppID, cardBean, GameZone.Graveyard);
								if (card == null) {
									log.debug("Couldn't find the card. Will also search between cascade cards");
									card = game.findCard(oppID, cardBean, GameZone.Cascade);
									if (card == null) {
										// log.debug("Couldn't find the card. Will also search between cascade cards");
										// card = gameManager.findCard(oppID,
										// cardBean, GameZone.Copied);
										// if (card == null) {
										log.debug("Couldn't find the card. Will also search between player's cards in play");
										card = game.findCard(playerID, cardBean, GameZone.Battlefield);
										if (card == null) {
											log.debug("Couldn't find the card. Will also search between opponent's cards in play");
											card = game.findCard(oppID, cardBean, GameZone.Battlefield);
											if (card == null) {
												log.debug("Couldn't find the card. Exit");
												return 0;
											}
										}
										// }
									}
								}
							}
						}
					}
				}
			}

			log.debug("done");

			if (sa.getTargetCount() == 1 && sa.getChoiceCount() == 1) {
				sa.setTargetCard(card);
			} else {
				sa.addTargetCard(card);
				int countLeft = 0;
				if (sa.getTargetCount() > 1) {
					countLeft = sa.getTargetCount() - sa.getTargetCards().size();
				}
				if (sa.getChoiceCount() > 1) {
					countLeft = sa.getChoiceCount() - sa.getTargetCards().size();
				}
				boolean noCardToDiscard = false;
				if (sa.isNeedsDiscardCard()) {
					int pid = sa.getTargetPlayerID();
					int count = game.getPlayer(pid).getHand().size();
					noCardToDiscard = (count - sa.getTargetCards().size()) == 0;
				}
				if (countLeft > 0 && !noCardToDiscard) {
					return countLeft;
				}
			}

			/**
			 * The cards were chosen (or there is no card left), so we can
			 * continue playing the current ability by accepting current stack.
			 */
			if (sa.hasAspect(MagicWarsModel.ASPECT_NEW_CORE_TARGET)) {
				if (checkSpellIsPaid(sa, playerID, false, true) != 0) {
					return 0;
				}
				completePlayingSpellAbility(playerID);
			} else {
				final int pid = playerID;
				//final SpellAbility _sa = sa;
				Thread t = new Thread(new Runnable() {
					public void run() {
						/*if (_sa.getSourceCard().getName().equals("Sovereigns of Lost Alara")) {
							completePlayingSpellAbility(pid);
						} else {
							acceptStack();
						}*/
						// Search() doesn't work with acceptStack() that's why replacing it with completePlayingSpellAbility
						// Ondu Giant
						completePlayingSpellAbility(pid);
					}
				});
				t.start();
			}
		}

		return 0;
	}

	public int addChosenAbility(int playerID, SpellBean spellBean) {
		SpellAbility sa = game.getCurrentSpellAbility();

		if (sa != null) {

			/**
			 * spellBean can be null in cases when player pressed Cancel in
			 * choice dialog
			 */
			if (spellBean == null) {
				log.info("User canceled to choose a card.");
				if (sa.getTargetCount() == 1) {
					sa.setTargetAbility(null);
				} else {
					sa.addTargetAbility(null);
				}
				// acceptStack();
				completePlayingSpellAbility(playerID);
				return 0;
			}

			SpellAbility spell = null; // TODO
			log.debug("Searching for the spell ability in the stack.");
			MWStack stack = game.getStack();
			for (int i = 0; i < stack.size(); i++) {
				SpellAbility s = stack.peek(i);
				if (spellBean.equals(s)) {
					spell = s;
					break;
				}
			}
			if (spell == null) {
				log.debug("Couldn't find the card. Exiting.");
				return 0;
			}
			log.debug("done");

			if (sa.getTargetCount() == 1) {
				sa.setTargetAbility(spell);
			} else {
				sa.addTargetAbility(spell);
				int countLeft = sa.getTargetCount() - sa.getTargetAbilities().size();
				if (sa.getAbililtyChoice().size() - sa.getTargetAbilities().size() > 0) { // is
																							// there
																							// something
																							// else
																							// to
																							// choose
					return countLeft;
				}
			}

			AbilityList list = sa.getTargetAbilities();
			String s = "[";
			for (SpellAbility target : list) {
				s += target.getSourceCard();
			}
			s += "]";
			sa.setStackDescription(sa.getStackDescription() + " - targeting " + s);

			checkManaPool(playerID, sa);

			if (sa.getManaToPlay().equals("0")) {
				completePlayingSpellAbility(playerID);
			} else {
				messages.publishMessageForUser(MessageID.SM_PAYFOR_SPELL_ABILITY, playerID, sa);
			}
		}

		return 0;
	}

	public void addChosenAbilityToPlay(int playerID, SpellBean sb) {
		if (choices == null) {
			log.debug("choices wasn't set before");
		}
		SpellAbility sa = null;
		for (int i = 0; i < choices.length; i++) {
			if (sb.toString().equals(choices[i].toString())) {
				sa = choices[i];
			}
		}
		if (sa != null) {
			playSpellAbility(sa, playerID, true);
			choices = null;
		}
	}

	public void setChosenPermanents(int playerID, CardBeanList cardBeanList) {
		SpellAbility sa = game.getCurrentSpellAbility();

		CardList cardlist = game.findCards(playerID, cardBeanList, GameZone.Battlefield);

		if (cardlist.size() == 0) {
			log.debug("Couldn't find the target cards");
			int oppID = game.getOpponentID(playerID);
			log.debug("Couldn't find the cards. Will also search in player's library");
			cardlist = game.findCards(playerID, cardBeanList, GameZone.Library);
			if (cardlist.size() == 0) {
				log.debug("Couldn't find the cards. Will also search in opponent's library");
				cardlist = game.findCards(oppID, cardBeanList, GameZone.Library);
				if (cardlist.size() == 0) {
					log.debug("Couldn't find the cards. Exit");
					return;
				}
			}
		}

		sa.setTargetCards(cardlist);

		if (checkSpellIsPaid(sa, playerID, true, false) != 0) {
			return;
		}
		
		completePlayingSpellAbility(playerID);
	}

	public void setTargets(int playerID, CardBeanList cardBeanList) {
		SpellAbility sa = game.getCurrentSpellAbility();

		CardList cardlist = game.findCards(playerID, cardBeanList, GameZone.Battlefield);

		if (cardlist.size() == 0) {
			log.debug("Couldn't find the target cards");
			return;
		}

		sa.setTargetCards(cardlist);

		if (checkSpellIsPaid(sa, playerID, true, false) != 0) {
			return;
		}

		completePlayingSpellAbility(playerID);
	}

	private int checkSpellIsPaid(SpellAbility sa, int playerID, boolean checkDiscard, boolean checkAdditional) {
		CostList costToPlay = sa.getCost();
		Cost cost = costToPlay.getCurrentCost();
		if (cost instanceof ManaCost) {
			checkManaPool(playerID, sa);
		}

		if (!sa.getManaToPlay().equals("0")) {
			messages.publishMessageForUser(MessageID.SM_PAYFOR_SPELL_ABILITY, playerID, sa);
			return 1;
		}

		if (checkDiscard && sa.isNeedsDiscardCardToPay()) {
			sa.formPayChoice();
			messages.publishMessageForUser(MessageID.SM_CHOOSE_CARD_TO_DISCARD_PAY, playerID, sa);
			return 1;
		}

		if (checkAdditional && sa.isNeedsAdditionalCostToPlay()) {
			sa.formPayChoice();
			messages.publishMessageForUser(MessageID.SM_CHOOSE_TARGETS_AS_ADDITIONAL_COST, playerID, sa);
			return 1;
		}

		while (costToPlay.hasNextCost()) {
			cost = costToPlay.getNextCost();
			if (cost instanceof TapCost) {
				cost.pay(sa.getSourceCard());
				checkChangesAndStateEffects();
			} else if (cost instanceof SacrificeCost) {
				cost.pay(sa.getSourceCard());
				checkChangesAndStateEffects();
			} else if (cost instanceof DiscardCost) {
				sa.setChoiceDetailedDescription(sa.getSourceCard() + ": discard a card.");
				//sa.formPayChoice();
				sa.setChoice(game.getPlayer(sa.getSourceCard().getController()).getHandList());
				messages.publishMessageForUser(MessageID.SM_CHOOSE_CARD_TO_DISCARD_PAY, playerID, sa);
				return 1;
			} else if (cost instanceof ExileCost) {
				sa.setDiscardEqualToRemove(true); //TODO: rework this part
				sa.setChoiceDetailedDescription(sa.getSourceCard() + ": exile a card.");
				sa.setChoice(game.getPlayer(sa.getSourceCard().getController()).getHandList());
				messages.publishMessageForUser(MessageID.SM_CHOOSE_CARD_TO_DISCARD_PAY, playerID, sa);
				return 1;
			} else if (cost instanceof LoyaltyCost) {
				cost.pay(sa.getSourceCard());
				checkChangesAndStateEffects();
			} else if (cost instanceof RemoveCounterCost) {
				cost.pay(sa.getSourceCard());
				checkChangesAndStateEffects();
			} else if (cost instanceof AdditionalCost) {
				if (((AdditionalCost) cost).hasChoice()) {
					bSecondLoop = true;
					game.setWaitingPlayerID(playerWithPriority.getPlayerId());
					game.setCurrentCost(cost);

					sa.setChoice(((AdditionalCost) cost).getChoice());
					sa.setChoiceDescription(((AdditionalCost) cost).getDescription());
					messages.publishMessageForUser(MessageID.SM_CHOOSE_TARGETS_AS_ADDITIONAL_COST, playerID, sa);

					return 1;
				} else {
					cost.pay(sa.getSourceCard());
					checkChangesAndStateEffects();
				}
			} else if (cost instanceof PayLifeCost) {
				cost.pay(sa.getSourceCard());
				checkChangesAndStateEffects();
			}
		}

		if (checkMultikicker(sa, playerID)) {
			return 1;
		}

		return 0;
	}

	private boolean checkMultikicker(SpellAbility sa, final int playerID) {
		if (sa.hasAspect(MagicWarsModel.ASPECT_MULTIKICKER)) {
			try {
				sa.resolve();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				messages.publishException(e);
			}
			sa = game.getStack().pop();
		}

		if (sa.getMultikickerCost() != null) {
			final SpellAbility sourceSA = sa;
			Thread t = new Thread(new Runnable() {				
				public void run() {
					ArrayList<Serializable> questions = new ArrayList<Serializable>();
					questions.add("Pay multikicker: " + sourceSA.getMultikickerCost() + ".");
					if (!sourceSA.getSourceCard().hasAspect(MagicWarsModel.ASPECT_MULTIKICKER)) {
						questions.add("Don't pay multikicker.");
					} else {
						questions.add("Don't pay multikicker any more.");
					}
					questions.add("Cancel the spell.");
					String answer = (String) game.modal(playerID, "Pay multikicker", questions);
					if (answer.equals("Cancel the spell.")) {
						sourceSA.getSourceCard().removeAspect(MagicWarsModel.ASPECT_MULTIKICKER);
						cancelPlayingCard(playerID);
					} else if (answer.startsWith("Pay multikicker: ")) {
						final AbilityActivated multikicker = new AbilityActivated(sourceSA.getSourceCard(), sourceSA.getMultikickerCost()) {
							private static final long serialVersionUID = 1L;

							public void resolve() {
								Card c = getSourceCard();
								if (!c.hasAspect(MagicWarsModel.ASPECT_MULTIKICKER)) {
									c.addAspect(MagicWarsModel.ASPECT_MULTIKICKER, 1);
								} else {
									Integer value = (Integer) c.getAspectValue(MagicWarsModel.ASPECT_MULTIKICKER);
									c.updateAspectValue(MagicWarsModel.ASPECT_MULTIKICKER, value + 1);
								}
							}
						};
						multikicker.setMultikickerCost(sourceSA.getMultikickerCost());
						game.setCurrentSpellAbility(multikicker);
						game.getStack().add(sourceSA);
						multikicker.addAspect(MagicWarsModel.ASPECT_MULTIKICKER);
						checkSpellIsPaid(multikicker, playerID, false, false);
					} else {
						game.setCurrentSpellAbility(sourceSA);
						completePlayingSpellAbility(playerID);
					}
				}
			});
			t.start();

			return true;
		}
		return false;
	}

	public void addChatMessage(int playerID, String text) {
		messages.publishMessageForUser(MessageID.SM_CHAT_MESSAGE, game.getOpponentID(playerID), text);
	}

	public int getOpponentHandSize(int playerID) {
		int pid = game.getOpponentID(playerID);

		return game.getPlayer(pid).getHand().size();
	}

	/**
	 * For Admin Tool
	 * 
	 * @param tableID
	 */
	public void destroyPermanent(int tableID) {
		Card c = game.getBattlefield().getPermanent(tableID);
		game.destroyNoRegeneration(c);

		//messages.publishMessage(MessageID.SM_TABLE_CHANGED);
		checkChangesAndStateEffects();
	}

	public void addCounter(int playerID, int tableID, int nCounter) {
		Card c = game.getBattlefield().getPermanent(tableID);
		c.putCounter(CounterType.P1P1, nCounter);

		checkChangesAndStateEffects();
		//messages.publishMessage(MessageID.SM_TABLE_CHANGED);
	}

	/**
	 * For Admin Tool
	 * 
	 * @param nPhase
	 */
	public void setPhase(PhaseName phase) {
		game.setCurrentPhase(phase);
		messages.publishMessage(MessageID.SM_PHASE_CHANGED);
	}

	/**
	 * Apply chosen mana color or mana symbol.
	 * 
	 * @param playerID player id
	 * @param manaSymbol
	 *            chosen mana color or mana symbol
	 * 
	 *            If manaSymbol doesn't contain "\\" so it's not hybrid mana and
	 *            we have chosen what mana symbol to use not what mana symbol to
	 *            pay for.
	 * 
	 *            Example: if manaSymbol is "B\\R" then we pay
	 *            gameManager.getCurrentManaColor() for B\\R and if manaSymbol
	 *            is "B" then we use "B" to pay for
	 *            gameManager.getCurrentManaCost()
	 * 
	 * @return changed manacost null if there is no current manacost to pay for
	 */
	public ManaCost setChosenManaSymbol(int playerID, String manaSymbol) {

		if (game.getCurrentSpellAbility() == null) {
			game.getPlayer(playerID).getManaPool().add(manaSymbol);
			//messages.publishMessage(MessageID.SM_MANAPOOL_CHANGED);
			checkChangesAndStateEffects();
			return null;
		}

		String color = game.getCurrentManaColor();
		ManaCost spellManaCost = game.getCurrentManaCost();
		if (spellManaCost == null) {
			game.getPlayer(playerID).getManaPool().add(manaSymbol);
			//messages.publishMessage(MessageID.SM_MANAPOOL_CHANGED);
			checkChangesAndStateEffects();
			completePlayingSpellAbility(playerID);
			return null;
		}

		ManaCost manaCostNew = null;

		/**
		 * Check hybrid
		 */
		if (!manaSymbol.contains("\\")) {
			color = manaSymbol;
			manaCostNew = spendManaForSpell(playerID, color, spellManaCost);
		} else {
			manaCostNew = PayManaCostUtil.spendManaSymbol(manaSymbol, color, spellManaCost);
		}

		if (manaCostNew.isPaid()) {
			completePlayingSpellAbility(playerID);
		} else {
			game.setCurrentManaCost(manaCostNew);
		}

		return manaCostNew;
	}

	public void adminSetLife(int playerID, int nLife) {
		game.getPlayer(playerID).setLifeCount(nLife);
		//messages.publishMessage(MessageID.SM_LIFE_CHANGED);
	}

	public void adminClearStack() {
		game.getStack().clear();
		messages.publishMessage(MessageID.SM_STACK_EMPTY);
	}

	public int getGraveCount(int playerID) {
		return game.getGraveyard().getPersonalCards(playerID).size();
	}

	public int getDeckCardCount(int playerID) {
		return game.getPlayerById(playerID).getLibrary().size();
	}

	public void saveCurrentGame(int playerID) {
		MWGameSnapshot save = new MWGameSnapshot(game, playerID);
		save.createSnapshot(game);
		save.saveSnapshot();

		messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, playerID, "game has been saved");
		messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, game.getOpponentID(playerID), "game has been saved");
	}

	public void loadGame(int playerID) {
		MWGameSnapshot load = new MWGameSnapshot(game, playerID);
		load.loadSnapshot();
		game = load.restoreGame(game);
		messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, playerID, "game has been loaded");
		messages.publishSystemMessageForUser(MessageID.SM_CHAT_MESSAGE, game.getOpponentID(playerID), "game has been loaded");
	}

	public void setChosenDeck(int playerId, String deckName) {
		game.getPlayer(playerId).setDeckName(deckName);
	}

	public void initialize() {
		try {
			game.getPlayer1().prepareDeckAndDraw7();
		} catch (Exception e) {
			messages.publishException(e);
		}
		try {
			game.getPlayer2().prepareDeckAndDraw7();
		} catch (Exception e) {
			messages.publishException(e);
		}

		/**
		 * Replace cards in the hand
		 */
		try {
			game.updateHand(pid1);
		} catch (Exception e) {
			messages.publishException(e);
		}
		try {
			game.updateHand(pid2);
		} catch (Exception e) {
			messages.publishException(e);
		}

		/**
		 * Put permanents into game before it starts
		 */
		try {
			game.giveOdds(pid1);
		} catch (Exception e) {
			messages.publishException(e);
		}
		try { 
			game.giveOdds(pid2);
		} catch (Exception e) {
			messages.publishException(e);
		}

		game.checkChangesAndStateEffects();
		
		/**
		 * Stack should be cleared because some cards put "comes into play"
		 * abilities into the stack when comes into play that should not happen
		 * in odds mode
		 */
		game.getStack().clear();

		nStartGame = 0;
	}

	public ArrayList<CardBean> getGraveyard(int playerID) {
		return game.getGraveyard().getPersonalCardBeans(playerID);
	}

	public ArrayList<DeckInfo> getListOfDecks(int playerID, DeckType type) {
		return game.getPlayerById(playerID).getListOfDecks(type);
	}

	public ZippedObject<Deck> findDeck(int playerID, String name) {
		return new ZippedObjectImpl<Deck>(game.getPlayerById(playerID).findDeck(name));
	}

	public void quitTheGame() {
		game.setGameActive(false);
	}

	public WinLose getWinLose(int playerID) {
		MWPlayer p = game.getPlayer(playerID);
		if (p != null) {
			return p.getWinLoseInfo();
		}
		return null;
	}

	public void setWinLose(int playerID, WinLose winLoseInfo) {
		MWPlayer p = game.getPlayer(playerID);
		if (p != null) {
			p.setWinLoseInfo(winLoseInfo);
		}
	}

	public void cancelChoosingSpecific(int playerID) {
		SpellAbility sa = game.getCurrentSpellAbility();

		if (sa != null && (sa.getSourceCard().getControllerID() == playerID || sa.getPlayerIdToAsk() == playerID)) {
			if (sa.isCancelStopsPlaying()) {
				/*
				 * SpellAbility inStack = gameManager.getStack().peek(); if
				 * (inStack.equals(sa)) { gameManager.getStack().pop(); }
				 */
				sa.reset();
				game.getPlayer(playerID).getManaPool().addFromManaPool(game.getGlobalManaPool());
				game.setCurrentSpellAbility(null);
				game.setCurrentManaColor("");
				game.setCurrentManaCost(null);

				if (game.getCascadeCards().size() > 0) {
					Card card = game.getCascadeCards().get(0);
					if (card.equals(sa.getSourceCard())) {
						game.getCascadeCards().remove(0);
					}
				}
				
				//FIXME: remove from the stack
				boolean removed = game.getStack().remove(sa);
				if (!removed) log.error("cancelChoosingSpecific: stack is empty.");

				checkChangesAndStateEffects();
				
				/**
				 * Check next spellability
				 */
				if (game.getStack().size() > 0) {
					bWaitingForAnswer = true;
					game.setWaitingPlayerID(opponentPlayer.getPlayerId());
				}
				checkStackForBeingEmpty(game.getStack().size());
			} else {
				
				// fix: devour doesn't work
				game.getStack().remove(sa);
				checkChangesAndStateEffects();
				
				/**
				 * Continue playing E.g. after further devour was canceled
				 */
				if (game.getStack().size() > 0) {
					// gameManager.setWaitingPlayerID(playerID);
					bWaitingForAnswer = true;
					checkStackForBeingEmpty(0);
				}/* else if (game.isCurrentPhase(PhaseName.upkeep)) {
					bWaitingForAnswer = false;
				}*/
			}
		} else if (sa != null) {
			//messages.publishMessageForUser(MessageID.SM_STACK_CHANGED, sa.getSourceCard().getControllerID());
			if (sa.getPlayerIdToAsk() == 0) {
				messages.publishMessageForUser(MWMessage.MessageID.SM_STACK_CHANGED, sa.getSourceCard().getControllerID());
			} else {
				messages.publishMessageForUser(MWMessage.MessageID.SM_STACK_CHANGED, sa.getPlayerIdToAsk());
			}
		}

	}

	public void addChosenTargetsForPay(int playerID, CardBeanList cardBeanList) {
		SpellAbility sa = game.getCurrentSpellAbility();

		if (sa.getSourceCard().getControllerID() == playerID) {

			CardList cardlist = game.findCards(playerID, cardBeanList, GameZone.Battlefield);

			if (!bSecondLoop) { // old behaviour
				game.sacrifice(cardlist.get(0));
			} else {
				Cost cost = game.getCurrentCost();
				if (cost != null && cost instanceof AdditionalCost) {
					AdditionalCost _cost = (AdditionalCost) cost;
					if (cardlist.size() == 1) {
						_cost.setChosen(cardlist.get(0));
					} else {
						_cost.setChosen(cardlist);
					}
					_cost.pay(sa.getSourceCard());
				} else {
					//FIXME: sure we need this? we have SacrificeCost for that now
					game.sacrifice(cardlist.get(0));
				}
			}

			checkChangesAndStateEffects();
			
			completePlayingSpellAbility(playerID);
		}
	}

	/**
	 * Empty revealed card list every second request
	 */
	//private int requestForRevealed = 0;

	public ZippedObjectImpl<CardBeanList> getRevealedCards(int playerID) {
		Map<Card, ArrayList<Card>> revealedCards = game.getRevealedCards();
		CardBeanList revealedBeans = new CardBeanList();
		if (revealedCards == null) return new ZippedObjectImpl<CardBeanList>(revealedBeans);
		
		for (Card source : revealedCards.keySet()) {
			String key = source.getName() + "#" + source.getUniqueNumber();
			for (Card card : revealedCards.get(source)) {
				CardBean cardBean = new CardBean(card);
				cardBean.setAspectValue(MagicWarsModel.ASPECT_REVEAL_SOURCE, key);
				revealedBeans.add(cardBean);
			}
		}
		
		return new ZippedObjectImpl<CardBeanList>(revealedBeans);
	}

	public ZippedObjectImpl<CardBeanList> getViewedCards(int playerID) {
		ArrayList<Card> viewedCards = game.getViewedCards();
		CardBeanList viewedBeans = new CardBeanList();

		for (int i = 0; i < viewedCards.size(); i++) {
			viewedBeans.add(new CardBean(viewedCards.get(i)));
		}

		game.resetViewedCards();

		return new ZippedObjectImpl<CardBeanList>(viewedBeans);
	}

	public ZippedObjectImpl<CardBeanList> getRevealedCardsToPlay(int playerID) {
		ArrayList<Card> revealedCards = game.getRevealedCardsToPlay();
		CardBeanList revealedBeans = new CardBeanList();

		for (int i = 0; i < revealedCards.size(); i++) {
			revealedBeans.add(new CardBean(revealedCards.get(i)));
		}

		// revealedCards.clear();

		return new ZippedObjectImpl<CardBeanList>(revealedBeans);
	}

	public void answerIsNo(int playerID) {
		SpellAbility sa = game.getStack().pop();

		int pid = sa.getPlayerIdToAsk();
		if (pid == 0) {
			pid = sa.getSourceCard().getControllerID();
		}

		if (sa != null && pid == playerID) {

			int countBeforeCancel = game.getStack().size();

			sa.cancelCommand();
			checkChangesAndStateEffects();

			game.setCurrentSpellAbility(null);
			/**
			 * Check next spellability
			 */
			if (game.getStack().size() > 0) {
				bWaitingForAnswer = true;
				game.setWaitingPlayerID(opponentPlayer.getPlayerId());
			}

			checkStackForBeingEmpty(countBeforeCancel);
		}
	}

	public void answerIsYes(int playerID) {
		SpellAbility sa = game.getStack().pop();
		sa.removeAspect(MagicWarsModel.ASPECT_NEW_SPELL_ABILITY);

		int pid = sa.getPlayerIdToAsk();
		if (pid == 0) {
			pid = sa.getSourceCard().getControllerID();
		}

		if (sa != null && pid == playerID) {
			playSpellAbility(sa, playerID, false);
		}
	}

	public void undo() {
	};

	public ZippedObjectImpl<CardBean> getCardToPlay(int playerId) {
		Card card = MWAIHelper.getCardToPlay(playerId, game);
		if (card != null) {
			return new ZippedObjectImpl<CardBean>(new CardBean(card));
		}
		return null;
	}

	public void setSideboarded(int playerID, ZippedObject<SideboardInfo> sideBoarded) {
		MWPlayer player = game.getPlayer(playerID);
		player.setSideBoarded(sideBoarded.unzip());
	}

	public void setSideboarded(int playerID, SideboardInfo sideBoarded) {
		MWPlayer player = game.getPlayer(playerID);
		player.setSideBoarded(sideBoarded);
	}

	public SideboardInfo getSideboarded(int playerID) {
		MWPlayer player = game.getPlayer(playerID);
		return player.getSideBoarded();
	}

	public void test(int playerID) {
		//messages.publishMessageForUser(MessageID.SM_LIFE_CHANGED, playerID);
		//messages.publishMessageForUser(MessageID.SM_LIFE_CHANGED, playerID);
	}
	
	public void payAIForCurrentSA(int playerId) {
		SpellAbility spellAbility = game.getCurrentSpellAbility();
		if (spellAbility == null) {
			log.log(ComputerLevel.COMPUTER, "no spell ability to pay mana for.");
			return;
		}
		
		ManaCost spellManaCost = new ManaCost(spellAbility.getManaToPlay());
		for (Card c : game.getBattlefield().getPermanentList(playerId)) {
			if (c.isUntapped() && c.isLand()) {
				log.log(ComputerLevel.COMPUTER, "tapping " + c);
				spellManaCost = tapPermanentForMana(playerId, c.getTableID(), spellManaCost);
				if (spellManaCost.isPaid()) {
					log.log(ComputerLevel.COMPUTER, "{Paid}");
					break;
				}
			} else {
				log.log(ComputerLevel.COMPUTER, "Skipped : " + c + ". isLand="+c.isLand()+",tapped="+c.isTapped());
			}
		}
	}

	public void attackByAI(int playerId) {
		if (playerId == game.getWaitingPlayerID()) {
			for (Card c : game.getBattlefield().getPermanentList(playerId)) {
				if (CombatUtil.canAttack(c) && c.getControllerID() == playerId) {
					log.log(ComputerLevel.COMPUTER, "Attack with " + c);
					addAttacker(playerId, c.getTableID(), GameZone.Battlefield, "Player");
				} else {
					log.log(ComputerLevel.COMPUTER, c + " can't attack. canAttack="+CombatUtil.canAttack(c));
					log.log(ComputerLevel.COMPUTER, !c.isCreature() + "," + c.isTapped() + "," + c.hasSickness() + "," + c.getKeyword().contains("Defender") + "," + c.getKeyword().contains("This creature cannot attack"));
				}
			}
			next(playerId, game.getCurrentPhaseName());
		}
	}
	
	public void concedeTheGame(int playerId) {
		MWPlayer player = game.getPlayer(playerId);
		if (player != null) {
			player.setHaveLostForSomeReason(true);
			game.addSystemMessage(game.getNicknameById(playerId) + " conceded the game.");
			game.checkChangesAndStateEffects();
		}
	}
	
	public GameManager getGame() {
		return game;
	}
}

