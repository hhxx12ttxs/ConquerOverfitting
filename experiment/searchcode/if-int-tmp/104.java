package pixelmon.battles.attacks.specialAttacks;

import java.util.ArrayList;

import pixelmon.battles.attacks.Attack;
import pixelmon.entities.pixelmon.EntityPixelmon;
import pixelmon.enums.EnumType;

public class HiddenPower extends SpecialAttackBase {

	public HiddenPower() {
		super(SpecialAttackType.HiddenPower, ApplyStage.During, false);
	}

	@Override
	public boolean ApplyEffect(EntityPixelmon user, EntityPixelmon target, Attack attack, ArrayList<String> attackList, ArrayList<String> targetAttackList) throws Exception {
		int a, b, c, d, e, f;

		a = user.stats.IVs.HP % 2;
		b = user.stats.IVs.Attack % 2;
		c = user.stats.IVs.Defence % 2;
		d = user.stats.IVs.Speed % 2;
		e = user.stats.IVs.SpAtt % 2;
		f = user.stats.IVs.SpDef % 2;

		double fedbca = 32 * f + 16 * e + 8 * d + 4 * c + 2 * b + a;
		int type = (int) Math.floor((fedbca) * 15f / 63f);

		if (type == 0)
			attack.baseAttack.attackType = EnumType.Fighting;
		else if (type == 1)
			attack.baseAttack.attackType = EnumType.Flying;
		else if (type == 2)
			attack.baseAttack.attackType = EnumType.Poison;
		else if (type == 3)
			attack.baseAttack.attackType = EnumType.Ground;
		else if (type == 4)
			attack.baseAttack.attackType = EnumType.Rock;
		else if (type == 5)
			attack.baseAttack.attackType = EnumType.Bug;
		else if (type == 6)
			attack.baseAttack.attackType = EnumType.Ghost;
		else if (type == 7)
			attack.baseAttack.attackType = EnumType.Steel;
		else if (type == 8)
			attack.baseAttack.attackType = EnumType.Fire;
		else if (type == 9)
			attack.baseAttack.attackType = EnumType.Water;
		else if (type == 10)
			attack.baseAttack.attackType = EnumType.Grass;
		else if (type == 11)
			attack.baseAttack.attackType = EnumType.Electric;
		else if (type == 12)
			attack.baseAttack.attackType = EnumType.Psychic;
		else if (type == 13)
			attack.baseAttack.attackType = EnumType.Ice;
		else if (type == 14)
			attack.baseAttack.attackType = EnumType.Dragon;
		else if (type == 15)
			attack.baseAttack.attackType = EnumType.Dark;

		boolean stab = false;
		for (EnumType t : user.type) {
			if (t == attack.baseAttack.attackType) {
				stab = true;
				break;
			}
		}

		int u = 0, v = 0, w = 0, x = 0, y = 0, z = 0;
		int tmp;
		tmp = user.stats.IVs.HP % 4;
		if (tmp == 2 || tmp == 3)
			u = 1;
		tmp = user.stats.IVs.Attack % 4;
		if (tmp == 2 || tmp == 3)
			v = 1;
		tmp = user.stats.IVs.Defence % 4;
		if (tmp == 2 || tmp == 3)
			w = 1;
		tmp = user.stats.IVs.Speed % 4;
		if (tmp == 2 || tmp == 3)
			x = 1;
		tmp = user.stats.IVs.SpAtt % 4;
		if (tmp == 2 || tmp == 3)
			y = 1;
		tmp = user.stats.IVs.SpDef % 4;
		if (tmp == 2 || tmp == 3)
			z = 1;

		attack.baseAttack.basePower = (int) Math.floor(((double) (u + 2 * v + 4 * w + 8 * x + 16 * y + 32 * z)) * 40 / 63 + 30);
		return false;
	}
}

