package com.framedobjects.dashwell.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Element;

import wsl.fw.datasource.DataSourceException;
import wsl.fw.datasource.Field;
import wsl.fw.exception.MdnException;
import wsl.mdn.dataview.DataSourceDobj;
import wsl.mdn.dataview.DataView;
import wsl.mdn.dataview.DataViewField;
import wsl.mdn.dataview.EntityDobj;
import wsl.mdn.dataview.FieldDobj;
import wsl.mdn.dataview.JoinDobj;
import wsl.mdn.dataview.ResultWrapper;

import com.framedobjects.dashwell.biz.ConnectionWizard;
import com.framedobjects.dashwell.db.DataAgentFactory;
import com.framedobjects.dashwell.db.DbConnection;
import com.framedobjects.dashwell.db.IDataAgent;
import com.framedobjects.dashwell.db.meta.MetaField;
import com.framedobjects.dashwell.db.meta.MetaRelation;
import com.framedobjects.dashwell.db.meta.MetaTable;
import com.framedobjects.dashwell.utils.Constants;
import com.framedobjects.dashwell.utils.XmlFormatter;

public class DbDataHandler {
	
	private static Logger logger = Logger.getLogger(DbDataHandler.class.getName());
	
	
	

	
	public ResultWrapper handleNewDbConnection(int projectId, DbConnection dbConn){
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		
		String validateConnResult = validateConnection(projectId, dbConn);
		if (validateConnResult == null){
			//try {
				ResultWrapper result = dataAgent.createNewDbConnection(projectId, dbConn);
				//if (result != null)
				//{
					//return "new db conn set up.";
					return result;
				//}
			/*} catch (MdnException e) {
				e.printStackTrace();
				return "new db conn not set up.";
				//return null;
			}*/
		}
		else
		{
			return new ResultWrapper(null, validateConnResult);
			//return null;
			//return "new db conn not set up.";
		}
		//return "new db conn not set up.";
		//return null;
	}
	/*
	public String handleDbWizard(HttpServletRequest request){
		String result = "";
		HttpSession session = request.getSession();
		ConnectionWizard wizard = (ConnectionWizard)session.getAttribute(Constants.SESSION_CONNECTION_WIZARD);
		String stepString = request.getParameter("step");
		int step = stepString != null ? Integer.parseInt(stepString) : 1;
		switch (step){
			case 1:
				if (wizard == null){
					logger.debug("Create new ConnectionWizard");
					wizard = new ConnectionWizard();
					session.setAttribute(Constants.SESSION_CONNECTION_WIZARD, wizard);
				}
				String type = request.getParameter("type");
				// TODO Need to check whether type is already defined and if so if it is
				// identical and has not changed.
				wizard.setType(type);
				result = "OK";
				break;
			case 2:
				DbConnection dbConn = this.dbConnection(request);
				// Check for duplicate connection name.
				String check = validateConnection(projectId, dbConn);
				if (check != null){
					return check;
				}
				// Need to execute a dbConnection to retrieve the availabe tables.
				logger.debug("+++++++++++++++++++++++++");
				DbSchema schema = DbSchemaFactory.getSchemaForConnection(dbConn);
		    wizard.setDbSchema(schema);
		    wizard.setDbConn(dbConn);
				wizard.initialiseTables();
				result = "new";
				break;
			case 3:
				// Save the imported tables to the session.
				String tables = request.getParameter("tables");
				if (tables != null){
					wizard.resetMetaTables();
					String[] tablesArray = tables.split(",");
					for (String table : tablesArray) {
						wizard.addToMetaTables(table);
					}
				}
				result = "OK";
				break;
			case 4:
				// TODO Save the relationships to the session.
				// TODO Save the entire wizard data to the database.
				break;
		}
		return result;
	}*/
	/**
	 * Validate connection 1. if duplicate name 2. Test connection
	 * @param dbConn DbConnection
	 * @return null means ok
	 */
	private String validateConnection(int projectId, DbConnection dbConn){
		// Check for duplicate connection name.
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		try {
			if (dataAgent.getDbConnectionByName(projectId, dbConn.getName()) != null){
				return "Duplicate Connection Name";
			}
		} catch (MdnException e) {
			e.printStackTrace();
			return e.toString();
		}
		// Check whether we can get a DB connection.
		String testResult = dataAgent.handleTestDbConnection(dbConn); 
		if (!testResult.equals("Connected!")){
			return testResult;
		}
		return null;
	}
	
