int intVersion = new Integer(version).intValue();
ObjectStore objectStore = ObjectFactory.getObjectStore(&quot;Magellan&quot;, myCESession);
int versionNumber = doc1.getPropertyIntValue(Property.MAJOR_VERSION_NUMBER);
if (versionNumber == intVersion)

