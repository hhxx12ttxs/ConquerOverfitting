package ee.widespace.banner.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.*;

import ee.widespace.banner.*;


/**
 * Insert the type's description here.
 * Creation date: (6/13/2001 14:58:55 PM)
 * @author: Vladislav Vislogubov
 */
public class BannerDataImpl implements BannerData {
	private Connection conn;
	private boolean operationExecuted;
/**
 * BannerDataImpl constructor comment.
 */
public BannerDataImpl( Connection conn ) {
	this.conn = conn;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 14:58:55 PM)
 * @param banner ee.widespace.banner.Banner
 */
public void addBanner( Banner banner ) throws SQLException {
	PreparedStatement stmtQB = conn.prepareStatement(
	"SELECT \"Name\" FROM \"UtBanner\" WHERE \"Name\"=? "+
	"AND \"Site\"=?"	
	);
	try {
		stmtQB.setString(1, banner.name );
		stmtQB.setString(2, banner.site );
		ResultSet rset = stmtQB.executeQuery();
		if (rset.next()) {
			throw new SQLException("Banner :"+ banner.name +" can't be added, becouse banner with this name is existed");
						} 
	PreparedStatement stmt = conn.prepareStatement(
		"INSERT INTO \"UtBanner\" (\"Site\", \"Name\", \"Img\", \"URL\", \"Alt\", \"Info\") VALUES(?, ?, ?, ?, ?, ?)"
	); try {
		stmt.setString( 1, banner.site );
		stmt.setString( 2, banner.name );
		stmt.setString( 3, banner.img );
		stmt.setString( 4, banner.url );
		stmt.setString( 5, banner.alt );
		stmt.setString( 6, banner.info );
		stmt.executeUpdate();
	} finally {stmt.close();}
	} finally {stmtQB.close();}
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 14:58:55 PM)
 * @param banner ee.widespace.banner.Banner
 */
public void addBannerInRt( BannerInRt bannerInRt ) throws SQLException {
	PreparedStatement stmtQB = conn.prepareStatement(
	"SELECT \"Banner\" FROM \"UtBannerInRt\" WHERE \"Banner\"=? "+
	"AND \"Site\"=? AND \"Rotation\"=?"	
	);
	try {
		stmtQB.setString(1, bannerInRt.banner );
		stmtQB.setString(2, bannerInRt.site );
		stmtQB.setString(3, bannerInRt.rotation );
		ResultSet rset = stmtQB.executeQuery();
		if (rset.next()) {
			throw new SQLException("Banner :"+ bannerInRt.banner +" can't be added in rotation"+bannerInRt.rotation+", becouse banner with this name is existed");
						} 
	PreparedStatement stmt = conn.prepareStatement(
		"INSERT INTO \"UtBannerInRt\" (\"Rotation\", \"Site\", \"Banner\", \"SDate\", \"EDate\", \"Counter\", \"Frequency\") VALUES(?, ?, ?, ?, ?, ?, ?)"
	); try {
		stmt.setString( 1, bannerInRt.rotation );
		stmt.setString( 2, bannerInRt.site );
		stmt.setString( 3, bannerInRt.banner );
		stmt.setString( 4, bannerInRt.sdate );
		stmt.setString( 5, bannerInRt.edate );
		stmt.setInt( 6, bannerInRt.counter );
		stmt.setInt( 7, bannerInRt.frequency );
		stmt.executeUpdate();
	} finally {stmt.close();}
	}finally {stmtQB.close();}
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @param owner ee.widespace.banner.BannerOwner
 */
public void addBannerRt( BannerRt bannerRt ) throws SQLException {
	PreparedStatement stmtQBRt = conn.prepareStatement(
	"SELECT \"Name\" FROM \"UtBannerRt\" WHERE \"Name\"=?"
	);
	try {
		stmtQBRt.setString(1, bannerRt.name);
		ResultSet rset = stmtQBRt.executeQuery();
		if (rset.next()) {
			throw new SQLException("BannerRt :"+ bannerRt.name +" can't be added, becouse bannerRt with this name is already existed");
						} 
	PreparedStatement stmt = conn.prepareStatement(
		"INSERT INTO \"UtBannerRt\" (\"Name\", \"Info\") VALUES(?, ?)"
	); try {
		stmt.setString( 1, bannerRt.name );
		stmt.setString( 2, bannerRt.info );
		stmt.executeUpdate();
	} finally {stmt.close();}
	} finally {stmtQBRt.close();}
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @param owner ee.widespace.banner.BannerOwner
 */
public void addSite( Site site ) throws SQLException {
	PreparedStatement stmtQS = conn.prepareStatement(
	"SELECT \"Name\" FROM \"UtSite\" WHERE \"Name\"=?"
	);
	try {
		stmtQS.setString(1, site.name);
		ResultSet rset = stmtQS.executeQuery();
		if (rset.next()) {
			throw new SQLException("Site :"+ site.name +" can't be added, becouse site with this name is existed");
						} 
		PreparedStatement stmt = conn.prepareStatement(
		"INSERT INTO \"UtSite\" (\"Name\", \"URL\", \"EMail\", \"Password\", \"Info\") VALUES(?, ?, ?, ?, ?)"
	); try {
		stmt.setString( 1, site.name );
		stmt.setString( 2, site.url );
		stmt.setString( 3, site.email );
		stmt.setString( 4, site.password );
		stmt.setString( 5, site.info );
		stmt.executeUpdate();
	} finally {	stmt.close();}
	} finally {stmtQS.close();}
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 14:58:55 PM)
 * @param name java.lang.String
 */
public String bannerClicked( String site, String banner, String addr ) throws SQLException {
	Date date = new Date();
	java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat();
	dateFormat.applyPattern( "yyyy-MM-dd HH:mm:ss.sss" );
	String time = dateFormat.format( date );
	dateFormat.applyPattern( "yyyy-MM-dd" );
	String period = dateFormat.format( date );

	String url = "";
	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"URL\"" +
		" FROM \"UtBanner\" WHERE \"Site\"=? AND \"Name\"=?"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, banner );
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				url = rset.getString(1);
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}
	
	stmt = conn.prepareStatement(
		"INSERT INTO \"UtBannerClick\" (\"Site\", \"Banner\", \"Time\", \"Address\") VALUES(?, ?, ?, ?)"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, banner );
		stmt.setString( 3, time );
		stmt.setString( 4, addr );
		stmt.executeUpdate();
	} finally {
		stmt.close();
	}

