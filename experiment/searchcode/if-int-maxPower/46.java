public class PowerContainer implements IPowerContainer, ISaveable {
private EnumTechLevel level;
private int maxPower;
private int currentPower;

public PowerContainer(EnumTechLevel level, int maxPower) {
this.level = level;

