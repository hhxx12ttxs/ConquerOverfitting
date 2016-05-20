package pixelmon.battles.attacks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import pixelmon.RandomHelper;
import pixelmon.battles.attacks.animations.IAttackAnimation;
import pixelmon.battles.attacks.attackEffects.EffectBase;
import pixelmon.battles.attacks.attackEffects.EffectBase.ApplyStage;
import pixelmon.battles.attacks.attackModifiers.AttackModifierBase;
import pixelmon.battles.attacks.attackModifiers.AttackModifierType;
import pixelmon.battles.attacks.specialAttacks.MultiTurnSpecialAttackBase;
import pixelmon.battles.attacks.specialAttacks.SpecialAttackBase;
import pixelmon.battles.attacks.statusEffects.StatusEffectBase;
import pixelmon.battles.attacks.statusEffects.StatusEffectType;
import pixelmon.comm.ChatHandler;
import pixelmon.entities.pixelmon.EntityPixelmon;
import pixelmon.enums.EnumType;
import pixelmon.enums.heldItems.EnumHeldItems;
import pixelmon.items.ItemHeld;
import pixelmon.items.heldItems.ChoiceItem;
import pixelmon.storage.PixelmonStorage;

public class Attack {
	public static final float EFFECTIVE_NORMAL = 1, EFFECTIVE_SUPER = 2, EFFECTIVE_MAX = 4, EFFECTIVE_NOT = 0.5F, EFFECTIVE_BARELY = 0.25F, EFFECTIVE_NONE = 0;
	public static final int TYPE_NORMAL = 0, TYPE_FIRE = 1, TYPE_WATER = 2, TYPE_ELECTRIC = 3, TYPE_GRASS = 4, TYPE_ICE = 5, TYPE_FIGHT = 6, TYPE_POISON = 7, TYPE_GROUND = 8,
			TYPE_FLYING = 9, TYPE_PSYCHIC = 10, TYPE_BUG = 11, TYPE_ROCK = 12, TYPE_GHOST = 13, TYPE_DRAGON = 14, TYPE_DARK = 15, TYPE_STEEL = 16;
	public static final int ATTACK_PHYSICAL = 0, ATTACK_SPECIAL = 1, ATTACK_STATUS = 2;

	private static AttackBase[] fullAttackList = new AttackBase[600];
	public AttackBase baseAttack;
	public int pp;
	public int ppBase;
	public boolean STAB;

	public Attack(int attackIndex, String moveName, ResultSet rs) throws SQLException {
		if (fullAttackList[attackIndex] == null) {
			AttackBase a = new AttackBase(attackIndex, moveName, rs);
			fullAttackList[attackIndex] = a;
			baseAttack = a;
		} else {
			baseAttack = fullAttackList[attackIndex];
		}

		this.pp = rs.getInt("PP");
		this.ppBase = pp;
	}

	public boolean flinched = false;

