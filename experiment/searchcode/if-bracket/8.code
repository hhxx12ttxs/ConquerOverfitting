public static play.mvc.Result show(Long id) {
models.Bracket bracket = models.Bracket.find.byId(id);
if (bracket == null) {
models.Bracket originBracket = models.Bracket.find.byId(id);

if (originBracket == null) {
return notFound(&quot;Couldn&#39;t find bracket (id: &quot; + id + &quot;) in database&quot;);

