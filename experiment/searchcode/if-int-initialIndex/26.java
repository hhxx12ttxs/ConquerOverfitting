public final class IntervalTextIterator implements ITextIterator {
private int currentIndex;
private final int initialIndex;
private int total;
public int next() {
return currentIndex++;
}

@Override
public void reset() {
currentIndex = initialIndex;
}
}

