@RequestMapping(value = &quot;/defaultyear/add&quot;, method = RequestMethod.POST)
public String addDefaultYear(
@ModelAttribute(&quot;defaultYear&quot;) DefaultYear defaultYear) {

if (defaultYear.getDefaultYearID() == 0) {
@RequestMapping(&quot;/removedefaultyear/{id}&quot;)
public String removeDefaultyear(@PathVariable(&quot;id&quot;) int id) {
this.defaultYearService.delete(id);

