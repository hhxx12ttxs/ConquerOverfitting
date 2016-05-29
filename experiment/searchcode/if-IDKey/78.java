public static <T> String getNextId(Class<T> clazz) {
//
EntityManager em = EntityManager.getInstance();

String idKey = clazz.getName();
IdValue idValue = em.find(IdValue.class, idKey);

if(idValue == null) {
idValue = new IdValue(idKey);