	stmt = conn.prepareStatement(
		"SELECT \"Clicks\"" +
		" FROM \"UtBannerStat\""+
		" WHERE \"Site\"=? AND \"Banner\"=? AND \"Period\"=? AND \"Address\"=?"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, banner );
		stmt.setString( 3, period );
		stmt.setString( 4, addr );
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				PreparedStatement stmt2 = conn.prepareStatement(
					"UPDATE \"UtBannerStat\" SET \"Clicks\"=?"+
					" WHERE \"Site\"=? AND \"Banner\"=? AND \"Period\"=? AND \"Address\"=?"
				); try {
					stmt2.setInt( 1, rset.getInt(1)+1 );
					stmt2.setString( 2, site );
					stmt2.setString( 3, banner );
					stmt2.setString( 4, period );
					stmt2.setString( 5, addr );
					stmt2.executeUpdate();
				} finally {
					stmt2.close();
				}
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	return url;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 14:58:55 PM)
 * @param name java.lang.String
 */
public String bannerViewed( String site, String banner, String addr ) throws SQLException {
	Date date = new Date();
	java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat();
	dateFormat.applyPattern( "yyyy-MM-dd" );
	String period = dateFormat.format( date );

	String img = "";
	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Img\"" +
		" FROM \"UtBanner\" WHERE \"Site\"=? AND \"Name\"=?"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, banner );
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				img = rset.getString(1);
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}
	
	stmt = conn.prepareStatement(
		"SELECT \"Views\"" +
		" FROM \"UtBannerStat\""+
		" WHERE \"Site\"=? AND \"Banner\"=? AND \"Period\"=? AND \"Address\"=?"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, banner );
		stmt.setString( 3, period );
		stmt.setString( 4, addr );
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				PreparedStatement stmt2 = conn.prepareStatement(
					"UPDATE \"UtBannerStat\" SET \"Views\"=?"+
					" WHERE \"Site\"=? AND \"Banner\"=? AND \"Period\"=? AND \"Address\"=?"
				); try {
					stmt2.setInt( 1, rset.getInt(1)+1 );
					stmt2.setString( 2, site );
					stmt2.setString( 3, banner );
					stmt2.setString( 4, period );
					stmt2.setString( 5, addr );
					stmt2.executeUpdate();
				} finally {
					stmt2.close();
				}
			} else {
				PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO \"UtBannerStat\" "+
					"(\"Site\", \"Banner\", \"Period\", \"Address\", \"Views\", \"Clicks\") "+
					"VALUES(?, ?, ?, ?, ?, ?)"
				); try {
					stmt2.setString( 1, site );
					stmt2.setString( 2, banner );
					stmt2.setString( 3, period );
					stmt2.setString( 4, addr );
					stmt2.setInt( 5, 1 );
					stmt2.setInt( 6, 0 );
					stmt2.executeUpdate();
				} finally {
					stmt2.close();
				}
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	return img;
}
/**
 * Insert the method's description here.
 * Creation date: (6/20/2001 17:44:08 PM)
 * @return int[]
 * [0] - today visits
 * [1] - today unique visits
 */
