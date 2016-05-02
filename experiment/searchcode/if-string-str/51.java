//==============================================================================
// QueryDobj.java
// Copyright (c) 2001 WAP Solutions Ltd.
//==============================================================================

package wsl.mdn.dataview;

// imports
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.StringTokenizer;

import wsl.fw.util.Util;
import wsl.fw.util.Log;
import wsl.fw.datasource.*;
import wsl.fw.exception.MdnException;
import wsl.mdn.guiconfig.MenuAction;

//------------------------------------------------------------------------------
/**
 * DataObject that persists Queries
 */
public class QueryDobj extends DataObject
{
    // version tag
    private final static String _ident = "$Date: 2003/06/12 23:26:09 $  $Revision: 1.2 $ "
        + "$Archive: /Mobile Data Now/Source/wsl/mdn/dataview/QueryDobj.java $ ";

    //--------------------------------------------------------------------------
    // constants

    public final static String ENT_QUERY            = "TBL_QUERY";
    public final static String FLD_ID               = "FLD_ID";
    public final static String FLD_NAME             = "FLD_NAME";
    public final static String FLD_TYPE             = "FLD_TYPE";
    public final static String FLD_PARENTID       	= "FLD_PARENTID";
    public final static String FLD_CRITERIA         = "FLD_CRITERIA";
    public final static String FLD_DESCRIPTION      = "FLD_DESCRIPTION";
    public final static String FLD_SORTS            = "FLD_SORTS";
    public final static String FLD_GROUPFIELDID     = "FLD_GROUPFIELDID";
    public final static String FLD_RAWQUERY         = "FLD_RAWQUERY";
    public final static String FLD_CLASS            = JdbcDataSource.CLASS_COLUMN_NAME;
    
    //  ---------------- INSERTED THESE FIELDS FOR MESSAGING INFO ------------------ //
    public final static String FLD_EMAIL_KEYWORD         = "FLD_EMAIL_KEYWORD";
    public final static String FLD_SMS_KEYWORD           = "FLD_SMS_KEYWORD";
    public final static String FLD_IM_KEYWORD            = "FLD_IM_KEYWORD";
    public final static String FLD_TIMEOUT				 = "FLD_TIMEOUT";
    
    public final static String FLD_EMAIL_ADDRESS_ID      = "FLD_EMAIL_ADDRESS_ID";
    public final static String FLD_EMAIL_DISPLAY_RESULT  = "FLD_EMAIL_DISPLAY_RESULT";    
    
    public final static String FLD_MOBILE_STATUS   	 	 = "FLD_MOBILE_STATUS";    
    public final static String FLD_MOBILE_DISPLAY_RESULT = "FLD_MOBILE_DISPLAY_RESULT";
    
    public final static String FLD_IM_STATUS   		 	 = "FLD_IM_STATUS";    
    public final static String FLD_IM_DISPLAY_RESULT   	 = "FLD_IM_DISPLAY_RESULT";
    
    public final static String FLD_CONDITION_SEPERATOR   = "FLD_CONDITION_SEPERATOR";

    public final static String FLD_RESPONSE              = "FLD_RESPONSE";
    public final static String FLD_DS_STATUS		 	 = "FLD_DS_STATUS";
    public final static String FLD_WS_ID		 		 = "FLD_WS_ID";
    public final static String FLD_PROJECT_ID        	 = "FLD_PROJECT_ID";
    
    //FK
    public final static String FLD_DB_ID 		     = "FLD_DB_ID";
    
    // ----------------------------------------------------------------------------- //

    public final static String FLD_DEL_STATUS   		 = "FLD_DEL_STATUS";

    public final static String DELIM_AND            = "\f";
    public final static String DELIM_OPS            = "\t";

    //--------------------------------------------------------------------------
    // attributes

    private Query _impl = null;
    private transient DataView _dv = null;
    private transient EntityDobj _table = null;
    private Vector _criteria = new Vector();
    private Vector _sorts = new Vector();
    private String _imageCriteria = null;
    private String _imageSorts = null;
    protected boolean _isImaging = false;
    private boolean _criteriaLoaded = false;
    private boolean _sortsLoaded = false;


    //--------------------------------------------------------------------------
    // construction

    /**
     * Blank ctor
     */
    public QueryDobj()
    {
    }


    //--------------------------------------------------------------------------
    // persistence

