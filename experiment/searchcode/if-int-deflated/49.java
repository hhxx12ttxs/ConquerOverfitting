// check for the unique identifier <idNum> field.
if(!deflated.has(&quot;idNum&quot;))
{
throw new JsonParseException(&quot;The serialized Project did not contain the required idNum field.&quot;);
// for all other attributes: instantiate as null, fill in if given.

//int idNum = deflated.get(&quot;idNum&quot;).getAsInt();
String idNum = deflated.get(&quot;idNum&quot;).getAsString();

