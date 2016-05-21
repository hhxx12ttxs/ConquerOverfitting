if (complex) {
if (CMDBSession.get().getConfig().useTreeComboBox()) {
String url = v.get(CIModel.CI_ICON_PATH);
((CIReferenceColumnConfig)column).setPermissions(perm);
} else if (config.isComplex()) {
if (config.isSelectTemplates()) {
text = v.getValueDisplayName();
String text = item.getValue();
if (complex && v.getValue() != null) {
String url = v.get(CIModel.CI_ICON_PATH);
text = v.getValueDisplayName();
if (config.isComplex() && v.getValue() != null) {
if (item.isComplex()) {
text = item.getValueDisplayName();

