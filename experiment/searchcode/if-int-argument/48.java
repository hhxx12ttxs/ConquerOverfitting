private static final int BITS_PER_ARGUMENT = (AbstractMessage.BITS_PER_INT - AbstractMessage.BITS_FOR_OPCODE) / 2;

private void validateArgument(int argument) throws InvalidArgumentException {
if (argument < 0) {
} else if (argument > (int) Math.pow(2, BITS_PER_ARGUMENT) - 1) {
throw new InvalidArgumentException(&quot;Provided argument &quot; + arg1

