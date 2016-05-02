package EpicsChins.util.tasks;

import EpicsChins.util.Data;
import EpicsChins.util.Method;
import EpicsChins.util.Paint;
import EpicsChins.util.Tiles;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.bot.Context;

/**
 * User: Epics
 * Date: 8/28/12
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThrowChins extends Strategy implements Runnable {

	@Override
	public void run() {
		if (Data.logAttackCode == 0) {
			Context.get().getActiveScript().log.info("Running attack code");
			Data.logAttackCode++;
		}
		Method.checkPrayer();
		Method.checkRenewal();
		Method.checkAntipoison();
		Method.checkRange();
		Method.setQuickOn();
		Method.checkGreegreeState();


		NPC monkeyZombie = NPCs.getNearest(Data.ID_NPC_MONKEY_ZOMBIE);

		Method.onBadTile(Tiles.AREA_CHIN_1);
		Context.get().getActiveScript().log.info("g");
		Method.onBadTile(Tiles.AREA_CHIN_2);
		Context.get().getActiveScript().log.info("h");
		Method.onBadTile(Tiles.AREA_CHIN_3_4);

		Context.get().getActiveScript().log.info("monkeyZombie state" + monkeyZombie);
		Context.get().getActiveScript().log.info("screenState" + monkeyZombie.isOnScreen());
		Context.get().getActiveScript().log.info("calculations" + (Calculations.distanceTo(monkeyZombie) <= 5));
		Context.get().getActiveScript().log.info("playeranim" + Players.getLocal().getAnimation());
		Context.get().getActiveScript().log.info("monkeyanim" + monkeyZombie.getAnimation());

		if (monkeyZombie.isOnScreen() && Calculations.distanceTo(monkeyZombie) <= 5) {
			Context.get().getActiveScript().log.info("ttt");

			monkeyZombie.interact("Attack");

			if(Players.getLocal().getAnimation() != -1){
				Context.get().getActiveScript().log.info("bbb");

				final Timer t = new Timer(5000);

				if (!t.isRunning()) {
					monkeyZombie.interact("Attack");
					Context.get().getActiveScript().log.info("Killing monkeys and nothing is needed. Using antiban...");
					Method.antiban();
					Time.sleep(Random.nextInt(700, 800));
					final Item vial = Inventory.getItem(229);

					if (vial != null) {

						if (vial.getWidgetChild().interact("Drop")) {
							Time.sleep(1500);
						}
						return;
					}
				}
			}

			if (Players.getLocal().getAnimation() == Data.ID_CHIN_THROW) {
				Context.get().getActiveScript().log.info("Chins thrown: " + Paint.chinsThrown);
				Paint.chinsThrown++;

				Context.get().getActiveScript().log.info("Chin number:  " + Data.chinNumber);
				Data.chinNumber--;
			}

			if (monkeyZombie.validate() && monkeyZombie.get().getAnimation() == Data.ID_ANIMATION_DEATH_ZOMBIE) {
				Context.get().getActiveScript().log.info("Kill count: " + Paint.zombieKillCount);
				Paint.zombieKillCount++;
			}

			if (Players.getLocal().getInteracting().equals(monkeyZombie)) {
				final Timer t1 = new Timer(5000);

				while (t1.isRunning() && monkeyZombie.interact("Attack")) {
					Time.sleep(50);
				}
			}

		} else {
			if (!monkeyZombie.isOnScreen()) {
				Context.get().getActiveScript().log.info("Monkey offscreen, rotating towards them!");
				Camera.turnTo(monkeyZombie);
			} else if (monkeyZombie.isOnScreen() && Calculations.distanceTo(monkeyZombie) > 5) {

			}
		}
	}

	@Override
	public boolean validate() {
		int prayPotCountData = 0;

		for (Item y : Inventory.getItems()) {
			for (int x : Data.POT_PRAYER) {
				if (y.getId() == x) {
					prayPotCountData++;
				}
			}
		}
		/*Context.get().getActiveScript().log.info("NPC value: " + String.valueOf(monkeyZombie =NPCs.getNearest(Data.ID_NPC_MONKEY_ZOMBIE)));
		Context.get().getActiveScript().log.info("chinNum: " +String.valueOf(Data.chinNumber));
		Context.get().getActiveScript().log.info("atDest: " + String.valueOf(Data.atDestination));
		*/
		return Game.isLoggedIn() && Data.chinNumber >= 200 && prayPotCountData > 0 && Data.START_SCRIPT && !Data.runCheck && Data.atDestination;
	}
}