public int[] countTodayViews( String site ) throws SQLException {
	Date date = new Date();
	java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat();
	dateFormat.applyPattern( "yyyy-MM-dd" );
	String period = dateFormat.format( date );
	
	PreparedStatement stmt = conn.prepareStatement(
		"SELECT sum(v), count(v)"+
		" FROM (SELECT sum(\"Views\") AS v"+
		" FROM \"UtSiteStat\""+
		" WHERE \"Site\"=?"+
		"   AND \"Period\" LIKE ?"+
		"  GROUP BY \"Address\") AS s"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, period+'%' );
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				int res[] = new int[2];
				res[0] = rset.getInt(1);
				res[1] = rset.getInt(2);

				return res;
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}
	
	return new int[2];
}
/**
 * Insert the method's description here.
 * Creation date: (6/20/2001 17:44:08 PM)
 * @return int
 */
public int countTotalViews( String site ) throws SQLException {
	PreparedStatement stmt = conn.prepareStatement(
		"SELECT sum(\"Views\") FROM (SELECT \"Views\" FROM \"UtSiteStat\" WHERE \"Site\"=? UNION ALL"+
 		" SELECT \"Views\" FROM \"UtSiteStat0\" WHERE \"Site\"=? AND \"Period\" LIKE 'M%') AS s"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, site );
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				return rset.getInt(1);
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	return 0;
}
/**
 * Return Banner and decrement BannerInRt' counter
 * Creation date: (6/18/2001 14:24:14 PM)
 * @param name java.lang.String
 */
