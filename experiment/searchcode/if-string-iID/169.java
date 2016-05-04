//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.server.common;

import java.util.Date;

import org.apache.log4j.Logger;
import org.nuclos.common.E;
import org.nuclos.common.SF;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbIncrement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbMap;
import org.nuclos.server.dblayer.statements.DbTableStatement;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.report.valueobject.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:stefan.geiling@novabit.de">Stefan Geiling</a>
 *
 */
@Component
public class LocalCachesUtil {

	private static final Logger LOG = Logger.getLogger(LocalCachesUtil.class);

	private static LocalCachesUtil INSTANCE;

	private SpringDataBaseHelper dataBaseHelper;

	LocalCachesUtil() {
		INSTANCE = this;
	}

	public static LocalCachesUtil getInstance() {
		return INSTANCE;
	}
	
	@Autowired
	void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	public Long queryLocalCache(String topic) {
		try {
			String sql = "SELECT INTID FROM " + dataBaseHelper.getDbAccess().getSchemaName() + ".T_AD_LOCALCACHES WHERE STRFIELD_CACHINGTOPIC='" + topic + "'";
			ResultVO queryResult = dataBaseHelper.getDbAccess().executePlainQueryAsResultVO(sql, 1);
			if (queryResult.getRowCount() == 0)
				return null;
			
			for (int col = 0 ; col < queryResult.getColumns().size(); col++) {
				if (queryResult.getColumns().get(col).getColumnLabel().equalsIgnoreCase("INTID")) {
					return ((Number)queryResult.getRows().get(0)[col]).longValue();
				}
			}
		} catch (Exception e) {
			LOG.warn("can not query local cache " + topic + ": " + e.getMessage());
		}
		return null;
	}

	public Date queryLocalCacheRevalidation(String topic) {
		try {
			String sql = "SELECT STRFIELD_REVALIDATION FROM " + dataBaseHelper.getDbAccess().getSchemaName() + ".T_AD_LOCALCACHES WHERE STRFIELD_CACHINGTOPIC='" + topic + "'";
			ResultVO queryResult = dataBaseHelper.getDbAccess().executePlainQueryAsResultVO(sql, 1);
			if (queryResult.getRowCount() == 0)
				return null;
			
			for (int col = 0 ; col < queryResult.getColumns().size(); col++) {
				if (queryResult.getColumns().get(col).getColumnLabel().equalsIgnoreCase("STRFIELD_REVALIDATION")) {
					return ((Date)queryResult.getRows().get(0)[col]);
				}
			}
		} catch (Exception e) {
			LOG.warn("can not query local cache " + topic + ": " + e.getMessage());
		}
		return null;
	}

	public void updateLocalCacheRevalidation(String topic) {
		boolean isUpdate = true;
		Long iId = queryLocalCache(topic);
		if (iId == null) {
			isUpdate = false;
			iId = dataBaseHelper.getNextIdAsLong(SpringDataBaseHelper.DEFAULT_SEQUENCE);
		}
		
		DbTableStatement<Long> stmt = null;
		DbMap values = new DbMap();
		if (!isUpdate) {
			values.put(E.LOCALECACHES.getPk(), iId);
			values.put(E.LOCALECACHES.cachingTopic, topic);
			values.put(E.LOCALECACHES.revalidation, DbCurrentDateTime.CURRENT_DATETIME);
			values.put(SF.CREATEDAT, DbCurrentDateTime.CURRENT_DATETIME);
			values.put(SF.CREATEDBY, getCurrentUserName());
			values.put(SF.CHANGEDAT, DbCurrentDateTime.CURRENT_DATETIME);
			values.put(SF.CHANGEDBY, getCurrentUserName());
			values.put(SF.VERSION, 1);
			stmt = new DbInsertStatement(E.LOCALECACHES, values);
        } else {
        	DbMap conditions = new DbMap(1);
			conditions.put(E.LOCALECACHES.getPk(), iId);
			values.put(E.LOCALECACHES.revalidation, DbCurrentDateTime.CURRENT_DATETIME);
			values.put(SF.CHANGEDAT, DbCurrentDateTime.CURRENT_DATETIME);
    		values.put(SF.CHANGEDBY, getCurrentUserName());
            values.put(SF.VERSION, DbIncrement.INCREMENT);
            stmt = new DbUpdateStatement(E.LOCALECACHES, values, conditions);
        }
        if (stmt != null) {
           try {
              dataBaseHelper.getDbAccess().execute(stmt);
           } catch (DbException e) {
        	   LOG.warn("can not update local cache " + topic + ": " + e.getMessage());
           }
        }
	}
	
	/**
	 * @return the name of the current user. Shortcut for <code>this.getSessionContext().getCallerPrincipal().getName()</code>.
	 */
	public final String getCurrentUserName() {
		return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
	}

}

