private static Properties defaults = null;





public static void normalize(CardDavContact contact) {
CountryNormalizer.normalize(contact);
NameNormalizer.normalize(contact);
}





private static void ensureDefaultsAreLoaded() {
if (defaults == null) {