public Banner getBannerFromRotation( String rotationName ) throws java.sql.SQLException {
	Date date = new Date();
	java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat();
	dateFormat.applyPattern( "yyyy-MM-dd" );
	String period = dateFormat.format( date );


	int sum = 0;
	LinkedList list = new LinkedList();
	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Site\", \"Banner\", "+
		"\"Counter\", \"Frequency\"" +
		" FROM \"UtBannerInRt\" WHERE \"Rotation\"=?"+
		" AND ? BETWEEN \"SDate\" AND \"EDate\""
	); try {
		stmt.setString( 1, rotationName );
		stmt.setString( 2, period );
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				BannerInRt b = new BannerInRt();
				b.site = rset.getString(1);
				b.banner = rset.getString(2);
				b.counter = rset.getInt(3);
				b.frequency = rset.getInt(4);

				if ( b.counter > 0 ) {
					list.add( b );
					sum += b.frequency;
				}
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	Random rnd = new Random();
	int r = rnd.nextInt( sum );
	sum = 0;
	Iterator iter = list.iterator();
	while ( iter.hasNext() ) {
		BannerInRt brt = (BannerInRt)iter.next();
		sum += brt.frequency;
		if ( sum >= r ) {
			stmt = conn.prepareStatement(
				"UPDATE \"UtBannerInRt\" SET \"Counter\"=?"+
				" WHERE \"Site\"=? AND \"Banner\"=? AND \"Rotation\"=?"
			); try {
				stmt.setInt( 1, brt.counter-1 );
				stmt.setString( 2, brt.site );
				stmt.setString( 3, brt.banner );
				stmt.setString( 4, rotationName );
				stmt.executeUpdate();
			} finally {
				stmt.close();
			}
				
			stmt = conn.prepareStatement(
				"SELECT \"Site\", \"Name\", "+
				"\"Img\", \"URL\", \"Alt\", \"Info\"" +
				" FROM \"UtBanner\" WHERE \"Site\"=?"+
				" AND \"Name\"=?"
			); try {
				stmt.setString( 1, brt.site );
				stmt.setString( 2, brt.banner );
				ResultSet rset = stmt.executeQuery();
				try {
					if ( rset.next() ) {
						Banner b = new Banner();
						b.site = rset.getString(1);
						b.name = rset.getString(2);
						b.img = rset.getString(3);
						b.url = rset.getString(4);
						b.alt = rset.getString(5);
						b.info = rset.getString(6);
						
						return b;
					}
				} finally {
					rset.close();
				}
			} finally {
				stmt.close();
			}
		}
	}
	
	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public java.util.List getBannerInRtNames( String rotation, String site ) throws SQLException {
	List names = new LinkedList();

	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Banner\"" +
		" FROM \"UtBannerInRt\" WHERE \"Rotation\"=? AND \"Site\"=?"
	); try {
		stmt.setString( 1, rotation );
		stmt.setString( 2, site );
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				names.add( rset.getString(1) );
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	return names;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public java.util.List getBannerNames( String site ) throws SQLException {
	List names = new LinkedList();

	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Name\"" +
		" FROM \"UtBanner\" WHERE \"Site\"=?"
	); try {
		stmt.setString( 1, site );
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				names.add( rset.getString(1) );
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	return names;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public java.util.List getAllBanners( String site ) throws SQLException {
	List list = new LinkedList();


	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Name\",\"Img\",\"URL\",\"Alt\",\"Info\"" +
		" FROM \"UtBanner\" WHERE \"Site\"=?"
	); try {
		stmt.setString( 1, site );
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				Banner bann = new Banner();
				bann.name = rset.getString(1) ;
				bann.img = rset.getString(2) ;
				bann.url = rset.getString(3) ;
				bann.alt = rset.getString(4) ;
				bann.info = rset.getString(5) ;
				if ( ( bann.info == null ) || ( bann.info.length()==0)){
					bann.info = "not avaible" ; 
					}
				list.add( bann );
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}


	return list;
}

/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public java.util.List getBannerRtNames() throws SQLException {
	List names = new LinkedList();

	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Name\"" +
		" FROM \"UtBannerRt\""
	); try {
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				names.add( rset.getString(1) );
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	return names;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return ee.widespace.stat.Site
 */
public Site getSite(String name) throws SQLException {
	Site site = new Site();

	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Name\", \"URL\", \"EMail\", \"Password\", \"Info\"" +
		" FROM \"UtSite\" WHERE \"Name\"=?"
	); try {
		stmt.setString(1, name);
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				site.name = rset.getString(1);
				site.url = rset.getString(2);
				site.email = rset.getString(3);
				site.password = rset.getString(4);
				site.info = rset.getString(5);
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	return site;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return ee.widespace.stat.Site
 */
public Banner getBanner(String siteName,String bannerName) throws SQLException {
	Banner banner = new Banner();


	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Site\", \"Name\", \"Img\", \"URL\", \"Alt\", \"Info\"" +
		" FROM \"UtBanner\" WHERE \"Site\"=? AND \"Name\"=?"
	); try {
		stmt.setString(1, siteName);
		stmt.setString(2, bannerName);
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				banner.site = rset.getString(1);
				banner.name = rset.getString(2);
				banner.img = rset.getString(3);
				banner.url = rset.getString(4);
				banner.alt = rset.getString(5);
				banner.info = rset.getString(6);
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}


	return banner;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public BannerInRt getBannerInRt(String rotation, String banner) throws SQLException {
		BannerInRt bannerInRt = new BannerInRt();


	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Site\", \"Banner\", \"SDate\" , \"EDate\", \"Counter\", \"Frequency\" " +
		" FROM \"UtBannerInRt\" WHERE \"Rotation\"=? AND \"Banner\"=?"
	); try {
		stmt.setString(1, rotation);
		stmt.setString(2, banner);
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				bannerInRt.site      = rset.getString(1) ;
				bannerInRt.banner    = rset.getString(2) ;
				bannerInRt.sdate     = rset.getString(3) ;
				bannerInRt.edate     = rset.getString(4) ;
				bannerInRt.counter   = rset.getInt(5) ;
				bannerInRt.frequency = rset.getInt(6) ;
				bannerInRt.rotation  = rotation ;
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}


	return bannerInRt;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public List getAllBannerInRt(String rotation) throws SQLException {
	List list = new LinkedList();
		

	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"B\".\"Site\", \"B\".\"Banner\", \"B\".\"SDate\" , \"B\".\"EDate\", \"B\".\"Counter\", \"B\".\"Frequency\", \"S\".\"Info\" " +
		" FROM \"UtBannerInRt\" AS \"B\", \"UtSite\" AS \"S\" WHERE \"B\".\"Rotation\"=? AND \"B\".\"Site\"=\"S\".\"Name\"" 
	); try {
		stmt.setString(1, rotation);
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				BannerInRtSi bann = new BannerInRtSi();
				bann.rotation	= rotation;
				bann.site       = rset.getString(1) ;
				bann.banner     = rset.getString(2) ;
				bann.sdate      = rset.getString(3) ;
				bann.edate      = rset.getString(4) ;
				bann.counter    = rset.getInt(5) ;
				bann.frequency  = rset.getInt(6) ;
				bann.siteInfo   = rset.getString(7);
				if ( ( bann.siteInfo == null ) || ( bann.siteInfo.length()==0 )){
					bann.siteInfo = "not avaible";
					}
			System.out.println("Data :" + bann.banner);
				list.add(bann) ;	
								
				}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}


	return list;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public List getAllRt() throws SQLException {
	List list = new LinkedList();


	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Name\", \"Info\"" +
		" FROM \"UtBannerRt\""
	); try {
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				BannerRt rotation = new BannerRt();
				rotation.name = rset.getString(1);
				rotation.info = rset.getString(2);
				if ( ( rotation.info == null ) || (rotation.info.length()==0)){
					rotation.info = "not avaible" ; 
					}
				list.add( rotation ) ;

			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}


	return list;
}/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public BannerRt getRt(String name) throws SQLException {
	BannerRt rotation = new BannerRt();

	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Name\", \"Info\"" +
		" FROM \"UtBannerRt\" WHERE \"Name\"=?"
	); try {
			stmt.setString(1, name);
			ResultSet rset = stmt.executeQuery();
		try {
				if (!rset.next()) return null;
				rotation.name = rset.getString(1);
				rotation.info = rset.getString(2);
				if ( ( rotation.info == null ) || (rotation.info.length()==0))
					rotation.info = "not avaible" ; 
			} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}

	return rotation;
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 15:10:31 PM)
 * @return java.util.List
 */
public List getAllSites() throws SQLException {
	List list = new LinkedList();

System.out.println("gerAllSites initiated");
	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Name\", \"Info\"" +
		" FROM \"UtSite\""
	); try {
		ResultSet rset = stmt.executeQuery();
		try {
			while ( rset.next() ) {
				Site site = new Site();
				site.name = rset.getString(1);
				site.info = rset.getString(2);
				if ( ( site.info == null ) || (site.info.length()==0)){
					site.info = "not avaible" ; 
					}
				list.add( site );

			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}


	return list;
}
/**
 * Increment veiw count in DB
 * Creation date: (6/13/2001 14:07:36 PM)
 * @param name java.lang.String
 * return array of int
 */
public void pageViewed(String site, String url, String addr) throws SQLException {
	Date date = new Date();
	java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat();
	dateFormat.applyPattern( "yyyy-MM-dd HH" );
	String period4site = dateFormat.format( date );
	dateFormat.applyPattern( "yyyy-MM-dd" );
	String period4page = dateFormat.format( date );

	PreparedStatement stmt = conn.prepareStatement(
		"SELECT \"Views\"" +
		" FROM \"UtPageStat\" WHERE \"Site\"=? AND \"Page\"=? AND \"Period\"=?"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, url );
		stmt.setString( 3, period4page );
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				PreparedStatement stmt2 = conn.prepareStatement(
					"UPDATE \"UtPageStat\" SET \"Views\"=?"+
					" WHERE \"Site\"=? AND \"Page\"=? AND \"Period\"=?"
				); try {
					stmt2.setInt( 1, rset.getInt(1)+1 );
					stmt2.setString( 2, site );
					stmt2.setString( 3, url );
					stmt2.setString( 4, period4page );
					stmt2.executeUpdate();
				} finally {
					stmt2.close();
				}
			} else {
				PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO \"UtPageStat\" "+
					"(\"Site\", \"Page\", \"Period\", \"Views\") "+
					"VALUES(?, ?, ?, ?)"
				); try {
					stmt2.setString( 1, site );
					stmt2.setString( 2, url );
					stmt2.setString( 3, period4page );
					stmt2.setInt( 4, 1 );
					stmt2.executeUpdate();
				} finally {
					stmt2.close();
				}
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}
	
	stmt = conn.prepareStatement(
		"SELECT \"Views\"" +
		" FROM \"UtSiteStat\" WHERE \"Site\"=? AND \"Address\"=? AND \"Period\"=?"
	); try {
		stmt.setString( 1, site );
		stmt.setString( 2, addr );
		stmt.setString( 3, period4site );
		ResultSet rset = stmt.executeQuery();
		try {
			if ( rset.next() ) {
				PreparedStatement stmt2 = conn.prepareStatement(
					"UPDATE \"UtSiteStat\" SET \"Views\"=?"+
					" WHERE \"Site\"=? AND \"Address\"=? AND \"Period\"=?"
				); try {
					stmt2.setInt( 1, rset.getInt(1)+1 );
					stmt2.setString( 2, site );
					stmt2.setString( 3, addr );
					stmt2.setString( 4, period4site );
					stmt2.executeUpdate();
				} finally {
					stmt2.close();
				}
			} else {
				PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO \"UtSiteStat\" "+
					"(\"Site\", \"Address\", \"Period\", \"Views\") "+
					"VALUES(?, ?, ?, ?)"
				); try {
					stmt2.setString( 1, site );
					stmt2.setString( 2, addr );
					stmt2.setString( 3, period4site );
					stmt2.setInt( 4, 1 );
					stmt2.executeUpdate();
				} finally {
					stmt2.close();
				}
			}
		} finally {
			rset.close();
		}
	} finally {
		stmt.close();
	}
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 18:12:24 PM)
 * @param name java.lang.String
 * SELECT \"Name\" FROM \"UtBanner\" WHERE \"Site\"=?"
 */
