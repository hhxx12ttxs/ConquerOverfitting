package mw.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.Map.Entry;

import mw.mtgforge.Constant;
import mw.mtgforge.MyRandom;
import mw.server.attackeffect.AttackEachTurnEffect;
import mw.server.blockeffect.BlockIfAbleEffect;
import mw.server.card.CardFactory;
import mw.server.core.MWGameThread;
import mw.server.core.edit.GameState;
import mw.server.event.EventManager;
import mw.server.event.EventParam;
import mw.server.inet.MWGameStateObserver;
import mw.server.list.CardBeanList;
import mw.server.list.CardList;
import mw.server.list.CardListFilter;
import mw.server.model.Card;
import mw.server.model.CardProxy;
import mw.server.model.Combat;
import mw.server.model.Constraint;
import mw.server.model.CounterType;
import mw.server.model.Damage;
import mw.server.model.MagicWarsModel;
import mw.server.model.ManaPool;
import mw.server.model.SpellAbility;
import mw.server.model.MagicWarsModel.CardOrder;
import mw.server.model.MagicWarsModel.CardSuperType;
import mw.server.model.MagicWarsModel.CardType;
import mw.server.model.MagicWarsModel.Color;
import mw.server.model.MagicWarsModel.GameZone;
import mw.server.model.MagicWarsModel.PhaseName;
import mw.server.model.ability.AbilityActivated;
import mw.server.model.ability.AbilityTriggered;
import mw.server.model.bean.CardBean;
import mw.server.model.bean.CombatBean;
import mw.server.model.cost.Cost;
import mw.server.model.cost.ManaCost;
import mw.server.model.effect.ContiniousEffect;
import mw.server.model.effect.Duration;
import mw.server.model.effect.GlobalEffect;
import mw.server.model.effect.ManaCostEffect;
import mw.server.model.spell.Madness;
import mw.server.model.zone.Hand;
import mw.server.model.zone.Library;
import mw.server.model.zone.PlayerZone;
import mw.server.pattern.Command;
import mw.server.phases.EndOfCombat;
import mw.server.phases.EndOfTurn;
import mw.server.phases.Upkeep;
import mw.settings.RuleSettingsManager;

import org.apache.log4j.Logger;

/**
 * Class for most game operation (game engine facade).
 */
public class GameManager implements /*Game, */Serializable {

	private final Logger log = Logger.getLogger(GameManager.class);
	private static final long serialVersionUID = -2775162914691980093L;
	
