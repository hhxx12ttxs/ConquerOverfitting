FormProcessor fp = new FormProcessor(request);
if (fp.getString(&quot;showMoreLink&quot;).equals(&quot;&quot;)) {
showMoreLink = true;
StudyBean studyWithEventDefinitions = currentStudy;
if (currentStudy.getParentStudyId() > 0) {

