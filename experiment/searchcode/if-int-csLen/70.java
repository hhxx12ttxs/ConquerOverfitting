* file, {@link #CONFIG_FILE}.
*
* @throws IOException if there is a problem loading from the default configuration file.
final InputStream in = ClassLoaderUtils.getResourceAsStream(CONFIG_FILE, getClass());
if (in != null)
{
try

