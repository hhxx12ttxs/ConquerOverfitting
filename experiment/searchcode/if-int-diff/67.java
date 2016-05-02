package me.fonkfader.EmeraldEconLink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import com.google.common.primitives.Ints;

public class Utils {

	@SuppressWarnings("unused")
	private static EmeraldEconLink plugin;

	@SuppressWarnings("static-access")
	public Utils(EmeraldEconLink plugin) {

		this.plugin = plugin;
	}

	public static ChatColor green = ChatColor.GREEN;
	public static ChatColor red = ChatColor.RED;
	public static ChatColor darkRed = ChatColor.DARK_RED;
	public static ChatColor blue = ChatColor.BLUE;
	public static ChatColor white = ChatColor.WHITE;

	public static String messageAdd(Player p, int amount) {

		String messageAdd = (Utils.blue + p.getName() + Utils.white
				+ Configuration.messageAdd + Utils.green + amount + Utils.white + Configuration.currencyName);

		return messageAdd;
	}

	public static String messageRemove(Player p, int amount) {

		String messageRemove = (Utils.blue + p.getName() + Utils.white
				+ Configuration.messageRemove + Utils.red + amount
				+ Utils.white + Configuration.currencyName);

		return messageRemove;
	}

	public static int countPlayerEmptySlot(Player p) {
		ItemStack[] content = p.getInventory().getContents();
		int count = 0;
		for (int i = 0; i < content.length; i++) {
			if (content[i] == null)
				count++;
		}
		return count;
	}

	public static int countItemSlots(Player p, int itemId) {

		int count = 0;
		if (itemId != 0) {
			ItemStack[] content = p.getInventory().getContents();

			for (int i = 0; i < content.length; i++) {
				if (content[i] != null) {
					if (content[i].getTypeId() == itemId) {
						count++;
					}
				}

			}
		}
		return count;
	}

	public static int countPlayerItem(Player p, int itemId) {

		int countItem = 0;

		if (itemId != 0) {
			int item = p.getItemOnCursor().getTypeId();
			ItemStack[] inv = p.getInventory().getContents();
			for (int i = 0; i < inv.length; i++) {
				if (inv[i] != null) {
					if (inv[i].getTypeId() == itemId) {
						int amount = inv[i].getAmount();
						countItem = countItem + amount;
					}
				}
			}

			if ((item == itemId)
					&& (p.getOpenInventory().getType() != InventoryType.CREATIVE))
				countItem = countItem + p.getItemOnCursor().getAmount();
			if (p.getOpenInventory().getType() == InventoryType.CRAFTING) {
				if ((p.getOpenInventory().getTopInventory().getItem(1) != null)
						&& (p.getOpenInventory().getTopInventory().getItem(1)
								.getTypeId() == itemId))
					countItem = countItem
							+ p.getOpenInventory().getTopInventory().getItem(1)
									.getAmount();
				if ((p.getOpenInventory().getTopInventory().getItem(2) != null)
						&& (p.getOpenInventory().getTopInventory().getItem(2)
								.getTypeId() == itemId))
					countItem = countItem
							+ p.getOpenInventory().getTopInventory().getItem(2)
									.getAmount();
				if ((p.getOpenInventory().getTopInventory().getItem(3) != null)
						&& (p.getOpenInventory().getTopInventory().getItem(3)
								.getTypeId() == itemId))
					countItem = countItem
							+ p.getOpenInventory().getTopInventory().getItem(3)
									.getAmount();
				if ((p.getOpenInventory().getTopInventory().getItem(4) != null)
						&& (p.getOpenInventory().getTopInventory().getItem(4)
								.getTypeId() == itemId))
					countItem = countItem
							+ p.getOpenInventory().getTopInventory().getItem(4)
									.getAmount();
			}
		}
		return countItem;

	}

	public static int countFreeSlotHolder(InventoryClickEvent e) {
		ItemStack[] content = e.getInventory().getContents();
		int count = 0;
		for (int i = 0; i < content.length; i++) {
			if (content[i] == null)
				count++;
		}
		return count;
	}

