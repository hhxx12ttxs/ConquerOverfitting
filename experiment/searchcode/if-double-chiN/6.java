package EpicsChins.util;

import org.powerbot.game.api.methods.*;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Prayer;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.methods.widget.Lobby;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.interactive.Player;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;

/**
 * User: Epics
 * Date: 8/28/12
 * Time: 8:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Method {
	public static boolean isPoisoned() {

		return Game.isLoggedIn() && Settings.get(102) != 0;
	}

	public static void changeWorlds() {

		Context.get().getActiveScript().log.info("Running changeWorlds code");
		Context.get().getActiveScript().log.info(String.valueOf(Game.logout(true)));

		if (Game.logout(true)) {
			Time.sleep(Random.nextInt(2000, 5000));

			if (Lobby.isOpen() && Game.getClientState() != Lobby.STATE_LOBBY_IDLE) {
				final int randomWorld = Data.WORLDS_MEMBER[Random.nextInt(0, Data.WORLDS_MEMBER.length) - 1];
				Context.setLoginWorld(randomWorld);

				Time.sleep(Random.nextInt(200, 400));

				if (Game.isLoggedIn()) {
					Prayer.setQuick();
				}
			}
		}
	}

	public static void checkRun() {

		if (Walking.getEnergy() > 30) {
			Walking.setRun(true);
		}
	}

	public static void checkPrayer() {

		if (Prayer.getPoints() <= 250) {
			final Item prayerPot = Inventory.getItem(Data.POT_PRAYER);

			if (prayerPot != null && prayerPot.getWidgetChild().interact("Drink")) {
				final int id = prayerPot.getId();
				final int count = Inventory.getCount(id);
				final Timer t = new Timer(2500);

				while (t.isRunning() && Inventory.getCount(id) == count) {
					Time.sleep(50);
				}
			}
		}
	}

	public static void checkRenewal() {

		if (Data.t == null || !Data.t.isRunning()) {
			drinkRenewal();
			Data.t = new Timer(300000);

		} else {
			Data.t.reset();
		}
	}

	public static void checkAntipoison() {

		final Item antipoisonAllItem = Inventory.getItem(GUI.antipoisonUser);

		if (antipoisonAllItem != null && isPoisoned() && antipoisonAllItem.getWidgetChild().interact("Drink")) {
			Context.get().getActiveScript().log.info("Running doDrinkAntipoison code");
			final int id = antipoisonAllItem.getId();
			final int count = Inventory.getCount(id);
			final Timer t = new Timer(2500);

			while (t.isRunning() && Inventory.getCount(id) == count) {
				Time.sleep(50);
			}
		}
	}

	public static void checkRange() {
		final Item rangeFlaskItem = Inventory.getItem(Data.FLASK_RANGING);
		final Item rangeFlaskExtremeItem = Inventory.getItem(Data.FLASK_RANGING_EXTREME);
		final int realRange = Skills.getRealLevel(Skills.RANGE);
		final int pottedRange = Skills.getLevel(Skills.RANGE);
		final int rangeDifference = pottedRange - realRange;

		if (rangeFlaskItem != null && rangeDifference <= 3 && rangeFlaskItem.getWidgetChild().interact("Drink")) {
			final int id = rangeFlaskItem.getId();
			final int count = Inventory.getCount(id);
			final Timer t = new Timer(2500);
			Data.outOfRangePots = false;

			while (t.isRunning() && Inventory.getCount(id) == count) {
				Time.sleep(50);
			}

		} else if (rangeFlaskExtremeItem != null && rangeDifference <= 3 && rangeFlaskExtremeItem.getWidgetChild().interact("Drink")) {
			final int id = rangeFlaskExtremeItem.getId();
			final int count = Inventory.getCount(id);
			final Timer t = new Timer(2500);
			Data.outOfRangePots = false;

			while (t.isRunning() && Inventory.getCount(id) == count) {
				Time.sleep(50);
			}
		} else if (rangeFlaskItem == null && rangeFlaskExtremeItem == null) {
			Context.get().getActiveScript().log.info("We're out of ranging pots, resuming until prayer potions are gone!");
			Data.outOfRangePots = true;
		}
	}

	private static void drinkRenewal() {

		final Item prayerRenewal = Inventory.getItem(Data.FLASK_PRAYER_RENEWAL);

		if (prayerRenewal != null && prayerRenewal.getWidgetChild().interact("Drink")) {
			final int id = prayerRenewal.getId();
			final int count = Inventory.getCount(id);
			final Timer t = new Timer(2500);

			while (t.isRunning() && Inventory.getCount(id) == count) {
				Time.sleep(50);
			}
		}
	}

	public static void breakTab() {
		Context.get().getActiveScript().log.info("Running doBreakTab code");
		final Item tabItem = Inventory.getItem(Data.TAB_VARROCK);

		if (tabItem != null && tabItem.getWidgetChild().interact("Break")) {
			final int id = tabItem.getId();
			final int count = Inventory.getCount(id);
			final Timer t = new Timer(2500);

			while (t.isRunning() && Inventory.getCount(id) == count) {
				Time.sleep(50);
			}
		}
	}

	private static void equipGreegree() {
		final Item greegree = Inventory.getItem(Data.GREEGREE_IDS);

		if (greegree != null) {
			final int ID = greegree.getId();
			final int COUNT = Inventory.getCount(ID);

			if (greegree.getWidgetChild().click(true)) {
				final Timer t = new Timer(2500);

				while (t.isRunning()) {

					if (Inventory.getCount(ID) == COUNT) {
						return;
					}
					Time.sleep(50);
				}
			}
		}
	}

	public static void utilizeGreegree() {

		if (Tiles.AREA_APE_ATOLL.contains(Players.getLocal().getLocation()) && Data.usingGreegree) {
			Method.checkRun();
			Method.equipGreegree();
		}

		if (Tiles.AREA_APE_ATOLL.contains(Players.getLocal().getLocation()) && !Data.usingGreegree) {
			Method.checkRun();
		}
	}

	public static boolean areaContainsTwoOrMore(final Area area) {
		final Player[] PLAYERS_IN_AREA = Players.getLoaded(new Filter<Player>() {
			@Override
			public boolean accept(Player t) {
				return area.contains(t) && !t.getName().equals(Players.getLocal().getName());
			}
		});
		return Game.getClientState() != Game.INDEX_MAP_LOADING && PLAYERS_IN_AREA.length >= 2;
	}

	public static boolean onBadTile(final Area area) {
		final Player[] PLAYERS_IN_AREA = Players.getLoaded(new Filter<Player>() {
			@Override
			public boolean accept(Player t) {

				return area.contains(t) && !t.getName().equals(Players.getLocal().getName());
			}
		});
		NPC[] monkies = NPCs.getLoaded(new Filter<NPC>() {
			@Override
			public boolean accept(final NPC n) {
				return n.getId() == NPCs.getNearest(Data.ID_NPC_MONKEY_ZOMBIE).getId() && area.contains(n);
			}
		});

		if (PLAYERS_IN_AREA != null) {
			for (Player p : PLAYERS_IN_AREA) {
				if (!p.getLocation().equals(Players.getLocal().getLocation()) && monkies != null) {
					Tile tile = p.getLocation();

					Walking.walk(tile);
				}
			}
		}
		return false;
	}

	private static void yesInterfaceClicker() {
		final WidgetChild yesInterface = Widgets.get(1188, 3);

		if (yesInterface != null && yesInterface.click(true)) {
			final Timer yesInterfaceTimer = new Timer(2500);

			while (yesInterfaceTimer.isRunning()) {
				Time.sleep(50);
			}
			Time.sleep(2000);
		}
	}

	public static void preEat() {
		final Item food = Inventory.getItem(GUI.foodUser);
		final Item prayerPot = Inventory.getItem(Data.POT_PRAYER_DOSE_4);

		if (Players.getLocal().getHpPercent() < 30) {
			Context.get().getActiveScript().log.info("HP is low, pre-eat triggered");

			if (Tiles.TILE_GRAND_BANK.validate()) {
				Method.checkRun();
				Walking.findPath(Tiles.TILE_GRAND_BANK).traverse();
			}

			if (Tiles.TILE_GRAND_BANK.isOnScreen()) {
				Camera.turnTo(Tiles.TILE_GRAND_BANK);
			}

			if (!Bank.isOpen()) {

				if (Tiles.TILE_GRAND_BANK != null && Calculations.distanceTo(Tiles.TILE_GRAND_BANK) >= 4) {
					Walking.findPath(Tiles.TILE_GRAND_BANK).traverse();
					runState();
					Camera.turnTo(Tiles.TILE_GRAND_BANK);
					Players.getLocal().isMoving();

				} else if (Calculations.distanceTo(Tiles.TILE_GRAND_BANK) <= 8) {
					Bank.open();

				} else if (Inventory.getCount(food.getId()) > 0) {
					food.getWidgetChild().interact("Eat");
					Time.sleep(Random.nextInt(300, 400));
					return;
				}
				return;
			}

			if (Bank.isOpen()) {

				if (Inventory.isFull()) {
					Bank.deposit(Data.POT_PRAYER_DOSE_4, 3);
				}

				if (!Inventory.isFull() && Bank.withdraw(food.getId(), 3)) {
					Time.sleep(200, 400);
				}

				if (Inventory.getCount(food.getId()) == 3) {
					Bank.close();
				}
				return;
			}

			if (Bank.isOpen() && Players.getLocal().getHpPercent() > 80) {
				Bank.deposit(prayerPot.getId(), 5);

				if (Inventory.getCount(prayerPot.getId()) == 0) {
					Bank.getNearest();
					Bank.open();

					if (Bank.isOpen()) {

						if (Bank.withdraw(food.getId(), 2)) {
							Time.sleep(200, 300);
						}
						Bank.withdraw(prayerPot.getId(), 3);
					}
				}
			}
		}
	}

	public static void isInGE() {

		if (Tiles.TILE_GRAND_TREE.validate() && Tiles.AREA_GE.contains(Players.getLocal().getLocation())) {
			Walking.findPath(Tiles.TILE_GRAND_TREE).traverse();
			runState();
		}
	}


	public static void isInGnome() {

		if (Tiles.TILE_TREE_DOOR.validate() && Tiles.AREA_GNOME_STRONGHOLD.contains(Players.getLocal().getLocation())) {
			Walking.findPath(Tiles.TILE_TREE_DOOR).traverse();
			runState();
		}
	}

	public static void checkNoobTele() {

		if (Tiles.AREA_GRAND_TELE.contains(Players.getLocal().getLocation())) {
			Context.get().getActiveScript().log.info("You have the n00b varrock teleport. Walking to bank");

			if (Tiles.TILE_GRAND_BANK.validate()) {

				if (Calculations.distanceTo(Tiles.TILE_GRAND_BANK) >= 2) {
					Walking.findPath(Tiles.TILE_GRAND_BANK).traverse();
					checkRun();
					runState();

				}
				return;

			} else {

				if (Tiles.TILE_GRAND_TREE.validate() && Tiles.AREA_GE.contains(Players.getLocal().getLocation())) {
					Tile nextTile = Walking.newTilePath(Tiles.PATH_TO_BANK_FROM_TELE).getNext();
					Walking.walk(nextTile);
					runState();
				}

			}

			if (!Tiles.TILE_GRAND_BANK.validate()) {
				final Tile nextTile = Walking.newTilePath(Tiles.PATH_TO_BANK_TILE).randomize(2, 4).getNext();

				Walking.walk(nextTile);

				if (!Tiles.TILE_GRAND_BANK.validate() && Calculations.distanceTo(Tiles.TILE_GRAND_BANK) >= 2) {
					Walking.findPath(Tiles.TILE_GRAND_BANK).traverse();
					checkRun();
					runState();
				}
				return;
			}
			Camera.turnTo(Tiles.TILE_GRAND_BANK);
			Context.get().getActiveScript().log.info("You aren't in the Grand Exchange or the n00b teleport zone! Shutting down...");
			Context.get().getActiveScript().stop();
		}
	}

	public static void chargePrayer() {

		if (prayerIsLow()) {

			if (Data.logWalkCode == 1) {
				Context.get().getActiveScript().log.info("Prayer is low, let's go charge up before we head out.");
				Data.logWalkCode++;
			}

			if (Tiles.AREA_GE.contains(Players.getLocal().getLocation())) {
				final Tile nextTile = Walking.newTilePath(Tiles.PATH_TO_PRAYER_FROM_GE).randomize(2, 4).getNext();

				if (Walking.walk(nextTile) && Walking.getDestination() == nextTile) {
					runState();
				}

			} else if (Tiles.AREA_GRAND_TELE.contains(Players.getLocal().getLocation())) {
				final Tile nextTile = Walking.newTilePath(Tiles.PATH_TO_PRAYER_FROM_TELE).randomize(2, 4).getNext();
				Walking.walk(nextTile);
				runState();

			} else if (!Tiles.AREA_GRAND_TELE.contains(Players.getLocal().getLocation()) && !Tiles.AREA_GE.contains(Players.getLocal().getLocation())) {
				final Tile nextTile = Walking.newTilePath(Tiles.PATH_TO_PRAYER_FROM_GE).randomize(2, 4).getNext();

				if (nextTile != null) {
					Walking.walk(nextTile);
					runState();

				} else {
					Context.get().getActiveScript().log.severe("OH MY GOD LAGG");
				}
			}

			if (Tiles.TILE_PRAYER.validate() && Calculations.distanceTo(Tiles.TILE_PRAYER) <= 5) {
				final SceneObject varrockAltar = SceneEntities.getNearest(Data.ID_ALTAR_VARROCK);

				if (varrockAltar != null && varrockAltar.isOnScreen()) {
					varrockAltar.click(true);

					if (Players.getLocal().getAnimation() == Data.ID_ANIMATION_PRAY) {
						Time.sleep(100, 400);

						if (Prayer.getPoints() == (Skills.getRealLevel(Skills.PRAYER) * 10)) {
							Context.get().getActiveScript().log.info("All charged up, let's get going.");

							if (!Tiles.TILE_GRAND_TREE.validate() && Calculations.distanceTo(Tiles.TILE_GRAND_TREE) >= 10) {
								final
								Tile
										nextTile =
										Walking.newTilePath(Tiles.PATH_TO_PRAYER_FROM_GE).randomize(2,
												                                                           4).reverse().getNext();
								Walking.walk(nextTile);
								runState();

							} else {
								Walking.findPath(Tiles.TILE_GRAND_TREE).traverse();
								runState();
							}
						}
					}
				} else {
					Camera.turnTo(varrockAltar);
				}
			}
		}
	}

	public static void openTreeDoor(final SceneObject se) {

		if (se != null && Calculations.distanceTo(Tiles.TILE_TREE_DOOR) <= 10 && se.isOnScreen() && !Tiles.TILE_INSIDE_TREE_DOOR.equals(Players.getLocal().getLocation()) && !Tiles.TILE_INSIDE_TREE_DOOR_2.equals(Players.getLocal().getLocation()) && se.interact("Open")) {
			Camera.turnTo(se);
			final Timer t = new Timer(2500);

			while (se.validate() && t.isRunning()) {
				Time.sleep(50);
			}
		}
	}

	public static void climbLadder(final SceneObject se) {

		if (Tiles.TILE_INSIDE_TREE_DOOR.equals(Players.getLocal().getLocation()) || Tiles.TILE_INSIDE_TREE_DOOR_2.equals(Players.getLocal().getLocation())) {

			if (se != null && Players.getLocal().getAnimation() == -1 && se.interact("Climb-up")) {
				final Timer t = new Timer(2500);

				while (se.validate() && t.isRunning()) {
					Time.sleep(50);
				}

			} else {

				if (se == null) {
					Context.get().getActiveScript().log.severe("LADDER IS NULL!!");
				}

				if (se != null && !se.isOnScreen()) {
					Camera.turnTo(se);
				}
			}
		}
	}

	public static void doYesInteraction(final Area area, final NPC npc) {

		if (area.contains(Players.getLocal().getLocation())) {
			Walking.findPath(npc.getLocation()).traverse();
			runState();

			if (npc.isOnScreen() && npc.interact("Travel")) {
				Time.sleep(1000);
				final Timer t = new Timer(2500);

				while (npc.validate() && t.isRunning()) {
					Method.yesInterfaceClicker();
					Time.sleep(1000);
				}

			} else if (!npc.isOnScreen()) {
				Camera.turnTo(npc);
			}
		}
	}

	public static void doYesInteractionDaero() {

		if (Tiles.AREA_GNOME_LEVEL_ONE.contains(Players.getLocal().getLocation())) {
			NPC daero = NPCs.getNearest(Data.ID_NPC_DAERO);

			Walking.findPath(daero.getLocation()).traverse();
			runState();

			if (daero.isOnScreen() && daero.interact("Travel")) {
				Time.sleep(1000);
				final Timer t = new Timer(2500);

				while (t.isRunning()) {
					Method.yesInterfaceClicker();
					Time.sleep(1000);
				}

			} else if (!daero.isOnScreen()) {
				Camera.turnTo(daero);
			}
		}
	}

	public static void clickSpiritTree(final Area area, final SceneObject se) {

		if (area.contains(Players.getLocal().getLocation())) {

			if (se != null && se.validate() && se.interact("Teleport")) {

				while (Players.getLocal().getAnimation() == Data.ID_ANIMATION_TREE || Players.getLocal().getAnimation() == Data.ID_ANIMATION_TREE_2) {
					Time.sleep(500);
				}
				final Timer spiritTreeTimer = new Timer(2500);

				while (se.validate() && spiritTreeTimer.isRunning()) {
					Time.sleep(50);
				}

			} else {

				if (se != null && !se.isOnScreen()) {
					Camera.turnTo(se);
					Time.sleep(500);
				}
			}
		}
	}

	public static void clickGnomeInterface() {
		final WidgetChild spiritTreeInterface = Widgets.get(864, 6).getChild(0);

		if (spiritTreeInterface != null && spiritTreeInterface.click(true)) {
			final Timer tt = new Timer(2500);

			while (tt.isRunning()) {
				Time.sleep(50);
			}

			if (Players.getLocal().getAnimation() == Data.ID_ANIMATION_TREE || Players.getLocal().getAnimation() == Data.ID_ANIMATION_TREE_2) {
				Time.sleep(500);
			}
		}
	}


	public static void interactAndWalkToLadder() {
		if (Tiles.AREA_APE_ATOLL.contains(Players.getLocal().getLocation())) {

			if (Calculations.distanceTo(Tiles.TILE_APE_LADDER_TOP) <= 2) {
				Data.runCheck = true;
			}

			if (Walking.findPath(Tiles.TILE_APE_LADDER_TOP).traverse()) {
				runState();
			}
			final SceneObject apeAtollLadder = SceneEntities.getNearest(Data.ID_LADDER_APE);

			if (apeAtollLadder != null && apeAtollLadder.isOnScreen() && Calculations.distanceTo(Tiles.TILE_APE_LADDER_TOP) <= 5 && apeAtollLadder.interact("Climb-down")) {
				Timer t = new Timer(2500);
				while (apeAtollLadder.validate() && t.isRunning()) {
					Time.sleep(50);
				}

			}

			if (apeAtollLadder != null && !apeAtollLadder.isOnScreen()) {
				Camera.turnTo(apeAtollLadder);
			}
		}
	}

	public static void setQuickOn() {

		if (!Prayer.isQuickOn()) {
			Prayer.toggleQuick(true);
			final Timer t = new Timer(2500);

			while (!Prayer.isQuickOn() && t.isRunning()) {
				Time.sleep(500);
			}
		}

		if (Prayer.isQuickOn() && !Data.prayerSetCorrectly) {

			if (Prayer.isQuickOn() && Data.prayerSetCorrectly) {
				Prayer.getActive();

				if (Players.getLocal().getPrayerIcon() == Prayer.Curses.DEFLECT_MELEE.getId() || Players.getLocal().getPrayerIcon() == Prayer.Normal.PROTECT_FROM_MELEE.getId()) {
					Context.get().getActiveScript().log.info("You set up your quick-prayers correctly, good going!");
					Data.prayerSetCorrectly = true;

				} else if (!(Players.getLocal().getPrayerIcon() == Prayer.Curses.DEFLECT_MELEE.getId()) || !(Players.getLocal().getPrayerIcon() == Prayer.Normal.PROTECT_FROM_MELEE.getId())) {
					Context.get().getActiveScript().log.info("You moron! You forgot to set deflect melee! Getting outta hurr..");
					breakTab();
					Game.logout(true);
					Context.get().getActiveScript().stop();
				}
			}
		}
	}

	public static boolean prayerIsLow() {
		final double prayerPoints = (Prayer.getPoints());
		final double prayerMinimum = ((Skills.getLevel(Skills.PRAYER) * 10) * 0.5);

		return prayerPoints <= prayerMinimum;
	}

	public static void watchHp() {
		final WidgetChild hpWidget = Widgets.get(748, 8);
		final String currentHpString = hpWidget.getText();
		final double hpPointsTotal = (Skills.getRealLevel(Skills.CONSTITUTION) * 10);
		final double hpMinimum = (hpPointsTotal / 2.5);
		final double currentHp = Integer.parseInt(currentHpString);

		if (currentHp < hpMinimum) {
			final Item food = Inventory.getItem(GUI.foodUser);

			if (Inventory.getCount(food.getId()) > 0) {

				if (food.getWidgetChild().interact("Eat")) {
					final int id = food.getId();
					final int count = Inventory.getCount(id);
					final Timer t = new Timer(2500);

					while (t.isRunning() && Inventory.getCount(id) == count) {
						Time.sleep(50);
					}
				}
				Time.sleep(Random.nextInt(300, 400));

			} else {
				Context.get().getActiveScript().log.severe("NO FOOD AND LOW HP, SHUTTING DOWN!");
				breakTab();
				Game.logout(true);
				Context.get().getActiveScript().stop();
			}
		}
	}

	public static void runState() {
		final Timer t = new Timer(2500);

		while (Players.getLocal().isMoving() && t.isRunning()) {
			antiban();
			Time.sleep(50);
		}
	}

	public static void chinRunState() {
		final Timer t = new Timer(2500);

		while (t.isRunning() && Players.getLocal().isMoving()) {
			t.reset();
			Method.watchHp();
			Method.checkAntipoison();
			Method.checkPrayer();
			Method.checkRenewal();
			Method.checkRun();
			Method.antiban();
		}
	}

	public static void checkGreegreeState() {

		if (Data.usingGreegree) {
			final Item chin = Inventory.getItem(10034);

			if (Inventory.contains(chin.getId())) {

				chin.getWidgetChild().click(true);
			} else {
				Context.get().getActiveScript().log.info("You're good scuba steve!");

			}
		} else {
			Context.get().getActiveScript().log.info("Not using greegree");
		}
	}

	public static void walkPath(Tile[] path) {

		if (Game.getClientState() == 12) {
			Time.sleep(200);
		}

		for (int i = path.length - 1; i >= 0; i--) {

			if (path[i].isOnMap() && path[i].canReach()) {
				Mouse.click(path[i].getMapPoint(), true);
				final Timer timer = new Timer(1500);

				if (Players.getLocal().isMoving()) {
					Method.chinRunState();
				}

				if (timer.isRunning() && Calculations.distanceTo(path[i]) > 5) {
					Time.sleep(100);
					Method.chinRunState();
				}
				break;
			}
		}
	}

	public static void antiban() {
		final int RANDOM_PITCH = Random.nextInt(350, 10);
		final int RANDOM_ANGLE = Random.nextInt(89, 50);
		int state = Random.nextInt(0, 3);

		switch (state) {
			case 1:
				Timer t = new Timer(5000);

				if (!t.isRunning()) {
					Camera.setAngle(RANDOM_ANGLE);
					Camera.setPitch(RANDOM_PITCH);
					t.reset();
				}
				break;
			case 2:
				WidgetChild c = Widgets.get(1213).getChild(12);

				if (c.validate()) {
					c.hover();
					Time.sleep(1000);
				}
				break;
			case 3:
				WidgetChild d = Widgets.get(1213).getChild(14);

				if (d.validate()) {
					d.hover();
					Time.sleep(1000);
					break;
				}
		}
	}
}

