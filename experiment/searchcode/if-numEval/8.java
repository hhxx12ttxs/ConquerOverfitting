public static final String HEIGHT = &quot;_height&quot;; //$NON-NLS-1$

/**
* Adds metadata to an element. Modifies if the name already exists.
public static void addMetaData(URNspec urnspec, String name, String value) {
Metadata md = getMetaDataObj(urnspec, name);
if (md == null) {
Metadata data = (Metadata) ModelCreationFactory.getNewObject(urnspec, Metadata.class);