	public Vector<MetaTable> handleDbWizardTables(HttpServletRequest request){
		Vector<MetaTable> tables = new Vector<MetaTable>();
		// Find the connection wizard.
		ConnectionWizard wizard = (ConnectionWizard)request.getSession().getAttribute(Constants.SESSION_CONNECTION_WIZARD);
		if (wizard != null){
			SortedSet<MetaTable> tablesSet = wizard.getAvailableTables();
			for (MetaTable table : tablesSet) {
				tables.add(table);
			}
		}
		return tables;
	}
	
	public Vector<MetaField> handleDbWizardFields(HttpServletRequest request){
		Vector<MetaField> fields = new Vector<MetaField>();
		String tableName = request.getParameter("table");
		// Find the connection wizard.
		ConnectionWizard wizard = (ConnectionWizard)request.getSession().getAttribute(Constants.SESSION_CONNECTION_WIZARD);
		if (wizard != null){
			ArrayList<MetaField> fieldSet = wizard.getDbSchema().getFieldsForTable(tableName);
			if (fieldSet != null){
				for (MetaField field : fieldSet) {
					fields.add(field);
				}
			}
		}
		return fields;
	}
	
	public String handleAbortWizard(HttpServletRequest request){
		logger.debug("removing connection wizard ..");
		request.getSession().setAttribute(Constants.SESSION_CONNECTION_WIZARD, null);
		return "OK";
	}
	/*
	public String handleFinishWizard(HttpServletRequest request){
		logger.debug("Finishing the connection wizard ..");
		ConnectionWizard wizard = (ConnectionWizard)request.getSession().getAttribute(Constants.SESSION_CONNECTION_WIZARD);
		String step = request.getParameter("step");
		if (step != null){
			if (step.equals("2")){
				DbConnection dbConn = this.dbConnection(request);
				// Check for duplicate connection name.
				String check = validateConnection(dbConn);
				if (check != null){
					return check;
				}
				// Put the connection on the wizard.
				DbSchema schema = DbSchemaFactory.getSchemaForConnection(dbConn);
				wizard.setDbSchema(schema);
				wizard.setDbConn(dbConn);
				wizard.initialiseTables();
			} else if (step.equals("3")){
				// Save the tables to the wizard on the session.
				String tables = request.getParameter("tables");
				if (tables != null){
					wizard.resetMetaTables();
					String[] tablesArray = tables.split(",");
					for (String table : tablesArray) {
						wizard.addToMetaTables(table);
					}
				}
			}
		}
		
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		if (wizard != null){
//			if (dataAgent.createNewDbConnection(wizard) > 0){
//				request.getSession().setAttribute(Constants.SESSION_CONNECTION_WIZARD, null);
//				return "OK";
//			} else {
//				return "Database Error";
//			}
		}
		return "Database Error";
	}*/
	
	public Vector<MetaTable> handleDbImportTables(HttpServletRequest request){
		Vector<MetaTable> tables = new Vector<MetaTable>();
		// Find the connection wizard.
		ConnectionWizard wizard = (ConnectionWizard)request.getSession().getAttribute(Constants.SESSION_CONNECTION_WIZARD);
		if (wizard != null){
			SortedSet<MetaTable> tablesSet = wizard.getMetaTables();
			for (MetaTable table : tablesSet) {
				tables.add(table);
			}
		}
		return tables;
	}
	
	public String handleAddRelation(HttpServletRequest request){
		String result = "";
		String leftTable = request.getParameter("leftTable");
		String leftField = request.getParameter("leftField");
		String rightTable = request.getParameter("rightTable");
		String rightField = request.getParameter("rightField");
		MetaRelation relation = new MetaRelation(0, 0, null, leftTable, leftField,
        rightTable, rightField);
		logger.info("--> Add: " + relation.getQualifiedName());
		// Find the connection wizard.
		ConnectionWizard wizard = (ConnectionWizard)request.getSession().getAttribute(Constants.SESSION_CONNECTION_WIZARD);
		if (wizard != null){
			wizard.getDbSchema().addRelation(relation);
			result = "OK";
		} else {
			result = "Could not find wizard in user session.";
		}
		return result;
	}
	
