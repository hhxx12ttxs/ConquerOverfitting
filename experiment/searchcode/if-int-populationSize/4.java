private final int bodySize;
private final int populationSize;
private final int fatFood;
private final List<Trait> traits;

public Species(int food, int bodySize, int populationSize, List<Trait> traits) {
this(food, bodySize, populationSize, 0, traits);

