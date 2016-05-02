/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.dsf.jsdi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Value implements IValue, Serializable {
	
	private static final long serialVersionUID = 1L;

	private VariableType m_type;
	private String m_typeName;
	private Object m_value; //object id or real value for primitive types
	private Map<String, IVariable> m_members = null;
	
	public static Value forPrimitive(Object primitiveValue)
	{
		if (primitiveValue == null)
			return new Value(VariableType.NULL, "null", primitiveValue);
		else if (primitiveValue == Value.UNDEFINED)
			return new Value(VariableType.UNDEFINED, "undefined", primitiveValue);
		else if (primitiveValue instanceof Boolean)
			return new Value(VariableType.BOOLEAN, "Boolean", primitiveValue);
		else if (primitiveValue instanceof Double)
			return new Value(VariableType.NUMBER, "Number", primitiveValue);
		else if (primitiveValue instanceof String)
			return new Value(VariableType.STRING, "String", primitiveValue);
		return null;
	}
	
	public Value(VariableType type, String typeName, Object value) {
		m_type = type;
		m_typeName = typeName;
		m_value = value;
	}
	
	public long getId() {
		if (m_value instanceof Long) {
			return ((Long)m_value).longValue();
		}
		else {
			return Value.UNKNOWN_ID;
		}
	}

	public int getMemberCount(ISession session) {
		loadMembers(session);
		return (m_members == null) ? 0 : m_members.size();
	}

	public IVariable getMember(String name, ISession session) {
		loadMembers(session);
		if (m_members != null) {
			return m_members.get(name);
		}
		return null;
	}
	
	public IVariable getMember(int childIndex, ISession session) {
		loadMembers(session);
		int count = 0;
		Iterator<IVariable> itr = m_members.values().iterator();
		while(itr.hasNext()) {
			IVariable variable = itr.next();
			if (childIndex == count) {
				return variable;
			}
			count++;
		}
		return null;
	}

	public IVariable[] getMembers(ISession session) {
		int count = getMemberCount(session);
		IVariable[] memberArr = new IVariable[count];

		if (count > 0) {
			count = 0;
			Iterator<IVariable> itr = m_members.values().iterator();
			while(itr.hasNext()) {
				memberArr[count++] = itr.next();
			}
		}
		return memberArr;
	}

	public VariableType getType() {
		return m_type;
	}

	public String getTypeName() {
		return m_typeName;
	}

	public String getObjectValueAsString(ISession session) {
		if (m_type != VariableType.OBJECT) {
			return getValueAsString(m_value);
		}
		try {
			return session.getObjectValueAsString(getId());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getValueAsString(Object value)
	{
		if (value == null) {
			return "null";
		}

		if (value instanceof Double) {
			// Java often formats whole numbers in ugly ways.  For example,
			// the number 3 might be formatted as "3.0" and, even worse,
			// the number 12345678 might be formatted as "1.2345678E7" !
			// So, if the number has no fractional part, then we override
			// the default display behavior.
			double doubleValue = ((Double)value).doubleValue();
			long longValue = (long) doubleValue;
			if (doubleValue == longValue) {
				return Long.toString(longValue);
			}
		}

		return value.toString();
	}
	
	public void addMember(IVariable v) {
		if (m_members == null) {
			m_members = new LinkedHashMap<String, IVariable>();
		}
		m_members.put(v.getName(), v);
	}
	
	public boolean membersObtained() {
		return (getId() == UNKNOWN_ID || m_members != null);
	}
	
	public void setMembersObtained(boolean obtained) {
		if (obtained) {
			if (m_members == null) {
				m_members = Collections.emptyMap();
			}
		}
		else {
			m_members = null;
		}
	}
	
	private void loadMembers(ISession session) {
		if (m_members != null) {
			return;
		}
		long id = getId();
		if (id == Value.UNKNOWN_ID) {
			return;
		}
		try {
			IVariable[] members = session.loadMembers(id);
			for (IVariable member : members) {
				addMember(member);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setMembersObtained(true);
	}
}