	public static int countHolderItem(InventoryClickEvent e, int itemId) {

		int countItem = 0;

		if (itemId != 0) {
			if (e.getInventory().getContents() != null) {
				ItemStack[] inv = e.getInventory().getContents();
				for (int i = 0; i < inv.length; i++) {
					if (inv[i] != null) {
						if (inv[i].getTypeId() == itemId) {
							int cant = inv[i].getAmount();
							countItem = countItem + cant;
						}
					}
				}
			}
		}
		return countItem;
	}

	public static int countSlotItemsHolder(InventoryClickEvent e, int itemId) {

		int count = 0;
		if (itemId != 0) {
			ItemStack[] content = e.getInventory().getContents();

			for (int i = 0; i < content.length; i++) {
				if (content[i] != null) {
					if (content[i].getTypeId() == itemId) {
						count++;
					}
				}

			}
		}
		return count;
	}

	public static int totalMoneyinInventory(Player p) {

		int total = 0;
		int add = 0;

		if (Configuration.itemsId != null) {
			for (int i = 0; i < Configuration.itemsId.size(); i++) {
				add = countPlayerItem(p, Configuration.itemsId.get(i))
						* Configuration.itemsCost.get(i);
				total = total + add;
				if (Configuration.itemsCanBeBlock
						.contains(Configuration.itemsId.get(i))) {
					total = total
							+ countPlayerItem(
									p,
									Configuration.blocksId.get(Configuration.itemsCanBeBlock
											.indexOf(Configuration.itemsId
													.get(i))))
							* blockcost(Configuration.itemsId.get(i));
				}
			}
		}
		return total;
	}

	public static int totalMoneyinInventoryHolder(InventoryClickEvent e) {

		int total = 0;
		int add = 0;
		for (int i = 0; i < Configuration.itemsId.size(); i++) {
			add = countHolderItem(e, Configuration.itemsId.get(i))
					* Configuration.itemsCost.get(i);
			total = total + add;
			if (Configuration.itemsCanBeBlock.contains(Configuration.itemsId
					.get(i))) {
				total = total
						+ countHolderItem(e,
								Configuration.blocksId
										.get(Configuration.itemsCanBeBlock
												.indexOf(Configuration.itemsId
														.get(i))))
						* blockcost(Configuration.itemsId.get(i));
			}
		}
		return total;
	}

	public static int howManyItemsCanHold(Player p, int itemId) {
		int total = 0;
		if (itemId != 0) {
			total = ((countPlayerEmptySlot(p) * 64) + (countItemSlots(p, itemId) * 64))
					- countPlayerItem(p, itemId);
		}
		return total;
	}

	public static int maxcost() {

		int maxcost[] = null;
		List<Integer> cost = new ArrayList<Integer>();

		for (int i = 0; i < Configuration.itemsId.size(); i++) {
			cost.add(Configuration.itemsCost.get(i));
			if (Configuration.itemsCanBeBlock.contains(Configuration.itemsId
					.get(i))) {
				cost.add(blockcost(Configuration.itemsId.get(i)));
			}
		}
		maxcost = Ints.toArray(cost);
		Arrays.sort(maxcost);

		return maxcost[maxcost.length - 1];
	}

	public static int amountiteminMoney(Player p, int itemId) {

		int costItem = 0;

		if (Configuration.itemsId.contains(itemId)) {
			int index = Configuration.itemsId.indexOf(itemId);
			costItem = countPlayerItem(p, itemId)
					* Configuration.itemsCost.get(index);
		}
		if (Configuration.blocksId.contains(itemId)) {
			int index = Configuration.blocksId.indexOf(itemId);
			costItem = countPlayerItem(p, itemId)
					* blockcost(Configuration.itemsCanBeBlock.get(index));
		}
		return costItem;
	}

