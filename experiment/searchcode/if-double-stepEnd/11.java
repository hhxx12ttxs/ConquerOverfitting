@QueryParam(&quot;rangeStart&quot;) double rangeStart,
@QueryParam(&quot;rangeEnd&quot;) @DefaultValue(&quot;1&quot;) double rangeEnd,
int endOffset = checkEndOffset(rangeEnd);

entityIds = EntityRelationsUtil.convertIdStringToList(eIds);

if (entityIds.size() == 1) {

