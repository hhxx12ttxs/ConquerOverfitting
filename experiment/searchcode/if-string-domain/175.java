package ee.widespace.stat.counter;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Base servlet for counters.
 *
 * @author: Igor Malinin
 */
abstract class AbstractServlet extends HttpServlet {
	// 2 months + a little
	private static final int COOKIE_MAX_AGE = 60*60*24*64;

	/**
	 * @return  JDBC connection
	 */
	protected final Connection getConnection() throws SQLException {
		String url = getServletContext()
			.getInitParameter( "jdbc.url" );

		Connection conn = DriverManager.getConnection( url );
		try {
			conn.setAutoCommit( false );
			return conn;
		} catch ( SQLException e ) {
			conn.close();
			throw e;
		}
	}

	/**
	 * Cookie domain, for example ".widespace.ee".
	 *
	 * @return  cookie domain
	 */
	private String getCookieDomain( HttpServletRequest req ) {
		String domain = getServletContext()
			.getInitParameter( "cookie.domain" );

		if ( domain == null || domain.length() == 0 ) {
			domain = req.getServerName();
			int pos = domain.indexOf( '.' );
			if ( pos > 0 ) {
				domain = domain.substring( pos );
			}
		}
		
		return domain;
	}

	/**
	 * Random string which is used as unique visitor
	 * identification.
	 *
	 * @return  random string
	 */
	private String getRandomCookie() {
		try {
			MessageDigest hash = MessageDigest.getInstance( "SHA-1" );

			long time = System.currentTimeMillis();
			for ( int i = 0; i < 8; i++ ) {
				hash.update( (byte) time );
				time >>>= 8;
			}

			byte[] digest = hash.digest();
			int len = digest.length / 2;

			StringBuffer buf = new StringBuffer( len );
			for ( int i = 0, j = 0; i < len; i++ ) {
				int n = ( ((digest[ j ] & 0xFF) << 8)
						+ (digest[ j++ ] & 0xFF) ) % 62;

				if ( n < 26 ) {
					buf.append( (char) (n + 'a') );
					continue;
				}
				n -= 26;

				if ( n < 26 ) {
					buf.append( (char) (n + 'A') );
					continue;
				}
				n -= 26;

				buf.append( n );
			}

			return buf.toString();
		} catch ( NoSuchAlgorithmException e ) {
			throw new RuntimeException( e.getMessage() );
		}
	}

	protected final String[] preprocess(
		HttpServletRequest req, HttpServletResponse res
	) throws IOException {
		res.setHeader( "Expires", "01 Dec 1994 16:00:00 GMT");
		res.setHeader( "Cache-control", "no-cache" );
		res.setHeader( "Pragma",        "no-cache" );

		String visitor = null;

		Cookie[] cookies = req.getCookies();
		if ( cookies != null ) {
			for ( int i = 0; i < cookies.length; i++ ) {
				Cookie cookie = cookies[ i ];
				if ( "UtStatUV".equalsIgnoreCase(cookie.getName()) ) {
					visitor = cookie.getValue();

					if ( visitor.length() != 10 || visitor.startsWith("aaa") ) {
						// old cookie sheme; just drop and rereate cookie
						break;
					}

					ServletContext context = getServletContext();
					cookie.setDomain( getCookieDomain(req) );
					cookie.setMaxAge( COOKIE_MAX_AGE );
					res.addCookie( cookie );
					break;
				}
			}
		}

		if ( visitor == null && req.getParameter("cookie") == null ) {
			String random = getRandomCookie();
			Cookie cookie = new Cookie( "UtStatUV", random );
			cookie.setDomain( getCookieDomain(req) );
			cookie.setMaxAge( COOKIE_MAX_AGE );
			res.addCookie( cookie );

			String redirect = req.getRequestURL()
				.append( '?' ).append( req.getQueryString() )
				.append( '&' ).append( "cookie=on" )
				.toString();

			res.sendRedirect( redirect );
			return null;
		}

		String address = req.getRemoteAddr();
		String forward = req.getHeader( "X-Forwarded-For" );
		if ( forward != null && forward.length() > 0 ) {
			address = forward + ", " + address;
		}

		return new String[] { visitor, address };
	}
}