public void removeBanner(String site, String name) throws SQLException {
	boolean	operationExecuted = false; 
	PreparedStatement stmtQB =
		conn.prepareStatement(
			"SELECT \"Banner\" FROM \"UtStatC\",\"UtBanner\" WHERE \"Banner\"=?");
	try {
		stmtQB.setString(1, name);
		ResultSet rset = stmtQB.executeQuery();
		if (rset.next()) {
			throw new SQLException("Banner :" + name + " can't be removed :(");
		}
			PreparedStatement stmt =
				conn.prepareStatement(
					"DELETE FROM \"UtBanner\" WHERE \"Name\"=? AND \"Site\"=?");
				try {
					stmt.setString(1, name);
					stmt.setString(2, site);
					stmt.executeUpdate();
					System.out.println("stmt :" + stmt);
					operationExecuted=true;
					}finally {stmt.close();}
				} finally {	stmtQB.close();}

		}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 18:12:24 PM)
 * @param name java.lang.String
 */
public void removeBannerInRt(String rotation, String site, String banner) throws SQLException {
	PreparedStatement stmt = conn.prepareStatement(
		"DELETE FROM \"UtBannerInRt\" WHERE \"Rotation\"=? AND \"Site\"=? AND \"Banner\"=?"
	); try {
		stmt.setString( 1, rotation );
		stmt.setString( 2, site );
		stmt.setString( 3, banner );
		stmt.executeUpdate();
	} finally {
		stmt.close();
	}
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 18:12:24 PM)
 * @param name java.lang.String
 */
