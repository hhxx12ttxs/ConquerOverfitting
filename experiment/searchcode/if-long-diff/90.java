/* $Id: MonitorUserStatus.java 124 2010-09-23 16:25:36Z altdotua@gmail.com $ */

package djudge.acmcontester.structures;

import java.util.HashMap;

import djudge.utils.xmlrpc.HashMapSerializable;

public class MonitorUserStatus extends HashMapSerializable// implements Comparable<MonitorUserStatus>
{
	public String userID;
	
	public String username;
	
	public int totalSolved;
	
	public long totalTime;
	
	public int place;
	
	public int totalAttempts;
	
	public int totalScoredAttempts;
	
	public int totalScore;
	
	public UserProblemStatusACM[] acmData;
	
	public UserProblemStatusIOI[] ioiData;

	public MonitorUserStatus()
	{
		// TODO Auto-generated constructor stub
	}
	
	@SuppressWarnings("unchecked")
	public MonitorUserStatus(HashMap hashMap)
	{
		fromHashMap(hashMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void fromHashMap(HashMap map)
	{
		userID = (String) map.get("user-id");
		username = (String) map.get("username");
		totalScoredAttempts = Integer.parseInt((String) map.get("total-scored-attempts"));
		totalAttempts = Integer.parseInt((String) map.get("total-attempts"));
		totalSolved = Integer.parseInt((String) map.get("total-solved"));
		totalScore = Integer.parseInt((String) map.get("total-score"));
		totalTime = Integer.parseInt((String) map.get("total-time"));
		place = Integer.parseInt((String) map.get("place"));
		Object[] data = (Object[]) map.get("data-acm");
		int n = data.length;
		acmData = new UserProblemStatusACM[n];
		for (int i = 0; i < n; i++)
		{
			acmData[i] = new UserProblemStatusACM((HashMap) data[i]);
		}
		data = (Object[]) map.get("data-ioi");
		n = data.length;
		ioiData = new UserProblemStatusIOI[n];
		for (int i = 0; i < n; i++)
		{
			ioiData[i] = new UserProblemStatusIOI((HashMap) data[i]);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashMap<String, Object> toHashMap()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("user-id", userID);
		map.put("username", username);
		map.put("total-attempts", "" + totalAttempts);
		map.put("total-solved", "" + totalSolved);
		map.put("total-score", "" + totalScore);
		map.put("total-time", "" + totalTime);
		map.put("place", "" + place);
		map.put("total-scored-attempts", "" + totalScoredAttempts);
		HashMap[] hm = new HashMap[acmData.length];
		for (int i = 0; i < acmData.length; i++)
		{
			hm[i] = acmData[i].toHashMap();
		}
		map.put("data-acm", hm);
		hm = new HashMap[ioiData.length];
		for (int i = 0; i < ioiData.length; i++)
		{
			hm[i] = ioiData[i].toHashMap();
		}
		map.put("data-ioi", hm);
		return map;
	}
	
/*	@Override
	public int compareTo(MonitorUserStatus t)
	{
		if (t.totalSolved != totalSolved)
			return t.totalSolved - totalSolved;
		if (totalTime != t.totalTime)
		{
			long diff = totalTime - t.totalTime;
			return diff > 0 ? 1 : diff < 0 ? -1 : 0; 
		}
		return 0;
	}*/
}