	public String handleDeleteRelation(HttpServletRequest request){
		String result = "";
		String relation = request.getParameter("relation");
		// Find the connection wizard.
		ConnectionWizard wizard = (ConnectionWizard)request.getSession().getAttribute(Constants.SESSION_CONNECTION_WIZARD);
		if (wizard != null){
	    logger.info("--> remove: " + relation);
	    wizard.getDbSchema().removeRelationByName(relation);
			result = "OK";
		}  else {
			result = "Could not find wizard in user session.";
		}
		return result;
	}
	
	public Vector<MetaRelation> handleGetRelations(HttpServletRequest request){
		Vector<MetaRelation> relations = new Vector<MetaRelation>();
		// Find the connection wizard.
		ConnectionWizard wizard = (ConnectionWizard)request.getSession().getAttribute(Constants.SESSION_CONNECTION_WIZARD);
		if (wizard != null){
	    ArrayList<MetaRelation> relArray = wizard.getDbSchema().getRelations();
	    for (MetaRelation relation : relArray) {
				relations.add(relation);
			}
		}
		return relations;
	}
	
	/*private DbConnection dbConnection(HttpServletRequest request){
	    String idString = request.getParameter("id");
	    int id = idString != null ? Integer.parseInt(idString) : 0;
	    String name = request.getParameter("name");
	    String url = request.getParameter("url");
	    String schema = request.getParameter("schema");
	    String driver = request.getParameter("driver");
	    String username = request.getParameter("username");
	    String password = request.getParameter("password");
	    String mirrorString = request.getParameter("mirrorred");
	    int mirror = 0;
	    if (mirrorString != null){
	    	mirror = mirrorString.equalsIgnoreCase("true") ? 1 : 0;
	    }
	    // Get the DbConnection object.
	    return new DbConnection(id, driver, name, username, password, url, schema, mirror);
	}*/
	
