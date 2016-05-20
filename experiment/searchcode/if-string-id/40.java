/* $Id: LanguageData.java 124 2010-09-23 16:25:36Z altdotua@gmail.com $ */

package djudge.acmcontester.structures;

import java.util.HashMap;
import org.apache.log4j.Logger;

import djudge.acmcontester.AuthentificationData;
import djudge.utils.xmlrpc.AbstractRemoteTable;
import djudge.utils.xmlrpc.HashMapSerializable;

public class LanguageData extends HashMapSerializable
{
	protected static final Logger log = Logger.getLogger(LanguageData.class);
	
	public String id = "-1";
	public String sid = "";
	public String shortName = "";
	public String fullName = "";
	public String compilationCommand = "";
	public String djudgeID = "";
	
	public LanguageData()
	{
		// TODO Auto-generated constructor stub
	}

	public LanguageData(HashMap<String, String> map)
	{
		fromHashMap(map);
	}
		
	public LanguageData(String sid, String shortName, String fullName, String compilationCommand, String djudgeID)
	{
		this.shortName = shortName;
		this.sid = sid;
		this.compilationCommand = compilationCommand;
		this.djudgeID = djudgeID;
		this.fullName = fullName;
	}

	public LanguageData(String id, String sid, String shortName, String fullName, String compilationCommand, String djudgeID)
	{
		this.id = id;
		this.shortName = shortName;
		this.sid = sid;
		this.compilationCommand = compilationCommand;
		this.djudgeID = djudgeID;
		this.fullName = fullName;
	}

	public void truncateInternalData()
	{
		compilationCommand = djudgeID = "";
	}

	@Override
	public HashMap<String, String> toHashMap()
	{
		HashMap<String, String> res = new HashMap<String, String>();
		res.put("id", id);
		res.put("sid", sid);
		res.put("short-name", shortName);
		res.put("full-name", fullName);
		res.put("compilation-command", compilationCommand);
		res.put("djudge-id", djudgeID);
		return res;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public void fromHashMap(HashMap map)
	{
		id = (String) map.get("id");
		sid = (String) map.get("sid");
		fullName = (String) map.get("full-name");
		shortName = (String) map.get("short-name");
		compilationCommand = (String) map.get("compilation-command");
		djudgeID = (String) map.get("djudge-id");
	}
	
	@Override
	public String toString()
	{
		return sid + " (" + fullName +  ")";
	}
	
	@Override
	protected int getColumnCount()
	{
		return 4;
	}

	@Override
	protected Class<? extends AbstractRemoteTable> getTableClass()
	{
		return RemoteTableLanguages.class;
	}

	@Override
	protected Object getValueAt(int column)
	{
		switch (column)
		{
		case 0: return id;
		case 1: return sid;
		//case 2: return shortName;
		case 2: return fullName;
		//case 4: return compilationCommand;
		case 3: return djudgeID;

		default:
			return id;
		}
	}
	
	@Override
	protected void setValueAt(int column, String value)
	{
		switch (column)
		{
		case 1: sid = value; break;
		//case 2: shortName = value; break;
		case 2: fullName = value; break;
		//case 4: compilationCommand = value; break;
		case 3: djudgeID = value; break;
		}
	}
	
	@Override
	protected boolean save()
	{
		if (!fDataChanged)
			return true;
		AuthentificationData ad = table.getAuthentificationData();
		boolean res = table.getConnector().editLanguage(
				ad.getUsername(),
				ad.getPassword(), id, sid,
				shortName, fullName, compilationCommand, djudgeID);
		fDataChanged = false;
		return res;
	}
	
	@Override
	protected boolean create()
	{
		AuthentificationData ad = table.getAuthentificationData();
		return table.getConnector().addLanguage(
				ad.getUsername(),
				ad.getPassword(), sid,
				shortName, fullName, compilationCommand, djudgeID);
	}
	
	@Override
	protected boolean delete()
	{
		AuthentificationData ad = table.getAuthentificationData();
		return table.getConnector().deleteLanguage(
				ad.getUsername(),
				ad.getPassword(), id);
	}
}

