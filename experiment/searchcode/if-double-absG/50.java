* - This version active check for Deserialization Vulnerability IF AND ONLY IF
* the base value is already a serialized Java Object. Maybe can be useful to add
* - Maybe search also in headers (I don&#39;t know if Burp set all headers as insertion
* points...)
*/

@Override
public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks)

