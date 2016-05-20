/* $Id: UserData.java 124 2010-09-23 16:25:36Z altdotua@gmail.com $ */

package djudge.acmcontester.structures;

import java.util.HashMap;

import djudge.acmcontester.AuthentificationData;
import djudge.utils.xmlrpc.AbstractRemoteTable;
import djudge.utils.xmlrpc.HashMapSerializable;

public class UserData extends HashMapSerializable
{
	public String id = "-1";
	public String username = "";
	public String password = "";
	public String name = "";
	public String role = "";
	
	public UserData()
	{
		// TODO Auto-generated constructor stub
	}
	
	public UserData(String username, String password, String name, String role)
	{
		this.name = name;
		this.password = password;
		this.role = role;
		this.username = username;
	}
	
	public UserData(String id, String username, String password, String name, String role)
	{
		this.id = id;
		this.name = name;
		this.password = password;
		this.role = role;
		this.username = username;
	}
	
	public UserData(HashMap<String, String> map)
	{
		fromHashMap(map);
	}
	
	@Override
	public HashMap<String, String> toHashMap()
	{
		HashMap<String, String> res = new HashMap<String, String>();
		res.put("id", id);
		res.put("username", username);
		res.put("password", password);
		res.put("name", name);
		res.put("role", role);
		return res;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void fromHashMap(HashMap map)
	{
		id = (String) map.get("id");
		username = (String) map.get("username");
		password = (String) map.get("password");
		name = (String) map.get("name");
		role = (String) map.get("role");
	}	

	@Override
	protected int getColumnCount()
	{
		return 5;
	}

	@Override
	protected Class<? extends AbstractRemoteTable> getTableClass()
	{
		return RemoteTableUsers.class;
	}

	@Override
	protected Object getValueAt(int column)
	{
		switch (column)
		{
		case 0: return id;
		case 1: return username;
		case 2: return password;
		case 3: return name;
		case 4: return role;

		default:
			return id;
		}
	}
	
	@Override
	protected void setValueAt(int column, String value)
	{
		switch (column)
		{
		case 1: username = value; break;
		case 2: password = value; break;
		case 3: name = value; break;
		case 4: role = value; break;
		}
	}
	
	@Override
	protected boolean save()
	{
		if (!fDataChanged)
			return true;
		AuthentificationData ad = table.getAuthentificationData();
		boolean res = table.getConnector().editUser(
				ad.getUsername(),
				ad.getPassword(), id, username, password, name, role);
		fDataChanged = false;
		return res;
	}
	
	@Override
	protected boolean create()
	{
		AuthentificationData ad = table.getAuthentificationData();
		return table.getConnector().addUser(
				ad.getUsername(),
				ad.getPassword(), username, password, name, role);
	}
	
	@Override
	protected boolean delete()
	{
		AuthentificationData ad = table.getAuthentificationData();
		return table.getConnector().deleteUser(
				ad.getUsername(),
				ad.getPassword(), id);
	}
	
	public String toString()
	{
		return id + " " + username + " " + password + " " + name + " " + role;
	}
}

