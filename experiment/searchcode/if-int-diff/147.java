package me.fonkfader.EmeraldEconLink;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.plugin.java.JavaPlugin;

public class ContenerListener extends JavaPlugin implements Listener {

	@SuppressWarnings("unused")
	private EmeraldEconLink plugin;

	public ContenerListener(EmeraldEconLink plugin) {

		this.plugin = plugin;
	}

	private int itemId = 0;
	private int cost = 0;

	public void ChestTypeInteract(InventoryClickEvent e, int slot) {

		final Player p = (Player) e.getWhoClicked();
		final String name = p.getName();
		String skname = null;
		int itemId2 = 0;
		int cost2 = 0;
		if (e.getCurrentItem() != null) {
			int item = e.getCursor().getTypeId();

			for (int i = 0; i < Configuration.itemsId.size(); i++) {
				if (item == Configuration.itemsId.get(i)) {
					itemId2 = Configuration.itemsId.get(i);
					cost2 = Configuration.itemsCost.get(i);
				}
			}
			for (int i = 0; i < Configuration.blocksId.size(); i++) {
				if (item == Configuration.blocksId.get(i)) {
					itemId2 = Configuration.blocksId.get(i);
					cost2 = Utils.blockcost(Configuration.itemsCanBeBlock
							.get(i));
				}
			}
		}

		if (e.isCancelled()) {
			return;
		}

		if (EmeraldEconLink.sk != null) {
			skname = EmeraldEconLink.sk.getConfig().getString("editor-title");
		}

		if (e.getCurrentItem() != null) {

			int amountInSlot = e.getCurrentItem().getAmount() * cost;
			int amountOnCursor = e.getCursor().getAmount() * cost;
			int bal = (int) EmeraldEconLink.econ.getBalance(name);

			if ((e.getView().getType() == InventoryType.CHEST)
					|| (e.getView().getType() == InventoryType.ENDER_CHEST)
					|| (e.getView().getType() == InventoryType.DISPENSER)) {
				if ((e.getView().countSlots() == slot)
						&& (e.getView().getTitle().toString() != skname)) {

					// si le slot cliqu� correspond a l'inventaire du haut et
					// le
					// curseur a deja l'item (pour un depot)
					if ((e.getRawSlot() < (slot - 36))
							&& (e.getCursor().getType().getId() == itemId)) {

						// si le slot cliqu� est vide
						if (e.getCurrentItem().getType().getId() == 0) {
							if (e.isShiftClick() == true) {
								return;
							} else if (e.isRightClick()) {

								if (bal >= cost) {

									EmeraldEconLink.econ.withdrawPlayer(name,
											cost);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageRemove(p,
												cost));
									}
								}
							} else {

								EmeraldEconLink.econ.withdrawPlayer(name,
										amountOnCursor);

								if (!Configuration.DisableChatMessage) {
									p.sendMessage(Utils.messageRemove(p,
											amountOnCursor));
								}
							}

						} else if ((e.getCurrentItem().getType().getId() != 0)
								&& (e.getCurrentItem().getType().getId() != itemId)) {

							if (e.isShiftClick() == true) {
								return;
							} else {

								EmeraldEconLink.econ.withdrawPlayer(name,
										amountOnCursor);

								if (!Configuration.DisableChatMessage) {
									p.sendMessage(Utils.messageRemove(p,
											amountOnCursor));
								}
							}

						} else if (e.getCurrentItem().getType().getId() == itemId) {

							if (e.isShiftClick()) {

								int getAmount = e.getCurrentItem().getAmount();

								if (getAmount <= Utils.howManyItemsCanHold(p,
										itemId)) {

									EmeraldEconLink.econ.depositPlayer(name,
											amountInSlot);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageAdd(p,
												amountInSlot));
									}
								} else if ((Utils.howManyItemsCanHold(p, itemId) < getAmount)
										&& (Utils.howManyItemsCanHold(p, itemId) > 0)) {

									EmeraldEconLink.econ.depositPlayer(name,
													Utils.howManyItemsCanHold(p, itemId) * cost);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageAdd(
												p,
												Utils.howManyItemsCanHold(p,
														itemId) * cost));
									}
								} else
									return;

							} else if (e.isRightClick()) {

								EmeraldEconLink.econ.withdrawPlayer(name, cost);
								if (!Configuration.DisableChatMessage) {
									p.sendMessage(Utils.messageRemove(p, cost));
								}

							} else {

								int slotFreeSpace = e.getCurrentItem().getMaxStackSize()
										- (e.getCurrentItem().getAmount());

								if (slotFreeSpace > e.getCursor().getAmount()) {

									EmeraldEconLink.econ.withdrawPlayer(name,
											amountOnCursor);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageRemove(p,
												amountOnCursor));
									}
								} else {
									if (slotFreeSpace > 0) {

										EmeraldEconLink.econ.withdrawPlayer(
												name, slotFreeSpace * cost);

										if (!Configuration.DisableChatMessage) {

											p.sendMessage(Utils.messageRemove(
													p, slotFreeSpace * cost));
										}
									} else
										return;
								}

							}
						}
						// fonctionnement pour le credit, pas besoins de check
					} else if ((e.getRawSlot() < (slot - 36))
							&& (e.getCursor().getType().getId() == 0)) {

						if (e.getCurrentItem().getType().getId() == itemId) {

							if (e.isShiftClick()) {

								int getAmount = e.getCurrentItem().getAmount();

								if (getAmount <= Utils.howManyItemsCanHold(p,
										itemId)) {

									EmeraldEconLink.econ.depositPlayer(name,
											amountInSlot);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageAdd(p,
												amountInSlot));
									}
								} else if ((Utils
										.howManyItemsCanHold(p, itemId) < getAmount)
										&& (Utils
												.howManyItemsCanHold(p, itemId) > 0)) {

									EmeraldEconLink.econ
											.depositPlayer(
													name,
													Utils.howManyItemsCanHold(
															p, itemId) * cost);

									if (!Configuration.DisableChatMessage) {
										p.sendMessage(Utils.messageAdd(
												p,
												Utils.howManyItemsCanHold(p,
														itemId) * cost));
									}
								} else
									return;
							} else if ((e.isRightClick())) {

								int amount4 = (e.getCurrentItem().getAmount() / 2)
										* cost;
								int amount5 = amount4 + cost;

								if ((e.getCurrentItem().getAmount() % 2) != 0) {

									EmeraldEconLink.econ.depositPlayer(name,
											amount5);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageAdd(p,
												amount5));
									}
									if ((e.getCursor().getTypeId() == itemId2)
											&& (itemId2 != 0)) {

										EmeraldEconLink.econ.withdrawPlayer(
												name, e.getCursor().getAmount()
														* cost2);
										if (!Configuration.DisableChatMessage) {

											p.sendMessage(Utils.messageRemove(
													p, e.getCursor()
															.getAmount()
															* cost2));
										}
									}
								} else {

									EmeraldEconLink.econ.depositPlayer(name,
											amount4);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageAdd(p,
												amount4));
									}
									if ((e.getCursor().getTypeId() == itemId2)
											&& (itemId2 != 0)) {

										EmeraldEconLink.econ.withdrawPlayer(
												name, e.getCursor().getAmount()
														* cost2);
										if (!Configuration.DisableChatMessage) {

											p.sendMessage(Utils.messageRemove(
													p, e.getCursor()
															.getAmount()
															* cost2));
										}
									}
								}
							} else {
								EmeraldEconLink.econ.depositPlayer(name,
										amountInSlot);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageAdd(p,
											amountInSlot));
								}
								if ((e.getCursor().getTypeId() == itemId2)
										&& (itemId2 != 0)) {
									EmeraldEconLink.econ.withdrawPlayer(name, e
											.getCursor().getAmount() * cost2);
									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageRemove(p, e
												.getCursor().getAmount()
												* cost2));
									}
								}
							}
						}
						// si il y a shiftclic depuis l'inventaire du joueur
					} else if ((e.getRawSlot() < (slot - 36))
							&& (e.getCursor().getType().getId() != 0)
							&& (e.getCursor().getType().getId() != 0)) {

						if (e.getCurrentItem().getType().getId() == itemId) {

							if (e.isShiftClick()) {

								int getAmount = e.getCurrentItem().getAmount();

								if (getAmount <= Utils.howManyItemsCanHold(p,
										itemId)) {

									EmeraldEconLink.econ.depositPlayer(name,
											amountInSlot);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageAdd(p,
												amountInSlot));
									}
								} else if ((Utils
										.howManyItemsCanHold(p, itemId) < getAmount)
										&& (Utils
												.howManyItemsCanHold(p, itemId) > 0)) {

									EmeraldEconLink.econ
											.depositPlayer(
													name,
													Utils.howManyItemsCanHold(
															p, itemId) * cost);

									if (!Configuration.DisableChatMessage) {
										p.sendMessage(Utils.messageAdd(
												p,
												Utils.howManyItemsCanHold(p,
														itemId) * cost));
									}
								} else
									return;
							} else if ((e.isRightClick())) {

								int amount4 = (e.getCurrentItem().getAmount())
										* cost;

								EmeraldEconLink.econ.depositPlayer(name,
										amount4);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageAdd(p, amount4));
								}
								if ((e.getCursor().getTypeId() == itemId2)
										&& (itemId2 != 0)) {

									EmeraldEconLink.econ.withdrawPlayer(name, e
											.getCursor().getAmount() * cost2);
									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageRemove(p, e
												.getCursor().getAmount()
												* cost2));
									}
								}

							} else {
								EmeraldEconLink.econ.depositPlayer(name,
										amountInSlot);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageAdd(p,
											amountInSlot));
								}
								if ((e.getCursor().getTypeId() == itemId2)
										&& (itemId2 != 0)) {
									EmeraldEconLink.econ.withdrawPlayer(name, e
											.getCursor().getAmount() * cost2);
									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageRemove(p, e
												.getCursor().getAmount()
												* cost2));
									}
								}
							}
						}
						// si il y a shiftclic depuis l'inventaire du joueur

					}

					else if ((e.getRawSlot() > (slot - 37))
							&& (e.getCurrentItem().getType().getId() == itemId)
							&& (e.isShiftClick())) {

						if (bal >= amountInSlot) {

							int getAmount = e.getCurrentItem().getAmount();

							if (getAmount <= Utils.howManyItemsHolderCanHold(e,
									itemId)) {

								EmeraldEconLink.econ.withdrawPlayer(name,
										amountInSlot);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageRemove(p,
											amountInSlot));
								}
							} else if ((Utils.howManyItemsHolderCanHold(e,
									itemId) < getAmount)
									&& (Utils.howManyItemsHolderCanHold(e,
											itemId) > 0)) {

								EmeraldEconLink.econ.withdrawPlayer(
										name,
										Utils.howManyItemsHolderCanHold(e,
												itemId) * cost);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageRemove(
											p,
											Utils.howManyItemsHolderCanHold(e,
													itemId) * cost));
								}
							} else
								return;
						}
					} else if (e.getView().getTitle() == skname)
						return;

				}

			}

		}
	}

	public void npcInteract(final InventoryClickEvent e) {

		final Player p = (Player) e.getWhoClicked();
		final String name = p.getName();

		if (((e.getView().getType() == InventoryType.MERCHANT) && (e.getView()
				.countSlots() == 39))) {

			if (e.getSlotType() == SlotType.RESULT) {
				if (e.isCancelled()) {
					return;
				}
				if (e.getCurrentItem().getTypeId() == itemId) {

					final int nbItem = Utils.totalMoneyinInventory(p);
					final int freespace = Utils.howManyItemsCanHold(p, itemId);

					if (e.isShiftClick()) {

						EmeraldEconLink.server.getScheduler()
								.scheduleSyncDelayedTask(this, new Runnable() {
									@Override
									public void run() {

										int nbItem2 = Utils
												.totalMoneyinInventory(p);
										int total = (nbItem2 - nbItem);

										if (freespace <= 0)
											return;

										else if (freespace > 0) {

											EmeraldEconLink.econ.depositPlayer(
													name, total);

											if (!Configuration.DisableChatMessage) {
												p.sendMessage(Utils.messageAdd(
														p, total));
											}

										} else {
											int amount = e.getCurrentItem()
													.getAmount();
											int credit = amount * cost;

											EmeraldEconLink.econ.depositPlayer(
													name, credit);

											if (!Configuration.DisableChatMessage) {
												p.sendMessage(Utils.messageAdd(
														p, credit));
											}
										}
									}
								}, 1);

					} else {
						int amount = e.getCurrentItem().getAmount();
						int credit = amount * cost;

						EmeraldEconLink.econ.depositPlayer(name, credit);

						if (!Configuration.DisableChatMessage) {

							p.sendMessage(Utils.messageAdd(p, credit));
						}
					}
				} else {

					final int count = Utils.totalMoneyinInventoryHolder(e);

					if ((e.getResult() != null)
							&& (e.getCurrentItem().getTypeId() != 0)
							&& (e.getInventory().contains(itemId))) {

						EmeraldEconLink.server.getScheduler()
								.scheduleSyncDelayedTask(this, new Runnable() {
									@Override
									public void run() {

										int count2 = Utils
												.totalMoneyinInventoryHolder(e);
										int total = count - count2;
										if (count - count2 > 0) {

											EmeraldEconLink.econ
													.withdrawPlayer(name, total);

											if (!Configuration.DisableChatMessage) {

												p.sendMessage(Utils
														.messageRemove(p, total));
											}
										} else
											return;
									}
								}, 1);
					}
				}
			}

		}
	}

	public void furnaceInteract(final InventoryClickEvent e) {

		final Player p = (Player) e.getWhoClicked();
		final String name = p.getName();

		if (e.getCurrentItem() != null) {

			int amount1 = e.getCurrentItem().getAmount() * cost;
			int amount2 = e.getCursor().getAmount() * cost;

			if ((e.getView().getType() == InventoryType.FURNACE)
					&& (e.getView().countSlots() == 39)) {

				if ((e.getRawSlot() < 3)
						&& (e.getCursor().getType().getId() == itemId)
						&& (e.getSlotType() != SlotType.RESULT)) {

					// si le slot cliqu� est vide
					if (e.getCurrentItem().getType().getId() != itemId) {

						if (e.isShiftClick() == true) {
							return;
						} else if (e.isRightClick()) {

							EmeraldEconLink.econ.withdrawPlayer(name, cost);

							if (!Configuration.DisableChatMessage) {

								p.sendMessage(Utils.messageRemove(p, cost));
							}
						} else {

							EmeraldEconLink.econ.withdrawPlayer(name, amount2);
							if (!Configuration.DisableChatMessage) {
								p.sendMessage(Utils.messageRemove(p, amount2));
							}

						}
						// si le slot contient deja l'item
					} else if (e.getCurrentItem().getType().getId() == itemId) {

						if (e.isShiftClick()) {

							int getAmount = e.getCurrentItem().getAmount();
							int totalInInv = Utils.countPlayerItem(p, itemId);
							int totalMaxInv = Utils.countItemSlots(p, itemId) * 64;

							if (getAmount <= ((totalMaxInv - totalInInv) + Utils
									.countPlayerEmptySlot(p) * 64)) {

								EmeraldEconLink.econ.depositPlayer(name,
										amount1);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageAdd(p, amount1));
								}
							}
						} else if (e.isRightClick()) {

							EmeraldEconLink.econ.withdrawPlayer(name, cost);

							if (!Configuration.DisableChatMessage) {

								p.sendMessage(Utils.messageRemove(p, cost));
							}

						} else {

							int slotFreeSpace = 64 - (e.getCurrentItem()
									.getAmount());

							if (slotFreeSpace > e.getCursor().getAmount()) {

								EmeraldEconLink.econ.withdrawPlayer(name,
										amount2);
								if (!Configuration.DisableChatMessage) {
									p.sendMessage(Utils.messageRemove(p,
											amount2));
								}
							} else {
								if (slotFreeSpace > 0) {
									EmeraldEconLink.econ.withdrawPlayer(name,
											slotFreeSpace * cost);

									if (!Configuration.DisableChatMessage) {

										p.sendMessage(Utils.messageRemove(p,
												slotFreeSpace * cost));
									}
								} else
									return;
							}

						}
					}
					// fonctionnement pour le credit, pas besoins de check
				} else if ((e.getRawSlot() < 3)
						&& (e.getCursor().getType().getId() != itemId)
						&& (e.getSlotType() != SlotType.RESULT)) {

					if (e.getCurrentItem().getType().getId() == itemId) {

						if (e.isShiftClick()) {

							int getAmount = e.getCurrentItem().getAmount();
							int cantake = Utils.howManyItemsCanHold(p, itemId);

							if (getAmount <= cantake) {

								EmeraldEconLink.econ.depositPlayer(name,
										amount1);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageAdd(p, amount1));
								}
							} else if (cantake > 0) {
								EmeraldEconLink.econ.depositPlayer(name,
										cantake * cost);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageAdd(p, cantake
											* cost));
								}
							} else
								return;
						} else if (e.isRightClick()) {

							int amount4 = (e.getCurrentItem().getAmount() / 2)
									* cost;
							int amount5 = amount4 + cost;

							if ((e.getCurrentItem().getAmount() % 2) != 0) {

								EmeraldEconLink.econ.depositPlayer(name,
										amount5);

								if (!Configuration.DisableChatMessage) {
									p.sendMessage(Utils.messageAdd(p, amount5));
								}
							} else {
								EmeraldEconLink.econ.depositPlayer(name,
										amount4);
								if (!Configuration.DisableChatMessage) {
									p.sendMessage(Utils.messageAdd(p, amount4));
								}
							}
						} else {
							EmeraldEconLink.econ.depositPlayer(name, amount1);
							if (!Configuration.DisableChatMessage) {
								p.sendMessage(Utils.messageAdd(p, amount1));
							}
						}
					}
				} else if (e.getSlotType() == SlotType.RESULT) {

					if (e.getCurrentItem().getTypeId() == itemId) {

						final int nbItem = Utils.countPlayerItem(p, itemId);
						final int freespace = Utils.howManyItemsCanHold(p,
								itemId);
						if (e.isShiftClick()) {

							EmeraldEconLink.server.getScheduler()
									.scheduleSyncDelayedTask(this,
											new Runnable() {

												@Override
												public void run() {

													int nbItem2 = Utils
															.countPlayerItem(p,
																	itemId);
													int total = (nbItem2 - nbItem)
															* cost;

													if (freespace <= 0) {

														return;

													} else if (freespace > 0) {

														EmeraldEconLink.econ
																.depositPlayer(
																		name,
																		total);

														if (!Configuration.DisableChatMessage) {

															p.sendMessage(Utils
																	.messageAdd(
																			p,
																			total));
														}

													} else {
														int amount = e
																.getCurrentItem()
																.getAmount();
														int credit = amount
																* cost;
														EmeraldEconLink.econ
																.depositPlayer(
																		name,
																		credit);

														if (!Configuration.DisableChatMessage) {

															p.sendMessage(Utils
																	.messageAdd(
																			p,
																			credit));
														}
													}
												}
											}, 1);

						} else {
							int amount = e.getCurrentItem().getAmount();
							int credit = amount * cost;

							EmeraldEconLink.econ.depositPlayer(name, credit);

							if (!Configuration.DisableChatMessage) {

								p.sendMessage(Utils.messageAdd(p, credit));
							}
						}
					} else {

						final int count = Utils.countHolderItem(e, itemId);

						if ((e.getResult() != null)
								&& (e.getCurrentItem().getTypeId() != 0)
								&& (e.getInventory().contains(itemId))) {

							EmeraldEconLink.server.getScheduler()
									.scheduleSyncDelayedTask(this,
											new Runnable() {

												@Override
												public void run() {

													int count2 = Utils
															.countHolderItem(e,
																	itemId);
													int total = (count - count2)
															* cost;

													if (count - count2 > 0) {

														EmeraldEconLink.econ
																.withdrawPlayer(
																		name,
																		total);

														if (!Configuration.DisableChatMessage) {

															p.sendMessage(Utils
																	.messageRemove(
																			p,
																			total));
														}
													} else
														return;
												}
											}, 1);
						}
					}
				}

			}
		}

	}

	public void craftInteract(final InventoryClickEvent e) {

		final Player p = (Player) e.getWhoClicked();
		final String name = p.getName();

		if (e.getCurrentItem() != null) {

			if ((e.getView().getType() == InventoryType.WORKBENCH)
					|| (e.getView().getType() == InventoryType.CRAFTING)) {

				if (e.getSlotType() == SlotType.RESULT) {

					if ((e.getCurrentItem().getTypeId() == itemId)
							&& (e.getCurrentItem().getTypeId() != 0)) {

						final int nbItem = Utils.countPlayerItem(p, itemId);
						final int freespace = Utils.howManyItemsCanHold(p,
								itemId);

						for (int i = 0; i < Configuration.blocksId.size(); i++) {
							if (e.getCurrentItem().getTypeId() == Configuration.blocksId
									.get(i))
								return;
						}

						if ((Configuration.itemsCanBeBlock.contains(itemId))
								&& (e.getCurrentItem().getAmount() == 9))
							return;

						else if (e.isShiftClick()) {

							EmeraldEconLink.server.getScheduler()
									.scheduleSyncDelayedTask(this,
											new Runnable() {
												@Override
												public void run() {

													int nbItem2 = Utils
															.countPlayerItem(p,
																	itemId);
													int total = (nbItem2 - nbItem)
															* cost;

													if (freespace <= 0) {
														return;

													} else if (freespace > 0) {

														EmeraldEconLink.econ
																.depositPlayer(
																		name,
																		total);
														if (!Configuration.DisableChatMessage) {
															p.sendMessage(Utils
																	.messageAdd(
																			p,
																			total));
														}

													} else {
														int amount = e
																.getCurrentItem()
																.getAmount();
														int credit = amount
																* cost;

														EmeraldEconLink.econ
																.depositPlayer(
																		name,
																		credit);

														if (!Configuration.DisableChatMessage) {

															p.sendMessage(Utils
																	.messageAdd(
																			p,
																			credit));
														}
													}

												}
											}, 1);

						} else {
							int amount = e.getCurrentItem().getAmount();
							int credit = amount * cost;

							EmeraldEconLink.econ.depositPlayer(name, credit);
							if (!Configuration.DisableChatMessage) {
								p.sendMessage(Utils.messageAdd(p, credit));
							}
						}
					} else {

						final int count = Utils.countHolderItem(e, itemId);

						if ((e.getResult() != null)
								&& (e.getCurrentItem().getTypeId() != 0)
								&& (e.getInventory().contains(itemId))) {

							EmeraldEconLink.server.getScheduler()
									.scheduleSyncDelayedTask(this,
											new Runnable() {
												@Override
												public void run() {

													int count2 = Utils
															.countHolderItem(e,
																	itemId);
													int total = (count - count2)
															* cost;

													if (count - count2 > 0) {

														EmeraldEconLink.econ
																.withdrawPlayer(
																		name,
																		total);
														if (!Configuration.DisableChatMessage) {

															p.sendMessage(Utils
																	.messageRemove(
																			p,
																			total));
														}
													} else
														return;
												}
											}, 1);
						}
					}
				}

			}
		}

	}

	public void AnvilInteract(final InventoryClickEvent e) {

		final Player p = (Player) e.getWhoClicked();
		final String name = p.getName();
		if (e.getCurrentItem() != null) {

			if (e.getView().getType() == InventoryType.ANVIL) {

				if (e.getRawSlot() == 2) {

					if ((e.getCurrentItem().getTypeId() == itemId)
							&& (e.getCurrentItem().getTypeId() != 0)
							&& (p.getOpenInventory().getBottomInventory()
									.getItem(0).getTypeId() == e
									.getCurrentItem().getTypeId())) {

						final int nbItem = Utils.countPlayerItem(p, itemId);
						final int freespace = Utils.howManyItemsCanHold(p,
								itemId);
						final int amountslot1 = e.getCurrentItem().getAmount();

						for (int i = 0; i < Configuration.blocksId.size(); i++) {
							if (e.getCurrentItem().getTypeId() == Configuration.blocksId
									.get(i))
								return;
						}

						if ((Configuration.itemsCanBeBlock.contains(itemId))
								&& (e.getCurrentItem().getAmount() == 9))
							return;

						EmeraldEconLink.server.getScheduler()
								.scheduleSyncDelayedTask(this, new Runnable() {
									@Override
									public void run() {
										int amountslot2 = e.getCurrentItem()
												.getAmount();
										if (e.isShiftClick()) {

											int nbItem2 = Utils
													.countPlayerItem(p, itemId);
											int total = (nbItem2 - nbItem)
													* cost;

											if (amountslot2 == amountslot1)
												return;

											else if (freespace <= 0) {
												return;

											} else if (freespace > 0) {

												EmeraldEconLink.econ
														.depositPlayer(name,
																total);
												if (!Configuration.DisableChatMessage) {
													p.sendMessage(Utils
															.messageAdd(p,
																	total));
												}

											} else {
												int amount = e.getCurrentItem()
														.getAmount();
												int credit = amount * cost;
												EmeraldEconLink.econ
														.depositPlayer(name,
																credit);

												if (!Configuration.DisableChatMessage) {
													p.sendMessage(Utils
															.messageAdd(p,
																	credit));
												}
											}

										} else {

											int amount = e.getCurrentItem()
													.getAmount();
											int credit = amount * cost;

											EmeraldEconLink.econ.depositPlayer(
													name, credit);

											if (amountslot2 == amountslot1)
												return;
											if (!Configuration.DisableChatMessage) {

												p.sendMessage(Utils.messageAdd(
														p, credit));
											}
										}
									}
								}, 1);

					} else if ((p.getOpenInventory().getBottomInventory()
							.getItem(0)!=null)&&(p.getOpenInventory().getBottomInventory()
							.getItem(0).getTypeId() != e.getCurrentItem()
							.getTypeId())) {

						final int count = Utils.countHolderItem(e, itemId);

						if ((e.getResult() != null)
								&& (e.getCurrentItem().getTypeId() != 0)
								&& (e.getInventory().contains(itemId))) {

							EmeraldEconLink.server.getScheduler()
									.scheduleSyncDelayedTask(this,
											new Runnable() {
												@Override
												public void run() {

													int count2 = Utils
															.countHolderItem(e,
																	itemId);
													int total = (count - count2)
															* cost;

													if (count - count2 > 0) {

														EmeraldEconLink.econ
																.withdrawPlayer(
																		name,
																		total);

														if (!Configuration.DisableChatMessage) {

															p.sendMessage(Utils
																	.messageRemove(
																			p,
																			total));
														}
													} else
														return;
												}
											}, 1);
						}
					}
				}

			}
		}

	}

	public void beaconInteract(InventoryClickEvent e, int slot) {

		final Player p = (Player) e.getWhoClicked();
		final String name = p.getName();

		if (e.isCancelled()) {
			return;
		}

		if (e.getCurrentItem() != null) {

			if (e.getView().getType() == InventoryType.BEACON) {

				// si le slot cliqu� correspond a l'inventaire du haut et le
				// curseur a deja l'item (pour un depot)
				if ((e.getRawSlot() < (slot - 36))
						&& (e.getCursor().getType().getId() == itemId)) {

					// si le slot cliqu� est vide
					if (e.getCurrentItem().getType().getId() != itemId) {

						if (e.isShiftClick())
							return;

						else {
							EmeraldEconLink.econ.withdrawPlayer(name, cost);

							if (!Configuration.DisableChatMessage) {

								p.sendMessage(Utils.messageRemove(p, cost));
							}

						}
						// si le slot contient deja l'item
					} else if (e.getCurrentItem().getType().getId() == itemId) {

						if (e.isShiftClick()) {
							int getAmount = e.getCurrentItem().getAmount();

							if (getAmount <= Utils.howManyItemsCanHold(p,
									itemId)) {

								EmeraldEconLink.econ.depositPlayer(name, cost);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageAdd(p, cost));

								}
							}
						} else
							return;
					}
					// fonctionnement pour le credit, pas besoins de check
				} else if ((e.getRawSlot() < (slot - 36))
						&& (e.getCursor().getType().getId() != itemId)) {

					if (e.getCurrentItem().getType().getId() == itemId) {

						if (e.isShiftClick()) {

							if (Utils.howManyItemsCanHold(p, itemId) >= 1) {

								EmeraldEconLink.econ.depositPlayer(name, cost);

								if (!Configuration.DisableChatMessage) {

									p.sendMessage(Utils.messageAdd(p, cost));
								}
							} else
								return;
						} else
							return;
					}
					// si il y a shiftclic depuis l'inventaire du joueur
				} else if ((e.getRawSlot() > (slot - 37))
						&& (e.getCurrentItem().getType().getId() == itemId)
						&& (e.isShiftClick())) {

					if (e.getCurrentItem().getAmount() == 1) {

						EmeraldEconLink.econ.withdrawPlayer(name, cost);

						if (!Configuration.DisableChatMessage) {

							p.sendMessage(Utils.messageRemove(p, cost));
						}
					} else
						return;
				}

			}

		}

	}

	@EventHandler
	public void onClickEvent(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();

		if (e.isCancelled()) {
			return;
		}
		if (!Configuration.DisabledWorlds.contains(p.getWorld().getName())) {

			if (e.getCurrentItem() != null) {
				int item = e.getCurrentItem().getTypeId();

				for (int i = 0; i < Configuration.itemsId.size(); i++) {
					if (item == Configuration.itemsId.get(i)) {
						itemId = Configuration.itemsId.get(i);
						cost = Configuration.itemsCost.get(i);
					}
				}
				for (int i = 0; i < Configuration.blocksId.size(); i++) {
					if (item == Configuration.blocksId.get(i)) {
						itemId = Configuration.blocksId.get(i);
						cost = Utils.blockcost(Configuration.itemsCanBeBlock
								.get(i));
					}
				}
				if (itemId != 0) {
					beaconInteract(e, 37);
					ChestTypeInteract(e, 63);
					ChestTypeInteract(e, 45);
					ChestTypeInteract(e, 54);
					ChestTypeInteract(e, 72);
					ChestTypeInteract(e, 81);
					npcInteract(e);
					furnaceInteract(e);
					craftInteract(e);
					AnvilInteract(e);

					if (e.getView().getTitle() != "Enchant Box") {

						ChestTypeInteract(e, 90);
					}
				}
			}
		}
	}

	private List<Integer> money1 = new ArrayList<Integer>();

	@EventHandler
	private void oncreativeclick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if ((e.getView().getType() == InventoryType.PLAYER)
				&& (p.getGameMode() == GameMode.CREATIVE)) {
			if (this.money1.size() == 0) {
				int getbal = Utils.totalMoneyinInventory(p);
				this.money1.add(0, getbal);

			}
		}
	}

	@EventHandler
	public void onCloseEvent(InventoryCloseEvent e) {

		Player p = (Player) e.getPlayer();

		int money2 = Utils.totalMoneyinInventory(p);

		if ((e.getView().getType() == InventoryType.CREATIVE)
				&& (p.getGameMode() == GameMode.CREATIVE)) {
			if (this.money1.size() != 0) {

				if (this.money1.get(0) > money2) {
					int diff = this.money1.get(0) - money2;

					if (diff > 0) {
						EmeraldEconLink.econ.withdrawPlayer(p.getName(), diff);
						if (!Configuration.DisableChatMessage) {
							p.sendMessage(Utils.messageRemove(p, diff));

						}
					}
				} else if (money2 > this.money1.get(0)) {
					int diff = money2 - this.money1.get(0);

					if (diff > 0) {
						EmeraldEconLink.econ.depositPlayer(p.getName(), diff);
						if (!Configuration.DisableChatMessage) {
							p.sendMessage(Utils.messageAdd(p, diff));

						}
					}

				}
				this.money1.remove(0);
			}
		}

	}

}

