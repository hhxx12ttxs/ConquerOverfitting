private int relevance;

public DartCompletionProposal(int relevance, String replacementString, int replacementOffset, int replacementLength,
CharSequence label, Supplier<Node> graphicSupplier) {
super(replacementString, replacementOffset, replacementLength, label, graphicSupplier);

