for (NUnserializerFactory factory : factorys) {
if (factory.version == requiredVersion) {
return factory;
}
}
return null;
ObjectTypeQuerier objectTypeQuerier) {
for (NUnserializerFactory factory : factorys) {
if (factory.version == requiredVersion) {

