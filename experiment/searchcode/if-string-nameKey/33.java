private String nameKey;
private HashMap<String, String> optionKeyToOption;


public static enum Type {
this(name, nameKey, PlanEntry.typeFromCharacter(type), options, optionKeys);
}

public PlanEntry(String name, String nameKey, PlanEntry.Type type)
{
if (type != PlanEntry.Type.CHECKBOX)

