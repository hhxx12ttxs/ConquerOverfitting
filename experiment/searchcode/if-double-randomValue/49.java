this.generator = generator;
}

public Symbol choose(List<Symbol> symbols) {
final double randomValue = generator.generate(getTotalWeight(symbols));
double runningWeight = 0;
for (Symbol matchingSymbol : symbols) {

