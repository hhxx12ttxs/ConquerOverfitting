package pixelmon.battles.controller;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import pixelmon.battles.participants.BattleParticipant;
import pixelmon.battles.participants.ParticipantType;
import pixelmon.config.PixelmonItems;
import pixelmon.config.PixelmonItemsHeld;
import pixelmon.entities.pixelmon.EntityPixelmon;
import pixelmon.enums.heldItems.EnumHeldItems;
import pixelmon.items.ItemHeld;
import pixelmon.storage.PixelmonStorage;
import pixelmon.storage.PlayerStorage;

public class Experience {

	static void awardExp(ArrayList<BattleParticipant> participants, BattleParticipant faintedParticipant, EntityPixelmon faintedPokemon) {
		for (BattleParticipant p : participants) {
			if (p.team != faintedParticipant.team && p.getType() == ParticipantType.Player) {
				PlayerStorage storage = PixelmonStorage.PokeballManager.getPlayerStorage((EntityPlayerMP) p.currentPokemon().getOwner());

				ArrayList<Integer> doneUsers = new ArrayList<Integer>();
				if (!p.attackersList.contains(p.currentPokemon().getPokemonId()))
					p.attackersList.add(p.currentPokemon().getPokemonId());
				for (int i = 0; i < p.attackersList.size(); i++) {
					if (p.currentPokemon().getOwner() != null) {
						if (storage.isFainted(p.attackersList.get(i))) {
							p.attackersList.remove(i);
							i--;
						}
					}
				}
				int expShareCount = 0;
				for (NBTTagCompound nbt : storage.partyPokemon) {
					if (nbt != null) {
						if (nbt.hasKey("HeldItem")) {
							int itemId = nbt.getInteger("HeldItem");
							if (itemId != -1) {
								if (PixelmonItemsHeld.getHeldItem(itemId).getHeldItemType() == EnumHeldItems.expShare) {
									expShareCount++;
								}
							}
						}
					}
				}

				for (NBTTagCompound nbt : storage.partyPokemon) {
					if (nbt != null) {
						if (nbt.hasKey("HeldItem")) {
							int itemId = nbt.getInteger("HeldItem");
							if (itemId != -1) {
								if (PixelmonItemsHeld.getHeldItem(itemId).getHeldItemType() == EnumHeldItems.expShare) {
									calcExp(storage, p, faintedPokemon, nbt.getInteger("pixelmonID"), true, 0.5 * 1 / (double) expShareCount);
								}
							}
						}
					}
				}

				for (int userIndex : p.attackersList) {
					if (!doneUsers.contains(userIndex)) {
						calcExp(storage, p, faintedPokemon, userIndex, false, expShareCount > 0 ? 0.5 : 1);
						doneUsers.add(userIndex);
					}
				}
			}
		}
	}

	private static void calcExp(PlayerStorage storage, BattleParticipant p, EntityPixelmon faintedPokemon, int userIndex, boolean isExpShare, double scaleFactor) {
		double a, t, b, e, L, Lp, s, power;
		NBTTagCompound user = null;
		user = storage.getNBT(userIndex);
		if (user != null)
			a = 1.5;
		else
			return;
		t = 1;// traded
		b = faintedPokemon.baseStats.baseExp;
		e = ItemHeld.isItemOfType(p.currentPokemon().getHeldItem(), EnumHeldItems.luckyEgg) ? 1.5 : 1;
		L = faintedPokemon.getLvl().getLevel();
		Lp = user.getInteger("Level");
		s = 1;
		power = 1;

		double exp = ((a * b * L) / (5 * s) * (Math.pow(2 * L + 10, 2.5) / Math.pow(L + Lp + 10, 2.5)) + 1) * t * e * power;
		exp = exp * scaleFactor;
		if (userIndex == p.currentPokemon().getPokemonId()) {
			p.currentPokemon().getLvl().awardEXP((int) exp);
			if (p.currentPokemon().getLvl().canLevelUp())
				p.currentPokemon().stats.EVs.gainEV(faintedPokemon.baseStats.evGain);
			storage.updateNBT(p.currentPokemon());
		} else {
			EntityPixelmon pix;
			boolean wasAlreadyOut = false;
			if (storage.EntityAlreadyExists(userIndex, p.currentPokemon().getOwner().worldObj)) {
				pix = storage.getAlreadyExists(userIndex, p.currentPokemon().getOwner().worldObj);
				wasAlreadyOut = true;
			} else
				pix = storage.sendOut(userIndex, p.currentPokemon().getOwner().worldObj);
			pix.getLvl().awardEXP((int) exp);
			pix.stats.EVs.gainEV(faintedPokemon.baseStats.evGain);
			if (!wasAlreadyOut)
				storage.retrieve(pix);
		}
	}

}

