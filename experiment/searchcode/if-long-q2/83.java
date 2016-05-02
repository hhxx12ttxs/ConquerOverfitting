package net.sf.egonet.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.egonet.model.Alter;
import net.sf.egonet.model.Answer;
import net.sf.egonet.model.Interview;
import net.sf.egonet.model.Question;
import net.sf.egonet.model.QuestionOption;
import net.sf.egonet.model.Question.QuestionType;

import net.sf.egonet.persistence.ExpressionNode.MathOp;

/**
 * the survey question prompts can be altered at runtime using special tags,
 * <VAR ... />  <COUNT ... />   <CALC ... />  <CONTAINS ... /> and <IF .. />
 * This class contains static functions that will perform this 
 * 'insertion' of variable text
 * @author Kevin
 */
public class TextInsertionUtil {
	
	private static final String strMonths[] = 
	   {"January", "February", "March", "April", "May", "June", 
		"July", "August", "September", "October", "November", "December" };
	
	private static final int monthCaps[] = 
	{31,28,31,30,31,30,31,31,30,31,30,31};
	
	private enum DATE_FIELD { MONTH, DAY, YEAR, HOUR, MINUTE, NONE};
	
	/**
	 * answerToQuestion
	 * a convenience function for answerInsertion below.
	 * Given the string that is a question title, it returns
	 * its answer in string form.  The question is determined
	 * by the questions title and type ( strQuestionTitle, iType ).
	 * If questions are not found in the current section of the survey
	 * 'earlier' questions will be examined.
	 * If a question is not found in the ALTER or ALTER_PAIR section
	 *     the EGO section will be searched
	 * If a question is not found in the EGO section
	 *     the EGO_ID section will be searched
	 * @param strQuestionTitle - title of question we want the answer to 
	 * @param interviewId - needed to look up variable values ( answers to previous questions )
	 * @param iType - needed to look up variable values ( answers to previous questions )
	 * @param studyId - needed to look up variable values ( answers to previous questions )
	 * @param listOfAlters - needed to look up variable values ( answers to previous questions )
	 * @return the questions answer in string form
	 */
	
	public static String answerToQuestion ( String strQuestionTitle, 
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters ) {
		ArrayList<Alter> emptyAlters = new ArrayList<Alter>();
		boolean isOtherSpecifyQuestion;
		String otherSpecifyText;
		Interview currentInterview;
		StringTokenizer strok;
		List<QuestionOption> optionsList;
		Question question = null;
		Answer theAnswer = null;
		String strAnswer = strQuestionTitle;
		String strOption;
		Long   iOptionId;
		
		if ( interviewId==null )
			return (strQuestionTitle);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strQuestionTitle);
		
		// if listOfAlters is null
		// that indicates this is being used in a preface.
		// In the case of a preface look ONLY in EGO and EGO_ID 
		// for answers, regardless of where we come from
		if ( listOfAlters==null && ( iType==QuestionType.ALTER || iType==QuestionType.ALTER_PAIR)) {
			iType = QuestionType.EGO;
		}
		// if alter or alter_pair questions are used in a 'list of alters'
		// format the listOfAlters will be null, or empty.
		// in this case we can only deal with ego or ego_id questions.
		if (( iType==QuestionType.ALTER || iType==QuestionType.ALTER_PAIR) && listOfAlters.isEmpty()) {
			iType = QuestionType.EGO;
		}
		
		question = Questions.getQuestionUsingTitleAndTypeAndStudy (strQuestionTitle, iType, studyId);
		if ( question==null  && (iType==QuestionType.ALTER ||  iType==QuestionType.ALTER_PAIR)) {
			iType = QuestionType.EGO;
			question = Questions.getQuestionUsingTitleAndTypeAndStudy (strQuestionTitle, iType, studyId);
		}
		if ( question==null  &&  iType==QuestionType.EGO ) {
			iType = QuestionType.EGO_ID;
			question = Questions.getQuestionUsingTitleAndTypeAndStudy (strQuestionTitle, iType, studyId);
		}
		if ( question==null )
			return ("[ " +strQuestionTitle + " NOT FOUND]");

		if ( iType==QuestionType.ALTER || iType==QuestionType.ALTER_PAIR) {
			theAnswer = Answers.getAnswerForInterviewQuestionAlters( currentInterview, question, listOfAlters);
		} else {
			theAnswer = Answers.getAnswerForInterviewQuestionAlters( currentInterview, question, emptyAlters);		
		}
				
		if ( theAnswer==null )
			return("[ no answer to " + strQuestionTitle + " found]");
	
		isOtherSpecifyQuestion = question.getOtherSpecify();
		if ( theAnswer.getValue()==null ) {
			switch ( theAnswer.getSkipReason()) {
			case REFUSE: return ("(refuse)"); 
			case DONT_KNOW: return ("(don't know)");
			case NONE:
			default:	return("(unanswered)");
			}
		}
		