	public void use(EntityPixelmon user, EntityPixelmon target, ArrayList<String> attackList, ArrayList<String> targetAttackList) {
		boolean attackHandled = false, cantMiss = false;
		flinched = false;
		user.getLookHelper().setLookPositionWithEntity(target, 0, 0);
		double accuracy = ((double) baseAttack.accuracy) * ((double) user.battleStats.getAccuracy()) / ((double) target.battleStats.getEvasion());
		double crit = calcCriticalHit(null);
		/* Check for Protect */
		for (int i = 0; i < target.status.size(); i++) {
			StatusEffectBase e = target.status.get(i);
			try {
				if (e.stopsIncomingAttack(user, target, this))
					return;
			} catch (Exception exc) {
				System.out.println("Error calculating stopsIncomingAttack for " + e.type.toString() + " for attack " + baseAttack.attackName);
				System.out.println(exc.getStackTrace());
			}

		}
		for (int i = 0; i < user.status.size(); i++) {
			StatusEffectBase e = user.status.get(i);
			try {
				if (!e.canAttackThisTurn(user, target))
					return;
			} catch (Exception exc) {
				System.out.println("Error calculating canAttackThisTurn for " + e.type.toString() + " for attack " + baseAttack.attackName);
				System.out.println(exc.getStackTrace());
			}
		}

		if (baseAttack.accuracy == -1 || cantMiss(user))
			cantMiss = true;
		for (int i = 0; i < baseAttack.effects.size(); i++) {
			EffectBase e = baseAttack.effects.get(i);
			try {
				if (e.hasSpecialAccuracyEffect())
					accuracy = e.getAccuracy(user, target);
			} catch (Exception exc) {
				System.out.println("Error calculating hasSpecialAccuracyEffect for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
				System.out.println(exc.getStackTrace());
			}
		}

		if (cantMiss || RandomHelper.getRandomNumberBetween(0, 100) <= accuracy) {
			ChatHandler.sendBattleMessage(user.getOwner(), target.getOwner(), user.getName() + " used " + baseAttack.attackName + " on " + target.getName() + "!");
			for (int j = 0; j < baseAttack.effects.size(); j++) {
				EffectBase e = baseAttack.effects.get(j);
				if (e.effectType == EffectType.Stats) {
					try {
						e.ApplyEffect(user, target, attackList);
					} catch (Exception exc) {
						System.out.println("Error in applyEffect for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
						System.out.println(exc.getStackTrace());
					}
				} else if (e.effectType == EffectType.Status) {
					if (target.status.size() > 0) {
						for (int i = 0; i < target.status.size(); i++) {
							StatusEffectBase et = target.status.get(i);
							try {
								if (!et.stopsStatusChange())
									e.ApplyEffect(user, target, attackList);
							} catch (Exception exc) {
								System.out.println("Error in applyEffect for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
								System.out.println(exc.getStackTrace());
							}
						}
					} else {
						try {
							e.ApplyEffect(user, target, attackList);
						} catch (Exception exc) {
							System.out.println("Error in applyEffect for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
							System.out.println(exc.getStackTrace());
						}
					}
				}
				if (e.effectType == EffectType.AttackModifier) {
				}
			}
			for (int i = 0; i < baseAttack.effects.size(); i++) {
				EffectBase e = baseAttack.effects.get(i);
				try {
					if (e.applyStage == ApplyStage.During) {
						if (e.effectType == EffectType.AttackModifier) {
							if (((AttackModifierBase) e).type == AttackModifierType.CriticalHit)
								crit = calcCriticalHit(e);
							else
								attackHandled = ((AttackModifierBase) e).ApplyEffect(user, target, this);
						} else if (e.effectType == EffectType.SpecialAttack)
							attackHandled = ((SpecialAttackBase) e).ApplyEffect(user, target, this, attackList, targetAttackList);

						else if (e.effectType == EffectType.MultiTurnSpecialAttack)
							attackHandled = ((MultiTurnSpecialAttackBase) e).ApplyEffect(user, target, this, attackList, targetAttackList);

					}
				} catch (Exception exc) {
					System.out.println("Error in applyEffect for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
					System.out.println(exc.getStackTrace());
				}
			}

			if (!attackHandled) {
				int power = doDamageCalc(user, target, crit);
				if (baseAttack.attackCategory == ATTACK_STATUS)
					power = 0;
				else {
					target.attackEntityFrom(DamageSource.causeMobDamage(user), power);
				}

				doMove(user, target);

				String s = null;
				if (baseAttack.attackCategory != ATTACK_STATUS) {
					if (crit > 1)
						ChatHandler.sendBattleMessage(user.getOwner(), target.getOwner(), "Critical Hit!");
					float effectiveness = EnumType.getTotalEffectiveness(target.type, baseAttack.attackType);
					if (effectiveness == EFFECTIVE_NONE)
						s = "It had no effect!";
					if (effectiveness == EFFECTIVE_NOT || effectiveness == EFFECTIVE_BARELY)
						s = "It wasn't very effective...";
					if (effectiveness == EFFECTIVE_SUPER || effectiveness == EFFECTIVE_MAX)
						s = "It's super effective!";
					if (s != null)
						ChatHandler.sendBattleMessage(user.getOwner(), target.getOwner(), s);
				}
			}

			for (int i = 0; i < baseAttack.effects.size(); i++) {
				EffectBase e = baseAttack.effects.get(i);
				try {
					if (e.applyStage == ApplyStage.End) {
						if (e.effectType == EffectType.AttackModifier) {
							if (((AttackModifierBase) e).type == AttackModifierType.Flinch)
								flinched = ((AttackModifierBase) e).ApplyEffect(user, target, this);
						} else if (e.effectType == EffectType.Remove)
							e.ApplyEffect(user, target, attackList);
					}
				} catch (Exception exc) {
					System.out.println("Error in applyEffect for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
					System.out.println(exc.getStackTrace());
				}
			}
		} else {
			ChatHandler.sendBattleMessage(user.getOwner(), target.getOwner(), user.getName() + " tried to use " + baseAttack.attackName + ", but it missed!");
			for (int i = 0; i < baseAttack.effects.size(); i++) {
				EffectBase e = baseAttack.effects.get(i);
				try {
					e.ApplyMissEffect(user, target);
				} catch (Exception exc) {
					System.out.println("Error in applyMissEffect for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
					System.out.println(exc.getStackTrace());
				}
			}
		}
		if (user.getOwner() != null)
			PixelmonStorage.PokeballManager.getPlayerStorage((EntityPlayerMP) user.getOwner()).updateNBT(user);
		if (target.getOwner() != null)
			PixelmonStorage.PokeballManager.getPlayerStorage((EntityPlayerMP) target.getOwner()).updateNBT(target);
		if (user.getTrainer() != null)
			user.getTrainer().pokemonStorage.updateNBT(user);
		if (target.getTrainer() != null)
			target.getTrainer().pokemonStorage.updateNBT(target);
		pp--;
		ItemHeld.useBattleItems(user, target);
		return;
	}

	public void doMove(EntityLiving user, EntityLiving target) {
		for (IAttackAnimation anim : baseAttack.animations)
			anim.doMove(user, target);
	}

	public int doDamageCalc(EntityPixelmon user, EntityPixelmon target, double crit) {
		double stab = 1;
		if (STAB)
			stab = 1.5;
		double type = EnumType.getTotalEffectiveness(target.type, baseAttack.attackType);
		double critical = crit;
		double rand = ((double) RandomHelper.getRandomNumberBetween(85, 100)) / 100;
		double modifier = stab * type * critical * rand;
		double attack = 0, defence = 0;
		if (baseAttack.attackCategory == ATTACK_PHYSICAL) {
			attack = ((double) user.stats.Attack) * ((double) user.battleStats.getAttackModifier()) / 100;
			defence = ((double) target.stats.Defence) * ((double) target.battleStats.getDefenceModifier()) / 100;
			if (ItemHeld.isItemOfType(user.getHeldItem(), EnumHeldItems.choiceItem)){
				attack = ((ChoiceItem)user.getHeldItem().getItem()).affectAttack(attack);
			}
		} else if (baseAttack.attackCategory == ATTACK_SPECIAL) {
			attack = ((double) user.stats.SpecialAttack) * ((double) user.battleStats.getSpecialAttackModifier()) / 100;
			defence = ((double) target.stats.SpecialDefence) * ((double) target.battleStats.getSpecialDefenceModifier()) / 100;
			if (ItemHeld.isItemOfType(user.getHeldItem(), EnumHeldItems.choiceItem)){
				attack = ((ChoiceItem)user.getHeldItem().getItem()).affectSpecialAttack(attack);
			}
		}
		double Damage = ((2 * ((float) user.getLvl().getLevel()) + 10) / 250 * (attack / defence) * baseAttack.basePower + 2) * modifier;

		for (int i = 0; i < target.status.size(); i++) {
			StatusEffectBase e = target.status.get(i);
			try {
				Damage = e.adjustDamage(this, Damage, user, target, crit);
			} catch (Exception exc) {
				System.out.println("Error in adjustDamage for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
				System.out.println(exc.getStackTrace());
			}
		}
		return (int) Math.floor(Damage);
	}

	public double calcCriticalHit(EffectBase e) {
		int critStage = 1;
		int percent = 6;
		if (e != null) {
			if (e.effectType == EffectType.AttackModifier)
				if (((AttackModifierBase) e).type == AttackModifierType.CriticalHit)
					critStage += e.value;

			if (critStage == 1)
				percent = 6;
			else if (critStage == 2)
				percent = 13;
			else if (critStage == 3)
				percent = 25;
			else if (critStage == 4)
				percent = 33;
			else if (critStage == 5)
				percent = 50;
		}
		if (RandomHelper.getRandomNumberBetween(0, 100) < percent)
			return 2;

		return 1;
	}

	public boolean canHit(EntityPixelmon pixelmon1, EntityPixelmon pixelmon2) {
		if (pixelmon2 == null) {
			return false;
		}

		if (pixelmon1.isDead || pixelmon1.isFainted || pixelmon2.isDead || pixelmon2.isFainted) {
			return false;
		}

		return true;
	}

	public static boolean canMovesHit(EntityPixelmon entity, EntityPixelmon target) {
		boolean[] b = new boolean[4];
		int i1 = 0;
		b[0] = b[1] = b[2] = b[3] = true;
		for (int i = 0; i < entity.moveset.size(); i++) {
			Attack a = entity.moveset.get(i);
			if (!a.canHit(entity, target)) {
				b[i1] = false;
			}
			i1++;
		}
		if (!(b[0] && b[1] && b[2] && b[3]))
			return false;

		return true;
	}

	public static Attack getWhichMoveIsBest(List<Attack> moves, ArrayList<EnumType> types, EntityPixelmon releasedPokemon, EntityPixelmon entityPixelmon) {
		int i1 = 0;
		Random r = new Random();
		i1 = r.nextInt(moves.size());
		return moves.get(i1);
	}

	public void setSTAB(boolean STAB) {
		this.STAB = STAB;
	}

	public static int getAttackCategory(String categoryString) {
		if (categoryString.equalsIgnoreCase("Special"))
			return ATTACK_SPECIAL;
		else if (categoryString.equalsIgnoreCase("Physical"))
			return ATTACK_PHYSICAL;
		else if (categoryString.equalsIgnoreCase("Status"))
			return ATTACK_STATUS;
		else {
			System.out.println("Unknown Attack Category: " + categoryString);
			return -1;
		}
	}

	public boolean doesPersist(EntityPixelmon entityPixelmon) {
		if (baseAttack.attackName.equalsIgnoreCase("Fly") || baseAttack.attackName.equalsIgnoreCase("Bounce")) {
			for (int i = 0; i < entityPixelmon.status.size(); i++) {
				StatusEffectBase s = entityPixelmon.status.get(i);
				if (s.type == StatusEffectType.Flying)
					return true;
			}
			return false;
		}
		for (int i = 0; i < baseAttack.effects.size(); i++) {
			EffectBase e = baseAttack.effects.get(i);
			try {
				if (e.doesPersist(entityPixelmon))
					return true;
			} catch (Exception exc) {
				System.out.println("Error in doesPersist for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
				System.out.println(exc.getStackTrace());
			}

		}
		return false;
	}

	public boolean cantMiss(EntityPixelmon user) {
		for (int i = 0; i < baseAttack.effects.size(); i++) {
			EffectBase e = baseAttack.effects.get(i);
			try {
				if (e instanceof MultiTurnSpecialAttackBase)
					if (((MultiTurnSpecialAttackBase) e).cantMiss(user))
						return true;
			} catch (Exception exc) {
				System.out.println("Error in cantMiss for " + e.effectType.toString() + " for attack " + baseAttack.attackName);
				System.out.println(exc.getStackTrace());
			}
		}
		return false;
	}
}
