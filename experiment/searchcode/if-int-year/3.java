package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import common.Constants;
import common.Scoring;
import common.UIHelper;

public class Year {

	private int year;
	
	// row is week (0 is total)
	// column is player
	// value is total score for that week
	private Table<Integer, Player, Integer> weeklyPoints;
	
	// row is week (0 is total)
	// column is player
	// value is total score for that week
	private Table<Integer, Player, Integer> totalPoints;

	

	private Week[] weeks;
	
	public Week[] getWeeks() {
		return weeks;
	}

	public void setWeeks(Week[] weeks) {
		this.weeks = weeks;
	}

	public Year()
	{		
		weeklyPoints = new Table<Integer, Player, Integer>();
		totalPoints =  new Table<Integer, Player, Integer>();
	}
	
	public int getWeeklyPoints(int week, Player player)
	{
		
		Integer points = weeklyPoints.get(week, player);
		
		return points == null ? 0 : points;
	}
	
	public int getTotalPoints(int week, Player player)
	{
		Integer points = totalPoints.get(week, player);
		
		return points == null ? 0 : points;
		
	}
	
	public void setWeeklyPoints(int week, Player player, int points)
	{
		weeklyPoints.put(week, player, points);
	}
	
	public void setTotalPoints(int week, Player player, int points)
	{
		totalPoints.put(week, player, points);
	}
	
	
	
	public void recalculateAllPoints()
	{
		scorePicks();
		recalculateWeeklyTotals();
		recalculatePlayerTotals();
	}
	
	public void scorePicks()
	{
		
		
		
		for (Week week : weeks)
		{
			
			if (!week.canScore()) continue;
			
			
			Map<Player,Integer> scores = new HashMap<Player,Integer>();
			
			for (Game game : week.getGames())
			{
			
				Map<Player,Pick> picksByGame = week.getAllPicksByGame(game);
				
				Map<Player,Integer> gameScores = Scoring.scorePicks(game, picksByGame);
			
				for (Player p : gameScores.keySet())
				{
					Integer currScore = scores.get(p);
					
					if(currScore == null) currScore = 0;
					
					currScore += gameScores.get(p);
					
					
					scores.put(p, currScore);
					
				}
				
			
			}
			for (Player p : scores.keySet())
			{
				this.setWeeklyPoints(week.getWeekNumber(), p, scores.get(p));
			}
		}
	}
	
	public void recalculateWeeklyTotals()
	{
		//do this really simply for now, no tiebreakers
		
		for (Week week : weeks)
		{
			
//			if (!week.canScore()) continue;
//		
			Map<Player,Integer> weekPointsMap = weeklyPoints.row(week.getWeekNumber());
			
			
			Map<Player,Integer> scores = Scoring.getTotals(weekPointsMap);
			
			for (Player p : scores.keySet())
			{
				int score = scores.get(p);
				// TODO dont do this here
				score *= UIHelper.getScoreMultiplierByWeek(week.getWeekNumber());
				
				
				this.setTotalPoints(week.getWeekNumber(), p, score);
			}
			
		}
		
	}
	
	public void recalculatePlayerTotals()
	{
		Map<Player, Integer> sums = new HashMap<Player,Integer>();
		for (Player p : weeklyPoints.columnKeySet())
		{
			Map<Integer, Integer> playerMap = weeklyPoints.column(p);
			
			int sum = 0;
			
			for (Integer week : playerMap.keySet())
			{
				if (week == -Constants.WEEK_NUMBER_FOR_TOTAL) continue;
				
				sum += playerMap.get(week);
			}
			
			sums.put(p, sum);
			
			
		}
		
		// this has to be outside the loop to avoid concurrentmodificationexception
		for (Entry<Player,Integer> e : sums.entrySet())
		{
			weeklyPoints.put(Constants.WEEK_NUMBER_FOR_TOTAL, e.getKey(), e.getValue());
		}
		
		sums.clear();
		
		
		for (Player p : totalPoints.columnKeySet())
		{
			Map<Integer, Integer> playerMap = totalPoints.column(p);
			
			int sum = 0;
			
			for (Integer week : playerMap.keySet())
			{
				if (week == Constants.WEEK_NUMBER_FOR_TOTAL) continue;
				
				sum += playerMap.get(week);
			}
			
			sums.put(p, sum);
		}
		
		for (Entry<Player,Integer> e : sums.entrySet())
		{
			totalPoints.put(Constants.WEEK_NUMBER_FOR_TOTAL, e.getKey(), e.getValue());
		}
		
		sums.clear();
		
	}
	
	
	
	
	public Week getWeek(int week)
	{
		if (week > 0 && week <= 21)
		{
			return weeks[week - 1];
		} else
		{
			return null;
		}
	}


	public int getYear() {
		return year;
	}


	public void setYear(int year) {
		this.year = year;
	}
	
	
}

