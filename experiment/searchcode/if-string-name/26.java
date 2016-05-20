/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
// $Id: Defaults.java 1315 2009-09-09 12:06:04Z vic $
// $Name:  $

package ru.adv.mozart;

import ru.adv.repository.Repository;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Arrays;

/**
 * ??????????? ?????????? ??????????? ??????? ??? ????? ??????.
 *
 * @author <a href="mailto:support@adv.ru">ADV</a>
 * @version $Revision: 1.28 $
 */

public interface Defaults {

	public static final String MOZART_PACKAGE_DIR_NAME = "ru/adv/";
	
	/** ???????? servlet */
	public static final String NAME = "Mozart";
	/** ?????? Mozart	*/
	public static final String VERSION = MozartVersion.VERSION;

	/** ???????? ? ??????? ??????? ??????, ??????? ???????? ??? ?????? ????? ????????-Mozart	*/
	public static final String CONFIG = "config";
	
	/** default JNDI name of {@link Repository} */
	public static final String REPOSITORY_JNDI_ID = "main";

	/** ??? property ??? ???????? root filepath	???????????? ????? */
//	public static final String HOSTROOT_PROP = "host.root";
	
	/** ??? servlet property ??? ???????? ?????????? ????????? NewtContext.isUseRemoved() */
	public static final String USE_REMOVED_PROP = "newt.use-removed";
	public static final String USE_REMOVED_DEFAULT = "false";

    /** ??? property ??? ???????? url prefix mozart application */
    public static final String APP_URL_PREFIX_PROP = "url";

    /** ??? property ??? ???????? root ??? mozart application */
    public static final String APPROOT_PROP = "root";

	/** root of filepath ??? ????????? ????? ????????????? (XSL)	*/
	public static final String TEMPLATES_ROOT = "templates";
	
	/** root of filepath ??? ????????? ????? ???????????? {@link ru.adv.cache.Cache}	*/
	public static final String CACHE_ROOT = MOZART_PACKAGE_DIR_NAME+"cache";
	
	/** root of filepath ??? ????????? ????????? ????? ?????? */
	public static final String SESSION_ROOT = MOZART_PACKAGE_DIR_NAME+"session";

    /** root of filepath ??? ????????? ????????? ????? ?????? */
    public static final String UPLOAD_ROOT = MOZART_PACKAGE_DIR_NAME+"upload";
    
    /** root of filepath ??? ????????? ????????? ????? {@link MailLogger} */
    public static final String MAIL_LOG_ROOT = MOZART_PACKAGE_DIR_NAME+"mail";

	public static final String CONFIG_DIR = "WEB-INF";

	/** ??? property, ?????????? ??? sitetree ????? */
	public static final String SITETREE_PROP = "sitetree.file";
	/** default file name of sitetree */
	public static final String SITETREE_DEFAULT = "tree/sitemap.xml";

	/** ??? property, ?????????? ??? ??????? ????? ??*/
	public static final String DBDESC_PROP = "db.file";
	/** default file name of db.xml */
	public static final String DBDESC_DEFAULT = CONFIG_DIR + "/db.xml";

	/** default file name of dbusers */
	public static final String DBUSERS_DEFAULT = "";

	/** ??? property, ?????????? ?????? ??????? */
	public static final String ALIASES_PROP = "aliases";

	/** property name for default xsl file name  */
	public static final String DEFAULT_XSL_PROP = "xsl.default";
	/** default xsl file name  */
	public static final String DEFAULT_XSL_DEFAULT = "default.xsl";

    /** property name for index files  */
    public static final String DEFAULT_INDEXES_PROP = "index.files";
    /** default index files  */
    public static final Set<String> DEFAULT_INDEXES = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(new String[]{"index.xml","index.xgi"}))
    );

	/** default database name  */
	public static final String DEFAULT_DB_NAME = "temp";

	/** property name for default cache java class  */
	public static final String CACHE_PROP = "cache";
	/** property name for default file cache root directory  */
	public static final String CACHEROOT_PROP = "cache.root";
    public static final String CACHESUFFIX_PROP = "cache.suffix";
	/** default cache java class  */
	public static final String CACHE_DEFAULT = "ru.adv.mozart.cache.Cache";

    /** default cache java class  */
    public static final String CACHE_DB_TYPE = "cache.db.type";

	public static final String SMTPSERVER_PROP = "smtp.server";
    /** default SMTP server*/
	public static final String SMTPSERVER_DEFAULT = "localhost";


	/** property name for default 404 error file  */
	public static final String ERRDOC404_PROP = "errdoc.404";
	/** default 404 error file  */
	public static final String ERRDOC404_DEFAULT = "/wp/errordocs/404.xml";

	public static final String TIMINGS_LOG_FILE_PROP = "timings.log.file";
	
	public static final String RELOAD_INTERVAL_PROP = "reload.interval";
	public static final int RELOAD_INTERVAL_DEFAULT = 10000;

	/** prefix word for define extended prefixes */
	public static final String PREFIX_PREFIX_PROP = "prefix";
	/** prefix name for ./htdocs */
	public static final String PREFIX_HTDOCS_NAME = "htdocs";
	/** prefix name for src, root directory with files for src attribute in the sitetree*/
	public static final String PREFIX_SRC_NAME = "src";
	/** prefix name for ./templates */
	public static final String PREFIX_TEMPLATES_NAME = "templates";
	/** prefix name for wp:// */
	public static final String PREFIX_WP_NAME = "wp://";
	/** default encoding */
	public static final String ENCODING = "UTF-8";

	/** default SMTP server*/
//	public static final String SMTP_SERVER = "localhost";

	/** ?????? ????? ??????? ?? ????????? */
	public static final int BLOCK_SIZE = 200;
	public static final String DEBUGGING_IPS_PROP = "debugging.ips";

	public static final String SESSION_IN_URL_PROP = "session.id.in.url";

    public static final String ERRORXSL_PROP = "error.xsl";
    
    public static final String LIMIT_CONNECTION_PROP = "servlet.connection.limit";
    public static final int LIMIT_CONNECTION_DEFAULT = 40;

    public static final String ERRORXSL = "resource:///ru/adv/mozart/error.xsl";
    
    public static final String DEFAULT_STATUS_URL = "/admin/mozart-status";
    
    /* query-stat */
    public static final String DEFAUL_STATISTIC_URL = "/admin/query-stat";
    public static final String LOG_QUERY_STAT = "log.stat";
    public static final String LOG_QUERY_STAT_DEFAULT = "/var/log/tomcat/mozartstat.log";

    public static final String VIEWER_USER = "viewer";
    public static final String VIEWER_ROLE = "viewer";
    public static final String INVALIDATE_SESSION_QUERY_KEYNAME = "j_logout";
    
    
    public static final String INLINE_PARAM_NAME = "_inline_editor";
    
    public static final String PROXY_PORT = "proxy.port";
    public static final int PROXY_PORT_DEFAULT = -1;

}

