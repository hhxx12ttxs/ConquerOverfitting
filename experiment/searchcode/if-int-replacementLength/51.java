public class DartCompletionProposal extends CompletetionProposal implements Comparable<DartCompletionProposal> {
private int relevance;

public DartCompletionProposal(int relevance, String replacementString, int replacementOffset, int replacementLength,
CharSequence label, Supplier<Node> graphicSupplier) {