    /**
     * Static factory method to create the entity to be used by this dataobject
     * and any subclasses. This is called by the DataManager's factory when
     * creating a ENTITY entity.
     * @return the created entity.
     */
    public static Entity createEntity()
    {
        // create the entity
        Entity ent = new EntityImpl(ENT_QUERY, QueryDobj.class);

        // add the key generator for the system id
        ent.addKeyGeneratorData(new DefaultKeyGeneratorData(ENT_QUERY, FLD_ID));

        // create the fields and add them to the entity
        ent.addField(new FieldImpl(FLD_ID, Field.FT_INTEGER, Field.FF_SYSTEM_KEY | Field.FF_UNIQUE_KEY));
        ent.addField(new FieldImpl(FLD_NAME, Field.FT_STRING, Field.FF_NAMING));
        ent.addField(new FieldImpl(FLD_TYPE, Field.FT_STRING));
        ent.addField(new FieldImpl(FLD_PARENTID, Field.FT_INTEGER));
        ent.addField(new FieldImpl(FLD_DESCRIPTION, Field.FT_STRING));
        ent.addField(new FieldImpl(FLD_CRITERIA, Field.FT_STRING, Field.FF_NONE, 1024));
        ent.addField(new FieldImpl(FLD_SORTS, Field.FT_STRING, Field.FF_NONE, 1024));
        ent.addField(new FieldImpl(FLD_GROUPFIELDID, Field.FT_INTEGER));
        ent.addField(new FieldImpl(FLD_RAWQUERY, Field.FT_STRING, Field.FF_NONE, 1024));
        ent.addField(new FieldImpl(FLD_CLASS, Field.FT_STRING)); // polymorph support
        
        ent.addField(new FieldImpl(FLD_EMAIL_KEYWORD, Field.FT_STRING, Field.FF_NONE));
        ent.addField(new FieldImpl(FLD_SMS_KEYWORD, Field.FT_STRING, Field.FF_NONE));
        ent.addField(new FieldImpl(FLD_IM_KEYWORD, Field.FT_STRING, Field.FF_NONE));
        ent.addField(new FieldImpl(FLD_TIMEOUT, Field.FT_STRING, Field.FF_NONE));
        
        ent.addField(new FieldImpl(FLD_EMAIL_ADDRESS_ID, Field.FT_INTEGER, Field.FF_NONE));
        ent.addField(new FieldImpl(FLD_EMAIL_DISPLAY_RESULT, Field.FT_INTEGER, Field.FF_NONE));        
        
        ent.addField(new FieldImpl(FLD_MOBILE_STATUS, Field.FT_INTEGER, Field.FF_NONE));    
        ent.addField(new FieldImpl(FLD_MOBILE_DISPLAY_RESULT, Field.FT_INTEGER, Field.FF_NONE));        
        
        ent.addField(new FieldImpl(FLD_IM_STATUS, Field.FT_INTEGER, Field.FF_NONE));    
        ent.addField(new FieldImpl(FLD_IM_DISPLAY_RESULT, Field.FT_INTEGER, Field.FF_NONE));
        
        ent.addField(new FieldImpl(FLD_CONDITION_SEPERATOR, Field.FT_STRING, Field.FF_NONE));
        
        ent.addField(new FieldImpl(FLD_RESPONSE, Field.FT_STRING, Field.FF_NONE, 1024));        
        
        ent.addField(new FieldImpl(FLD_DB_ID, Field.FT_INTEGER, Field.FF_NONE));
        ent.addField(new FieldImpl(FLD_DS_STATUS, Field.FT_INTEGER, Field.FF_NONE));
        ent.addField(new FieldImpl(FLD_WS_ID, Field.FT_INTEGER, Field.FF_NONE));
        
        ent.addField(new FieldImpl(FLD_DEL_STATUS, Field.FT_INTEGER, Field.FF_NONE));
        ent.addField(new FieldImpl(FLD_PROJECT_ID, Field.FT_INTEGER));        

        // return the entity
        return ent;
    }

    /**
     * @return String the name of the entity that defines this DataObject
     */
    public String getEntityName()
    {
        return ENT_QUERY;
    }


    //--------------------------------------------------------------------------
    // accessors


    /**
     * @return int the id of this query
     */
    public int getId()
    {
        return getIntValue(FLD_ID);
    }

    /**
     * set the id.
     */
    public void setId(Object id)
    {
        setValue(FLD_ID, id);
    }

 	/**
 	 * @return int the id of this project
 	 */
 	public int
 	getProjectId ()
 	{
 		return getIntValue (FLD_PROJECT_ID);
 	}
 	//	--------------------------------------------------------------------------
 	/**
 	 * Set the id of this project
 	 * @param id
 	 */
 	public void
 	setProjectId (
 	 int id)
 	{
 		setValue (FLD_PROJECT_ID, id);
 	}  
    
    /**
     * Returns the name of the query
     * @return String
     */
    public String getName()
    {
        return getStringValue(FLD_NAME);
    }

    /**
     * Sets the query name into the query
     * @param name
     * @return void
     */
    public void setName(String name)
    {
        setValue(FLD_NAME, name);
    }

    /**
     * @return int the id of the parent DataView of this query
     */
    public int getViewOrTableId()
    {
        return getIntValue(FLD_PARENTID);
    }

    /**
     * Set the id of the parent dataview of this query
     * @param id the parent DataView id
     */
    public void setViewOrTableId(int id)
    {
        setValue(FLD_PARENTID, id);
    }
    
	public String getType() {
		return getStringValue(FLD_TYPE);
	}


	public void setType(String fld_type) {
		setValue(FLD_TYPE, fld_type);
	}    

    /**
     * Returns the criteria of the query
     * @return String
     */
    public String getCriteriaString()
    {
        return getStringValue(FLD_CRITERIA);
    }

    /**
     * Sets the criteria into the query
     * @param criteria
     * @return void
     */
    public void setCriteriaString(String criteria)
    {
        setValue(FLD_CRITERIA, criteria);
    }

    /**
     * Returns the sort of the query
     * @return String
     */
    public String getSortString()
    {
        return getStringValue(FLD_SORTS);
    }

    /**
     * Sets the sort string into the query
     * @param sorts
     * @return void
     */
    public void setSortString(String sorts)
    {
        setValue(FLD_SORTS, sorts);
    }

    /**
     * Returns the description of the query
     * @return String
     */
    public String getDescription()
    {
        return getStringValue(FLD_DESCRIPTION);
    }

    /**
     * Sets the query description into the query
     * @param name
     * @return void
     */
    public void setDescription(String name)
    {
        setValue(FLD_DESCRIPTION, name);
    }