	public static int howManyMoneyCanHold(Player p) {

		int total = (countPlayerEmptySlot(p) * 64) * maxcost();

		for (int i = 0; i < Configuration.itemsId.size(); i++) {

			total = total
					+ (countItemSlots(p, Configuration.itemsId.get(i)) * 64)
					* Configuration.itemsCost.get(i);
			if (Configuration.itemsCanBeBlock.contains(Configuration.itemsId
					.get(i))) {
				total = total
						+ (countItemSlots(p,
								Configuration.blocksId
										.get(Configuration.itemsCanBeBlock
												.indexOf(Configuration.itemsId
														.get(i)))) * 64)
						* blockcost(Configuration.itemsId.get(i));
			}
		}

		total = total - totalMoneyinInventory(p);

		return total;
	}

	public static int howManyItemsHolderCanHold(InventoryClickEvent e,
			int itemId) {
		int total = 0;
		if (itemId != 0) {
			total = ((countFreeSlotHolder(e) * 64)
					+ ((countSlotItemsHolder(e, itemId) * 64)) - countHolderItem(
					e, itemId));
		}
		return total;
	}

	public static int itemForCost(int cost) {
		int itemId = 0;

		for (int i = 0; i < Configuration.itemsCost.size(); i++) {
			if (cost == Configuration.itemsCost.get(i))
				itemId = Configuration.itemsId.get(i);
			else if (blockcost(Configuration.itemsId.get(i)) == cost) {

				itemId = Configuration.blocksId
						.get(Configuration.itemsCanBeBlock
								.indexOf(Configuration.itemsId.get(i)));
			}
		}
		return itemId;
	}

	public static void remove(Player p, int diff) {

		Integer[] values = null;
		Integer[] dispos = null;
		List<Integer> cost = new ArrayList<Integer>();
		List<Integer> dispo = new ArrayList<Integer>();

		for (int i = 0; i < Configuration.itemsId.size(); i++) {
			cost.add(Configuration.itemsCost.get(i));
			if (Configuration.itemsCanBeBlock.contains(Configuration.itemsId
					.get(i))) {
				cost.add(blockcost(Configuration.itemsId.get(i)));
			}
		}

		values = (Integer[]) cost.toArray(new Integer[cost.size()]);
		Arrays.sort(values);

		for (int i = 0; i < values.length; i++) {

			if (((diff / values[i]) * diff != diff)
					&& (countPlayerItem(p, itemForCost(values[i])) > 0)
					&& (countPlayerItem(p, itemForCost(values[0])) < 1)) {
				int amount = countPlayerItem(p, itemForCost(values[i]))
						* values[i];
				diff = diff - amount;
				p.getInventory().remove(itemForCost(values[i]));
				int amount2 = countPlayerItem(p, itemForCost(values[0]))
						* values[0];
				p.getInventory().addItem(
						new ItemStack(itemForCost(values[0]), amount
								/ values[0]));
				diff = diff
						+ ((countPlayerItem(p, itemForCost(values[0])) * values[0]) - amount2);
				break;
			}
		}

		for (int i = 0; i < values.length; i++) {

			dispo.add(countPlayerItem(p, itemForCost(values[i])));

		}

		dispos = (Integer[]) dispo.toArray(new Integer[dispo.size()]);

		int bestscore = Integer.MAX_VALUE;
		int[] bestsolution = null;
		int[] solution = new int[values.length];
		int index = 0, score = 0;

		while (index >= 0) {

			while (index < values.length && score < bestscore) {
				solution[index] = Math.min(diff / values[index], dispos[index]);
				diff -= solution[index] * values[index];
				score += solution[index];
				index++;
			}
			index--;

			if (diff == 0 && score < bestscore) {
				bestscore = score;
				bestsolution = Arrays.copyOf(solution, solution.length);
			}

			score -= solution[index];
			diff += values[index] * solution[index];
			solution[index] = 0;

			while (index >= 0 && solution[index] == 0)
				index--;
			if (index < 0)
				break;

			score--;
			diff += values[index];
			solution[index]--;

			index++;
		}

		if (bestsolution != null) {
			for (int i = 0; i < bestsolution.length; i++) {

				if (bestsolution[i] > 0) {
					if (p.getInventory().contains(itemForCost(values[i]))) {
						p.getInventory().removeItem(
								new ItemStack(itemForCost(values[i]),
										bestsolution[i]));
					}
				}

			}
		}

	}