		switch ( theAnswer.getAnswerType()) {
			case SELECTION:
			case MULTIPLE_SELECTION:
				 strAnswer = "";
				 optionsList = Options.getOptionsForQuestion(question.getId());
				 strok = new StringTokenizer(theAnswer.getValue(), ",");
				 while ( strok.hasMoreElements()) {
					 strOption = strok.nextToken();
					 try {
						 iOptionId = Long.parseLong(strOption);
					 } catch ( NumberFormatException nfe ) {
						 iOptionId = -1L;
					 }
					 for ( QuestionOption qo : optionsList ) {
						 if ( qo.getId().equals(iOptionId)) {
							 if ( strAnswer.length()>1 )
								 strAnswer += ", ";
							 // special check for 'other/specify' questions.
							 otherSpecifyText = null;
							 if ( isOtherSpecifyQuestion &&
								  qo.getName().trim().startsWith("OTHER SPECIFY"))
								 	otherSpecifyText = theAnswer.getOtherSpecifyText();
							 if ( otherSpecifyText!=null && otherSpecifyText.length()>0 )
						         strAnswer += otherSpecifyText;
							 else
							     strAnswer += qo.getName();
						 }
					 }
				 }
				 break;
			case TEXTUAL:
			case NUMERICAL:
			case DATE:
			case TIME_SPAN:
				 strAnswer = theAnswer.getValue();
				 break;
		}
	return(strAnswer);
	}
	
	/**
	 * variableInsertion
	 * used with the tag <VAR ... />
	 * this will accept any arbitrary string and, if markers of the format <VAR ... />
	 * are found, will create a new string by substituting in answers.
	 * For example, if Question Q1 asked how many times last week a person smoked crack,
	 * a later question might be "Of the <VAR Q1 /> times you smoked crack last week, how many
	 * times did you also drink?"
	 * The pattern for embedded variables is <VAR ... />
	 * This is a static function in anticipation of cases where it has to be used on strings
	 * not immediately associated with this question.
	 * @param strInput - the original string to (possibly) alter
	 * @param interviewId - needed to look up variable values ( answers to previous questions )
	 * @param iType - needed to look up variable values ( answers to previous questions )
	 * @param studyId - needed to look up variable values ( answers to previous questions )
	 * @param listOfAlters - needed to look up variable values ( answers to previous questions )
	 * @return strInput but with any <VAR /> tags replaced with that variables value
	 */

	public static String variableInsertion (String strInput, 
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters ) {
		Interview currentInterview;
		String strResult = "";
		String pattern = "<VAR.*?/>";
		String strVariableName;
		String strVariableValue;
		String str;
		ArrayList<String> theList;
		int ix;
		
		// if no interviewId we are previewing the question
		// return original prompt unaltered
		if ( interviewId==null )
			return(strInput);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strInput);
		
		theList = parseExpressionList ( strInput, pattern);
		if (theList==null)
			return(strInput);

		// At this point we have an array list with literal strings
		// alternating with variable markers .
		// now construct the output string by replace the
		// question names between the <VAR /> markers with the answer from
		// that question
		for ( ix=0 ; ix<theList.size(); ++ix ) {
			str = theList.get(ix);
			strVariableName = trimPrefixAndSuffix ( str, "<VAR", "/>"); 
			if ( strVariableName!=null ) {
				strVariableValue = answerToQuestion(strVariableName, interviewId, iType, studyId, listOfAlters );
				if ( strVariableValue == null || strVariableValue.length()==0 )
					strVariableValue = "[no value for " + strVariableName + "]";
				strResult += " " + strVariableValue + " ";
			} else {
				strResult += str;
			}
		} 
		return(strResult);
	}	
	
	/**
	 * dateDataInsertion is very similar to variableInsertion but has more options and
	 * code specific to dates
	 * <DATE question /> will default to <VAR question /> behaviour
	 * <DATE question DATE_FIELD /> will print only the specific date field, such as the year
	 * <DATE question DATE_FIELD offset /> will print the specified date field offset from
	 * the one in the question by offset amount
	 * @param strInput - the original string to (possibly) alter
	 * @param interviewId - needed to look up variable values ( answers to previous questions )
	 * @param iType - needed to look up variable values ( answers to previous questions )
	 * @param studyId - needed to look up variable values ( answers to previous questions )
	 * @param listOfAlters - needed to look up variable values ( answers to previous questions )
	 * @return
	 */
	public static String dateDataInsertion (String strInput, 
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters ) {
		int Month = DATE_FIELD.MONTH.ordinal();
		int Day   = DATE_FIELD.DAY.ordinal();
		int Year  = DATE_FIELD.YEAR.ordinal();
		Interview currentInterview;		
		String strResult = "";
		String pattern = "<DATE.*?/>";
		String strParameters; // question title, optional field specifier, optional offset
		String[] paramInfo; // strParameters split into 3 parts
		String strDate; // date as retrieved from answer Data
		int[] dateInfo; // date split into year, month, day
		int offset = 0;
		String str;
		DATE_FIELD dateField = DATE_FIELD.NONE;
		ArrayList<String> theList;
		int ix;
		
		// if no interviewId we are previewing the question
		// return original prompt unaltered
		if ( interviewId==null )
			return(strInput);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strInput);
		
		theList = parseExpressionList ( strInput, pattern);
		if (theList==null)
			return(strInput);

		// At this point we have an array list with literal strings
		// alternating with variable markers .
		// now construct the output string by replacing the
		// parameters between the <DATE /> markers with formatted DATE data
		for ( ix=0 ; ix<theList.size(); ++ix ) {
			str = theList.get(ix);
			strParameters = trimPrefixAndSuffix ( str, "<DATE", "/>"); 
			if ( strParameters==null ) {
				strResult += str;
			} else {
			if ( strParameters.length()==0) {
				strResult += "[empty <DATE /> tag]";
			} else {
				paramInfo = strParameters.split(" ");
				if ( paramInfo.length>1 ) {
					try {  dateField = DATE_FIELD.valueOf(paramInfo[1]);
					} catch ( java.lang.RuntimeException rte ) {
						strResult += "[" + paramInfo[1] + " should be YEAR, MONTH, or DAY]";
					}
				}	
				if ( paramInfo.length>2 ) {
					if ( paramInfo[2].startsWith("+")  &&  paramInfo[2].length()>1 )
						paramInfo[2] = paramInfo[2].substring(1).trim();
					try { offset = Integer.parseInt(paramInfo[2]);
					} catch ( java.lang.RuntimeException rte ) {
						offset = 0;
					}
				}
				strDate = answerToQuestion(paramInfo[0], interviewId, iType, studyId, listOfAlters );
				if ( strDate==null  || strDate.length()==0 ) {
					strResult += "[date "+strDate+" not found]";
				} else {
				    dateInfo = strDateToNumeric(strDate);
					switch ( paramInfo.length ) {
					    case 0:
					    case 1:
					         strResult += " " + strDate + " ";
					         break;
					    case 2: {
					    	switch ( dateField ) {
					    	    case NONE: break;
					    	    case MONTH:
					    	    	 if ( dateInfo[Month]<0  || dateInfo[Month]>11)
						    	    	 strResult += " " + dateInfo[Month] + " ";
					    	    	 else
					    	    	     strResult += " " + strMonths[dateInfo[Month]] + " "; 
					    	    	 break;
					    	    case DAY: strResult += " " + dateInfo[Day] + " "; break;
					    	    case YEAR: strResult += " " + dateInfo[Year] + " "; break;
					    	    }
					    	 }
					         break;
					    case 3: {
					    	 int[] newDate;
					    	 
					    	 newDate = applyOffsetToDate ( dateInfo, dateField, offset );
					    	 switch ( dateField ) {
					    	     case NONE: break;
					    	     case MONTH: 
					    	    	  if ( dateInfo[Month]<0  || dateInfo[Month]>11)
						    	    	  strResult += " " + dateInfo[Month] + " ";
					    	    	  else
					    	    	      strResult += " " + strMonths[newDate[Month]] + " "; 
					    	    	  if ( newDate[Year] != dateInfo[Year] )
					    	    		  strResult += newDate[Year] + " ";
					    	    	  break;
					    	     case DAY:
					    	    	  if ( newDate[Month] < 0 || newDate[Month]>11 )
						    	    	  strResult += " " + newDate[Month] + " " + newDate[Day] + " ";
					    	    	  else
					    	    	      strResult += " " + strMonths[newDate[Month]] + " " + newDate[Day] + " ";
					    	    	  if ( newDate[Year] != dateInfo[Year] )
					    	    		  strResult += newDate[Year] + " ";					    	    	  
					    	    	  break;
					    	     case YEAR:
					    	    	  strResult += " " + newDate[Year] + " ";
					    	    	  break;					    	    	  
					    	 }
					    }   	
					}	
				}
			}
		} 
		}
		return(strResult);
	}	
	
	/**
	 * calculationInsertion
	 * used with the tag <CALC ... />
	 * this will accept any arbitrary string and, if markers of the format <CALC ... />
	 * are found, will create a new string by substituting in the results of simple
	 * calculations based on previous numeric answers.
	 * For example, if Question Q1 asked how many times a person had sex with a female
	 * and Question Q2 asked how many times a person had sex with a male
	 * a later question might be "Of the <CALC Q1+Q2 /> times you had sex last week, how many
	 * times did you also shoot up heroin?"
	 * The pattern for embedded calculations is <CALC ... />
	 * This is a static function in anticipation of cases where it has to be used on strings
	 * not immediately associated with this question.
	 * @param strInput - the original string to (possibly) alter
	 * @param interviewId - needed to look up variable values ( answers to previous questions )
	 * @param iType - needed to look up variable values ( answers to previous questions )
	 * @param studyId - needed to look up variable values ( answers to previous questions )
	 * @param listOfAlters - needed to look up variable values ( answers to previous questions )
	 * @return strInput, but with any <CALC /> tags replaced by the appropriate calculation
	 */

	public static String calculationInsertion (String strInput, 
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters ) {
		Interview currentInterview;
		String strResult = "";
		String pattern = "<CALC.*?/>";
		String str;
		String strExpression;
		String strExpressionValue;
		ArrayList<String> theList = null;
		int ix;

		// if no interviewId we are previewing the question
		// return original prompt unaltered
		if ( interviewId==null )
			return(strInput);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strInput);
		
		theList = parseExpressionList(strInput, pattern);
		if ( theList==null )
			return(strInput);
		
		// At this point we have an array list with literal strings
		// alternating with variable markers .
		// now construct the output string by replace the
		// question names between the <VAR /> markers with the answer from
		// that question
		for ( ix=0 ; ix<theList.size(); ++ix ) {
			str = theList.get(ix);
			strExpression = trimPrefixAndSuffix(str, "<CALC", "/>");
			if ( strExpression!=null) {
				strExpressionValue = calculateSimpleExpression(strExpression, interviewId, iType, studyId, listOfAlters );
				if ( strExpressionValue==null || strExpressionValue.length()==0 )
					strExpressionValue = " (CALC error) ";
				strResult += " " +strExpressionValue + " ";
			} else {
				strResult += str;
			}
		} 
		return(strResult);
	}		

	/**
	 * calculateSimpleExpression
	 * given a string of the format Q1+Q2-Q4 where Q1, Q2 and Q4 are answers to 
	 * previously asked numeric answer questions, calculates the results and returns
	 * it as a string
	 * USES LEFT-TO-RIGHT 'CALCULATOR' PRECEDENCE  NOT ALGEBRAIC
	 * @param strInput - string containing a simple mathematical expression such as
	 *  "Q1 + Q2"
	 *  TODO: combine with similar parsing function SimpleLogicMgr.parseComparisonList ???
	 * @param interviewId - needed to lookup variables (answers to previous questions)
	 * @param iType - needed to lookup variables (answers to previous questions)
	 * @param studyId - needed to lookup variables (answers to previous questions)
	 * @param listOfAlters - needed to lookup variables (answers to previous questions)
	 * @return the arithmetic result of the expression in string form
	 */
	
	private static String calculateSimpleExpression (String strInput, 
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters ) {
		Interview currentInterview;
		ArrayList<String> theList = new ArrayList<String>();
		String strReturn = "";
		String strNextNumber;
	    boolean bConvertedOkay = true;
	    MathOp mathOp = MathOp.ADD;
	    int iTemp;
	    int iResult = 0;
	    
		// First, check for special cases
		if ( strInput==null || strInput.length()==0)
			return(strInput);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strInput);
		
		theList = parseCalculationList(strInput);
		if ( theList.isEmpty())
			return(strInput);

		iResult = 0;
		for ( String str : theList ) {
			// System.out.println ( "calc " + str);
			if ( str.equals("+")) {
				mathOp = MathOp.ADD;
			} else if ( str.equals("-")) {
				mathOp = MathOp.SUB;
			} else if ( str.equals("/")) {
				mathOp = MathOp.DIV;
			} else if ( str.equals("*")) {
				mathOp = MathOp.MUL;
			} else {
				// first, attempt to treat str as a literal integer value
				// if the parse fails assume it is a question title.
				// ( question titles are our variables )
				try {
					iTemp = Integer.parseInt(str);
				} catch ( NumberFormatException e ) { // this catch is actually the normal flow
					strNextNumber = answerToQuestion(str, interviewId, iType, studyId, listOfAlters );
					// System.out.println ( "next number=" + strNextNumber + " " + str + " " + mathOp);
					if ( !bConvertedOkay) {
						strReturn += "?"+str+"? ";
						iTemp = 1;
					} else {
						try { 
							iTemp = Integer.parseInt(strNextNumber);
						} catch ( NumberFormatException nfe) {
							bConvertedOkay = false;
							iTemp = 1;
						}
					}
				}
				switch ( mathOp ) {
				   case ADD: iResult += iTemp; break;
				   case SUB: iResult -= iTemp; break;
				   case MUL: iResult *= iTemp; break;
				   case DIV: 
					    // special check for divide-by-zero
					    if ( iTemp==0 )
					    	return (" (Divide By Zero Error) ");
					    iResult /= iTemp; 
					    break;
				}
			}
		}
		if ( bConvertedOkay ) {
			strReturn = Integer.toString(iResult);
		}
		return(strReturn);
	}
	

	/**
	 * removes leading/trailing spaces and quotes from a string
	 * @param str the string to really trim
	 * @return str without quotes
	 */
	public static String trimQuotes ( String str ) {
		str = str.trim();
	    if ( str.startsWith("\""))
	    	str = str.substring(1);
	    if ( str.endsWith("\""))
	    	str = str.substring(0,str.length()-1);
	    str = str.trim();
	    return(str);
	}
	
	/**
	 * answerCountInsertion
	 * used with tags of the type <COUNT ... ... />
	 * this will accept any arbitrary string and, if markers of the format 
	 * <COUNT Question Answer />
	 * are found, will create a new string by substituting in the number of
	 * times Answer was given to Question
	 * For example, if Question Q_RELATION in the ALTER section asked the relationship
	 * between the ego and the alters a later question might as
	 * "Of the <COUNT Q_RELATION "brother"> brothers you list, how many are alive?"
	 * The pattern for embedded calculations is <CALC ... />
	 * This is a static function in anticipation of cases where it has to be used on strings
	 * not immediately associated with this question.
	 * Note that is function does not need as much information as other functions that
	 * need to look up answers to perform the text substitution, as the COUNT tags
	 * will need to examine ALL answers to a given question, not just the answer given
	 * by one alter or an alter pair
	 * @param strInput - the full prompt to examine
	 * @param interviewId - needed to look up answers
	 * @param studyId - needed to look up answers
	 * @return a new version of strInput with tags <COUNT .../> replaced with appropriate values
	 */

	public static String answerCountInsertion (String strInput, 
			Long interviewId, Long studyId) {
		Interview currentInterview;
		String strResult = "";
		String pattern = "<COUNT.*?/>";
		String str;
		String strExpression;
		String strQuestionTitle;
		String strAnswerToCount;
		String strCountValue;
		ArrayList<String> theList = null;
		int firstSpace;
		int ix;

		// if no interviewId we are previewing the question
		// return original prompt unaltered
		if ( interviewId==null )
			return(strInput);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strInput);
		
		theList = parseExpressionList( strInput, pattern);
		if ( theList==null )
			return(strInput);
		
		// At this point we have an array list with literal strings
		// alternating with variable markers .
		// now construct the output string by replace the
		// question names between the <VAR /> markers with the answer from
		// that question
		for ( ix=0 ; ix<theList.size(); ++ix ) {
			str = theList.get(ix);
			strExpression = trimPrefixAndSuffix(str, "<COUNT", "/>");
			if ( strExpression != null ) {
				firstSpace = strExpression.indexOf(" "); // split strExpression into question title and answer
				if ( firstSpace <= 0 ) {
					strResult += "[ERROR " + strExpression + " COUNT needs question and answer]";
				} else {
				    // flips and twists to deal with whitespace and (optional) quotes
				    strQuestionTitle = strExpression.substring(0,firstSpace).trim();
				    strAnswerToCount = strExpression.substring(firstSpace).trim();
				    strQuestionTitle = trimQuotes(strQuestionTitle);
				    strAnswerToCount = trimQuotes(strAnswerToCount);
				    if ( strQuestionTitle.length()==0 || strAnswerToCount.length()==0 ) {
						strResult += "[ERROR " + strExpression + " COUNT needs Question and Answer]";
				    } else {
				    	strCountValue = getAnswerCountToQuestion(strQuestionTitle, strAnswerToCount, interviewId, studyId );
				        strResult += " " + strCountValue + " ";
				    }
				}
			} else {
				strResult += str;
			}
		} 
		return(strResult);
	}		

	/**
	 * scan the input string for tags of the format <CONTAINS question "answer" /> and 
	 * replaces the text with the number of times this alter ( or alter-pair, or ego )
	 * answered the question with the answer.  The result should be zero or one.
	 * This will mostly be used in "Use If" expressions.
	 * @param strInput
	 * @param interviewId
	 * @param iType
	 * @param studyId
	 * @param listOfAlters
	 * @return
	 */
	public static String questionContainsAnswerInsertion (String strInput, 
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters) {
		Interview currentInterview;
		String strResult = "";
		String pattern = "<CONTAINS.*?/>";
		String str;
		String strExpression;
		String strQuestionTitle;
		String strAnswerToCount;
		String strCountValue;
		ArrayList<String> theList = null;
		int firstSpace;
		int ix;

		// if no interviewId we are previewing the question
		// return original prompt unaltered
		if ( interviewId==null )
			return(strInput);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strInput);
		
		theList = parseExpressionList( strInput, pattern);
		if ( theList==null )
			return(strInput);
		
		// At this point we have an array list with literal strings
		// alternating with variable markers .
		// now construct the output string by replace the
		// question names between the <VAR /> markers with the answer from
		// that question
		for ( ix=0 ; ix<theList.size(); ++ix ) {
			str = theList.get(ix);
			strExpression = trimPrefixAndSuffix(str, "<CONTAINS", "/>");
			if ( strExpression != null ) {
				firstSpace = strExpression.indexOf(" "); // split strExpression into question title and answer
				if ( firstSpace <= 0 ) {
					strResult += "[ERROR " + strExpression + " CONTAINS needs question and answer]";
				} else {
				    // flips and twists to deal with whitespace and (optional) quotes
				    strQuestionTitle = strExpression.substring(0,firstSpace).trim();
				    strAnswerToCount = strExpression.substring(firstSpace).trim();
				    strQuestionTitle = trimQuotes(strQuestionTitle);
				    strAnswerToCount = trimQuotes(strAnswerToCount);
				    // System.out.println ( "question=" + strQuestionTitle);
				    // System.out.println ( "  answer=" + strAnswerToCount);
				    if ( strQuestionTitle.length()==0 || strAnswerToCount.length()==0 ) {
						strResult += "[ERROR " + strExpression + " CONTAINS needs Question and Answer]";
				    } else {
				    	strCountValue = getQuestionContains(strQuestionTitle, strAnswerToCount, 
				        		interviewId, iType, studyId, listOfAlters );
				        strResult += " " + strCountValue + " ";
				    }
				}
			} else {
				strResult += str;
			}
		} 
		return(strResult);
	}		

	
	/**
	 * used to find how many of a given answer where given in response to a specific
	 * question.  This is used only in the ALTER section.  For example, if a question
	 * uses a selection of Male or Female for each alter, a prompt could include this:
	 * "Of the <COUNT alter_sex "male"/> men you had sex with..."
	 * @param strQuestionTitle identifies the question (alter_sex in example)
	 * @param strSurveyAnswer answer to count ( "male" in example )
	 * @param interviewId - needed for query
	 * @param studyId - needed for query
	 * @return - a count of the times this answer was given to this question, -1 on error
	 */
	public static String getAnswerCountToQuestion ( String strQuestionTitle, String strSurveyAnswer, 
			Long interviewId, Long studyId ) {
		List<Alter> listOfAlters = Alters.getForInterview(interviewId);
		ArrayList<Alter> alterPair;
		Interview currentInterview;
		QuestionOption answerOption;
		List<QuestionOption> optionsList = null;
		Question question = null;
		Answer theAnswer = null;
		String strAnswer = strQuestionTitle;
		Long   iOptionId;
		int iCount = 0;

		if ( interviewId==null )
			return(strQuestionTitle);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strQuestionTitle);
		
		strSurveyAnswer = strSurveyAnswer.trim();
		question = Questions.getQuestionUsingTitleAndTypeAndStudy (strQuestionTitle, QuestionType.ALTER, studyId);
	
		if ( question==null )
			return ("[ question " + strQuestionTitle + " not found]"); 
		currentInterview = Interviews.getInterview(interviewId);
		optionsList = Options.getOptionsForQuestion(question.getId());
		 
		for ( Alter alter : listOfAlters ) {
			alterPair = new ArrayList<Alter>();
			alterPair.add(alter);
			theAnswer = Answers.getAnswerForInterviewQuestionAlters( currentInterview, question, alterPair);
			
			if ( theAnswer!=null ) {
			    switch ( theAnswer.getAnswerType()) {
				    case SELECTION:
				    case MULTIPLE_SELECTION:
				         strAnswer = "";
				         // the answer is a list of comma separated ids
				         // that is, a list of optionIDs
				         for ( String strOption : theAnswer.getValue().split(",")) {
			   		         try {
					             iOptionId = Long.parseLong(strOption);
					         } catch ( NumberFormatException nfe ) {
					            iOptionId = -1L;
					         }
					     // Now find the option that this ID refers to
					     answerOption = null;
					     for ( QuestionOption qo : optionsList ) {
						     if ( qo.getId().equals(iOptionId)) {
						    	 answerOption = qo;
						     }
					     }
					     // lastly, if this answer matches the answer we 
					     // are counting up increment our count
					     if ((answerOption != null) && strSurveyAnswer.equalsIgnoreCase(answerOption.getName().trim()))
						     ++iCount;
				         }
				         break;
			        case TEXTUAL:
			        case NUMERICAL:
				         strAnswer = theAnswer.getValue().trim();
				         if ( strSurveyAnswer.equals(strAnswer))
					         ++iCount;
				        break;
			    }
			}
		}
	return(Integer.toString(iCount));
	}
	
	/**
	 * this is similar to getAnswerCountToQuestion above, but tests to see if 
	 * the answer to a question contains a string for just one alter ( or alter pair)
	 * this is so the <CONTAINS logic can be used for skipping questions
	 * @param strQuestionTitle
	 * @param strSurveyAnswer
	 * @param interviewId
	 * @param iType
	 * @param studyId
	 * @param listOfAlters
	 * @return
	 */
	public static String getQuestionContains ( String strQuestionTitle, String strSurveyAnswer, 
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters   ) {
		Interview currentInterview;
		QuestionOption answerOption;
		List<QuestionOption> optionsList = null;
		Question question = null;
		Answer theAnswer = null;
		String strAnswer = strQuestionTitle;
		String   userAnswer;
		String[] answersToLookFor;
		Long   iOptionId;
		int iCount = 0;

		if ( interviewId==null )
			return(strQuestionTitle);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strQuestionTitle);
		
		answersToLookFor = strSurveyAnswer.trim().split(":");
		question = Questions.getQuestionUsingTitleAndTypeAndStudy (strQuestionTitle, iType, studyId);
	
		if ( question==null )
			return ("[ " + strQuestionTitle + " not found]");
		
		currentInterview = Interviews.getInterview(interviewId);
		optionsList = Options.getOptionsForQuestion(question.getId()); 
		theAnswer = Answers.getAnswerForInterviewQuestionAlters( currentInterview, question, listOfAlters);
			
		if ( theAnswer!=null ) {	
		    switch ( theAnswer.getAnswerType()) {
			    case SELECTION:
			    case MULTIPLE_SELECTION:
			         strAnswer = "";
			         // the answer is a list of comma separated ids
			         // that is, a list of optionIDs
			         for ( String strOption : theAnswer.getValue().split(",")) {
		   		         try {
				             iOptionId = Long.parseLong(strOption);
				         } catch ( NumberFormatException nfe ) {
				            iOptionId = -1L;
				         }
				     // Now find the option that this ID refers to
				     answerOption = null;
				     for ( QuestionOption qo : optionsList ) {
					     if ( qo.getId().equals(iOptionId)) 
					    	 answerOption = qo;
				     }
				     // lastly, if this answer matches the answer we 
				     // are counting up increment our count
				     // if ((answerOption != null) && strSurveyAnswer.equalsIgnoreCase(answerOption.getName().trim()))
					 //    ++iCount;
			         // }
				     if ( answerOption != null ) {
				    	 userAnswer = answerOption.getName().trim();
				    	 for ( String str : answersToLookFor ) {
				    		 if ( str.equalsIgnoreCase(userAnswer))
				    			 ++iCount;
				    	 }
				     }
			         }
			         break;
		        case TEXTUAL:
		        case NUMERICAL:
			         strAnswer = theAnswer.getValue().trim();
			         for ( String str : answersToLookFor ) {
			        	 if ( str.equals(strAnswer))
			        		 ++iCount;
			         	}
			        break;
		    }
		}
	return(Integer.toString(iCount)); // Should be zero or one
	}

	/**
	 * conditionalTextInsertion
	 * this will accept any arbitrary string and, if markers of the format <VAR ... />
	 * are found, will create a new string by substituting in answers.
	 * For example, if Question Q1 asked how many times last week a person smoked crack,
	 * a later question might be "Of the <VAR Q1 /> times you smoked crack last week, how many
	 * times did you also drink?"
	 * The pattern for embedded variables is <VAR ... />
	 * This is a static function in anticipation of cases where it has to be used on strings
	 * not immediately associated with this question.
	 * @param strInput - the original string to (possibly) alter
	 * @param interviewId - needed to look up variable values ( answers to previous questions )
	 * @param iType - needed to look up variable values ( answers to previous questions )
	 * @param studyId - needed to look up variable values ( answers to previous questions )
	 * @param listOfAlters - needed to look up variable values ( answers to previous questions )
	 * @return strInput, but with any <IF /> tags either removed or replaced by text
	 */

	public static String conditionalTextInsertion (String strInput, 
			Long interviewId, Question.QuestionType iType, Long studyId, ArrayList<Alter> listOfAlters ) {
		Interview currentInterview;
		String strResult = "";
		String pattern = "<IF.*?/>";
		String strContents;
		String strExpression;
		String strText;
		String str;
		ArrayList<String> theList;
		int iExpressionResult;
		int iLastQuote;
		int iFirstQuote;
		int ix;

		// if no interviewId we are previewing the question
		// return original prompt unaltered
		if ( interviewId==null )
			return(strInput);
		currentInterview = Interviews.getInterview(interviewId);
		if ( currentInterview==null || currentInterview.getStudyId()==null )
			return(strInput);
		
		theList = parseExpressionList ( strInput, pattern);
		if (theList==null)
			return(strInput);

		// At this point we have an array list with literal strings
		// alternating with variable markers .
		// now construct the output string by replace the
		// question names between the <VAR /> markers with the answer from
		// that question
		for ( ix=0 ; ix<theList.size(); ++ix ) {
			str = theList.get(ix);
			strContents = trimPrefixAndSuffix ( str, "<IF", "/>"); 
			if ( strContents!=null ) {
				iLastQuote = strContents.lastIndexOf("\"");
				iFirstQuote = strContents.lastIndexOf("\"", iLastQuote-1);
				// System.out.println ( "quotes=" + iFirstQuote + "," + iLastQuote);
				if ( iLastQuote<0 || iFirstQuote<0) {
					System.out.println ("ERROR Problem in TextInsertionUtil.conditionalTextInsertion, missing quotes");
					System.out.println ("Returning " + strResult);
					return(strResult);
				}
				strExpression = strContents.substring(0,iFirstQuote);
				strText = strContents.substring(iFirstQuote+1, iLastQuote);
				// System.out.println ( "strExpression=#" + strExpression + "#");
				// System.out.println ("strText=#" + strText + "#");
				iExpressionResult = SimpleLogicMgr.createSimpleExpressionAndEvaluate ( 
						strExpression, interviewId, iType, studyId, listOfAlters );
			    if ( iExpressionResult != 0 )
				    strResult += " " + strText + " ";
				
			} else {
				strResult += str;
			}
		} 
		return(strResult);
	}	
	
	/**
	 * converts a string to an Arraylist of strings that alternate between those 
	 * matching a regular expression and those that don't
	 * @param strInput the original string to alter
	 * @param strRegExp the regular expression describing the pattern to search for
	 * @return an ArrayList<String> with alternating strings matching the pattern and not
	 */

	private static ArrayList<String> parseExpressionList ( String strInput, String strRegExp) {
		
		Pattern p = Pattern.compile(strRegExp, Pattern.CASE_INSENSITIVE );
		Matcher matcher = p.matcher(strInput);
		ArrayList<String> theList = null;
		boolean found = false;
		int iVarCount = 0;
		int iStartIndex = 0;
		int iVarStart;
		int iVarEnd;

		// First, check for special cases
		if ( strInput==null || strInput.length()==0)
			return(theList);
		
		// another special check
		found = matcher.find(iStartIndex);
		if ( !found )
			return(theList);
		theList = new ArrayList<String>();
		// Second, split the input string into substrings, which will be 
		// literal portions and variable portions ( <VAR.../>  ) (or the strRegExp)
		while ( found ) {
			found = matcher.find(iStartIndex);
			if ( found ) {
				++iVarCount;
				iVarStart = matcher.start();
				iVarEnd = matcher.end();
				if ( iVarStart>iStartIndex )
					theList.add( strInput.substring(iStartIndex,iVarStart));
				theList.add(strInput.substring(iVarStart,iVarEnd));
				iStartIndex = iVarEnd;
			} else {
				theList.add(strInput.substring(iStartIndex));
			}
		}
	return(theList);
	}
	
	/**
	 * IF a string starts with the prefix and ends with the suffix this extracts the middle
	 * and returns it.
	 * This is useful in the above functions that need to look for and deal with specific
	 * tags 'enclosing' things like question titles, <VAR Q1 /> et al
	 * @param strInput the original string to 'tear apart'
	 * @param strPrefix the starting string
	 * @param strSuffix the ending string
	 * @return null if the prefix AND suffix are not present, otherwise
	 * the middle portion minus these
	 */
	
	private static String trimPrefixAndSuffix ( String strInput, String strPrefix, String strSuffix ) {
		
		String strReturn = null;
		int preLen;
		int sufLen;
		
		if ( strInput.startsWith(strPrefix)  &&  strInput.endsWith(strSuffix)) {
			preLen = strPrefix.length();
			sufLen = strSuffix.length();
			strReturn = strInput.substring(preLen, strInput.length()-sufLen).trim(); // extract question title
		}
		return(strReturn);
	}
	
	/**
	 * creates an arrayList of strings from a larger input string
	 * in anticipation of (rather simple) expression calculation.
	 * For our purposes the calculations will only involve variables
	 * separated by the operands +,-,*,/
	 * @param strInput  string of the form Q1+Q2-4
	 * @return Arraylist of the form "q1","+","Q2","-","4"
	 */
	private static ArrayList<String> parseCalculationList ( String strInput ) {
		
	// now create an array of strings alternating between "-" and "+"
	// and the text segments between them.  Have to do this the hard way
	ArrayList<String> theList = new ArrayList<String>();
	int iInputLength;
	int iWordStart;
	int iWordEnd;
	char ch;
	    
	ch = ' ';
	iWordStart = iWordEnd = 0;
	iInputLength = strInput.length();
	for ( iWordEnd=0 ; iWordEnd<iInputLength; ++iWordEnd ) {
		ch = strInput.charAt(iWordEnd);
		if ( ch=='+' || ch=='-' || ch=='/' || ch=='*') {
			if ( iWordEnd>iWordStart+1) 
				theList.add(strInput.substring(iWordStart,iWordEnd).trim());
			theList.add(new String(new char[]{ch}));
			iWordStart = iWordEnd+1;
		}
	}
	// pick up the trailing word
	if ( iWordStart < iInputLength )
		theList.add(strInput.substring(iWordStart).trim());
	return(theList);
	}

	/**
	 * simple little function to get the index of the month.
	 * compare just the first three letters to avoid confusion with
	 * misspellings and whether or not abreviations have a period.
	 * also allow for 1 based indexs
	 * @param str a string which (should be) a month
	 * @return one based index
	 */
	private static int getMonthIndex ( String str ) {
		int ix;
		int iValue;
		
		if (str==null || str.length()==0)
			return(-1);
		str = str.trim();
		if ( str.length()>3 )
			str = str.substring(0,3);
		for ( ix=0 ; ix<strMonths.length ; ++ix ) {
			if ( str.equalsIgnoreCase(strMonths[ix].substring(0,3)))
				return(ix);
		}
		
		// if the month value does not match one of the three letter
		// abreviations it may be the 1 based index of the month
		
		try {
			iValue = Integer.parseInt(str);
		} catch ( NumberFormatException nfe ) {
			return(-1);
		}
		
		while ( iValue < 1 )
			iValue += 12;
		while ( iValue>12 )
			iValue -= 12;
		return(iValue-1);
	}

	/** 
	 * a simple convenience function to convert a date in string format
	 * to three integers.  assumes the date is of the format
	 * MMM DD YYYY
	 * where YYY and DD are already integers and the month MM
	 * is either an integer or a string
	 * @param strDate date in format "Mar 27 1958"
	 * @return array int[3] = {3, 27, 1958};
	 */
	private static int[] strDateToNumeric ( String strDate) {
		String[] dateInfo;
		int[] returnInfo = new int[3];
		
		returnInfo[0] = returnInfo[1] = returnInfo[2] = 0;
		dateInfo = strDate.split(" ");

		switch ( dateInfo.length ) {
		   default:
		    case 3: 
		    	 try {
		    	    returnInfo[2] = Integer.parseInt(dateInfo[2].trim());
		    	   } catch ( NumberFormatException nfe3 ) {
		    		   returnInfo[2] = 0;
		    	   }
		    	   // fall thru
		    case 2:
		    	 try {
		    		 returnInfo[1] = Integer.parseInt(dateInfo[1].trim());
		    	 } catch ( NumberFormatException nfe1 ) {
		    		 returnInfo[1] = 0;
		    	 }
		    	 // fall thru
		    case 1:
		    	 returnInfo[0] = getMonthIndex(dateInfo[0].trim());
		    	 // if the month is out of bounds most likely 
		    	 // there was an error in constructing the <DATE tag
		    	 if ( returnInfo[0]<0 ) {
		    		 System.out.println ( "strDate=" + strDate);
		    		 System.out.println ( "returnInfo[0]=" + returnInfo[0]);
		    		 returnInfo[0] = 0;
		    	 }
		    	 if ( returnInfo[0]>11 ) {
		    		 System.out.println ( "strDate=" + strDate);
		    		 System.out.println ( "returnInfo[0]=" + returnInfo[0]);
		    		 returnInfo[0] = 11;
		    	 } 
		}
		return(returnInfo);
	}
	
	/**
	 * applys an offset to the appropriate field of a date represented by an
	 * array of 3 ints.
	 * @param date 'starting point' date
	 * @param dateField the field in increment/decrement
	 * @param offset amount to add/subtract from the specified field
	 * @return a new array of three ints representing a date
	 */
	private static int[] applyOffsetToDate ( int[] date, DATE_FIELD dateField, int offset ) {
		int returnArray[] = new int[3];
		int Month = DATE_FIELD.MONTH.ordinal();
		int Day   = DATE_FIELD.DAY.ordinal();
		int Year  = DATE_FIELD.YEAR.ordinal();
		
		returnArray[Month] = date[Month];
		returnArray[Day] = date[Day];
		returnArray[Year] = date[Year];
		
		switch ( dateField ) {
			case NONE:
				 break;
			case DAY:
				 returnArray[Day] += offset;
				 while ( returnArray[Day]<1 ) {
					 returnArray[Day] += 31;
					 --returnArray[Month];
					 if ( returnArray[Month] < 0 ) {
						 returnArray[Month] = 11;
						 --returnArray[Year];
					 }
				 }
				 
				 while ( returnArray[Day]>31 ) {
					 returnArray[Day] -= 31;
					 ++returnArray[Month];
					 if ( returnArray[Month] > 11 ) {
						 returnArray[Month] = 0;
						 ++returnArray[Year];
					 }
				 }
				 break;
			case MONTH: 
				 returnArray[Month] += offset;
				 while ( returnArray[Month] < 0 ) {
					 returnArray[Month] += 12;
					 --returnArray[Year];
				 }
				 while ( returnArray[Month] > 11 ) {
					 returnArray[Month] -= 12;
					 ++returnArray[Year];
				 }
				 break;
			case YEAR:
				 returnArray[Year] += offset;
				 break;
		}
		if ( returnArray[Day] > monthCaps[returnArray[Month]])
			returnArray[Day] = monthCaps[returnArray[Month]];
		return(returnArray);
	}
	
	/**
	 * this will be for 'preview' panels, where we need to display the
	 * text of a question and its variable insertion tags as-is but
	 * escaped so they are not interpreted by the HTML functions.
	 * Also, this will do *some* syntax checking
	 * @param strInput string to escape
	 * @return same string, but escaped
	 */
	
	public static String escapeTextInsertionTags ( String strInput ) {
		String[] pattern = { 
				"<VAR.*?/>", "<DATE.*?/>", "<CALC.*?/>", "<COUNT.*?/>", "<CONTAINS.*?/>", "<IF.*?/>"};
		String[] strStartTag = { 
				"<VAR", "<DATE", "<CALC", "<COUNT", "<CONTAINS", "<IF"};
		String[] strStartTagReplacement = {
				"&lt;VAR ", "&lt;DATE ", "&lt;CALC ", "&lt;COUNT ", "&lt;CONTAINS ", "&lt;IF "};				
		String strEndTag = "/>";
		String strEndTagReplacement = " /&gt;";
		String innerString;
		String newString = "";
		ArrayList<String> theList;
		int ix;
		
		if ( strInput==null || strInput.length()==0)
			return(strInput);
		
		for ( ix=0 ; ix<pattern.length ; ++ix ) {
			theList = parseExpressionList ( strInput, pattern[ix]);
			if ( theList != null ) {
				newString = "";
		        for ( String string : theList ) {
			        if ( string.startsWith(strStartTag[ix])) {
				        innerString = trimPrefixAndSuffix ( string, strStartTag[ix], strEndTag);
				        innerString = tagSpecificVerification (innerString, ix);
				        newString += strStartTagReplacement[ix] + innerString + strEndTagReplacement;
			        } else {
				        newString += string;
			        }
		        }
		        strInput = newString;
			}
		}
	return(strInput);
	}
		
	/**
	 * will do (simple) verification of the syntax of specific tags
	 * @param str string within the start and stop tags ( <VAR ... />
	 * @param iTagIndex an integer indicating which tag we are verifying.
	 * look to the inner variables of escapeTextInsertionTags and the
	 * switch cases statements for explanation
	 * @return the original string with (optional) warning messages appended.
	 */
	private static String tagSpecificVerification (String str, int iTagIndex) {
		String strReturn = str;
		String[] innerList;
		int innerWordCount;
		
		innerList = str.trim().split(" ");
		innerWordCount = innerList.length;
		
   	 	if (innerWordCount<1 ) {
   	 		strReturn += " NO PARAMETER ";
   	 	} else if (innerWordCount==1 && innerList[0].trim().length()==0 ) {
   	 		strReturn += " EMPTY ";
   	 	} else {
   	 		switch ( iTagIndex ) {
   	 			case 0: // VAR
   	 				 if ( innerWordCount>1 ) {
   	 					strReturn += " TOO MANY PARAMETERS ";
   	 				 }
   	 				 break;
   	 			case 1: // DATE
   	 				 if ( innerWordCount>3 ) {
   	 					 strReturn += " TOO MANY PARAMETERS ";
   	 				 }
   	 				 break;
   	 			case 2: // CALC
   	 				 if ( innerWordCount>3 ) {
   	 					 strReturn += " TOO MANY PARAMTERS ";
   	 				 }
   	 				 break;
   	 			case 3: // COUNT
   	 				 break;
   	 			case 4: // CONTAINS
   	 				 break;
   	 			case 5: // IF
   	 				 break;
   	 			default:
   	 				break;
   	 		}
		}
		return(strReturn);
	}
}


