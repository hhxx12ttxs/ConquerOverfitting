connection = new LdapNetworkConnection(jptDirectorySearch.getProperty(&quot;HOST&quot;), Integer.parseInt(jptDirectorySearch.getProperty(&quot;PORT&quot;)), Boolean.parseBoolean(jptDirectorySearch
.getProperty(&quot;SSL&quot;)));
if (&quot;TRUE&quot;.equalsIgnoreCase(jptDirectorySearch.getProperty(&quot;ANONYMOUS&quot;))) {