	static void solve(Player p, int diff) {

		Integer[] values = null;
		Integer[] dispos = null;
		List<Integer> cost = new ArrayList<Integer>();
		List<Integer> dispo = new ArrayList<Integer>();

		if (!Configuration.oneItemOnEarnMoney) {

			for (int i = 0; i < Configuration.itemsId.size(); i++) {
				cost.add(Configuration.itemsCost.get(i));
				if (Configuration.itemsCanBeBlock
						.contains(Configuration.itemsId.get(i))) {
					cost.add(blockcost(Configuration.itemsId.get(i)));
				}
			}
		} else {
			cost.add(Configuration.itemsCost.get(0));
			if (Configuration.itemsCanBeBlock.contains(Configuration.itemsId
					.get(0))) {
				cost.add(blockcost(Configuration.itemsId.get(0)));
			}
		}
		values = (Integer[]) cost.toArray(new Integer[cost.size()]);
		Arrays.sort(values);
		Arrays.sort(values, Collections.reverseOrder());

		for (int i = 0; i < values.length; i++) {

			dispo.add(((countItemSlots(p, itemForCost(values[i])) * 64) + (countPlayerEmptySlot(p) * 64))
					- countPlayerItem(p, itemForCost(values[i])));

		}

		dispos = (Integer[]) dispo.toArray(new Integer[dispo.size()]);

		int bestscore = Integer.MAX_VALUE;
		int[] bestsolution = null;
		int[] solution = new int[values.length];
		int index = 0, score = 0;

		while (index >= 0) {

			while (index < values.length && score < bestscore) {
				solution[index] = Math.min(diff / values[index], dispos[index]);
				diff -= solution[index] * values[index];
				score += solution[index];
				index++;
			}
			index--;

			if (diff == 0 && score < bestscore) {
				bestscore = score;
				bestsolution = Arrays.copyOf(solution, solution.length);
			}

			score -= solution[index];
			diff += values[index] * solution[index];
			solution[index] = 0;

			while (index >= 0 && solution[index] == 0)
				index--;
			if (index < 0)
				break;

			score--;
			diff += values[index];
			solution[index]--;

			index++;
		}

		if (bestsolution != null) {
			int sollenght = 0;
			for (int i = 0; i < bestsolution.length; i++) {
				if (bestsolution[i] > 0)
					sollenght = sollenght + 1;
			}
			for (int i = 0; i < bestsolution.length; i++) {

				if ((bestsolution[i] > 0) && (i + 1 < bestsolution.length)
						&& (sollenght > 1)) {
					boolean noneedotherslot = countItemSlots(p,
							itemForCost(values[i])) * 64 >= countPlayerItem(p,
							(values[i])) + bestsolution[i];

					if (noneedotherslot) {

						p.getInventory().addItem(
								new ItemStack(itemForCost(values[i]),
										bestsolution[i]));

					} else if (!noneedotherslot) {
						int next = 0;
						for (int search = i + 1; search < bestsolution.length; search++) {
							if (bestsolution[search] > 0) {
								next = search;
								break;
							}
						}

						double slotleft = (((countPlayerEmptySlot(p) * 64) - (((countPlayerItem(
								p, itemForCost(values[i])) + bestsolution[i])))) / 64);

						if (slotleft > 0) {

							p.getInventory().addItem(
									new ItemStack(itemForCost(values[i]),
											bestsolution[i]));
						} else {

							bestsolution[next] = bestsolution[next]
									+ (values[i] * bestsolution[i] / values[next]);
						}
					}
				} else if ((bestsolution[i] > 0)
						&& ((i + 1 == bestsolution.length) || (sollenght == 1))) {

					p.getInventory().addItem(
							new ItemStack(itemForCost(values[i]),
									bestsolution[i]));
				}
			}
		}
	}

	public static int blockcost(int itemId) {

		int cost = 0;

		for (int i = 0; i < Configuration.itemsId.size(); i++) {
			if ((itemId == Configuration.itemsId.get(i))
					&& (Configuration.itemsCanBeBlock.contains(itemId))) {
				cost = Configuration.itemsCost.get(i) * 9;

			}

		}
		return cost;

	}

}

