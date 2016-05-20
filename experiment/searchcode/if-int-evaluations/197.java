package ee.widespace.banner.statistic;

import ee.widespace.stat.*;
import ee.widespace.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
import java.io.*;

/**
 * Insert the type's description here.
 * @author: Dmitri Silinski
 */
public class TimeStatisticTag implements BodyTag {
	private   Tag         parent;
	protected PageContext pageContext;
	protected BodyContent bodyContent;

	private String id;
	private String time;

	private int sum;
	private int hour;

	private java.util.SortedMap statMap;
	private java.util.Iterator iterator;
 /**
     * Actions after some body has been evaluated.
     * <p>
     * Not invoked in empty tags or in tags returning SKIP_BODY in doStartTag()
     * This method is invoked after every body evaluation.
     * The pair "BODY -- doAfterBody()" is invoked initially if doStartTag()
     * returned EVAL_BODY_TAG, and it is repeated as long
     * as the doAfterBody() evaluation returns EVAL_BODY_TAG
     * <p>
     * The method re-invocations may be lead to different actions because
     * there might have been some changes to shared state, or because
     * of external computation.
     *
     * @returns whether additional evaluations of the body are desired
     * @seealso #doInitBody
     */
public int doAfterBody() throws javax.servlet.jsp.JspException {
	if ( hour < 24  ) {
		
		StringBuffer b = new StringBuffer();
		b.append(hour++);
		if ( b.length() < 2 ) b.insert(0, '0');
		b.append(".00");
		String p = b.toString();
		TimeStat ts = (TimeStat)statMap.get( p );
		if ( ts == null ) {
			ts = new TimeStat();
		} else {
			ts.setPercent( (double)Math.round(1000.0*ts.getVisits()/sum)/10 );
		}
		pageContext.setAttribute( id, ts );
		pageContext.setAttribute( time, p );
		return EVAL_BODY_AGAIN;
	}

	try {
		bodyContent.writeOut( bodyContent.getEnclosingWriter() );
	} catch ( IOException e ) {
		throw new JspException( e.getMessage() );
	}

	return SKIP_BODY;
}
/**
 * doEndTag method comment.
 */
public int doEndTag() throws JspException {
	return EVAL_PAGE;
}
 /**
     * Prepare for evaluation of the body.
     * <p>
     * The method will be invoked once per action invocation by the page implementation
     * after a new BodyContent has been obtained and set on the tag handler
     * via the setBodyContent() method and before the evaluation
     * of the tag's body into that BodyContent.
     * <p>
     * This method will not be invoked if there is no body evaluation.
     *
     * @seealso #doAfterBody
     */
public void doInitBody() throws JspException {
	StringBuffer b = new StringBuffer();
	b.append(hour++);
	if ( b.length() < 2 ) b.insert(0, '0');
	b.append(".00");
	String p = b.toString();
	TimeStat ts = (TimeStat)statMap.get( p );
	if ( ts == null ) {
		ts = new TimeStat();
	} else {
		ts.setPercent( (double)Math.round(1000.0*ts.getVisits()/sum)/10 );
	}
	pageContext.setAttribute( id, ts );
	pageContext.setAttribute( time, p );
}
/**
 * doStartTag method comment.
 */
public int doStartTag() throws JspException {
	long start = System.currentTimeMillis();
	try {
        ServletContext context = pageContext.getServletContext();
        String url = context.getInitParameter("jdbc.url");
        Connection conn = DriverManager.getConnection(url);
        try {
	        StatisticData data = new StatisticData( conn );
	        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
			String statType = req.getParameter("type");
      
		    statMap = (java.util.SortedMap)req.getAttribute("statData");
		    hour = 0;
	    	pageContext.setAttribute( "statID", id );
	    	sum = ((Integer)req.getAttribute("sumVisits")).intValue();
		} finally {
	        conn.close();
        }
    } catch (Exception e) {
        e.printStackTrace();
        throw new JspException(e.getMessage());
    }
	System.out.println( "SiteStat: " + (System.currentTimeMillis()-start) );
		
    return EVAL_BODY_AGAIN;
}
/**
 * getParent method comment.
 */
public Tag getParent() {
	return parent;
}
/**
 * release method comment.
 */
public void release() {
	parent      = null;
	pageContext = null;
	bodyContent	= null;
	id 			= null;
	time		= null;
	statMap		= null;
	iterator	= null;
}
 /**
     * Setter method for the bodyContent property.
     * <p>
     * This method will not be invoked if there is no body evaluation.
     *
     * @param b the BodyContent
     * @seealso #doInitBody
     * @seealso #doAfterBody
     */
public void setBodyContent( BodyContent b ) {
	bodyContent = b;
}
/**
 * Insert the method's description here.
 * Creation date: (27.06.2001 17:02:06)
 * @param id java.lang.String
 */
public void setId(String id) {
	this.id = id;
}
/**
 * setPageContext method comment.
 */
public void setPageContext( PageContext pc ) {
	pageContext = pc;
}
/**
 * setParent method comment.
 */
public void setParent( Tag tag ) {
	parent = tag;
}
/**
 * Insert the method's description here.
 * Creation date: (27.06.2001 17:02:06)
 * @param id java.lang.String
 */
public void setTime(String time) {
	this.time = time;
}
}

