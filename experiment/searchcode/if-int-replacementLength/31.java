public final int replacementLength;

public AutoCompleterItem(String payload, String[] extras, int replacementLength) {
public AutoCompleterItem(String payload, String[] extras, int cursorAdjust, boolean keepSelection, int replacementLength) {
this.payload = payload;
this.extras = extras;

