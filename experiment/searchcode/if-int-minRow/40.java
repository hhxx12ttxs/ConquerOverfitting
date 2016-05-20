package de.ggj14.wap.common.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.carrotsearch.hppc.cursors.ObjectIntCursor;
import com.thoughtworks.xstream.mapper.OuterClassMapper;

import de.ggj14.wap.common.CommonValues;
import de.ggj14.wap.common.communication.GameStateTransferObject;
import de.ggj14.wap.common.communication.MoveAction;
import de.ggj14.wap.common.datamodel.conversation.Conversation;
import de.ggj14.wap.common.datamodel.conversation.ConversationState;
import de.ggj14.wap.common.gamefield.GameField;
import de.ggj14.wap.common.gamefield.objects.Minion;

public class GameState {
	/**
	 * The maximum number of seconds on the countdown (it may not have started
	 * in this case)
	 */
	public static final int COUNTDOWN_MAX = 5;
	private int maxID;
	private long time;
	private Map<Integer, Conversation> idConversationMap;
	private Map<Integer, Conversation> minionIDConversationMap;
	private Map<Integer, Minion> minionMap;
	private GameField gameField;
	private int startCountdown;
	private transient boolean isGlobalState;

	public GameState() {
		minionMap = new HashMap<Integer, Minion>();
		minionIDConversationMap = new HashMap<Integer, Conversation>();
		idConversationMap = new HashMap<Integer, Conversation>();
		gameField = new GameField();
		time = 0;
		maxID = 0;
	}

	public void init(int rows, int columns, int minions, boolean isGlobalState) {
		this.isGlobalState = isGlobalState;
		gameField.init(rows, columns);
		
		Minion p1Prophet = new Minion(0, 75, 75, MinionType.PROPHET);
		p1Prophet.setFaction(Faction.PLAYER1);
		this.minionMap.put(p1Prophet.getId(), p1Prophet);

		Minion p2Prophet = new Minion(1, 1005, 645, MinionType.PROPHET);
		p2Prophet.setFaction(Faction.PLAYER2);
		this.minionMap.put(p2Prophet.getId(), p2Prophet);
		
		maxID = 2;
		
		// start at 2 because prophets are minions as well
		for (int i = 2; i < minions; i++) {
			Minion m = new Minion();
			do
			{
				m.fillMinionRandom(maxID, rows, columns);
			} while(p1Prophet.distanceToMinion(m) < 360 || p2Prophet.distanceToMinion(m) < 360);
			this.minionMap.put(m.getId(), m);
			maxID++;
		}
		this.startCountdown = COUNTDOWN_MAX;
	}

	public void update(int timeDelta) {
		for (Minion outerMinion : minionMap.values()) {
			outerMinion.update(timeDelta, gameField.getColumns(), gameField.getRows(), isGlobalState);
			int minRow = (int) (outerMinion.getY() / CommonValues.HEIGHT_OF_MAP_ELEMENT);
			int minCol = (int) (outerMinion.getX() / CommonValues.WIDTH_OF_MAP_ELEMENT);
			gameField.getGameFieldElement(minRow, minCol).incrementFactionMapElement(outerMinion);
			
			checkForConversationsOf(outerMinion);
		}

		// update conversations
		if (isGlobalState) {
			Iterator<Conversation> it = idConversationMap.values().iterator();
			while (it.hasNext()) {
				Conversation conv = it.next();
				ConversationState cs = conv.update(timeDelta);
				if (cs != ConversationState.BATTLING) {
					finalizeConversation(conv, cs.getCorrespondingFaction());
					it.remove();
				}
			}
		}
		gameField.update(timeDelta);
		this.time += timeDelta;
	}
	
	/**
	 * 
	 * @param minion
	 */
	private void checkForConversationsOf(Minion minion) {
		// check for colliding minions if they are not grey and not
		// invulnerable
		if (isGlobalState 
				&& !minionIDConversationMap.containsKey(minion.getId())	// if the minion is part of the conversation, nothing has to be done
				&& !minion.isInvulnerable(this.time) ) {
			// after outerminion was updated we want to check its position in
			// regard to other minions
			for (Minion innerMinion : minionMap.values()) {
				// search for nearby minions to talk to
				if (minion.getId() != innerMinion.getId() 
						&& !innerMinion.isInvulnerable(this.time)
//						&& !innerMinion.getMinionType().equals(MinionType.PROPHET)
						&& minion.getFaction() != innerMinion.getFaction()
						&& minion.distanceToMinion(innerMinion) < 60.0) {
	
					// check whether the colliding minion is part of a conversation
					Conversation conv = null;
					if (minionIDConversationMap.containsKey(innerMinion.getId())) {
						conv = minionIDConversationMap.get(innerMinion.getId());
					}
					
					// check for prophet stuff
					if(minion.getMinionType() == MinionType.PROPHET					// if I'm a prophet
						&& (innerMinion.getMinionType() == MinionType.PROPHET		// and (my enemy is a prophet
							|| (conv != null && conv.hasProphet()))					// or the conversation I want to join has a prophet)
						) {
						continue;
					}
	
					// no conversation yet
					if(conv == null) {
						Conversation newConv = new Conversation(maxID);
						idConversationMap.put(newConv.getId(), newConv);
						maxID++;
	
						if (newConv.addParticipant(innerMinion)) {
							minionIDConversationMap.put(innerMinion.getId(), newConv);
						}
	
						if (newConv.addParticipant(minion)) {
							minionIDConversationMap.put(minion.getId(), newConv);
						}
						// a conversation was found so we could leave
						return;
					} else { // if (conv != null) which means that the other minion already is part of a conversation
						if (conv.addParticipant(minion)) {
							minionIDConversationMap.put(minion.getId(), conv);
						}
						// a conversation was found so we could leave
						return;
					}
				}
			}
		}
	}

