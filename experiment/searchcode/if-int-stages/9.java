package pixelmon.entities.pixelmon.stats;

import pixelmon.entities.pixelmon.Entity6CanBattle;
import pixelmon.entities.pixelmon.EntityPixelmon;
import pixelmon.enums.heldItems.EnumHeldItems;
import pixelmon.items.ItemHeld;
import pixelmon.items.heldItems.ChoiceItem;
import net.minecraft.nbt.NBTTagCompound;

public class BattleStats {
	private EntityPixelmon pixelmon;
	private int Accuracy, Evasion, AttackModifier, DefenceModifier, SpecialAttackModifier, SpecialDefenceModifier, SpeedModifier;
	private int[] stages = new int[7];

	public int getAccuracy() {
		return Accuracy;
	}

	public int getEvasion() {
		return Evasion;
	}

	public double getAttackModifier() {
		if (ItemHeld.isItemOfType(pixelmon.getHeldItem(), EnumHeldItems.choiceItem)) {
			return ((ChoiceItem) pixelmon.getHeldItem().getItem()).affectAttack(AttackModifier);
		}
		return AttackModifier;
	}

	public int getDefenceModifier() {
		return DefenceModifier;
	}

	public double getSpecialAttackModifier() {
		if (ItemHeld.isItemOfType(pixelmon.getHeldItem(), EnumHeldItems.choiceItem)) {
			return ((ChoiceItem) pixelmon.getHeldItem().getItem()).affectSpecialAttack(SpecialAttackModifier);
		}
		return SpecialAttackModifier;
	}

	public int getSpecialDefenceModifier() {
		return SpecialDefenceModifier;
	}

	public double getSpeedModifier() {
		if (ItemHeld.isItemOfType(pixelmon.getHeldItem(), EnumHeldItems.choiceItem)) {
			return ((ChoiceItem) pixelmon.getHeldItem().getItem()).affectSpeed(SpeedModifier);
		}
		return SpeedModifier;
	}

	public void setIsParalyzed() {
		SpeedModifier /= 4;
	}

	public void setNotParalyzed() {
		SpeedModifier *= 4;
	}

	public boolean IncreaseAccuracy(int amount) {
		return IncreaseStat(amount, "Accuracy");
	}

	public boolean DecreaseAccuracy(int amount) {
		return DecreaseStat(amount, "Accuracy");
	}

	public boolean IncreaseEvasion(int amount) {
		return IncreaseStat(amount, "Evasion");
	}

	public boolean DecreaseEvasion(int amount) {
		return DecreaseStat(amount, "Evasion");
	}

	public boolean IncreaseAttack(int amount) {
		return IncreaseStat(amount, "AttackModifier");
	}

	public boolean DecreaseAttack(int amount) {
		return DecreaseStat(amount, "AttackModifier");
	}

	public boolean IncreaseDefence(int amount) {
		return IncreaseStat(amount, "DefenceModifier");
	}

	public boolean DecreaseDefence(int amount) {
		return DecreaseStat(amount, "DefenceModifier");
	}

	public boolean IncreaseSpecialAttack(int amount) {
		return IncreaseStat(amount, "SpecialAttackModifier");
	}

	public boolean DecreaseSpecialAttack(int amount) {
		return DecreaseStat(amount, "SpecialAttackModifier");
	}

	public boolean IncreaseSpecialDefence(int amount) {
		return IncreaseStat(amount, "SpecialDefenceModifier");
	}

	public boolean DecreaseSpecialDefence(int amount) {
		return DecreaseStat(amount, "SpecialDefenceModifier");
	}

	public boolean IncreaseSpeed(int amount) {
		return IncreaseStat(amount, "SpeedModifier");
	}

	public boolean DecreaseSpeed(int amount) {
		return DecreaseStat(amount, "SpeedModifier");
	}

	public BattleStats(Entity6CanBattle entity6CanBattle) {
		this.pixelmon = (EntityPixelmon) entity6CanBattle;
		clearBattleStats();
	}

	/**
	 * Gets the new value of the stat. Specific to Accuracy or Evasion
	 * 
	 * @param stage
	 *            - The currrent stage of the stat (bounds of -6 to 6)
	 * @return The new value for the stat
	 */
	private int GetAccOrEva(double stage) {
		if (stage < 1)
			return (int) Math.round((3 / (Math.abs(stage) + 3)) * 100);
		else
			return (int) Math.round(((Math.abs(stage) + 3) / 3) * 100);
	}

	/**
	 * Gets the new value of the stat
	 * 
	 * @param stage
	 *            - The currrent stage of the stat (bounds of -6 to 6)
	 * @return The new value for the stat
	 */
	private int GetStat(double stage) {
		if (stage < 1)
			return (int) Math.round((2 / (Math.abs(stage) + 2)) * 100);
		else
			return (int) Math.round(((Math.abs(stage) + 2) / 2) * 100);
	}

	/**
	 * Increases the given stat the amount of stages
	 * 
	 * @param amount
	 *            - The amount of stages to increase the stat
	 * @param stat
	 *            - The name of the stat to increase
	 * @return Whether the change was successful
	 */
	public boolean IncreaseStat(int amount, String stat) {
		if (amount < 0)
			return DecreaseStat(Math.abs(amount), stat); // If received a
															// negative value
															// then we assume we
															// need to decrease
															// stat
		int stageIndex = GetStageIndex(stat);
		int currentStage = stages[stageIndex];
		if (currentStage == 6)
			return false; // If currently at highest stage return failed
		currentStage += Math.abs(amount);
		if (currentStage > 6)
			currentStage = 6; // If new stage is higher then allowed reset to
								// max

		int newValue;
		if (stageIndex < 2)
			newValue = GetAccOrEva(currentStage);
		else
			newValue = GetStat(currentStage);

		stages[stageIndex] = currentStage;
		ChangeStat(stat, newValue);
		return true;
	}

