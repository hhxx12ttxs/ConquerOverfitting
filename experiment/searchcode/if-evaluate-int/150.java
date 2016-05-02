package com.robaone.api.business;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.robaone.api.business.ROSessionManagerInterface;
import com.robaone.api.business.ROSessionRecordInterface;
import com.robaone.api.data.AppDatabase;
import com.robaone.api.data.DatabaseImpl;
import com.robaone.api.data.SQLInputStream;
import com.robaone.api.data.SessionData;
import com.robaone.api.data.UserManagerInterface;
import com.robaone.api.data.jdo.App_credentials_jdo;
import com.robaone.api.data.jdo.App_credentials_jdoManager;
import com.robaone.api.data.jdo.User_jdo;
import com.robaone.api.data.jdo.User_jdoManager;
import com.robaone.api.json.DSResponse;
import com.robaone.api.json.JSONResponse;
import com.robaone.api.oauth.ROAPIOAuthProvider;
import com.robaone.dbase.ConnectionBlock;
import com.robaone.dbase.HDBConnectionManager;

public abstract class BaseAction<T> {
	private OutputStream out;
	private SessionData session;
	private HttpServletRequest request;
	private DSResponse<T> dsr;
	DocumentBuilderFactory factory;
	public DocumentBuilder builder;
	public XPathFactory xfactory;
	public XPath xpath;
	public static final String QUERY_PACKAGE_NAME = "com.robaone.api.query_package";
	public static String QUERY_PACKAGE = System.getProperty(QUERY_PACKAGE_NAME);
	public final static String RECORD_NOT_FOUND_ERROR = "Record not found";
	public final static String NOT_SUPPORTED = "Not Supported";
	private SQLInputStream in_stream = null;
	protected DatabaseImpl db = new DatabaseImpl();
	private String m_contenttype = "text/plain";
	public void setQueryPackage(String query_package){
		QUERY_PACKAGE = query_package;
	}
	abstract public class FunctionCall{
		@SuppressWarnings("rawtypes")
		protected BaseAction action;
		private String xml;
		abstract protected void run(JSONObject jo) throws Exception;
		public String findXPathString(String xpath) throws Exception{
			return action.findXPathText(xml, xpath);
		}
		public void run(BaseAction<T> action,JSONObject jo){
			try{
				this.action = action;
				action.validate();
				this.xml = XML.toString(jo, "request");
				if(action.requireLogin() == false){
					run(jo);
				}else{
					action.getResponse().setStatus(JSONResponse.LOGIN_REQUIRED);
					action.getResponse().setError("Login Required");
				}
			}catch(Exception e){
				action.sendError(e);
			}
		}
		public void run(BaseAction<T> action,List<FileItem> list){
			try{
				this.action = action;
				action.validate();
				if(action.requireLogin() == false){
					// Process the uploaded items
					Iterator<FileItem> iter = list.iterator();
					JSONObject jo = new JSONObject();
					while (iter.hasNext()) {
						FileItem item = iter.next();
						if (item.isFormField()) {
							jo.put(item.getFieldName(), "FileItem");
						} else {
							jo.put(item.getFieldName(), item.getString());
						}
					}
					run(jo);
				}else{
					action.getResponse().setStatus(JSONResponse.LOGIN_REQUIRED);
					action.getResponse().setError("Login Required");
				}
			}catch(Exception e){
				action.sendError(e);
			}
		}

	}
	protected void setContentType(String contenttype){
		this.m_contenttype =contenttype;
	}
	public String getContentType(){
		return this.m_contenttype;
	}
	public void setSQLStream(SQLInputStream in){
		this.in_stream = in;
	}
	public SQLInputStream getSQLStream(){
		return this.in_stream;
	}
	abstract public class PagedFunctionCall extends FunctionCall {
		private Document query_doc;
		private String m_query_name;
		public PagedFunctionCall(String query_name) {
			m_query_name = query_name;
		}
		public void setQueryDocument(Document doc){
			this.query_doc = doc;
		}
		@Override
		protected void run(JSONObject jo) throws Exception {
			this.buildQueryDoc(m_query_name);
			String filter = this.findXPathString("//filter");
			String page = this.findXPathString("//page");
			String limit = this.findXPathString("//limit");
			int p = 0;
			int lim = 5;
			if(FieldValidator.exists(page) && (!FieldValidator.isNumber(page) || Integer.parseInt(page) < 0)){
				getResponse().setStatus(JSONResponse.FIELD_VALIDATION_ERROR);
				getResponse().addError("page", "You must enter a number greater than or equal to zero");
			}else if(FieldValidator.exists(page)){
				p = Integer.parseInt(page);
			}
			if(FieldValidator.exists(limit) && (!FieldValidator.isNumber(limit) || Integer.parseInt(limit) < 1)){
				getResponse().setStatus(JSONResponse.FIELD_VALIDATION_ERROR);
				getResponse().addError("limit", "You must enter a limit that is greater than zero");
			}else if(FieldValidator.exists(limit)){
				lim = Integer.parseInt(limit);
				lim = lim > 100 ? 100 : lim;
			}
			if(FieldValidator.exists(filter)){
				/**
				 * Search for failed jobs based on the filter
				 */
				if(getResponse().getStatus() == JSONResponse.OK){
					filteredSearch(jo,p,lim,filter);
				}
			}else{
				/**
				 * Search for all failed jobs
				 */
				if(getResponse().getStatus() == JSONResponse.OK){
					unfilteredSearch(jo,p,lim);
				}
			}
		}

