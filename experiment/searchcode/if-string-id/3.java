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
// File: Id.java
//
// Created: May 21, 2003 7:16:53 PM
//
// $Id: Id.java 1258 2009-08-07 12:02:50Z vic $
// $Name:  $
//
package ru.adv.db.base;

import java.io.Serializable;

import org.apache.fop.datatypes.Numeric;

/**
 * @version $Revision: 1.9 $
 */
public class Id implements Comparable<Id>, Serializable {

	private static final long serialVersionUID = -3617986448449303522L;

	private Long longId;
	final private String stringId;
	final private int hashCode; // CPU optimization

	public Id(Long id) {
		this.longId = id;
		this.stringId = String.valueOf(this.longId);
		this.hashCode = this.stringId.hashCode(); 
	}

	public Id(String id) {
		this.stringId = id;
		this.hashCode = this.stringId.hashCode();
	}

	public Id(Id id) {
		this.longId = id.longId;
		this.stringId = id.stringId;
		this.hashCode = id.hashCode;
	}

	public Id(Object id) {
		if (id instanceof Numeric) {
			this.longId = ((Number)id).longValue();
			this.stringId = String.valueOf(this.longId);
			this.hashCode = this.stringId.hashCode(); 
		} else {
			this.stringId = id.toString();
			this.hashCode = this.stringId.hashCode(); 
		}
	}

	public Long getLongId() {
		if (longId==null) {
			return new Long(stringId);
		}
		return longId;
	}

	public String getStringId() {
		return stringId;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Id other = (Id) obj;
		if (this.longId!=null && other.longId!=null) {
			return this.longId.equals(other.longId);
		}
		return this.stringId.equals(other.stringId);
	}

	public String toString() {
		return this.stringId;
	}


	@Override
	public int compareTo(Id other) {
		if (other == null ) {
			return -1;
		}
		if (this.longId!=null && other.longId!=null) {
			return this.longId.compareTo(other.longId);
		}
		return this.stringId.compareTo(other.stringId);
	}

}