	private void finalizeConversation(Conversation conv, Faction winningFaction) {
		final long endpointOfInvulnerability = time + CommonValues.TIME_OF_INVULNERABILITY;
		for (Minion min : conv.getParticipants()) {
			min.setInvulnerableEndpoint(endpointOfInvulnerability);
			min.moveAgain();
			minionIDConversationMap.remove(min.getId());
			if (min.getFaction() != winningFaction) {
				min.setFaction(winningFaction);
			}
		}
	}

	public Faction checkWinningCondition() {
		Faction f = Faction.GREY;
		for (ObjectIntCursor<Faction> factionCount : gameField.getGameFieldFactionCountMap()) {
			if (factionCount.value >= CommonValues.getNumberOfFieldsToWin()) {
				f = factionCount.key;
			}
		}

		return f;
	}

	public GameStateTransferObject getGameStateTransferObject() {
		GameStateTransferObject gsto = new GameStateTransferObject();
		gsto.init(time, startCountdown, new ArrayList<Minion>(minionMap.values()), new ArrayList<Conversation>(
				idConversationMap.values()), gameField);
		return gsto;
	}

	public void doMoveAction(MoveAction ma) {
		setMinionTargetPosition(ma.getObjectID(), ma.getTargetX(), ma.getTargetY());
	}

	public static GameState fromGameStateTransferObject(GameStateTransferObject gsto) {
		GameState gs = new GameState();

		gs.setGameField(gsto.getGameField());

		// build up the minion map
		Map<Integer, Minion> minionMap = new HashMap<Integer, Minion>();
		for (Minion m : gsto.getMinionList()) {
			minionMap.put(m.getId(), m);
		}
		gs.setMinions(minionMap);

		// build up the conversation maps
		Map<Integer, Conversation> idConversationMap = new HashMap<Integer, Conversation>();
		Map<Integer, Conversation> minionConvesationMap = new HashMap<Integer, Conversation>();
		if (gsto.getConversationList() != null) {
			for (Conversation conv : gsto.getConversationList()) {
				idConversationMap.put(conv.getId(), conv);
				for (Minion min : conv.getParticipants()) {
					minionConvesationMap.put(min.getId(), conv);
				}
			}
		}
		gs.setIdConversationMap(idConversationMap);
		gs.setMinionIDConversationMap(minionConvesationMap);

		gs.setStartCountdown(gsto.getStartCountdown());
		gs.setTime(gsto.getTime());

		return gs;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Map<Integer, Minion> getMinions() {
		return minionMap;
	}

	public void setMinions(Map<Integer, Minion> minions) {
		this.minionMap = minions;
	}

	public void setMinionTargetPosition(int minionID, int targetX, int targetY) {
		if (!(targetX < 0 || targetY < 0
				|| targetX >= (CommonValues.WIDTH_OF_MAP_ELEMENT * gameField.getColumns()) 
				|| targetY >= (CommonValues.HEIGHT_OF_MAP_ELEMENT * gameField.getRows()))) {
			Minion m = minionMap.get(minionID);
			m.setTargetPosX(targetX);
			m.setTargetPosY(targetY);
		}
	}

	public GameField getGameField() {
		return gameField;
	}

	public void setGameField(GameField gameField) {
		this.gameField = gameField;
	}

	/**
	 * @return The amount of seconds left until the game starts - if this value
	 *         is {@value #COUNTDOWN_MAX}, the countdown may not have started
	 *         yet
	 */
	public int getStartCountdown() {
		return startCountdown;
	}

	public void setStartCountdown(int startCountdown) {
		this.startCountdown = startCountdown;
	}

	public void print() {
		System.out.println("Objects Created	: " + maxID);
		System.out.println("Time			: " + time);
		gameField.print();

		for (Minion m : minionMap.values()) {
			m.print();
		}
	}

	public int getMaxID() {
		return maxID;
	}

	public void setMaxID(int maxID) {
		this.maxID = maxID;
	}

	public Map<Integer, Conversation> getIdConversationMap() {
		return idConversationMap;
	}

	public void setIdConversationMap(Map<Integer, Conversation> idConversationMap) {
		this.idConversationMap = idConversationMap;
	}

	public Map<Integer, Conversation> getMinionIDConversationMap() {
		return minionIDConversationMap;
	}

	public void setMinionIDConversationMap(Map<Integer, Conversation> minionIDConversationMap) {
		this.minionIDConversationMap = minionIDConversationMap;
	}

	public Map<Integer, Minion> getMinionMap() {
		return minionMap;
	}

	public void setMinionMap(Map<Integer, Minion> minionMap) {
		this.minionMap = minionMap;
	}

	public boolean isGlobalState() {
		return isGlobalState;
	}

	public void setGlobalState(boolean isGlobalState) {
		this.isGlobalState = isGlobalState;
	}

	public static int getCountdownMax() {
		return COUNTDOWN_MAX;
	}
}
