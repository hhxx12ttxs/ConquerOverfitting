public enum Type {Combat, Range, Blackpowder, Defence, Misc, Special};
public Type type = Type.Combat;
public int rarity = 0;//0 is common
public int cost = 1;

public void setType(String s) {
if (s == &quot;combat&quot;) type = Type.Combat;

