public static Domain ensure(Session session, String name) {
Query q = session.createQuery(&quot;from Domain where domain = :domain&quot;).setParameter(&quot;domain&quot;, name);
List list = q.list();
if (list.isEmpty()) {
domain = new Domain(name);
session.save(domain);

