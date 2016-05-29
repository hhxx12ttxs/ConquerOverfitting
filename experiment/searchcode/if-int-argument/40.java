public DuplicateArgumentException(IntStream input) {
super(input);
}

public DuplicateArgumentException(IntStream input, String argument) {
super(input);
this.argument = argument;
}

@Override

