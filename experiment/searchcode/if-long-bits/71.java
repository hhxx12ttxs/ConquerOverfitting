final class HugeEnumSet<E extends Enum<E>> extends EnumSet<E> {

final private E[] enums;

private long[] bits;
int endElementInBits = elementInBits;
long range = 0;
if (startBitsIndex == endBitsIndex) {
range = (-1l >>> (BIT_IN_LONG -(endElementInBits - startElementInBits + 1))) << startElementInBits;