		abstract protected void unfilteredSearch(JSONObject jo, int p, int lim) throws Exception;

		abstract protected void filteredSearch(JSONObject jo, int p, int lim, String filter) throws Exception;

		abstract protected void buildQueryDoc(String name) throws Exception;

		protected String getQueryStatement(String name) throws Exception {
			XPathExpression expr = xpath.compile("//ResultSet[@name=\""+name+"\"]//PreparedStatement");
			return (String)expr.evaluate(this.query_doc, XPathConstants.STRING);
		}

		protected Integer getParameterCount(String name) throws Exception {
			String path = "count(//ResultSet[@name=\""+name+"\"]//Parameter)";
			XPathExpression expr = xpath.compile(path);
			return (Integer)expr.evaluate(this.query_doc, XPathConstants.NUMBER);
		}

		protected NodeList getParameters(String name) throws Exception {
			String path = "//ResultSet[@name=\""+name+"\"]//Parameter";
			XPathExpression expr = xpath.compile(path);
			return (NodeList)expr.evaluate(this.query_doc, XPathConstants.NODESET);
		}
		protected int executeUpdate(final JSONObject jo,final int p, final int lim,final String query) throws Exception {
			final Vector<Integer> retval = new Vector<Integer>();
			new ConnectionBlock(){

				@Override
				protected void run() throws Exception {
					setPage(jo, p, lim);
					String query_str = getQueryStatement(query);
					this.prepareStatement(query_str);
					applyParameters(jo,query,getPS());
					int updated = this.executeUpdate();
					retval.add(updated);
				}

			}.run(getConnectionManager());
			return retval.size() > 0 ? retval.get(0) : 0;
		}
		protected void applyParameters(final JSONObject jo,
				final String list_query,PreparedStatement ps) throws Exception, SQLException {
			NodeList parameters = getParameters(list_query);
			for(int i = 0 ; i < parameters.getLength();i++){
				Node attrib = parameters.item(i).getAttributes().getNamedItem("name");
				String name = attrib.getTextContent();
				try{
					Object o = null;
					if(!jo.isNull(name)){
						o = jo.get(name);
					}
					if(name.equals("filter")){
						ps.setString(i+1, "%"+o.toString()+"%");
					}else{
						if(o == null){
							ps.setNull(i+1, getAttributeType(parameters.item(i).getAttributes().getNamedItem("type").getTextContent()));
						}else{
							ps.setObject(i+1, o);
						}
					}
				}catch(JSONException e){
					getResponse().setStatus(JSONResponse.FIELD_VALIDATION_ERROR);
					getResponse().addError(name, ""+e.getMessage());
				}
			}
		}
		protected void getList(final JSONObject jo,final int p ,final int lim,final String list_query,final String count_query) throws Exception {
			new ConnectionBlock(){

				@Override
				protected void run() throws Exception {
					int endindex = setPage(jo, p, lim);
					String query_str = getQueryStatement(list_query);
					this.prepareStatement(query_str);
					applyParameters(jo,list_query,this.getPS());
					this.executeQuery();
					if(getResponse().getStatus() == JSONResponse.OK){
						convert(getResultSet());
						if(count_query != null){
							new ConnectionBlock(){

								@Override
								protected void run() throws Exception {
									String count_str = getQueryStatement(count_query);
									this.prepareStatement(count_str);
									applyParameters(jo,count_query,this.getPS());
									if(getResponse().getStatus() == JSONResponse.OK){
										this.executeQuery();
										if(next()){
											int count = this.getResultSet().getInt(1);
											getResponse().setTotalRows(count);
										}
									}
								}

							}.run(getConnectionManager());
						}
					}
					endindex = (endindex-1) < getResponse().getTotalRows() ? endindex-1 : getResponse().getTotalRows()-1;
					getResponse().setEndRow(endindex);
				}


			}.run(getConnectionManager());

		}
		protected int setPage(final JSONObject jo, final int p,
				final int lim) throws JSONException {
			int startindex = (lim*p) + 1;
			int endindex = startindex + lim -1;
			jo.put("start_index", startindex);
			jo.put("end_index", endindex);
			return endindex;
		}
		protected HDBConnectionManager getConnectionManager() throws Exception {
			return DatabaseImpl.getConnectionManager(DatabaseImpl.SQLSERVERDEV);
		}
	}
	public BaseAction(OutputStream o, SessionData d, HttpServletRequest request) throws ParserConfigurationException{
		this.out = o;
		this.session = d;
		this.request = request;
		QUERY_PACKAGE = AppDatabase.getProperty(QUERY_PACKAGE_NAME);
		factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		builder = factory.newDocumentBuilder();
		xfactory = XPathFactory.newInstance();
		xpath = xfactory.newXPath();
		this.setDSResponse(this.newDSResponse());
	}
	public int getAttributeType(String textContent) {
		if(textContent.equals("string")){
			return java.sql.Types.VARCHAR;
		}else if(textContent.equals("int")){
			return java.sql.Types.INTEGER;
		}else if(textContent.equals("date")){
			return java.sql.Types.DATE;
		}
		return 0;
	}
	abstract protected DSResponse<T> newDSResponse();
	public String findXPathText(String xmldoc,String path) throws SAXException, IOException, XPathExpressionException{
		ByteArrayInputStream bin = new ByteArrayInputStream(xmldoc.getBytes());
		Document doc = builder.parse(bin);
		XPathExpression expr = xpath.compile(path);
		return (String)expr.evaluate(doc, XPathConstants.STRING);
	}
	public NodeList findXPathNode(String xmldoc,String path) throws Exception {
		ByteArrayInputStream bin = new ByteArrayInputStream(xmldoc.getBytes());
		Document doc = builder.parse(bin);
		XPathExpression expr = xpath.compile(path);
		return (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
	}
	public OutputStream getOutputStream(){
		return out;
	}
	public HttpServletRequest getRequest(){
		return this.request;
	}
	public String[] getRequestParameterValues(String name){
		return this.request.getParameterValues(name);
	}
	public String getParameter(String name){
		String[] values = this.getRequestParameterValues(name);
		if(values == null){
			return "";
		}else{
			return values[0];
		}
	}
	public void resetSession() throws Exception{
		SessionData d = new SessionData();
		this.request.getSession().setAttribute("sessiondata", d);
		this.session = d;
	}
	public SessionData getSessionData(){
		return session;
	}
	public void setDSResponse(DSResponse<T> r){
		this.dsr = r;
	}
	public JSONResponse<T> getResponse(){
		return this.dsr.getResponse();
	}
	public boolean requireLogin(){
		SessionData data = this.getSessionData();
		if(data == null){
			this.getResponse().setStatus(JSONResponse.LOGIN_REQUIRED);
			return true;
		}
		boolean loggedin = false;
		try {
			loggedin = data.isAuthorized();
		} catch (Exception e) {
		}
		if(loggedin == false){
			this.getResponse().setStatus(JSONResponse.LOGIN_REQUIRED);
			return true;
		}else{
			return false;
		}
	}
	public void writeResonse() throws JSONException{
		PrintWriter pw = new PrintWriter(this.out);
		JSONObject jo = new JSONObject(this.dsr);
		pw.print(jo.toString(4));
		AppDatabase.writeLog("00011: Response = "+jo.toString());
		pw.flush();
		pw.close();
	}
	public Properties getProperties(){
		return this.dsr.getResponse().getProperties();
	}
	public void validate() throws Exception {
		AppDatabase.writeLog("00012: BaseAction.validate()");
		boolean debug = false;
		try{
			debug = AppDatabase.getProperty("debug").equals("true");
		}catch(Exception e){}
		if(debug) return;
		else{
			try{
				if(this.getSessionData().getUser().getUserId() != null){
					return;
				}
			}catch(Exception e){}
		}
		OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);

		final OAuthAccessor accessor = ROAPIOAuthProvider.getAccessor(requestMessage);
		ROAPIOAuthProvider.VALIDATOR.validateMessage(requestMessage, accessor);
		AppDatabase.writeLog("00013: Request validated");
		// make sure token is authorized
		if (!Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
			OAuthProblemException problem = new OAuthProblemException("permission_denied");
			throw problem;
		}
		/**
		 * Setup the session
		 */
		
		new ConnectionBlock(){

			@Override
			protected void run() throws Exception {
				App_credentials_jdoManager man = new App_credentials_jdoManager(this.getConnection());
				this.setPreparedStatement(man.prepareStatement(App_credentials_jdo.ACCESS_TOKEN + " = ? or "+App_credentials_jdo.REQUEST_TOKEN + " = ?"));
				this.getPreparedStatement().setString(1, accessor.accessToken);
				this.getPreparedStatement().setString(2, accessor.requestToken);
				this.setResultSet(this.getPreparedStatement().executeQuery());
				if(this.getResultSet().next()){
					App_credentials_jdo cred = man.bindApp_credentials(getResultSet());
					getSessionData().getUser().setUserByID(cred.getIduser());
					getSessionData().setCredentials(cred);
					AppDatabase.writeLog("00014: Credentials Saved");
				}
			}

		}.run(DatabaseImpl.getConnectionManager(AppDatabase.getProperty(AppDatabase.DEFAULT_DB_CONTEXT)));
	}
	public void deAuthorize() throws Exception {
		OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
		AppDatabase.writeLog("00015: BaseAction.deAuthorize()");
		final OAuthAccessor accessor = ROAPIOAuthProvider.getAccessor(requestMessage);
		new ConnectionBlock(){

			@Override
			public void run() throws Exception {
				App_credentials_jdoManager man = new App_credentials_jdoManager(this.getConnection());
				this.setPreparedStatement(man.prepareStatement(App_credentials_jdo.ACCESS_TOKEN + " = ? or "+App_credentials_jdo.REQUEST_TOKEN+" = ?"));
				this.getPreparedStatement().setString(1, accessor.accessToken);
				this.getPreparedStatement().setString(2, accessor.requestToken);
				this.setResultSet(this.getPreparedStatement().executeQuery());
				if(this.getResultSet().next()){
					App_credentials_jdo cred = man.bindApp_credentials(getResultSet());
					cred.setActive(0);
					man.save(cred);
					AppDatabase.writeLog("00016: credentials deauthorized");
				}
			}

		}.run(DatabaseImpl.getConnectionManager(AppDatabase.getProperty(AppDatabase.DEFAULT_DB_CONTEXT)));
	}
	public void sendError(Exception e){
		this.getResponse().setStatus(JSONResponse.GENERAL_ERROR);
		this.getResponse().setError(e.getClass().getName()+": "+e.getMessage());
	}
	public void convert(ResultSet rs) throws SQLException {
		int index = 0;
		while(rs.next()){
			convertRecord(rs);
			index++;
		}
		this.getResponse().setEndRow(index > 0 ? index-1 : index);
		this.getResponse().setTotalRows(index);
		this.getResponse().setStartRow(0);
	}
	@SuppressWarnings("unchecked")
	public void convertRecord(ResultSet rs) throws SQLException {
		Map<String, Object> map = rs_to_map(rs);
		JSONObject data = new JSONObject(map);
		this.getResponse().addData((T) data);
	}
	public Map<String, Object> rs_to_map(ResultSet rs) throws SQLException {
		Map<String,Object> map = new HashMap<String,Object>();
		ResultSetMetaData rsmeta = rs.getMetaData();
		for(int i = 0; i < rsmeta.getColumnCount();i++){
			String fieldname = rsmeta.getColumnLabel(i+1);
			Object value = rs.getObject(fieldname);
			if(value instanceof java.math.BigDecimal){
				value = new Double(value.toString());
			}
			if(!fieldname.equalsIgnoreCase("password")){
				map.put(fieldname.toLowerCase(), value);
			}
		}
		return map;
	}
	protected void fieldError(String string, String string2) {
		getResponse().setStatus(JSONResponse.FIELD_VALIDATION_ERROR);
		getResponse().addError(string, string2);
	}
	protected void generalError(String error){
		getResponse().setStatus(JSONResponse.GENERAL_ERROR);
		getResponse().setError(error);
	}
	public void removeReservedFields(JSONObject jo){
		jo.remove("created_by");
		jo.remove("creation_date");
		jo.remove("creation_host");
		jo.remove("modified_by");
		jo.remove("modified_date");
		jo.remove("modification_host");
	}
	public void setCreationFields(JSONObject jo) throws Exception{
		jo.put("created_by", getSessionData().getUser().getIduser());
		jo.put("creation_date", AppDatabase.getTimestamp());
		jo.put("creation_host", getSessionData().getRemoteHost());
	}
	public void setModificationFields(JSONObject jo) throws Exception {
		jo.put("modified_by",getSessionData().getUser().getIduser());
		jo.put("modified_date", AppDatabase.getTimestamp());
		jo.put("modification_host", getSessionData().getRemoteHost());
	}
	protected ROSessionManagerInterface getRecordSessionManager() {
		return new ROSessionManagerInterface(){

			@Override
			public void prepareRecord(ROSessionRecordInterface record) {
				if(record.getCreateddate() == null){
					record.setCreateddate(AppDatabase.getTimestamp());
					record.setCreatedby(getSessionData().getUser().getIduser());
					record.setCreationhost(getSessionData().getRemoteHost());
				}else{
					record.setModifieddate(AppDatabase.getTimestamp());
					record.setModifiedby(getSessionData().getUser().getIduser());
					record.setModificationhost(getSessionData().getRemoteHost());
				}
			}

			@Override
			public void cleanJSON(JSONObject jo) {
				@SuppressWarnings("unchecked")
				Iterator<String> it = jo.keys();
				HashMap<String,Boolean> restricted = new HashMap<String,Boolean>();
				restricted.put("createdby", new Boolean(true));
				restricted.put("createddate", new Boolean(true));
				restricted.put("creationhost", new Boolean(true));
				restricted.put("modifiedby", new Boolean(true));
				restricted.put("modifieddate", new Boolean(true));
				restricted.put("modificationhost", new Boolean(true));
				restricted.put("_void", new Boolean(true));
				restricted.put("_lock", new Boolean(true));
				restricted.put("_lockowner", new Boolean(true));
				while(it.hasNext()){
					String key = it.next();
					if(restricted.get(key) != null){
						jo.remove(key);
					}
				}
			}

			@Override
			public boolean lockRecord(ROSessionRecordInterface record) {
				try{
					if(record.get_lock() == null || 
							(new java.util.Date().getTime() - record.get_lock().getTime()) > (100*60)){
						lock(record);
						return true;
					}else if(record.get_lock() != null && record.get_lockowner() == getSessionData().getUser().getIduser()){
						lock(record);
						return true;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				return false;
			}

			private void lock(ROSessionRecordInterface record) {
				record.set_lock(AppDatabase.getTimestamp());
				record.set_lockowner(getSessionData().getUser().getIduser());
			}

			@Override
			public boolean isLocked(ROSessionRecordInterface record) {
				if(record.get_lockowner() != null && record.get_lockowner() == getSessionData().getUser().getIduser()){
					return false;
				}else if(record.get_lock() == null){
					return false;
				}else if((new java.util.Date().getTime() - record.get_lock().getTime()) > (100*60)){
					return false;
				}
				return true;
			}

		};
	}	
}