public void removeBannerRt(String name) throws SQLException {
	PreparedStatement stmtQBRt =conn.prepareStatement(
		"SELECT \"Name\" FROM \"UtBanner\" WHERE \"Rotation\"=?");
	try {
		stmtQBRt.setString(1, name);
		ResultSet rset = stmtQBRt.executeQuery();
		if (rset.next()) {
			throw new SQLException("BannerRt :"+ name +" can't be removed, this BannerRt stil have banners");
		} 
	PreparedStatement stmt = conn.prepareStatement(
		"DELETE FROM \"UtBannerRt\" WHERE \"Name\"=?"
	); try {
		stmt.setString( 1, name );
		stmt.executeUpdate();
	} finally {stmt.close();}
	}finally {stmtQBRt.close();}
}
/**
 * Insert the method's description here.
 * Creation date: (6/13/2001 18:12:24 PM)
 * @param name java.lang.String
 * method for removing site
 */

public void removeSite(String name) throws SQLException {
	PreparedStatement stmtBQ =conn.prepareStatement(
		"SELECT \"Name\" FROM \"UtBanner\" WHERE \"Site\"=?");
	try {
		stmtBQ.setString(1, name);
		ResultSet rset = stmtBQ.executeQuery();
		if (rset.next()) {
			throw new SQLException("Site :"+ name +" can't be removed, site stil having banner");
		} 
		PreparedStatement stmt =conn.prepareStatement(
			"DELETE FROM \"UtSite\" WHERE \"Name\"=?");
		try {
			stmt.setString(1, name);
			stmt.executeUpdate();
		} finally {
			stmt.close();
		}
	} finally {
		stmtBQ.close();
	}
	
}
/**
 * Insert the method's description here.
 * Creation date: (8/9/2001 18:41:38)
 */
