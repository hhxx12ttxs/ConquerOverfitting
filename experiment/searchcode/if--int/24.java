/*  
 * Copyright (c) 2009  Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package cc.creativecomputing.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.creativecomputing.io.CCIOUtil;

/**
 * @author christianriekoff
 * 
 */
public class Lorem {
	public static void main(String[] args) {
		String[] myLines = CCIOUtil.loadStrings("demo/ui/lorem.txt");
		
		Map<String, Integer> _myStartWordMap = new HashMap<String, Integer>();
		Map<String, Integer> _myWordMap = new HashMap<String, Integer>();
		Map<Integer, Integer> _myPhraseLengths = new HashMap<Integer, Integer>();
		Map<Integer, Integer> _mySentenceLengths = new HashMap<Integer, Integer>();
		Map<Integer, Integer> _myParagraphLengths = new HashMap<Integer, Integer>();
		
		for (String myLine : myLines) {
			if (myLine.length() == 0)
				continue;
			
			String[] mySentences = myLine.split("\\.");
//			for(String mySentence:mySentences) {
//				String[] myPhrases = mySentence.split(", ");
//				if(!_mySentenceLengths.containsKey(myPhrases.length)) {
//					_mySentenceLengths.put(myPhrases.length, 0);
//				}
//				_mySentenceLengths.put(myPhrases.length, _mySentenceLengths.get(myPhrases.length) + 1);
//				for(String myPhrase:myPhrases) {
//					System.out.println(myPhrase.trim());
//					
//					String[] myWords = myPhrase.split(" ");
//					if(!_myPhraseLengths.containsKey(myWords.length)) {
//						_myPhraseLengths.put(myWords.length, 0);
//					}
//					_myPhraseLengths.put(myWords.length, _myPhraseLengths.get(myWords.length) + 1);
//					for(String myWord:myWords) {
//						if(myWord.length() == 0)continue;
//						
//						if((int)myWord.charAt(0) >= (int)'A' && (int)myWord.charAt(0) <= (int)'Z') {
//							if(!_myStartWordMap.containsKey(myWord)) {
//								_myStartWordMap.put(myWord, 0);
//							}
//							_myStartWordMap.put(myWord, _myStartWordMap.get(myWord) + 1);
//						}else {
//							if(!_myWordMap.containsKey(myWord)) {
//								_myWordMap.put(myWord, 0);
//							}
//							_myWordMap.put(myWord, _myWordMap.get(myWord) + 1);
//						}
//					}
//				}
//			}
			
			if(!_myParagraphLengths.containsKey(mySentences.length)) {
				_myParagraphLengths.put(mySentences.length, 0);
			}
			_myParagraphLengths.put(mySentences.length, _myParagraphLengths.get(mySentences.length) + 1);
		}
		

		
		float myNumberOfPhrases = 0;
		for(int myLength:_myParagraphLengths.keySet()) {
			myNumberOfPhrases += _myParagraphLengths.get(myLength);
		}
		
		for(int myLength:_myParagraphLengths.keySet()) {
			System.out.println("new CCLoremLength("+myLength + ", " + (_myParagraphLengths.get(myLength)/myNumberOfPhrases)+"f),");
		}
		
//		float myNumberOfPhrases = 0;
//		for(int myLength:_mySentenceLengths.keySet()) {
//			myNumberOfPhrases += _mySentenceLengths.get(myLength);
//		}
//		
//		for(int myLength:_mySentenceLengths.keySet()) {
//			System.out.println("new CCLoremLength("+myLength + ", " + (_mySentenceLengths.get(myLength)/myNumberOfPhrases)+"f),");
//		}
//		
//		float myNumberOfPhrases = 0;
//		for(int myLength:_myPhraseLengths.keySet()) {
//			myNumberOfPhrases += _myPhraseLengths.get(myLength);
//		}
//		
//		for(int myLength:_myPhraseLengths.keySet()) {
//			System.out.println("new CCLoremLength("+myLength + ", " + (_myPhraseLengths.get(myLength)/myNumberOfPhrases)+"f),");
//		}
//		
//		float myNumberOfWords = 0;
//		for(String myWord:_myWordMap.keySet()) {
//			myNumberOfWords += _myWordMap.get(myWord);
//		}
//		for(String myWord:_myWordMap.keySet()) {
//			System.out.println("new CCLoremWord(\""+myWord + "\", " + (_myWordMap.get(myWord)/myNumberOfWords)+"f),");
//		}
//		float myNumberOfStartWords = 0;
//		for(String myWord:_myStartWordMap.keySet()) {
//			myNumberOfStartWords += _myStartWordMap.get(myWord);
//		}
//		for(String myWord:_myStartWordMap.keySet()) {
//			System.out.println("new CCLoremWord(\""+myWord + "\", " + (_myStartWordMap.get(myWord)/myNumberOfStartWords)+"f),");
//		}
		System.out.println(_myWordMap.size());
		System.out.println(_myStartWordMap.size());
	}
}

