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
// -*- java -*-
// File: FormData.java
//
// Created: Wed Feb 13 18:19:34 2002
//
// $Id: FormData.java 1106 2009-06-03 07:32:17Z vic $
// $Name:  $
//

package ru.adv.xml.newt.form;

import ru.adv.http.*;
import java.util.*;
import java.io.Serializable;

/**
 * ?????? ????? ??????????? ? ??????
 * @version $Revision: 1.8 $
 */
public class FormData implements Serializable {

    private static final long serialVersionUID = 570971365393237L;

    private HashMap<String, Query> _map = new HashMap<String, Query>();
	private boolean _successed;
	private String _encoding;
	private String _sessionId;

	public FormData(String sessionId, String encoding) {
		_encoding = encoding;
		_sessionId = sessionId;
	}

	/**
	 * ?????????? ????? ?????? (id ?????).
	 */
	public Collection<String> getKeys() {
		return _map.keySet();
	}

	public String getSessionId() {
		return _sessionId;
	}

	/**
	 * ?????????? ????????? query
	 */
	public Query getSumQuery() {
		Query sum = new Query(_encoding);
        for (Query query : _map.values()) {
            sum.put(query);
        }
        return sum;
	}

	/**
	 * ?????????? query ????????? ???? ??? null ???? ??? ????? ????
	 * ??? query.
	 */
	public Query getQuery(String id) {
		// return null if id not exists
		return _map.get(id);
	}



	/**
	 * ????????????? query ??? ????????? ????.
	 */
	public void setQuery(String id, Query query) {
		_map.put(id, query);
	}

	/**
	 * ??????? query  ??? ????????? ????.
	 */
	public void remove(String id) {
		_map.remove(id);
	}

	/**
	 * ?????????? ???? ????????????? ??????.
	 */
	public boolean isSuccessed() {
		return _successed;
	}

	/**
	 * ????????????? ???? ????????????? ??????.
	 */
	public void setSuccessed(boolean v) {
		_successed = v;
	}

	protected void finalize() throws Throwable {
		_map.clear();
		_map = null;
		super.finalize();
	}
}

