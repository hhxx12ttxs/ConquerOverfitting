package net.citizensnpcs.questers.quests;

import java.util.Map;

import net.citizensnpcs.properties.RawYAMLObject;
import net.citizensnpcs.questers.QuestManager;
import net.citizensnpcs.questers.quests.progress.QuestProgress;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

/**
 * Represents what is necessary to complete a quest objective. In order to
 * represent a wide range of quest possibilities, the class merely stores
 * possible data that could be used by a quest. For most objectives, the simple
 * 'amount' field will be enough to judge what is needed - for example, how many
 * monsters have been killed.
 * 
 * The message field is sent to the player upon completion of the objective.
 * 
 * @author fullwall
 * 
 */
public class Objective {
	private final int amount;

	private final boolean completeHere;
	private final int destination;
	private final RewardGranter granter;
	private final ItemStack item;
	private final Location location;
	private final Material material;
	private final boolean optional;
	private final String questType;
	private final Map<String, RawYAMLObject> parameters;
	private final String string;

	private Objective(String type, boolean optional, boolean completeHere,
			int amount, int destination, ItemStack item, String string,
			Material material, Location location, RewardGranter granter,
			Map<String, RawYAMLObject> params) {
		this.questType = type;
		this.amount = amount;
		this.destination = destination;
		this.item = item;
		this.string = string;
		this.material = material;
		this.location = location;
		this.granter = granter;
		this.optional = optional;
		this.completeHere = completeHere;
		this.parameters = params;
	}

	public RawYAMLObject getParameter(String name) {
		return parameters.get(name);
	}

	public boolean hasParameter(String string) {
		return parameters.get(string) != null;
	}

	public int getAmount() {
		return amount;
	}

	public int getDestNPCID() {
		return destination;
	}

	public RewardGranter getGranter() {
		return this.granter;
	}

	public ItemStack getItem() {
		return item;
	}

	public Location getLocation() {
		return location;
	}

	public Material getMaterial() {
		return material;
	}

	public String getString() {
		return string;
	}

	public String getType() {
		return questType;
	}

	public boolean isOptional() {
		return optional;
	}

	public void onCompletion(Player player, QuestProgress progress) {
		granter.onCompletion(player, progress);
		if (this.completeHere)
			QuestManager.completeQuest(player);
	}

	public static class Builder {
		private int amount = -1;
		private boolean completeHere = false;

		private int destination = -1;
		private RewardGranter granter;
		private ItemStack item = null;
		private Location location = null;
		private Material material = null;
		private boolean optional = false;
		private String string = "";
		private final String type;
		private final Map<String, RawYAMLObject> params = Maps.newHashMap();

		public Builder(String type) {
			this.type = type;
		}

		public Builder param(String name, RawYAMLObject value) {
			this.params.put(name, value);
			return this;
		}

		public Builder amount(int amount) {
			this.amount = amount;
			return this;
		}

		public Objective build() {
			return new Objective(type, optional, completeHere, amount,
					destination, item, string, material, location, granter,
					params);
		}

		public Builder completeHere(boolean completeHere) {
			this.completeHere = completeHere;
			return this;
		}

		public Builder destination(int destination) {
			this.destination = destination;
			return this;
		}

		public Builder granter(RewardGranter granter) {
			this.granter = granter;
			return this;
		}

		public Builder item(ItemStack item) {
			this.item = item;
			return this;
		}

		public Builder location(Location location) {
			this.location = location;
			return this;
		}

		public Builder material(Material material) {
			this.material = material;
			return this;
		}

		public Builder optional(boolean optional) {
			this.optional = optional;
			return this;
		}

		public Builder string(String string) {
			this.string = string;
			return this;
		}
	}
}
