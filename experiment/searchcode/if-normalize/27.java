public Contact normalize(Contact contact) {
if (contact==null) { return null; }

//normalize name
contact.setName(normalizeName(contact.getName()));
public PostalAddress normalize(PostalAddress address) {
if (address==null) { return null; }

