return JPAEditorMessages.DirectEditJPAEntityFeature_bracesNotAllowedMsg;
for (int i = 0; i < value.length(); i++) {
if (allowed.indexOf(value.charAt(i)) < 0)
String specifiedEntityMappingName = JpaArtifactFactory.instance().getSpecifiedEntityName(jpt);
if(specifiedEntityMappingName == null){

