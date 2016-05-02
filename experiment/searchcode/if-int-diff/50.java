package me.fonkfader.EmeraldEconLink;

import java.util.Collections;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Checks extends JavaPlugin implements Listener {

	private static EmeraldEconLink plugin;

	@SuppressWarnings("static-access")
	public Checks(EmeraldEconLink plugin) {

		this.plugin = plugin;
	}

	public static int task1;
	public static int task2;
	public static int task3;
	public static int task4;
	public static int canceltask = 0;

	public static void synctask() {

		task1 = plugin.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, new Runnable() {

					public void run() {
						Player players[] = plugin.getServer()
								.getOnlinePlayers();
						if (canceltask == 0) {
							for (Player p : players)
								Checks.compare(p);
						}

					}
				}, 1l, Configuration.tiksPerChecksForEnableItemOnEarnMoney);

	}

	public static void compareOnCommand() {

		Player players[] = plugin.getServer().getOnlinePlayers();

		for (final Player p : players) {

			final int money0 = Utils.totalMoneyinInventory(p);
			final int bal0 = (int) EmeraldEconLink.econ.getBalance(p.getName());

			task3 = plugin.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {

							int money1 = money0;
							int money2 = Utils.totalMoneyinInventory(p);
							int bal2 = (int) EmeraldEconLink.econ.getBalance(p
									.getName());
							int item = p.getItemOnCursor().getTypeId();
							int itemId = 0;
							int bal1 = bal0;

							;

							if (!Configuration.DisabledWorlds.contains(p
									.getWorld().getName())) {

								if (((bal2 / Collections
										.min(Configuration.itemsCost))
										* Collections
												.min(Configuration.itemsCost) != bal2)
										|| ((bal1 / Collections
												.min(Configuration.itemsCost))
												* Collections
														.min(Configuration.itemsCost) != bal1)) {

									bal1 = (bal1 / Collections
											.min(Configuration.itemsCost))
											* Collections
													.min(Configuration.itemsCost);
									bal2 = (bal2 / Collections
											.min(Configuration.itemsCost))
											* Collections
													.min(Configuration.itemsCost);
								}

								if (bal1 != bal2) {

									if ((bal1 > money1)
											&& (Configuration.EnableItemOnEarnMoney)) {

										int diff = bal1 - money1;
										if (diff > Utils.howManyMoneyCanHold(p))
											diff = Utils.howManyMoneyCanHold(p);
										if (diff > 0) {

											Utils.solve(p, diff);
											money1 = money2;

										}

									} else if (money1 > bal1) {

										int diff = money1 - bal1;

										if (diff > 0)
											Utils.remove(p, diff);

										money1 = Utils.totalMoneyinInventory(p);
										bal1 = (int) EmeraldEconLink.econ
												.getBalance(p.getName());
										money1 = money2;

									} else if (bal1 > bal2) {
										int diff = bal1 - bal2;

										if (diff > 0)
											Utils.remove(p, diff);

										money1 = Utils.totalMoneyinInventory(p);
										bal1 = (int) EmeraldEconLink.econ
												.getBalance(p.getName());
										money1 = money2;

									} else if (bal2 > bal1) {
										int diff = bal2 - bal1;

										if (diff > 0)
											Utils.solve(p, diff);

										money1 = Utils.totalMoneyinInventory(p);
										bal1 = (int) EmeraldEconLink.econ
												.getBalance(p.getName());
										money1 = money2;
									}
								} else if (bal1 == bal2) {

									if (p.getOpenInventory().getType() == InventoryType.WORKBENCH) {
										return;

									}
									if (p.getOpenInventory().getType() == InventoryType.ANVIL) {
										return;

									}
									if (p.getOpenInventory().getType() == InventoryType.MERCHANT) {
										return;

									}
									if (((itemId != 0) && (item == itemId))
											&& (p.getOpenInventory() == p
													.getOpenInventory()
													.getTopInventory())) {
										return;

									}
									if (money1 < money2) {

										int amount = money2 - money1;

										EmeraldEconLink.econ.depositPlayer(
												p.getName(), amount);

										p.sendMessage(Utils.blue + p.getName()
												+ Utils.white
												+ Configuration.messageAdd
												+ Utils.green + amount
												+ Utils.white
												+ Configuration.currencyName);

									} else if (money1 > money2) {

										int amount = money1 - money2;
										EmeraldEconLink.econ.withdrawPlayer(
												p.getName(), amount);
										p.sendMessage(Utils.blue + p.getName()
												+ Utils.white
												+ Configuration.messageRemove
												+ Utils.red + amount
												+ Utils.white
												+ Configuration.currencyName);

									}
								}

							}
						}
					}, 1L);
		}
	}

	public static void compareOnPlayerInteract(final Player p) {

		final int money0 = Utils.totalMoneyinInventory(p);
		final int bal0 = (int) EmeraldEconLink.econ.getBalance(p.getName());

		task3 = plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {

						int money1 = money0;
						int money2 = Utils.totalMoneyinInventory(p);
						int bal2 = (int) EmeraldEconLink.econ.getBalance(p
								.getName());

						int bal1 = bal0;

						if (!Configuration.DisabledWorlds.contains(p.getWorld()
								.getName())) {

							if (((bal2 / Collections
									.min(Configuration.itemsCost))
									* Collections.min(Configuration.itemsCost) != bal2)
									|| ((bal1 / Collections
											.min(Configuration.itemsCost))
											* Collections
													.min(Configuration.itemsCost) != bal1)) {

								bal1 = (bal1 / Collections
										.min(Configuration.itemsCost))
										* Collections
												.min(Configuration.itemsCost);
								bal2 = (bal2 / Collections
										.min(Configuration.itemsCost))
										* Collections
												.min(Configuration.itemsCost);
							}

							if (bal1 != bal2) {
								if ((bal1 > money1)
										&& (Configuration.EnableItemOnEarnMoney)) {
									int diff = bal1 - money1;
									if (diff > Utils.howManyMoneyCanHold(p))
										diff = Utils.howManyMoneyCanHold(p);
									if (diff > 0) {

										Utils.solve(p, diff);
										money1 = money2;

									}

								} else if (money1 > bal1) {
									int diff = money1 - bal1;

									if (diff > 0)
										Utils.remove(p, diff);

									money1 = Utils.totalMoneyinInventory(p);
									bal1 = (int) EmeraldEconLink.econ
											.getBalance(p.getName());
									money1 = money2;

								} else if (bal1 > bal2) {
									int diff = bal1 - bal2;

									if (diff > 0)
										Utils.remove(p, diff);

									money1 = Utils.totalMoneyinInventory(p);
									bal1 = (int) EmeraldEconLink.econ
											.getBalance(p.getName());
									money1 = money2;

								} else if (bal2 > bal1) {
									int diff = bal2 - bal1;

									if (diff > 0)
										Utils.solve(p, diff);

									money1 = Utils.totalMoneyinInventory(p);
									bal1 = (int) EmeraldEconLink.econ
											.getBalance(p.getName());
									money1 = money2;
								}
							} else if (bal1 == bal2) {

								if (money1 < money2) {

									int amount = money2 - money1;

									EmeraldEconLink.econ.depositPlayer(
											p.getName(), amount);

									p.sendMessage(Utils.blue + p.getName()
											+ Utils.white
											+ Configuration.messageAdd
											+ Utils.green + amount
											+ Utils.white
											+ Configuration.currencyName);

								} else if (money1 > money2) {

									int amount = money1 - money2;
									EmeraldEconLink.econ.withdrawPlayer(
											p.getName(), amount);
									p.sendMessage(Utils.blue + p.getName()
											+ Utils.white
											+ Configuration.messageRemove
											+ Utils.red + amount + Utils.white
											+ Configuration.currencyName);

								}
							}

						}
					}
				}, 1L);
	}

	public static void compare(final Player p) {

		final int money0 = Utils.totalMoneyinInventory(p);
		final int bal0 = (int) EmeraldEconLink.econ.getBalance(p.getName());

		task4 = plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {

						int money1 = money0;

						int bal1 = bal0;
						int bal2 = (int) EmeraldEconLink.econ.getBalance(p
								.getName());

						if (!Configuration.DisabledWorlds.contains(p.getWorld()
								.getName())) {

							if (p.getOpenInventory().getType() == InventoryType.WORKBENCH) {
								return;

							}
							if (p.getOpenInventory().getType() == InventoryType.ANVIL) {
								return;

							}
							if (p.getOpenInventory().getType() == InventoryType.MERCHANT) {
								return;

							}
							
							if (((bal2 / Collections
									.min(Configuration.itemsCost))
									* Collections.min(Configuration.itemsCost) != bal2)
									|| ((bal1 / Collections
											.min(Configuration.itemsCost))
											* Collections
													.min(Configuration.itemsCost) != bal1)) {

								bal1 = (bal1 / Collections
										.min(Configuration.itemsCost))
										* Collections
												.min(Configuration.itemsCost);
								bal2 = (bal2 / Collections
										.min(Configuration.itemsCost))
										* Collections
												.min(Configuration.itemsCost);
							}

							if (p.getGameMode() != GameMode.CREATIVE) {

								if ((bal1 > money1)
										&& (Configuration.EnableItemOnEarnMoney)) {
									int diff = bal1 - money1;
									if (diff > Utils.howManyMoneyCanHold(p))
										diff = Utils.howManyMoneyCanHold(p);
									if (diff > 0) {

										Utils.solve(p, diff);
									}

								}
								if (money1 > bal1) {
									int diff = money1 - bal1;

									if (diff > 0)
										Utils.remove(p, diff);

								}
							}
						}
					}
				}, 1L);

	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {

		if (e.getMessage().startsWith("/eel"))
			return;
		if (e.getMessage().startsWith("/cash"))
			return;

		compareOnCommand();

	}
}

