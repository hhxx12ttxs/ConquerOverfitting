private String replacementString;
private int replacementOffset;
private int replacementLength;
private int cursorPosition;
public XMLCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
int cursorPosition) {
this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);

