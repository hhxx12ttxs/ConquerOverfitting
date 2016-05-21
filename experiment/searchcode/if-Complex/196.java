public static void main(String[] args) throws Exception {
// Uncomment these two lines to check out if Issue #15 is fixed: http://code.google.com/p/javasimon/issues/detail?id=15
//Simon jdbcSimon = SimonManager.getStopwatch(org.javasimon.jdbc4.Driver.DEFAULT_PREFIX);
 */
public final class Complex extends Simple {
Complex complex = new Complex();
complex.setUp();
 *
 * @throws SQLException if something goes wrong
 */
complex.doInsert(conn);
complex.doInsertSimple(conn);

