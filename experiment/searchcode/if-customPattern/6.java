private Text customEntryText;
private Label statusMessageLabel;

private int selectedAction;
private String customPattern;
if (selectedAction == ADD_CUSTOM_ENTRY) {
customPattern = customEntryText.getText();
if (customPattern.length() == 0) {
setError(Policy.bind(&quot;IgnoreResourcesDialog.patternMustNotBeEmpty&quot;)); //$NON-NLS-1$

