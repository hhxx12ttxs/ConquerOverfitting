@RequestMapping(value = &quot;facebook/notifications&quot;, method = RequestMethod.GET)
public String getFriendRequests(Model model, String offset) {
int resultOffset = 0;
int resultLimit = 0;
int listSize = 0;
int int_offset = 0;
if (offset != null) {
int_offset = Integer.valueOf(offset);

