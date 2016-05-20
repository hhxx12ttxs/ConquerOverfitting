package com.jieshuhuiyou.util;
import java.util.HashMap;
import java.util.Map;
/**
 * ??????????QQ????????
 * @author Administrator
 *
 */
public class LevelHelper {
     
	static public Map<String, Integer> getCreditLevelArray(int score){
		//??
		int increase = 20;
		//??????
		int nScore = 0;
		//??????
		int increaseI = 10;
		//???   ???1
		int starNum = 1;
		//???
		int moonNum = 0;
		//???
		int sunNum = 0;
		//??????4????1???
		int rate = 4;
		
		Map<String, Integer> levelMap = new HashMap<String, Integer>();
		
		while(true){
			nScore += increase;
			if(nScore <= score){
				starNum ++;
				increase += increaseI;
				
				moonNum = moonNum + (int)(starNum / rate);
				starNum = starNum % rate;
				sunNum = sunNum + (int)(moonNum / rate);
				moonNum = moonNum % rate;
				
			}else{
				break;
			}
		}
		
		levelMap.put("star", starNum);
		levelMap.put("moon", moonNum);
		levelMap.put("sun", sunNum);
		levelMap.put("score",score);
		
		return levelMap;
	}
}