	public Element handleGetTables(int projectId, int connectionID){
		Element root = null;
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		DataSourceDobj conn;
		try {
			conn = dataAgent.getDbConnectionByID(projectId, connectionID);
			if (conn != null){
				List<EntityDobj> tables;
				try {
					tables = dataAgent.getAllMetaTables(connectionID, true);
					root = new XmlFormatter().getTables(conn, tables);
				} catch (MdnException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}			
		} catch (MdnException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return root;
	}
	
	public Element handleGetJoins(int projectId, int connectionID){
		Element root = null;
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		DataSourceDobj conn;
		try {
			conn = dataAgent.getDbConnectionByID(projectId, connectionID);
			if (conn != null){
				List<JoinDobj> joins;
				try {
					joins = dataAgent.getJoins(connectionID);
					root = new XmlFormatter().getJoins(conn, joins);
				} catch (MdnException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}			
		} catch (MdnException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return root;
	}	
	
	public Element handleGetTablesView(int projectId, int connectionID, int viewID){
		Element root = null;
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		DataSourceDobj conn;
		try {
			conn = dataAgent.getDbConnectionByID(projectId, connectionID);
			if (conn != null){
				List<EntityDobj> tables;
				try {
					tables = dataAgent.getAllMetaTables(connectionID, true);
					DataView view = null;
					try {
						if (viewID != 0)
							view = dataAgent.getMetaViewByID(viewID, true);
						root = new XmlFormatter().getTablesView(conn, tables, view);
					} catch (MdnException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (MdnException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
			}
		} catch (MdnException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return root;
	}	
	/**
	 * get all the views based on specified connection ID
	 * @param connectionID
	 * @return Element
	 */
	public Element handleGetViews(int projectId, int connectionID){
		Element root = null;
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		DataSourceDobj conn;
		try {
			conn = dataAgent.getDbConnectionByID(projectId, connectionID);
			if (conn != null){
				List<DataView> views;
				try {
					views = dataAgent.getAllMetaViews(connectionID, true);
					root = new XmlFormatter().getViews(conn, views);
				} catch (MdnException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		} catch (MdnException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return root;
	}	
	
	public EntityDobj getTable(int tableID, boolean includeFields) throws MdnException{
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		EntityDobj table = dataAgent.getMetaTableByID(tableID, includeFields);
		return table;
	}
	/**
	 * get MetaView object from database
	 * @param viewID
	 * @param includeFields
	 * @return MetaView
	 * @throws MdnException 
	 */
	public DataView getView(int viewID, boolean includeFields) throws MdnException{
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		DataView view = dataAgent.getMetaViewByID(viewID, includeFields);
		return view;
	}	
	/**
	 * 
	 * @param table
	 * @return
	 */
	public String editTable(EntityDobj table){
		String result = null;
		// Check for duplicate table name.
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		EntityDobj check = null;
		try {
			check = dataAgent.getMetaTableByName(table.getName(), 
					table.getDataSourceId(), false);
		} catch (MdnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (check != null){
			if (check.getName().equalsIgnoreCase(table.getName()) && 
					check.getId() != table.getId()){
				result = "Duplicate table name";
			} 
		} 
		if (result == null){
			if (dataAgent.editMetaTable(table) > 0){
				result = "OK";
			} else {
				result = "Database error";
			}
		}
		return result;
	}
	
	public Element getField(int fieldID, boolean includeTable) throws MdnException{
		Element root = null;
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		FieldDobj field = dataAgent.getMetaField(fieldID);
		EntityDobj table = null;
		if (field != null){
			table = dataAgent.getMetaTableByID(field.getEntityId(), false);
			root = new XmlFormatter().getFieldResult(field, table);
		}
		return root;
	}
	
	public Element getViewField(String action, String file, String fieldID){
		Element root = null;
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		DataViewField field;
		try {
			int intFieldID = Integer.parseInt(fieldID);
			field = dataAgent.getViewField(intFieldID);
			root = new XmlFormatter().getViewField(field, action, "OK");
		} catch (MdnException e) {
			String result = "Exception: No Field ID.";
		    root = new XmlFormatter().editResult(file, action, result);
		}catch (NumberFormatException e) {
			String result = "Exception: Field ID is invalid.";
		    root = new XmlFormatter().editResult(file, action, result);
		}	

		return root;
	}	
	public Element saveViewField(String action, String fieldID, String fieldDescription, String displayName, boolean namingField, String file){
		Element root = null;
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		DataViewField field;
		String result = "";
		try {
			int intFieldID = Integer.parseInt(fieldID);
			field = dataAgent.getViewField(intFieldID);
			if (field != null){
				field.setDescription(fieldDescription);
				field.setDisplayName(displayName);
				field.setFlags(namingField? Field.FF_NAMING : Field.FF_NONE);
				try {
					field.save();
				} catch (DataSourceException e) {
					result = "DataSourceException: Field is not saved.";
				    root = new XmlFormatter().editResult(file, action, result);
				}
				result = "OK";
				root = new XmlFormatter().getViewField(field, action, result);				
			}else{
				result = "Please select one view field.";
			    root = new XmlFormatter().editResult(file, action, result);				
			}

		} catch (MdnException e) {
			result = "Exception: Field is not saved.";
		    root = new XmlFormatter().editResult(file, action, result);
		}catch (NumberFormatException e) {
			result = "Please select one view field.";
		    root = new XmlFormatter().editResult(file, action, result);
		}	

		return root;
	}
	
	public Element getTableFieldForView(int fieldID, boolean includeTable) throws MdnException{
		Element root = null;
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		FieldDobj field = dataAgent.getMetaField(fieldID);
		EntityDobj table = null;
		if (field != null){
			table = dataAgent.getMetaTableByID(field.getEntityId(), false);
			root = new XmlFormatter().getTableFieldForView(field, table);
		}
		return root;
	}	
	public String handleRecycleConnection(int projectId, int connectionID){
		String result = "";
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		if (dataAgent.recycleConnection(projectId, connectionID) > 0){
			result = "OK";
		} else {
			result = "Database Error";
		}
		return result;
	}
	public String handleClearConnection(int projectId, int connectionID){
		String result = "";
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		if (dataAgent.clearConnection(projectId, connectionID) > 0){
			result = "OK";
		} else {
			result = "Database Error";
		}
		return result;
	}
	public String handleDeleteConnection(int projectId, int connectionID){
		String result = "";
		IDataAgent dataAgent = DataAgentFactory.getDataInterface();
		if (dataAgent.deleteConnection(projectId, connectionID) > 0){
			result = "OK";
		} else {
			result = "Database Error";
		}
		return result;
	}	
}

