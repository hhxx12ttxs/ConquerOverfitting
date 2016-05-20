/* $Id: ProblemData.java 124 2010-09-23 16:25:36Z altdotua@gmail.com $ */

package djudge.acmcontester.structures;

import java.util.HashMap;

import djudge.acmcontester.AuthentificationData;
import djudge.utils.xmlrpc.AbstractRemoteTable;
import djudge.utils.xmlrpc.HashMapSerializable;

public class ProblemData extends HashMapSerializable
{
	public String id = "-1";
	public String sid = "";
	public String name = "";
	public String djudgeProblem = "";
	public String djudgeContest = "";
	
	public ProblemData()
	{
		// TODO Auto-generated constructor stub
	}
	
	public ProblemData(HashMap<String, String> map)
	{
		fromHashMap(map);
	}

	public ProblemData(String sid, String name, String djudgeProblem, String djudgeContest)
	{
		this.sid = sid;
		this.name = name;
		this.djudgeProblem = djudgeProblem;
		this.djudgeContest = djudgeContest;
	}

	public ProblemData(String id, String sid, String name, String djudgeProblem, String djudgeContest)
	{
		this.id = id;
		this.sid = sid;
		this.name = name;
		this.djudgeProblem = djudgeProblem;
		this.djudgeContest = djudgeContest;
	}
	
	@Override
	public void truncateInternalData()
	{
		djudgeContest = djudgeProblem = "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fromHashMap(HashMap map)
	{
		id = (String) map.get("id");
		sid = (String) map.get("sid");
		name = (String) map.get("name");
		djudgeProblem = (String) map.get("djudge-problem");
		djudgeContest = (String) map.get("djudge-contest");
	}

	@Override
	public HashMap<String, String> toHashMap()
	{
		HashMap<String, String> res = new HashMap<String, String>();
		res.put("id", id);
		res.put("sid", sid);
		res.put("name", name);
		res.put("djudge-problem", djudgeProblem);
		res.put("djudge-contest", djudgeContest);
		return res;
	}
	
	@Override
	public String toString()
	{
		return sid + ": " + name;
	}
	
	@Override
	protected int getColumnCount()
	{
		return 5;
	}

	@Override
	protected Class<? extends AbstractRemoteTable> getTableClass()
	{
		return RemoteTableProblems.class;
	}

	@Override
	protected Object getValueAt(int column)
	{
		switch (column)
		{
		case 0: return id;
		case 1: return sid;
		case 2: return name;
		case 3: return djudgeProblem;
		case 4: return djudgeContest;

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
		case 2: name = value; break;
		case 3: djudgeProblem = value; break;
		case 4: djudgeContest = value; break;
		}
	}
	
	@Override
	protected boolean save()
	{
		if (!fDataChanged)
			return true;
		AuthentificationData ad = table.getAuthentificationData();
		boolean res = table.getConnector().editProblem(
				ad.getUsername(),
				ad.getPassword(),
				id, sid, name, djudgeProblem, djudgeContest);
		fDataChanged = false;
		return res;
	}
	
	@Override
	protected boolean create()
	{
		AuthentificationData ad = table.getAuthentificationData();
		return table.getConnector().addProblem(
				ad.getUsername(),
				ad.getPassword(), sid, name, djudgeProblem, djudgeContest);
	}
	
	@Override
	protected boolean delete()
	{
		AuthentificationData ad = table.getAuthentificationData();
		return table.getConnector().deleteProblem(
				ad.getUsername(),
				ad.getPassword(), id);
	}
}