public void updateSite(Site newSite, String oldSiteName ) throws SQLException{
	PreparedStatement stmt = conn.prepareStatement(
		"UPDATE \"UtSite\" SET \"Name\"=?,\"URL\"=?,\"EMail\"=?,\"Password\"=?,\"Info\"=?"+
		" WHERE \"Name\"=? "
	); try {
		stmt.setString( 1, newSite.name );
		stmt.setString( 2, newSite.url );
		stmt.setString( 3, newSite.email );
		stmt.setString( 4, newSite.password );
		stmt.setString( 5, newSite.info );
		stmt.setString( 6, oldSiteName );
		stmt.executeUpdate();
	} finally {stmt.close();}
	
	}
	/**
 * Insert the method's description here.
 * Creation date: (8/9/2001 18:41:38)
 */
public void updateBanner(Banner newBanner) throws SQLException{
	PreparedStatement stmt = conn.prepareStatement(
		"UPDATE \"UtBanner\" SET \"Site\"=?,\"Name\"=?,\"Img\"=?,\"URL\"=?,\"Alt\"=?,\"Info\"=?"+
		" WHERE \"Site\"=? AND \"Name\"=? "
	); try {
		stmt.setString( 1, newBanner.site );
		stmt.setString( 2, newBanner.name );
		stmt.setString( 3, newBanner.img );
		stmt.setString( 4, newBanner.url );
		stmt.setString( 5, newBanner.alt );
		stmt.setString( 6, newBanner.info );
		stmt.setString( 7, newBanner.site );
		stmt.setString( 8, newBanner.name );
		stmt.executeUpdate();
	} finally {stmt.close();}
	
	}
/**
 * Insert the method's description here.
 * Creation date: (8/9/2001 18:41:38)
*/
	public void updateRt(BannerRt rotation) throws SQLException{
	PreparedStatement stmt = conn.prepareStatement(
		"UPDATE \"UtBannerRt\" SET \"Info\"=?"+
		" WHERE \"Name\"=?"
	); try {
		stmt.setString( 1, rotation.info );
		stmt.setString( 2, rotation.name );

		stmt.executeUpdate();
	} finally {stmt.close();}
	}
	
/**
 * Insert the method's description here.
 * Creation date: (8/9/2001 18:41:38)
 */
public void updateBannerInRt(BannerInRt newBannerInRt) throws SQLException{
	PreparedStatement stmt = conn.prepareStatement(
		"UPDATE \"UtBannerInRt\" SET \"Rotation\"=?,\"Site\"=?,"+ 
		"\"Banner\"=?, \"SDate\"=?, \"EDate\"=?, \"Counter\"=?, "
		+"\"Frequency\"=?"+
		" WHERE \"Site\"=? AND \"Banner\"=? AND \"Rotation\"= ?"
	); try {
		stmt.setString( 1, newBannerInRt.rotation );
		stmt.setString( 2, newBannerInRt.site );
		stmt.setString( 3, newBannerInRt.banner );
		stmt.setString( 4, newBannerInRt.sdate );
		stmt.setString( 5, newBannerInRt.edate );

		stmt.setInt   ( 6, newBannerInRt.counter );
		stmt.setInt   ( 7, newBannerInRt.frequency );
		
		stmt.setString( 8, newBannerInRt.site );
		stmt.setString( 9, newBannerInRt.banner );
		stmt.setString(10, newBannerInRt.rotation );

		stmt.executeUpdate();
	} finally {stmt.close();}
	}
}


