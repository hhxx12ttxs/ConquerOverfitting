public class Concierge extends HttpServlet {
Connection c = null;
TimeZone pst = TimeZone.getTimeZone(&quot;America/Los_Angeles&quot;);
int hoursOffset = (pst.getDSTSavings()>0)?7:8;

public void doGet(HttpServletRequest req, HttpServletResponse resp)