	public GameManager() {
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values())
			effects.put(layer, new ArrayList<ContiniousEffect>());
	}
	
	public GameManager getManager() {
		return this;
	}

	public Integer getUniqueTableID() {
		uniqueTableID++;
		return uniqueTableID;
	}

	public Integer getUniqueCardID() {
		uniqueCardID++;
		return uniqueCardID;
	}

	public int getPriorityPID() {
		return priorityPID;
	}

	public void givePriorityToPlayer(int id) {
		priorityPID = id;
	}

	public int getOpponentID(int playerID) {
		if (player1.getPlayerId() == playerID) {
			return player2.getPlayerId();
		} else {
			return player1.getPlayerId();
		}
	}

	public MWPlayer getOpponentById(int playerID) {
		return getPlayerById(getOpponentID(playerID));
	}

	/**
	 * Return spell ability that we try to play.
	 * 
	 * @return current spell ability
	 */
	public SpellAbility getCurrentSpellAbility() {
		return currentSpellAbiility;
	}

	/**
	 * Save spell ability that we are trying to play.
	 * 
	 */
	public void setCurrentSpellAbility(SpellAbility sa) {
		currentSpellAbiility = sa;
	}

	public HashMap<Integer, CardBean> getTableBeans() {
		return battlefield.getTableBeans();
	}

	public MWBattlefield getBattlefield() {
		return battlefield;
	}

	public void clearBattlefield() {
		battlefield.clear();
	}

	public void clearGraveyard() {
		graveyard.clear();
	}

	public void resetCombat() {
		CardList attackerList = combat.getAttackersList();
		for (Card attacker : attackerList) {
			attacker.setAttacking(false);
			for (Card blocker : combat.getBlockers(attacker)) {
				blocker.setBlocking(false);
			}
		}
		for (Combat pwCombat : pwCombatMap.values()) {
			attackerList = pwCombat.getAttackersList();
			for (Card attacker : attackerList) {
				attacker.setAttacking(false);
				for (Card blocker : pwCombat.getBlockers(attacker)) {
					blocker.setBlocking(false);
				}
			}
		}
		combat.reset();
		pwCombatMap.clear();
	}

	public Combat getCombat() {
		return combat;
	}

	public Combat getCombat(int id) {
		Integer pwid = Integer.valueOf(id);
		Combat pwCombat = pwCombatMap.get(pwid);
		if (pwCombat == null) {
			pwCombat = new Combat();
			pwCombatMap.put(pwid, pwCombat);
			Card card = battlefield.getPermanent(id);
			pwCombat.setPlaneswalker(card);
		}
		return pwCombat;
	}

	public ArrayList<Combat> getCombats() {
		ArrayList<Combat> combats = new ArrayList<Combat>();
		combats.add(combat);
		for (Combat c : pwCombatMap.values()) {
			combats.add(c);
		}
		return combats;
	}

	public void updateCombat(CombatBean combatBean) {
		combat.updateCombat(this, combatBean);
	}

	/**
	 * Damage defending player and his planeswalker (also count as players). Then deal damage to attacking and blocking creatures according
	 * to assigned damage.
	 * 
	 * @param attPID
	 *            attacking player id
	 * @param defPID
	 *            defending player id
	 */
	public void damageCreaturesAndPlayers(int attPID, int defPID) {

		if (isPreventCombatDamageThisTurn()) {
			addSystemMessage("Combat damage has been prevented.");
			return;
		}

		MWPlayer attackingPlayer = getPlayerById(attPID);
		MWPlayer defendingPlayer = getPlayerById(defPID);

		/**
		 * Damage Players
		 */
		HashMap<Card, Integer> sources = combat.getDefendingDamage().getSources();
		for (Card damageDealer : sources.keySet()) {
			Integer damage = sources.get(damageDealer);
			if (damage > 0) {
				dealCombatDamageToThePlayer(defendingPlayer.getPlayerId(), damage, damageDealer);
			}
		}
		sources.clear();

		sources = combat.getAttackingDamage().getSources();
		for (Card damageDealer : sources.keySet()) {
			Integer damage = sources.get(damageDealer);
			if (damage > 0) {
				dealCombatDamageToThePlayer(attackingPlayer.getPlayerId(), damage, damageDealer);
			}
		}
		sources.clear();

		damageCreaturesInCombat(combat);

		/**
		 * Damage Plainswalkers
		 */
		for (Entry<Integer, Combat> pwCombatEntry : pwCombatMap.entrySet()) {
			Combat pwCombat = pwCombatEntry.getValue();
			Card card = pwCombat.getPlaneswalker();
			if (battlefield.isCardInPlay(card.getTableID())) {
				HashMap<Card, Integer> pwSources = card.getAssignedDamage().getSources();
				for (Entry<Card, Integer> source : pwSources.entrySet()) {
					Integer damage = source.getValue();
					addCombatDamage(card, damage, source.getKey());
				}
				pwSources.clear();
			}
			damageCreaturesInCombat(pwCombat);
		}

	}

	private void damageCreaturesInCombat(Combat combat) {
		damageCreatures(combat.getAttackersList());
		damageCreatures(combat.getAllBlockers());
	}

	private void damageCreatures(CardList creatures) {
		for (Card creature : creatures) {
			Damage d = creature.getAssignedDamage();
			d.convertBeansToCards(this);

			for (Entry<Card, Integer> source : d.getSources().entrySet()) {
				Card damageDealer = source.getKey();

				if (isNeedFirstStrikeCombat && !damageDealer.hasKeyword(MagicWarsModel.KEYWORD_FIRST_STRIKE_SA)
						&& !damageDealer.hasKeyword(MagicWarsModel.KEYWORD_DOUBLE_STRIKE_SA)) {
					continue;
				}
				if (!isNeedFirstStrikeCombat && damageDealer.hasKeyword(MagicWarsModel.KEYWORD_FIRST_STRIKE_SA)
						&& !damageDealer.hasKeyword(MagicWarsModel.KEYWORD_DOUBLE_STRIKE_SA)) {
					continue;
				}

				Integer damage = source.getValue();
				addCombatDamage(creature, damage, damageDealer);
			}

			d.setDamage(0);
			d.setWither(0);
		}
	}

	public void checkChangesAndStateEffects() {
		int size = getUpdateAtomsSize();
		int prevSize;
		int limit = 5;
		do {
			prevSize = size;
			checkStateEffects();
			createUpdateAtoms();
			size = getUpdateAtomsSize();
			limit--;
		} while (size > prevSize && limit > 0);
		if (limit == 0) {
			log.error("The update limit has been exceeded. May happen because of error in some script.");
		}
	}

	public void createUpdateAtoms() {
		gameStateObserver.checkForUpdates();
	}

	public void checkStateEffects() {
		if (!gameActive) {
			return;
		}

		boolean stop = checkPlayers();

		if (stop) {
			log.info("Game has been finished.");
			setGameActive(false);
		}

		refreshEffects();
		checkAsLongAs();
		checkCreatures();
		checkEquipments();
		checkAuras();

		destroyLegendaryCreatures();
		destroyPlaneswalkers();
	}

	private boolean checkPlayers() {
		boolean stop = false;
		boolean won1 = player2.getLifeCount() <= 0 || player1.haveWonForSomeReason() || player2.haveLostForSomeReason();
		boolean won2 = player1.getLifeCount() <= 0 || player2.haveWonForSomeReason() || player1.haveLostForSomeReason();

		won1 &= player1.isCanWin() && player2.isCanLose();
		won2 &= player2.isCanWin() && player1.isCanLose();

		if (won1) {
			player1.getWinLoseInfo().addWin();
			player2.getWinLoseInfo().addLose();
			stop = true;
		}
		if (won2) {
			player1.getWinLoseInfo().addLose();
			player2.getWinLoseInfo().addWin();
			stop = true;
		}
		if (won1 && won2) {
			player1.getWinLoseInfo().addWin();
			player2.getWinLoseInfo().addWin();
		}
		if (won1 || won2) {
			player1.resetAll();
			player2.resetAll();
		}

		return stop;
	}

	private void checkCreatures() {
		for (Card creature : battlefield.getAllCreatures()) {
			if (creature.getDefense() <= creature.getDamage()) {
				destroy(creature);
			}
		}
	}

	/**
	 * Make sure all equipments stop equipping previously equipped creatures that have left play.
	 */
	private void checkEquipments() {
		CardList equipments = battlefield.getAllPermanents().getType("Equipment");

		for (Card equipment : equipments) {
			if (equipment.isEquipping()) {
				Card equippedCreature = equipment.getEquipping().get(0);
				if (!getBattlefield().isCardInPlay(equippedCreature)) {
					unequipCreature(equippedCreature, equipment);
				}
			}
		}
	}

	/**
	 * Make sure all auras stop enchanting previously enchanted creatures that have left play.
	 */
	private void checkAuras() {
		CardList enchantPermanents = getBattlefield().getAllPermanents().getType("Aura");

		for (Card enchantment : enchantPermanents) {
			if (enchantment.isEnchanting()) {
				Card enchantedPermanent = enchantment.getEnchanting().get(0);
				if (!getBattlefield().isCardInPlay(enchantedPermanent)) {
					getBattlefield().unEnchantPermanent(enchantedPermanent, enchantment);
				}
			} else if (enchantment.hasAspect(MagicWarsModel.ASPECT_NOT_ATTACHED_AURA)) {
				if (enchantment.getSpellAbilities().size() > 0) {
					final SpellAbility aura = enchantment.getSpellAbilities().get(0).getCopy();
					AbilityTriggered ability = new AbilityTriggered(enchantment) {
						private static final long serialVersionUID = 7099247206283893421L;
						@Override
						public void resolve() {
							aura.setTargetCards(getTargetCards());
							aura.resolve();
							aura.getSourceCard().removeAspect(MagicWarsModel.ASPECT_ATTACHING_AURA);
						}
					};
					ability.setNeedsSpecificTarget(true);
					if (aura.isNeedsTargetCreature()) {
						ability.setChoiceCommand(new ChoiceCommand() {
							private static final long serialVersionUID = 7013490656575963867L;
							public void execute() {
								setInputChoice(getBattlefield().getAllCreatures());
							}
						});
						ability.setChooseTargetDescription("Choose creature to attach aura");
					} else {
						ability.setChoiceCommand(ability.getChoiceCommand());
						ability.setChoiceDescription(aura.getChoiceDescription());
					}
					ability.setInvisible(true);
					ability.addAspect(MagicWarsModel.ASPECT_CHOOSING_NOT_TARGETING);
					stack.add(ability);
					enchantment.removeAspect(MagicWarsModel.ASPECT_NOT_ATTACHED_AURA);
					enchantment.addAspect(MagicWarsModel.ASPECT_ATTACHING_AURA);
				}
			} else if (enchantment.hasAspect(MagicWarsModel.ASPECT_ATTACHING_AURA)) {
				// do nothing
			} else {
				sacrificeDestroy(enchantment);
			}
		}
	}

	private void checkAsLongAs() {
		for (Card card : graveyard.getAllCards()) {
			Command aslongas = card.getAsLongAsCommand();
			if (aslongas != null) {
				aslongas.setParam(GameZone.Graveyard);
				aslongas.execute();
			}
		}

		for (Card card : exiled.getAllCards()) {
			Command aslongas = card.getAsLongAsCommand();
			if (aslongas != null) {
				aslongas.setParam(GameZone.Exile);
				aslongas.execute();
			}
		}

		for (Card card : battlefield.getAllPermanents()) {
			Command aslongas = card.getAsLongAsCommand();
			if (aslongas != null) {
				aslongas.setParam(GameZone.Battlefield);
				aslongas.execute();
			}
		}
	}
	
	public void refreshEffects() {
		Map<Card, Integer> controllers = findControllers();
		
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values()) {							
			for (ContiniousEffect effect : effects.get(layer))
				effect.discardEffect();			
		}				
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values()) {
			for (ContiniousEffect effect : effects.get(layer))
				effect.applyEffect();			
		}
		
		removeContiniousEffects(findChangedControllers(controllers), Duration.WHILE_UNDER_CONTROL);
	}
	
	private Map<Card, Integer> findControllers() {
		Map<Card, Integer> controllers = new HashMap<Card, Integer>();
		for (Entry<Integer,Card> entry : getBattlefield().getBattlefieldLink().entrySet()) {
			controllers.put(entry.getValue(), Integer.valueOf(entry.getValue().getController()));
		}
		return controllers;
	}
	
	private List<Card> findChangedControllers(Map<Card, Integer> controllers) {
		List<Card> cardsThatChangedController = new ArrayList<Card>();
		for (Entry<Integer,Card> entry : getBattlefield().getBattlefieldLink().entrySet()) {
			if (entry.getValue().getController() != controllers.get(entry.getValue())) {
				cardsThatChangedController.add(entry.getValue());
			}
		}
		return cardsThatChangedController;
	}
	
	//TODO: write junit test
	private void destroyPlaneswalkers() {
		if (!getRules(player1.getPlayerId()).planeswalkerUniqueness())
			return;

		CardList plainswalkers = getBattlefield().getAllPermanents();
		plainswalkers = plainswalkers.getCardType(CardType.Planeswalker);

		for (Card p : plainswalkers) {
			if (p.getLoyaltyCounters() <= 0) {
				sacrificeDestroy(p);
			}
		}

		for (Card p1 : plainswalkers) {
			for (Card p2 : plainswalkers) {
				if (!p1.equals(p2)) {
					ArrayList<String> subtypes = p1.getSubType();
					for (String subtype : subtypes) {
						if (p2.subType(subtype)) {
							sacrificeDestroy(p1);
							sacrificeDestroy(p2);
						}
					}
				}
			}
		}
	}

	private void destroyLegendaryCreatures() {

		CardList legends = getBattlefield().getAllPermanents();
		legends = legends.getSuperType(CardSuperType.Legendary);

		while (!legends.isEmpty()) {

			final Card legend = legends.get(0);
			CardList b = legends.filter(new CardListFilter() {
				public boolean addCard(Card c) {
					return c.getName().equals(legend.getName());
				}
			});

			legends.remove(0);
			if (b.size() > 1) {
				for (int i = 0; i < b.size(); i++) {
					destroy(b.get(i));
				}
			}
		}
	}

	public void checkDamagePlayerEffects() {
		combat.executeDealDamagePlayer(this);
	}

	public boolean destroyTarget(Card target, Card source) {
		if (source != null) {
			if (checkProtection(target, source)) {
				addSystemMessage("{" + target + "} wasn't destroyed as it has protection from {" + source + "}");
				return false;
			}
		}
		destroy(target);
		return !getBattlefield().isCardInPlay(target);
	}

	public void destroy(Card c) {

		if (!battlefield.isCardInPlay(c.getTableID()) || c.getKeyword().contains(MagicWarsModel.KEYWORD_INDESTRUCTIBLE_SA)) {
			return;
		}

		if (c.getShield() > 0) {
			c.subtractShield();
			CardProxy.resetDamage(c);
			addSystemMessage("{" + c + "} was regenerated.");
			return;
		}
		this.sacrificeDestroy(c);
	}

	public boolean destroyTargetNoRegeneration(Card target, Card source) {
		log.info("destroyTargetNoRegeneration():" + target);
		if (!getBattlefield().isCardInPlay(target)) {
			return false;
		}

		if (source != null) {
			if (checkProtection(target, source)) {
				addSystemMessage("{" + target + "} wasn't destroyed as it has protection from {" + source + "}");
				return false;
			}
		}
		destroyNoRegeneration(target);
		return getBattlefield().isCardInPlay(target);
	}

	public boolean exileTarget(Card target, Card source) {
		if (source != null) {
			if (checkProtection(target, source)) {
				addSystemMessage("{" + target + "} wasn't exiled as it has protection from {" + source + "}");
				return false;
			}
		}
		moveToZone(new PlayerZone(GameZone.Battlefield, target.getControllerID()), new PlayerZone(GameZone.Exile, target.getOwnerID()),
				target);
		return true;
	}

	public void destroyNoRegeneration(Card c) {
		if (!battlefield.isCardInPlay(c.getTableID()) || c.getKeyword().contains(MagicWarsModel.KEYWORD_INDESTRUCTIBLE_SA)) {
			return;
		}

		sacrificeDestroy(c);
	}

	public void sacrifice(Card c) {
		if (getBattlefield().isCardInPlay(c)) {
			sacrificeDestroy(c);
			if (!c.getName().equals("AI_PET")) { // reserved for AI
				addSystemMessage(getNicknameById(c.getControllerID()) + " sacrificed {" + c + "}");
				if (c.isCreature()) {
					this.eventManager.getSacrificeCreatureEvent().notifyObservers(c);
				}
			}
		}
	}

	private void sacrificeDestroy(final Card c) {
		if (!battlefield.isCardInPlay(c.getTableID())) {
			return;
		}

		/*
		 * Command undo = new Command() { public void execute() { table.addPermanent(c); } private final long serialVersionUID = 1L; };
		 * actions.add(undo);
		 */

		if (!c.hasAspect(MagicWarsModel.ASPECT_EXILE_INSTEAD)) {
			moveToZone(GameZone.Battlefield, GameZone.Graveyard, c);
		} else {
			moveToZone(GameZone.Battlefield, GameZone.Exile, c);
		}

		c.destroy();
		//c.leavesTheBattlefield();
	}

	/**
	 * Resets the card, untaps the card, removes anything "extra", Resets attack and defense
	 * 
	 * @param c
	 */
	private void moveToGraveyard(Card c) {
		_moveToGraveyard(c);
	}

	private void _moveToGraveyard(Card c) {
		if (!c.isToken()) {

			/**
			 * Check if it is equipment
			 */
			if (c.isEquipping()) {
				Card card = c.getEquipping().get(0);
				c.unEquipCard(card);
				card.unattachCard(c);
			}

			//int counters = c.getCounters(CounterType.P1P1) - c.getCounters(CounterType.M1M1);
			// this stopped working Unearth 
			// as spellabilities were not copied 
			// whenever creature with unearth went to graveyard
			// commented: c = CardManager.copyCardWithoutReload(c);
			graveyard.add(c);
		}
	}

	public MWPlayer getPlayer1() {
		return player1;
	}

	public void setPlayer1(MWPlayer player1) {
		this.player1 = player1;
	}

	public MWPlayer getPlayer2() {
		return player2;
	}

	public void setPlayer2(MWPlayer player2) {
		this.player2 = player2;
	}

	public MWPlayer getPlayer(Integer id) {
		if (id == null) {
			log.error("Error: getPlayer parameter is null.");
		}
		if (player1.getPlayerId() == id) {
			return player1;
		}
		if (player2.getPlayerId() == id) {
			return player2;
		}
		return null;
	}

	public MWPlayer getPlayerById(int id) {
		return getPlayer(id);
	}

	public String getNicknameById(int id) {
		return "%" + (new MWProfile(id)).getNickName() + "%";
	}

	public void setGameID(int gameID) {
		this.gameID = gameID;
		player1.cur_game_id = gameID;
		player2.cur_game_id = gameID;
	}

	public int getGameID() {
		return gameID;
	}

	/**
	 * Remove sickness and untap permanents.
	 */
	public void untap() {
		Card[] c = battlefield.getControllerPermanents(getPriorityPID());

		for (int i = 0; i < c.length; i++) {
			c[i].setSickness(false);
		}

		if (isMarbleTitanInPlay()) {
			marbleUntap(c);
		} else {
			regularUntap(c);
		}

		for (int i = 0; i < c.length; i++) {
			int p = c[i].getPreventUntapCounters();
			if (p > 0) {
				c[i].setPreventUntapCounters(p - 1);
			}

			removeAspect(c[i], MagicWarsModel.ASPECT_DOESNT_UNTAP_ON_NEXT_TURN);
		}
	}

	/**
	 * Common untap.
	 */
	protected void regularUntap(Card[] c) {
		for (int i = 0; i < c.length; i++) {
			if (!c[i].hasAspect(MagicWarsModel.ASPECT_DOESNT_UNTAP_ON_NEXT_TURN) && !c[i].hasAspect(MagicWarsModel.ASPECT_DOESNT_UNTAP)) {
				c[i].untap();
			}
		}
	}

	protected void removeAspect(Card c, String aspect) {
		while (c.hasAspect(aspect)) {
			c.removeAspect(aspect);
		}
	}

	/**
	 * Special untap: in case of Marble Titan is in play (~"creatures with power 3 or bigger don't untap during untap phase").
	 */
	protected void marbleUntap(Card[] c) {
		for (int i = 0; i < c.length; i++) {
			if (c[i].getAttack() < 3 && !c[i].hasAspect(MagicWarsModel.ASPECT_DOESNT_UNTAP_ON_NEXT_TURN)) {
				c[i].untap();
			}
		}
	}

	protected boolean isMarbleTitanInPlay() {
		CardList all = new CardList();
		all.addAll(battlefield.getControllerPermanents(player1.getPlayerId()));
		all.addAll(battlefield.getControllerPermanents(player2.getPlayerId()));

		all = all.getName("Marble Titan");
		return all.size() > 0;
	}

	public MWStack getStack() {
		return stack;
	}

	public MWGraveyard getGraveyard() {
		return graveyard;
	}

	public MWExile getRemoved() {
		return exiled;
	}

	public MWExile getExile() {
		return exiled;
	}

	public GameZone getCardZone() {
		return cardZone;
	}

	public void setCardZone(GameZone cardZone) {
		this.cardZone = cardZone;
	}

	public EndOfTurn getEndOfTurn() {
		return endOfTurn;
	}
	
	public EndOfCombat getEndOfCombat() {
		return endOfCombat;
	}

	public Upkeep getUpkeep() {
		return upkeep;
	}

	public void executeAttackEffects() {
		attackEachTurnEffect.execute();
	}

	public void executeBlockEffects() {
		blockIfAbleEffect.execute();
	}

	public boolean checkAttackingAlone() {
		if (getAttackerCount() != 1) {
			return false;
		}
		int pid = combat.getAttackingPlayerId();
		
		/**
		 * Notify "Attacks alone" event observers
		 */
		final Card aloneAttacker = getExaltedAttacker();
		if (aloneAttacker != null) {
			eventManager.getAttacksAloneEvent().notifyObservers(aloneAttacker);
		}

		CardList creatures = getBattlefield().getCreatures(pid);
		creatures = creatures.filter(new CardListFilter() {
			public boolean addCard(Card c) {
				return c.getKeyword().contains(MagicWarsModel.KEYWORD_EXALTED_SA);
			}
		});

		final int exalted = creatures.size();

		/**
		 * No creatures with exalted
		 */
		if (exalted == 0) {
			return false;
		}

		return true;
	}

	public boolean removeTimeCounters() {
		final int aid = getPriorityPID();

		CardList list = getBattlefield().getSuspended(aid);

		boolean removed = false;
		for (int i = 0; i < list.size(); i++) {
			Card c = list.get(i);
			if (c.getTimeCounters() > 0) {
				c.removeTimeCounters(1);
				removed = true;
			}
			if (c.getTimeCounters() == 0) {
				getStack().add(c.getSpellAbility()[0]);
				exile(c);
			}
		}

		return removed;
	}

	public boolean checkPrecombatMainPhaseCommands() {
		int aid = getPriorityPID();
		CardList list = getBattlefield().getPermanentList(aid);
		boolean found = false;
		for (Card card : list) {
			Command c = card.getPreCombatMainPhaseCommand();
			if (c != null) {
				found = true;
				c.execute();
			}
		}
		return found;
	}

	/**
	 * Get current global mana pool. This mana pool is temporary and filled by players during playing. If a player cancels a spell, this
	 * manapool is being migrated to his manapool.
	 * 
	 * @return
	 */
	public ManaPool getGlobalManaPool() {
		return globalManaPool;
	}

	public void setGlobalManaPool(ManaPool manaPool) {
		globalManaPool = manaPool;
	}

	public boolean isGameActive() {
		return gameActive;
	}

	public void setGameActive(boolean gameActive) {
		this.gameActive = gameActive;
	}

	/**
	 * Remove all damage counters
	 */
	public void removeAllDamageCounters() {
		CardList c = battlefield.getAllPermanents();
		for (int i = 0; i < c.size(); i++) {
			CardProxy.resetDamage(c.getCard(i));
		}
	}

	public void giveOdds(int playerID) {

		BufferedReader in;
		String filename = "odds" + Integer.toString(playerID) + ".txt";
		String defaultFilename = "odds.txt";

		try {
			/**
			 * Check odd file existing.
			 */
			File oddFile = new File(filename);
			if (!oddFile.exists()) {
				log.warn("Couldn't find specific odd file for the user, id = " + playerID);
				log.warn("Will use default odd file instead.");
				oddFile = new File(defaultFilename);
				if (!oddFile.exists()) {
					log.warn("Couldn't find default odd file. Will skip odds for the user.");
					return;
				}
			}

			Reader reader = new FileReader(oddFile);
			in = new BufferedReader(reader);
			String s = in.readLine();
			String cardName;
			Integer count = Integer.valueOf(0);
			Card card;

			CardFactory cardFactory = CardManager.getCardFactory();

			while (!s.equals("End")) {
				cardName = s.trim();
				s = in.readLine();
				try {
					count = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				int state = 0;
				if (cardName.startsWith("top:")) {
					cardName = cardName.replaceFirst("top:", "");
					state = 1;
				} else if (cardName.startsWith("grave:")) {
					cardName = cardName.replaceFirst("grave:", "");
					state = 2;
				}

				if (cardName.equals("lastImplemented")) {

					CardList list = cardFactory.getLastImplemented(count, true);
					for (Card recent : list) {
						recent.entersTheGame();
						recent.setOwner(playerID);
						recent.setController(playerID);
						recent.setUniqueNumber(getUniqueCardID());
						battlefield.addPermanent(recent);
					}

				} else {

					while (count > 0) {
						card = cardFactory.getCard(cardName.trim());
						card.entersTheGame();
						card.setOwner(playerID);
						card.setController(playerID);
						card.setUniqueNumber(getUniqueCardID());
						
						switch (state) {
						case 0:
							battlefield.addPermanent(card);
							break;
						case 1:
							putCardOnTop(playerID, card);
							break;
						case 2:
							graveyard.add(card);
							break;
						}
						count--;
					}

				}

				s = in.readLine();
			}

			reader.close();
			in.close();
		} catch (IOException ex) {
			throw new RuntimeException("giving odds : filename is " + filename + ", " + ex);
		}

	}

	public void updateHand(int playerId) throws Exception {

		BufferedReader in;
		String filename = "hand" + Integer.toString(playerId) + ".txt";

		try {
			File f = new File(filename);

			if (!f.exists()) {
				return;
			}

			CardFactory cardFactory = CardManager.getCardFactory();
			MWPlayer player = getPlayer(playerId);
			Hand hand = player.getHand();

			in = new BufferedReader(new FileReader(filename));
			String s = in.readLine().trim();
			String cardName;
			Integer count = Integer.valueOf(0);
			Card card;
			
			RuleSettingsManager rules = getRules(playerId);

			while (!s.equals("End")) {
				cardName = s;
				s = in.readLine().trim();
				try {
					count = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

				if (cardName.equals("lastImplemented")) {
					CardList list = cardFactory.getLastImplemented(count, false);
					for (Card recent : list) {
						recent.setOwner(playerId);
						recent.setController(playerId);
						recent.setUniqueNumber(getUniqueCardID());
						recent.entersTheGame();
						hand.add(recent);
						if (hand.size() > rules.getMaxHandSize()) {
							hand.remove(0);
						}
					}
				} else {

					/**
					 * Remove(pop) first card and push new one
					 */
					while (count > 0) {
						card = cardFactory.getCard(cardName);						
						card.setOwner(playerId);
						card.setController(playerId);
						card.setUniqueNumber(getUniqueCardID());
						card.entersTheGame();
						
						hand.add(card);
						if (hand.size() > rules.getMaxHandSize()) {
							hand.remove(0);
						}
						count--;
					}
				}

				s = in.readLine().trim();
			}

			in.close();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new RuntimeException(ex.getMessage(), ex);
		}

	}

	public void addGlobalEffect(GlobalEffect ge) {
		globalEffects.add(ge);
		ge.applyEffect();
	}
	
	public void removeGlobalEffect(GlobalEffect ge) {
		globalEffects.remove(ge);
		ge.discardEffect();
	}
	
	/**
	 * Add continious effect to the game.
	 * It have to be applied to initialize some values and to be discarded properly on refresh.
	 * @param ce
	 */
	public void addContiniousEffect(ContiniousEffect ce) {
		effects.get(ce.getLayer()).add(ce);
		ce.applyEffect();
	}
	
	private void removeContiniousEffects(Duration duration) {
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values()) {
			for (ContiniousEffect effect : effects.get(layer))
				effect.discardEffect();			
		}
		
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values()) {
			ArrayList<ContiniousEffect> obsolete = new ArrayList<ContiniousEffect>();
			for (ContiniousEffect effect : effects.get(layer))
				if (effect.getDuration() == duration)
					obsolete.add(effect);
			for (ContiniousEffect effect : obsolete) {
				effect.finalize();
				effects.get(layer).remove(effect);
			}
		}
		
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values()) {
			for (ContiniousEffect effect : effects.get(layer))
				effect.applyEffect();			
		}
	}
	
	private void removeContiniousEffects(List<Card> sourceCards, Duration duration) {
		for (Card sourceCard : sourceCards) {
			removeContiniousEffects(sourceCard, duration);
		}
	}
	
	private void removeContiniousEffects(Card sourceCard, Duration duration) {
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values()) {
			for (ContiniousEffect effect : effects.get(layer))
				effect.discardEffect();			
		}
		
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values()) {
			ArrayList<ContiniousEffect> obsolete = new ArrayList<ContiniousEffect>();
			for (ContiniousEffect effect : effects.get(layer))
				if (effect.getDuration() == duration && effect.getSource() == sourceCard)
					obsolete.add(effect);
			for (ContiniousEffect effect : obsolete){
				effect.finalize();
				effects.get(layer).remove(effect);
			}
		}
		
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values()) {
			for (ContiniousEffect effect : effects.get(layer))
				effect.applyEffect();			
		}
	}

	public void addManaCostEffect(ManaCostEffect mce) {
		manaCostEffects.add(mce);
	}

	public void removeManaCostEffect(ManaCostEffect mce) {
		manaCostEffects.remove(mce);
	}

	public void resetGlobalEffects() {
		globalEffects.clear();
	}

	public ArrayList<GlobalEffect> getGlobalEffects() {
		return globalEffects;
	}

	public void resetContiniousEffects() {
		for (ContiniousEffect.Layer layer : ContiniousEffect.Layer.values())
			effects.get(layer).clear();		
	}
	
	public ArrayList<Card> getMadnessCards() {
		return madnessCards;
	}

	public int removeMadnessCardsIfAny(int playerID) {
		int nRemoved = 0;
		for (int i = madnessCards.size() - 1; i >= 0; i--) {
			if (madnessCards.get(i).getOwnerID() == playerID) {
				madnessCards.remove(i);
				nRemoved++;
			}
		}

		return nRemoved;
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public void setTurnNumber(int turnNumber) {
		this.turnNumber = turnNumber;
	}

	public void verifyCreaturesInPlay() {
		combat.verifyCreaturesInPlay(this);

		for (Combat combat : getPWCombat().values()) {
			combat.verifyCreaturesInPlay(this);
		}
	}

	public void checkFirstStrike() {
		combat.checkFirstStrike(this);

		Set<Integer> keys = pwCombatMap.keySet();
		for (Integer pwid : keys) {
			Combat pwCombat = pwCombatMap.get(pwid);
			pwCombat.checkFirstStrike(this);
		}
	}

	public void acceptAssignedDamage() {
		combat.acceptAssignedDamage(this);

		Set<Integer> keys = pwCombatMap.keySet();
		for (Integer pwid : keys) {
			Combat pwCombat = pwCombatMap.get(pwid);
			pwCombat.acceptAssignedDamage(this);
		}
	}

	public int getAttackerCount() {
		int n = combat.getAttackers().length;

		Set<Integer> keys = pwCombatMap.keySet();
		for (Integer pwid : keys) {
			Combat pwCombat = pwCombatMap.get(pwid);
			n += pwCombat.getAttackers().length;
		}

		return n;
	}

	public CardList getAttackerList() {
		CardList attackers = combat.getAttackersList();

		for (Combat pwCombat : pwCombatMap.values()) {
			attackers.addAll(pwCombat.getAttackers());
		}

		return attackers;
	}

	public CardList getBlockerList() {
		CardList blockers = combat.getAllBlockers();

		for (Combat pwCombat : pwCombatMap.values()) {
			blockers.addAll(pwCombat.getAllBlockers().getArrayList());
		}

		return blockers;
	}

	/**
	 * Search for alone attacker in combat and planeswalker combats
	 * 
	 * @return
	 */
	public Card getExaltedAttacker() {
		if (getAttackerCount() > 1) {
			return null;
		}
		if (combat.getAttackersList().size() == 1) {
			return combat.getAttackersList().get(0);
		}
		Set<Integer> keys = pwCombatMap.keySet();
		for (Integer pwid : keys) {
			Combat pwCombat = pwCombatMap.get(pwid);
			if (pwCombat.getAttackersList().size() == 1) {
				return pwCombat.getAttackersList().get(0);
			}
		}
		return null;
	}

	public void executeBlockedCommands() {
		for (Card blocked : combat.getCombatMap().keySet()) {
			if (combat.getCombatMap().get(blocked).size() > 0) {
				EventParam event = new EventParam();
				event.addParam(blocked);
				event.addParam(combat.getCombatMap().get(blocked));
				eventManager.getWasBlockedEvent().notifyObservers(event);
			}
		}
		for (Combat pwCombat : pwCombatMap.values()) {
			for (Card blocked : pwCombat.getCombatMap().keySet()) {
				if (pwCombat.getCombatMap().get(blocked).size() > 0) {
					EventParam event = new EventParam();
					event.addParam(blocked);
					event.addParam(combat.getCombatMap().get(blocked));
					eventManager.getWasBlockedEvent().notifyObservers(event);
				}
			}
		}
	}
	
	public void executeBlocksCommands() {
		for (Card blocker : combat.getAllBlockers()) {
			EventParam event = new EventParam();
			event.addParam(blocker);
			event.addParam(combat.getBlockedBy(blocker));
			eventManager.getBlocksEvent().notifyObservers(event);
		}
		
		for (Combat pwCombat : pwCombatMap.values()) {
			for (Card blocker : pwCombat.getAllBlockers()) {
				EventParam event = new EventParam();
				event.addParam(blocker);
				event.addParam(combat.getBlockedBy(blocker));
				eventManager.getBlocksEvent().notifyObservers(event);
			}
		}
	}

	public HashMap<Integer, Combat> getPWCombat() {
		return pwCombatMap;
	}

	public void updatePWCombat(HashMap<Integer, CombatBean> pwCombatBean) {

		for (Integer key : pwCombatBean.keySet()) {
			CombatBean cb = pwCombatBean.get(key);
			Combat pwCombat = pwCombatMap.get(key);
			if (pwCombat == null) {
				log.debug("pw combat is null");
				return;
			}
			pwCombat.updateCombat(this, cb);
		}
	}

	private void returnPermToItsOwnerHand(Card card) {
		Card copy = CardManager.copyCardWithoutReload(card);
		card.resetCard(copy);
		copy.addAspect(MagicWarsModel.ASPECT_TEMPORARY_CARD);
		getPlayerById(card.getOwnerID()).getHand().add(card);
	}

	public String getCurrentManaColor() {
		return currentManaColor;
	}

	public void setCurrentManaColor(String currentManaColor) {
		this.currentManaColor = currentManaColor;
	}

	public ManaCost getCurrentManaCost() {
		return currentManaCost;
	}

	public void setCurrentManaCost(ManaCost currentManaCost) {
		this.currentManaCost = currentManaCost;
	}

	protected void exile(Card card) {
		if (!card.isToken()) {
			Card copy = CardManager.copyCardWithoutReload(card);
			card.resetCard(copy);
			copy.addAspect(MagicWarsModel.ASPECT_TEMPORARY_CARD);
			exiled.add(card);
		}
	}

	public void shuffleGraveyardToLibrary(int playerID) {
		//Library libraryLink = getPlayerById(playerID).getLibrary();
		CardList cards = graveyard.getPersonalCards(playerID);

		for (Card card : cards) {
			if (card.getOwnerID() == playerID) {
				//graveyard.remove(card);
				//libraryLink.add(card);
				moveToZone(GameZone.Graveyard, GameZone.Library, card);
			}
		}

		getPlayerById(playerID).shuffleLibrary();
	}

	protected void returnToItsOwnerHand(Card card) {
		if (!card.isToken()) {
			returnPermToItsOwnerHand(card);
		}
	}

	public int addDamage(Card target, int damage, Card source) {
		if (target == null || damage == 0) {
			return 0;
		}
		return addDamage(target.getTableID(), damage, source, false);
	}
	
	public int addCombatDamage(Card target, int damage, Card source) {
		if (target == null || damage == 0) {
			return 0;
		}
		return addDamage(target.getTableID(), damage, source, true);
	}

	private int addDamage(int tableID, int damage, Card source, boolean combatDamage) {
		if (!battlefield.isCardInPlay(tableID) || damage <= 0)
			return 0;

		Card card = battlefield.getPermanent(tableID);
		boolean prevented = checkProtection(card, source);

		if (!prevented) {

			// Check Prevent Damage
			if (source != null) {
				// First check aspects
				if (source.hasAspect(MagicWarsModel.ASPECT_PREVENT_ALL_DAMAGE_FROM_THIS)
						|| card.hasAspect(MagicWarsModel.ASPECT_PREVENT_ALL_DAMAGE_DEALT)) {
					if (isDamageCantBePrevented()) {
						addSystemMessage("Failed to prevent damage from source (damage: " + damage + ",source: {" + source
								+ "}). Damage can't be prevented.");
					} else if (source.hasAspect(MagicWarsModel.ASPECT_THIS_DAMAGE_CANT_BE_PREVENTED)) {
						addSystemMessage("Failed to prevent damage from source (damage: " + damage + ",source: {" + source
								+ "}). Damage can't be prevented from this source.");
					} else {
						addSystemMessage("Damage from source (damage: " + damage + ",source: {" + source + "}) has been prevented.");
						damage = 0;
					}
				} else { // Then check prevent "counters" ("Prevent next 3 damage dealt this turn")
					int prevent = card.getPreventDamage();
					if (prevent > 0) {
						if (isDamageCantBePrevented()) {
							addSystemMessage("Failed to prevent damage (damage: " + damage + ",source: {" + source
									+ "}). Damage can't be prevented.");
						} else if (source.hasAspect(MagicWarsModel.ASPECT_THIS_DAMAGE_CANT_BE_PREVENTED)) {
							addSystemMessage("Failed to prevent damage from source (damage: " + damage + ",source: {" + source
									+ "}). Damage can't be prevented from this source.");
						} else {
							if (prevent >= damage) {
								card.setPreventDamage(prevent - damage);
								addSystemMessage(prevent + " damage from source (damage: " + damage + ",source: {" + source + "}) has been prevented.");
								damage = 0;
							} else {
								card.setPreventDamage(0);
								addSystemMessage(prevent + " damage from source (damage: " + damage + ",source: {" + source + "}) has been prevented.");
								damage = damage - prevent;
							}
						}
					}
				}
			}

			if (damage > 0) {
				checkDeathtouch(card, source);
				if (battlefield.isCardInPlay(card)) { // not death touched or still in play
					if (source.hasKeyword(MagicWarsModel.KEYWORD_WITHER_SA)) {
						dealWitherDamage(card, damage, source);
					} else {
						CardProxy.addDamage(card, damage, source);
					}
				}
			}
		} else {
			damage = 0; // no lifelink or other triggers
			addSystemMessage("{" + card + "} has protection, damage from {" + source + "} has been prevented.");
		}

		if (source != null && damage > 0) {
			EventParam event = new EventParam();
			event.setTarget(card);
			event.setSource(source);
			event.getParams().add(Integer.valueOf(damage));
			event.getParams().add(Integer.valueOf(0)); // playerId, dummy
			eventManager.getDealDamageEvent().notifyObservers(event);

			if (combatDamage) {
				event = new EventParam();
				event.setTarget(card);
				event.setSource(source);
				event.getParams().add(Integer.valueOf(damage));
				event.getParams().add(Integer.valueOf(0));
				eventManager.getDealCombatDamageEvent().notifyObservers(event);
			}
			
			if (card.isCreature()) {
				source.dealDamageToCreatureCommand();
			}
		}

		return damage;
	}

	private void dealWitherDamage(Card card, int damage, Card source) {
		card.putCounter(CounterType.M1M1, damage);
		card.setLatestDealtDamage(damage);
		if (card.getDealtDamageCommand() != null) {
			card.getDealtDamageCommand().execute();
		}
		if (card.getDealtDamageCommandEx() != null) {
			card.getDealtDamageCommandEx().setTarget(source);
			card.getDealtDamageCommandEx().execute();
		}
	}

	private void checkDeathtouch(Card card, Card source) {
		if (source != null && source.getKeyword().contains(MagicWarsModel.KEYWORD_DEATHTOUCH_SA) && !card.isPlaneswalker()) { // ability, doesn't go to stack
			destroy(card);
		}
	}

	public int gainLife(MWPlayer player, int lifeToGain, Card source) {
		return player.gainLife(lifeToGain);
	}

	public int loseLife(MWPlayer player, int lifeToLose, Card source) {
		return player.loseLife(lifeToLose);
	}

	public boolean discardCard(int playerID, Card card) {

		if (card == null) {
			return false;
		}

		/**
		 * Search for madness
		 */
		boolean bFoundMadness = false;
		ArrayList<SpellAbility> sa = card.getSpellAbilities();
		for (int i = 0; i < sa.size(); i++) {
			if (sa.get(i) instanceof Madness) {
				bFoundMadness = true;
				madnessCards.add(card);
				card.addAspect(MagicWarsModel.ASPECT_PLAY_MADNESS);
				break;
			}
		}

		/**
		 * No madness. Then put it into graveyard.
		 */
		boolean discarded = true;
		if (!bFoundMadness) {
			putIntoGraveyardFromHand(playerID, card);
		} else { // else just remove
			discarded = getPlayerById(playerID).getHand().remove(card);
		}
		addSystemMessage(getNicknameById(playerID) + " discarded {" + card + "}");

		return discarded;
	}

	public int discardRandom(int playerId, int count) {
		MWPlayer player = getPlayer(playerId);
		int discarded = 0;
		if (count < 0) {
			log.error("discardRandom: count can't be less than zero: " + count);
			return 0;
		}
		if (player != null) {
			while (player.getHandSize() > 0 && count > 0) {
				int index = MyRandom.random.nextInt(player.getHandSize());
				if (discardCard(playerId, player.getHand().get(index))) {
					discarded++;
				}
				count--;
			}
			return discarded;
		} else {
			log.error("discardRandom: no such player, id = " + playerId);
		}
		return 0;
	}

	public void removeCardFromHand(int playerID, Card card) {
		boolean bRemoved = getPlayerById(playerID).getHand().remove(card);
		if (bRemoved) {
			exiled.add(card);
		}
	}

	public void removeCardFromLibrary(int playerID, Card card) {
		boolean bRemoved = getPlayerById(playerID).getLibrary().remove(card);
		if (bRemoved) {
			exiled.add(card);
		}
	}

	public Card putCardFromTopToHand(int playerID) {
		Library library = getPlayerById(playerID).getLibrary();
		if (library.size() > 0) {
			Card card = library.remove(0);
			getPlayerById(playerID).getHand().add(card);
			return card;
		} else {
			return null;
		}
	}

	public boolean removeCardFromGraveyard(Card card) {
		Card removed = getGraveyard().remove(card);
		if (removed != null) {
			exiled.add(removed);
			return true;
		}
		return false;
	}

	public void putCardFromTopOnBottom(int playerID) {
		Library library = getPlayerById(playerID).getLibrary();
		if (library.size() > 0) {
			Card card = library.removeFirst();
			library.addLast(card);
		}
	}

	public Card putCardFromTopIntoGrave(int playerID) {
		Library library = getPlayerById(playerID).getLibrary();
		Card card = null;
		if (library.size() > 0) {
			card = library.removeFirst();
			putIntoGraveyard(card);
		}

		return card;
	}

	public void putCardFromHandOnBottom(int playerID, Card card) {
		boolean bRemoved = getPlayerById(playerID).getHand().remove(card);
		if (bRemoved) {
			Library library = getPlayerById(playerID).getLibrary();
			library.add(card);
		}
	}

	public void putCardFromHandOnTop(int playerId, Card card) {
		boolean bRemoved = getPlayerById(playerId).getHand().remove(card);
		if (bRemoved) {
			putCardOnTop(playerId, card);
		}
	}

	public void putCardOnTop(int playerId, Card card) {
		Library top = new Library(this, playerId);
		top.add(card);
		MWPlayer player = getPlayer(playerId);
		if (player != null) {
			Library library = player.library;
			for (int i = 0; i < library.size(); i++) {
				top.add(library.get(i));
			}
			getPlayerById(playerId).setLibrary(top);
		}
	}

	public void moveInLibrary(Card card, CardOrder order) {
		if (card == null) {
			return;
		}
		MWPlayer player = getPlayerById(card.getOwner());
		Library library = player.getLibrary();
		library.remove(card);
		if (order.equals(CardOrder.Top)) {
			library.addFirst(card);
		} else if (order.equals(CardOrder.Bottom)) {
			library.addLast(card);
		}
	}

	public void moveInLibrary(Card card, Integer order) {
		if (card == null) {
			return;
		}
		if (order < 0) {
			order = 0;
		}
		MWPlayer player = getPlayerById(card.getOwner());
		player.getLibrary().remove(card);
		if (player.getLibrary().size() < order) {
			player.getLibrary().add(card); // add at the bottom
		} else {
			player.getLibrary().add(order, card);
		}
	}

	public void shuffleLibrary(int playerID) {
		getPlayerById(playerID).shuffleLibrary();
		addSystemMessage(getNicknameById(playerID) + " shuffled the library.");
	}

	public void addRemovedCardToHand(int playerID, Card removedCard) {
		getPlayerById(playerID).getHand().add(removedCard);
		removeCardFromRemovedZone(removedCard);
	}

	public void putRemovedCardOnTop(int playerID, Card removedCard) {
		if (removeCardFromRemovedZone(removedCard)) {
			Card copy = CardManager.copyCardWithoutReload(removedCard);
			removedCard.resetCard(copy);
			copy.addAspect(MagicWarsModel.ASPECT_TEMPORARY_CARD);
			putCardOnTop(playerID, removedCard);
		}
	}

	public boolean removeCardFromRemovedZone(Card removedCard) {
		return exiled.remove(removedCard);
	}

	public void putIntoGraveyardFromHand(int playerID, Card card) {
		moveToZone(GameZone.Hand, GameZone.Graveyard, card);
	}

	public void putIntoGraveyard(Card card) {
		graveyard.add(card);
	}

	public void putIntoGraveyardFromPlay(Card card) {
		graveyard.add(card);
	}

	public void changePermanentControllerTo(int newControllerID, Card card) {
		if (!battlefield.isCardInPlay(card)) return;
		
		ArrayList<GlobalEffect> removedGB = battlefield.clearGlobalEffects(card);
		battlefield.discardGlobalEffects(card);
		
		removeContiniousEffects(card, Duration.WHILE_UNDER_CONTROL);
		
		Command command = card.getChangeControllerCommand();
		if (command != null) {
			command.setParam(Integer.valueOf(newControllerID));
			command.execute();
		}
		card.setController(newControllerID);
		
		getBattlefield().applyGlobalEffects(card);
		
		for (GlobalEffect gb : removedGB) {
			addGlobalEffect(gb);
		}
	}

	public void counterTargetSpell(SpellAbility spell) {
		if (spell == null) {
			return;
		}
		MWStack stack = getStack();
		for (int i = 0; i < stack.size(); i++) {
			SpellAbility sa = stack.peek(i);
			if (spell.equals(sa) && !sa.getSourceCard().getKeyword().contains("Can't be countered") && canBeCountered(sa)) {
				stack.pop(i);
				if (sa.hasAspect(MagicWarsModel.ASPECT_PUT_ON_TOP_INSTEAD)) {
					sa.removeAspect(MagicWarsModel.ASPECT_PUT_ON_TOP_INSTEAD);
					putCardOnTop(sa.getSourceCard().getOwnerID(), sa.getSourceCard());
					addSystemMessage("{" + sa.getSourceCard() + "} was put on top of its owner's library");
				} else {
					getGraveyard().add(sa.getSourceCard());
				}

				// Remove all attached spells
				ArrayList<Integer> toRemove = new ArrayList<Integer>();
				for (int j = stack.size() - 1; j >= 0; j--) {
					SpellAbility stackSA = stack.peek(j);
					if (stackSA.isAttached() && stackSA.getSourceCard().getUniqueNumber() == spell.getSourceCard().getUniqueNumber()) {
						toRemove.add(j);
					}
				}
				for (Integer index : toRemove) {
					stack.pop(index);
				}

				break;
			}
		}
	}

	/**
	 * This methods counters target spell unless spell's controller doesn't pay {cost}
	 * 
	 * @param target
	 *            target spell to counter
	 * @param cost
	 *            cost to play to ignore "counter" ability
	 */
	public void counterTargetSpellUnlessPaid(SpellAbility target, String cost, Card source) {
		Card card = target.getSourceCard();
		GameManager game = card.getGame();
		int playerToAsk = card.getControllerID();

		AbilityActivated unlessAbility = new AbilityActivated(card, cost) {
			private static final long serialVersionUID = 2261887606193255853L;

			public void resolve() {
				Card source = (Card) getAspectValue("Source");
				getSourceCard().getGame()
						.addSystemMessage(
								"{" + source.toStringWithUID() + "} didn't counter spell [unless cost (" + getAspectValue("Unless")
										+ ") was paid]");
			}
		};
		unlessAbility.setInvisible(true);

		Command cancelCommand = new Command() {
			private static final long serialVersionUID = 4120779644445706276L;

			public void execute() {
				String count = (String) getParam();
				SpellAbility spell = (SpellAbility) getTarget();
				Card source = (Card) getSource();
				GameManager game = source.getGame();
				game.addSystemMessage("{" + source.toStringWithUID() + "} has countered {" + spell.getSourceCard() + "} [unless cost ("
						+ count + ") wasn't paid].");
				game.counterTargetSpell(spell);
			}
		};
		unlessAbility.addAspect("Unless", cost);
		unlessAbility.addAspect("Source", source);
		unlessAbility.setPlayerIdToAsk(playerToAsk);
		unlessAbility.setYesNoQuestion("Counter target spell unless " + cost + " is paid. Pay?");
		unlessAbility.setCancelCommand(cancelCommand);
		unlessAbility.setStackDescription(target.getSourceCard().toStringWithUID()
				+ ": Counter target spell unless its controller pays 1 for each basic land type among lands you control.");

		cancelCommand.setParam(cost);
		cancelCommand.setTarget(target);
		cancelCommand.setSource(source);
		game.getStack().add(unlessAbility);
	}

	/**
	 * Should be called on spell fizzle because of wrong target
	 * 
	 * @param spell
	 */
	public void fizzleSpell(SpellAbility spell) {
		spell.setSpellChain(null);
		if (spell.isSpell()) {
			addSystemMessage("Spell {" + spell.getSourceCard() + "} has been fizzled.");
		} else {
			addSystemMessage("Ability from {" + spell.getSourceCard() + "} has been fizzled.");
		}
	}

	public void setCurrentPhase(PhaseName phaseName) {
		this.currentPhaseName = phaseName;
		boolean found = false;
		for (Integer num : phaseNumVsPhaseName.keySet()) {
			if (phaseNumVsPhaseName.get(num).equals(phaseName)) {
				this.currentPhase = num;
				found = true;
				break;
			}
		}

		if (!found) { // actually can't be, just to make sure
			try {
				throw new Exception("There is no such phase name: " + phaseName);
			} catch (Exception e) { // funny code ;)
				e.printStackTrace();
			}
		}
	}

	public void goToNextPhase() {
		this.currentPhase++;
		this.currentPhaseName = phaseNumVsPhaseName.get(this.currentPhase);
		log.info("goToNextPhase() - " + currentPhaseName);
	}

	public PhaseName getCurrentPhaseName() {
		return this.currentPhaseName;
	}

	/**
	 * User getCurrentPhaseName instead
	 * 
	 * @return
	 */
	@Deprecated
	public PhaseName getPhaseName() {
		return getCurrentPhaseName();
	}

	/**
	 * Compare phaseName to current phase name
	 * 
	 * @param phaseName
	 *            phase name to compare
	 * @return true if current phase has the same name as phaseName parameter, otherwise - false
	 */
	public boolean isCurrentPhase(PhaseName phaseName) {
		return this.currentPhaseName.equals(phaseName);
	}

	public int getWaitingPlayerID() {
		return waitingPlayerID;
	}

	public void setWaitingPlayerID(int waitingPlayerID) {
		this.waitingPlayerID = waitingPlayerID;
	}

	public Card findCard(int playerID, CardBean cardBean, GameZone zone) {

		if (cardBean.isPlayer()) {
			Card playerCard = new Card();
			playerCard.setName(cardBean.getName());
			playerCard.setPlayer(true);
			playerCard.setOwner(cardBean.getOwnerID());
			return playerCard;
		}

		if (zone.equals(GameZone.Hand)) {

			MWPlayer player = getPlayer(playerID);
			Hand hand = player.getHand();
			for (int i = 0; i < hand.size(); i++) {
				if (hand.get(i).getUniqueNumber() == cardBean.getUniqueNumber()) {
					return hand.get(i);
				}
			}

			player = getOpponentById(playerID);
			hand = player.getHand();
			for (int i = 0; i < hand.size(); i++) {
				if (hand.get(i).getUniqueNumber() == cardBean.getUniqueNumber()) {
					return hand.get(i);
				}
			}

		} else if (zone.equals(GameZone.Battlefield)) {

			CardList table = getBattlefield().getPermanentList(playerID);
			for (int i = 0; i < table.size(); i++) {
				if (table.get(i).getUniqueNumber() == cardBean.getUniqueNumber()) {
					return table.get(i);
				}
			}

		} else if (zone.equals(GameZone.Effect)) {
			for (int i = 0; i < madnessCards.size(); i++) {
				if (madnessCards.get(i).getUniqueNumber() == cardBean.getUniqueNumber()) {
					return madnessCards.get(i);
				}
			}

		} else if (zone.equals(GameZone.Library)) {
			MWPlayer player = getPlayer(playerID);
			Library lib = player.getLibrary();
			for (int i = 0; i < lib.size(); i++) {
				if (lib.get(i).getUniqueNumber() == cardBean.getUniqueNumber()) {
					return lib.get(i);
				}
			}
		} else if (zone.equals(GameZone.Graveyard)) {
			CardList list = graveyard.getPersonalCards(playerID);
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getUniqueNumber() == cardBean.getUniqueNumber()) {
					return list.get(i);
				}
			}
		} else if (zone.equals(GameZone.Cascade)) {
			for (Card card : cascadeCards) {
				if (card.getUniqueNumber() == cardBean.getUniqueNumber()) {
					return card;
				}
			}
		} else if (zone.equals(GameZone.Copied)) {
			for (Card card : copiedSpells) {
				if (card.getUniqueNumber() == cardBean.getUniqueNumber()) {
					return card;
				}
			}
		}

		return null;
	}

	public CardList findCards(int playerID, CardBeanList cardBeanList, GameZone zone) {

		CardList cards = new CardList();

		for (int i = 0; i < cardBeanList.size(); i++) {
			CardBean cardBean = cardBeanList.get(i);
			Card card = findCard(cardBean.getControllerID(), cardBean, zone);
			if (card != null) {
				cards.add(card);
			}
		}

		return cards;
	}

	public ArrayList<String> getDeckList() {

		/**
		 * Create map once containing deck names with their filename paths where they are described in
		 */
		if (deckPathList == null) {
			deckPathList = new HashMap<String, String>();

			File deckList = new File(mw.server.constant.Constant.deckListPath);

			if (!deckList.exists() || !deckList.isFile()) {
				log.warn("Couldn't find file with deck list: " + mw.server.constant.Constant.deckListPath);
				return new ArrayList<String>();
			}

			try {
				BufferedReader in;
				FileReader reader = new FileReader(deckList);
				in = new BufferedReader(reader);
				String s = in.readLine();
				String deckCommonName;
				String deckPath = "";

				while (!s.equals("End")) {
					deckCommonName = s;
					s = in.readLine();
					deckPath = s;
					deckPathList.put(deckCommonName.trim(), deckPath.trim());
					s = in.readLine();
				}
			} catch (FileNotFoundException fe) {
				log.error(fe.toString());
				return new ArrayList<String>();
			} catch (IOException ioe) {
				log.error(ioe.toString());
				return new ArrayList<String>();
			}
		}

		/**
		 * Create list of deck names
		 */
		if (deckNameList == null) {
			deckNameList = new ArrayList<String>(deckPathList.keySet());
			//TODO: move to constant
			//deckNameList.add("Astral mode (all cards)");
		}

		return deckNameList;
	}

	public String getDeckPath(String deckName) {
		return deckPathList.get(deckName.trim());
	}

	public void addLocalEffect(Card card) {
		madnessCards.add(card);
	}

	public boolean isNeedFirstStrikeCombat() {
		return isNeedFirstStrikeCombat;
	}

	public boolean wasNeedFirstStrikeCombat() {
		return wasNeedFirstStrikeCombat;
	}

	public void setIsNeedFirstStrikeCombat(boolean value) {
		isNeedFirstStrikeCombat = value;
	}

	public void setWasNeedFirstStrikeCombat(boolean value) {
		wasNeedFirstStrikeCombat = value;
	}

	public ArrayList<Card> getCascadeCards() {
		return cascadeCards;
	}

	public ArrayList<Card> getCopiedSpells() {
		return copiedSpells;
	}

	public void setCopiedSpells(ArrayList<Card> copiedSpells) {
		this.copiedSpells = copiedSpells;
	}

	public SpellAbility getRememberedSpellAbililty() {
		return rememberedSpellAbililty;
	}

	public void rememberSpellAbililty(SpellAbility spellAbililty) {
		this.rememberedSpellAbililty = spellAbililty;
	}

	public Map<Card, ArrayList<Card>> getRevealedCards() {
		return revealedCards;
	}

	public ArrayList<Card> getRevealedCardsToPlay() {
		return revealedCardsToPlay;
	}

	public ArrayList<Card> getViewedCards() {
		return viewedCards;
	}

	public void revealCard(Card source, Card card) {
		if (source != null && card != null) {
			if (revealedCards.containsKey(source)) {
				revealedCards.get(source).add(card);
			} else {
				ArrayList<Card> list = new ArrayList<Card>();
				list.add(card);
				revealedCards.put(source, list);
			}
			if (!isInTransaction()) {
				eventManager.getRevealCardEvent().notifyObservers(card);
			}
		}
	}
	
	public void revealCards(Card source, List<Card> cards) {
		if (source != null && cards != null && cards.size() > 0) {
			if (revealedCards.containsKey(source)) {
				List<Card> list = revealedCards.get(source);
				for (Card card : cards) {
					list.add(card);
				}
			} else {
				ArrayList<Card> list = new ArrayList<Card>();
				for (Card card : cards) {
					list.add(card);
				}
				revealedCards.put(source, list);
			}
			if (!isInTransaction()) {
				eventManager.getRevealCardEvent().notifyObservers(cards.get(0));
			}
		}
	}

	public void lookAtCard(Card card) {
		if (card != null) {
			viewedCards.add(card);
			if (!isInTransaction()) {
				eventManager.getLookAtCardEvent().notifyObservers(card);
			}
		}
	}

	public void revealCardToPlay(Card card) {
		revealedCardsToPlay.add(card);
	}

	public void resetRevealedCards() {
		revealedCards.clear();
	}

	public void resetViewedCards() {
		viewedCards.clear();
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public void requestTableUpdate() {
		requestTableUpdate = true;
	}

	public void resetTableUpdateRequest() {
		requestTableUpdate = false;
	}

	public boolean isTableUpdateRequested() {
		return requestTableUpdate;
	}

	public void addSacrificeCreatureObserver(Observer obs) {
		this.eventManager.getSacrificeCreatureEvent().addObserver(obs);
	}

	public void removeSacrificeCreatureObserver(Observer obs) {
		this.eventManager.getSacrificeCreatureEvent().deleteObserver(obs);
	}

	public ArrayList<String> getSystemMessages() {
		return this.systemMessages;
	}

	public void addSystemMessage(String message) {
		this.systemMessages.add(message);
		this.eventManager.getUpdateSystemMessageEvent().notifyObservers();
	}

	public void resetSystemMessages() {
		this.systemMessages.clear();
	}

	public CardBeanList getAllCards(boolean uniqueNames) {
		return new CardBeanList(CardManager.getAllCards(uniqueNames));
	}

	public ArrayList<String> getAllCardNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (String name : CardManager.getAllCardNames()) {
			names.add(name);
		}
		Collections.sort(names);
		return names;
	}

	public ArrayList<String> getCardsThatCantBePlayed() {
		return cardsThatCantBePlayed;
	}

	public void addCardThatCantBePlayed(String cardNameThatCantBePlayed) {
		this.cardsThatCantBePlayed.add(cardNameThatCantBePlayed);
	}

	public void removeCardThatCantBePlayed(String cardNameThatCantBePlayed) {
		this.cardsThatCantBePlayed.remove(cardNameThatCantBePlayed);
	}

	public Map<Integer, List<Color>> getCardColorsThatCantBeCast() {
		return cardColorsThatCantBeCast;
	}

	public void addCardColorsThatCantBeCast(int playerId, Color color) {
		if (this.cardColorsThatCantBeCast.containsKey(playerId)) {
			this.cardColorsThatCantBeCast.get(playerId).add(color);
		} else {
			ArrayList<Color> list = new ArrayList<Color>();
			list.add(color);
			this.cardColorsThatCantBeCast.put(playerId, list);
		}
	}

	public void removeCardColorsThatCantBeCast(int playerId, Color color) {
		if (this.cardColorsThatCantBeCast.containsKey(playerId)) {
			this.cardColorsThatCantBeCast.get(playerId).remove(color);
		}
	}

	public int getActivePlayerId() {
		return activePlayerId;
	}

	public void setActivePlayerId(int turnOwnerId) {
		this.activePlayerId = turnOwnerId;
	}

	public boolean canBeCountered(SpellAbility sa) {
		// we can't counter attached effect, only parent spells and abilities these effects attached to
		if (sa.hasAspect(MagicWarsModel.ASPECT_ATTACHED)) {
			return false;
		}

		if (sa.getSourceCard().getKeyword().contains("Can't be countered")) {
			return false;
		}

		//FIXME: refactor this, it can be moved to card in current engine
		if (sa.getSourceCard().getName().equals("Banefire") && sa.getSourceCard().getSpellAbilities().size() > 0
				&& sa.getSourceCard().getSpellAbilities().get(0).getXValue() >= 5) {
			return false;
		}

		//FIXME: refactor this, it can be moved to card in current engine
		if (sa.getSourceCard().isCreature()) {
			if (sa.getSourceCard().getAttack() >= 5) {
				int cid = sa.getSourceCard().getControllerID();
				CardList spellBreakers = getBattlefield().getPermanentList(cid);
				spellBreakers = spellBreakers.getName("Spellbreaker Behemoth");

				if (spellBreakers.size() > 0) {
					return false;
				}
			}
		}

		return true;
	}

	public boolean canBeTargetedBySource(Card source, Card target) {

		ArrayList<String> keywords = target.getKeyword();

		for (String keyword : keywords) {

			if (keyword.equals("Shroud")) {
				return false;
			}

			if (keyword.equals("OppShroud") && source.getControllerID() != target.getControllerID()) {
				return false;
			}

			if (keyword.equals("Protection from black") && target.getColor().contains(Constant.Color.Black)) {
				return false;
			}
			if (keyword.equals("Protection from red") && target.getColor().contains(Constant.Color.Red)) {
				return false;
			}
			if (keyword.equals("Protection from green") && target.getColor().contains(Constant.Color.Green)) {
				return false;
			}
			if (keyword.equals("Protection from white") && target.getColor().contains(Constant.Color.White)) {
				return false;
			}
			if (keyword.equals("Protection from blue") && target.getColor().contains(Constant.Color.Blue)) {
				return false;
			}

			if (keyword.equals("Protection from creatures") && source.isCreature()) {
				return false;
			}
			if (keyword.equals("Protection from artifacts") && source.isArtifact()) {
				return false;
			}
			if (keyword.equals("Protection from everything")) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 
	 * 
	 * @param playerId
	 *            player id to damage
	 * @param damage
	 *            amount of damage to deal
	 * @param source
	 *            source card that deals damage
	 * @return
	 */
	private int dealDamageToThePlayerWithRedirection(final int playerId, int damage, final Card source, boolean isCombatDamage) {

		/**
		 * Prevent damage
		 */
		if (source != null) {
			// First check aspects
			if (source.hasAspect(MagicWarsModel.ASPECT_PREVENT_ALL_DAMAGE_FROM_THIS)) {
				if (isDamageCantBePrevented()) {
					addSystemMessage("Failed to prevent damage from source (damage: " + damage + ",source: {" + source
							+ "}). Damage can't be prevented.");
				} else if (source.hasAspect(MagicWarsModel.ASPECT_THIS_DAMAGE_CANT_BE_PREVENTED)) {
					addSystemMessage("Failed to prevent damage from source (damage: " + damage + ",source: {" + source
							+ "}). Damage can't be prevented from this source.");
				} else {
					addSystemMessage("Damage from source (damage: " + damage + ",source: {" + source + "}) has been prevented.");
					return 0;
				}
			} else { // Then check prevent "counters" ("Prevent next 3 damage dealt this turn")
				int prevent = getPlayer(playerId).getPreventDamage();
				if (prevent > 0) {
					if (isDamageCantBePrevented()) {
						addSystemMessage("Failed to prevent damage (damage: " + damage + ",source: {" + source
								+ "}). Damage can't be prevented.");
					} else if (source.hasAspect(MagicWarsModel.ASPECT_THIS_DAMAGE_CANT_BE_PREVENTED)) {
						addSystemMessage("Failed to prevent damage from source (damage: " + damage + ",source: {" + source
								+ "}). Damage can't be prevented from this source.");
					} else {
						if (prevent >= damage) {
							getPlayer(playerId).setPreventDamage(prevent - damage);
							addSystemMessage(prevent + " damage from source (damage: " + damage + ",source: {" + source + "}) has been prevented.");
							return 0;
						} else {
							getPlayer(playerId).setPreventDamage(0);
							addSystemMessage(prevent + " damage from source (damage: " + damage + ",source: {" + source + "}) has been prevented.");
							damage = damage - prevent;
						}
					}
				}
			}
		}

		/**
		 * Ask about redirecting damage to a planeswalker
		 * 
		 * 306.8 If noncombat damage would be dealt to a player by a source controlled by an opponent, that opponent may have that source
		 * deal that damage to a planeswalker the first player controls instead.
		 */
		if (!isCombatDamage && playerId != source.getControllerID()) {
			final CardList planeswalkers = battlefield.getPermanentList(playerId).getType("Planeswalker");
			if (planeswalkers.size() > 0) {
				final int _damage = damage;
				final Command cancelCommand = new Command() {
					private static final long serialVersionUID = 1L;

					public void execute() {
						dealFinalDamage(playerId, _damage, source, false);
					}
				};
				final ChoiceCommand runtime = new ChoiceCommand() {
					private static final long serialVersionUID = 1L;

					public void execute() {
						setInputChoice(planeswalkers);
					}
				};
				final SpellAbility choosePlaneswalker = new AbilityTriggered(source) {
					private static final long serialVersionUID = -2899150997615877934L;

					public void resolve() {
						Card planeswalker = getTargetCard();
						if (planeswalker != null) {
							addSystemMessage("Damage has been redirected to planeswalker: " + planeswalker + ".");
							addDamage(planeswalker.getTableID(), _damage, source, false);
						}
					}
				};
				choosePlaneswalker.setInvisible(true);
				choosePlaneswalker.setNeedsToChooseCard(true);
				choosePlaneswalker.setChoiceCommand(runtime);
				choosePlaneswalker.setTargetsAreOptional(false);
				final SpellAbility redirect = new AbilityTriggered(source) {
					private static final long serialVersionUID = -6293368489064690803L;

					public void resolve() {
						getStack().add(choosePlaneswalker);
					}
				};
				redirect.setPlayerIdToAsk(source.getControllerID());
				redirect.setYesNoQuestion("Redirect " + damage + " damage to a planeswalker?");
				redirect.setInvisible(true);
				redirect.setCancelCommand(cancelCommand);
				getStack().add(redirect);
			} else {
				dealFinalDamage(playerId, damage, source, false);
			}
		} else {
			dealFinalDamage(playerId, damage, source, true);
		}

		return damage;
	}

	/**
	 * This is the only place where player.dealDamage can be used
	 * 
	 * @param playerId
	 * @param damage
	 * @param source
	 * @param combatDamage defines the type of damage: combat or common (includes combat)
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private int dealFinalDamage(int playerId, int damage, Card source, boolean combatDamage) {
		MWPlayer player = getPlayerById(playerId);
		player.dealDamage(damage);

		// new messaging system: send this message to game, not to player
		EventParam event = new EventParam();
		event.setSource(source);
		event.getParams().add(Integer.valueOf(damage));
		event.getParams().add(Integer.valueOf(playerId));
		eventManager.getDealDamageEvent().notifyObservers(event);
		
		if (combatDamage) {
			event = new EventParam();
			event.setSource(source);
			event.getParams().add(Integer.valueOf(damage));
			event.getParams().add(Integer.valueOf(playerId));
			eventManager.getDealCombatDamageEvent().notifyObservers(event);
		}

		return damage;
	}

	/**
	 * Deal combat damage
	 * 
	 * @param playerId
	 * @param damage
	 * @param source
	 * @return
	 */
	public int dealCombatDamageToThePlayer(int playerId, int damage, Card source) {
		return dealDamageToThePlayerWithRedirection(playerId, damage, source, true);
	}

	/**
	 * Deal non combat damage
	 * 
	 * @param playerId
	 * @param damage
	 * @param source
	 * @return
	 */
	public int dealDamageToThePlayer(int playerId, int damage, Card source) {
		return dealDamageToThePlayerWithRedirection(playerId, damage, source, false);
	}

	public int dealDamage(int playerId, int damage, Card source) {
		return dealDamageToThePlayer(playerId, damage, source);
	}

	public int dealDamage(Card target, int damage, Card source) {
		return addDamage(target, damage, source);
	}

	public Card getRememberedCard() {
		return rememberedCard;
	}

	public void setRememberedCard(Card rememberedCard) {
		this.rememberedCard = rememberedCard;
	}

	public boolean isPreventCombatDamageThisTurn() {
		return preventCombatDamageThisTurn;
	}

	public void setPreventCombatDamageThisTurn(boolean preventCombatDamageThisTurn) {
		this.preventCombatDamageThisTurn = preventCombatDamageThisTurn;
	}

	public boolean isDamageCantBePrevented() {
		return damageCantBePrevented;
	}

	public void setDamageCantBePrevented(boolean damageCantBePrevented) {
		this.damageCantBePrevented = damageCantBePrevented;
	}

	public String getPlayersNickname(int playerId) {
		return new MWProfile(playerId).getNickName();
	}

	public int moveToZone(GameZone from, GameZone to, CardList cards) {
		int count = 0;
		for (Card card : cards) {
			if (moveToZone(from, to, card))
				count++;
		}
		return count;
	}

	public boolean moveToZone(GameZone from, GameZone to, Card card) {
		if (card == null) {
			return false;
		}
		int fromId = card.getOwnerID();
		int toId = card.getOwnerID();
		if (from.equals(GameZone.Battlefield)) {
			fromId = card.getControllerID();
		}
		if (to.equals(GameZone.Battlefield)) {
			toId = card.getControllerID();
		}
		return moveToZone(new PlayerZone(from, fromId), new PlayerZone(to, toId), card);
	}

	public boolean moveToZone(PlayerZone from, PlayerZone to, Card card) {
		if (card == null) {
			return false;
		}
		boolean result = _moveToZone(from, to, card);
		EventParam params = new EventParam();
		params.addParam(from);
		params.addParam(to);
		params.addParam(card);
		getEventManager().getMoveToZoneEvent().notifyObservers(params);
		return result;
	}

	private boolean _moveToZone(PlayerZone from, PlayerZone to, Card card) {
		if (card == null) {
			return false;
		}
		if (from.getGameZone().equals(GameZone.Library)) {
			if (to.getGameZone().equals(GameZone.Battlefield)) {
				boolean bRemoved = getPlayerById(from.getPlayerId()).getLibrary().remove(card);
				if (bRemoved) {
					card.setController(to.getPlayerId());
					battlefield.addPermanent(card, true);
					if (card.isAura()) {
						card.addAspect(MagicWarsModel.ASPECT_NOT_ATTACHED_AURA);
					}
				}
				return bRemoved;
			}
			if (to.getGameZone().equals(GameZone.Exile)) {
				boolean bRemoved = getPlayerById(from.getPlayerId()).getLibrary().remove(card);
				if (bRemoved) {
					exiled.add(card);
				}
				return bRemoved;
			}
			if (to.getGameZone().equals(GameZone.Hand)) {
				boolean bRemoved = getPlayerById(from.getPlayerId()).getLibrary().remove(card);
				if (bRemoved) {
					getPlayerById(to.getPlayerId()).getHand().add(card);
				}
				return bRemoved;
			}
			if (to.getGameZone().equals(GameZone.Graveyard)) {
				boolean bRemoved = getPlayerById(from.getPlayerId()).getLibrary().remove(card);
				if (bRemoved) {
					graveyard.add(card);
				}
				return bRemoved;
			}
			log.error("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
			throw new RuntimeException("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
		} else if (from.getGameZone().equals(GameZone.Battlefield)) {
			if (!battlefield.isCardInPlay(card)) {
				return false;
			}
			card.leavesTheBattlefield();			
			battlefield.remove(card);
			removeContiniousEffects(card, Duration.WHILE_UNDER_CONTROL);
			removeContiniousEffects(card, Duration.WHILE_SOURCE_ON_BATTLEFIELD);

			if (to.getGameZone().equals(GameZone.Hand)) {
				returnToItsOwnerHand(card);
				return true;
			}
			if (to.getGameZone().equals(GameZone.Graveyard)) {
				moveToGraveyard(card);
				return true;
			}
			if (to.getGameZone().equals(GameZone.Exile)) {
				exile(card);
				return true;
			}
			if (to.getGameZone().equals(GameZone.Library)) {
				Card copy = CardManager.copyCardWithoutReload(card);
				card.resetCard(copy);
				copy.addAspect(MagicWarsModel.ASPECT_TEMPORARY_CARD);
				MWPlayer player = getPlayerById(card.getOwnerID());
				player.putOnTop(card);
				return true;
			}
			log.error("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
			throw new RuntimeException("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
		} else if (from.getGameZone().equals(GameZone.Exile)) {
			if (exiled.remove(card)) {
				if (to.getGameZone().equals(GameZone.Battlefield)) {
					Card copy = CardManager.copyCardWithoutReload(card);
					card.resetCard(copy);
					card.setController(to.getPlayerId());
					battlefield.addPermanent(card, true);
					copy.addAspect(MagicWarsModel.ASPECT_TEMPORARY_CARD);
					return true;
				}
				log.error("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
				throw new RuntimeException("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
			} else {
				log.error("Couldn't find card in Exile: " + card);
				return false;
			}
		} else if (from.getGameZone().equals(GameZone.Graveyard)) {
			if (to.getGameZone().equals(GameZone.Exile)) {
				return removeCardFromGraveyard(card);
			}
			if (to.getGameZone().equals(GameZone.Battlefield)) {
				Card removed = getGraveyard().remove(card);
				if (removed != null) {
					removed.setController(to.getPlayerId());
					battlefield.addPermanent(removed, true);
					return true;
				} else {
					return false;
				}
			}
			if (to.getGameZone().equals(GameZone.Hand)) {
				if (removeCardFromGraveyard(card)) {
					addRemovedCardToHand(to.getPlayerId(), card);
					return true;
				} else {
					return false;
				}
			}
			if (to.getGameZone().equals(GameZone.Library)) {
				Card removed = getGraveyard().remove(card);
				if (removed != null) {
					Card copy = CardManager.copyCardWithoutReload(removed);
					removed.resetCard(copy);
					copy.addAspect(MagicWarsModel.ASPECT_TEMPORARY_CARD);
					putCardOnTop(removed.getOwner(), removed);
					return true;
				} else {
					return false;
				}
			}
			log.error("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
			throw new RuntimeException("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
		} else if (from.getGameZone().equals(GameZone.Hand)) {
			if (to.getGameZone().equals(GameZone.Battlefield)) {
				boolean bRemoved = getPlayerById(from.getPlayerId()).getHand().remove(card);
				if (bRemoved) {
					card.setController(to.getPlayerId());
					battlefield.addPermanent(card, true);
					if (card.isAura()) {
						card.addAspect(MagicWarsModel.ASPECT_NOT_ATTACHED_AURA);
					}
				}
				return bRemoved;
			} else if (to.getGameZone().equals(GameZone.Graveyard)) {
				boolean bRemoved = getPlayerById(from.getPlayerId()).getHand().remove(card);
				if (bRemoved) {
					_moveToGraveyard(card);
				}
				return bRemoved;
			} else if (to.getGameZone().equals(GameZone.Library)) {
				boolean bRemoved = getPlayerById(from.getPlayerId()).getHand().remove(card);
				if (bRemoved) {
					MWPlayer player = getPlayerById(card.getOwnerID());
					player.putOnTop(card);
				}
				return bRemoved;
			}
			log.error("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
			throw new RuntimeException("moveToZone(), not implemented for: " + from.getGameZone() + "-->" + to.getGameZone());
		} else {
			log.error("moveToZone(), not implemented for: " + from.getGameZone() + "--> Any");
			throw new RuntimeException("moveToZone(), not implemented for: " + from.getGameZone() + "--> Any");
		}

		//return true;
	}

	public CardList filterOutTargetsWithProtection(CardList cardList, SpellAbility sa) {
		CardList filtered = new CardList();

		Card source = sa.getSourceCard();
		for (Card card : cardList) {
			if (card.getKeyword().contains("Protection from black") && source.getColor().contains(Constant.Color.Black)) {
				continue;
			}
			if (card.getKeyword().contains("Protection from red") && source.getColor().contains(Constant.Color.Red)) {
				continue;
			}
			if (card.getKeyword().contains("Protection from green") && source.getColor().contains(Constant.Color.Green)) {
				continue;
			}
			if (card.getKeyword().contains("Protection from white") && source.getColor().contains(Constant.Color.White)) {
				continue;
			}
			if (card.getKeyword().contains("Protection from blue") && source.getColor().contains(Constant.Color.Blue)) {
				continue;
			}
			if (card.getKeyword().contains("Shroud")) {
				continue;
			}
			if (card.getKeyword().contains("OppShroud") && source.getControllerID() != card.getControllerID()) {
				continue;
			}

			filtered.add(card);
		}

		return filtered;
	}

	public boolean checkProtection(Card card, Card source) {
		boolean prevented = false;
		if (card.getKeyword().contains("Protection from black") && source.getColor().contains(Constant.Color.Black)) {
			prevented = true;
		}
		if (card.getKeyword().contains("Protection from red") && source.getColor().contains(Constant.Color.Red)) {
			prevented = true;
		}
		if (card.getKeyword().contains("Protection from green") && source.getColor().contains(Constant.Color.Green)) {
			prevented = true;
		}
		if (card.getKeyword().contains("Protection from white") && source.getColor().contains(Constant.Color.White)) {
			prevented = true;
		}
		if (card.getKeyword().contains("Protection from blue") && source.getColor().contains(Constant.Color.Blue)) {
			prevented = true;
		}

		if (card.getKeyword().contains(MagicWarsModel.KEYWORD_PROLANDS_SA) && source.isLand()) {
			prevented = true;
		}

		if (card.hasAspect(MagicWarsModel.ASPECT_HAS_PROTECTION_FROM_A_TYPE)) {
			for (String keyword : card.getKeyword()) {
				if (keyword.contains("protection from type:")) {
					String type = keyword.split(":")[1].trim();
					if (source.getType().contains(type)) {
						prevented = true;
					}
				}
			}
		}

		return prevented;
	}

	public boolean checkShroud(Card card, Card source) {
		boolean prevented = false;
		if (card.getKeyword().contains(MagicWarsModel.KEYWORD_SHROUD_SA)) {
			prevented = true;
		}
		if (card.getKeyword().contains(MagicWarsModel.KEYWORD_OPPONENT_SHROUD_SA) && card.getControllerID() != source.getControllerID()) {
			prevented = true;
		}

		return prevented;
	}

	public void typesUntilEOT(final Card card, final ArrayList<String> types, final boolean add) {
		for (String type : types) {
			if (add)
				card.addType(type);
			else
				card.removeType(type);
		}

		Command untilEOT = new Command() {
			private static final long serialVersionUID = -3143670156141150487L;
			public void execute() {
				if (getBattlefield().isCardInPlay(card)) {
					for (String type : types) {
						if (add)
							card.removeType(type);
						else
							card.addType(type);
					}
				}
			}
		};

		endOfTurn.addUntil(untilEOT);
	}

	public void addAspectUntilEOT(final Card card, final String aspect) {
		card.addAspect(aspect);
		Command untilEOT = new Command() {
			public void execute() {
				if (getBattlefield().isCardInPlay(card)) {
					card.removeAspect(aspect);
				}
			}

			private static final long serialVersionUID = -6676605179568422886L;
		};
		endOfTurn.addUntil(untilEOT);
	}

	public ArrayList<Command> getActions() {
		return actions;
	}

	public boolean isInTransaction() {
		return transaction;
	}

	public void beginTransaction() {
		this.transaction = true;
	}

	public void endTransaction() {
		this.transaction = false;
	}

	public void winTheGame(int playerId) {
		getPlayerById(playerId).setHaveWonForSomeReason(true);
	}

	public void loseTheGame(int playerId) {
		getPlayerById(playerId).setHaveLostForSomeReason(true);
	}

	public void addThread(int playerId, MWGameThread thread) {
		this.threads.put(playerId, thread);
	}

	public Serializable modal(int playerId, String question, ArrayList<Serializable> values) {
		MWGameThread game = threads.get(playerId);
		if (game == null) {
			return "game wasn't find, possibly wrong player id: " + playerId;
		}
		return game.showModal(playerId, question, values);
	}

	public Serializable getResult() {
		return result;
	}

	public void setResult(Serializable result) {
		this.result = result;
	}

	public void resetResult() {
		this.result = null;
	}

	public boolean noResult() {
		return this.result == null;
	}

	public void lookAtTopAndPutBack(int playerId, int count) {
		Library library = getPlayer(playerId).getLibrary();
		if (library.size() < count) {
			count = library.size();
		}

		Card c;
		CardList cards = new CardList();

		for (int i = 0; i < count; i++) {
			c = library.removeFirst();
			cards.add(c);
		}

		putBack(playerId, cards, count);
	}

	public void putBack(Integer playerId, CardList cards, Integer count) {
		Library library = getPlayer(playerId).getLibrary();
		if (library.size() < count) {
			count = library.size();
		}

		ArrayList<Serializable> cardBeans = new ArrayList<Serializable>();
		for (Card card : cards) {
			cardBeans.add(new CardBean(card));
		}

		CardBean chosen;
		while (count > 0 && cardBeans.size() > 0) {
			chosen = (CardBean) modal(playerId, "Put back, last chosen will be on top", cardBeans);
			cardBeans.remove(chosen);
			for (Card card : cards) {
				if (card.getName().equals(chosen.getName())) {
					library.addFirst(card);
					break;
				}
			}
			count--;
		}
	}

	public Cost getCurrentCost() {
		return currentCost;
	}

	public void setCurrentCost(Cost currentCost) {
		this.currentCost = currentCost;
	}

	/**
	 * This method load rules 1. from rules.properties
	 * 
	 * @see loadRulePlugins
	 */
	public void loadRules(List<MWPlayer> players) {
		rules = new HashMap<Integer, RuleSettingsManager>();
		for (MWPlayer player : players) {
			rules.put(player.getPlayerId(), new RuleSettingsManager());
			player.init();
		}
	}

	/**
	 * This method load rules 2. using cards implemented by groovy script. All such cards should hash supertype "Rule".
	 * 
	 * @see loadRules
	 */
	public void loadRulePlugins() {
		List<String> rulesCardIds = CardManager.getCardFactory().lookupCardBySupertype(MagicWarsModel.CardSuperType.Rule);
		for (String id : rulesCardIds) {
			Card c = CardManager.getCard(id);
			c.entersTheGame();
			c.setOwner(getPlayer1().getPlayerId());
			c.setController(getPlayer1().getPlayerId());
			getExile().add(c);
			c.entersTheBattlefield();
		}
	}

	public RuleSettingsManager getRules(int playerId) {
		return rules.get(playerId);
	}

	public Card copyCard(Card card) {
		return CardManager.copyCard(card);
	}

	public String applyManaCostEffects(SpellAbility sa) {
		ManaCost manaCost = new ManaCost(sa.getManaCost());
		for (ManaCostEffect effect : this.manaCostEffects) {
			try {
				effect.apply(sa, manaCost);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return manaCost.toString();
	}

	public void initGameStateObserver() {
		gameStateObserver = new MWGameStateObserver();
		gameStateObserver.init(this);
	}

	public MWGameStateObserver getGameStateObserver() {
		return gameStateObserver;
	}

	public boolean isUpdateAvailable() {
		return gameStateObserver.getCurrentAtoms().size() > 0;
	}

	public int getUpdateAtomsSize() {
		return gameStateObserver.getCurrentAtoms().size();
	}

	public void resetUpdateObjects() {
		gameStateObserver.resetAtoms();
	}

	/**
	 * Loads card from database. Used in groovy scripted cards. Has restrictions to cards that may call this method.
	 * 
	 * @param creator
	 *            Card that requests for another card to be loaded
	 * @param name
	 *            card to load
	 * @return
	 */
	public Card getCard(Card creator, String name) {
		if (creator == null || !creator.getName().equals("Ancient Garden")) {
			throw new RuntimeException("game.getCard(): this method is restricted.");
		}
		CardFactory cardFactory = CardManager.getCardFactory();
		Card card = cardFactory.getCard(name);
		card.setOwner(creator.getOwnerID());
		card.setController(creator.getController());
		card.setUniqueNumber(getUniqueCardID());
		return card;
	}
	
	public void upkeep() {
		upkeep.executeUpkeepOnce(activePlayerId);
		upkeep.executeUpkeepPlayersTurn(activePlayerId);
		upkeep.executeUpkeepEachTurn(activePlayerId);

		removeTimeCounters();
	}
	
	public void endOfTurn() {
		endOfTurn.executeAt();
		endOfTurn.executeAtEachTurn();
		endOfTurn.executeUntil();
		removeContiniousEffects(Duration.UNTIL_END_OF_TURN);
		removeAllDamageCounters();
		resetRevealedCards();
	}
	
	public void declareBlockers() {
		checkAttackingAlone();
		
		MWPlayer player = getPlayerById(combat.getAttackingPlayerId());
		for (Card attacker : getAttackerList()) {
			player.getGameStatistics().addAttackingCreature(attacker.getTableID());
		}
	}
	
	public void addConstraint(String aspect, Constraint constraint) {
		if (constraints.containsKey(aspect)) {
			List<Constraint> list = constraints.get(aspect);
			list.add(constraint);
		} else {
			List<Constraint> list = new ArrayList<Constraint>();
			list.add(constraint);
			constraints.put(aspect, list);
		}
	}
	
	public void removeConstraint(String aspect, Constraint constraint) {
		if (constraints.containsKey(aspect)) {
			List<Constraint> list = constraints.get(aspect);
			list.remove(constraint);
		}
	}
	
	public List<Constraint> getConstraints(String aspect) {
		return constraints.get(aspect);
	}
	
    public void equipCreature(Card targetCreature, Card equipment) {
    	if (equipment.isEquipping()) {
			Card card = equipment.getEquipping().get(0);
			if (card.equals(targetCreature)) {
				return;
			}
			unequipCreature(card, equipment);
		}
    	targetCreature.attachCard(equipment);
        equipment.equipCard(targetCreature);
        eventManager.getEquipEvent().notifyObservers(targetCreature);
    }
    
    public void unequipCreature(Card card, Card equipment) {
    	equipment.unEquipCard(card);
		card.unattachCard(equipment);
		eventManager.getUnequipEvent().notifyObservers(card);
		removeContiniousEffects(equipment, Duration.WHILE_EQUIPPED);
    }

	public GameState getGameState() {
		return gameState;
	}
	
	public void cast(Card card, GameZone zone) {
		if (card == null) throw new IllegalArgumentException("cast(card,zone): card can't be null.");
		if (zone == null) zone = GameZone.Hand;
		card.updateAspectValue(MagicWarsModel.ASPECT_WAS_CAST_FROM_ZONE, zone);
		int playerId = card.getControllerID();
		MWGameThread gameThread = threads.get(playerId);
		if (gameThread == null) {
			throw new RuntimeException("game wasn't find, possibly wrong player id: " + playerId + ". Card: " + card);
		}
		gameThread.castSpell(playerId, card, zone);
	}
	
	public List<MWPlayer> getPlayers() {
		List<MWPlayer> list = new ArrayList<MWPlayer>();
		list.add(player1);
		list.add(player2);
		return list;
	}

	private int gameID;
	private int priorityPID;
	private int waitingPlayerID;
	private SpellAbility currentSpellAbiility;
	private Cost currentCost;
	private String currentManaColor;
	private ManaCost currentManaCost;
	private MWBattlefield battlefield = new MWBattlefield(this);
	private MWGraveyard graveyard = new MWGraveyard(this);
	private MWExile exiled = new MWExile();
	private MWPlayer player1;
	private MWPlayer player2;
	private MWStack stack = new MWStack();

	private Integer uniqueTableID = 0;
	private Integer uniqueCardID = 0;

	private Combat combat = new Combat();
	private HashMap<Integer, Combat> pwCombatMap = new HashMap<Integer, Combat>();

	private GameZone cardZone;
	private EndOfTurn endOfTurn = new EndOfTurn();
	private EndOfCombat endOfCombat = new EndOfCombat();
	private Upkeep upkeep = new Upkeep();
	private ManaPool globalManaPool = new ManaPool();
	private boolean gameActive;
	private int turnNumber = 1;
	
	private AttackEachTurnEffect attackEachTurnEffect = new AttackEachTurnEffect(this);

	private BlockIfAbleEffect blockIfAbleEffect = new BlockIfAbleEffect(this);

	private ArrayList<GlobalEffect> globalEffects = new ArrayList<GlobalEffect>();	
	private HashMap<ContiniousEffect.Layer, ArrayList<ContiniousEffect>> effects = new HashMap<ContiniousEffect.Layer, ArrayList<ContiniousEffect>>();
	private ArrayList<ManaCostEffect> manaCostEffects = new ArrayList<ManaCostEffect>();

	private Map<Card, ArrayList<Card>> revealedCards = new HashMap<Card, ArrayList<Card>>();
	private ArrayList<Card> viewedCards = new ArrayList<Card>();
	private ArrayList<Card> revealedCardsToPlay = new ArrayList<Card>();

	private ArrayList<Card> madnessCards = new ArrayList<Card>();
	private ArrayList<Card> cascadeCards = new ArrayList<Card>();
	private ArrayList<Card> copiedSpells = new ArrayList<Card>();

	private HashMap<String, String> deckPathList = null;
	private ArrayList<String> deckNameList = null;

	private boolean wasNeedFirstStrikeCombat = false;
	private boolean isNeedFirstStrikeCombat = false;

	private boolean requestTableUpdate = false;

	private EventManager eventManager = new EventManager();

	/**
	 * Place to remember a spell ability. Used to play two spell ability at once (e.g. for Primal Command card)
	 */
	private SpellAbility rememberedSpellAbililty;

	private ArrayList<String> systemMessages = new ArrayList<String>();

	/**
	 * Card names that can't be case (Meddling Mage)
	 */
	private ArrayList<String> cardsThatCantBePlayed = new ArrayList<String>();

	/**
	 * Card colors that can't be cast (Iona, Shield of Emeria)
	 */
	private Map<Integer, List<Color>> cardColorsThatCantBeCast = new HashMap<Integer, List<Color>>();

	/**
	 * Turn owner player ID. Used by cards like Glory of Warfare ("As long as it's your turn") It differs from activePlayer.
	 */
	private int activePlayerId = 0;

	/**
	 * Variable to remember different cards
	 */
	private Card rememberedCard;

	/**
	 * Maps phase number and phase name
	 */
	@SuppressWarnings("serial")
	private static final HashMap<Integer, PhaseName> phaseNumVsPhaseName = new HashMap<Integer, PhaseName>() {
		{
			put(0, PhaseName.blank);
			put(1, PhaseName.untap);
			put(2, PhaseName.upkeep);
			put(3, PhaseName.draw);
			put(4, PhaseName.main1);
			put(5, PhaseName.before_combat);
			put(6, PhaseName.combat_declare_attackers);
			put(7, PhaseName.combat_declare_blockers);
			put(8, PhaseName.combat_predamage_player);
			put(9, PhaseName.combat_predamage_opponent);

			put(10, PhaseName.combat_stack_damage_player);
			put(11, PhaseName.combat_stack_damage_opponent);
			put(12, PhaseName.combat_damage);
			put(13, PhaseName.main2);
			put(14, PhaseName.at_endofturn);
			put(15, PhaseName.endofturn);
			put(16, PhaseName.cleanup);
			put(17, PhaseName.nextturn);
		}
	};

	private boolean transaction = false;

	/**
	 * Current phase name
	 */
	private PhaseName currentPhaseName;
	private int currentPhase;

	/**
	 * Some spells can prevent combat damage
	 */
	private boolean preventCombatDamageThisTurn = false;

	/**
	 * Some spells say "damage can't be prevented"
	 */
	private boolean damageCantBePrevented = false;

	/**
	 * Some spells say "damage can't be prevented this turn"
	 */
	private boolean damageCantBePreventedThisTurn = false;

	/**
	 * Place to store game actions to undo
	 */
	private ArrayList<Command> actions = new ArrayList<Command>();

	/**
	 * Game threads for modal dialogs
	 */
	private HashMap<Integer, MWGameThread> threads = new HashMap<Integer, MWGameThread>();

	/**
	 * Result for modal dialog
	 */
	private Serializable result = null;

	/**
	 * Holds game runtime (that can be changed while a game is in progress) rules for every player
	 */
	private Map<Integer, RuleSettingsManager> rules;

	/**
	 * Looks into game changes and create update patch objects that would be sent to clients later
	 */
	private MWGameStateObserver gameStateObserver;
	
	private Map<String, List<Constraint>> constraints = new HashMap<String, List<Constraint>>();
	
	/**
	 * Holds all game states (moves that can be undone)
	 */
	private GameState gameState = new GameState();
}

