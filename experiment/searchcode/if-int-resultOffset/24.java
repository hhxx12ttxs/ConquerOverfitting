@RequestMapping(value = &quot;/twitter/friends&quot;, method = RequestMethod.GET)
public String friends(Model model, String offset) {
int resultLimit = 0;
int resultOffset = 0;
int listSize = 0;
int resultOffset = 0;
int listSize = 0;