    /**
     * @return int the id of the grouping field
     */
    public int getGroupFieldId()
    {
        return getIntValue(FLD_GROUPFIELDID);
    }

    /**
     * Set the id of the grouping field
     * @param id
     */
    public void setGroupFieldId(Integer id)
    {
        setValue(FLD_GROUPFIELDID, id);
    }

    /**
     * Returns the transient DataView of the query
     * @return DataView
     */
    public DataView getDataView()
    {
        // delegate with no load
        return getDataView(false);
    }
    
    
    /**
     * Returns the transient DataView of the query
     * @param doLoad if true loads dv from db
     * @return DataView
     */
    public DataView getDataView(boolean doLoad)
    {
        // if null, load
        if(_dv == null && doLoad && this.getViewOrTableId() >= 0)
        {
            try
            {
                Query q = new Query(DataView.ENT_DATAVIEW);
                q.addQueryCriterium(new QueryCriterium(DataView.ENT_DATAVIEW,
                    DataView.FLD_ID, QueryCriterium.OP_EQUALS,
                    new Integer(this.getViewOrTableId())));
                RecordSet rs = DataManager.getSystemDS().select(q);
                if(rs != null && rs.next())
                    _dv = (DataView)rs.getCurrentObject();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e.toString());
            }
        }

        // return
        return _dv;
    }

    public EntityDobj getTable(boolean doLoad)
    {
        // if null, load
        if(_table == null && doLoad && this.getViewOrTableId() >= 0)
        {
            try
            {
                Query q = new Query(EntityDobj.ENT_ENTITY);
                q.addQueryCriterium(new QueryCriterium(EntityDobj.ENT_ENTITY,
                    DataView.FLD_ID, QueryCriterium.OP_EQUALS,
                    new Integer(this.getViewOrTableId())));
                RecordSet rs = DataManager.getSystemDS().select(q);
                if(rs != null && rs.next())
                	_table = (EntityDobj)rs.getCurrentObject();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e.toString());
            }
        }

        // return
        return _table;
    }   
    
    public String getParentName(){
		String parentName = null;
    	if (getType().equals("insert") || getType().equals("update")){
			EntityDobj table = getTable(true);
			parentName = table.getName();
		}
		else
		{
			DataView view = getDataView(true);
			parentName = view.getName();
		}    	
    	System.out.println("Parent name: " + parentName);
    	return parentName;
    }
    
    /**
     * Sets the transient DataView into the query
     * @param dv
     * @return void
     */
    public void setDataView(DataView dv)
    {
        _dv = dv;
    }


    //--------------------------------------------------------------------------
    // impl

    /**
     * Create and return the Query object
     * @return Query
     */
    public Query createNewImpl()
    {
        // create
        Query impl = new Query();

        // add qcs
        QueryCriterium qc;
        Vector v = this.getCriteria(null);
        for(int i = 0; v != null && i < v.size(); i++)
        {
            // get qc and add
            qc = (QueryCriterium)v.elementAt(i);
            if(qc != null)
                impl.addQueryCriterium(new QueryCriterium(qc));
        }

        // add sorts
        Sort sort;
        v = this.getSorts();
        for(int i = 0; v != null && i < v.size(); i++)
        {
            // get sort and add
            sort = (Sort)v.elementAt(i);
            if(sort != null)
                impl.addSort(new Sort(sort));
        }

        // evaluate functions
        impl.evaluateFunctions();
        return impl;
    }

    //--------------------------------------------------------------------------
    /**
     * Create and return the Query object
     * @return Query
     */
    public Query createImpl()
    {
        return createNewImpl();
    }


    //--------------------------------------------------------------------------
    // criteria

    /**
     * Add a criterium
     * @param qc the criterium
     */
    public void addCriterium(QueryCriterium qc)
    {
        // validate
        Util.argCheckNull(qc);

        // add the qc
        _criteria.add(qc);
    }

    /**
     * Remove a criterium
     */
    public QueryCriterium removeCriterium(QueryCriterium qc, boolean doDelete)
    {
        // if it is indb, remove by key
        QueryCriterium rem = null;
        if(qc != null)
        {
        // try to remove by ref
            if(_criteria.remove(qc))
                rem = qc;
            else
            {
                // iterate
                QueryCriterium temp;
                for(int i = 0; i < _criteria.size(); i++)
                {
                    temp = (QueryCriterium)_criteria.elementAt(i);
                    if(temp != null )
                        rem = (QueryCriterium)_criteria.remove(i);
                }
            }
        }

        // return
        return rem;
    }
	public List<QueryCriteriaDobj> getQueryCriteria(int queryID) throws MdnException
	{
        // build query
        DataSource ds = DataManager.getSystemDS();
        Query q = new Query(QueryCriteriaDobj.ENT_QUERY_CRITERIA);
        q.addQueryCriterium(new QueryCriterium(QueryCriteriaDobj.ENT_QUERY_CRITERIA,
            QueryCriteriaDobj.FLD_QUERY_ID, QueryCriterium.OP_EQUALS,
            new Integer(queryID)));
        RecordSet rs;
		try {
			rs = ds.select(q);
			List<QueryCriteriaDobj>     queryCriteria        = new Vector<QueryCriteriaDobj>();
			while(rs != null && rs.next())
            {
                // get the query
				QueryCriteriaDobj qdobj = (QueryCriteriaDobj)rs.getCurrentObject();
                if(qdobj != null)
                	queryCriteria.add(qdobj);
            }
			return queryCriteria;
		} catch (DataSourceException e) {
			e.printStackTrace();
			throw new MdnException("DataSourceException in getQueryCriteriaByQueryID", e);
		}
	}	
	public QueryCriteriaHistoryDobj getQueryCriteriaHistory(int queryID, int msgID, int originalCriteriaID) throws MdnException
	{
        // build query
        DataSource ds = DataManager.getSystemDS();
        Query q = new Query(QueryCriteriaHistoryDobj.ENT_QUERY_CRITERIA_HISTORY);
        q.addQueryCriterium(new QueryCriterium(QueryCriteriaHistoryDobj.ENT_QUERY_CRITERIA_HISTORY,
        		QueryCriteriaHistoryDobj.FLD_QUERY_ID, QueryCriterium.OP_EQUALS,
        		new Integer(queryID)));
        q.addQueryCriterium(new QueryCriterium(QueryCriteriaHistoryDobj.ENT_QUERY_CRITERIA_HISTORY,
        		QueryCriteriaHistoryDobj.FLD_MSG_ID, QueryCriterium.OP_EQUALS,
        		new Integer(msgID)));
        q.addQueryCriterium(new QueryCriterium(QueryCriteriaHistoryDobj.ENT_QUERY_CRITERIA_HISTORY,
        		QueryCriteriaHistoryDobj.FLD_ORIGINAL_ID, QueryCriterium.OP_EQUALS,
        		new Integer(originalCriteriaID)));        
        RecordSet rs;
		try {
			rs = ds.select(q);
			if(rs != null && rs.next())
            {
                // get the query
				QueryCriteriaHistoryDobj qdobj = (QueryCriteriaHistoryDobj)rs.getCurrentObject();
				return qdobj;
            }
			return null;
		} catch (DataSourceException e) {
			e.printStackTrace();
			throw new MdnException("DataSourceException in getQueryCriteriaHistory", e);
		}
	}	
	/**
     * Get the Vector of QueryCriteriums
     * @return Vector the Query Criteria
     */
    public Vector getCriteria(String msgId)
    {
        // if not loaded, load
        if(!_criteriaLoaded)
        {
            // get the criteria string
        	List<QueryCriteriaDobj> queryCriteria;
			try {
				queryCriteria = getQueryCriteria(this.getId());
				
	        	if(queryCriteria != null && queryCriteria.size() > 0)
	            {
	                
	                for (QueryCriteriaDobj queryCriteriaDobj : queryCriteria)
	                {
	                	if (queryCriteriaDobj.getUsed() == 0){
	                		continue;
	                	}
	                	QueryCriterium qc = null;
	                	if (queryCriteriaDobj.hasUserInputValue() && msgId != null && !msgId.equals("")){
	                		int msgIdInt = Integer.parseInt(msgId);
	                		QueryCriteriaHistoryDobj queryCriteriaHistoryDobj = getQueryCriteriaHistory(queryCriteriaDobj.getQueryId(), msgIdInt, queryCriteriaDobj.getId());
	                		if (queryCriteriaHistoryDobj != null)
	                			qc = new QueryCriterium(getParentName(), queryCriteriaHistoryDobj.getName(), getComparison(queryCriteriaHistoryDobj.getCompId()), queryCriteriaHistoryDobj.getValue());
	                	}
	                	else{
	                		qc = new QueryCriterium(getParentName(), queryCriteriaDobj.getName(), getComparison(queryCriteriaDobj.getCompId()), queryCriteriaDobj.getValue());
	                	}
	                	
	                	if (qc == null){
	                		_criteria = new Vector();
	                		return _criteria;
	                	}
	                	
	                	// parse the qc and add to vector
	                    if (qc != null)
	                        addCriterium(qc);
	                }
	            }

	            // set flag
	            _criteriaLoaded = true;
	            
			} catch (MdnException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }

        // return
        return _criteria;
    }
    /*
    public Vector getCriteria()
    {
        // if not loaded, load
        if(!_criteriaLoaded)
        {
            // get the criteria string
            String strCriteria = getStringValue(FLD_CRITERIA);
            if(strCriteria != null && strCriteria.length() > 0)
            {
                // create tokenizer and parse string criteria
                StringTokenizer strTok = new StringTokenizer(strCriteria, DELIM_AND);

                while(strTok.hasMoreTokens())
                {
                    // get the token
                    String token = strTok.nextToken();

                    // parse the qc and add to vector
                    QueryCriterium qc = parseCriteria(token);
                    if (qc != null)
                        addCriterium(qc);
                }
            }

            // set flag
            _criteriaLoaded = true;
        }

        // return
        return _criteria;
    }    */

    /**
     * Parse a criteria
     * @param str the string criterium
     * @return QueryCriterium
     */
    private QueryCriterium parseCriteria(String str)
    {
        // skip if empty
        if (Util.isEmpty(str))
            return null;

        // create a tokenizer and getting the entity.field, operation and value
        StringTokenizer strTok = new StringTokenizer(str, DELIM_OPS);

        String ef    = "";
        String op    = "";
        String value = null;

        if (strTok.hasMoreTokens())
            ef = strTok.nextToken();
        if (strTok.hasMoreTokens())
            op = strTok.nextToken();
        if (strTok.hasMoreTokens())
            value = strTok.nextToken();

        // check the operator
        if(op.equals(QueryCriterium.OP_EQUALS)
            || op.equals(QueryCriterium.OP_NOT_EQUALS)
            || op.equals(QueryCriterium.OP_LIKE)
            || op.equals(QueryCriterium.OP_NOT_LIKE)
            || op.equals(QueryCriterium.OP_GREATER_THAN_EQUALS)
            || op.equals(QueryCriterium.OP_GREATER_THAN)
            || op.equals(QueryCriterium.OP_LESS_THAN_EQUALS)
            || op.equals(QueryCriterium.OP_LESS_THAN)
            || op.equals(QueryCriterium.OP_IS_NULL)
            || op.equals(QueryCriterium.OP_IS_NOT_NULL))
        {
            // ok
        }
        else
        {
            Log.error("QueryDobj.parseCriteria, Invalid operation: " + op);
            return null;
        }

        // find dot and get entity and field
        ef = ef.trim();
        int pos = ef.indexOf('.');
        if(pos < 0)
        {
            Log.error("QueryDobj.parseCriteria, could not parse ENTITY.FIELD: " + ef);
            return null;
        }
        String entity = ef.substring(0, pos);
        String field = ef.substring(pos + 1);

        // create the qc and set values
        QueryCriterium qc = new QueryCriterium(entity, field, op, value);
        return qc;
    }
    
	public static String getSQLCondition(QueryCriteriaDobj queryCriteriaDobj) {
		String field = null;
		int compID = 0;
		String value = null;
		String value2 = null;
		
		field = queryCriteriaDobj.getName();
		compID = queryCriteriaDobj.getCompId();
		value = queryCriteriaDobj.getValue();
		value2 = queryCriteriaDobj.getValue2();
		if (value.equals("_____")){
			value = "";
		}
		// Deal with the pattern cases for the values.
		switch (compID){
			case 10: 	// starts with.
			case 11:  // does not start with.
				value = value + '%';
				break;
			case 12: 	// contains.
			case 13: 	// does not contain.
				value = '%' + value + '%';
		}
		// Deal with the IS NULL case.
		String thisRow = null;
		if (compID == 7){
			thisRow = "('" + field + "' " + getComparison(compID) + ")";
		} else {
			// Need to cater for BETWEEN and NOT BETWEEN.
			if (compID == 8 || compID == 9){
				String[] valArray = value.split(",");
				String tempVal = "";
				for (int i = 0; i < valArray.length; i++){
					tempVal += valArray[i] + "','";
				}
				//Take the tail away
				if (tempVal.length() > 3)
				{
					tempVal = tempVal.substring(0, tempVal.length() - 3);
					System.out.println("tempVal: " + tempVal);					
				}
				
				value = "('" + tempVal + "')";
				thisRow = "('" + field + "' " + getComparison(compID) + " " + value + ")";
			} else if (compID == 14 || compID == 15){
				if (value2.equals("_____")){
					value2 = "";
				}
				thisRow = "('" + field + "' " + getComparison(compID) + " '" + value + "' AND '" + value2 + "')";
			} else {
				thisRow = "('" + field + "' " + getComparison(compID) + " '" + value + "')";
			}
		}
		switch (compID){
			case 9:	 // is not in list.
			case 11: // does not start with.
			case 13: // does not contain.
				thisRow = "(NOT" + thisRow + ")";
				break;
		}			
		
		return thisRow;
	}   
	
	public static String getSQLCondition(QueryCriteriaDobj queryCriteriaDobj, ArrayList<String> userInputs, int fieldType, String value, String value2) {
		String field = null;
		int compID = 0;
//		String value = null;
//		String value2 = null;
		
		field = queryCriteriaDobj.getName();
		compID = queryCriteriaDobj.getCompId();
/*		value = queryCriteriaDobj.getValue();
		value2 = queryCriteriaDobj.getValue2();
		if (value.equals("_____")){
			value = "";
		}
		if (value2.equals("_____")){
			value2 = "";
		}
		
		//set value from user input
		if (value.equalsIgnoreCase(queryCriteriaDobj.VALUE_USERINPUT) && userInputs.size()>0 ){
			try{
				value = userInputs.get(Integer.parseInt(queryCriteriaDobj.getValueUserInputSeq())-1);
			}catch (Exception e) {
				System.out.println("USERINPUT ERROR : UserInput is wrong ");
			}
		}
		
		//set value2 if value2 is userinput also
		if (value2.equalsIgnoreCase(queryCriteriaDobj.VALUE_USERINPUT) && userInputs.size()>0 ){
			try{
				value2 = userInputs.get(Integer.parseInt(queryCriteriaDobj.getValue2UserInputSeq())-1);
			}catch (Exception e) {
				System.out.println("USERINPUT ERROR : UserInput is wrong ");
			}
		}
*/		
		// Deal with the pattern cases for the values.
		switch (compID){
			case 10: 	// starts with.
			case 11:  // does not start with.
				value = value + '%';
				break;
			case 12: 	// contains.
			case 13: 	// does not contain.
				value = '%' + value + '%';
		}
		// Deal with the IS NULL case.
		String thisRow = null;
		if (compID == 7){
			thisRow = "('" + field + "' " + getComparison(compID) + ")";
		} else {
			// Need to cater for BETWEEN and NOT BETWEEN.
			if (compID == 8 || compID == 9){
				String[] valArray = value.split(",");
				String tempVal = "";
				for (int i = 0; i < valArray.length; i++){
					tempVal += valArray[i] + "','";
				}
				//Take the tail away
				if (tempVal.length() > 3)
				{
					tempVal = tempVal.substring(0, tempVal.length() - 3);
					System.out.println("tempVal: " + tempVal);					
				}
				if (fieldType == Field.FT_STRING)
					value = "('" + tempVal + "')";
				else
					value = "( " + tempVal + " )";
				thisRow = "( " + field + "  " + getComparison(compID) + " " + value + ")";
			} else if (compID == 14 || compID == 15){
				if (fieldType == Field.FT_STRING)
					thisRow = "( " + field + "  " + getComparison(compID) + " '" + value + "' AND '" + value2 + "')";
				else
					thisRow = "( " + field + "  " + getComparison(compID) + "  " + value + "  AND  " + value2 + " )";
			} else {
				if (fieldType == Field.FT_STRING)
					thisRow = "( " + field + "  " + getComparison(compID) + " '" + value + "')";
				else
					thisRow = "( " + field + "  " + getComparison(compID) + "  " + value + " )";
			}
		}
		switch (compID){
			case 9:	 // is not in list.
			case 11: // does not start with.
			case 13: // does not contain.
				thisRow = "(NOT" + thisRow + ")";
				break;
		}			
		
		return thisRow;
	}   	
	private static String getComparison(int compID) {
		String comp = "?";
		switch (compID){
			case 1:
				comp = "=";
				break;
			case 2:
				comp = "<>";
				break;
			case 3:
				comp = "<";
				break;
			case 4:
				comp = "<=";
				break;
			case 5:
				comp = ">";
				break;
			case 6:
				comp = ">=";
				break;
			case 7:
				comp = "IS NULL";
				break;
			case 8:   // is in list.
			case 9:   // is not in list.
				comp = "IN";
				break;
			case 10:	// starts with.
			case 11:  // does not start with.
			case 12:  // contains.
			case 13:  // does not contain.
				comp = "LIKE";
				break;
			case 14:	// between.
				comp = "BETWEEN";
				break;
			case 15:	// is not between.
				comp = "NOT BETWEEN";
				break;
		}
		return comp;
	}
    /**
     * Set criteria vector
     * @param criteria the new criteria Vector
     */
    public void setCriteria(Vector criteria)
    {
        _criteria = criteria;
    }

    /**
     * @return String a string containing all criteria
     */
    public String createCriteriaString()
    {
        // iterate vector
        String ret = "";
        QueryCriterium qc;
        Vector criteria = getCriteria(null);
        for(int i = 0; i < criteria.size(); i++)
        {
            // get the sort
            qc = (QueryCriterium)criteria.elementAt(i);
            if(qc != null)
            {
                if(ret.length() > 0)
                    ret += DELIM_AND;

                // build string
                ret += qc._entityName + "." + qc._fieldName + DELIM_OPS + qc._op;
                if (QueryCriterium.isBinary(qc._op))
                    if(qc._value != null)
                        ret += DELIM_OPS + qc._value.toString();
            }
        }

        // return
        return ret;
    }

    
    /**
     * Clear the criteria
     */
    public void clearCriteria()
    {
        _criteria.clear();
        _criteriaLoaded = false;
    }

    //--------------------------------------------------------------------------
    /**
     * @return true if all the criteria are complete.
     */
    public boolean isComplete(String msgId)
    {
        Vector criteria = getCriteria(msgId);
    	for (int i = 0; criteria != null && i < criteria.size(); i++)
            if (!((QueryCriterium) criteria.get(i)).isComplete())
                return false;
        return true;
    }


    //--------------------------------------------------------------------------
    // sorts

    /**
     * Add a sort
     * @param sort the Sort
     */
    public void addSort(Sort sort)
    {
        // validate
        Util.argCheckNull(sort);

        // add the sort
        _sorts.add(sort);
    }

    /**
     * Remove a sort
     */
    public Sort removeSort(Sort sort, boolean doDelete)
    {
        Sort rem = null;
        if(sort != null)
        {
            // try to remove by ref
            if(_sorts.remove(sort))
                rem = sort;
            else
            {
                // iterate
                Sort temp;
                for(int i = 0; i < _sorts.size(); i++)
                {
                    temp = (Sort)_sorts.elementAt(i);
                    if(temp != null )
                        rem = (Sort)_sorts.remove(i);
                }
            }
        }

        // return
        return rem;
    }

    /**
     * Get the Vector of Sorts
     * @return Vector the Sorts
     */
    public Vector getSorts()
    {
        // if not loaded, load
        if(!_sortsLoaded)
        {
            // get the sorts string
            String strSorts = getStringValue(FLD_SORTS);
            if(strSorts != null && strSorts.length() > 0)
            {
                // parse string sorts
                Sort sort;
                String sub;
                int pos;
                int start = 0;
                while((pos = strSorts.indexOf(",", start)) >= 0)
                {
                    // get the sub string
                    sub = strSorts.substring(start, pos);

                    // parse the sort and add to vector
                    sort = parseSort(sub);
                    addSort(sort);

                    // increment start
                    start = pos + 1;
                }

                // parse the remainder of the string
                if(start < strSorts.length())
                    addSort(parseSort(strSorts.substring(start)));
            }

            // set flag
            _sortsLoaded = true;
        }

        // return
        return _sorts;
    }

    /**
     * Parse a sort
     * @param str the string sort
     * @return Sort
     */
    private Sort parseSort(String str)
    {
        // trim whitespace
        str = str.trim();

        // remove comma
        int pos = str.indexOf(",");
        if(pos >= 0)
            str = str.substring(0, pos);
        str = str.trim();

        // find the direction
        pos = -1;
        String dir = Sort.DIR_ASC;
        if((pos = str.indexOf(Sort.DIR_ASC)) > 0)
            dir = Sort.DIR_ASC;
        else if((pos = str.indexOf(Sort.DIR_DESC)) > 0)
            dir = Sort.DIR_DESC;
        if(pos >= 0)
            str = str.substring(0, pos);

        // get the entity and field
        String ef = str.trim();

        // find dot and get entity and field
        ef = ef.trim();
        pos = ef.indexOf('.');
        if(pos < 0)
            throw new RuntimeException("Unable to parse sort 2");
        String entity = ef.substring(0, pos);
        String field = ef.substring(pos + 1);

        // create the sort and set values
        Sort sort = new Sort(entity, field, dir);
        return sort;
    }

    /**
     * Set sorts vector
     * @param sorts the new sorts Vector
     */
    public void setSorts(Vector sorts)
    {
        _sorts = sorts;
    }

    /**
     * @return String a string containing all sorts
     */
    public String createSortString()
    {
        // iterate vector
        String ret = "";
        Sort s;
        for(int i = 0; i < getSorts().size(); i++)
        {
            // get the sort
            s = (Sort)getSorts().elementAt(i);
            if(s != null)
            {
                if(ret.length() > 0)
                    ret += ", ";
                ret += s._entityName + "." + s._fieldName + " " + s._direction;
            }
        }

        // return
        return ret;
    }


    //--------------------------------------------------------------------------
    // imaging

    /**
     * Start an imaging session
     */
    public void imageQuery()
    {
        // clone
        String str = getStringValue(FLD_CRITERIA);
        if(str != null)
            _imageCriteria = new String(str);
        str = getStringValue(FLD_SORTS);
        if(str != null)
            _imageSorts = new String(str);
        _isImaging = true;
    }

    /**
     * Reverts to the image
     */
    public void revertToImage()
    {
        // must be imaging
        if(isImaging())
        {
            // set the strings back to the image
            setValue(FLD_CRITERIA, _imageCriteria);
            setValue(FLD_SORTS, _imageSorts);

            // clear the criteria and sort vectors
            _criteria.clear();
            _sorts.clear();
            _criteriaLoaded = false;
            _sortsLoaded = false;

            // clear the image
            clearImage();
        }
    }

    /**
     * Process and finish the imaging
     */
    public void processImaging()
    {
        clearImage();
    }

    /**
     * Clear the image
     */
    public void clearImage()
    {
        // clear the image strings
        _imageCriteria = null;
        _imageSorts = null;
        _isImaging = false;
    }

    /**
     * @return boolean true if imaging
     */
    public boolean isImaging()
    {
        return _isImaging;
    }


    //--------------------------------------------------------------------------
    // pre save

    /**
     * Called before a save
     */
    public void preSave() throws DataSourceException
    {
        String str = null;
    	// create the criteria string and set
        //str = createCriteriaString();
        //if(str != null && !str.equals(""))
        //    setValue(FLD_CRITERIA, str);

        // create the sort string and set
        if (getDatasourceStatus() == 1 && getType().equalsIgnoreCase("select")){
	        str = createSortString();
	        if(str != null)
	            setValue(FLD_SORTS, str);
        }
        // clear the impl
        _impl = null;
    }

    //--------------------------------------------------------------------------
    /**
     * Called pre delete call on DataSource.
     * Performa cascading delete.
     */
    protected void preDelete() throws DataSourceException
    {
        // delete all menu actions (QueryRecords actions) that reference this
        DataSource ds = DataManager.getSystemDS();
        Query query = new Query(new QueryCriterium(MenuAction.ENT_MENUACTION,
            MenuAction.FLD_QUERYDOBJID, QueryCriterium.OP_EQUALS,
            new Integer(getId())));
        RecordSet rs = ds.select(query);
        while (rs.next())
        {
            DataObject childMenuAction = rs.getCurrentObject();
            if (childMenuAction != null)
                childMenuAction.delete();
        }
    }

    // ---------------- INSERTED THESE FIELDS FOR MESSAGING INFO ------------------ //    
    /**
     * @return int the EmailAddressId
     */
    public int getEmailAddressId()
    {
        return getIntValue(FLD_EMAIL_ADDRESS_ID);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the EmailAddressId
     * @param emailAddressId
     */
    public void setEmailAddressId(int emailAddressId)
    {
        setValue(FLD_EMAIL_ADDRESS_ID, emailAddressId);
    }
    
    //--------------------------------------------------------------------------    
    /**
     * @return int the EmailAddressId
     */
    public int getMobileStatus()
    {
        return getIntValue(FLD_MOBILE_STATUS);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the MobileStatus
     * @param mobileStatus
     */
    public void setMobileStatus(int mobileStatus)
    {
        setValue(FLD_MOBILE_STATUS, mobileStatus);
    }
    //--------------------------------------------------------------------------
    /**
     * Get the EmailKeyword
     * @return the emailKeyword.
     */
     public String getEmailKeyword()
     {
        return getStringValue(FLD_EMAIL_KEYWORD);
     }
    //--------------------------------------------------------------------------
    /**
    * Set the Emailkeyword.
    * @param emailKeyword
    */
    public void setEmailKeyword(String emailKeyword)
    {
       setValue(FLD_EMAIL_KEYWORD, emailKeyword);
    }
    //--------------------------------------------------------------------------    
    /**
    * Get the smsKeyword
    * @return the smsKeyword.
    */
    public String getSmsKeyword()
    {
         return getStringValue(FLD_SMS_KEYWORD);
    }
    //--------------------------------------------------------------------------
    /**
    * Set the smskeyword.
    * @param smsKeyword
    */
    public void setSmsKeyword(String smsKeyword)
    {
       setValue(FLD_SMS_KEYWORD, smsKeyword);
    }
    //--------------------------------------------------------------------------    

    /**
    * Get the response
    * @return the response.
    */
    public String getResponse()
    {
         return getStringValue(FLD_RESPONSE);
    }
    //--------------------------------------------------------------------------
    /**
    * Set the response.
    * @param response
    */
    public void setResponse(String response)
    {
       setValue(FLD_RESPONSE, response);
    }
    //--------------------------------------------------------------------------    
    
    /**
    * Get the imKeyword
    * @return the imKeyword.
    */
    public String getImKeyword()
    {
       return getStringValue(FLD_IM_KEYWORD);
    }

    //--------------------------------------------------------------------------
    /**
    * Set the imkeyword.
    * @param imKeyword
    */
    public void setImKeyword(String imKeyword)
    {
            setValue(FLD_IM_KEYWORD, imKeyword);
    }
        
    //--------------------------------------------------------------------------    
    /**
     * Get the timeout
     * @return the timeout.
     */
     public String getTimeout()
     {
        return getStringValue(FLD_TIMEOUT);
     }

     //--------------------------------------------------------------------------
     /**
     * Set the timeout.
     * @param timeout
     */
     public void setTimeout(String timeout)
     {
             setValue(FLD_TIMEOUT, timeout);
     }

     //---------------------------------------------------------------------------
    /**
     * @return int the IM
     */
    public int getIMStatus()
    {
        return getIntValue(FLD_IM_STATUS);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the ImStatus
     * @param imStatus
     */
    public void setImStatus(int imStatus)
    {
        setValue(FLD_IM_STATUS, imStatus);
    }
    //--------------------------------------------------------------------------    
    /**
     * @return int the EmailDisplayResult number
     */
    public int getEmailDisplayResult()
    {
        return getIntValue(FLD_EMAIL_DISPLAY_RESULT);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the emailDisplayResult
     * @param emailDisplayResult
     */
    public void setEmailDisplayResult(int emailDisplayResult)
    {
        setValue(FLD_EMAIL_DISPLAY_RESULT, emailDisplayResult);
    }

    //--------------------------------------------------------------------------    
    /**
     * @return int the MobileDisplayResult number
     */
    public int getMobileDisplayResult()
    {
        return getIntValue(FLD_MOBILE_DISPLAY_RESULT);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the mobileDisplayResult
     * @param mobileDisplayResult
     */
    public void setMobileDisplayResult(int mobileDisplayResult)
    {
        setValue(FLD_MOBILE_DISPLAY_RESULT, mobileDisplayResult);
    }
    
    //--------------------------------------------------------------------------    
    /**
     * @return int the imDisplayResult number
     */
    public int getImDisplayResult()
    {
        return getIntValue(FLD_IM_DISPLAY_RESULT);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the imDisplayResult
     * @param imDisplayResult
     */
    public void setImDisplayResult(int imDisplayResult)
    {
        setValue(FLD_IM_DISPLAY_RESULT, imDisplayResult);
    }

    //--------------------------------------------------------------------------
    
    /**
     * Get the conditionSeperator
     * @return the conditionSeperator.
     */
     public String getConditionSeperator()
     {
        return getStringValue(FLD_CONDITION_SEPERATOR);
     }

     //--------------------------------------------------------------------------
     /**
     * Set the conditionSeperator.
     * @param conditionSeperator
     */
     public void setConditionSeperator(String conditionSeperator)
     {
             setValue(FLD_CONDITION_SEPERATOR, conditionSeperator);
     }
         
    //----------------------------------------------------------------------------
    /**
     * @return int the Delete Status
     */
    public int getDelStatus()
    {
        return getIntValue(FLD_DEL_STATUS);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the DeleteStatus
     * @param delStatus
     */
    public void setDelStatus(int delStatus)
    {
        setValue(FLD_DEL_STATUS, delStatus);
    }

    //--------------------------------------------------------------------------    
    /**
     * @return int the databaseId
     */
    public int getDatabaseId()
    {
        return getIntValue(FLD_DB_ID);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the databaseId
     * @param databaseId
     */
    public void setDatabaseId(int database)
    {
        setValue(FLD_DB_ID, database);
    }
    //--------------------------------------------------------------------------    
    /**
     * @return int the datasourceStatus
     */
    public int getDatasourceStatus()
    {
        return getIntValue(FLD_DS_STATUS);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the datasourceStatus
     * @param datasourceStatus
     */
    public void setDatasourceStatus(int datasourceStatus)
    {
        setValue(FLD_DS_STATUS, datasourceStatus);
    }

    //--------------------------------------------------------------------------    
    /**
     * @return int the webServiceId
     */
    public int getWebServiceId()
    {
        return getIntValue(FLD_WS_ID);
    }
    
    //--------------------------------------------------------------------------
    /**
     * Set the webServiceId
     * @param webServiceId
     */
    public void setWebServiceId(int webServiceId)
    {
        setValue(FLD_WS_ID, webServiceId);
    }

}

//==============================================================================
// end of file QueryDobj.java
//==============================================================================

