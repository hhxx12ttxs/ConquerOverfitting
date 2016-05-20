/* $Id: SubmissionData.java 124 2010-09-23 16:25:36Z altdotua@gmail.com $ */

package djudge.acmcontester.structures;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import djudge.acmcontester.AuthentificationData;
import djudge.utils.xmlrpc.AbstractRemoteTable;
import djudge.utils.xmlrpc.HashMapSerializable;

public class SubmissionData extends HashMapSerializable
{
	public String id;
	public String userID;
	public String problemID;
	public String languageID;
	public int contestTime;
	public String realTime;
	public String judgement;
	public int maxTime;
	public int maxMemory;
	public int maxOutput;
	public int failedTest;
	public int score;
	public int judged;
	public int active;
	public String sourceCode;
	public int djudgeFlag;
	public String xml;
	public String username;
	public String problemSid;
	public String problemName;
	public String languageSid;
	public int fFirstTestOnly;
	
	public SubmissionData()
	{
		id = "-1";
		active = 1;
		contestTime = -1;
		failedTest = -1;
		judged = 0;
		judgement = "N/A";
		languageID = "-1";
		maxMemory = -1;
		maxTime = -1;
		maxOutput = -1;
		problemID = "-1";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String dateStr = dateFormat.format(date);		
		realTime = dateStr;
		score = -1;
		sourceCode = "";
		userID = "-1";
		djudgeFlag = 0;
		xml = "";
		username = "";
		problemSid = "";
		problemName = "";
		languageSid = "";
		fFirstTestOnly = 0;
	}
	
	public static final String[] fieldNames = {
		"ID",
		"Time",
		"Team",
		"Problem",
		"Language",
		"Result",
	};
	
	public Object getField(int fieldIndex)
	{
		switch (fieldIndex)
		{
		case 0: return id;
		case 1: return contestTime;
		case 2: return username;
		case 3: return problemSid;
		case 4: return languageSid;
		case 5: return judgement;
		}
		return "";
	}
	
	public SubmissionData(HashMap<String, String> map)
	{
		fromHashMap(map);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fromHashMap(HashMap map0)
	{
		HashMap<String, String> map = (HashMap<String, String>) map0;
		id = map.get("id");
		userID = map.get("user-id");
		problemID = map.get("problem-id");
		languageID = map.get("language-id");
		contestTime = Integer.parseInt(map.get("contest-time"));
		realTime = map.get("real-time");
		judgement = map.get("judgement");
		maxTime = Integer.parseInt(map.get("max-time"));
		maxMemory = Integer.parseInt(map.get("max-memory"));
		maxOutput = Integer.parseInt(map.get("max-output"));
		failedTest = Integer.parseInt(map.get("failed-test"));
		score = Integer.parseInt(map.get("score"));
		judged = Integer.parseInt(map.get("judged"));
		active = Integer.parseInt(map.get("active"));
		sourceCode = map.get("source-core");
		djudgeFlag = Integer.parseInt(map.get("djudge-flag"));
		xml = map.get("xml");
		username = map.get("username");
		problemSid = map.get("problem-sid");
		problemName = map.get("problem-name");
		languageSid = map.get("language-sid");
		fFirstTestOnly = Integer.parseInt(map.get("first-test-only"));
	}

	@Override
	public HashMap<String, String> toHashMap()
	{
		HashMap<String, String> res = new HashMap<String, String>();
		res.put("id", id);
		res.put("user-id", userID);
		res.put("problem-id", problemID);
		res.put("language-id", languageID);
		res.put("contest-time", "" + contestTime);
		res.put("real-time", realTime);
		res.put("judgement", judgement);
		res.put("max-time", "" + maxTime);
		res.put("max-memory", "" + maxMemory);
		res.put("max-output", "" + maxOutput);
		res.put("failed-test", "" + failedTest);
		res.put("score", "" + score);
		res.put("judged", "" + judged);
		res.put("active", "" + active);
		res.put("source-core", sourceCode);
		res.put("djudge-flag", "" + djudgeFlag);
		res.put("xml", xml);
		res.put("username", username);
		res.put("problem-sid", problemSid);
		res.put("problem-name", problemName);
		res.put("language-sid", languageSid);
		res.put("first-test-only", "" + fFirstTestOnly);
		return res;
	}
	
	@Override
	protected int getColumnCount()
	{
		return 12;
	}

	@Override
	protected Class<? extends AbstractRemoteTable> getTableClass()
	{
		return RemoteTableSubmissions.class;
	}

	public enum SubmissionDataColumnsEnum
	{
		ID,
		Username,
		ProblemSID,
		LanguageSID,
		ContestTime,
		RealTime,
		Judgement,
		MaxTime,
		MaxMemory,
		FailedTest,
		Score,
		DJudgeFlag,
		MaxOutput,
		Judged,
		Active,
		SourceCode,
		Xml,
		ProblemID,
		ProblemName,
		LanguageID,
		UserID,
	}
	
	@Override
	protected Object getValueAt(int column)
	{
		switch (column)
		{
		case 0: return id;
		case 1: return username;
		case 2: return problemSid;
		case 3: return languageSid;
		case 4: return contestTime;
		case 5: return realTime;
		case 6: return judgement;
		case 7: return maxTime;
		case 8: return maxMemory;
		case 9: return failedTest;
		case 10: return score;
		case 11: return djudgeFlag;
		case 12: return maxOutput;
		case 13: return judged;
		case 14: return active;
		case 15: return sourceCode;
		case 16: return xml;
		case 17: return username;
		case 18: return problemID;
		case 19: return problemName;
		case 20: return languageID;
		case 21: return userID;
		
		default:
			return id;
		}
	}

	protected void setValueAt(int column, String value)
	{
		log.fatal("Cannot directly change table Submissions");
	}

	
/*	@Override
	protected void setValueAt(int column, String value)
	{
		switch (column)
		{
		case 0: id = value; break;
		case 1: username = value; break;
		case 2: problemSid = value; break;
		case 3: languageSid = value; break;
		case 4: contestTime = Integer.parseInt(value); break;
		case 5: realTime = value; break;
		case 6: judgement = value; break;
		case 7: maxTime = Integer.parseInt(value);; break;
		case 8: maxMemory = Integer.parseInt(value); break;
		case 9: maxOutput = Integer.parseInt(value); break;
		case 10: failedTest = Integer.parseInt(value); break;
		case 11: score = Integer.parseInt(value); break;
		case 12: judged = Integer.parseInt(value); break;
		case 13: active = Integer.parseInt(value); break;
		case 14: sourceCode = value; break;
		case 15: djudgeFlag = Integer.parseInt(value); break;
		case 16: xml = value; break;
		case 17: username = value; break;
		case 18: problemSid = value; break;
		case 19: problemName = value; break;
		case 20: languageSid = value; break;
		}
	}*/
	
	@Override
	protected boolean save()
	{
		if (!fDataChanged)
			return true;
		AuthentificationData ad = table.getAuthentificationData();
		boolean res = table.getConnector().editSubmission(ad.getUsername(),
				ad.getPassword(), id, toHashMap());
		fDataChanged = false;
		return res;
	}
	
	@Override
	protected boolean create()
	{
		return false;
	}
	
	@Override
	protected boolean delete()
	{
		AuthentificationData ad = table.getAuthentificationData();
		return table.getConnector().deleteSubmission(
				ad.getUsername(),
				ad.getPassword(), id);
	}
}