	/**
	 * Decreases the given stat the amount of stages
	 * 
	 * @param amount
	 *            - The amount of stages to decrease the stat
	 * @param stat
	 *            - The name of the stat to increase
	 * @return Whether the change was successful
	 */
	public boolean DecreaseStat(int amount, String stat) {
		int stageIndex = GetStageIndex(stat);
		int currentStage = stages[stageIndex];
		if (currentStage == -6)
			return false; // If currently at lowest stage return failed
		currentStage -= Math.abs(amount);
		if (currentStage < -6)
			currentStage = -6; // If new stage is lower then allowed reset to
								// max

		int newValue;
		if (stageIndex < 2)
			newValue = GetAccOrEva(currentStage);
		else
			newValue = GetStat(currentStage);

		stages[stageIndex] = currentStage;
		ChangeStat(stat, newValue);
		return true;
	}

	/**
	 * Changes a stat to its value
	 * 
	 * @param stat
	 *            - The name of the stat to change
	 * @param value
	 *            - The new value to change the stat to
	 */
	private void ChangeStat(String stat, int value) {
		if (stat.equalsIgnoreCase("Accuracy"))
			Accuracy = value;
		if (stat.equalsIgnoreCase("Evasion"))
			Evasion = value;
		if (stat.equalsIgnoreCase("AttackModifier"))
			AttackModifier = value;
		if (stat.equalsIgnoreCase("DefenceModifier"))
			DefenceModifier = value;
		if (stat.equalsIgnoreCase("SpecialAttackModifier"))
			SpecialAttackModifier = value;
		if (stat.equalsIgnoreCase("SpecialDefenceModifier"))
			SpecialDefenceModifier = value;
		if (stat.equalsIgnoreCase("SpeedModifier"))
			SpeedModifier = value;
	}

	/**
	 * Gets the array index for the stage array of the equivalent stat
	 * 
	 * @param stat
	 *            - The stat you need the index for
	 * @return The index to the stage array based on the given stat
	 */
	private int GetStageIndex(String stat) {
		if (stat.equalsIgnoreCase("Accuracy"))
			return 0;
		if (stat.equalsIgnoreCase("Evasion"))
			return 1;
		if (stat.equalsIgnoreCase("AttackModifier"))
			return 2;
		if (stat.equalsIgnoreCase("DefenceModifier"))
			return 3;
		if (stat.equalsIgnoreCase("SpecialAttackModifier"))
			return 4;
		if (stat.equalsIgnoreCase("SpecialDefenceModifier"))
			return 5;
		if (stat.equalsIgnoreCase("SpeedModifier"))
			return 6;
		return -1;
	}

	public void writeToNBT(NBTTagCompound var1) {
		var1.setInteger("BattleAccuracy", Accuracy);
		var1.setInteger("BattleEvasion", Evasion);
		var1.setInteger("BattleAttackModifier", AttackModifier);
		var1.setInteger("BattleDefenceModifier", DefenceModifier);
		var1.setInteger("BattleSpecialAttackModifier", SpecialAttackModifier);
		var1.setInteger("BattleSpecialDefenceModifier", SpecialDefenceModifier);
		var1.setInteger("BattleSpeedModifier", SpeedModifier);
	}

	/**
	 * Reset battle specific stats.
	 */
	public void clearBattleStats() {
		AttackModifier = DefenceModifier = SpecialAttackModifier = SpecialDefenceModifier = SpeedModifier = Accuracy = Evasion = 100;
		stages = new int[7];
	}

	public void readFromNBT(NBTTagCompound var1) {
		Accuracy = var1.getInteger("BattleAccuracy");
		Evasion = var1.getInteger("BattleEvasion");
		AttackModifier = var1.getInteger("BattleAttackModifier");
		DefenceModifier = var1.getInteger("BattleDefenceModifier");
		SpecialAttackModifier = var1.getInteger("BattleSpecialAttackModifier");
		SpecialDefenceModifier = var1.getInteger("BattleSpecialDefenceModifier");
		SpeedModifier = var1.getInteger("BattleSpeedModifier");
	}

	public void setSpeedModifier(int value) {
		SpeedModifier = value;
	}

	public void setAttackModifier(int value) {
		AttackModifier = value;
	}

	public void setDefenceModifier(int value) {
		DefenceModifier = value;
	}

	public void setSpecialAttackModifier(int value) {
		SpecialAttackModifier = value;
	}

	public void setSpecialDefenceModifier(int value) {
		SpecialDefenceModifier = value;
	}

	public void setEvasion(int value) {
		Evasion = value;
	}

	public void setAccuracy(int value) {
		Accuracy = value;
	}

	public void copyStats(BattleStats battleStats) {
		SpeedModifier = battleStats.SpeedModifier;
		AttackModifier = battleStats.AttackModifier;
		DefenceModifier = battleStats.DefenceModifier;
		SpecialAttackModifier = battleStats.SpecialAttackModifier;
		SpecialDefenceModifier = battleStats.SpecialDefenceModifier;
		Evasion = battleStats.Evasion;
		Accuracy = battleStats.Accuracy;
	}

	public int getSum() {
		return SpeedModifier + AttackModifier + DefenceModifier + SpecialAttackModifier + SpecialDefenceModifier + Evasion + Accuracy;
	}

}

