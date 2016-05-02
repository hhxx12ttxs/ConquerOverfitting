/**
 * 
 */
package atlas.jetty.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.log.Log;

/**
 * @author formica
 *
 */
public class JdbcHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.jetty.server.Handler#handle(java.lang.String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
		Log.getLogger((String)null).info("Getting datasource...");
		InitialContext ic = null;
		Connection conn = null;
		try {
			ic = new InitialContext();
			DataSource myDS = (DataSource)ic.lookup("jdbc/OraDS");
			Log.getLogger((String)null).info("DS is "+myDS);
			
			conn = myDS.getConnection();
			Log.getLogger((String)null).info("Connection is "+conn);
			
			PreparedStatement pstmt;
			pstmt = conn.prepareStatement("SELECT * FROM ALIGN_CONF");
			if (pstmt.execute()) {
				ResultSet rs = pstmt.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				int ncol = rsmd.getColumnCount();
				System.out.println("metadata columns "+ncol);
				while (rs.next()) {
					System.out.print("received row " + rs.getRow());
					// +" "+rs.getString(0)+" "+rs.getString(1));
					for (int icol = 1; icol <= ncol; icol++) {
						System.out.print(rsmd.getColumnName(icol)+"="+rs.getObject(rsmd.getColumnName(icol))+", ");
					}
					System.out.println();
				}
			}
			pstmt.close();
			
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
			    // ... code to handle exceptions
		} finally {
			    if (conn != null)
					try {
						conn.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		}
		
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>Hello World, this is my JdbcHandler</h1>");
	}

}

